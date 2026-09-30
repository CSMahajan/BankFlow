package com.bankflow.ai;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Component
public class AiIntentToolArgumentMapper {

    public Map<String, Object> map(AiIntent.Operation operation) {

        if (operation == null) {
            throw new IllegalArgumentException(
                    "AI operation must not be null"
            );
        }

        return switch (operation.intent()) {

            case TRANSACTION_TOTAL -> mapTransactionTotal(operation);

            case ACCOUNTS, ACCOUNT_BALANCE, CARDS, LOANS,
                 FIXED_DEPOSITS, SCHEDULED_TRANSFERS, ADMIN_DASHBOARD_SUMMARY -> Map.of();

            case ADMIN_USERS -> mapAdminUsers(operation);

            case ADMIN_USER_DETAILS -> mapAdminUserDetails(operation);

            case ADMIN_ACCOUNTS -> mapAdminAccounts(operation);

            case ADMIN_USER_ACCOUNTS -> mapAdminUserAccounts(operation);

            case ADMIN_CARDS -> mapAdminCards(operation);

            case ADMIN_USER_CARDS -> mapAdminUserCards(operation);

            case ADMIN_LOANS -> mapAdminLoans(operation);

            case ADMIN_USER_LOANS -> mapAdminUserLoans(operation);

            case ACCOUNT -> mapAccount(operation);

            case FIXED_DEPOSIT -> mapFixedDeposit(operation);

            case CALCULATE_FD_MATURITY -> mapCalculateFdMaturity(operation);

            case LOAN_REPAYMENT_HISTORY -> mapLoanRepaymentHistory(operation);

            case TRANSACTIONS -> mapTransactions(operation);

            default -> throw new UnsupportedOperationException(
                    "Live-data intent is not mapped yet: "
                            + operation.intent()
            );
        };
    }

    public String mapToolName(AiIntent.Operation operation) {

        if (operation == null) {
            throw new IllegalArgumentException(
                    "AI operation must not be null"
            );
        }

        return switch (operation.intent()) {

            case TRANSACTION_TOTAL -> "get_my_transaction_total";

            case ACCOUNTS -> "get_my_accounts";

            case ACCOUNT -> "get_my_account_by_number";

            case ACCOUNT_BALANCE -> "get_my_total_balance";

            case TRANSACTIONS -> "get_my_transactions";

            case CARDS -> "get_my_cards";

            case LOANS -> "get_my_loans";

            case FIXED_DEPOSITS -> "get_my_fixed_deposits";

            case FIXED_DEPOSIT -> "get_fd_by_number";

            case CALCULATE_FD_MATURITY -> "calculate_fd_maturity";

            case SCHEDULED_TRANSFERS -> "get_my_scheduled_transfers";

            case LOAN_REPAYMENT_HISTORY -> "get_loan_repayment_history";

            case ADMIN_DASHBOARD_SUMMARY -> "get_admin_dashboard_summary";

            case ADMIN_USERS -> "get_admin_users";

            case ADMIN_USER_DETAILS -> "get_admin_user_details";

            case ADMIN_ACCOUNTS -> "get_admin_accounts";

            case ADMIN_USER_ACCOUNTS -> "get_admin_user_accounts";

            case ADMIN_CARDS -> "get_admin_cards";

            case ADMIN_USER_CARDS -> "get_admin_user_cards";

            case ADMIN_LOANS -> "get_admin_loans";

            case ADMIN_USER_LOANS -> "get_admin_user_loans";

            default -> throw new UnsupportedOperationException(
                    "Live-data intent is not mapped yet: "
                            + operation.intent()
            );
        };
    }

    private Map<String, Object> mapAdminUsers(
            AiIntent.Operation operation) {

        Map<String, Object> arguments = new HashMap<>();

        if (operation.search() != null
                && !operation.search().isBlank()) {

            arguments.put(
                    "search",
                    operation.search().trim()
            );
        }

        if (operation.role() != null) {
            arguments.put(
                    "role",
                    operation.role()
            );
        }

        return arguments;
    }

    private Map<String, Object> mapAdminUserDetails(
            AiIntent.Operation operation) {

        if (operation.userId() == null) {
            throw new IllegalArgumentException(
                    "userId is required for ADMIN_USER_DETAILS"
            );
        }

        return Map.of(
                "userId",
                operation.userId()
        );
    }

    private Map<String, Object> mapAdminAccounts(
            AiIntent.Operation operation) {

        Map<String, Object> arguments = new HashMap<>();

        if (operation.search() != null
                && !operation.search().isBlank()) {

            arguments.put(
                    "search",
                    operation.search().trim()
            );
        }

        if (operation.accountStatus() != null
                && !operation.accountStatus().isBlank()) {

            arguments.put(
                    "status",
                    operation.accountStatus().trim().toUpperCase()
            );
        }

        return arguments;
    }

    private Map<String, Object> mapAdminUserAccounts(
            AiIntent.Operation operation) {

        if (operation.userId() == null) {
            throw new IllegalArgumentException(
                    "userId is required for ADMIN_USER_ACCOUNTS"
            );
        }

        return Map.of(
                "userId",
                operation.userId()
        );
    }

    private Map<String, Object> mapAdminCards(
            AiIntent.Operation operation) {

        Map<String, Object> arguments = new HashMap<>();

        if (operation.search() != null
                && !operation.search().isBlank()) {

            arguments.put(
                    "search",
                    operation.search().trim()
            );
        }

        if (operation.cardStatus() != null
                && !operation.cardStatus().isBlank()) {

            arguments.put(
                    "status",
                    operation.cardStatus().trim().toUpperCase()
            );
        }

        return arguments;
    }

    private Map<String, Object> mapAdminUserCards(
            AiIntent.Operation operation) {

        if (operation.userId() == null) {
            throw new IllegalArgumentException(
                    "userId is required for ADMIN_USER_CARDS"
            );
        }

        return Map.of(
                "userId",
                operation.userId()
        );
    }

    private Map<String, Object> mapAdminLoans(
            AiIntent.Operation operation) {

        Map<String, Object> arguments = new HashMap<>();

        if (operation.search() != null &&
                !operation.search().isBlank()) {

            arguments.put(
                    "search",
                    operation.search().trim()
            );
        }

        if (operation.loanType() != null &&
                !operation.loanType().isBlank()) {

            arguments.put(
                    "loanType",
                    operation.loanType().trim().toUpperCase()
            );
        }

        return arguments;
    }

    private Map<String, Object> mapAdminUserLoans(
            AiIntent.Operation operation) {

        if (operation.userId() == null) {
            throw new IllegalArgumentException(
                    "userId is required for ADMIN_USER_LOANS"
            );
        }

        return Map.of(
                "userId",
                operation.userId()
        );
    }

    private Map<String, Object> mapTransactionTotal(
            AiIntent.Operation operation) {

        if (operation.transactionType() == null) {
            throw new IllegalArgumentException(
                    "Transaction type is required for TRANSACTION_TOTAL"
            );
        }

        if (operation.period() == null) {
            throw new IllegalArgumentException(
                    "Period is required for TRANSACTION_TOTAL"
            );
        }

        LocalDate today =
                LocalDate.now(ZoneId.systemDefault());

        if (operation.monthOffset() != null) {
            return mapRelativeMonthTransactionTotal(operation);
        }

        DateRange dateRange =
                resolveDateRange(
                        operation.period(),
                        today
                );

        return Map.of(
                "type",
                operation.transactionType().name(),

                "startDate",
                dateRange.startDate().toString(),

                "endDate",
                dateRange.endDate().toString()
        );
    }

    private Map<String, Object> mapTransactions(
            AiIntent.Operation operation) {

        Map<String, Object> arguments =
                new HashMap<>();

        if (operation.transactionType() != null) {
            arguments.put(
                    "type",
                    operation.transactionType().name()
            );
        }

        if (operation.accountNumber() != null
                && !operation.accountNumber().isBlank()) {

            arguments.put(
                    "accountNumber",
                    operation.accountNumber().trim()
            );
        }

        if (operation.search() != null
                && !operation.search().isBlank()) {

            arguments.put(
                    "search",
                    operation.search().trim()
            );
        }

        if (operation.period() != null) {

            LocalDate today =
                    LocalDate.now(ZoneId.systemDefault());

            DateRange dateRange;

            if (operation.monthOffset() != null) {

                int monthOffset = operation.monthOffset();

                YearMonth targetMonth =
                        YearMonth.from(today)
                                .plusMonths(monthOffset);

                LocalDate startDate =
                        targetMonth.atDay(1);

                LocalDate endDate =
                        monthOffset == 0
                                ? today
                                : targetMonth.atEndOfMonth();

                dateRange =
                        new DateRange(
                                startDate,
                                endDate
                        );

            } else if (operation.period()
                    == AiIntent.Period.CUSTOM_RANGE) {

                LocalDate startDate =
                        parseRequiredDate(
                                operation.startDate(),
                                "startDate"
                        );

                LocalDate endDate =
                        operation.endDate() == null
                                ? today
                                : parseDate(
                                operation.endDate()
                        );

                dateRange =
                        new DateRange(
                                startDate,
                                endDate
                        );

            } else {

                dateRange =
                        resolveDateRange(
                                operation.period(),
                                today
                        );
            }

            if (dateRange.startDate()
                    .isAfter(dateRange.endDate())) {

                throw new IllegalArgumentException(
                        "Start date cannot be after end date"
                );
            }

            arguments.put(
                    "startDate",
                    dateRange.startDate().toString()
            );

            arguments.put(
                    "endDate",
                    dateRange.endDate().toString()
            );
        }

        return Map.copyOf(arguments);
    }

    private Map<String, Object> mapAccount(
            AiIntent.Operation operation) {

        if (operation.accountNumber() == null
                || operation.accountNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "accountNumber is required for ACCOUNT"
            );
        }

        return Map.of(
                "accountNumber",
                operation.accountNumber().trim()
        );
    }

    private Map<String, Object> mapFixedDeposit(
            AiIntent.Operation operation) {

        if (operation.fdNumber() == null
                || operation.fdNumber().isBlank()) {

            throw new IllegalArgumentException(
                    "fdNumber is required for FIXED_DEPOSIT"
            );
        }

        return Map.of(
                "fdNumber",
                operation.fdNumber().trim()
        );
    }

    private Map<String, Object> mapCalculateFdMaturity(
            AiIntent.Operation operation) {

        if (operation.depositAmount() == null) {
            throw new IllegalArgumentException(
                    "depositAmount is required for CALCULATE_FD_MATURITY"
            );
        }

        if (operation.tenureYears() == null) {
            throw new IllegalArgumentException(
                    "tenureYears is required for CALCULATE_FD_MATURITY"
            );
        }

        return Map.of(
                "depositAmount", operation.depositAmount(),
                "tenureYears", operation.tenureYears()
        );
    }

    private Map<String, Object> mapLoanRepaymentHistory(
            AiIntent.Operation operation) {

        if (operation.loanNumber() == null
                || operation.loanNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "loanNumber is required for LOAN_REPAYMENT_HISTORY"
            );
        }

        return Map.of(
                "loanNumber",
                operation.loanNumber().trim()
        );
    }

    private LocalDate parseRequiredDate(
            String value,
            String fieldName) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName
                            + " is required for CUSTOM_RANGE"
            );
        }

        return parseDate(value);
    }

    private LocalDate parseDate(String value) {

        try {
            return LocalDate.parse(value.trim());

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Invalid date: "
                            + value
                            + ". Expected format yyyy-MM-dd",
                    e
            );
        }
    }

    private DateRange resolveDateRange(
            AiIntent.Period period,
            LocalDate today) {

        return switch (period) {

            case CURRENT_CALENDAR_YEAR -> new DateRange(
                    LocalDate.of(
                            today.getYear(),
                            1,
                            1
                    ),
                    today
            );

            case PREVIOUS_CALENDAR_YEAR -> new DateRange(
                    LocalDate.of(
                            today.getYear() - 1,
                            1,
                            1
                    ),
                    LocalDate.of(
                            today.getYear() - 1,
                            12,
                            31
                    )
            );

            case CURRENT_FINANCIAL_YEAR -> currentFinancialYear(today);

            case PREVIOUS_FINANCIAL_YEAR -> previousFinancialYear(today);

            case CURRENT_MONTH -> new DateRange(
                    today.withDayOfMonth(1),
                    today
            );

            case PREVIOUS_MONTH -> {

                YearMonth previousMonth =
                        YearMonth.from(today)
                                .minusMonths(1);

                yield new DateRange(
                        previousMonth.atDay(1),
                        previousMonth.atEndOfMonth()
                );
            }

            case CUSTOM_RANGE -> throw new UnsupportedOperationException(
                    "CUSTOM_RANGE requires explicit dates"
            );
        };
    }

    private Map<String, Object> mapRelativeMonthTransactionTotal(
            AiIntent.Operation operation) {

        if (operation.transactionType() == null) {
            throw new IllegalArgumentException(
                    "Transaction type is required for TRANSACTION_TOTAL"
            );
        }

        int monthOffset = operation.monthOffset();

        LocalDate today =
                LocalDate.now(ZoneId.systemDefault());

        YearMonth targetMonth =
                YearMonth.from(today).plusMonths(monthOffset);

        LocalDate startDate = targetMonth.atDay(1);

        LocalDate endDate =
                monthOffset == 0
                        ? today
                        : targetMonth.atEndOfMonth();

        return Map.of(
                "type", operation.transactionType().name(),
                "startDate", startDate.toString(),
                "endDate", endDate.toString()
        );
    }

    private DateRange currentFinancialYear(
            LocalDate today) {

        int startYear =
                today.getMonthValue() >= 4
                        ? today.getYear()
                        : today.getYear() - 1;

        return new DateRange(
                LocalDate.of(
                        startYear,
                        4,
                        1
                ),
                today
        );
    }

    private DateRange previousFinancialYear(
            LocalDate today) {

        int currentFinancialYearStart =
                today.getMonthValue() >= 4
                        ? today.getYear()
                        : today.getYear() - 1;

        return new DateRange(
                LocalDate.of(
                        currentFinancialYearStart - 1,
                        4,
                        1
                ),
                LocalDate.of(
                        currentFinancialYearStart,
                        3,
                        31
                )
        );
    }

    private record DateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}