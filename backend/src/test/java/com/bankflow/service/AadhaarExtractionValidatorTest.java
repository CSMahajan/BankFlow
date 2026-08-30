package com.bankflow.service;

import com.bankflow.dto.AadhaarExtractedData;
import com.bankflow.exception.ExtractionValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AadhaarExtractionValidatorTest {

    private AadhaarExtractionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AadhaarExtractionValidator();
    }

    @Test
    void validate_shouldPassForValidData() {

        AadhaarExtractedData data = new AadhaarExtractedData(
                "123456789012",
                "Rahul Sharma",
                LocalDate.of(1995, 8, 15),
                "MALE",
                "123 Main Street, Pune",
                "9876543210"
        );

        assertDoesNotThrow(
                () -> validator.validate(data)
        );
    }

    @Test
    void validate_shouldThrowExceptionWhenAadhaarNumberIsNull() {

        AadhaarExtractedData data = new AadhaarExtractedData(
                null,
                "Rahul Sharma",
                LocalDate.of(1995, 8, 15),
                "MALE",
                "123 Main Street, Pune",
                "9876543210"
        );

        ExtractionValidationException exception =
                assertThrows(
                        ExtractionValidationException.class,
                        () -> validator.validate(data)
                );

        assertEquals(
                "Aadhaar number could not be extracted",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldThrowExceptionWhenAadhaarNumberIsBlank() {

        AadhaarExtractedData data = new AadhaarExtractedData(
                "   ",
                "Rahul Sharma",
                LocalDate.of(1995, 8, 15),
                "MALE",
                "123 Main Street, Pune",
                "9876543210"
        );

        ExtractionValidationException exception =
                assertThrows(
                        ExtractionValidationException.class,
                        () -> validator.validate(data)
                );

        assertEquals(
                "Aadhaar number could not be extracted",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldThrowExceptionWhenNameIsNull() {

        AadhaarExtractedData data = new AadhaarExtractedData(
                "123456789012",
                null,
                LocalDate.of(1995, 8, 15),
                "MALE",
                "123 Main Street, Pune",
                "9876543210"
        );

        ExtractionValidationException exception =
                assertThrows(
                        ExtractionValidationException.class,
                        () -> validator.validate(data)
                );

        assertEquals(
                "Aadhaar holder name could not be extracted",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldThrowExceptionWhenNameIsBlank() {

        AadhaarExtractedData data = new AadhaarExtractedData(
                "123456789012",
                "   ",
                LocalDate.of(1995, 8, 15),
                "MALE",
                "123 Main Street, Pune",
                "9876543210"
        );

        ExtractionValidationException exception =
                assertThrows(
                        ExtractionValidationException.class,
                        () -> validator.validate(data)
                );

        assertEquals(
                "Aadhaar holder name could not be extracted",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldThrowExceptionWhenDobIsNull() {

        AadhaarExtractedData data = new AadhaarExtractedData(
                "123456789012",
                "Rahul Sharma",
                null,
                "MALE",
                "123 Main Street, Pune",
                "9876543210"
        );

        ExtractionValidationException exception =
                assertThrows(
                        ExtractionValidationException.class,
                        () -> validator.validate(data)
                );

        assertEquals(
                "Aadhaar DOB could not be extracted",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldValidateAadhaarNumberBeforeName() {

        AadhaarExtractedData data = new AadhaarExtractedData(
                null,
                null,
                null,
                null,
                null,
                null
        );

        ExtractionValidationException exception =
                assertThrows(
                        ExtractionValidationException.class,
                        () -> validator.validate(data)
                );

        assertEquals(
                "Aadhaar number could not be extracted",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldValidateNameBeforeDob() {

        AadhaarExtractedData data = new AadhaarExtractedData(
                "123456789012",
                null,
                null,
                null,
                null,
                null
        );

        ExtractionValidationException exception =
                assertThrows(
                        ExtractionValidationException.class,
                        () -> validator.validate(data)
                );

        assertEquals(
                "Aadhaar holder name could not be extracted",
                exception.getMessage()
        );
    }
}