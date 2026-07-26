package com.bankflow.dto;

import com.bankflow.entity.ScheduledTransfer.Frequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateScheduledTransferRequest(
        @NotBlank(message = "Source account number is required")
        String sourceAccountNumber,

        @NotBlank(message = "Recipient account number is required")
        String recipientAccountNumber,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Transfer amount must be at least 1.00")
        BigDecimal amount,

        String description,

        @NotNull(message = "Frequency is required")
        Frequency frequency, // DAILY, WEEKLY, MONTHLY

        @NotNull(message = "Start date is required")
        LocalDate startDate
) {}
