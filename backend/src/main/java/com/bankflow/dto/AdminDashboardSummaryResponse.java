package com.bankflow.dto;

import java.math.BigDecimal;

public record AdminDashboardSummaryResponse(
        long totalCustomers,
        long totalAccounts,
        long activeLoans,
        long pendingLoans,
        long activeFixedDeposits,
        BigDecimal totalDeposits,
        long pendingKycDocuments
) {}