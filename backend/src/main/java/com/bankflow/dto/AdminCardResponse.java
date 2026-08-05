package com.bankflow.dto;

import com.bankflow.entity.Card;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminCardResponse(

        Long id,
        String customerName,
        String accountNumber,
        String maskedCardNumber,
        Card.CardType cardType,
        Card.CardStatus cardStatus,
        BigDecimal dailyLimit,
        LocalDate expiryDate

) {}