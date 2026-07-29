package com.bankflow.controller;

import com.bankflow.dto.AccountResponse;
import com.bankflow.dto.BalanceResponse;
import com.bankflow.dto.CreateAccountRequest;
import com.bankflow.dto.UpdateProfileRequest;
import com.bankflow.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/accounts/create")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/accounts/my-accounts")
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {
        return ResponseEntity.ok(accountService.getMyAccounts());
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @GetMapping("/accounts/{accountNumber}/balance")
    public ResponseEntity<BalanceResponse> getAvailableBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAvailableBalance(accountNumber));
    }

    @GetMapping("/admin/accounts/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllAccountsForAdmin() {
        return ResponseEntity.ok(accountService.getAllAccountsForAdmin());
    }
}
