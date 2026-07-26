package com.bankflow.service;

import com.bankflow.dto.FundTransferRequest;
import com.bankflow.dto.FundTransferResponse;
import com.bankflow.entity.Account;
import com.bankflow.entity.Transaction;
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
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FundTransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public FundTransferResponse transferFunds(FundTransferRequest request) {
        User currentUser = getAuthenticatedUser();

        // 1. Validate distinct accounts
        if (request.sourceAccountNumber().equalsIgnoreCase(request.targetAccountNumber())) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }

        // 2. Fetch & Validate Source Account
        Account sourceAccount = accountRepository.findByAccountNumber(request.sourceAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));

        if (!sourceAccount.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to transfer funds from this account");
        }

        if (sourceAccount.getAccountStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Source account is not active");
        }

        if (sourceAccount.getCurrentBalance().compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds in source account");
        }

        // 3. Fetch & Validate Target Account
        Account targetAccount = accountRepository.findByAccountNumber(request.targetAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        if (targetAccount.getAccountStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Destination account is inactive or frozen");
        }

        // 4. Update Balances
        BigDecimal newSourceBalance = sourceAccount.getCurrentBalance().subtract(request.amount());
        BigDecimal newTargetBalance = targetAccount.getCurrentBalance().add(request.amount());

        sourceAccount.setCurrentBalance(newSourceBalance);
        targetAccount.setCurrentBalance(newTargetBalance);

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        // 5. Create Transaction Audit Records
        String txRef = "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String remark = request.remark() != null && !request.remark().isBlank() ? " | " + request.remark() : "";

        Transaction debitTx = Transaction.builder()
                .transactionId(txRef + "-D")
                .account(sourceAccount)
                .transactionType(Transaction.TransactionType.DEBIT)
                .amount(request.amount())
                .availableBalance(newSourceBalance)
                .description("Transfer to " + targetAccount.getAccountNumber() + remark)
                .build();

        Transaction creditTx = Transaction.builder()
                .transactionId(txRef + "-C")
                .account(targetAccount)
                .transactionType(Transaction.TransactionType.CREDIT)
                .amount(request.amount())
                .availableBalance(newTargetBalance)
                .description("Transfer from " + sourceAccount.getAccountNumber() + remark)
                .build();

        transactionRepository.save(debitTx);
        transactionRepository.save(creditTx);

        return new FundTransferResponse(
                txRef,
                sourceAccount.getAccountNumber(),
                targetAccount.getAccountNumber(),
                request.amount(),
                newSourceBalance,
                "SUCCESS",
                LocalDateTime.now()
        );
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }
}
