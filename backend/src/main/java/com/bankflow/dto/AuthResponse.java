package com.bankflow.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        String email,
        String role,
        String fullName
) {

    public AuthResponse(
            String accessToken,
            String refreshToken,
            String email,
            String role,
            String fullName
    ) {
        this(accessToken, refreshToken, "Bearer", email, role, fullName);
    }
}