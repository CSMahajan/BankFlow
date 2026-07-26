package com.bankflow.dto;

import java.math.BigDecimal;
import java.util.List;

public record AccountDashboardSummary(
        String accountNumber,
        BigDecimal currentBalance,
        BigDecimal totalCreditAmount,
        BigDecimal totalDebitAmount,
        List<TransactionResponse> recentTransactions // Last 10 transactions
) {}
