package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.google.genai.types.FunctionDeclaration;

import java.util.Set;

public interface AiTool {

    String name();

    FunctionDeclaration functionDeclaration();

    Set<AiAudience> supportedAudiences();

    Object execute();
}