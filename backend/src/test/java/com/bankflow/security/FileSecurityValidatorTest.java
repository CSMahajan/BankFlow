package com.bankflow.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileSecurityValidatorTest {

    private FileSecurityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileSecurityValidator();
    }

    @Test
    void validate_shouldAcceptValidPdfFile() {

        byte[] content = "%PDF-1.7".getBytes();

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                content
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void validate_shouldAcceptValidPngFile() {

        byte[] content = {
                (byte) 0x89, 'P', 'N', 'G',
                0x0D, 0x0A, 0x1A, 0x0A
        };

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.png",
                "image/png",
                content
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void validate_shouldAcceptValidJpegFile() {

        byte[] content = {
                (byte) 0xFF,
                (byte) 0xD8,
                (byte) 0xFF
        };

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.jpg",
                "image/jpeg",
                content
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void validate_shouldRejectEmptyFile() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                new byte[0]
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(file)
                );

        assertEquals(
                "File cannot be empty",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldRejectFileLargerThan5MB() {

        byte[] content = new byte[5 * 1024 * 1024 + 1];

        MultipartFile file = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                content
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(file)
                );

        assertEquals(
                "File size cannot exceed 5 MB",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldAcceptFileExactly5MB() {

        byte[] content = new byte[5 * 1024 * 1024];

        // Make the signature valid for PDF.
        content[0] = '%';
        content[1] = 'P';
        content[2] = 'D';
        content[3] = 'F';

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                content
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void validate_shouldRejectUnsupportedContentType() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "hello".getBytes()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(file)
                );

        assertEquals(
                "Only PDF, PNG and JPG files are allowed",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldRejectNullContentType() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                null,
                "%PDF-1.7".getBytes()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(file)
                );

        assertEquals(
                "Only PDF, PNG and JPG files are allowed",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldRejectPdfWithInvalidSignature() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "This is not a PDF".getBytes()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(file)
                );

        assertEquals(
                "File content does not match file type",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldRejectPngWithInvalidSignature() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.png",
                "image/png",
                "not-a-png".getBytes()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(file)
                );

        assertEquals(
                "File content does not match file type",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldRejectJpegWithInvalidSignature() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.jpg",
                "image/jpeg",
                "not-a-jpeg".getBytes()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(file)
                );

        assertEquals(
                "File content does not match file type",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldRejectFileWithValidContentTypeButInvalidContent() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "PNG content pretending to be PDF".getBytes()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(file)
                );

        assertEquals(
                "File content does not match file type",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldRejectFileWithInsufficientSignatureBytes() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                new byte[]{'%', 'P', 'D'}
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(file)
                );

        assertEquals(
                "File content does not match file type",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldRejectJpegWithIncompleteSignature() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.jpg",
                "image/jpeg",
                new byte[]{
                        (byte) 0xFF,
                        (byte) 0xD8
                }
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(file)
                );

        assertEquals(
                "File content does not match file type",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldRejectPngWithIncompleteSignature() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.png",
                "image/png",
                new byte[]{
                        (byte) 0x89,
                        'P',
                        'N',
                        'G'
                }
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(file)
                );

        assertEquals(
                "File content does not match file type",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldRejectUnsupportedContentTypeBeforeCheckingSignature() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.exe",
                "application/octet-stream",
                "%PDF-1.7".getBytes()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(file)
                );

        assertEquals(
                "Only PDF, PNG and JPG files are allowed",
                exception.getMessage()
        );
    }
}