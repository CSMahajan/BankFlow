package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.service.TransactionService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetAdminTransactionDetailsTool implements AiTool {

    public static final String NAME = "get_admin_transaction_details";
    public static final String TRANSACTION_ID = "transactionId";

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
                        Returns details of one specific BankFlow transaction
                        for administrators.

                        The transaction ID must be provided.

                        This function is available only to administrators.
                        """)
                .parameters(
                        Schema.builder()
                                .type("OBJECT")
                                .properties(
                                        Map.of(
                                                TRANSACTION_ID,
                                                Schema.builder()
                                                        .type("STRING")
                                                        .description(
                                                                "The BankFlow transaction ID"
                                                        )
                                                        .build()
                                )
                                )
                                .required(List.of(TRANSACTION_ID))
                                .build()
                )
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.ADMIN);
    }

    @Override
    public TransactionResponse execute(
            Map<String, Object> arguments) {

        Object transactionIdValue =
                arguments.get(TRANSACTION_ID);

        if (transactionIdValue == null) {
            throw new IllegalArgumentException(
                    "transactionId is required"
            );
        }

        String transactionId =
                transactionIdValue.toString().trim();

        if (transactionId.isBlank()) {
            throw new IllegalArgumentException(
                    "transactionId must not be blank"
            );
        }

        return transactionService.getTransactionDetails(
                transactionId
        );
    }
}