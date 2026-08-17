package com.bankflow.dto;

import java.time.LocalDateTime;

public record KycExtractionResponse(
        Long documentId,
        String extractionStatus,
        String extractedText,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}