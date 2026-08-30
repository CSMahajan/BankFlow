package com.bankflow.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileNameSanitizerTest {

    private FileNameSanitizer sanitizer;

    @BeforeEach
    void setUp() {
        sanitizer = new FileNameSanitizer();
    }

    @Test
    void sanitize_shouldReturnDefaultNameWhenFilenameIsNull() {

        String result = sanitizer.sanitize(null);

        assertEquals(
                "uploaded-document",
                result
        );
    }

    @Test
    void sanitize_shouldReturnDefaultNameWhenFilenameIsBlank() {

        String result = sanitizer.sanitize("   ");

        assertEquals(
                "uploaded-document",
                result
        );
    }

    @Test
    void sanitize_shouldKeepValidFilenameCharacters() {

        String result = sanitizer.sanitize(
                "aadhaar-document_123.pdf"
        );

        assertEquals(
                "aadhaar-document_123.pdf",
                result
        );
    }

    @Test
    void sanitize_shouldRemoveDirectoryPath() {

        String result = sanitizer.sanitize(
                "/documents/uploads/aadhaar.pdf"
        );

        assertEquals(
                "aadhaar.pdf",
                result
        );
    }

    @Test
    void sanitize_shouldRemoveWindowsDirectoryPath() {

        String result = sanitizer.sanitize(
                "C:\\documents\\uploads\\aadhaar.pdf"
        );

        assertEquals(
                "aadhaar.pdf",
                result
        );
    }

    @Test
    void sanitize_shouldReplaceSpecialCharacters() {

        String result = sanitizer.sanitize(
                "my document@2026!.pdf"
        );

        assertEquals(
                "my*document*2026*.pdf",
                result
        );
    }

    @Test
    void sanitize_shouldAllowDots() {

        String result = sanitizer.sanitize(
                "document.final.pdf"
        );

        assertEquals(
                "document.final.pdf",
                result
        );
    }

    @Test
    void sanitize_shouldAllowHyphens() {

        String result = sanitizer.sanitize(
                "aadhaar-document.pdf"
        );

        assertEquals(
                "aadhaar-document.pdf",
                result
        );
    }

    @Test
    void sanitize_shouldReplaceSpaces() {

        String result = sanitizer.sanitize(
                "aadhaar document.pdf"
        );

        assertEquals(
                "aadhaar*document.pdf",
                result
        );
    }

    @Test
    void sanitize_shouldReplacePathTraversalCharacters() {

        String result = sanitizer.sanitize(
                "../../etc/passwd"
        );

        assertEquals(
                "passwd",
                result
        );
    }

    @Test
    void sanitize_shouldHandleFilenameWithOnlySpecialCharacters() {

        String result = sanitizer.sanitize(
                "@#$%^&"
        );

        assertEquals(
                "******",
                result
        );
    }

    @Test
    void sanitize_shouldPreserveUppercaseAndLowercaseCharacters() {

        String result = sanitizer.sanitize(
                "AadhaarDocument.PDF"
        );

        assertEquals(
                "AadhaarDocument.PDF",
                result
        );
    }
}