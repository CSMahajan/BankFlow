package com.bankflow.service;

import com.bankflow.dto.PanExtractedData;
import com.bankflow.exception.ExtractionValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PanExtractionValidatorTest {

    private PanExtractionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PanExtractionValidator();
    }

    @Test
    void validate_shouldPassForValidData() {

        PanExtractedData data = new PanExtractedData(
                "ABCDE1234F",
                "Rahul Sharma",
                "Rajesh Sharma",
                LocalDate.of(1995, 8, 15)
        );

        assertDoesNotThrow(
                () -> validator.validate(data)
        );
    }

    @Test
    void validate_shouldThrowExceptionWhenPanNumberIsNull() {

        PanExtractedData data = new PanExtractedData(
                null,
                "Rahul Sharma",
                "Rajesh Sharma",
                LocalDate.of(1995, 8, 15)
        );

        ExtractionValidationException exception =
                assertThrows(
                        ExtractionValidationException.class,
                        () -> validator.validate(data)
                );

        assertEquals(
                "PAN number could not be extracted",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldThrowExceptionWhenPanNumberIsBlank() {

        PanExtractedData data = new PanExtractedData(
                "   ",
                "Rahul Sharma",
                "Rajesh Sharma",
                LocalDate.of(1995, 8, 15)
        );

        ExtractionValidationException exception =
                assertThrows(
                        ExtractionValidationException.class,
                        () -> validator.validate(data)
                );

        assertEquals(
                "PAN number could not be extracted",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldThrowExceptionWhenNameIsNull() {

        PanExtractedData data = new PanExtractedData(
                "ABCDE1234F",
                null,
                "Rajesh Sharma",
                LocalDate.of(1995, 8, 15)
        );

        ExtractionValidationException exception =
                assertThrows(
                        ExtractionValidationException.class,
                        () -> validator.validate(data)
                );

        assertEquals(
                "PAN holder name could not be extracted",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldThrowExceptionWhenNameIsBlank() {

        PanExtractedData data = new PanExtractedData(
                "ABCDE1234F",
                "   ",
                "Rajesh Sharma",
                LocalDate.of(1995, 8, 15)
        );

        ExtractionValidationException exception =
                assertThrows(
                        ExtractionValidationException.class,
                        () -> validator.validate(data)
                );

        assertEquals(
                "PAN holder name could not be extracted",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldThrowExceptionWhenDateOfBirthIsNull() {

        PanExtractedData data = new PanExtractedData(
                "ABCDE1234F",
                "Rahul Sharma",
                "Rajesh Sharma",
                null
        );

        ExtractionValidationException exception =
                assertThrows(
                        ExtractionValidationException.class,
                        () -> validator.validate(data)
                );

        assertEquals(
                "PAN date of birth could not be extracted",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldValidatePanNumberBeforeName() {

        PanExtractedData data = new PanExtractedData(
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
                "PAN number could not be extracted",
                exception.getMessage()
        );
    }

    @Test
    void validate_shouldValidateNameBeforeDateOfBirth() {

        PanExtractedData data = new PanExtractedData(
                "ABCDE1234F",
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
                "PAN holder name could not be extracted",
                exception.getMessage()
        );
    }
}