package com.bankflow.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        String accountNumber,
        BigDecimal currentBalance,
        String accountStatus
) {}
