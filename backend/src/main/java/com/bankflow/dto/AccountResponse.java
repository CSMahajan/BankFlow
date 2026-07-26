package com.bankflow.dto;

import com.bankflow.entity.Account.AccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String accountNumber,
        String customerName,
        String customerEmail,
        AccountType accountType,
        String branchName,
        BigDecimal currentBalance,
        String accountStatus,
        LocalDateTime createdAt
) {}
