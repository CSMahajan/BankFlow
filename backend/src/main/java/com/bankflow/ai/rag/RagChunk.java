package com.bankflow.ai.rag;

public record RagChunk(
        int chunkIndex,
        String section,
        String content,
        RagAudience audience
) {
}