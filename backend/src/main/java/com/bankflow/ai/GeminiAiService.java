package com.bankflow.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiAiService implements AiService {

    private final Client client;
    private final ObjectMapper objectMapper;
    private final RagRetrievalService ragRetrievalService;

    @Value("${gemini.model:gemini-3.5-flash-lite}")
    private String model;

    public GeminiAiService(
            @Value("${gemini.api-key}") String apiKey,
            ObjectMapper objectMapper,
            RagRetrievalService ragRetrievalService) {

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();

        this.objectMapper = objectMapper;
        this.ragRetrievalService = ragRetrievalService;
    }

    @Override
    public AiResponse ask(String question) {

        List<RagRetrievedChunk> retrievedChunks =
                ragRetrievalService.retrieve(
                        question,
                        RagAudience.SHARED
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
                BankFlowAiContext.CONTEXT
                        + "\n\n"
                        + """
                        RAG INSTRUCTIONS:
                        
                        The following information was retrieved from
                        BankFlow's project documentation.
                        
                        Use this retrieved information when answering
                        BankFlow-specific questions.
                        
                        Do not invent implementation details.
                        
                        If the retrieved information does not contain
                        enough information to answer a BankFlow-specific
                        question, say that the available BankFlow
                        documentation does not provide enough information.
                        
                        Retrieved documentation:
                        """
                        + "\n"
                        + ragContext;

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
}