package com.bankflow.service;

import com.bankflow.dto.PanExtractedData;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PanTextExtractorService {

    private static final Pattern PAN_PATTERN =
            Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]");

    private static final Pattern DOB_PATTERN =
            Pattern.compile("\\d{2}/\\d{2}/\\d{4}");

    public PanExtractedData extract(String text) {

        return new PanExtractedData(
                extractPan(text),
                extractName(text),
                extractFatherName(text),
                extractDob(text)
        );
    }

    private String extractPan(String text) {

        Matcher matcher = PAN_PATTERN.matcher(
                text.replace(" ", "")
        );

        return matcher.find()
                ? matcher.group()
                : null;
    }

    private String extractName(String text) {

        String[] lines = text.split("\\r?\\n");

        for (int i = 0; i < lines.length; i++) {

            if (lines[i].trim().equalsIgnoreCase("Name")
                    && i + 1 < lines.length) {

                return lines[i + 1].trim();
            }
        }

        return null;
    }

    private String extractFatherName(String text) {

        String[] lines = text.split("\\r?\\n");

        for (int i = 0; i < lines.length; i++) {

            if (lines[i].trim().equalsIgnoreCase("Father's Name")
                    && i + 1 < lines.length) {

                return lines[i + 1].trim();
            }
        }

        return null;
    }

    private LocalDate extractDob(String text) {

        Matcher matcher = DOB_PATTERN.matcher(text);

        if (matcher.find()) {

            return LocalDate.parse(
                    matcher.group(),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );
        }

        return null;
    }
}