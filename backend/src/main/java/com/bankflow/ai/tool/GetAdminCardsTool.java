package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.AdminCardResponse;
import com.bankflow.entity.Card;
import com.bankflow.service.CardService;
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
public class GetAdminCardsTool implements AiTool {

    public static final String NAME = "get_admin_cards";

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final CardService cardService;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public FunctionDeclaration functionDeclaration() {
        return FunctionDeclaration.builder()
                .name(NAME)
                .description("""
                        Returns a paginated list of all BankFlow cards
                        for administrators.

                        Optional filters:
                        - search: searches card number, account number,
                          or customer full name
                        - status: ACTIVE, FROZEN, or BLOCKED

                        The result includes card ID, customer name,
                        account number, masked card number, card type,
                        card status, daily limit, and expiry date.

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
                                                                "Optional text used to search card number, account number, or customer name"
                                                        )
                                                        .build(),

                                                "status",
                                                Schema.builder()
                                                        .type("STRING")
                                                        .enum_(List.of(
                                                                "ACTIVE",
                                                                "FROZEN",
                                                                "BLOCKED"
                                                        ))
                                                        .description(
                                                                "Optional card status filter"
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
    public Page<AdminCardResponse> execute(
            Map<String, Object> arguments) {

        String search = parseString(arguments.get("search"));
        Card.CardStatus status =
                parseStatus(arguments.get("status"));

        return cardService.getAllCardsForAdmin(
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

    private Card.CardStatus parseStatus(Object value) {
        String status = parseString(value);

        if (status == null) {
            return null;
        }

        try {
            return Card.CardStatus.valueOf(
                    status.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "status must be ACTIVE, FROZEN, or BLOCKED"
            );
        }
    }
}