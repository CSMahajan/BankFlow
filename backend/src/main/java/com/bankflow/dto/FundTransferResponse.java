package com.bankflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FundTransferResponse(
        String transactionReference,
        String sourceAccountNumber,
        String targetAccountNumber,
        BigDecimal amount,
        BigDecimal remainingBalance,
        String status,
        LocalDateTime timestamp
) {}
