package com.bankflow.service;

import com.bankflow.dto.AadhaarExtractedData;
import com.bankflow.exception.ExtractionValidationException;
import org.springframework.stereotype.Service;

@Service
public class AadhaarExtractionValidator {


    public void validate(AadhaarExtractedData data) {


        if (data.aadhaarNumber() == null
                || data.aadhaarNumber().isBlank()) {

            throw new ExtractionValidationException(
                    "Aadhaar number could not be extracted"
            );
        }


        if (data.fullName() == null
                || data.fullName().isBlank()) {

            throw new ExtractionValidationException(
                    "Aadhaar holder name could not be extracted"
            );
        }


        if (data.dateOfBirth() == null) {

            throw new ExtractionValidationException(
                    "Aadhaar DOB could not be extracted"
            );
        }
    }
}