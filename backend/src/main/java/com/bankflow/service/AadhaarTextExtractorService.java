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
            Pattern.compile("^\\s*(\\d{4}\\s+\\d{4}\\s+\\d{4})\\s*$");

    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("\\b[6-9]\\d{9}\\b");

    private static final Pattern FEMALE_PATTERN =
            Pattern.compile("\\bFEMALE\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern MALE_PATTERN =
            Pattern.compile("\\bMALE\\b", Pattern.CASE_INSENSITIVE);


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

        for (String line : lines) {
            Matcher matcher = AADHAAR_PATTERN.matcher(line);

            if (matcher.matches()) {
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

        if (line == null || line.isBlank()) {
            return false;
        }

        line = line.trim();

        if (line.matches(".*\\d+.*")) {
            return false;
        }

        if (line.matches("(?i)government of india")) {
            return false;
        }

        if (line.matches("(?i).*(aadhaar|unique identification|dob|date of birth|male|female).*")) {
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
        if (FEMALE_PATTERN.matcher(text).find()) {
            return "FEMALE";
        }

        if (MALE_PATTERN.matcher(text).find()) {
            return "MALE";
        }

        return null;
    }

    private String extractMobile(String text) {
        Matcher matcher = MOBILE_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }


    private String extractAddress(String text) {

        String[] lines = text.split("\\r?\\n");
        int mobileIndex = -1;

        // Find mobile number line - address ends before this
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.matches(".*\\b[6-9]\\d{9}\\b.*")) {
                mobileIndex = i;
                break;
            }
        }

        if (mobileIndex == -1) {
            return null;
        }

        String name = extractName(text);

        if (name == null) {
            return null;
        }

        int nameIndex = -1;
        // Find name line position
        for (int i = 0; i < mobileIndex; i++) {
            if (lines[i].trim().equalsIgnoreCase(name.trim())) {
                nameIndex = i;
                break;
            }
        }

        if (nameIndex == -1) {
            return null;
        }

        StringBuilder address = new StringBuilder();

        for (int i = nameIndex + 1; i < mobileIndex; i++) {
            String line = lines[i].trim();
            if (line.isBlank()) {
                continue;
            }
            if (line.matches("(?i).*(S/O|D/O|W/O|C/O).*")) {
                continue;
            }

            if (line.matches("(?i).*\\b(DOB|D\\.O\\.B|Date of Birth)\\b.*")) {
                continue;
            }

            if (line.matches("(?i)^(MALE|FEMALE)$")) {
                continue;
            }
            address.append(line).append(", ");
        }
        if (address.isEmpty()) {
            return null;
        }

        String result = address.substring(0, address.length() - 2);
        log.info("address: {}", result);
        return result;
    }
}