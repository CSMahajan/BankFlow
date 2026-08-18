package com.bankflow.dto;

import java.time.LocalDate;

public record AadhaarExtractedData(
        String aadhaarNumber,
        String fullName,
        LocalDate dateOfBirth,
        String gender,
        String address,
        String mobileNumber
) {
}