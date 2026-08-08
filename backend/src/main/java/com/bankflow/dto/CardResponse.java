package com.bankflow.dto;

import com.bankflow.entity.Account;
import com.bankflow.entity.Card.CardStatus;
import com.bankflow.entity.Card.CardType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse(
        Long id,
        String accountNumber,
        String maskedCardNumber,
        CardType cardType,
        CardStatus cardStatus,
        Account.AccountStatus accountStatus,
        String cardHolderName,
        LocalDate expiryDate,
        String cvv,
        BigDecimal dailyLimit
) {}
