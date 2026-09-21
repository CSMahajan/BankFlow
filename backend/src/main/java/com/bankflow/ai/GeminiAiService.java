package com.bankflow.ai;

import com.bankflow.ai.tool.AiToolRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class GeminiAiService implements AiService {

    private final Client client;
    private final ObjectMapper objectMapper;
    private final RagRetrievalService ragRetrievalService;
    private final AiToolRegistry aiToolRegistry;
    private final AiAudienceResolver aiAudienceResolver;

    @Value("${gemini.model:gemini-3.5-flash-lite}")
    private String model;

    public GeminiAiService(
            @Value("${gemini.api-key}") String apiKey,
            ObjectMapper objectMapper,
            RagRetrievalService ragRetrievalService,
            AiAudienceResolver aiAudienceResolver,
            AiToolRegistry aiToolRegistry) {

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();

        this.objectMapper = objectMapper;
        this.ragRetrievalService = ragRetrievalService;
        this.aiAudienceResolver = aiAudienceResolver;
        this.aiToolRegistry = aiToolRegistry;
    }

    @Override
    public AiResponse ask(String question) {

        AiAudience audience =
                aiAudienceResolver.resolve();

        RagAudience ragAudience =
                audience == AiAudience.CUSTOMER
                        ? RagAudience.CUSTOMER
                        : RagAudience.ADMIN;

        log.info(
                "AI request audience: {}, RAG audience: {}",
                audience,
                ragAudience
        );

        List<RagRetrievedChunk> retrievedChunks =
                ragRetrievalService.retrieve(
                        question,
                        ragAudience
                );

        log.info(
                "RAG retrieved {} chunks for question: {}",
                retrievedChunks.size(),
                question
        );

        for (int i = 0; i < retrievedChunks.size(); i++) {

            RagRetrievedChunk chunk =
                    retrievedChunks.get(i);

            log.info(
                    "RAG chunk {} | source={} | section={}",
                    i + 1,
                    chunk.sourcePath(),
                    chunk.section()
            );
        }

        String ragContext =
                buildRagContext(retrievedChunks);

        String systemInstruction =
                buildSystemInstruction(
                        audience,
                        ragContext
                );

        Content systemContent = Content.fromParts(
                Part.fromText(systemInstruction)
        );

        Schema responseSchema = Schema.builder()
                .type("OBJECT")
                .properties(Map.of(
                        "answer", Schema.builder()
                                .type("STRING")
                                .build(),

                        "category", Schema.builder()
                                .type("STRING")
                                .enum_(List.of(
                                        "BANKFLOW_FEATURE",
                                        "BANKFLOW_TECHNOLOGY",
                                        "BANKFLOW_SECURITY",
                                        "GENERAL",
                                        "UNKNOWN"
                                ))
                                .build()
                ))
                .required(List.of("answer", "category"))
                .build();

        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .systemInstruction(systemContent)
                        .responseMimeType("application/json")
                        .responseSchema(responseSchema)
                        .tools(aiToolRegistry.getToolsForAudience(audience))
                        .build();

        GenerateContentResponse response =
                client.models.generateContent(
                        model,
                        question,
                        config
                );

        if (!Objects.requireNonNull(response.functionCalls()).isEmpty()) {

            FunctionCall functionCall =
                    response.functionCalls().getFirst();

            return handleToolCall(
                    question,
                    response,
                    functionCall,
                    audience,
                    config
            );
        }

        log.info(
                "Gemini raw response: {}",
                response.text()
        );

        return parseResponse(response.text());
    }

    private AiResponse parseResponse(String json) {

        try {
            return objectMapper.readValue(
                    json,
                    AiResponse.class
            );
        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Failed to parse Gemini response",
                    e
            );
        }
    }

    private String buildRagContext(
            List<RagRetrievedChunk> chunks) {

        if (chunks == null || chunks.isEmpty()) {
            return "No relevant BankFlow documentation was retrieved.";
        }

        StringBuilder context =
                new StringBuilder();

        for (int i = 0; i < chunks.size(); i++) {

            RagRetrievedChunk chunk =
                    chunks.get(i);

            context.append("\n--- DOCUMENT ")
                    .append(i + 1)
                    .append(" ---\n");

            context.append("Source: ")
                    .append(chunk.sourcePath())
                    .append("\n");

            if (chunk.section() != null) {

                context.append("Section: ")
                        .append(chunk.section())
                        .append("\n");
            }

            context.append("Content:\n")
                    .append(chunk.content())
                    .append("\n");
        }

        return context.toString();
    }

    private AiResponse handleToolCall(
            String question,
            GenerateContentResponse response,
            FunctionCall functionCall,
            AiAudience audience,
            GenerateContentConfig config) {

        String functionName =
                functionCall.name()
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Gemini function call has no name"
                                )
                        );

        log.info(
                "Gemini requested tool: {}",
                functionName
        );

        Map<String, Object> arguments =
                functionCall.args()
                        .orElseGet(Map::of);

        log.info(
                "Gemini tool arguments: {}",
                arguments
        );

        Object toolResult =
                aiToolRegistry.execute(
                        functionName,
                        audience,
                        arguments
                );

        log.info(
                "Tool [{}] returned result",
                functionName
        );

        return generateFinalResponseAfterToolCall(
                question,
                response,
                functionCall,
                toolResult,
                config
        );
    }

    private AiResponse generateFinalResponseAfterToolCall(
            String question,
            GenerateContentResponse response,
            FunctionCall functionCall,
            Object toolResult,
            GenerateContentConfig config) {

        try {
            String toolResultJson =
                    objectMapper.writeValueAsString(toolResult);

            Content functionResponseContent =
                    Content.fromParts(
                            Part.fromFunctionResponse(
                                    functionCall.name().orElseThrow(
                                            () -> new IllegalStateException(
                                                    "Gemini function call has no name"
                                            )
                                    ),
                                    Map.of(
                                            "result",
                                            toolResultJson
                                    )
                            )
                    );

            GenerateContentResponse finalResponse =
                    client.models.generateContent(
                            model,
                            List.of(
                                    Content.fromParts(
                                            Part.fromText(question)
                                    ),
                                    response.candidates()
                                            .orElseThrow()
                                            .getFirst()
                                            .content()
                                            .orElseThrow(),
                                    functionResponseContent
                            ),
                            config
                    );

            log.info(
                    "Gemini final response after tool call: {}",
                    finalResponse.text()
            );

            return parseResponse(finalResponse.text());

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize tool result",
                    e
            );
        }
    }

    private String buildSystemInstruction(
            AiAudience audience,
            String ragContext) {

        if (audience == AiAudience.CUSTOMER) {
            return """
                    %s
                    
                    CUSTOMER ASSISTANT RULES:
                    
                    The user is an authenticated banking customer.
                    
                    Answer in simple, customer-friendly language.
                    
                    Never expose internal implementation details,
                    administrative information, or another customer's data.
                    
                    For customer-specific live information, use the available
                    backend tools instead of guessing.
                    
                    Never invent account balances, transactions, cards,
                    loans, or other customer-specific information.
                    
                    When a backend tool returns a list of records requested
                    by the customer, include every returned record unless
                    the customer explicitly asks for a subset, summary, or limit.
                    
                    Do not silently omit records from tool results.
                    
                    Use the retrieved BankFlow documentation when answering
                    questions about BankFlow functionality.
                    
                    If the requested information is unavailable,
                    clearly say that it is unavailable.
                    
                    RETRIEVED BANKFLOW DOCUMENTATION:
                    ---
                    %s
                    ---
                    """.formatted(
                    BankFlowAiContext.CONTEXT,
                    ragContext
            );
        }

        return """
                %s
                
                ADMINISTRATOR ASSISTANT RULES:
                
                The user is an authenticated BankFlow administrator.
                
                You may explain BankFlow's technical architecture,
                APIs, backend functionality, administrative functionality,
                security implementation, database-related concepts,
                and other technical documentation available through
                the retrieved context.
                
                Never expose customer-specific information unless it is
                explicitly provided by an authorized backend tool.
                
                Never invent system behavior, APIs, database information,
                or administrative capabilities.
                
                Use retrieved BankFlow documentation as the source of truth.
                
                RETRIEVED BANKFLOW DOCUMENTATION:
                ---
                %s
                ---
                """.formatted(
                BankFlowAiContext.CONTEXT,
                ragContext
        );
    }
}