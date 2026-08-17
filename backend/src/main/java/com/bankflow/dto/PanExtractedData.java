package com.bankflow.dto;

import java.time.LocalDate;

public record PanExtractedData(
        String panNumber,
        String fullName,
        String fatherName,
        LocalDate dateOfBirth
) {
}