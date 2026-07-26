package com.bankflow.dto;

import java.math.BigDecimal;

public record FdCalculatorResponse(
        BigDecimal depositAmount,
        BigDecimal interestRate,
        Integer tenureYears,
        BigDecimal totalInterestEarned,
        BigDecimal maturityAmount
) {}
