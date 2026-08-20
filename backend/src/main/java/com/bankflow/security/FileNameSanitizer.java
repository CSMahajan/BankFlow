package com.bankflow.security;

import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class FileNameSanitizer {


    public String sanitize(String originalFilename) {

        if (originalFilename == null ||
                originalFilename.isBlank()) {

            return "uploaded-document";
        }


        String fileName =
                Paths.get(originalFilename)
                        .getFileName()
                        .toString();


        return fileName
                .replaceAll("[^a-zA-Z0-9._-]", "_");

    }

}