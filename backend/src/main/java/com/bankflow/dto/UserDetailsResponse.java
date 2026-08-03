package com.bankflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserDetailsResponse(

        Long id,
        String fullName,
        String email,
        String role,
        LocalDateTime createdAt,

        long accountCount,
        long cardCount,
        long loanCount,
        long fixedDepositCount,

        BigDecimal totalBalance,
        BigDecimal outstandingLoanAmount

) {}