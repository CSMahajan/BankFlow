package com.bankflow.controller;

import com.bankflow.dto.AccountDashboardSummary;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.entity.Transaction;
import com.bankflow.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    //My All Transactions
    @GetMapping("/my-transactions")
    public ResponseEntity<Page<TransactionResponse>> getMyTransactions(

            @RequestParam(required = false)
            String accountNumber,

            @RequestParam(required = false)
            Transaction.TransactionType type,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @PageableDefault(
                    size = 20,
                    sort = "transactionDate",
                    direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                transactionService.getMyTransactions(
                        accountNumber,
                        type,
                        startDate,
                        endDate,
                        pageable
                )
        );
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionDetails(
            @PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionDetails(transactionId)
        );
    }

    // Admin endpoint: Search transactions for any customer account
    @GetMapping("/admin/search/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransactionResponse>> searchAccountTransactionsAdmin(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.getAllTransactionsForAdmin(accountNumber));
    }
}
