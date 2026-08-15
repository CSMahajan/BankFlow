package com.bankflow.dto;

import java.time.LocalDateTime;

public record KycDocumentResponse(

        Long id,
        String documentType,
        String originalFileName,
        String status,
        LocalDateTime uploadedAt,
        String rejectionReason
) {
}
