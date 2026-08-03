package com.bankflow.dto;

import com.bankflow.entity.Account.AccountStatus;
import com.bankflow.entity.Account.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminUserAccountResponse(

        String accountNumber,

        AccountType accountType,

        BigDecimal currentBalance,

        AccountStatus accountStatus,

        String branchName,

        LocalDateTime createdAt
) {}