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

    private Account getAccount(String accountNumber) {

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found with number: " + accountNumber));

    }

    @Transactional
    public AccountResponse toggleAccountStatus(String accountNumber) {

        User currentUser = getAuthenticatedUser();

        log.info("Toggling account status for account [{}] by user [{}]",
                accountNumber, currentUser.getEmail());

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found with number: " + accountNumber));

        validateAccountOwnership(account, currentUser);

        validateStatusChange(account);

        if (account.getAccountStatus() == Account.AccountStatus.ACTIVE) {
            account.setAccountStatus(Account.AccountStatus.FROZEN);
            log.info("Account [{}] has been FROZEN", accountNumber);
            auditLogService.log(AuditAction.ACCOUNT_FROZEN,
                    "Account " + account.getAccountNumber() + " frozen");
        } else if (account.getAccountStatus() == Account.AccountStatus.FROZEN) {
            account.setAccountStatus(Account.AccountStatus.ACTIVE);
            log.info("Account [{}] has been ACTIVATED", accountNumber);
            auditLogService.log(AuditAction.ACCOUNT_ACTIVATED,
                    "Account " + account.getAccountNumber() + " activated"
            );
        }

        Account updatedAccount = accountRepository.save(account);

        return mapToResponse(updatedAccount);
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

    @Transactional
    public AccountResponse freezeAccountByAdmin(String accountNumber) {
        log.info("ADMIN requested to freeze account [{}]", accountNumber);
        Account account = getAccount(accountNumber);
        if (account.getAccountStatus() == Account.AccountStatus.INACTIVE) {
            throw new IllegalStateException(
                    "Inactive account cannot be frozen.");
        }
        if (account.getAccountStatus() == Account.AccountStatus.FROZEN) {
            throw new IllegalStateException(
                    "Account is already frozen.");
        }
        account.setAccountStatus(Account.AccountStatus.FROZEN);
        auditLogService.log(
                AuditAction.ACCOUNT_FROZEN,
                "Account " + account.getAccountNumber() + " frozen"
        );
        Account updatedAccount = accountRepository.save(account);
        log.info("ADMIN successfully froze account [{}]", accountNumber);
        return mapToResponse(updatedAccount);
    }

    @Transactional
    public AccountResponse unfreezeAccountByAdmin(String accountNumber) {
        log.info("ADMIN requested to unfreeze account [{}]", accountNumber);
        Account account = getAccount(accountNumber);
        if (account.getAccountStatus() == Account.AccountStatus.INACTIVE) {
            throw new IllegalStateException(
                    "Inactive account cannot be activated.");
        }
        if (account.getAccountStatus() == Account.AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Account is already active.");
        }
        account.setAccountStatus(Account.AccountStatus.ACTIVE);
        auditLogService.log(
                AuditAction.ACCOUNT_ACTIVATED,
                "Account " + account.getAccountNumber() + " activated"
        );
        Account updatedAccount = accountRepository.save(account);
        log.info("ADMIN successfully unfroze account [{}]", accountNumber);
        return mapToResponse(updatedAccount);
    }

    private void validateAccountOwnership(Account account, User currentUser) {
        if (!account.getUser().getId().equals(currentUser.getId())) {
            log.warn("Security Alert: User [{}] attempted to modify account [{}] belonging to another user",
                    currentUser.getEmail(), account.getAccountNumber());

            throw new AccessDeniedException("You are not authorized to modify this account");
        }
    }

    private void validateStatusChange(Account account) {
        if (account.getAccountStatus() == Account.AccountStatus.INACTIVE) {
            log.warn("Account [{}] cannot be modified because current status is [{}]",
                    account.getAccountNumber(), account.getAccountStatus());

            throw new IllegalStateException("Inactive accounts cannot be activated or frozen.");
        }
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
