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
    private final AiIntentClassifier aiIntentClassifier;
    private final AiIntentToolArgumentMapper aiIntentToolArgumentMapper;

    @Value("${gemini.model:gemini-3.5-flash-lite}")
    private String model;

    private static final int MAX_TOOL_ROUNDS = 5;

    public GeminiAiService(
            @Value("${gemini.api-key}") String apiKey,
            ObjectMapper objectMapper,
            RagRetrievalService ragRetrievalService,
            AiAudienceResolver aiAudienceResolver,
            AiToolRegistry aiToolRegistry,
            AiIntentClassifier aiIntentClassifier,
            AiIntentToolArgumentMapper aiIntentToolArgumentMapper) {

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();

        this.objectMapper = objectMapper;
        this.ragRetrievalService = ragRetrievalService;
        this.aiAudienceResolver = aiAudienceResolver;
        this.aiToolRegistry = aiToolRegistry;
        this.aiIntentClassifier = aiIntentClassifier;
        this.aiIntentToolArgumentMapper = aiIntentToolArgumentMapper;
    }

    @Override
    public AiResponse ask(String question) {

        AiAudience audience =
                aiAudienceResolver.resolve();

        AiIntent intent =
                aiIntentClassifier.classify(question);

        log.info(
                "AI intent resolved | route={} | operations={}",
                intent.route(),
                intent.operations()
        );

        if (intent.route() == AiIntent.Route.LIVE_DATA) {

            if (intent.operations() == null
                    || intent.operations().isEmpty()) {

                throw new IllegalArgumentException(
                        "LIVE_DATA intent must contain at least one operation"
                );
            }

            if (intent.operations().size() == 1) {

                return executeSingleLiveDataOperation(
                        question,
                        audience,
                        intent.operations().getFirst()
                );
            }

            return executeMultipleLiveDataOperations(
                    question,
                    audience,
                    intent.operations()
            );
        }

        return answerKnowledgeQuestion(
                question,
                audience
        );
    }

    private AiResponse answerKnowledgeQuestion(
            String question,
            AiAudience audience) {

        RagAudience ragAudience =
                audience == AiAudience.CUSTOMER
                        ? RagAudience.CUSTOMER
                        : RagAudience.ADMIN;

        log.info(
                "AI knowledge request | audience={}, RAG audience={}",
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

        Content systemContent =
                Content.fromParts(
                        Part.fromText(systemInstruction)
                );

        Schema responseSchema =
                Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "answer",
                                Schema.builder()
                                        .type("STRING")
                                        .build(),

                                "category",
                                Schema.builder()
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
                        .build();

        GenerateContentResponse response =
                client.models.generateContent(
                        model,
                        question,
                        config
                );

        log.info(
                "Gemini raw response: {}",
                response.text()
        );

        return parseResponse(response.text());
    }

    private AiResponse executeSingleLiveDataOperation(
            String question,
            AiAudience audience,
            AiIntent.Operation operation) {

        String toolName =
                aiIntentToolArgumentMapper.mapToolName(
                        operation
                );

        Map<String, Object> arguments =
                aiIntentToolArgumentMapper.map(
                        operation
                );

        log.info(
                "Deterministic AI tool route | intent={} | tool={} | arguments={}",
                operation.intent(),
                toolName,
                arguments
        );

        Object toolResult =
                aiToolRegistry.execute(
                        toolName,
                        audience,
                        arguments
                );

        log.info(
                "Deterministic tool result | tool={} | result={}",
                toolName,
                toolResult
        );

        return generateLiveDataResponse(
                question,
                audience,
                toolResult
        );
    }

    private AiResponse executeMultipleLiveDataOperations(
            String question,
            AiAudience audience,
            List<AiIntent.Operation> operations) {

        List<LiveDataOperationResult> results =
                new ArrayList<>();

        for (int i = 0; i < operations.size(); i++) {

            AiIntent.Operation operation =
                    operations.get(i);

            String toolName =
                    aiIntentToolArgumentMapper.mapToolName(
                            operation
                    );

            Map<String, Object> arguments =
                    aiIntentToolArgumentMapper.map(
                            operation
                    );

            log.info(
                    "Deterministic multi-operation route | operation={} | intent={} | tool={} | arguments={}",
                    i + 1,
                    operation.intent(),
                    toolName,
                    arguments
            );

            Object toolResult =
                    aiToolRegistry.execute(
                            toolName,
                            audience,
                            arguments
                    );

            log.info(
                    "Deterministic multi-operation result | operation={} | tool={} | result={}",
                    i + 1,
                    toolName,
                    toolResult
            );

            results.add(
                    new LiveDataOperationResult(
                            operation,
                            toolName,
                            toolResult
                    )
            );
        }

        return generateMultipleLiveDataResponse(
                question,
                audience,
                results
        );
    }

    private AiResponse generateMultipleLiveDataResponse(
            String question,
            AiAudience audience,
            List<LiveDataOperationResult> results) {

        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException(
                    "Multiple-operation response requires at least one result"
            );
        }

        String audienceInstruction =
                audience == AiAudience.CUSTOMER
                        ? """
                        Answer for a BankFlow customer.
                        
                        Use simple, clear, customer-friendly language.
                        Do not expose internal implementation details,
                        service names, database details, APIs, or infrastructure.
                        
                        All monetary amounts are in Indian Rupees (INR).
                        Use the ₹ symbol when displaying monetary amounts.
                        Never use $, USD, or any other currency symbol.
                        
                        Do not convert or change numeric values.
                        """
                        : """
                        Answer for a BankFlow administrator.
                        
                        You may provide deeper technical or operational context
                        when relevant and appropriate.
                        
                        All monetary amounts are in Indian Rupees (INR).
                        Use the ₹ symbol when displaying monetary amounts.
                        Never use $, USD, or any other currency symbol.
                        
                        Use backend results as authoritative.
                        Never invent additional data.
                        """;

        StringBuilder backendResults =
                new StringBuilder();

        for (int i = 0; i < results.size(); i++) {

            LiveDataOperationResult result =
                    results.get(i);

            backendResults
                    .append("Operation ")
                    .append(i + 1)
                    .append(":\n")
                    .append("Intent: ")
                    .append(result.operation().intent())
                    .append("\n")
                    .append("Tool: ")
                    .append(result.toolName())
                    .append("\n")
                    .append("Result: ")
                    .append(result.result())
                    .append("\n\n");
        }

        StringBuilder deterministicCalculations =
                new StringBuilder();

        /*
         * Calculate deterministic comparisons for every adjacent pair
         * of numeric backend results.
         *
         * There is intentionally no fixed operation-count limit.
         */
        for (int i = 0; i < results.size() - 1; i++) {

            Object firstResult =
                    results.get(i).result();

            Object secondResult =
                    results.get(i + 1).result();

            if (firstResult instanceof java.math.BigDecimal firstAmount
                    && secondResult instanceof java.math.BigDecimal secondAmount) {

                java.math.BigDecimal difference =
                        firstAmount.subtract(secondAmount);

                java.math.BigDecimal percentageChange = null;

                if (secondAmount.compareTo(java.math.BigDecimal.ZERO) != 0) {
                    percentageChange =
                            difference
                                    .divide(
                                            secondAmount,
                                            4,
                                            java.math.RoundingMode.HALF_UP
                                    )
                                    .multiply(
                                            java.math.BigDecimal.valueOf(100)
                                    );
                }

                deterministicCalculations
                        .append("Comparison ")
                        .append(i + 1)
                        .append(" (Operation ")
                        .append(i + 1)
                        .append(" vs Operation ")
                        .append(i + 2)
                        .append("):\n")
                        .append("Difference = ")
                        .append(difference)
                        .append("\n")
                        .append("Percentage change = ")
                        .append(
                                percentageChange == null
                                        ? "not available because the comparison baseline is zero"
                                        : percentageChange + "%"
                        )
                        .append("\n\n");
            }
        }

        String prompt = """
                Answer the user's question using the authoritative backend
                results and deterministic calculations provided below.
                
                %s
                
                User question:
                %s
                
                Backend results:
                %s
                
                Deterministic calculations:
                %s
                
                Interpret the operations according to the user's question
                and preserve their order.
                
                Use the backend results as authoritative.
                
                Do not perform arithmetic yourself when a deterministic
                calculation is provided.
                
                Do not invent missing values or additional data.
                
                If the user only asked for multiple values, clearly present
                those values without unnecessarily emphasizing comparisons.
                
                If the user asked for a comparison, use the relevant
                deterministic comparison provided above.
                
                Do not mention tools, function calls, internal routing,
                operation classification, or this instruction.
                
                Return only the answer text.
                """.formatted(
                audienceInstruction,
                question,
                backendResults,
                deterministicCalculations.isEmpty()
                        ? "No deterministic numeric comparisons were generated."
                        : deterministicCalculations.toString()
        );

        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("text/plain")
                        .build();

        long startTime =
                System.currentTimeMillis();

        GenerateContentResponse response =
                client.models.generateContent(
                        model,
                        prompt,
                        config
                );

        log.info(
                "AI TIMING | multi-operation live-data Gemini call = {} ms",
                System.currentTimeMillis() - startTime
        );

        String answer =
                response.text();

        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException(
                    "Gemini returned an empty multi-operation live-data answer"
            );
        }

        return new AiResponse(
                answer.trim(),
                AiResponse.Category.BANKFLOW_FEATURE
        );
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

    private AiResponse generateLiveDataResponse(
            String question,
            AiAudience audience,
            Object toolResult) {

        String audienceInstruction =
                audience == AiAudience.CUSTOMER
                        ? """
                        Answer for a BankFlow customer.
                        
                        Use simple, clear, customer-friendly language.
                        Do not expose internal implementation details,
                        service names, database details, APIs, or infrastructure.
                        
                        Use the backend result as the authoritative source
                        for the customer's live banking data.
                        
                        All monetary amounts are in Indian Rupees (INR).
                        Use the ₹ symbol when displaying monetary amounts.
                        Never use $, USD, or any other currency symbol.
                        Do not convert or change the numeric value.
                        """
                        : """
                        Answer for a BankFlow administrator.
                        
                        You may provide deeper technical or operational context
                        when relevant and appropriate.
                        
                        Use the backend result as the authoritative source.
                        Never invent additional customer or system data.
                        """;

        String toolResultJson;

        try {
            toolResultJson =
                    objectMapper.writeValueAsString(toolResult);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize live-data tool result",
                    e
            );
        }

        String prompt = """
                Answer the user's question using the authoritative backend result below.
                
                %s
                
                User question:
                %s
                
                Backend result:
                %s
                
                Do not mention tools, function calls, internal routing,
                or this instruction.
                
                Return only the answer text.
                """.formatted(
                audienceInstruction,
                question,
                toolResultJson
        );

        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("text/plain")
                        .build();

        long startTime = System.currentTimeMillis();

        GenerateContentResponse response =
                client.models.generateContent(
                        model,
                        prompt,
                        config
                );

        log.info(
                "AI TIMING | live-data Gemini call = {} ms",
                System.currentTimeMillis() - startTime
        );

        String answer = response.text();

        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException(
                    "Gemini returned an empty live-data answer"
            );
        }

        return new AiResponse(
                answer.trim(),
                AiResponse.Category.BANKFLOW_FEATURE
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
                    
                    TOOL ELIGIBILITY:
                    
                    Customer data tools are ONLY for questions that explicitly require
                    the authenticated customer's own live banking data.
                    
                    Use a customer data tool only when the answer depends on data that
                    belongs specifically to the authenticated customer, such as:
                    - their accounts
                    - their account balances
                    - their transactions
                    - their cards
                    - their loans
                    - their fixed deposits
                    - their scheduled transfers
                    
                    Do NOT call a customer data tool for questions about:
                    - BankFlow features
                    - BankFlow functionality
                    - BankFlow technology
                    - BankFlow architecture
                    - BankFlow APIs
                    - BankFlow security
                    - BankFlow workflows
                    - BankFlow documentation
                    - BankFlow infrastructure
                    - databases
                    - deployment
                    - implementation details
                    
                    For these questions, answer using the retrieved BankFlow
                    documentation and the information available in the system
                    instruction.
                    
                    The mere availability of a customer data tool does not mean that
                    the tool should be called.
                    
                    Before calling a customer data tool, determine whether the question
                    actually requires the authenticated customer's own live data.
                    If it does not, do not call any customer data tool.
                    
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
                    tools instead of guessing. Customer data tools must only be used
                    when the question requires the authenticated customer's own live
                    data.
                    
                    When a customer asks for their own live banking information,
                    you MUST call the appropriate backend tool to obtain the information.
                    
                    Do not describe or simulate a tool call in the answer.
                    
                    Never write phrases such as "Calling tool: ..." or
                    "Using tool: ..." as the answer.
                    
                    The tool must be invoked through the available function-calling
                    mechanism before providing the final answer.
                    
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

    private record LiveDataOperationResult(
            AiIntent.Operation operation,
            String toolName,
            Object result
    ) {
    }
}