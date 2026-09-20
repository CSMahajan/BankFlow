package com.bankflow.ai;

import com.bankflow.entity.RagChunkEntity;
import com.bankflow.entity.RagSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RagIngestionService {

    private final GeminiEmbeddingService embeddingService;
    private final RagSourceRepository ragSourceRepository;
    private final RagChunkRepository ragChunkRepository;

    @Transactional
    public RagSource ingest(
            RagDocument document,
            List<RagChunk> chunks) {

        String sourceHash = sha256(document.content());

        RagSource source = ragSourceRepository
                .findBySourcePath(document.sourcePath())
                .orElse(null);

        if (source != null
                && sourceHash.equals(source.getContentHash())) {

            return source;
        }

        if (source == null) {
            source = new RagSource();
        }

        source.setSourcePath(document.sourcePath());
        source.setSourceType(document.sourceType());
        source.setTitle(document.title());
        source.setContentHash(sourceHash);
        source.setActive(true);

        RagSource savedSource =
                ragSourceRepository.save(source);

        /*
         * If the source already existed and changed,
         * remove its previous chunks before recreating them.
         */
        if (savedSource.getId() != null) {
            ragChunkRepository
                    .deleteBySourceId(savedSource.getId());
        }

        for (RagChunk chunk : chunks) {

            String chunkText =
                    buildEmbeddingText(chunk);

            String chunkHash =
                    sha256(chunkText);

            List<Float> embedding =
                    embeddingService.generateEmbedding(
                            chunkText
                    );

            RagChunkEntity entity =
                    RagChunkEntity.builder()
                            .source(savedSource)
                            .chunkIndex(chunk.chunkIndex())
                            .section(chunk.section())
                            .content(chunk.content())
                            .audience(chunk.audience())
                            .contentHash(chunkHash)
                            .embedding(toFloatArray(embedding))
                            .build();

            ragChunkRepository.save(entity);
        }

        return savedSource;
    }

    private float[] toFloatArray(List<Float> embedding) {

        float[] result = new float[embedding.size()];

        for (int i = 0; i < embedding.size(); i++) {
            result[i] = embedding.get(i);
        }

        return result;
    }

    private String sha256(String content) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            content.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder result = new StringBuilder();

            for (byte value : hash) {
                result.append(
                        String.format("%02x", value)
                );
            }

            return result.toString();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to calculate SHA-256 hash",
                    e
            );
        }
    }

    private String buildEmbeddingText(RagChunk chunk) {

        if (chunk.section() == null
                || chunk.section().isBlank()) {
            return chunk.content();
        }

        return chunk.section()
                + "\n\n"
                + chunk.content();
    }
}