package com.bankflow.deposit;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/fixed-deposits")
public class FixedDepositController {
    private final FixedDepositCalculator calculator;

    public FixedDepositController(FixedDepositCalculator calculator) {
        this.calculator = calculator;
    }

    @PostMapping("/quote")
    public FixedDepositCalculator.FixedDepositQuote quote(@Valid @RequestBody QuoteRequest request) {
        return calculator.quote(request.depositAmount(), request.annualInterestRate(), request.tenureYears(), LocalDate.now());
    }

    public record QuoteRequest(
            @NotNull @DecimalMin(value = "10000.01", message = "Deposit amount must be greater than ₹10,000") @Digits(integer = 12, fraction = 2) BigDecimal depositAmount,
            @NotNull @DecimalMin("0.0") BigDecimal annualInterestRate,
            @NotNull Integer tenureYears) {
    }
}
