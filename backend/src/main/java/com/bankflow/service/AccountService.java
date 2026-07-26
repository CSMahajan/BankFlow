package com.bankflow.service;

import com.bankflow.dto.AccountResponse;
import com.bankflow.dto.BalanceResponse;
import com.bankflow.dto.CreateAccountRequest;
import com.bankflow.dto.UpdateProfileRequest;
import com.bankflow.entity.Account;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        User currentUser = getAuthenticatedUser();

        Account account = Account.builder()
                .accountNumber(generateUniqueAccountNumber())
                .user(currentUser)
                .accountType(request.accountType())
                .branchName(request.branchName())
                .currentBalance(request.initialDeposit() != null ? request.initialDeposit() : BigDecimal.ZERO)
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);
        return mapToResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts() {
        User currentUser = getAuthenticatedUser();
        return accountRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        User currentUser = getAuthenticatedUser();
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!currentUser.getRole().equals(User.Role.ADMIN) && !account.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to view this account");
        }

        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getAvailableBalance(String accountNumber) {
        User currentUser = getAuthenticatedUser();
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!currentUser.getRole().equals(User.Role.ADMIN) && !account.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to view this balance");
        }

        return new BalanceResponse(
                account.getAccountNumber(),
                account.getCurrentBalance(),
                account.getAccountStatus().name()
        );
    }

    @Transactional
    public void updateCustomerProfile(UpdateProfileRequest request) {
        User currentUser = getAuthenticatedUser();
        currentUser.setFullName(request.fullName());
        userRepository.save(currentUser);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccountsForAdmin() {
        return accountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }

    private String generateUniqueAccountNumber() {
        String accNum;
        Random random = new Random();
        do {
            long number = 1000000000L + (long) (random.nextDouble() * 9000000000L);
            accNum = "BF" + number;
        } while (accountRepository.existsByAccountNumber(accNum));
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
