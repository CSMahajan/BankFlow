package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.AdminUserLoanResponse;
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
public class GetAdminUserLoansTool implements AiTool {

    public static final String NAME = "get_admin_user_loans";

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
                        Returns all loans belonging to one specific
                        BankFlow user for administrators.

                        The user ID must be provided.

                        The result includes loan number, loan type,
                        loan status, principal amount, remaining balance,
                        monthly EMI, tenure, and next due date.

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
                                                                "The ID of the BankFlow user whose loans are requested"
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
    public List<AdminUserLoanResponse> execute(
            Map<String, Object> arguments) {

        Object userIdValue = arguments.get("userId");

        if (userIdValue == null) {
            throw new IllegalArgumentException(
                    "userId is required"
            );
        }

        Long userId =
                userIdValue instanceof Number number
                        ? number.longValue()
                        : Long.valueOf(
                                userIdValue.toString().trim()
                        );

        return userService.getUserLoans(userId);
    }
}