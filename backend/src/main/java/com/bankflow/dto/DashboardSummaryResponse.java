package com.bankflow.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardSummaryResponse(
        String customerName,
        BigDecimal totalNetWorth,          // Accounts balance + FD principal
        BigDecimal totalAccountBalance,    // Total across active accounts
        BigDecimal totalFdInvestment,      // Total active FD amount
        BigDecimal totalOutstandingLoans,   // Total active loan remaining balance
        int activeAccountsCount,
        int activeFdCount,
        int activeLoanCount,
        LocalDate nextEmiDueDate,
        BigDecimal nextEmiAmount,
        List<TransactionResponse> recentTransactions
) {}
