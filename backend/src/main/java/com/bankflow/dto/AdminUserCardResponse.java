package com.bankflow.dto;

import com.bankflow.entity.Card.CardStatus;
import com.bankflow.entity.Card.CardType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminUserCardResponse(

        String cardNumber,

        CardType cardType,

        CardStatus cardStatus,

        BigDecimal dailyLimit,

        LocalDate expiryDate
) {}