package com.bankflow.ai.fraud;

import java.util.List;

public record FraudAssessment(
        Long userId,
        List<FraudSignal> signals
) {

    public boolean suspicious() {
        return !signals.isEmpty();
    }
}