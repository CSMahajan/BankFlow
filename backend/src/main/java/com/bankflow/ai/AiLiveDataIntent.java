package com.bankflow.ai;

import java.util.Map;

public record AiLiveDataIntent(
        String toolName,
        Map<String, Object> arguments
) {
}