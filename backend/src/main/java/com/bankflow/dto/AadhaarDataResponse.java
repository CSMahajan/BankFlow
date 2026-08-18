package com.bankflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AadhaarDataResponse(

        Long documentId,
        String aadhaarNumber,
        String fullName,
        LocalDate dateOfBirth,
        String gender,
        String address,
        String mobileNumber,
        LocalDateTime createdAt
) {
}