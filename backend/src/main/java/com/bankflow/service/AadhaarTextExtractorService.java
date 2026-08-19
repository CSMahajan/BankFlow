package com.bankflow.service;

import com.bankflow.dto.AadhaarExtractedData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AadhaarTextExtractorService {


    private static final Pattern AADHAAR_PATTERN =
            Pattern.compile("\\d{4}\\s?\\d{4}\\s?\\d{4}");


    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("\\b[6-9]\\d{9}\\b");


    public AadhaarExtractedData extract(String text) {

        return new AadhaarExtractedData(
                extractAadhaarNumber(text),
                extractName(text),
                extractDob(text),
                extractGender(text),
                extractAddress(text),
                extractMobile(text)
        );
    }


    private String extractAadhaarNumber(String text) {

        String[] lines = text.split("\\r?\\n");

        Pattern pattern =
                Pattern.compile(
                        "^\\s*(\\d{4}\\s+\\d{4}\\s+\\d{4})\\s*$"
                );

        for(String line : lines) {

            Matcher matcher = pattern.matcher(line);

            if(matcher.matches()) {

                return matcher.group(1)
                        .replaceAll("\\s", "");
            }
        }

        return null;
    }


    private String extractName(String text) {

        String[] lines = text.split("\\r?\\n");


        // Approach 1: Name before S/O, D/O, W/O
        for (int i = 1; i < lines.length; i++) {

            String currentLine = lines[i].trim();

            if (currentLine.matches(
                    "(?i).*(S/O|D/O|W/O).*"
            )) {

                String candidate = lines[i - 1].trim();

                if (isValidName(candidate)) {
                    return candidate;
                }
            }
        }


        // Approach 2: Name immediately before DOB
        for (int i = 0; i < lines.length; i++) {

            String currentLine = lines[i].trim();

            if (currentLine.matches(
                    "(?i).*(DOB|D.O.B|Date of Birth).*"
            )) {

                for (int j = i - 1; j >= 0; j--) {

                    String candidate = lines[j].trim();

                    if (isValidName(candidate)) {
                        return candidate;
                    }
                }
            }
        }

        return null;
    }

    private boolean isValidName(String line) {

        if(line.matches(".*\\d+.*")) {
            return false;
        }

        return line.matches("[A-Za-z ]{3,50}");
    }


    private LocalDate extractDob(String text) {

        Pattern dobPattern =
                Pattern.compile(
                        "(DOB|D.O.B|Date of Birth)[^0-9]*(\\d{2}/\\d{2}/\\d{4})",
                        Pattern.CASE_INSENSITIVE
                );


        Matcher matcher =
                dobPattern.matcher(text);


        if (matcher.find()) {

            return LocalDate.parse(
                    matcher.group(2),
                    DateTimeFormatter.ofPattern(
                            "dd/MM/yyyy"
                    )
            );
        }

        return null;
    }


    private String extractGender(String text) {

        if (text.contains("MALE")) {
            return "MALE";
        }

        if (text.contains("FEMALE")) {
            return "FEMALE";
        }

        return null;
    }


    private String extractMobile(String text) {

        Matcher matcher =
                MOBILE_PATTERN.matcher(text);

        return matcher.find()
                ? matcher.group()
                : null;
    }


    private String extractAddress(String text) {

        int index =
                text.indexOf("Address:");

        if (index == -1) {
            return null;
        }

        String address =
                text.substring(index + "Address:".length());

        int end =
                address.indexOf("Aadhaar is proof");

        if (end != -1) {
            address = address.substring(0, end);
        }

        return address.trim();
    }
}