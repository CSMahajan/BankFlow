package com.bankflow.ai.mcp;

import com.bankflow.ai.AiAudience;
import com.bankflow.ai.tool.AiTool;
import com.bankflow.ai.tool.AiToolRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class McpAiToolAdapter {

    private final AiToolRegistry aiToolRegistry;
    private final ObjectMapper objectMapper;
    private final List<AiTool> aiTools;

    public McpAiToolAdapter(
            AiToolRegistry aiToolRegistry,
            ObjectMapper objectMapper,
            List<AiTool> aiTools) {

        this.aiToolRegistry = aiToolRegistry;
        this.objectMapper = objectMapper;
        this.aiTools = List.copyOf(aiTools);
    }

    public List<McpServerFeatures.SyncToolSpecification> createToolSpecifications() {

        return aiTools.stream()
                .map(this::createToolSpecification)
                .toList();
    }

    public boolean isVisible(
            McpSchema.Tool tool,
            AiAudience audience) {

        return aiTools.stream()
                .filter(aiTool -> aiTool.name().equals(tool.name()))
                .findFirst()
                .map(aiTool ->
                        aiTool.supportedAudiences().contains(audience))
                .orElse(false);
    }

    private McpServerFeatures.SyncToolSpecification createToolSpecification(
            AiTool aiTool) {

        FunctionDeclaration declaration =
                aiTool.functionDeclaration();

        Tool tool = Tool.builder(
                        aiTool.name(),
                        toMcpInputSchema(declaration)
                )
                .description(
                        declaration.description().orElse("")
                )
                .build();

        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, request) ->
                        execute(
                                aiTool,
                                exchange.transportContext().get(
                                        McpServerConfig.AUTHENTICATION
                                ),
                                exchange.transportContext().get(
                                        McpServerConfig.AUDIENCE
                                ),
                                exchange.transportContext().get(
                                        McpServerConfig.TRACE_ID
                                ),
                                request
                        ))
                .build();
    }

    private CallToolResult execute(
            AiTool aiTool,
            Object authenticationValue,
            Object audienceValue,
            Object traceIdValue,
            McpSchema.CallToolRequest request) {

        if (!(authenticationValue
                instanceof org.springframework.security.core.Authentication authentication)) {

            log.warn(
                    "MCP TOOL REJECTED | tool={} | reason=AUTHENTICATION_REQUIRED",
                    request.name()
            );

            return errorResult("Authenticated user is required.");
        }

        if (!(audienceValue instanceof AiAudience audience)) {

            log.warn(
                    "MCP TOOL REJECTED | tool={} | reason=AUDIENCE_REQUIRED",
                    request.name()
            );

            return errorResult("Authenticated BankFlow audience is required.");
        }

        String userId = authentication.getName();
        String toolName = request.name();
        String traceId =
                traceIdValue instanceof String value && !value.isBlank()
                        ? value
                        : "unknown";

        if (!aiTool.supportedAudiences().contains(audience)) {

            log.warn(
                    "MCP TOOL REJECTED | traceId={} | userId={} | audience={} | tool={} | reason=UNAUTHORIZED_AUDIENCE",
                    traceId,
                    userId,
                    audience,
                    toolName
            );

            return errorResult(
                    "Tool [" + aiTool.name()
                            + "] is not available for audience ["
                            + audience + "]."
            );
        }

        long startTime = System.nanoTime();

        log.info(
                "MCP TOOL START | traceId={} | userId={} | audience={} | tool={} | arguments={}",
                traceId,
                userId,
                audience,
                toolName,
                request.arguments()
        );

        var previousContext =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext();

        try {
            var context =
                    org.springframework.security.core.context.SecurityContextHolder
                            .createEmptyContext();

            context.setAuthentication(authentication);

            org.springframework.security.core.context.SecurityContextHolder
                    .setContext(context);

            Object result = aiToolRegistry.execute(
                    toolName,
                    audience,
                    request.arguments()
            );

            String json =
                    objectMapper.writeValueAsString(result);

            long durationMs =
                    (System.nanoTime() - startTime) / 1_000_000;

            log.info(
                    "MCP TOOL END | traceId={} | userId={} | audience={} | tool={} | status=SUCCESS | durationMs={}",
                    traceId,
                    userId,
                    audience,
                    toolName,
                    durationMs
            );

            return CallToolResult.builder()
                    .content(List.of(new TextContent(json)))
                    .build();

        } catch (Exception e) {

            long durationMs =
                    (System.nanoTime() - startTime) / 1_000_000;

            log.error(
                    "MCP TOOL END | traceId={} | userId={} | audience={} | tool={} | status=ERROR | exception={} | durationMs={}",
                    traceId,
                    userId,
                    audience,
                    toolName,
                    e.getClass().getSimpleName(),
                    durationMs,
                    e
            );

            return errorResult(
                    e.getMessage() == null
                            ? "MCP tool execution failed."
                            : e.getMessage()
            );

        } finally {
            org.springframework.security.core.context.SecurityContextHolder
                    .setContext(previousContext);
        }
    }

    private Map<String, Object> toMcpInputSchema(
            FunctionDeclaration declaration) {

        if (declaration.parameters().isPresent()) {
            return toMcpSchema(declaration.parameters().orElseThrow());
        }

        if (declaration.parametersJsonSchema().isPresent()) {
            return objectMapper.convertValue(
                    declaration.parametersJsonSchema().orElseThrow(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    }
            );
        }

        return Map.of("type", "object");
    }

    private Map<String, Object> toMcpSchema(Schema schema) {

        Map<String, Object> result = new LinkedHashMap<>();

        schema.type().ifPresent(type ->
                result.put(
                        "type",
                        type.toString().toLowerCase(Locale.ROOT)
                )
        );

        schema.description().ifPresent(value ->
                result.put("description", value)
        );

        schema.format().ifPresent(value ->
                result.put("format", value)
        );

        schema.enum_().ifPresent(value ->
                result.put("enum", value)
        );

        schema.required().ifPresent(value ->
                result.put("required", value)
        );

        schema.properties().ifPresent(properties -> {
            Map<String, Object> mappedProperties =
                    new LinkedHashMap<>();

            properties.forEach((name, propertySchema) ->
                    mappedProperties.put(
                            name,
                            toMcpSchema(propertySchema)
                    )
            );

            result.put("properties", mappedProperties);
        });

        schema.items().ifPresent(value ->
                result.put("items", toMcpSchema(value))
        );

        schema.anyOf().ifPresent(value -> {
            List<Map<String, Object>> anyOf =
                    new ArrayList<>();

            value.forEach(item ->
                    anyOf.add(toMcpSchema(item))
            );

            result.put("anyOf", anyOf);
        });

        schema.default_().ifPresent(value ->
                result.put("default", value)
        );

        schema.example().ifPresent(value ->
                result.put("examples", List.of(value))
        );

        schema.pattern().ifPresent(value ->
                result.put("pattern", value)
        );

        schema.minimum().ifPresent(value ->
                result.put("minimum", value)
        );

        schema.maximum().ifPresent(value ->
                result.put("maximum", value)
        );

        schema.minLength().ifPresent(value ->
                result.put("minLength", value)
        );

        schema.maxLength().ifPresent(value ->
                result.put("maxLength", value)
        );

        schema.minItems().ifPresent(value ->
                result.put("minItems", value)
        );

        schema.maxItems().ifPresent(value ->
                result.put("maxItems", value)
        );

        schema.minProperties().ifPresent(value ->
                result.put("minProperties", value)
        );

        schema.maxProperties().ifPresent(value ->
                result.put("maxProperties", value)
        );

        schema.nullable().ifPresent(value ->
                result.put("nullable", value)
        );

        schema.title().ifPresent(value ->
                result.put("title", value)
        );

        return result;
    }

    private CallToolResult errorResult(String message) {

        return CallToolResult.builder()
                .content(List.of(new TextContent(message)))
                .isError(true)
                .build();
    }
}
