package com.bankflow.dto;

import com.bankflow.entity.Loan.LoanStatus;
import com.bankflow.entity.Loan.LoanType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminUserLoanResponse(

        String loanNumber,

        LoanType loanType,

        LoanStatus status,

        BigDecimal principalAmount,

        BigDecimal remainingBalance,

        BigDecimal monthlyEmi,

        Integer tenureMonths,

        LocalDate nextDueDate
) {}