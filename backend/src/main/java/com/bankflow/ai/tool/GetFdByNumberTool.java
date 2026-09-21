package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.FdResponse;
import com.bankflow.service.FixedDepositService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetFdByNumberTool implements AiTool {

    public static final String NAME = "get_fd_by_number";

    private final FixedDepositService fixedDepositService;

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
                        fixed deposits.

                        The FD number must be provided.

                        The backend verifies that the requested fixed deposit
                        belongs to the authenticated customer.
                        """)
                .parameters(
                        Schema.builder()
                                .type("OBJECT")
                                .properties(
                                        Map.of(
                                                "fdNumber",
                                                Schema.builder()
                                                        .type("STRING")
                                                        .description(
                                                                "The fixed deposit number whose details are requested"
                                                        )
                                                        .build()
                                        )
                                )
                                .required(List.of("fdNumber"))
                                .build()
                )
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.CUSTOMER);
    }

    @Override
    public FdResponse execute(Map<String, Object> arguments) {

        Object fdNumberValue = arguments.get("fdNumber");

        if (fdNumberValue == null) {
            throw new IllegalArgumentException(
                    "fdNumber is required"
            );
        }

        String fdNumber = fdNumberValue.toString().trim();

        if (fdNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "fdNumber must not be blank"
            );
        }

        return fixedDepositService.getFdByNumber(fdNumber);
    }
}