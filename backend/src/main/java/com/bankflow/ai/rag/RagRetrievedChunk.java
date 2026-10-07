package com.bankflow.ai.rag;

public record RagRetrievedChunk(
        String sourcePath,
        String section,
        String content,
        RagAudience audience
) {
}