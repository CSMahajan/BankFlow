package com.bankflow.dto;

import com.bankflow.entity.Loan.LoanStatus;
import com.bankflow.entity.Loan.LoanType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanResponse(
        Long id,
        String loanNumber,
        String accountNumber,
        LoanType loanType,
        BigDecimal principalAmount,
        BigDecimal annualInterestRate,
        Integer tenureMonths,
        BigDecimal monthlyEmi,
        BigDecimal remainingBalance,
        LoanStatus status,
        LocalDate startDate,
        LocalDate nextDueDate
) {}
