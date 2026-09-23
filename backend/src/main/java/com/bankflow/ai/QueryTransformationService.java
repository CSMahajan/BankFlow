package com.bankflow.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class QueryTransformationService {

    private final Client client;
    private final String model;

    public QueryTransformationService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model:gemini-3.5-flash-lite}") String model) {

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();

        this.model = model;
    }

    public String transform(String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be blank"
            );
        }

        String prompt = """
                Rewrite the user's question into a concise retrieval-oriented
                query for searching BankFlow documentation.

                Use terminology likely to appear in technical documentation,
                API documentation, architecture documentation, workflows,
                database documentation, and ERD documentation.

                Preserve the user's original intent.

                Do not answer the question.
                Return only the rewritten query.

                User question:
                %s
                """.formatted(question);

        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("text/plain")
                        .build();

        var response = client.models.generateContent(
                model,
                prompt,
                config
        );

        String transformedQuery = response.text();

        if (transformedQuery == null || transformedQuery.isBlank()) {
            throw new IllegalStateException(
                    "Gemini returned an empty transformed query"
            );
        }

        transformedQuery = transformedQuery.trim();

        log.info(
                "RAG query transformation | original={} | transformed={}",
                question,
                transformedQuery
        );

        return transformedQuery;
    }
}