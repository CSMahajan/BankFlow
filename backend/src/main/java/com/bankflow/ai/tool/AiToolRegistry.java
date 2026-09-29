package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiToolRegistry {

    private final List<AiTool> tools;

    public List<Tool> getToolsForAudience(AiAudience audience) {

        List<FunctionDeclaration> declarations = tools.stream()
                .filter(tool ->
                        tool.supportedAudiences().contains(audience)
                )
                .map(AiTool::functionDeclaration)
                .toList();

        log.info(
                "AI tools available for audience [{}]: {}",
                audience,
                declarations.stream()
                        .map(declaration -> declaration.name().orElse("unknown"))
                        .toList()
        );

        if (declarations.isEmpty()) {
            return List.of();
        }

        return List.of(
                Tool.builder()
                        .functionDeclarations(declarations)
                        .build()
        );
    }

    public Object execute(
            String toolName,
            AiAudience audience,
            Map<String, Object> arguments) {

        AiTool tool = tools.stream()
                .filter(candidate ->
                        candidate.name().equals(toolName)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Unknown AI tool requested: " + toolName
                        )
                );

        if (!tool.supportedAudiences().contains(audience)) {
            throw new IllegalStateException(
                    "AI tool [" + toolName +
                            "] is not available for audience [" +
                            audience + "]"
            );
        }

        return tool.execute(arguments);
    }
}