package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.google.genai.types.FunctionDeclaration;

import java.util.Map;
import java.util.Set;

public interface AiTool {

    String name();

    FunctionDeclaration functionDeclaration();

    Set<AiAudience> supportedAudiences();

    default Object execute() {
        throw new UnsupportedOperationException(
                "This tool requires arguments"
        );
    }

    default Object execute(Map<String, Object> arguments) {
        return execute();
    }
}