package com.bankflow.dto;

import com.bankflow.entity.Account.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotNull(message = "Account type is required")
        AccountType accountType,

        @NotBlank(message = "Branch name is required")
        String branchName,

        BigDecimal initialDeposit
) {}
