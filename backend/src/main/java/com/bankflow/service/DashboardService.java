package com.bankflow.service;

import com.bankflow.dto.DashboardSummaryResponse;
import com.bankflow.dto.MonthlyAnalyticsResponse;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.entity.Account;
import com.bankflow.entity.FixedDeposit;
import com.bankflow.entity.Loan;
import com.bankflow.entity.Transaction;
import com.bankflow.entity.Transaction.TransactionType;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.FixedDepositRepository;
import com.bankflow.repository.LoanRepository;
import com.bankflow.repository.TransactionRepository;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

        // 1. Account Balances
        List<Account> accounts = accountRepository.findByUserId(currentUser.getId());
        List<Long> accountIds = accounts.stream().map(Account::getId).toList();

        BigDecimal totalAccountBalance = accounts.stream()
                .map(Account::getCurrentBalance)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Fixed Deposit Investments
        List<FixedDeposit> activeFds = fdRepository.findByUserId(currentUser.getId()).stream()
                .filter(fd -> fd.getStatus() == FixedDeposit.FdStatus.ACTIVE)
                .toList();

        BigDecimal totalFdInvestment = activeFds.stream()
                .map(FixedDeposit::getDepositAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Loans & Next EMI Calculation
        List<Loan> activeLoans = loanRepository.findByUserId(currentUser.getId()).stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE)
                .toList();

        BigDecimal totalOutstandingLoans = activeLoans.stream()
                .map(Loan::getRemainingBalance)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Loan nextEmiLoan = activeLoans.stream()
                .filter(l -> Objects.nonNull(l.getNextDueDate()))
                .min(Comparator.comparing(Loan::getNextDueDate))
                .orElse(null);

        LocalDate nextEmiDueDate = (nextEmiLoan != null) ? nextEmiLoan.getNextDueDate() : null;
        BigDecimal nextEmiAmount = (nextEmiLoan != null) ? nextEmiLoan.getMonthlyEmi() : null;

        // 4. Calculate Net Worth
        BigDecimal totalNetWorth = totalAccountBalance.add(totalFdInvestment);

        // 5. Fetch Top 5 Recent Transactions
        List<TransactionResponse> recentTransactions = List.of();
        if (!accountIds.isEmpty()) {
            recentTransactions = transactionRepository
                    .findByAccountIdInOrderByTransactionDateDesc(
                            accountIds,
                            PageRequest.of(0, 5)
                    )
                    .stream()
                    .map(this::mapToTransactionResponse)
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

    @Transactional(readOnly = true)
    public MonthlyAnalyticsResponse getCurrentMonthAnalytics() {
        User currentUser = getAuthenticatedUser();
        List<Long> accountIds = getUserAccountIds(currentUser.getId());

        if (accountIds.isEmpty()) {
            return new MonthlyAnalyticsResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal income = transactionRepository.sumAmountByAccountIdsAndTypeAndDateRange(
                accountIds, TransactionType.CREDIT, startOfMonth, endOfMonth);

        BigDecimal expense = transactionRepository.sumAmountByAccountIdsAndTypeAndDateRange(
                accountIds, TransactionType.DEBIT, startOfMonth, endOfMonth);

        BigDecimal netCashFlow = income.subtract(expense);

        return new MonthlyAnalyticsResponse(income, expense, netCashFlow);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getDashboardTransactions(int page, int size) {
        User currentUser = getAuthenticatedUser();
        List<Long> accountIds = getUserAccountIds(currentUser.getId());

        if (accountIds.isEmpty()) {
            return Page.empty();
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        return transactionRepository.findByAccountIdInOrderByTransactionDateDesc(accountIds, pageable)
                .map(this::mapToTransactionResponse);
    }

    private List<Long> getUserAccountIds(Long userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(Account::getId)
                .toList();
    }

    private TransactionResponse mapToTransactionResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getTransactionId(),
                tx.getAccount().getAccountNumber(),
                tx.getTransactionDate(),
                tx.getTransactionType(),
                tx.getAmount(),
                tx.getAvailableBalance(),
                tx.getDescription()
        );
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found: " + auth.getName()));
    }
}
