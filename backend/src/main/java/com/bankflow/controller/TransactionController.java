package com.bankflow.controller;

import com.bankflow.dto.AccountDashboardSummary;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // View Account Summary & Last 10 Transactions
    @GetMapping("/dashboard/{accountNumber}")
    public ResponseEntity<AccountDashboardSummary> getDashboardSummary(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.getDashboardSummary(accountNumber));
    }

    // Filter Transactions by Date Range
    @GetMapping("/filter/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByDateRange(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(accountNumber, startDate, endDate));
    }

    // Admin endpoint: Search transactions for any customer account
    @GetMapping("/admin/search/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransactionResponse>> searchAccountTransactionsAdmin(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.getAllTransactionsForAdmin(accountNumber));
    }
}
