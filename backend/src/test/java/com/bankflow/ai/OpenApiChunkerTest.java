package com.bankflow.ai;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiChunkerTest {

    private final OpenApiDocumentParser parser =
            new OpenApiDocumentParser();

    private final OpenApiChunker chunker =
            new OpenApiChunker();

    @Test
    void shouldCreateChunksForBankFlowApiOperations() {

        Path path = Path.of("../docs/api/bankflow_openapi.yml");

        RagDocument document = parser.parse(path);

        List<RagChunk> chunks = chunker.chunk(document);

        assertFalse(chunks.isEmpty());

        assertTrue(chunks.stream()
                .anyMatch(chunk ->
                        chunk.section().equals(
                                "API > POST /api/v1/auth/login"
                        )));

        assertTrue(chunks.stream()
                .anyMatch(chunk ->
                        chunk.section().equals(
                                "API > POST /api/v1/transfers"
                        )));
    }

    @Test
    void shouldMarkAdminEndpointsAsAdminAudience() {

        Path path = Path.of("../docs/api/bankflow_openapi.yml");

        RagDocument document = parser.parse(path);

        List<RagChunk> chunks = chunker.chunk(document);

        assertTrue(chunks.stream()
                .filter(chunk ->
                        chunk.section().contains("/api/v1/admin/"))
                .allMatch(chunk ->
                        chunk.audience() == RagAudience.ADMIN));
    }

    @Test
    void shouldKeepNormalEndpointsShared() {

        Path path = Path.of("../docs/api/bankflow_openapi.yml");

        RagDocument document = parser.parse(path);

        List<RagChunk> chunks = chunker.chunk(document);

        assertTrue(chunks.stream()
                .filter(chunk ->
                        chunk.section().equals(
                                "API > POST /api/v1/auth/login"
                        ))
                .allMatch(chunk ->
                        chunk.audience() == RagAudience.SHARED));
    }

    @Test
    void shouldIncludeRequestAndResponseInformation() {

        Path path = Path.of("../docs/api/bankflow_openapi.yml");

        RagDocument document = parser.parse(path);

        List<RagChunk> chunks = chunker.chunk(document);

        RagChunk transferChunk = chunks.stream()
                .filter(chunk ->
                        chunk.section().equals(
                                "API > POST /api/v1/transfers"
                        ))
                .findFirst()
                .orElseThrow();

        assertTrue(
                transferChunk.content()
                        .contains("transferFunds")
        );

        assertTrue(
                transferChunk.content()
                        .contains("FundTransferRequest")
        );

        assertTrue(
                transferChunk.content()
                        .contains("FundTransferResponse")
        );
    }

    @Test
    void shouldMarkAdminLoanEndpointsAsAdminAudience() {
        Path path = Path.of("../docs/api/bankflow_openapi.yml");
        RagDocument document = parser.parse(path);
        List<RagChunk> chunks = chunker.chunk(document);

        assertTrue(chunks.stream()
                .filter(chunk ->
                        chunk.section().equals(
                                "API > GET /api/v1/loans/pending"
                        ))
                .allMatch(chunk ->
                        chunk.audience() == RagAudience.ADMIN));

        assertTrue(chunks.stream()
                .filter(chunk ->
                        chunk.section().equals(
                                "API > PUT /api/v1/loans/{loanId}/approve"
                        ))
                .allMatch(chunk ->
                        chunk.audience() == RagAudience.ADMIN));

        assertTrue(chunks.stream()
                .filter(chunk ->
                        chunk.section().equals(
                                "API > PUT /api/v1/loans/{loanId}/reject"
                        ))
                .allMatch(chunk ->
                        chunk.audience() == RagAudience.ADMIN));
    }
}