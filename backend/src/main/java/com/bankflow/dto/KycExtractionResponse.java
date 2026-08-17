package com.bankflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record KycExtractionResponse(
        Long documentId,
        String extractionStatus,
        String extractedText,
        String panNumber,
        String fullName,
        String fatherName,
        LocalDate dateOfBirth,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}