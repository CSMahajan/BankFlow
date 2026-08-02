package com.bankflow.service;

import com.bankflow.dto.AccountDashboardSummary;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.entity.Account;
import com.bankflow.entity.Transaction;
import com.bankflow.entity.Transaction.TransactionType;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.TransactionRepository;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AccountDashboardSummary getDashboardSummary(String accountNumber) {
        log.debug("Generating dashboard summary for account [{}]", accountNumber);
        Account account = getAuthorizedAccount(accountNumber);

        List<TransactionResponse> recent10 = transactionRepository
                .findTop10ByAccountIdOrderByTransactionDateDesc(account.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();

        BigDecimal totalCredit = transactionRepository.sumAmountByAccountIdAndTransactionType(account.getId(), TransactionType.CREDIT);
        BigDecimal totalDebit = transactionRepository.sumAmountByAccountIdAndTransactionType(account.getId(), TransactionType.DEBIT);

        // Safely handle null totals when no transactions exist yet
        totalCredit = totalCredit != null ? totalCredit : BigDecimal.ZERO;
        totalDebit = totalDebit != null ? totalDebit : BigDecimal.ZERO;

        log.info("Dashboard summary generated for account [{}]. Balance: [Rs. {}], Recent Tx Count: [{}]",
                accountNumber, account.getCurrentBalance(), recent10.size());

        return new AccountDashboardSummary(
                account.getAccountNumber(),
                account.getCurrentBalance(),
                totalCredit,
                totalDebit,
                recent10
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByDateRange(String accountNumber, LocalDate startDate, LocalDate endDate) {
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        log.debug("Fetching transactions for account [{}] between [{}] and [{}]", accountNumber, startDate, endDate);
        Account account = getAuthorizedAccount(accountNumber);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<TransactionResponse> transactions = transactionRepository
                .findByAccountIdAndTransactionDateBetweenOrderByTransactionDateDesc(account.getId(), startDateTime, endDateTime)
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Retrieved [{}] transactions for account [{}] in specified date range", transactions.size(), accountNumber);
        return transactions;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getMyTransactions(TransactionType type, Pageable pageable) {
        User currentUser = getAuthenticatedUser();

        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Maximum page size is 100");
        }

        log.info("Fetching transaction history for user [{}], filter [{}], page [{}]",
                currentUser.getEmail(), type == null ? "ALL" : type, pageable.getPageNumber()
        );
        Page<Transaction> page;

        if (type == null) {
            page = transactionRepository.
                    findByAccountUserIdOrderByTransactionDateDesc(currentUser.getId(), pageable);
        } else {
            page = transactionRepository
                    .findByAccountUserIdAndTransactionTypeOrderByTransactionDateDesc(currentUser.getId(), type, pageable);
        }

        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactionsForAdmin(String accountNumber) {
        User currentUser = getAuthenticatedUser();
        log.info("ADMIN action: User [{}] requesting full transaction history for account [{}]",
                currentUser.getEmail(), accountNumber);

        List<TransactionResponse> transactions = transactionRepository
                .findByAccountAccountNumberOrderByTransactionDateDesc(accountNumber)
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("ADMIN lookup completed. Retrieved [{}] transactions for account [{}]", transactions.size(), accountNumber);
        return transactions;
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionDetails(String transactionId) {

        User currentUser = getAuthenticatedUser();

        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Transaction not found"));

        if (!currentUser.getRole().equals(User.Role.ADMIN)
                && !transaction.getAccount().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to view this transaction");
        }

        log.info("Fetched transaction [{}] for user [{}]", transactionId, currentUser.getEmail());

        return mapToResponse(transaction);
    }

    // Security Helper: Enforces that regular customers can only view their own account activity
    private Account getAuthorizedAccount(String accountNumber) {
        User currentUser = getAuthenticatedUser();
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("Transaction service error: Account [{}] not found", accountNumber);
                    return new IllegalArgumentException("Account not found");
                });

        if (!currentUser.getRole().equals(User.Role.ADMIN) && !account.getUser().getId().equals(currentUser.getId())) {
            log.warn("Security Alert: User [{}] attempted unauthorized access to transactions of account [{}]",
                    currentUser.getEmail(), accountNumber);
            throw new AccessDeniedException("You are not authorized to view transactions for this account");
        }

        return account;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> {
                    log.error("Authentication Context Error: User [{}] not found in database", auth.getName());
                    return new IllegalArgumentException("Authenticated user not found");
                });
    }

    private TransactionResponse mapToResponse(Transaction tx) {
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
}
