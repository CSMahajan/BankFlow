package com.bankflow.service;

import com.bankflow.dto.TransactionResponse;
import com.bankflow.entity.Transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.pdf.PdfReader;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfExportServiceTest {

    private PdfExportService pdfExportService;

    @BeforeEach
    void setUp() {
        pdfExportService = new PdfExportService();
    }

    @Test
    void generateTransactionPdf_shouldGenerateValidPdf() {

        TransactionResponse transaction = createTransaction(
                "TXN001",
                "1234567890",
                TransactionType.CREDIT,
                new BigDecimal("15000.50"),
                new BigDecimal("25000.75"),
                "Salary credit"
        );

        byte[] result =
                pdfExportService.generateTransactionPdf(
                        List.of(transaction)
                );

        assertNotNull(result);
        assertTrue(result.length > 0);

        assertDoesNotThrow(() -> {
            PdfReader reader = new PdfReader(
                    new ByteArrayInputStream(result)
            );

            assertNotNull(reader);
            reader.close();
        });
    }

    @Test
    void generateTransactionPdf_shouldCreateSinglePageForSmallData() throws Exception {

        TransactionResponse transaction = createTransaction(
                "TXN001",
                "1234567890",
                TransactionType.CREDIT,
                new BigDecimal("1000.00"),
                new BigDecimal("5000.00"),
                "Salary"
        );

        byte[] result =
                pdfExportService.generateTransactionPdf(
                        List.of(transaction)
                );

        PdfReader reader = new PdfReader(
                new ByteArrayInputStream(result)
        );

        assertEquals(1, reader.getNumberOfPages());

        reader.close();
    }

    @Test
    void generateTransactionPdf_shouldUseLandscapeA4Page()
            throws Exception {

        byte[] result =
                pdfExportService.generateTransactionPdf(List.of());

        PdfReader reader = new PdfReader(
                new ByteArrayInputStream(result)
        );

        float width = reader.getPageSizeWithRotation(1).getWidth();
        float height = reader.getPageSizeWithRotation(1).getHeight();

        assertTrue(
                width > height,
                "PDF should use landscape orientation"
        );

        assertEquals(842f, width, 1f);
        assertEquals(595f, height, 1f);

        reader.close();
    }

    @Test
    void generateTransactionPdf_shouldContainTitleAndSubtitle()
            throws Exception {

        byte[] result =
                pdfExportService.generateTransactionPdf(List.of());

        String text = extractPdfText(result);

        assertTrue(text.contains("BankFlow"));
        assertTrue(text.contains("Transaction History"));
    }

    @Test
    void generateTransactionPdf_shouldContainGeneratedOnText()
            throws Exception {

        byte[] result =
                pdfExportService.generateTransactionPdf(List.of());

        String text = extractPdfText(result);

        assertTrue(text.contains("Generated On :"));
    }

    @Test
    void generateTransactionPdf_shouldContainTransactionCount()
            throws Exception {

        List<TransactionResponse> transactions = List.of(
                createTransaction(
                        "TXN001",
                        "1234567890",
                        TransactionType.CREDIT,
                        new BigDecimal("1000.00"),
                        new BigDecimal("5000.00"),
                        "Credit"
                ),
                createTransaction(
                        "TXN002",
                        "1234567890",
                        TransactionType.DEBIT,
                        new BigDecimal("500.00"),
                        new BigDecimal("4500.00"),
                        "Payment"
                )
        );

        byte[] result =
                pdfExportService.generateTransactionPdf(transactions);

        String text = extractPdfText(result);

        assertTrue(
                text.contains("Total Transactions : 2")
        );
    }

    @Test
    void generateTransactionPdf_shouldContainCorrectTableHeaders()
            throws Exception {

        TransactionResponse transaction = createTransaction(
                "TXN001",
                "1234567890",
                TransactionType.CREDIT,
                new BigDecimal("1000.00"),
                new BigDecimal("5000.00"),
                "Salary"
        );

        byte[] result =
                pdfExportService.generateTransactionPdf(
                        List.of(transaction)
                );

        String text = extractPdfText(result);

        assertTrue(text.contains("Date"));
        assertTrue(text.contains("Reference"));
        assertTrue(text.contains("Type"));
        assertTrue(text.contains("Amount"));
        assertTrue(text.contains("Balance"));
        assertTrue(text.contains("Description"));
    }

    @Test
    void generateTransactionPdf_shouldWriteTransactionDataCorrectly()
            throws Exception {

        TransactionResponse transaction = new TransactionResponse(
                "TXN001",
                "1234567890",
                LocalDateTime.of(
                        2026,
                        8,
                        30,
                        14,
                        30
                ),
                TransactionType.CREDIT,
                new BigDecimal("15000.50"),
                new BigDecimal("25000.75"),
                "Salary credit"
        );

        byte[] result =
                pdfExportService.generateTransactionPdf(
                        List.of(transaction)
                );

        String text = extractPdfText(result);

        assertTrue(
                text.contains("30 Aug 2026 14:30")
        );

        assertTrue(
                text.contains("TXN001")
        );

        assertTrue(
                text.contains(
                        TransactionType.CREDIT.toString()
                )
        );

        assertTrue(
                text.contains("Rs. 15,000.50")
        );

        assertTrue(
                text.contains("Rs. 25,000.75")
        );

        assertTrue(
                text.contains("Salary credit")
        );
    }

    @Test
    void generateTransactionPdf_shouldUseDashForNullDescription()
            throws Exception {

        TransactionResponse transaction = createTransaction(
                "TXN001",
                "1234567890",
                TransactionType.DEBIT,
                new BigDecimal("500.00"),
                new BigDecimal("4500.00"),
                null
        );

        byte[] result =
                pdfExportService.generateTransactionPdf(
                        List.of(transaction)
                );

        String text = extractPdfText(result);

        assertTrue(text.contains("-"));
    }

    @Test
    void generateTransactionPdf_shouldHandleEmptyTransactionList()
            throws Exception {

        byte[] result =
                pdfExportService.generateTransactionPdf(List.of());

        assertNotNull(result);
        assertTrue(result.length > 0);

        String text = extractPdfText(result);

        assertTrue(text.contains("BankFlow"));
        assertTrue(text.contains("Transaction History"));
        assertTrue(text.contains("Total Transactions : 0"));
    }

    @Test
    void generateTransactionPdf_shouldWriteMultipleTransactions()
            throws Exception {

        List<TransactionResponse> transactions = List.of(
                createTransaction(
                        "TXN001",
                        "1234567890",
                        TransactionType.CREDIT,
                        new BigDecimal("1000.00"),
                        new BigDecimal("5000.00"),
                        "Salary"
                ),
                createTransaction(
                        "TXN002",
                        "1234567890",
                        TransactionType.DEBIT,
                        new BigDecimal("500.00"),
                        new BigDecimal("4500.00"),
                        "Payment"
                ),
                createTransaction(
                        "TXN003",
                        "1234567890",
                        TransactionType.CREDIT,
                        new BigDecimal("2000.00"),
                        new BigDecimal("6500.00"),
                        "Refund"
                )
        );

        byte[] result =
                pdfExportService.generateTransactionPdf(
                        transactions
                );

        String text = extractPdfText(result);

        assertTrue(text.contains("TXN001"));
        assertTrue(text.contains("TXN002"));
        assertTrue(text.contains("TXN003"));

        assertTrue(text.contains("Salary"));
        assertTrue(text.contains("Payment"));
        assertTrue(text.contains("Refund"));

        assertTrue(
                text.contains("Total Transactions : 3")
        );
    }

    @Test
    void generateTransactionPdf_shouldContainCreditAndDebitTypes()
            throws Exception {

        List<TransactionResponse> transactions = List.of(
                createTransaction(
                        "TXN001",
                        "1234567890",
                        TransactionType.CREDIT,
                        new BigDecimal("1000.00"),
                        new BigDecimal("5000.00"),
                        "Credit"
                ),
                createTransaction(
                        "TXN002",
                        "1234567890",
                        TransactionType.DEBIT,
                        new BigDecimal("500.00"),
                        new BigDecimal("4500.00"),
                        "Debit"
                )
        );

        byte[] result =
                pdfExportService.generateTransactionPdf(
                        transactions
                );

        String text = extractPdfText(result);

        assertTrue(
                text.contains(
                        TransactionType.CREDIT.toString()
                )
        );

        assertTrue(
                text.contains(
                        TransactionType.DEBIT.toString()
                )
        );
    }

    @Test
    void generateTransactionPdf_shouldFormatCurrencyWithTwoDecimals()
            throws Exception {

        TransactionResponse transaction = createTransaction(
                "TXN001",
                "1234567890",
                TransactionType.CREDIT,
                new BigDecimal("100"),
                new BigDecimal("5000.5"),
                "Test"
        );

        byte[] result =
                pdfExportService.generateTransactionPdf(
                        List.of(transaction)
                );

        String text = extractPdfText(result);

        assertTrue(
                text.contains("Rs. 100.00")
        );

        assertTrue(
                text.contains("Rs. 5,000.50")
        );
    }

    @Test
    void generateTransactionPdf_shouldGeneratePdfWithLargeTransactionList()
            throws Exception {

        List<TransactionResponse> transactions =
                java.util.stream.IntStream.rangeClosed(1, 100)
                        .mapToObj(i ->
                                createTransaction(
                                        "TXN" + i,
                                        "1234567890",
                                        i % 2 == 0
                                                ? TransactionType.CREDIT
                                                : TransactionType.DEBIT,
                                        new BigDecimal("1000.00"),
                                        new BigDecimal("5000.00"),
                                        "Transaction " + i
                                )
                        )
                        .toList();

        byte[] result =
                pdfExportService.generateTransactionPdf(
                        transactions
                );

        assertNotNull(result);
        assertTrue(result.length > 0);

        PdfReader reader = new PdfReader(
                new ByteArrayInputStream(result)
        );

        assertTrue(reader.getNumberOfPages() > 1);

        reader.close();

        String text = extractPdfText(result);

        assertTrue(text.contains("TXN1"));
        assertTrue(text.contains("TXN100"));
        assertTrue(
                text.contains("Total Transactions : 100")
        );
    }

    private String extractPdfText(byte[] pdfBytes)
            throws Exception {

        PdfReader reader = new PdfReader(
                new ByteArrayInputStream(pdfBytes)
        );

        org.openpdf.text.pdf.parser.PdfTextExtractor extractor =
                new org.openpdf.text.pdf.parser.PdfTextExtractor(reader);

        StringBuilder text = new StringBuilder();

        for (int page = 1;
             page <= reader.getNumberOfPages();
             page++) {

            text.append(
                    extractor.getTextFromPage(page)
            );
        }

        reader.close();

        return text.toString();
    }

    private TransactionResponse createTransaction(
            String transactionId,
            String accountNumber,
            TransactionType transactionType,
            BigDecimal amount,
            BigDecimal availableBalance,
            String description
    ) {

        return new TransactionResponse(
                transactionId,
                accountNumber,
                LocalDateTime.of(
                        2026,
                        8,
                        30,
                        14,
                        30
                ),
                transactionType,
                amount,
                availableBalance,
                description
        );
    }
}