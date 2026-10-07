package com.bankflow.ai;

import com.bankflow.ai.rag.*;
import com.bankflow.ai.rag.RagChunkEntity;
import com.bankflow.ai.rag.RagSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RagIngestionServiceTest {

    @Mock
    private GeminiEmbeddingService embeddingService;

    @Mock
    private RagSourceRepository ragSourceRepository;

    @Mock
    private RagChunkRepository ragChunkRepository;

    @InjectMocks
    private RagIngestionService ragIngestionService;

    @Test
    void shouldGenerateEmbeddingAndPersistChunk() {

        RagDocument document = new RagDocument(
                "docs/test-document.md",
                RagSourceType.MARKDOWN,
                "Test Document",
                "# Test\n\nBankFlow allows customers to transfer funds."
        );

        RagChunk chunk = new RagChunk(
                0,
                "Test",
                "BankFlow allows customers to transfer funds.",
                RagAudience.SHARED
        );

        RagSource savedSource = RagSource.builder()
                .id(1L)
                .sourcePath(document.sourcePath())
                .sourceType(document.sourceType())
                .title(document.title())
                .contentHash("source-hash")
                .active(true)
                .build();

        when(ragSourceRepository.findBySourcePath(document.sourcePath()))
                .thenReturn(Optional.empty());

        when(ragSourceRepository.save(any(RagSource.class)))
                .thenReturn(savedSource);

        List<Float> embedding = List.of(
                0.1f,
                0.2f,
                0.3f
        );

        when(embeddingService.generateEmbedding(
                "Test\n\nBankFlow allows customers to transfer funds."
        )).thenReturn(embedding);

        when(ragChunkRepository.save(any(RagChunkEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RagSource result =
                ragIngestionService.ingest(
                        document,
                        List.of(chunk)
                );

        assertSame(savedSource, result);

        verify(embeddingService)
                .generateEmbedding(
                        "Test\n\nBankFlow allows customers to transfer funds."
                );

        ArgumentCaptor<RagChunkEntity> captor =
                ArgumentCaptor.forClass(RagChunkEntity.class);

        verify(ragChunkRepository)
                .save(captor.capture());

        RagChunkEntity savedChunk = captor.getValue();

        assertSame(savedSource, savedChunk.getSource());
        assertEquals(0, savedChunk.getChunkIndex());
        assertEquals("Test", savedChunk.getSection());
        assertEquals(
                chunk.content(),
                savedChunk.getContent()
        );
        assertEquals(
                RagAudience.SHARED,
                savedChunk.getAudience()
        );
        assertArrayEquals(
                new float[]{0.1f, 0.2f, 0.3f},
                savedChunk.getEmbedding()
        );
        assertNotNull(savedChunk.getContentHash());
    }

    @Test
    void shouldSkipIngestionWhenSourceContentHasNotChanged() {

        RagDocument document = new RagDocument(
                "docs/test-document.md",
                RagSourceType.MARKDOWN,
                "Test Document",
                "# Test\n\nBankFlow allows customers to transfer funds."
        );

        RagSource existingSource = RagSource.builder()
                .id(1L)
                .sourcePath(document.sourcePath())
                .sourceType(document.sourceType())
                .title(document.title())
                .contentHash(
                        sha256ForTest(document.content())
                )
                .active(true)
                .build();

        when(ragSourceRepository.findBySourcePath(document.sourcePath()))
                .thenReturn(Optional.of(existingSource));

        RagSource result =
                ragIngestionService.ingest(
                        document,
                        List.of(
                                new RagChunk(
                                        0,
                                        "Test",
                                        "BankFlow allows customers to transfer funds.",
                                        RagAudience.SHARED
                                )
                        )
                );

        assertSame(existingSource, result);

        verify(ragSourceRepository, never())
                .save(any(RagSource.class));

        verify(embeddingService, never())
                .generateEmbedding(anyString());

        verify(ragChunkRepository, never())
                .save(any(RagChunkEntity.class));

        verify(ragChunkRepository, never())
                .deleteBySourceId(anyLong());
    }

    private String sha256ForTest(String content) {

        try {
            var digest =
                    java.security.MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            content.getBytes(
                                    java.nio.charset.StandardCharsets.UTF_8
                            )
                    );

            StringBuilder result = new StringBuilder();

            for (byte value : hash) {
                result.append(
                        String.format("%02x", value)
                );
            }

            return result.toString();

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}