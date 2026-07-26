package com.bankflow.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    @GetMapping
    public DashboardSummary overview(Authentication authentication) {
        return new DashboardSummary(authentication.getName(), new BigDecimal("268450.00"), new BigDecimal("85393.00"), new BigDecimal("1740.00"), List.of(
                new TransactionView("TXN-260724-001", "Salary credit", LocalDate.now().minusDays(2), "CREDIT", new BigDecimal("84500.00"), new BigDecimal("186450.00")),
                new TransactionView("TXN-260724-002", "Green Grocers", LocalDate.now().minusDays(3), "DEBIT", new BigDecimal("1240.00"), new BigDecimal("101950.00"))));
    }

    record DashboardSummary(String customerEmail, BigDecimal availableBalance, BigDecimal totalCredit,
                            BigDecimal totalDebit, List<TransactionView> recentTransactions) {
    }

    record TransactionView(String transactionId, String description, LocalDate transactionDate, String type,
                           BigDecimal amount, BigDecimal availableBalance) {
    }
}
