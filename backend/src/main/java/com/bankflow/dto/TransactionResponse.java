package com.bankflow.dto;

import com.bankflow.entity.Transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String transactionId,
        String accountNumber,
        LocalDateTime transactionDate,
        TransactionType transactionType,
        BigDecimal amount,
        BigDecimal availableBalance,
        String description
) {}
