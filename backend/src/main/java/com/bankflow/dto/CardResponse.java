package com.bankflow.dto;

import com.bankflow.entity.Card.CardStatus;
import com.bankflow.entity.Card.CardType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse(
        Long id,
        String accountNumber,
        String maskedCardNumber, // e.g., "4111********1234" for security
        CardType cardType,
        CardStatus cardStatus,
        String cardHolderName,
        LocalDate expiryDate,
        String cvv,
        BigDecimal dailyLimit
) {}
