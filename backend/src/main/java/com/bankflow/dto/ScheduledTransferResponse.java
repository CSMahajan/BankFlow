package com.bankflow.dto;

import com.bankflow.entity.ScheduledTransfer.Frequency;
import com.bankflow.entity.ScheduledTransfer.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ScheduledTransferResponse(
        Long id,
        String sourceAccountNumber,
        String recipientAccountNumber,
        BigDecimal amount,
        String description,
        Frequency frequency,
        TransferStatus status,
        LocalDate nextExecutionDate,
        LocalDateTime createdAt
) {}
