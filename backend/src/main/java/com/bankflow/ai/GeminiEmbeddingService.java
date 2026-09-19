package com.bankflow.ai;

import com.google.genai.Client;
import com.google.genai.types.ContentEmbedding;
import com.google.genai.types.EmbedContentConfig;
import com.google.genai.types.EmbedContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeminiEmbeddingService implements EmbeddingService {

    private static final int EMBEDDING_DIMENSIONS = 1536;

    private final Client client;
    private final String embeddingModel;

    public GeminiEmbeddingService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.embedding-model:gemini-embedding-2}") String embeddingModel) {

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();

        this.embeddingModel = embeddingModel;
    }

    @Override
    public List<Float> generateEmbedding(String text) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Text for embedding must not be blank"
            );
        }

        EmbedContentConfig config = EmbedContentConfig.builder()
                .outputDimensionality(EMBEDDING_DIMENSIONS)
                .build();

        EmbedContentResponse response = client.models.embedContent(
                embeddingModel,
                text,
                config
        );

        ContentEmbedding embedding = response.embeddings()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Gemini returned no embedding"
                        ))
                .getFirst();

        return embedding.values()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Gemini returned no embedding values"
                        ));
    }
}