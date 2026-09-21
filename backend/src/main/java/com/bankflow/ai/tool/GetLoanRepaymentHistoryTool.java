package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.RepaymentResponse;
import com.bankflow.service.LoanService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetLoanRepaymentHistoryTool implements AiTool {

    public static final String NAME = "get_loan_repayment_history";

    private final LoanService loanService;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public FunctionDeclaration functionDeclaration() {
        return FunctionDeclaration.builder()
                .name(NAME)
                .description("""
                    Returns the repayment history for one of the
                    authenticated customer's loans.

                    The loan number must be provided.

                    The backend verifies that the requested loan
                    belongs to the authenticated customer.
                    """)
                .parameters(
                        Schema.builder()
                                .type("OBJECT")
                                .properties(
                                        Map.of(
                                                "loanNumber",
                                                Schema.builder()
                                                        .type("STRING")
                                                        .description(
                                                                "The loan number whose repayment history is requested"
                                                        )
                                                        .build()
                                        )
                                )
                                .required(List.of("loanNumber"))
                                .build()
                )
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.CUSTOMER);
    }

    @Override
    public List<RepaymentResponse> execute(
            Map<String, Object> arguments) {

        Object loanNumberValue = arguments.get("loanNumber");

        if (loanNumberValue == null) {
            throw new IllegalArgumentException(
                    "loanNumber is required"
            );
        }

        String loanNumber = loanNumberValue.toString();

        if (loanNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "loanNumber must not be blank"
            );
        }

        return loanService.getRepaymentHistory(loanNumber);
    }
}