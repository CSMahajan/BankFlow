package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.AdminUserFixedDepositResponse;
import com.bankflow.service.UserService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetAdminUserFixedDepositsTool implements AiTool {

    public static final String NAME = "get_admin_user_fixed_deposits";
    public static final String USER_ID = "userId";

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
                        Returns the fixed deposits belonging to one specific user.

                        This function is available only to administrators.
                        The userId must be provided.
                        """)
                .parameters(
                        Schema.builder()
                                .type("OBJECT")
                                .properties(
                                        Map.of(
                                                USER_ID,
                                                Schema.builder()
                                                        .type("INTEGER")
                                                        .description(
                                                                "The numeric ID of the user whose fixed deposits are requested"
                                                        )
                                                        .build()
                                        )
                                )
                                .required(List.of(USER_ID))
                                .build()
                )
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.ADMIN);
    }

    @Override
    public List<AdminUserFixedDepositResponse> execute(
            Map<String, Object> arguments) {

        Object userIdValue = arguments.get(USER_ID);

        if (userIdValue == null) {
            throw new IllegalArgumentException(
                    "userId is required"
            );
        }

        Long userId;

        if (userIdValue instanceof Number number) {
            userId = number.longValue();
        } else {
            try {
                userId = Long.parseLong(
                        userIdValue.toString().trim()
                );
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "userId must be a valid numeric ID",
                        e
                );
            }
        }

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "userId must be greater than zero"
            );
        }

        return userService.getUserFixedDeposits(userId);
    }
}