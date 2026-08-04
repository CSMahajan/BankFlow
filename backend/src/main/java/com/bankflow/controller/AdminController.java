package com.bankflow.controller;

import com.bankflow.dto.*;
import com.bankflow.service.AccountService;
import com.bankflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AccountService accountService;

    @PostMapping("/users/create-admin")
    @PreAuthorize("hasRole('ADMIN')") // Blocks non-admins (HTTP 403 Forbidden)
    @ResponseStatus(HttpStatus.CREATED)
    public void createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        userService.createAdminAccount(request);
    }

    @GetMapping("/users/{userId}/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserAccountResponse>> getUserAccounts(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userService.getUserAccounts(userId)
        );
    }

    @GetMapping("/users/{userId}/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserCardResponse>> getUserCards(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userService.getUserCards(userId)
        );
    }

    @GetMapping("/users/{userId}/loans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserLoanResponse>> getUserLoans(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userService.getUserLoans(userId)
        );
    }

    @GetMapping("/users/{userId}/fixed-deposits")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserFixedDepositResponse>> getUserFixedDeposits(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userService.getUserFixedDeposits(userId)
        );
    }

    @PatchMapping("/accounts/{accountNumber}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> freezeAccount(
            @PathVariable String accountNumber) {

        return ResponseEntity.ok(
                accountService.freezeAccountByAdmin(accountNumber)
        );
    }

    @PatchMapping("/accounts/{accountNumber}/unfreeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> unfreezeAccount(
            @PathVariable String accountNumber) {

        return ResponseEntity.ok(
                accountService.unfreezeAccountByAdmin(accountNumber)
        );
    }
}
