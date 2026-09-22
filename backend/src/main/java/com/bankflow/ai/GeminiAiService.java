package com.bankflow.ai;

import com.bankflow.ai.tool.AiToolRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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

    private static final int MAX_TOOL_ROUNDS = 5;

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

        String currentDate =
                LocalDate.now(ZoneId.systemDefault()).toString();

        String systemInstruction =
                buildSystemInstruction(
                        audience,
                        ragContext,
                        currentDate
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

            return handleToolCalls(
                    question,
                    response,
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

    private AiResponse handleToolCalls(
            String question,
            GenerateContentResponse initialResponse,
            AiAudience audience,
            GenerateContentConfig config) {

        List<Content> conversation = new ArrayList<>();

        conversation.add(
                Content.fromParts(
                        Part.fromText(question)
                )
        );

        GenerateContentResponse response = initialResponse;

        for (int round = 1; round <= MAX_TOOL_ROUNDS; round++) {

            List<FunctionCall> functionCalls =
                    Objects.requireNonNull(
                            response.functionCalls()
                    );

            if (functionCalls.isEmpty()) {

                log.info(
                        "Gemini completed tool loop after {} round(s)",
                        round - 1
                );

                return parseResponse(response.text());
            }

            log.info(
                    "Gemini requested {} tool call(s) in round {}",
                    functionCalls.size(),
                    round
            );

            Content modelResponse =
                    response.candidates()
                            .orElseThrow(
                                    () -> new IllegalStateException(
                                            "Gemini response contains no candidates"
                                    )
                            )
                            .getFirst()
                            .content()
                            .orElseThrow(
                                    () -> new IllegalStateException(
                                            "Gemini response contains no content"
                                    )
                            );

            conversation.add(modelResponse);

            List<Part> functionResponseParts = new ArrayList<>();

            for (FunctionCall functionCall : functionCalls) {

                String functionName =
                        functionCall.name()
                                .orElseThrow(
                                        () -> new IllegalStateException(
                                                "Gemini function call has no name"
                                        )
                                );

                Map<String, Object> arguments =
                        functionCall.args()
                                .orElseGet(Map::of);

                log.info(
                        "Gemini requested tool: {}",
                        functionName
                );

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

                try {

                    String toolResultJson =
                            objectMapper.writeValueAsString(
                                    toolResult
                            );

                    functionResponseParts.add(
                            Part.fromFunctionResponse(
                                    functionName,
                                    Map.of(
                                            "result",
                                            toolResultJson
                                    )
                            )
                    );

                } catch (JsonProcessingException e) {

                    throw new IllegalStateException(
                            "Failed to serialize tool result",
                            e
                    );
                }
            }

            conversation.add(
                    Content.fromParts(
                            functionResponseParts.toArray(new Part[0])
                    )
            );

            response =
                    client.models.generateContent(
                            model,
                            conversation,
                            config
                    );

            log.info(
                    "Gemini response received after tool round {}",
                    round
            );
        }

        throw new IllegalStateException(
                "Maximum AI tool-calling rounds exceeded: "
                        + MAX_TOOL_ROUNDS
        );
    }

    private String buildSystemInstruction(
            AiAudience audience,
            String ragContext,
            String currentDate) {

        if (audience == AiAudience.CUSTOMER) {
            return """
                    %s
                    
                    CURRENT DATE:
                    %s
                    
                    DATE INTERPRETATION:
                    The current date is provided above as the temporal reference.
                    
                    When the user provides dates without a year, interpret them using
                    natural calendar context relative to the current date.
                    
                    For a date range where the user clearly refers to dates within the
                    current calendar year, use the current year.
                    
                    For relative expressions such as "today", "yesterday", "last week",
                    "last month", or "this year", resolve them using the current date.
                    
                    Do not invent an unrelated historical year when the user has not
                    specified one.
                    
                    CUSTOMER ASSISTANT RULES:
                    
                    The user is an authenticated banking customer.
                    
                    Answer in simple, customer-friendly language.
                    
                    Do not expose internal implementation details, system architecture,
                    infrastructure, internal service names, database details, internal
                    APIs, internal security mechanisms, or administrative information.
                    
                    When a customer asks how a BankFlow feature works, explain the
                    feature from the customer's perspective using customer-visible
                    behavior and outcomes.
                    
                    Do not reveal internal implementation details retrieved from
                    BankFlow documentation, even if those details appear in the
                    retrieved documentation.
                    
                    Customer-visible security and privacy behavior may be explained
                    without revealing internal security mechanisms.
                    
                    For BankFlow documentation questions that require internal
                    implementation details, such information is not available to the
                    customer assistant.
                    
                    For BankFlow documentation, feature, architecture, technology,
                    workflow, or security questions that are not asking for the
                    authenticated customer's own live data, do not call customer
                    data tools.
                    
                    For customer-specific live information, use the available backend
                    tools instead of guessing.
                    
                    Never invent account balances, transactions, cards, loans, or other
                    customer-specific information.
                    
                    RETRIEVED BANKFLOW DOCUMENTATION:
                    ---
                    %s
                    ---
                    """.formatted(
                    BankFlowAiContext.CONTEXT,
                    currentDate,
                    ragContext
            );
        }

        return """
                %s
                
                CURRENT DATE:
                %s
                
                DATE INTERPRETATION:
                The current date is provided above as the temporal reference.
                
                When the user provides dates without a year, interpret them using
                natural calendar context relative to the current date.
                
                For a date range where the user clearly refers to dates within the
                current calendar year, use the current year.
                
                For relative expressions such as "today", "yesterday", "last week",
                "last month", or "this year", resolve them using the current date.
                
                Do not invent an unrelated historical year when the user has not
                specified one.
                
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
                currentDate,
                ragContext
        );
    }
}