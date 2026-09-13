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

    private static final String LINE_SEPARATOR_REGEX = "\\r?\\n";

    private static final Pattern AADHAAR_PATTERN =
            Pattern.compile("^\\s*(\\d{4}\\s+\\d{4}\\s+\\d{4})\\s*$");

    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("\\b[6-9]\\d{9}\\b");

    private static final Pattern FEMALE_PATTERN =
            Pattern.compile("\\bFEMALE\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern MALE_PATTERN =
            Pattern.compile("\\bMALE\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern DEPENDENCY_PATTERN =
            Pattern.compile("(?:S/O|D/O|W/O)", Pattern.CASE_INSENSITIVE);

    private static final Pattern DOB_LABEL_PATTERN =
            Pattern.compile(
                    "(?:DOB|D\\.O\\.B|Date of Birth)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern DOB_WITH_DATE_PATTERN =
            Pattern.compile(
                    "(?:DOB|D\\.O\\.B|Date of Birth)\\D*(\\d{2}/\\d{2}/\\d{4})",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern NUMERIC_PATTERN =
            Pattern.compile("\\d+");

    private static final Pattern GOVERNMENT_OF_INDIA_PATTERN =
            Pattern.compile("government of india", Pattern.CASE_INSENSITIVE);

    private static final Pattern NAME_EXCLUDED_TERMS_PATTERN =
            Pattern.compile(
                    "(?:aadhaar|unique identification|dob|date of birth|male|female)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern WHITESPACE_PATTERN =
            Pattern.compile("\\s");

    private static final Pattern VALID_NAME_PATTERN =
            Pattern.compile("[A-Za-z ]{3,50}");

    private static final DateTimeFormatter DOB_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
        String[] lines = text.split(LINE_SEPARATOR_REGEX);

        for (String line : lines) {
            Matcher matcher = AADHAAR_PATTERN.matcher(line);

            if (matcher.matches()) {
                return WHITESPACE_PATTERN.matcher(matcher.group(1)).replaceAll("");
            }
        }

        return null;
    }

    private String extractName(String text) {
        String[] lines = text.split(LINE_SEPARATOR_REGEX);

        String name = findNameBeforeDependency(lines);
        if (name != null) {
            return name;
        }

        return findNameBeforeDob(lines);
    }

    private String findNameBeforeDependency(String[] lines) {
        for (int i = 1; i < lines.length; i++) {
            String currentLine = lines[i].trim();

            if (DEPENDENCY_PATTERN.matcher(currentLine).find()) {
                String candidate = lines[i - 1].trim();

                if (isValidName(candidate)) {
                    return candidate;
                }
            }
        }

        return null;
    }

    private String findNameBeforeDob(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            String currentLine = lines[i].trim();

            if (DOB_LABEL_PATTERN.matcher(currentLine).find()) {
                String name = findPreviousValidName(lines, i);

                if (name != null) {
                    return name;
                }
            }
        }

        return null;
    }

    private String findPreviousValidName(String[] lines, int startIndex) {
        for (int i = startIndex - 1; i >= 0; i--) {
            String candidate = lines[i].trim();

            if (isValidName(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private boolean isValidName(String line) {
        if (line == null || line.isBlank()) {
            return false;
        }

        String candidate = line.trim();

        if (NUMERIC_PATTERN.matcher(candidate).find()) {
            return false;
        }

        if (GOVERNMENT_OF_INDIA_PATTERN.matcher(candidate).matches()) {
            return false;
        }

        if (NAME_EXCLUDED_TERMS_PATTERN.matcher(candidate).find()) {
            return false;
        }

        return VALID_NAME_PATTERN.matcher(candidate).matches();
    }

    private LocalDate extractDob(String text) {
        Matcher matcher = DOB_WITH_DATE_PATTERN.matcher(text);

        if (matcher.find()) {
            return LocalDate.parse(
                    matcher.group(1),
                    DOB_FORMATTER
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
        String[] lines = text.split(LINE_SEPARATOR_REGEX);
        int mobileIndex = findMobileIndex(lines);

        if (mobileIndex == -1) {
            return null;
        }

        String name = extractName(text);
        if (name == null) {
            return null;
        }

        int nameIndex = findNameIndex(lines, name, mobileIndex);
        if (nameIndex == -1) {
            return null;
        }

        String result = buildAddress(lines, nameIndex, mobileIndex);

        if (result == null) {
            return null;
        }

        log.info("address: {}", result);
        return result;
    }

    private int findMobileIndex(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            if (MOBILE_PATTERN.matcher(lines[i].trim()).find()) {
                return i;
            }
        }

        return -1;
    }

    private int findNameIndex(String[] lines, String name, int endIndex) {
        for (int i = 0; i < endIndex; i++) {
            if (lines[i].trim().equalsIgnoreCase(name.trim())) {
                return i;
            }
        }

        return -1;
    }

    private String buildAddress(
            String[] lines,
            int nameIndex,
            int mobileIndex
    ) {
        StringBuilder address = new StringBuilder();

        for (int i = nameIndex + 1; i < mobileIndex; i++) {
            appendAddressLine(address, lines[i].trim());
        }

        if (address.isEmpty()) {
            return null;
        }

        return address.substring(0, address.length() - 2);
    }

    private void appendAddressLine(
            StringBuilder address,
            String line
    ) {
        if (line.isBlank()
                || DEPENDENCY_PATTERN.matcher(line).find()
                || DOB_LABEL_PATTERN.matcher(line).find()
                || FEMALE_PATTERN.matcher(line).matches()
                || MALE_PATTERN.matcher(line).matches()) {
            return;
        }

        address.append(line).append(", ");
    }
}