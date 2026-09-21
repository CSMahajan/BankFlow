package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.service.AccountService;
import com.google.genai.types.FunctionDeclaration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetMyTotalBalanceTool implements AiTool {

    public static final String NAME = "get_my_total_balance";

    private final AccountService accountService;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public FunctionDeclaration functionDeclaration() {

        return FunctionDeclaration.builder()
                .name(NAME)
                .description("""
                        Returns the authenticated customer's total
                        balance across all of their bank accounts.

                        The customer is determined by the server-side
                        authentication context. This function takes no parameters.

                        The total balance is calculated by the backend.
                        """)
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.CUSTOMER);
    }

    @Override
    public BigDecimal execute() {
        return accountService.getMyTotalBalance();
    }
}