package com.bankflow.service;

import com.bankflow.dto.TransactionResponse;
import com.bankflow.entity.Transaction.TransactionType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPane;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelExportServiceTest {

    private ExcelExportService excelExportService;

    @BeforeEach
    void setUp() {
        excelExportService = new ExcelExportService();
    }

    @Test
    void generateTransactionExcel_shouldGenerateValidExcelFile() {

        List<TransactionResponse> transactions = List.of(
                createTransaction(
                        "TXN001",
                        "1234567890",
                        TransactionType.CREDIT,
                        new BigDecimal("15000.50"),
                        new BigDecimal("25000.50"),
                        "Salary credit"
                )
        );

        byte[] result =
                excelExportService.generateTransactionExcel(transactions);

        assertNotNull(result);
        assertTrue(result.length > 0);

        assertDoesNotThrow(() -> {
            try (Workbook workbook = new XSSFWorkbook(
                    new ByteArrayInputStream(result)
            )) {
                assertNotNull(workbook);
            }
        });
    }

    @Test
    void generateTransactionExcel_shouldCreateTransactionsSheet() throws Exception {

        List<TransactionResponse> transactions = List.of(
                createTransaction(
                        "TXN001",
                        "1234567890",
                        TransactionType.CREDIT,
                        new BigDecimal("15000.50"),
                        new BigDecimal("25000.50"),
                        "Salary credit"
                )
        );

        byte[] result =
                excelExportService.generateTransactionExcel(transactions);

        try (Workbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(result)
        )) {

            assertEquals(1, workbook.getNumberOfSheets());

            Sheet sheet = workbook.getSheet("Transactions");

            assertNotNull(sheet);
        }
    }

    @Test
    void generateTransactionExcel_shouldCreateTitleAndSubtitle() throws Exception {

        byte[] result =
                excelExportService.generateTransactionExcel(List.of());

        try (Workbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(result)
        )) {

            Sheet sheet = workbook.getSheet("Transactions");

            assertEquals(
                    "BankFlow",
                    sheet.getRow(0).getCell(0).getStringCellValue()
            );

            assertEquals(
                    "Transaction History",
                    sheet.getRow(1).getCell(0).getStringCellValue()
            );
        }
    }

    @Test
    void generateTransactionExcel_shouldCreateInformationRows() throws Exception {

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
                excelExportService.generateTransactionExcel(transactions);

        try (Workbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(result)
        )) {

            Sheet sheet = workbook.getSheet("Transactions");

            assertEquals(
                    "Generated On",
                    sheet.getRow(2).getCell(0).getStringCellValue()
            );

            assertFalse(
                    sheet.getRow(2).getCell(1)
                            .getStringCellValue()
                            .isBlank()
            );

            assertEquals(
                    "Total Transactions",
                    sheet.getRow(3).getCell(0).getStringCellValue()
            );

            assertEquals(
                    2,
                    sheet.getRow(3).getCell(1).getNumericCellValue()
            );
        }
    }

    @Test
    void generateTransactionExcel_shouldCreateCorrectHeaders() throws Exception {

        byte[] result =
                excelExportService.generateTransactionExcel(List.of());

        try (Workbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(result)
        )) {

            Sheet sheet = workbook.getSheet("Transactions");

            Row headerRow = sheet.getRow(5);

            assertNotNull(headerRow);

            assertEquals(
                    "Date",
                    headerRow.getCell(0).getStringCellValue()
            );

            assertEquals(
                    "Reference",
                    headerRow.getCell(1).getStringCellValue()
            );

            assertEquals(
                    "Type",
                    headerRow.getCell(2).getStringCellValue()
            );

            assertEquals(
                    "Amount",
                    headerRow.getCell(3).getStringCellValue()
            );

            assertEquals(
                    "Balance",
                    headerRow.getCell(4).getStringCellValue()
            );

            assertEquals(
                    "Description",
                    headerRow.getCell(5).getStringCellValue()
            );
        }
    }

    @Test
    void generateTransactionExcel_shouldWriteTransactionDataCorrectly()
            throws Exception {

        LocalDateTime transactionDate =
                LocalDateTime.of(2026, 8, 30, 14, 30);

        TransactionResponse transaction =
                new TransactionResponse(
                        "TXN001",
                        "1234567890",
                        transactionDate,
                        TransactionType.CREDIT,
                        new BigDecimal("15000.50"),
                        new BigDecimal("25000.75"),
                        "Salary credit"
                );

        byte[] result =
                excelExportService.generateTransactionExcel(
                        List.of(transaction)
                );

        try (Workbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(result)
        )) {

            Sheet sheet = workbook.getSheet("Transactions");

            Row row = sheet.getRow(6);

            assertNotNull(row);

            assertEquals(
                    "30 Aug 2026 14:30",
                    row.getCell(0).getStringCellValue()
            );

            assertEquals(
                    "TXN001",
                    row.getCell(1).getStringCellValue()
            );

            assertEquals(
                    TransactionType.CREDIT.toString(),
                    row.getCell(2).getStringCellValue()
            );

            assertEquals(
                    "Rs. 15,000.50",
                    row.getCell(3).getStringCellValue()
            );

            assertEquals(
                    "Rs. 25,000.75",
                    row.getCell(4).getStringCellValue()
            );

            assertEquals(
                    "Salary credit",
                    row.getCell(5).getStringCellValue()
            );
        }
    }

    @Test
    void generateTransactionExcel_shouldUseDashForNullDescription()
            throws Exception {

        TransactionResponse transaction =
                createTransaction(
                        "TXN001",
                        "1234567890",
                        TransactionType.DEBIT,
                        new BigDecimal("500.00"),
                        new BigDecimal("4500.00"),
                        null
                );

        byte[] result =
                excelExportService.generateTransactionExcel(
                        List.of(transaction)
                );

        try (Workbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(result)
        )) {

            Sheet sheet = workbook.getSheet("Transactions");

            Row row = sheet.getRow(6);

            assertEquals(
                    "-",
                    row.getCell(5).getStringCellValue()
            );
        }
    }

    @Test
    void generateTransactionExcel_shouldHandleEmptyTransactionList()
            throws Exception {

        byte[] result =
                excelExportService.generateTransactionExcel(List.of());

        assertNotNull(result);
        assertTrue(result.length > 0);

        try (Workbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(result)
        )) {

            Sheet sheet = workbook.getSheet("Transactions");

            assertNotNull(sheet);

            assertEquals(
                    "Total Transactions",
                    sheet.getRow(3)
                            .getCell(0)
                            .getStringCellValue()
            );

            assertEquals(
                    0,
                    sheet.getRow(3)
                            .getCell(1)
                            .getNumericCellValue()
            );

            assertNotNull(sheet.getRow(5));
        }
    }

    @Test
    void generateTransactionExcel_shouldFreezeHeaderRows() throws Exception {

        byte[] result =
                excelExportService.generateTransactionExcel(List.of());

        try (XSSFWorkbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(result)
        )) {

            XSSFSheet sheet =
                    workbook.getSheet("Transactions");

            assertNotNull(sheet);

            var pane =
                    sheet.getCTWorksheet()
                            .getSheetViews()
                            .getSheetViewArray(0)
                            .getPane();

            assertNotNull(pane);

            assertEquals(
                    6,
                    pane.getYSplit()
            );

            assertEquals(
                    STPane.BOTTOM_LEFT,
                    pane.getActivePane()
            );
        }
    }

    @Test
    void generateTransactionExcel_shouldMergeTitleRows() throws Exception {

        byte[] result =
                excelExportService.generateTransactionExcel(List.of());

        try (Workbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(result)
        )) {

            Sheet sheet = workbook.getSheet("Transactions");

            assertTrue(
                    sheet.getMergedRegions().stream()
                            .anyMatch(region ->
                                    region.getFirstRow() == 0 &&
                                            region.getLastRow() == 0 &&
                                            region.getFirstColumn() == 0 &&
                                            region.getLastColumn() == 5
                            )
            );

            assertTrue(
                    sheet.getMergedRegions().stream()
                            .anyMatch(region ->
                                    region.getFirstRow() == 1 &&
                                            region.getLastRow() == 1 &&
                                            region.getFirstColumn() == 0 &&
                                            region.getLastColumn() == 5
                            )
            );
        }
    }

    @Test
    void generateTransactionExcel_shouldCreateMultipleTransactionRows()
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
                excelExportService.generateTransactionExcel(
                        transactions
                );

        try (Workbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(result)
        )) {

            Sheet sheet = workbook.getSheet("Transactions");

            assertEquals(
                    "TXN001",
                    sheet.getRow(6)
                            .getCell(1)
                            .getStringCellValue()
            );

            assertEquals(
                    "TXN002",
                    sheet.getRow(7)
                            .getCell(1)
                            .getStringCellValue()
            );
        }
    }

    @Test
    void generateTransactionExcel_shouldStyleHeaderCorrectly()
            throws Exception {

        byte[] result =
                excelExportService.generateTransactionExcel(List.of());

        try (Workbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(result)
        )) {

            Sheet sheet = workbook.getSheet("Transactions");

            Cell headerCell = sheet
                    .getRow(5)
                    .getCell(0);

            CellStyle style = headerCell.getCellStyle();

            Font font = workbook.getFontAt(
                    style.getFontIndex()
            );

            assertTrue(font.getBold());

            assertEquals(
                    IndexedColors.WHITE.getIndex(),
                    font.getColor()
            );

            assertEquals(
                    HorizontalAlignment.CENTER,
                    style.getAlignment()
            );

            assertEquals(
                    FillPatternType.SOLID_FOREGROUND,
                    style.getFillPattern()
            );
        }
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
                LocalDateTime.of(2026, 8, 30, 14, 30),
                transactionType,
                amount,
                availableBalance,
                description
        );
    }

}