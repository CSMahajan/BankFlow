package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.LoanResponse;
import com.bankflow.service.LoanService;
import com.google.genai.types.FunctionDeclaration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetMyLoansTool implements AiTool {

    public static final String NAME = "get_my_loans";

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
                        Returns the authenticated customer's loans.

                        The customer is determined by the server-side
                        authentication context. This function takes no parameters.
                        """)
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.CUSTOMER);
    }

    @Override
    public List<LoanResponse> execute() {
        return loanService.getMyLoans();
    }
}