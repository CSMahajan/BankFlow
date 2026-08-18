package com.bankflow.dto;

import java.time.LocalDateTime;

public record AdminKycDocumentResponse(

        Long id,
        Long userId,
        String customerName,
        String email,
        String documentType,
        String originalFileName,
        String status,
        String extractionStatus,
        String rejectionReason,
        LocalDateTime uploadedAt
) {}