package com.bankflow.service;

import com.bankflow.dto.PanExtractedData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PanTextExtractorServiceTest {

    private PanTextExtractorService service;

    @BeforeEach
    void setUp() {
        service = new PanTextExtractorService();
    }

    @Test
    void extract_shouldExtractAllPanDetails() {

        String text = """
                INCOME TAX DEPARTMENT
                Name
                Rahul Sharma
                Father's Name
                Rajesh Sharma
                Date of Birth
                15/08/1995
                ABCDE1234F
                """;

        PanExtractedData result = service.extract(text);

        assertNotNull(result);

        assertEquals(
                "ABCDE1234F",
                result.panNumber()
        );

        assertEquals(
                "Rahul Sharma",
                result.fullName()
        );

        assertEquals(
                "Rajesh Sharma",
                result.fatherName()
        );

        assertEquals(
                LocalDate.of(1995, 8, 15),
                result.dateOfBirth()
        );
    }

    @Test
    void extract_shouldExtractPanNumber() {

        String text = """
                Rahul Sharma
                ABCDE1234F
                """;

        PanExtractedData result = service.extract(text);

        assertEquals(
                "ABCDE1234F",
                result.panNumber()
        );
    }

    @Test
    void extract_shouldReturnNullWhenPanNumberIsMissing() {

        String text = """
                Name
                Rahul Sharma
                Father's Name
                Rajesh Sharma
                Date of Birth
                15/08/1995
                """;

        PanExtractedData result = service.extract(text);

        assertNull(result.panNumber());
    }

    @Test
    void extract_shouldExtractName() {

        String text = """
                Name
                Rahul Sharma
                """;

        PanExtractedData result = service.extract(text);

        assertEquals(
                "Rahul Sharma",
                result.fullName()
        );
    }

    @Test
    void extract_shouldReturnNullWhenNameIsMissing() {

        String text = """
                Father's Name
                Rajesh Sharma
                Date of Birth
                15/08/1995
                ABCDE1234F
                """;

        PanExtractedData result = service.extract(text);

        assertNull(result.fullName());
    }

    @Test
    void extract_shouldExtractFatherName() {

        String text = """
                Father's Name
                Rajesh Sharma
                """;

        PanExtractedData result = service.extract(text);

        assertEquals(
                "Rajesh Sharma",
                result.fatherName()
        );
    }

    @Test
    void extract_shouldReturnNullWhenFatherNameIsMissing() {

        String text = """
                Name
                Rahul Sharma
                Date of Birth
                15/08/1995
                ABCDE1234F
                """;

        PanExtractedData result = service.extract(text);

        assertNull(result.fatherName());
    }

    @Test
    void extract_shouldExtractDob() {

        String text = """
                Date of Birth
                15/08/1995
                """;

        PanExtractedData result = service.extract(text);

        assertEquals(
                LocalDate.of(1995, 8, 15),
                result.dateOfBirth()
        );
    }

    @Test
    void extract_shouldReturnNullWhenDobIsMissing() {

        String text = """
                Name
                Rahul Sharma
                Father's Name
                Rajesh Sharma
                ABCDE1234F
                """;

        PanExtractedData result = service.extract(text);

        assertNull(result.dateOfBirth());
    }

    @Test
    void extract_shouldHandleEmptyText() {

        PanExtractedData result = service.extract("");

        assertNotNull(result);

        assertNull(result.panNumber());
        assertNull(result.fullName());
        assertNull(result.fatherName());
        assertNull(result.dateOfBirth());
    }

    @Test
    void extract_shouldHandleTextWithOnlyWhitespace() {

        PanExtractedData result = service.extract(
                "   \n   \n   "
        );

        assertNotNull(result);

        assertNull(result.panNumber());
        assertNull(result.fullName());
        assertNull(result.fatherName());
        assertNull(result.dateOfBirth());
    }

    @Test
    void extract_shouldExtractPanWhenTextContainsOtherContent() {

        String text = """
                GOVERNMENT OF INDIA
                INCOME TAX DEPARTMENT

                Permanent Account Number Card

                Name
                Rahul Sharma

                Father's Name
                Rajesh Sharma

                Date of Birth
                20/12/1990

                Permanent Account Number
                ABCDE1234F
                """;

        PanExtractedData result = service.extract(text);

        assertEquals(
                "ABCDE1234F",
                result.panNumber()
        );

        assertEquals(
                "Rahul Sharma",
                result.fullName()
        );

        assertEquals(
                "Rajesh Sharma",
                result.fatherName()
        );

        assertEquals(
                LocalDate.of(1990, 12, 20),
                result.dateOfBirth()
        );
    }

    @Test
    void extract_shouldNotExtractInvalidPanNumber() {

        String text = """
                Name
                Rahul Sharma
                Father's Name
                Rajesh Sharma
                Date of Birth
                15/08/1995
                ABC1234567
                """;

        PanExtractedData result = service.extract(text);

        assertNull(result.panNumber());
    }

    @Test
    void extract_shouldExtractPanWithoutSpaces() {

        String text = """
                Name
                Rahul Sharma
                Father's Name
                Rajesh Sharma
                Date of Birth
                15/08/1995
                ABCDE1234F
                """;

        PanExtractedData result = service.extract(text);

        assertEquals(
                "ABCDE1234F",
                result.panNumber()
        );
    }
}