package com.bankflow.ai;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class MarkdownChunkerRealDocumentTest {

    @Test
    void shouldPrintRealReadmeChunks() throws Exception {

        Path readmePath = Path.of("../README.md");

        String content = Files.readString(readmePath);

        RagDocument document = new RagDocument(
                "README.md",
                RagSourceType.MARKDOWN,
                "README",
                content
        );

        MarkdownChunker chunker = new MarkdownChunker();

        List<RagChunk> chunks = chunker.chunk(document);

        System.out.println("Total chunks: " + chunks.size());

        for (RagChunk chunk : chunks) {
            System.out.println();
            System.out.println("==================================================");
            System.out.println("Chunk index: " + chunk.chunkIndex());
            System.out.println("Section: " + chunk.section());
            System.out.println("Audience: " + chunk.audience());
            System.out.println("Content length: " + chunk.content().length());
            System.out.println("==================================================");
            System.out.println(chunk.content());
        }
    }
}