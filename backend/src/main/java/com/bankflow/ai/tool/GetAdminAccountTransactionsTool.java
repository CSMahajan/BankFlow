package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.service.TransactionService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetAdminAccountTransactionsTool implements AiTool {

    public static final String NAME = "get_admin_account_transactions";

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

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
                        Returns transactions belonging to one specific
                        BankFlow account for administrators.

                        The account number must be provided.

                        Transactions are returned with the most recent
                        transactions first.

                        This function is available only to administrators.
                        """)
                .parameters(
                        Schema.builder()
                                .type("OBJECT")
                                .properties(
                                        Map.of(
                                                "accountNumber",
                                                Schema.builder()
                                                        .type("STRING")
                                                        .description(
                                                                "The BankFlow account number whose transactions are requested"
                                                        )
                                                        .build()
                                )
                                )
                                .required(List.of("accountNumber"))
                                .build()
                )
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.ADMIN);
    }

    @Override
    public Page<TransactionResponse> execute(
            Map<String, Object> arguments) {

        Object accountNumberValue =
                arguments.get("accountNumber");

        if (accountNumberValue == null) {
            throw new IllegalArgumentException(
                    "accountNumber is required"
            );
        }

        String accountNumber =
                accountNumberValue.toString().trim();

        if (accountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "accountNumber must not be blank"
            );
        }

        PageRequest pageable =
                PageRequest.of(
                        DEFAULT_PAGE,
                        DEFAULT_SIZE,
                        Sort.by(
                                Sort.Direction.DESC,
                                "transactionDate"
                        )
                );

        return transactionService.getAccountTransactionsForAdmin(
                accountNumber,
                pageable
        );
    }
}