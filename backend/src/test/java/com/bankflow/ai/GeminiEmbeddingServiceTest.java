package com.bankflow.ai;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GeminiEmbeddingServiceTest {

    @Test
    void shouldGenerate1536DimensionalEmbedding() {

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY environment variable is required to run GeminiEmbeddingServiceTest"
            );
        }

        GeminiEmbeddingService service =
                new GeminiEmbeddingService(
                        apiKey,
                        "gemini-embedding-2"
                );

        List<Float> embedding =
                service.generateEmbedding(
                        "BankFlow allows customers to transfer funds."
                );

        assertFalse(embedding.isEmpty());
        assertEquals(1536, embedding.size());
    }
}