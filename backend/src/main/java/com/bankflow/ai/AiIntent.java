package com.bankflow.ai;

import java.math.BigDecimal;

public record AiIntent(
        Route route,
        java.util.List<Operation> operations
) {

    public enum Route {
        LIVE_DATA,
        KNOWLEDGE
    }

    public record Operation(
            Intent intent,
            TransactionType transactionType,
            Period period,
            Integer monthOffset,
            String startDate,
            String endDate,
            String accountNumber,
            String search,
            String accountStatus,
            String cardStatus,
            String role,
            Long userId,
            String fdNumber,
            String loanNumber,
            BigDecimal depositAmount,
            Integer tenureYears
    ) {}

    public enum Intent {
        ACCOUNTS,
        ACCOUNT,
        ACCOUNT_BALANCE,
        TRANSACTIONS,
        TRANSACTION_TOTAL,
        CARDS,
        LOANS,
        LOAN_REPAYMENT_HISTORY,
        FIXED_DEPOSITS,
        FIXED_DEPOSIT,
        CALCULATE_FD_MATURITY,
        SCHEDULED_TRANSFERS,
        ADMIN_DASHBOARD_SUMMARY,
        ADMIN_USERS,
        ADMIN_USER_DETAILS,
        ADMIN_ACCOUNTS,
        ADMIN_USER_ACCOUNTS,
        ADMIN_CARDS,
        ADMIN_USER_CARDS,
        BANKFLOW_DOCUMENTATION,
        GENERAL
    }

    public enum TransactionType {
        CREDIT,
        DEBIT
    }

    public enum Period {
        CURRENT_CALENDAR_YEAR,
        PREVIOUS_CALENDAR_YEAR,
        CURRENT_FINANCIAL_YEAR,
        PREVIOUS_FINANCIAL_YEAR,
        CURRENT_MONTH,
        PREVIOUS_MONTH,
        CUSTOM_RANGE
    }
}