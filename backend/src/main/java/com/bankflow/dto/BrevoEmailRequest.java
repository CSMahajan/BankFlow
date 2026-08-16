package com.bankflow.dto;

import java.util.List;

public record BrevoEmailRequest(
        BrevoSender sender,
        List<BrevoRecipient> to,
        String subject,
        String htmlContent
) {
}