package com.bankflow.ai;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownChunkerTest {

    private final MarkdownChunker chunker = new MarkdownChunker();

    @Test
    void shouldCreateChunksFromMarkdownHeadings() {

        RagDocument document = new RagDocument(
                "README.md",
                RagSourceType.MARKDOWN,
                "README",
                """
                # BankFlow
    
                Core banking application.
    
                ## Features
    
                Customer and admin banking features.
    
                ## Security
    
                JWT authentication and authorization.
                """
        );

        List<RagChunk> chunks = chunker.chunk(document);

        assertFalse(chunks.isEmpty());

        assertTrue(chunks.stream()
                .anyMatch(chunk ->
                        chunk.section().equals("BankFlow")));

        assertTrue(chunks.stream()
                .anyMatch(chunk ->
                        chunk.section().equals("BankFlow > Features")));

        assertTrue(chunks.stream()
                .anyMatch(chunk ->
                        chunk.section().equals("BankFlow > Security")));
    }
}