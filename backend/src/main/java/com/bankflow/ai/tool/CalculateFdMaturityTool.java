package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.FdCalculatorResponse;
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
public class CalculateFdMaturityTool implements AiTool {

    public static final String NAME = "calculate_fd_maturity";
    public static final String TENURE_YEARS = "tenureYears";
    public static final String DEPOSIT_AMOUNT = "depositAmount";

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
                        Calculates the maturity amount and interest earned
                        for a fixed deposit.

                        The deposit amount and tenure in years must be provided.

                        Supported tenure options are 1, 3, or 5 years.
                        The calculation is performed by the backend.
                        This function does not create a fixed deposit.
                        """)
                .parameters(
                        Schema.builder()
                                .type("OBJECT")
                                .properties(
                                        Map.of(
                                                DEPOSIT_AMOUNT,
                                                Schema.builder()
                                                        .type("NUMBER")
                                                        .description(
                                                                "The FD deposit amount in INR. Must be greater than Rs. 10,000."
                                                        )
                                                        .build(),
                                                TENURE_YEARS,
                                                Schema.builder()
                                                        .type("INTEGER")
                                                        .description(
                                                                "FD tenure in years. Supported values are 1, 3, or 5."
                                                        )
                                                        .build()
                                        )
                                )
                                .required(List.of(
                                        DEPOSIT_AMOUNT,
                                        TENURE_YEARS
                                ))
                                .build()
                )
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.CUSTOMER);
    }

    @Override
    public FdCalculatorResponse execute(
            Map<String, Object> arguments) {

        Object depositAmountValue =
                arguments.get(DEPOSIT_AMOUNT);

        if (depositAmountValue == null) {
            throw new IllegalArgumentException(
                    "depositAmount is required"
            );
        }

        Object tenureYearsValue =
                arguments.get(TENURE_YEARS);

        if (tenureYearsValue == null) {
            throw new IllegalArgumentException(
                    "tenureYears is required"
            );
        }

        java.math.BigDecimal depositAmount;

        if (depositAmountValue instanceof Number number) {
            depositAmount =
                    new java.math.BigDecimal(number.toString());
        } else {
            depositAmount =
                    new java.math.BigDecimal(
                            depositAmountValue.toString()
                    );
        }

        Integer tenureYears;

        if (tenureYearsValue instanceof Number number) {
            tenureYears = number.intValue();
        } else {
            tenureYears =
                    Integer.valueOf(
                            tenureYearsValue.toString()
                    );
        }

        if (depositAmount.compareTo(
                new java.math.BigDecimal("10000.00")) <= 0) {
            throw new IllegalArgumentException(
                    "depositAmount must be greater than Rs. 10,000"
            );
        }

        return fixedDepositService.calculateMaturity(
                new com.bankflow.dto.FdCalculatorRequest(
                        depositAmount,
                        tenureYears
                )
        );
    }
}