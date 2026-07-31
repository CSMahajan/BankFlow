package com.bankflow.dto;

import com.bankflow.entity.Loan.LoanStatus;
import com.bankflow.entity.Loan.LoanType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanResponse(
        Long id,
        String loanNumber,
        String accountNumber,
        String customerName,
        LoanType loanType,
        BigDecimal principalAmount,
        BigDecimal annualInterestRate,
        Integer tenureMonths,
        BigDecimal monthlyEmi,
        BigDecimal remainingBalance,
        LoanStatus status,
        String rejectionRemarks,
        LocalDate applicationDate,
        LocalDate startDate,
        LocalDate nextDueDate
) {}
