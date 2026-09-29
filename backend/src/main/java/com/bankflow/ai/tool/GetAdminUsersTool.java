package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.UserSummaryResponse;
import com.bankflow.service.UserService;
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
public class GetAdminUsersTool implements AiTool {

    public static final String NAME = "get_admin_users";

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final UserService userService;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public FunctionDeclaration functionDeclaration() {
        return FunctionDeclaration.builder()
                .name(NAME)
                .description("""
                        Returns a paginated list of BankFlow users for administrators.

                        Optional filters:
                        - search: case-insensitive partial match against the user's
                          full name or email address
                        - role: filter by CUSTOMER or ADMIN

                        The result includes user ID, full name, email, role,
                        account creation date, and account count.

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
                                                                "Optional text to search in user full name or email address"
                                                        )
                                                        .build(),
                                                "role",
                                                Schema.builder()
                                                        .type("STRING")
                                                        .enum_(List.of("CUSTOMER", "ADMIN"))
                                                        .description(
                                                                "Optional user role filter"
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
    public Page<UserSummaryResponse> execute(
            Map<String, Object> arguments) {

        String search = parseString(arguments.get("search"));
        String role = parseRole(arguments.get("role"));

        return userService.getAllUsers(
                DEFAULT_PAGE,
                DEFAULT_SIZE,
                search,
                role
        );
    }

    private String parseString(Object value) {
        if (value == null) {
            return null;
        }

        String valueString = value.toString().trim();

        return valueString.isBlank() ? null : valueString;
    }

    private String parseRole(Object value) {
        String role = parseString(value);

        if (role == null) {
            return null;
        }

        role = role.toUpperCase();

        if (!role.equals("CUSTOMER")
                && !role.equals("ADMIN")) {
            throw new IllegalArgumentException(
                    "role must be CUSTOMER or ADMIN"
            );
        }

        return role;
    }
}