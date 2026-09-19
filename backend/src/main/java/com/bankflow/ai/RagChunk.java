package com.bankflow.ai;

public record RagChunk(
        int chunkIndex,
        String section,
        String content,
        RagAudience audience
) {
}