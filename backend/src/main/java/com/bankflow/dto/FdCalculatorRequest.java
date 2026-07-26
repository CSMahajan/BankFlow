package com.bankflow.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FdCalculatorRequest(
        @NotNull(message = "Deposit amount is required")
        @DecimalMin(value = "10000.01", message = "Deposit amount must be greater than Rs. 10,000")
        BigDecimal depositAmount,

        @NotNull(message = "Tenure is required")
        Integer tenureYears
) {}
