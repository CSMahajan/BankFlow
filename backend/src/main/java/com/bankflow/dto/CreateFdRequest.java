package com.bankflow.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateFdRequest(
        @NotBlank(message = "Source account number is required")
        String sourceAccountNumber,

        @NotNull(message = "Deposit amount is required")
        @DecimalMin(value = "10000.01", message = "Deposit amount must be greater than Rs. 10,000")
        BigDecimal depositAmount,

        @NotNull(message = "Tenure is required")
        Integer tenureYears // Must be 1, 3, or 5
) {}
