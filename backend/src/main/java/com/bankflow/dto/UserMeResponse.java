package com.bankflow.dto;

public record UserMeResponse(
        Long id,
        String fullName,
        String email,
        String role
) {}