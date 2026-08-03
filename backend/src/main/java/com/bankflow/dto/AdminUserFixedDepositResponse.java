package com.bankflow.dto;

import com.bankflow.entity.FixedDeposit.FdStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminUserFixedDepositResponse(

        String fdNumber,

        BigDecimal principalAmount,

        BigDecimal interestRate,

        LocalDate maturityDate,

        BigDecimal maturityAmount,

        FdStatus status
) {}