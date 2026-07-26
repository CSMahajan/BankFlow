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

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AccountDashboardSummary getDashboardSummary(String accountNumber) {
        Account account = getAuthorizedAccount(accountNumber);

        List<TransactionResponse> recent10 = transactionRepository
                .findTop10ByAccountIdOrderByTransactionDateDesc(account.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();

        BigDecimal totalCredit = transactionRepository.sumAmountByAccountIdAndTransactionType(account.getId(), TransactionType.CREDIT);
        BigDecimal totalDebit = transactionRepository.sumAmountByAccountIdAndTransactionType(account.getId(), TransactionType.DEBIT);

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
        Account account = getAuthorizedAccount(accountNumber);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return transactionRepository
                .findByAccountIdAndTransactionDateBetweenOrderByTransactionDateDesc(account.getId(), startDateTime, endDateTime)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactionsForAdmin(String accountNumber) {
        // Explicit admin route to search any customer's account transactions
        return transactionRepository.findByAccountAccountNumberOrderByTransactionDateDesc(accountNumber)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Security Helper: Enforces that regular customers can only view their own account activity
    private Account getAuthorizedAccount(String accountNumber) {
        User currentUser = getAuthenticatedUser();
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!currentUser.getRole().equals(User.Role.ADMIN) && !account.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to view transactions for this account");
        }

        return account;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
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
