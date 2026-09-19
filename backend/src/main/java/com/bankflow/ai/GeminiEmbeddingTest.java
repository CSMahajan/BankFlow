package com.bankflow.ai;

import com.google.genai.Client;
import com.google.genai.types.ContentEmbedding;
import com.google.genai.types.EmbedContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GeminiEmbeddingTest implements CommandLineRunner {

    private final Client client;

    @Value("${gemini.embedding-model}")
    private String embeddingModel;

    public GeminiEmbeddingTest(
            @Value("${gemini.api-key}") String apiKey) {

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public void testEmbedding() {

        String text = "BankFlow uses AWS Textract for OCR.";

        EmbedContentResponse response =
                client.models.embedContent(
                        embeddingModel,
                        text,
                        null
                );

        ContentEmbedding embedding =
                response.embeddings()
                        .orElseThrow()
                        .getFirst();

        var values = embedding.values().orElseThrow();

        log.info("Embedding model: {}", embeddingModel);
        log.info("Vector dimensions: {}", values.size());

        log.info("First 10 values: {}", values.subList(0, Math.min(10, values.size())));
    }

    @Override
    public void run(String... args) {
        testEmbedding();
    }
}
