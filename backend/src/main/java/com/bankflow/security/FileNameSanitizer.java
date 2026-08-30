package com.bankflow.security;

import org.springframework.stereotype.Component;

@Component
public class FileNameSanitizer {

    public String sanitize(String originalFilename) {

        if (originalFilename == null ||
                originalFilename.isBlank()) {

            return "uploaded-document";
        }

        // Handle both Unix (/) and Windows (\) paths
        String fileName = originalFilename
                .replace('\\', '/');

        fileName = fileName.substring(
                fileName.lastIndexOf('/') + 1
        );

        return fileName.replaceAll(
                "[^a-zA-Z0-9._-]",
                "*"
        );
    }
}