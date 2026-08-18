package com.bankflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PanDataResponse(
        Long documentId,
        String panNumber,
        String fullName,
        String fatherName,
        LocalDate dateOfBirth,
        LocalDateTime createdAt
) {
}