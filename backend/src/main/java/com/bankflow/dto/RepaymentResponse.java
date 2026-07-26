package com.bankflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RepaymentResponse(
        Long id,
        String loanNumber,
        BigDecimal amountPaid,
        BigDecimal principalComponent,
        BigDecimal interestComponent,
        BigDecimal remainingLoanBalance,
        LocalDateTime paymentDate,
        String transactionReference
) {}
