package com.bankflow.ai;

import com.bankflow.entity.RagChunkEntity;
import com.bankflow.entity.RagSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class RagChunkEntityRepositoryTest {

    @Autowired
    private RagSourceRepository ragSourceRepository;

    @Autowired
    private RagChunkRepository ragChunkRepository;

    @BeforeEach
    void cleanRagData() {
        /*
         * This test uses the real local PostgreSQL database.
         *
         * Clear only the RAG tables so vector-search tests operate
         * on a deterministic dataset.
         *
         * @DataJpaTest rolls the transaction back after each test,
         * so the real 137 RAG chunks remain untouched.
         */
        ragChunkRepository.deleteAllInBatch();
        ragSourceRepository.deleteAllInBatch();
    }

    @Test
    void shouldPersistAndReadRagChunkWithEmbedding() {

        RagSource source = RagSource.builder()
                .sourcePath("test/rag/test-document.md")
                .sourceType(RagSourceType.MARKDOWN)
                .title("Test Document")
                .contentHash("test-source-hash")
                .active(true)
                .build();

        RagSource savedSource =
                ragSourceRepository.save(source);

        float[] embedding = new float[1536];

        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = i / 1536.0f;
        }

        RagChunkEntity chunk = RagChunkEntity.builder()
                .source(savedSource)
                .chunkIndex(0)
                .section("Test > Section")
                .content(
                        "BankFlow allows customers to transfer funds."
                )
                .audience(RagAudience.SHARED)
                .contentHash("test-chunk-hash")
                .embedding(embedding)
                .build();

        RagChunkEntity savedChunk =
                ragChunkRepository.saveAndFlush(chunk);

        assertNotNull(savedChunk.getId());

        RagChunkEntity loadedChunk =
                ragChunkRepository.findById(savedChunk.getId())
                        .orElseThrow();

        assertEquals(
                "BankFlow allows customers to transfer funds.",
                loadedChunk.getContent()
        );

        assertEquals(
                RagAudience.SHARED,
                loadedChunk.getAudience()
        );

        assertEquals(
                "Test > Section",
                loadedChunk.getSection()
        );

        assertNotNull(loadedChunk.getEmbedding());

        assertEquals(
                1536,
                loadedChunk.getEmbedding().length
        );

        assertTrue(
                Arrays.equals(
                        embedding,
                        loadedChunk.getEmbedding()
                )
        );
    }

    @Test
    void shouldFindNearestChunksInCosineSimilarityOrder() {

        RagSource source =
                saveSource("similarity-test.md");

        RagChunkEntity exactMatch = saveChunk(
                source,
                0,
                "Exact matching content",
                RagAudience.SHARED,
                createVector(1.0f, 0.0f)
        );

        RagChunkEntity closeMatch = saveChunk(
                source,
                1,
                "Close matching content",
                RagAudience.SHARED,
                createVector(0.8f, 0.6f)
        );

        RagChunkEntity distantMatch = saveChunk(
                source,
                2,
                "Distant content",
                RagAudience.SHARED,
                createVector(0.0f, 1.0f)
        );

        String queryEmbedding =
                toPgVector(createVector(1.0f, 0.0f));

        List<RagChunkEntity> results =
                ragChunkRepository.findNearestChunks(
                        queryEmbedding,
                        List.of("SHARED"),
                        3
                );

        assertEquals(3, results.size());

        assertEquals(
                exactMatch.getId(),
                results.get(0).getId()
        );

        assertEquals(
                closeMatch.getId(),
                results.get(1).getId()
        );

        assertEquals(
                distantMatch.getId(),
                results.get(2).getId()
        );
    }

    @Test
    void shouldRespectAudienceFilterDuringVectorSearch() {

        RagSource source =
                saveSource("audience-test.md");

        RagChunkEntity sharedChunk = saveChunk(
                source,
                0,
                "Shared banking documentation",
                RagAudience.SHARED,
                createVector(1.0f, 0.0f)
        );

        RagChunkEntity customerChunk = saveChunk(
                source,
                1,
                "Customer documentation",
                RagAudience.CUSTOMER,
                createVector(0.8f, 0.6f)
        );

        RagChunkEntity adminChunk = saveChunk(
                source,
                2,
                "Admin documentation",
                RagAudience.ADMIN,
                createVector(0.0f, 1.0f)
        );

        String queryEmbedding =
                toPgVector(createVector(1.0f, 0.0f));

        /*
         * Customer:
         * SHARED + CUSTOMER
         * ADMIN must not be returned.
         */
        List<RagChunkEntity> customerResults =
                ragChunkRepository.findNearestChunks(
                        queryEmbedding,
                        List.of("SHARED", "CUSTOMER"),
                        10
                );

        assertEquals(2, customerResults.size());

        assertTrue(
                customerResults.stream()
                        .anyMatch(chunk ->
                                chunk.getId()
                                        .equals(sharedChunk.getId()))
        );

        assertTrue(
                customerResults.stream()
                        .anyMatch(chunk ->
                                chunk.getId()
                                        .equals(customerChunk.getId()))
        );

        assertTrue(
                customerResults.stream()
                        .noneMatch(chunk ->
                                chunk.getId()
                                        .equals(adminChunk.getId()))
        );

        /*
         * Admin:
         * SHARED + ADMIN
         * CUSTOMER must not be returned.
         */
        List<RagChunkEntity> adminResults =
                ragChunkRepository.findNearestChunks(
                        queryEmbedding,
                        List.of("SHARED", "ADMIN"),
                        10
                );

        assertEquals(2, adminResults.size());

        assertTrue(
                adminResults.stream()
                        .anyMatch(chunk ->
                                chunk.getId()
                                        .equals(sharedChunk.getId()))
        );

        assertTrue(
                adminResults.stream()
                        .anyMatch(chunk ->
                                chunk.getId()
                                        .equals(adminChunk.getId()))
        );

        assertTrue(
                adminResults.stream()
                        .noneMatch(chunk ->
                                chunk.getId()
                                        .equals(customerChunk.getId()))
        );
    }

    private RagSource saveSource(String path) {

        RagSource source = RagSource.builder()
                .sourcePath("test/rag/" + path)
                .sourceType(RagSourceType.MARKDOWN)
                .title("Test Document")
                .contentHash("hash-" + path)
                .active(true)
                .build();

        return ragSourceRepository.saveAndFlush(source);
    }

    private RagChunkEntity saveChunk(
            RagSource source,
            int chunkIndex,
            String content,
            RagAudience audience,
            float[] embedding) {

        RagChunkEntity chunk = RagChunkEntity.builder()
                .source(source)
                .chunkIndex(chunkIndex)
                .section("Test > Section")
                .content(content)
                .audience(audience)
                .contentHash(
                        "hash-" + chunkIndex + "-" + content
                )
                .embedding(embedding)
                .build();

        return ragChunkRepository.saveAndFlush(chunk);
    }

    /*
     * Creates a 1536-dimensional vector where only the first
     * two dimensions contain values.
     */
    private float[] createVector(
            float firstDimension,
            float secondDimension) {

        float[] embedding = new float[1536];

        embedding[0] = firstDimension;
        embedding[1] = secondDimension;

        return embedding;
    }

    private String toPgVector(float[] embedding) {

        StringBuilder result =
                new StringBuilder("[");

        for (int i = 0; i < embedding.length; i++) {

            if (i > 0) {
                result.append(",");
            }

            result.append(embedding[i]);
        }

        result.append("]");

        return result.toString();
    }
}