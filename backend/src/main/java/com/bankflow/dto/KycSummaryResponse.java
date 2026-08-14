package com.bankflow.dto;

public record KycSummaryResponse(

        long totalDocuments,
        long pendingDocuments,
        long verifiedDocuments,
        long rejectedDocuments,
        long pendingCustomers
) {}