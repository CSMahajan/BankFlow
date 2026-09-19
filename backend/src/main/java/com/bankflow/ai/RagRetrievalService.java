package com.bankflow.ai;

import com.bankflow.entity.RagChunkEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RagRetrievalService {

    private static final int DEFAULT_TOP_K = 5;

    private final GeminiEmbeddingService embeddingService;
    private final RagChunkRepository ragChunkRepository;

    @Transactional(readOnly = true)
    public List<RagRetrievedChunk> retrieve(
            String question,
            RagAudience audience) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be blank"
            );
        }

        if (audience == null) {
            throw new IllegalArgumentException(
                    "Audience must not be null"
            );
        }

        List<Float> embedding =
                embeddingService.generateEmbedding(question);

        String pgVector =
                toPgVector(embedding);

        List<RagChunkEntity> chunks =
                ragChunkRepository.findNearestChunks(
                        pgVector,
                        allowedAudiences(audience),
                        DEFAULT_TOP_K
                );

        return chunks.stream()
                .map(chunk -> new RagRetrievedChunk(
                        chunk.getSource().getSourcePath(),
                        chunk.getSection(),
                        chunk.getContent(),
                        chunk.getAudience()
                ))
                .toList();
    }

    private List<String> allowedAudiences(
            RagAudience audience) {

        return switch (audience) {
            case CUSTOMER ->
                    List.of("SHARED", "CUSTOMER");

            case ADMIN ->
                    List.of("SHARED", "ADMIN");

            case SHARED ->
                    List.of("SHARED");
        };
    }

    private String toPgVector(List<Float> embedding) {

        if (embedding == null || embedding.isEmpty()) {
            throw new IllegalArgumentException(
                    "Embedding must not be empty"
            );
        }

        StringBuilder result =
                new StringBuilder("[");

        for (int i = 0; i < embedding.size(); i++) {

            if (i > 0) {
                result.append(",");
            }

            result.append(embedding.get(i));
        }

        result.append("]");

        return result.toString();
    }
}