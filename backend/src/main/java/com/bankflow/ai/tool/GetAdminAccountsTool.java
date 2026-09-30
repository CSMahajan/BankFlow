package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.AccountResponse;
import com.bankflow.entity.Account;
import com.bankflow.service.AccountService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetAdminAccountsTool implements AiTool {

    public static final String NAME = "get_admin_accounts";

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

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
                    Returns a paginated list of all BankFlow accounts
                    for administrators.

                    Optional filters:
                    - search: text to search account information
                    - status: filter by ACTIVE, FROZEN, or INACTIVE

                    The result includes account number, customer name,
                    customer email, account type, branch, current balance,
                    account status, and account creation date.

                    This function is available only to administrators.
                    """)
                .parameters(
                        Schema.builder()
                                .type("OBJECT")
                                .properties(
                                        Map.of(
                                                "search",
                                                Schema.builder()
                                                        .type("STRING")
                                                        .description(
                                                                "Optional text used to search accounts"
                                                        )
                                                        .build(),

                                                "status",
                                                Schema.builder()
                                                        .type("STRING")
                                                        .enum_(List.of(
                                                                "ACTIVE",
                                                                "FROZEN",
                                                                "INACTIVE"
                                                        ))
                                                        .description(
                                                                "Optional account status filter"
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
        return Set.of(AiAudience.ADMIN);
    }

    @Override
    public Page<AccountResponse> execute(
            Map<String, Object> arguments) {

        String search = parseString(arguments.get("search"));
        Account.AccountStatus status =
                parseStatus(arguments.get("status"));

        return accountService.getAllAccountsForAdmin(
                DEFAULT_PAGE,
                DEFAULT_SIZE,
                search,
                status
        );
    }

    private String parseString(Object value) {
        if (value == null) {
            return null;
        }

        String valueString = value.toString().trim();

        return valueString.isBlank()
                ? null
                : valueString;
    }

    private Account.AccountStatus parseStatus(Object value) {
        String status = parseString(value);

        if (status == null) {
            return null;
        }

        try {
            return Account.AccountStatus.valueOf(
                    status.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "status must be ACTIVE, FROZEN, or INACTIVE"
            );
        }
    }
}