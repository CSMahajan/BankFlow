package com.bankflow.dto;

import java.time.LocalDateTime;

public record AdminKycExtractionResponse(

        Long documentId,
        String documentType,
        String extractionStatus,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer malwareScanAttempt,
        String malwareScanStatus
) {}