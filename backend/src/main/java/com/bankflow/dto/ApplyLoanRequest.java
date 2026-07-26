package com.bankflow.dto;

import com.bankflow.entity.Loan.LoanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ApplyLoanRequest(
        @NotBlank(message = "Disbursement account number is required")
        String accountNumber,

        @NotNull(message = "Loan type is required")
        LoanType loanType,

        @NotNull(message = "Principal amount is required")
        @DecimalMin(value = "10000.00", message = "Minimum loan amount is Rs. 10,000")
        BigDecimal principalAmount,

        @NotNull(message = "Tenure in months is required")
        @Min(value = 6, message = "Minimum loan tenure is 6 months")
        Integer tenureMonths
) {}
