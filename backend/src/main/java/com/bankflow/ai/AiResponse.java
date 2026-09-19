package com.bankflow.ai;

public record AiResponse(
        String answer,
        Category category
) {
    public enum Category {
        BANKFLOW_FEATURE,
        BANKFLOW_TECHNOLOGY,
        BANKFLOW_SECURITY,
        GENERAL,
        UNKNOWN
    }
}