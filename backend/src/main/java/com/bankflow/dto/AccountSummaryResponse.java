package com.bankflow.dto;

public record AccountSummaryResponse(

        long activeAccounts,

        long frozenAccounts,

        long savingsAccounts,

        long currentAccounts

) {}