package com.bankflow.service;

import com.bankflow.dto.PanExtractedData;
import com.bankflow.exception.ExtractionValidationException;
import org.springframework.stereotype.Service;

@Service
public class PanExtractionValidator {


    public void validate(PanExtractedData data) {


        if (data.panNumber() == null
                || data.panNumber().isBlank()) {

            throw new ExtractionValidationException(
                    "PAN number could not be extracted"
            );
        }


        if (data.fullName() == null
                || data.fullName().isBlank()) {

            throw new ExtractionValidationException(
                    "PAN holder name could not be extracted"
            );
        }


        if (data.dateOfBirth() == null) {

            throw new ExtractionValidationException(
                    "PAN date of birth could not be extracted"
            );
        }
    }
}