package com.bankflow.dto;

public record AuthResponse(
        String token,
        String tokenType,
        String email,
        String role,
        String fullName
) {
    public AuthResponse(String token, String email, String role, String fullName) {
        this(token, "Bearer", email, role, fullName);
    }
}
