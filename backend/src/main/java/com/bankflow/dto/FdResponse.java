package com.bankflow.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FdResponse(
        Long id,
        String fdNumber,
        String customerName,
        String sourceAccountNumber,
        BigDecimal depositAmount,
        BigDecimal interestRate,
        Integer tenureYears,
        LocalDate depositDate,
        LocalDate maturityDate,
        BigDecimal maturityAmount,
        String status
) {}
