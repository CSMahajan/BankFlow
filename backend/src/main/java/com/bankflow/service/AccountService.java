package com.bankflow.service;

import com.bankflow.dto.AccountResponse;
import com.bankflow.dto.BalanceResponse;
import com.bankflow.dto.CreateAccountRequest;
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
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        User currentUser = getAuthenticatedUser();
        log.info("Initiating account creation for user [{}] of type [{}] in branch [{}]",
                currentUser.getEmail(), request.accountType(), request.branchName());

        BigDecimal initialDeposit = request.initialDeposit() != null ? request.initialDeposit() : BigDecimal.ZERO;
        String accountNumber = generateUniqueAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .user(currentUser)
                .accountType(request.accountType())
                .branchName(request.branchName())
                .currentBalance(initialDeposit)
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Account successfully created. Number: [{}], ID: [{}], Initial Balance: [Rs. {}]",
                accountNumber, savedAccount.getId(), initialDeposit);
        auditLogService.log(
                AuditAction.ACCOUNT_CREATED,
                "Opened " +
                        savedAccount.getAccountType() +
                        " Account (" +
                        savedAccount.getAccountNumber() +
                        ")"
        );
        // Record initial deposit transaction if amount is greater than zero
        if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            String txId = "DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Transaction creditTx = Transaction.builder()
                    .transactionId(txId)
                    .account(savedAccount)
                    .transactionType(Transaction.TransactionType.CREDIT)
                    .amount(initialDeposit)
                    .availableBalance(initialDeposit)
                    .description("Initial Account Opening Deposit")
                    .build();

            transactionRepository.save(creditTx);
            log.info("Initial deposit transaction created with Ref: [{}] for Account [{}]", txId, accountNumber);
        }

        return mapToResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts() {
        User currentUser = getAuthenticatedUser();
        log.debug("Fetching all accounts for user [{}]", currentUser.getEmail());

        List<AccountResponse> accounts = accountRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.debug("Retrieved [{}] accounts for user [{}]", accounts.size(), currentUser.getEmail());
        return accounts;
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        User currentUser = getAuthenticatedUser();
        log.debug("Fetching details for account [{}] requested by user [{}]", accountNumber, currentUser.getEmail());

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("Account lookup failed: Account number [{}] not found", accountNumber);
                    return new IllegalArgumentException("Account not found");
                });

        if (!currentUser.getRole().equals(User.Role.ADMIN) && !account.getUser().getId().equals(currentUser.getId())) {
            log.warn("Security Alert: User [{}] attempted unauthorized view of account [{}]",
                    currentUser.getEmail(), accountNumber);
            throw new AccessDeniedException("You are not authorized to view this account");
        }

        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getAvailableBalance(String accountNumber) {
        User currentUser = getAuthenticatedUser();
        log.debug("Balance inquiry for account [{}] requested by user [{}]", accountNumber, currentUser.getEmail());

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("Balance inquiry failed: Account number [{}] not found", accountNumber);
                    return new IllegalArgumentException("Account not found");
                });

        if (!currentUser.getRole().equals(User.Role.ADMIN) && !account.getUser().getId().equals(currentUser.getId())) {
            log.warn("Security Alert: User [{}] attempted unauthorized balance check for account [{}]",
                    currentUser.getEmail(), accountNumber);
            throw new AccessDeniedException("You are not authorized to view this balance");
        }

        log.info("Balance fetched for account [{}]: [Rs. {}]", accountNumber, account.getCurrentBalance());
        return new BalanceResponse(
                account.getAccountNumber(),
                account.getCurrentBalance(),
                account.getAccountStatus().name()
        );
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccountsForAdmin() {
        User currentUser = getAuthenticatedUser();
        log.info("ADMIN action: [{}] fetching all accounts across system", currentUser.getEmail());

        List<AccountResponse> allAccounts = accountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("ADMIN view retrieved [{}] total accounts", allAccounts.size());
        return allAccounts;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> {
                    log.error("Authentication Context Error: User [{}] not found in database", auth.getName());
                    return new IllegalArgumentException("Authenticated user not found");
                });
    }

    private String generateUniqueAccountNumber() {
        String accNum;
        do {
            long number = 1000000000L + (long) (secureRandom.nextDouble() * 9000000000L);
            accNum = "BF" + number;
        } while (accountRepository.existsByAccountNumber(accNum));

        log.debug("Generated unique account number: [{}]", accNum);
        return accNum;
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getUser().getFullName(),
                account.getUser().getEmail(),
                account.getAccountType(),
                account.getBranchName(),
                account.getCurrentBalance(),
                account.getAccountStatus().name(),
                account.getCreatedAt()
        );
    }
}
