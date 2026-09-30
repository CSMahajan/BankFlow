package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.LoanResponse;
import com.bankflow.entity.Loan;
import com.bankflow.service.LoanService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetAdminLoansTool implements AiTool {

    public static final String NAME = "get_admin_loans";

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

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
                        Returns pending loan applications in BankFlow
                        for administrators.

                        Optional filters:
                        - search: searches loan number, customer full name,
                          or disbursement account number
                        - loanType: PERSONAL, VEHICLE, or HOME

                        The result includes loan details such as loan number,
                        customer, loan type, principal amount, interest rate,
                        EMI, remaining balance, status, and dates.

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
                                                                "Optional text used to search loan number, customer name, or disbursement account number"
                                                        )
                                                        .build(),

                                                "loanType",
                                                Schema.builder()
                                                        .type("STRING")
                                                        .enum_(List.of(
                                                                "PERSONAL",
                                                                "VEHICLE",
                                                                "HOME"
                                                        ))
                                                        .description(
                                                                "Optional loan type filter"
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
    public Page<LoanResponse> execute(
            Map<String, Object> arguments) {

        String search = parseString(arguments.get("search"));
        Loan.LoanType loanType =
                parseLoanType(arguments.get("loanType"));

        PageRequest pageable =
                PageRequest.of(
                        DEFAULT_PAGE,
                        DEFAULT_SIZE,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        return loanService.getPendingLoans(
                search,
                loanType,
                pageable
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

    private Loan.LoanType parseLoanType(Object value) {
        String loanType = parseString(value);

        if (loanType == null) {
            return null;
        }

        try {
            return Loan.LoanType.valueOf(
                    loanType.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "loanType must be PERSONAL, VEHICLE, or HOME"
            );
        }
    }
}