package com.bankflow.exception;

public class ExtractionValidationException
        extends RuntimeException {

    public ExtractionValidationException(String message) {
        super(message);
    }
}