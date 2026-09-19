package com.bankflow.ai;

public record RagDocument(
        String sourcePath,
        RagSourceType sourceType,
        String title,
        String content
) {
}