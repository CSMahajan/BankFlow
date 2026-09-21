package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.TransactionResponse;
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

                    Optional startDate and endDate can be provided
                    to retrieve transactions within a specific date range.

                    Dates must use ISO format yyyy-MM-dd.

                    The customer is determined by the server-side
                    authentication context.
                    """)
                .parameters(
                        Schema.builder()
                                .type("OBJECT")
                                .properties(
                                        Map.of(
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

        LocalDate startDate = parseDate(arguments.get("startDate"));
        LocalDate endDate = parseDate(arguments.get("endDate"));

        return transactionService.getMyTransactions(
                        null,
                        null,
                        startDate,
                        endDate,
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

    private LocalDate parseDate(Object value) {

        if (value == null) {
            return null;
        }

        String date = value.toString().trim();

        if (date.isBlank()) {
            return null;
        }

        return LocalDate.parse(date);
    }
}