package com.bankflow.service;

import com.bankflow.dto.BrevoEmailRequest;
import com.bankflow.dto.BrevoEmailResponse;
import com.bankflow.dto.BrevoRecipient;
import com.bankflow.dto.BrevoSender;
import com.bankflow.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final RestClient brevoRestClient;

    private final EmailTemplateService emailTemplateService;

    @Value("${app.brevo.api-key}")
    private String brevoApiKey;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private void sendEmail(
            String to,
            String recipientName,
            String subject,
            String htmlBody) {

        BrevoEmailRequest request = new BrevoEmailRequest(
                new BrevoSender(fromEmail, "BankFlow"),
                List.of(new BrevoRecipient(to, recipientName)),
                subject,
                htmlBody
        );

        BrevoEmailResponse response = brevoRestClient.post()
                .uri("/v3/smtp/email")
                .header("api-key", brevoApiKey)
                .header("accept", "application/json")
                .body(request)
                .retrieve()
                .body(BrevoEmailResponse.class);

        if (response != null) {
            // We will use this message ID later when we add
            // email delivery tracking/webhooks.
        }
    }

    public void sendVerificationEmail(
            User user,
            String token
    ) {

        String verificationUrl =
                frontendUrl
                        + "/verify-email?token="
                        + token;


        String htmlBody =
                emailTemplateService.render(
                        "verify-email",
                        Map.of(
                                "name",
                                user.getFullName(),

                                "verificationUrl",
                                verificationUrl
                        )
                );


        sendEmail(
                user.getEmail(),
                user.getFullName(),
                "Verify your BankFlow Account",
                htmlBody
        );
    }

    public void sendPasswordResetEmail(User user, String token) {

        String resetLink =
                frontendUrl
                        + "/reset-password?token="
                        + token;


        String htmlBody =
                emailTemplateService.render(
                        "password-reset",
                        Map.of(
                                "name",
                                user.getFullName(),

                                "resetUrl",
                                resetLink
                        )
                );


        sendEmail(
                user.getEmail(),
                user.getFullName(),
                "Reset your BankFlow password",
                htmlBody
        );
    }

    public void sendKycApprovedEmail(
            User user,
            String documentType
    ) {

        String dashboardUrl =
                frontendUrl + "?open=dashboard";

        String htmlBody =
                emailTemplateService.render(
                        "kyc-approved",
                        Map.of(
                                "name",
                                user.getFullName(),

                                "documentType",
                                documentType,

                                "dashboardUrl",
                                dashboardUrl
                        )
                );


        sendEmail(
                user.getEmail(),
                user.getFullName(),
                "BankFlow KYC Document Approved",
                htmlBody
        );
    }

    public void sendKycRejectedEmail(
            User user,
            String documentType,
            String reason
    ) {

        String uploadUrl =
                frontendUrl + "?open=kyc";

        String htmlBody =
                emailTemplateService.render(
                        "kyc-rejected",
                        Map.of(
                                "name",
                                user.getFullName(),

                                "documentType",
                                documentType,

                                "reason",
                                reason,

                                "uploadUrl",
                                uploadUrl
                        )
                );


        sendEmail(
                user.getEmail(),
                user.getFullName(),
                "BankFlow KYC Document Rejected",
                htmlBody
        );
    }
}