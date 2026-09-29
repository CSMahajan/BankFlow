package com.bankflow.ai;

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
            String search
    ) {}

    public enum Intent {
        ACCOUNTS,
        ACCOUNT_BALANCE,
        TRANSACTIONS,
        TRANSACTION_TOTAL,
        CARDS,
        LOANS,
        FIXED_DEPOSITS,
        SCHEDULED_TRANSFERS,
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