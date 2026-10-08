package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.ai.fraud.FraudAssessment;
import com.bankflow.ai.fraud.FraudDetectionService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetFraudAssessmentTool implements AiTool {

    public static final String NAME = "get_fraud_assessment";
    public static final String USER_ID = "userId";

    private final FraudDetectionService fraudDetectionService;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public FunctionDeclaration functionDeclaration() {

        return FunctionDeclaration.builder()
                .name(NAME)
                .description("""
                        Performs a read-only fraud risk assessment for one specific
                        BankFlow user.

                        The assessment is based on deterministic transaction,
                        account, loan, and fixed-deposit activity signals.

                        The result contains detected suspicious signals and the
                        evidence supporting each signal.

                        This function does not block accounts, freeze cards,
                        reject loans, or take any other action.

                        The user ID must be provided.

                        This function is available only to administrators.
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
                                                                "The ID of the BankFlow user to assess"
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
    public FraudAssessment execute(
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
            userId = Long.valueOf(
                    userIdValue.toString().trim()
            );
        }

        return fraudDetectionService.assessUser(userId);
    }
}