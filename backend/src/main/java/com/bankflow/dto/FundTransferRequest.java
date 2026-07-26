package com.bankflow.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FundTransferRequest(
        @NotBlank(message = "Source account number is required")
        String sourceAccountNumber,

        @NotBlank(message = "Destination account number is required")
        String targetAccountNumber,

        @NotNull(message = "Transfer amount is required")
        @DecimalMin(value = "1.00", message = "Minimum transfer amount is Rs. 1.00")
        BigDecimal amount,

        String remark
) {}
