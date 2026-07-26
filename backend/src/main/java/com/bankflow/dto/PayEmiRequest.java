package com.bankflow.dto;

import jakarta.validation.constraints.NotBlank;

public record PayEmiRequest(
        @NotBlank(message = "Loan number is required")
        String loanNumber,

        @NotBlank(message = "Source account number is required")
        String sourceAccountNumber
) {}
