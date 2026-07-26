package com.bankflow.deposit;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FixedDepositCalculatorTest {
    private final FixedDepositCalculator calculator = new FixedDepositCalculator();

    @Test
    void calculatesCompoundedMaturityAndDate() {
        var quote = calculator.quote(new BigDecimal("25000"), new BigDecimal("7.1"), 3, LocalDate.of(2026, 7, 26));
        assertEquals(new BigDecimal("30712.02"), quote.maturityAmount());
        assertEquals(LocalDate.of(2029, 7, 26), quote.maturityDate());
    }

    @Test
    void rejectsMinimumAmountAndInvalidTenure() {
        assertThrows(IllegalArgumentException.class, () -> calculator.quote(new BigDecimal("10000"), new BigDecimal("7.1"), 1, LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () -> calculator.quote(new BigDecimal("25000"), new BigDecimal("7.1"), 2, LocalDate.now()));
    }
}
