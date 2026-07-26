package com.bankflow.dto;

import com.bankflow.entity.Card.CardType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record IssueCardRequest(
        @NotBlank(message = "Account number is required")
        String accountNumber,

        @NotNull(message = "Card type is required")
        CardType cardType, // DEBIT or CREDIT

        @NotNull(message = "Daily limit is required")
        @DecimalMin(value = "1000.00", message = "Minimum daily limit is 1000.00")
        BigDecimal dailyLimit
) {}
