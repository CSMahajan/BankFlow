package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.entity.Transaction;
import com.bankflow.service.TransactionService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetMyTransactionTotalTool implements AiTool {

    public static final String NAME = "get_my_transaction_total";

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
                        Returns the authenticated customer's total transaction
                        amount for a transaction type and date range.

                        Use DEBIT for total spending or money debited.
                        Use CREDIT for total money credited or received.

                        The total is calculated by the backend across all
                        accounts belonging to the authenticated customer.

                        Dates must use ISO format yyyy-MM-dd.

                        If startDate is provided without endDate,
                        the backend uses the current date as the end date.
                        """)
                .parameters(
                        Schema.builder()
                                .type("OBJECT")
                                .properties(
                                        Map.of(
                                                "type",
                                                Schema.builder()
                                                        .type("STRING")
                                                        .enum_(List.of(
                                                                "CREDIT",
                                                                "DEBIT"
                                                        ))
                                                        .description(
                                                                "Transaction type to aggregate"
                                                        )
                                                        .build(),
                                                "startDate",
                                                Schema.builder()
                                                        .type("STRING")
                                                        .description(
                                                                "Start date in ISO format yyyy-MM-dd"
                                                        )
                                                        .build(),
                                                "endDate",
                                                Schema.builder()
                                                        .type("STRING")
                                                        .description(
                                                                "End date in ISO format yyyy-MM-dd"
                                                        )
                                                        .build()
                                        )
                                )
                                .required(List.of(
                                        "type",
                                        "startDate"
                                ))
                                .build()
                )
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.CUSTOMER);
    }

    @Override
    public BigDecimal execute(Map<String, Object> arguments) {

        Object typeValue = arguments.get("type");
        Object startDateValue = arguments.get("startDate");

        if (typeValue == null) {
            throw new IllegalArgumentException(
                    "type is required"
            );
        }

        if (startDateValue == null) {
            throw new IllegalArgumentException(
                    "startDate is required"
            );
        }

        Transaction.TransactionType type =
                Transaction.TransactionType.valueOf(
                        typeValue.toString().trim().toUpperCase()
                );

        LocalDate startDate =
                LocalDate.parse(
                        startDateValue.toString().trim()
                );

        Object endDateValue = arguments.get("endDate");

        LocalDate endDate =
                endDateValue == null
                        ? null
                        : LocalDate.parse(
                                endDateValue.toString().trim()
                        );

        return transactionService.getMyTransactionTotal(
                type,
                startDate,
                endDate
        );
    }
}