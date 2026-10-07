package com.bankflow.ai.rag;

public record RagDocument(
        String sourcePath,
        RagSourceType sourceType,
        String title,
        String content
) {
}