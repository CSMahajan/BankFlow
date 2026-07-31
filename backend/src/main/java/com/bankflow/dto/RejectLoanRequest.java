package com.bankflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectLoanRequest(

        @NotBlank(message = "Rejection remarks are required")
        @Size(max = 500, message = "Rejection remarks cannot exceed 500 characters")
        String remarks
) {}