package com.bankflow.service;

import com.bankflow.dto.FundTransferRequest;
import com.bankflow.dto.FundTransferResponse;
import com.bankflow.entity.Account;
import com.bankflow.entity.AuditAction;
import com.bankflow.entity.Transaction;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.TransactionRepository;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundTransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public FundTransferResponse transferFunds(FundTransferRequest request) {
        User currentUser = getAuthenticatedUser();

        log.info("Initiating fund transfer request by user [{}] from account [{}] to target [{}] for amount [Rs. {}]",
                currentUser.getEmail(), request.sourceAccountNumber(), request.targetAccountNumber(), request.amount());

        if (request.sourceAccountNumber().equalsIgnoreCase(request.targetAccountNumber())) {
            log.warn("Transfer failed: Source and target accounts are identical [{}]", request.sourceAccountNumber());
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }

        Account sourceAccount = accountRepository.findByAccountNumber(request.sourceAccountNumber())
                .orElseThrow(() -> {
                    log.error("Source account [{}] not found", request.sourceAccountNumber());
                    return new IllegalArgumentException("Source account not found");
                });

        if (!sourceAccount.getUser().getId().equals(currentUser.getId())) {
            log.warn("Security Alert: User [{}] attempted unauthorized transfer from account [{}] owned by User ID [{}]",
                    currentUser.getEmail(), sourceAccount.getAccountNumber(), sourceAccount.getUser().getId());
            throw new AccessDeniedException("You are not authorized to transfer funds from this account");
        }

        if (sourceAccount.getAccountStatus() != Account.AccountStatus.ACTIVE) {
            log.warn("Transfer failed: Source account [{}] is in state [{}]",
                    sourceAccount.getAccountNumber(), sourceAccount.getAccountStatus());
            throw new IllegalArgumentException("Source account is not active");
        }

        if (sourceAccount.getCurrentBalance().compareTo(request.amount()) < 0) {
            log.warn("Transfer failed: Insufficient balance in account [{}]. Current: {}, Requested: {}",
                    sourceAccount.getAccountNumber(), sourceAccount.getCurrentBalance(), request.amount());
            throw new IllegalArgumentException("Insufficient funds in source account");
        }

        Account targetAccount = accountRepository.findByAccountNumber(request.targetAccountNumber())
                .orElseThrow(() -> {
                    log.error("Destination account [{}] not found", request.targetAccountNumber());
                    return new IllegalArgumentException("Destination account not found");
                });

        if (targetAccount.getAccountStatus() != Account.AccountStatus.ACTIVE) {
            log.warn("Transfer failed: Destination account [{}] is in state [{}]",
                    targetAccount.getAccountNumber(), targetAccount.getAccountStatus());
            throw new IllegalArgumentException("Destination account is inactive or frozen");
        }

        BigDecimal newSourceBalance = sourceAccount.getCurrentBalance().subtract(request.amount());
        BigDecimal newTargetBalance = targetAccount.getCurrentBalance().add(request.amount());

        sourceAccount.setCurrentBalance(newSourceBalance);
        targetAccount.setCurrentBalance(newTargetBalance);

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

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

        log.info("Fund transfer successful. Ref: [{}], Source New Balance: [Rs. {}], Target New Balance: [Rs. {}]",
                txRef, newSourceBalance, newTargetBalance);
        auditLogService.log(
                AuditAction.MONEY_TRANSFER,
                "Transferred ₹" +
                        request.amount() +
                        " from " +
                        sourceAccount.getAccountNumber() +
                        " to " +
                        targetAccount.getAccountNumber()
        );
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
