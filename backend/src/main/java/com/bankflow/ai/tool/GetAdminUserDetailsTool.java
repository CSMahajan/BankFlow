package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.UserDetailsResponse;
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
public class GetAdminUserDetailsTool implements AiTool {

    public static final String NAME = "get_admin_user_details";

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
                        Returns detailed information about one BankFlow user
                        for administrators.

                        The user ID must be provided.

                        The result includes:
                        - user profile information
                        - account count
                        - card count
                        - loan count
                        - fixed deposit count
                        - total balance
                        - outstanding loan amount

                        This function is available only to administrators.
                        """)
                .parameters(
                        Schema.builder()
                                .type("OBJECT")
                                .properties(
                                        Map.of(
                                                "userId",
                                                Schema.builder()
                                                        .type("INTEGER")
                                                        .description(
                                                                "The ID of the BankFlow user"
                                                        )
                                                        .build()
                                        )
                                )
                                .required(List.of("userId"))
                                .build()
                )
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.ADMIN);
    }

    @Override
    public UserDetailsResponse execute(
            Map<String, Object> arguments) {

        Object userIdValue = arguments.get("userId");

        if (userIdValue == null) {
            throw new IllegalArgumentException(
                    "userId is required"
            );
        }

        Long userId;

        if (userIdValue instanceof Number number) {
            userId = number.longValue();
        } else {
            userId = Long.valueOf(
                    userIdValue.toString().trim()
            );
        }

        return userService.getUserDetails(userId);
    }
}