package com.bankflow.ai.fraud;

public record FraudSignal(
        FraudSignalType type,
        String description
) {
}