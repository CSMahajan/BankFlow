package com.bankflow.service;

import com.bankflow.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    public byte[] generateTransactionExcel(
            List<TransactionResponse> transactions) {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Transactions");
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(28);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BankFlow");

            Row subtitleRow = sheet.createRow(1);
            subtitleRow.setHeightInPoints(20);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("Transaction History");
            sheet.addMergedRegion(
                    new CellRangeAddress(0, 0, 0, 5)
            );

            sheet.addMergedRegion(
                    new CellRangeAddress(1, 1, 0, 5)
            );
            XSSFCellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            XSSFFont titleFont = workbook.createFont();

            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 18);
            titleFont.setColor(
                    new XSSFColor(
                            new java.awt.Color(13, 99, 96),
                            null
                    )
            );

            CellStyle subtitleStyle = workbook.createCellStyle();
            subtitleStyle.setAlignment(HorizontalAlignment.CENTER);

            Font subtitleFont = workbook.createFont();

            subtitleFont.setBold(true);
            subtitleFont.setFontHeightInPoints((short) 13);
            subtitleStyle.setFont(subtitleFont);
            subtitleCell.setCellStyle(subtitleStyle);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            subtitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Row infoRow1 = sheet.createRow(2);
            CellStyle labelStyle = workbook.createCellStyle();
            Font labelFont = workbook.createFont();
            labelFont.setBold(true);
            labelStyle.setFont(labelFont);
            infoRow1.createCell(0).setCellValue("Generated On");
            infoRow1.createCell(1).setCellValue(
                    LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
                    )
            );

            infoRow1.getCell(0).setCellStyle(labelStyle);

            Row infoRow2 = sheet.createRow(3);

            infoRow2.createCell(0).setCellValue("Total Transactions");

            infoRow2.createCell(1).setCellValue(transactions.size());
            infoRow2.getCell(0).setCellStyle(labelStyle);

            Row headerRow = sheet.createRow(5);

            headerRow.createCell(0).setCellValue("Date");
            headerRow.createCell(1).setCellValue("Reference");
            headerRow.createCell(2).setCellValue("Type");
            headerRow.createCell(3).setCellValue("Amount");
            headerRow.createCell(4).setCellValue("Balance");
            headerRow.createCell(5).setCellValue("Description");
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(13, 99, 96), null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            for (Cell cell : headerRow) {
                cell.setCellStyle(headerStyle);
            }
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

            NumberFormat currencyFormat =
                    NumberFormat.getNumberInstance(Locale.of("en", "IN"));

            currencyFormat.setMinimumFractionDigits(2);
            currencyFormat.setMaximumFractionDigits(2);

            int rowNumber = 6;
            sheet.createFreezePane(0, 6);
            for (TransactionResponse tx : transactions) {

                Row row = sheet.createRow(rowNumber++);

                row.createCell(0).setCellValue(
                        tx.transactionDate().format(formatter));

                row.createCell(1).setCellValue(
                        tx.transactionId());

                row.createCell(2).setCellValue(
                        tx.transactionType().toString());

                row.createCell(3).setCellValue(
                        "Rs. " + currencyFormat.format(tx.amount()));

                row.createCell(4).setCellValue(
                        "Rs. " + currencyFormat.format(tx.availableBalance()));

                row.createCell(5).setCellValue(
                        tx.description() == null
                                ? "-"
                                : tx.description());
            }
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate Excel",
                    e
            );
        }
    }
}