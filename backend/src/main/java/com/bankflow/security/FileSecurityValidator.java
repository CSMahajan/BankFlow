package com.bankflow.security;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
public class FileSecurityValidator {


    private static final long MAX_FILE_SIZE =
            5 * 1024 * 1024;


    public void validate(MultipartFile file) {

        validateSize(file);

        validateContentType(file);

        validateFileSignature(file);

    }


    private void validateSize(MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File cannot be empty"
            );
        }


        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size cannot exceed 5 MB"
            );
        }

    }


    private void validateContentType(MultipartFile file) {

        String contentType = file.getContentType();


        if (contentType == null ||
                !(contentType.equals("application/pdf")
                        || contentType.equals("image/png")
                        || contentType.equals("image/jpeg"))) {

            throw new IllegalArgumentException(
                    "Only PDF, PNG and JPG files are allowed"
            );
        }

    }


    private void validateFileSignature(MultipartFile file) {

        try (InputStream inputStream = file.getInputStream()) {

            byte[] header =
                    inputStream.readNBytes(8);


            boolean valid =
                    isPdf(header)
                            ||
                            isPng(header)
                            ||
                            isJpeg(header);


            if (!valid) {

                throw new IllegalArgumentException(
                        "File content does not match file type"
                );

            }


        } catch (IOException e) {

            throw new IllegalArgumentException(
                    "Unable to validate uploaded file"
            );

        }

    }


    private boolean isPdf(byte[] bytes) {

        return bytes.length >= 4
                &&
                bytes[0] == '%'
                &&
                bytes[1] == 'P'
                &&
                bytes[2] == 'D'
                &&
                bytes[3] == 'F';

    }


    private boolean isPng(byte[] bytes) {

        return bytes.length >= 8
                &&
                bytes[0] == (byte) 0x89
                &&
                bytes[1] == 'P'
                &&
                bytes[2] == 'N'
                &&
                bytes[3] == 'G'
                &&
                bytes[4] == 0x0D
                &&
                bytes[5] == 0x0A
                &&
                bytes[6] == 0x1A
                &&
                bytes[7] == 0x0A;

    }


    private boolean isJpeg(byte[] bytes) {

        return bytes.length >= 3
                &&
                bytes[0] == (byte) 0xFF
                &&
                bytes[1] == (byte) 0xD8
                &&
                bytes[2] == (byte) 0xFF;

    }

}