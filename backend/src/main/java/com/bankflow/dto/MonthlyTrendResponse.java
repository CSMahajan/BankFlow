package com.bankflow.dto;

import java.math.BigDecimal;

public record MonthlyTrendResponse(
        String month, // e.g., "JAN", "FEB"
        int year,
        BigDecimal totalIncome,
        BigDecimal totalExpense
) {}
