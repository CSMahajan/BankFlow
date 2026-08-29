package com.bankflow.service;

import com.bankflow.dto.AadhaarExtractedData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AadhaarTextExtractorServiceTest {

    private AadhaarTextExtractorService service;

    @BeforeEach
    void setUp() {
        service = new AadhaarTextExtractorService();
    }

    @Test
    void extract_shouldExtractAllAadhaarDetails() {

        String text = """
                Government of India
                Rahul Sharma
                S/O Rajesh Sharma
                DOB: 15/08/1995
                MALE
                123 Main Street
                Pune Maharashtra
                9876543210
                1234 5678 9012
                """;

        AadhaarExtractedData result = service.extract(text);

        assertNotNull(result);

        assertEquals(
                "123456789012",
                result.aadhaarNumber()
        );

        assertEquals(
                "Rahul Sharma",
                result.fullName()
        );

        assertEquals(
                LocalDate.of(1995, 8, 15),
                result.dateOfBirth()
        );

        assertEquals(
                "MALE",
                result.gender()
        );

        assertEquals(
                "123 Main Street, Pune Maharashtra",
                result.address()
        );

        assertEquals(
                "9876543210",
                result.mobileNumber()
        );
    }

    @Test
    void extract_shouldExtractAadhaarNumberWithSpaces() {

        String text = """
                Rahul Sharma
                S/O Rajesh Sharma
                1234 5678 9012
                """;

        AadhaarExtractedData result = service.extract(text);

        assertEquals(
                "123456789012",
                result.aadhaarNumber()
        );
    }

    @Test
    void extract_shouldReturnNullWhenAadhaarNumberIsMissing() {

        String text = """
                Rahul Sharma
                S/O Rajesh Sharma
                DOB: 15/08/1995
                MALE
                9876543210
                """;

        AadhaarExtractedData result = service.extract(text);

        assertNull(result.aadhaarNumber());
    }

    @Test
    void extract_shouldExtractNameUsingRelationshipLine() {

        String text = """
                Government of India
                Rahul Sharma
                S/O Rajesh Sharma
                1234 5678 9012
                """;

        AadhaarExtractedData result = service.extract(text);

        assertEquals(
                "Rahul Sharma",
                result.fullName()
        );
    }

    @Test
    void extract_shouldExtractNameUsingDobFallback() {

        String text = """
                Government of India
                Rahul Sharma
                DOB: 15/08/1995
                1234 5678 9012
                """;

        AadhaarExtractedData result = service.extract(text);

        assertEquals(
                "Rahul Sharma",
                result.fullName()
        );
    }

    @Test
    void extract_shouldReturnNullWhenNameCannotBeIdentified() {

        String text = """
                Government of India
                1234 5678 9012
                DOB: 15/08/1995
                9876543210
                """;

        AadhaarExtractedData result = service.extract(text);

        assertNull(result.fullName());
    }

    @Test
    void extract_shouldExtractDob() {

        String text = """
                Rahul Sharma
                DOB: 15/08/1995
                """;

        AadhaarExtractedData result = service.extract(text);

        assertEquals(
                LocalDate.of(1995, 8, 15),
                result.dateOfBirth()
        );
    }

    @Test
    void extract_shouldSupportDateOfBirthFormat() {

        String text = """
                Rahul Sharma
                Date of Birth: 20/12/1990
                """;

        AadhaarExtractedData result = service.extract(text);

        assertEquals(
                LocalDate.of(1990, 12, 20),
                result.dateOfBirth()
        );
    }

    @Test
    void extract_shouldReturnNullWhenDobIsMissing() {

        String text = """
                Rahul Sharma
                MALE
                1234 5678 9012
                """;

        AadhaarExtractedData result = service.extract(text);

        assertNull(result.dateOfBirth());
    }

    @Test
    void extract_shouldExtractMaleGender() {

        AadhaarExtractedData result =
                service.extract("Rahul Sharma\nMALE");

        assertEquals(
                "MALE",
                result.gender()
        );
    }

    @Test
    void extract_shouldExtractFemaleGender() {

        AadhaarExtractedData result =
                service.extract("Priya Sharma\nFEMALE");

        assertEquals(
                "FEMALE",
                result.gender()
        );
    }

    @Test
    void extract_shouldReturnNullWhenGenderIsMissing() {

        AadhaarExtractedData result =
                service.extract("Rahul Sharma");

        assertNull(result.gender());
    }

    @Test
    void extract_shouldExtractMobileNumber() {

        String text = """
                Rahul Sharma
                Mobile: 9876543210
                """;

        AadhaarExtractedData result = service.extract(text);

        assertEquals(
                "9876543210",
                result.mobileNumber()
        );
    }

    @Test
    void extract_shouldReturnNullWhenMobileNumberIsMissing() {

        AadhaarExtractedData result =
                service.extract("Rahul Sharma\nMALE");

        assertNull(result.mobileNumber());
    }

    @Test
    void extract_shouldNotExtractInvalidMobileNumber() {

        String text = """
                Rahul Sharma
                Mobile: 5123456789
                """;

        AadhaarExtractedData result = service.extract(text);

        assertNull(result.mobileNumber());
    }

    @Test
    void extract_shouldExtractAddressBetweenNameAndMobile() {

        String text = """
                Government of India
                Rahul Sharma
                S/O Rajesh Sharma
                Flat 101 Building A
                Hinjewadi
                Pune Maharashtra
                9876543210
                """;

        AadhaarExtractedData result = service.extract(text);

        assertEquals(
                "Flat 101 Building A, Hinjewadi, Pune Maharashtra",
                result.address()
        );
    }

    @Test
    void extract_shouldIgnoreRelationshipLineFromAddress() {

        String text = """
                Government of India
                Rahul Sharma
                S/O Rajesh Sharma
                Flat 101
                Pune
                9876543210
                """;

        AadhaarExtractedData result = service.extract(text);

        assertEquals(
                "Flat 101, Pune",
                result.address()
        );
    }

    @Test
    void extract_shouldReturnNullWhenMobileIsMissingForAddressExtraction() {

        String text = """
                Government of India
                Rahul Sharma
                S/O Rajesh Sharma
                Flat 101
                Pune
                """;

        AadhaarExtractedData result = service.extract(text);

        assertNull(result.address());
    }

    @Test
    void extract_shouldReturnNullAddressWhenNoAddressExists() {

        String text = """
                Government of India
                Rahul Sharma
                S/O Rajesh Sharma
                9876543210
                """;

        AadhaarExtractedData result = service.extract(text);

        assertNull(result.address());
    }

    @Test
    void extract_shouldHandleEmptyText() {

        AadhaarExtractedData result =
                service.extract("");

        assertNotNull(result);

        assertNull(result.aadhaarNumber());
        assertNull(result.fullName());
        assertNull(result.dateOfBirth());
        assertNull(result.gender());
        assertNull(result.address());
        assertNull(result.mobileNumber());
    }

    @Test
    void extract_shouldHandleTextWithOnlyWhitespace() {

        AadhaarExtractedData result =
                service.extract("   \n   \n   ");

        assertNotNull(result);

        assertNull(result.aadhaarNumber());
        assertNull(result.fullName());
        assertNull(result.dateOfBirth());
        assertNull(result.gender());
        assertNull(result.address());
        assertNull(result.mobileNumber());
    }

    @Test
    void extract_shouldExtractAadhaarNumberOnlyWhenItIsASeparateLine() {

        String text = """
                Rahul Sharma
                Aadhaar: 1234 5678 9012
                """;

        AadhaarExtractedData result = service.extract(text);

        /*
         * Current implementation requires the Aadhaar number
         * to occupy its own line.
         */
        assertNull(result.aadhaarNumber());
    }

    @Test
    void extract_shouldNotTreatNumbersAsName() {

        String text = """
                Government of India
                Rahul123
                DOB: 15/08/1995
                1234 5678 9012
                """;

        AadhaarExtractedData result = service.extract(text);

        assertNull(result.fullName());
    }
}