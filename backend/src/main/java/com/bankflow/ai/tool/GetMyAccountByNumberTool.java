package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.AccountResponse;
import com.bankflow.service.AccountService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetMyAccountByNumberTool implements AiTool {

    public static final String NAME = "get_my_account_by_number";
    public static final String ACCOUNT_NUMBER = "accountNumber";

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
                        Returns details of one of the authenticated customer's
                        bank accounts.

                        The account number must be provided.

                        The backend verifies that the requested account
                        belongs to the authenticated customer.
                        """)
                .parameters(
                        Schema.builder()
                                .type("OBJECT")
                                .properties(
                                        Map.of(
                                                ACCOUNT_NUMBER,
                                                Schema.builder()
                                                        .type("STRING")
                                                        .description(
                                                                "The bank account number whose details are requested"
                                                        )
                                                        .build()
                                        )
                                )
                                .required(List.of(ACCOUNT_NUMBER))
                                .build()
                )
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.CUSTOMER);
    }

    @Override
    public AccountResponse execute(
            Map<String, Object> arguments) {

        Object accountNumberValue =
                arguments.get(ACCOUNT_NUMBER);

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

        return accountService.getAccountByNumber(accountNumber);
    }
}