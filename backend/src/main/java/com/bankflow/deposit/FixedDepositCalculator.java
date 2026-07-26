package com.bankflow.deposit;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Set;

@Service
public class FixedDepositCalculator {
    private static final BigDecimal MINIMUM_DEPOSIT = new BigDecimal("10000");
    private static final Set<Integer> ALLOWED_TENURES = Set.of(1, 3, 5);

    public FixedDepositQuote quote(BigDecimal principal, BigDecimal annualRate, int years, LocalDate depositDate) {
        if (principal == null || principal.compareTo(MINIMUM_DEPOSIT) <= 0)
            throw new IllegalArgumentException("Deposit amount must be greater than ₹10,000");
        if (!ALLOWED_TENURES.contains(years)) throw new IllegalArgumentException("Tenure must be 1, 3, or 5 years");
        if (annualRate == null || annualRate.signum() < 0)
            throw new IllegalArgumentException("Interest rate must be zero or positive");
        BigDecimal factor = BigDecimal.ONE.add(annualRate.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP));
        BigDecimal maturity = principal.multiply(factor.pow(years)).setScale(2, RoundingMode.HALF_UP);
        return new FixedDepositQuote(principal, annualRate, years, depositDate, depositDate.plusYears(years), maturity);
    }

    public record FixedDepositQuote(BigDecimal principal, BigDecimal annualRate, int tenureYears, LocalDate depositDate,
                                    LocalDate maturityDate, BigDecimal maturityAmount) {
    }
}
