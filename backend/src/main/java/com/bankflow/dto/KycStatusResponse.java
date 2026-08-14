package com.bankflow.dto;

public record KycStatusResponse(

        String overallStatus,

        DocumentStatus pan,

        DocumentStatus aadhaar

) {

    public record DocumentStatus(

            boolean uploaded,

            String status,

            String rejectionReason

    ) {}
}