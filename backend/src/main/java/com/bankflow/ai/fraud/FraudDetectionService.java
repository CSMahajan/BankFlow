package com.bankflow.ai.fraud;

import com.bankflow.entity.Account;
import com.bankflow.entity.FixedDeposit;
import com.bankflow.entity.Loan;
import com.bankflow.entity.Transaction;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.FixedDepositRepository;
import com.bankflow.repository.LoanRepository;
import com.bankflow.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private static final long RAPID_ACCOUNT_WINDOW_MINUTES = 10;

    private static final long RAPID_LOAN_WINDOW_HOURS = 24;

    private static final long PREMATURE_FD_WINDOW_DAYS = 3;

    private static final BigDecimal LARGE_INITIAL_DEPOSIT_THRESHOLD =
            new BigDecimal("1000000");

    private static final BigDecimal HIGH_BALANCE_THRESHOLD =
            new BigDecimal("10000000");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;
    private final FixedDepositRepository fixedDepositRepository;

    @Transactional(readOnly = true)
    public FraudAssessment assessUser(Long userId) {

        List<FraudSignal> signals = new ArrayList<>();

        List<Account> accounts = accountRepository.findByUserId(userId);
        List<Loan> loans = loanRepository.findByUserId(userId);
        List<FixedDeposit> fixedDeposits =
                fixedDepositRepository.findByUserId(userId);

        detectRapidAccountCreation(accounts, signals);

        detectLargeInitialDeposits(accounts, signals);

        detectRapidLoanAfterAccountCreation(
                accounts,
                loans,
                signals
        );

        detectPrematureFdChurn(
                fixedDeposits,
                signals
        );

        detectOverdueLoanWithHighBalance(
                accounts,
                loans,
                signals
        );

        return new FraudAssessment(
                userId,
                List.copyOf(signals)
        );
    }

    private void detectRapidAccountCreation(
            List<Account> accounts,
            List<FraudSignal> signals) {

        if (accounts.size() < 2) {
            return;
        }

        List<Account> sortedAccounts = accounts.stream()
                .sorted(Comparator.comparing(Account::getCreatedAt))
                .toList();

        List<String> rapidAccounts = new ArrayList<>();

        for (int i = 1; i < sortedAccounts.size(); i++) {

            Account previous = sortedAccounts.get(i - 1);
            Account current = sortedAccounts.get(i);

            long minutes = ChronoUnit.MINUTES.between(
                    previous.getCreatedAt(),
                    current.getCreatedAt()
            );

            if (minutes <= RAPID_ACCOUNT_WINDOW_MINUTES) {
                rapidAccounts.add(
                        current.getAccountNumber()
                                + " created "
                                + minutes
                                + " minutes after "
                                + previous.getAccountNumber()
                );
            }
        }

        if (!rapidAccounts.isEmpty()) {

            signals.add(new FraudSignal(
                    FraudSignalType.RAPID_ACCOUNT_CREATION,
                    "Multiple accounts were created within a short time window: "
                            + String.join("; ", rapidAccounts)
            ));
        }
    }

    private void detectLargeInitialDeposits(
            List<Account> accounts,
            List<FraudSignal> signals) {

        BigDecimal totalInitialDeposits = BigDecimal.ZERO;
        List<String> evidence = new ArrayList<>();

        for (Account account : accounts) {

            List<Transaction> transactions =
                    transactionRepository
                            .findByAccountAccountNumberOrderByTransactionDateDesc(
                                    account.getAccountNumber()
                            );

            for (Transaction transaction : transactions) {

                if (transaction.getTransactionType()
                        != Transaction.TransactionType.CREDIT) {
                    continue;
                }

                String description = transaction.getDescription();

                if (description == null) {
                    continue;
                }

                if (!description
                        .trim()
                        .equalsIgnoreCase("Initial Account Opening Deposit")) {
                    continue;
                }

                if (transaction.getAmount()
                        .compareTo(LARGE_INITIAL_DEPOSIT_THRESHOLD) < 0) {
                    continue;
                }

                totalInitialDeposits =
                        totalInitialDeposits.add(transaction.getAmount());

                evidence.add(
                        "Account "
                                + account.getAccountNumber()
                                + " received ₹"
                                + transaction.getAmount()
                                + " as an initial account opening deposit"
                );
            }
        }

        if (!evidence.isEmpty()) {

            signals.add(new FraudSignal(
                    FraudSignalType.LARGE_INITIAL_DEPOSIT,
                    "Large initial account-opening deposits totaling ₹"
                            + totalInitialDeposits
                            + ". "
                            + String.join("; ", evidence)
            ));
        }
    }

    private void detectRapidLoanAfterAccountCreation(
            List<Account> accounts,
            List<Loan> loans,
            List<FraudSignal> signals) {

        List<String> evidence = new ArrayList<>();

        for (Loan loan : loans) {

            if (loan.getCreatedAt() == null) {
                continue;
            }

            Account disbursementAccount =
                    loan.getDisbursementAccount();

            if (disbursementAccount == null
                    || disbursementAccount.getCreatedAt() == null) {
                continue;
            }

            LocalDateTime accountCreatedAt =
                    disbursementAccount.getCreatedAt();

            LocalDateTime loanCreatedAt =
                    loan.getCreatedAt();

            if (loanCreatedAt.isBefore(accountCreatedAt)) {
                continue;
            }

            long hours = ChronoUnit.HOURS.between(
                    accountCreatedAt,
                    loanCreatedAt
            );

            if (hours <= RAPID_LOAN_WINDOW_HOURS) {

                evidence.add(
                        "Loan "
                                + loan.getLoanNumber()
                                + " for ₹"
                                + loan.getPrincipalAmount()
                                + " was created "
                                + hours
                                + " hours after account "
                                + disbursementAccount.getAccountNumber()
                                + " was created"
                );
            }
        }

        if (!evidence.isEmpty()) {

            signals.add(new FraudSignal(
                    FraudSignalType.RAPID_LOAN_AFTER_ACCOUNT_CREATION,
                    String.join("; ", evidence)
            ));
        }
    }

    private void detectPrematureFdChurn(
            List<FixedDeposit> fixedDeposits,
            List<FraudSignal> signals) {

        List<FixedDeposit> churnedFds = fixedDeposits.stream()
                .filter(fd -> fd.getStatus() == FixedDeposit.FdStatus.PREMATURELY_CLOSED)
                .filter(fd -> fd.getCreatedAt() != null)
                .filter(fd -> fd.getClosedDate() != null)
                .filter(fd -> {
                    long daysBetween = ChronoUnit.DAYS.between(
                            fd.getCreatedAt().toLocalDate(),
                            fd.getClosedDate()
                    );

                    return daysBetween <= PREMATURE_FD_WINDOW_DAYS;
                })
                .toList();

        if (churnedFds.isEmpty()) {
            return;
        }

        String description = churnedFds.stream()
                .map(fd -> {
                    long daysBetween = ChronoUnit.DAYS.between(
                            fd.getCreatedAt().toLocalDate(),
                            fd.getClosedDate()
                    );

                    return String.format(
                            "%s for ₹%.2f was prematurely closed %d day(s) after creation",
                            fd.getFdNumber(),
                            fd.getDepositAmount(),
                            daysBetween
                    );
                })
                .collect(Collectors.joining("; "));

        signals.add(new FraudSignal(
                FraudSignalType.PREMATURE_FD_CHURN,
                "Multiple fixed deposits were prematurely closed shortly after creation: "
                        + description
        ));
    }

    private void detectOverdueLoanWithHighBalance(
            List<Account> accounts,
            List<Loan> loans,
            List<FraudSignal> signals) {

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getCurrentBalance)
                .filter(balance -> balance != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalBalance.compareTo(HIGH_BALANCE_THRESHOLD) < 0) {
            return;
        }

        LocalDate today = LocalDate.now();

        List<String> evidence = new ArrayList<>();

        for (Loan loan : loans) {

            if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
                continue;
            }

            if (loan.getNextDueDate() == null
                    || loan.getRemainingBalance() == null) {
                continue;
            }

            if (!loan.getNextDueDate().isBefore(today)) {
                continue;
            }

            if (loan.getRemainingBalance()
                    .compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            evidence.add(
                    "Loan "
                            + loan.getLoanNumber()
                            + " is overdue since "
                            + loan.getNextDueDate()
                            + " with ₹"
                            + loan.getRemainingBalance()
                            + " remaining while total account balance is ₹"
                            + totalBalance
            );
        }

        if (!evidence.isEmpty()) {

            signals.add(new FraudSignal(
                    FraudSignalType.OVERDUE_LOAN_WITH_HIGH_BALANCE,
                    String.join("; ", evidence)
            ));
        }
    }
}