package com.bankflow.security;

public record VirusScanResult(
        boolean clean,
        String message
) {

    public static VirusScanResult success() {
        return new VirusScanResult(
                true,
                "File is clean"
        );
    }

    public static VirusScanResult failure(String message) {
        return new VirusScanResult(
                false,
                message
        );
    }
}