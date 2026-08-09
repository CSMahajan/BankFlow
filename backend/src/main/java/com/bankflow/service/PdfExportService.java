package com.bankflow.service;

import com.bankflow.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    public byte[] generateTransactionPdf(List<TransactionResponse> transactions) {
        try {
            Document document = new Document(PageSize.A4.rotate());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph title = new Paragraph("BankFlow", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            Paragraph subtitle = new Paragraph("Transaction History", headingFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Generated On : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")), normalFont));
            document.add(new Paragraph("Total Transactions : " + transactions.size(), normalFont));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(6);
            table.setHeaderRows(1);
            table.setWidthPercentage(100);
            table.setSpacingBefore(15f);
            table.setWidths(new float[]{2.2f, // Date
                    3.2f, // Reference
                    1.5f, // Type
                    2.0f, // Amount
                    2.2f, // Balance
                    4.5f  // Description
            });
            table.addCell(createHeaderCell("Date"));
            table.addCell(createHeaderCell("Reference"));
            table.addCell(createHeaderCell("Type"));
            table.addCell(createHeaderCell("Amount"));
            table.addCell(createHeaderCell("Balance"));
            table.addCell(createHeaderCell("Description"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));
            numberFormat.setMinimumFractionDigits(2);
            numberFormat.setMaximumFractionDigits(2);
            Color alternateRow = new Color(245, 247, 250);

            for (int i = 0; i < transactions.size(); i++) {

                TransactionResponse tx = transactions.get(i);

                Color rowColor = i % 2 == 0 ? Color.WHITE : alternateRow;

                table.addCell(createCell(tx.transactionDate().format(formatter), Element.ALIGN_LEFT, rowColor));

                table.addCell(createCell(tx.transactionId(), Element.ALIGN_LEFT, rowColor));

                table.addCell(createCell(tx.transactionType().toString(), Element.ALIGN_CENTER, rowColor));

                table.addCell(createCell("Rs. " + numberFormat.format(tx.amount()), Element.ALIGN_RIGHT, rowColor));

                table.addCell(createCell("Rs. " + numberFormat.format(tx.availableBalance()), Element.ALIGN_RIGHT, rowColor));

                table.addCell(createCell(tx.description() == null ? "-" : tx.description(), Element.ALIGN_LEFT, rowColor));
            }
            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private PdfPCell createHeaderCell(String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(13, 99, 96));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell createCell(String text) {
        return createCell(text, Element.ALIGN_LEFT, Color.WHITE);
    }

    private PdfPCell createCell(String text, int alignment) {
        return createCell(text, alignment, Color.WHITE);
    }

    private PdfPCell createCell(String text, int alignment, Color backgroundColor) {

        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);

        PdfPCell cell = new PdfPCell(new Phrase(text, font));

        cell.setPadding(6);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(backgroundColor);

        return cell;
    }
}