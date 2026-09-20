package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.service.TransactionService;
import com.google.genai.types.FunctionDeclaration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
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
                        Returns the authenticated customer's
                        most recent transactions.

                        The customer is determined by the server-side
                        authentication context. This function takes
                        no parameters.
                        """)
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
}