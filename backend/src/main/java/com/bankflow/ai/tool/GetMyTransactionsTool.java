package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.entity.Transaction;
import com.bankflow.service.TransactionService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetMyTransactionsTool implements AiTool {

    public static final String NAME = "get_my_transactions";
    public static final String SCHEMA_TYPE_STRING = "STRING";

    private final TransactionService transactionService;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public FunctionDeclaration functionDeclaration() {

        return FunctionDeclaration.builder()
                .name(NAME)
                .description("""
                        Returns the authenticated customer's transactions.

                        Optional filters can be provided:
                        - accountNumber to filter by a specific account
                        - type to filter by CREDIT or DEBIT
                        - startDate and endDate to filter by date range
                        - search to search transaction ID or description

                        Dates must use ISO format yyyy-MM-dd.

                        If startDate is provided without endDate,
                        the backend uses the current date as the end date.

                        The customer is determined by the server-side
                        authentication context.
                        """)
                .parameters(
                        Schema.builder()
                                .type("OBJECT")
                                .properties(
                                        Map.of(
                                                "accountNumber",
                                                Schema.builder()
                                                        .type(SCHEMA_TYPE_STRING)
                                                        .description(
                                                                "The customer's bank account number to filter transactions by"
                                                        )
                                                        .build(),

                                                "type",
                                                Schema.builder()
                                                        .type(SCHEMA_TYPE_STRING)
                                                        .enum_(List.of(
                                                                "CREDIT",
                                                                "DEBIT"
                                                        ))
                                                        .description(
                                                                "Transaction type to filter by"
                                                        )
                                                        .build(),

                                                "startDate",
                                                Schema.builder()
                                                        .type(SCHEMA_TYPE_STRING)
                                                        .description(
                                                                "Start date in ISO format yyyy-MM-dd"
                                                        )
                                                        .build(),

                                                "endDate",
                                                Schema.builder()
                                                        .type(SCHEMA_TYPE_STRING)
                                                        .description(
                                                                "End date in ISO format yyyy-MM-dd"
                                                        )
                                                        .build(),

                                                "search",
                                                Schema.builder()
                                                        .type(SCHEMA_TYPE_STRING)
                                                        .description(
                                                                "Text to search in transaction ID or transaction description"
                                                        )
                                                        .build()
                                        )
                                )
                                .build()
                )
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.CUSTOMER);
    }

    @Override
    public List<TransactionResponse> execute() {

        return transactionService.getMyTransactions(
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(
                                0,
                                20,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "transactionDate"
                                )
                        )
                )
                .getContent();
    }

    @Override
    public List<TransactionResponse> execute(
            Map<String, Object> arguments) {

        String accountNumber =
                parseString(arguments.get("accountNumber"));

        Transaction.TransactionType type =
                parseTransactionType(arguments.get("type"));

        LocalDate startDate =
                parseDate(arguments.get("startDate"));

        LocalDate endDate =
                parseDate(arguments.get("endDate"));

        String search =
                parseString(arguments.get("search"));

        return transactionService.getMyTransactions(
                        accountNumber,
                        type,
                        startDate,
                        endDate,
                        search,
                        PageRequest.of(
                                0,
                                20,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "transactionDate"
                                )
                        )
                )
                .getContent();
    }

    private String parseString(Object value) {

        if (value == null) {
            return null;
        }

        String valueAsString = value.toString().trim();

        return valueAsString.isBlank()
                ? null
                : valueAsString;
    }

    private Transaction.TransactionType parseTransactionType(
            Object value) {

        String type = parseString(value);

        if (type == null) {
            return null;
        }

        return Transaction.TransactionType.valueOf(
                type.toUpperCase()
        );
    }

    private LocalDate parseDate(Object value) {

        String date = parseString(value);

        if (date == null) {
            return null;
        }

        return LocalDate.parse(date);
    }
}