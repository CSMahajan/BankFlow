package com.bankflow.service;

import com.bankflow.dto.DashboardSummaryResponse;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.entity.Account;
import com.bankflow.entity.FixedDeposit;
import com.bankflow.entity.Loan;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.FixedDepositRepository;
import com.bankflow.repository.LoanRepository;
import com.bankflow.repository.TransactionRepository;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AccountRepository accountRepository;
    private final FixedDepositRepository fdRepository;
    private final LoanRepository loanRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        User currentUser = getAuthenticatedUser();
        log.info("Fetching aggregated dashboard summary for user [{}]", currentUser.getEmail());

        // 1. Fetch user's active accounts
        List<Account> accounts = accountRepository.findByUserId(currentUser.getId());
        BigDecimal totalAccountBalance = accounts.stream()
                .map(Account::getCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Fetch user's fixed deposits
        List<FixedDeposit> fds = fdRepository.findByUserId(currentUser.getId());
        List<FixedDeposit> activeFds = fds.stream()
                .filter(fd -> fd.getStatus() == FixedDeposit.FdStatus.ACTIVE)
                .toList();

        BigDecimal totalFdInvestment = activeFds.stream()
                .map(FixedDeposit::getDepositAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Fetch user's active loans
        List<Loan> loans = loanRepository.findByUserId(currentUser.getId());
        List<Loan> activeLoans = loans.stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE)
                .toList();

        BigDecimal totalOutstandingLoans = activeLoans.stream()
                .map(Loan::getRemainingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Find upcoming EMI due date & amount
        Loan nextEmiLoan = activeLoans.stream()
                .filter(l -> Objects.nonNull(l.getNextDueDate()))
                .min(Comparator.comparing(Loan::getNextDueDate))
                .orElse(null);

        LocalDate nextEmiDueDate = (nextEmiLoan != null) ? nextEmiLoan.getNextDueDate() : null;
        BigDecimal nextEmiAmount = (nextEmiLoan != null) ? nextEmiLoan.getMonthlyEmi() : null;

        // 4. Calculate Net Worth (Liquid Accounts + Investments)
        BigDecimal totalNetWorth = totalAccountBalance.add(totalFdInvestment);

        // 5. Fetch recent 5 transactions across user's accounts
        List<Long> accountIds = accounts.stream().map(Account::getId).toList();
        List<TransactionResponse> recentTransactions = List.of();

        if (!accountIds.isEmpty()) {
            recentTransactions = transactionRepository.findByAccountIdInOrderByTransactionDateDesc(
                            accountIds, PageRequest.of(0, 5))
                    .stream()
                    .map(tx -> new TransactionResponse(
                            tx.getTransactionId(),
                            tx.getAccount().getAccountNumber(),
                            tx.getTransactionDate(),
                            tx.getTransactionType(),
                            tx.getAmount(),
                            tx.getAvailableBalance(),
                            tx.getDescription()
                    ))
                    .toList();
        }



        return new DashboardSummaryResponse(
                currentUser.getFullName(),
                totalNetWorth,
                totalAccountBalance,
                totalFdInvestment,
                totalOutstandingLoans,
                accounts.size(),
                activeFds.size(),
                activeLoans.size(),
                nextEmiDueDate,
                nextEmiAmount,
                recentTransactions
        );
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }
}
