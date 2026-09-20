package com.bankflow.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagRetrievalService {

    private static final int CANDIDATE_LIMIT = 10;
    private static final int DEFAULT_TOP_K = 5;
    public static final String SHARED_AUDIENCE = "SHARED";

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

        List<RagChunkSearchResult> results =
                ragChunkRepository.findNearestChunksWithDistance(
                        pgVector,
                        allowedAudiences(audience),
                        CANDIDATE_LIMIT
                );

        for (int i = 0; i < results.size(); i++) {

            RagChunkSearchResult result = results.get(i);

            log.info(
                    "RAG result {} | distance={} | source={} | section={} | content={}",
                    i + 1,
                    result.getDistance(),
                    result.getSourcePath(),
                    result.getSection(),
                    preview(result.getContent())
            );
        }

        return results.stream()
                .limit(DEFAULT_TOP_K)
                .map(result -> new RagRetrievedChunk(
                        result.getSourcePath(),
                        result.getSection(),
                        result.getContent(),
                        RagAudience.valueOf(result.getAudience())
                ))
                .toList();
    }

    private List<String> allowedAudiences(
            RagAudience audience) {

        return switch (audience) {
            case CUSTOMER -> List.of(SHARED_AUDIENCE, "CUSTOMER");

            case ADMIN -> List.of(SHARED_AUDIENCE, "ADMIN");

            case SHARED -> List.of(SHARED_AUDIENCE);
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

    private String preview(String content) {

        if (content == null) {
            return "";
        }

        String normalized =
                content.replaceAll("\\s+", " ").trim();

        if (normalized.length() <= 250) {
            return normalized;
        }

        return normalized.substring(0, 250) + "...";
    }
}