package com.bankflow.dto;

public record LoanSummaryResponse(
        long totalPending,
        long personalLoans,
        long homeLoans,
        long vehicleLoans
) {
}