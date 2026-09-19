package com.bankflow.ai;

public record RagRetrievedChunk(
        String sourcePath,
        String section,
        String content,
        RagAudience audience
) {
}