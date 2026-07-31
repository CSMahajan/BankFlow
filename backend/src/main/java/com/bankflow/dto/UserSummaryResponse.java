package com.bankflow.dto;

import java.time.LocalDateTime;

public record UserSummaryResponse(
        Long id,
        String fullName,
        String email,
        String role,
        LocalDateTime createdAt,
        long accountCount
) {}