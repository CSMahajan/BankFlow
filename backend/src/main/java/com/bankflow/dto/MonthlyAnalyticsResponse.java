package com.bankflow.dto;

import java.math.BigDecimal;

public record MonthlyAnalyticsResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netCashFlow
) {}
