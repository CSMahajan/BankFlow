package com.bankflow.dto;

public record StoredFileMetadata(
        String path,
        String storageType,
        String bucket,
        String objectKey,
        String encryptionType,
        String checksum
) {
}
