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

@Service
@RequiredArgsConstructor
public class EmailService {

    private final RestClient brevoRestClient;

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
            String body) {

        BrevoEmailRequest request = new BrevoEmailRequest(
                new BrevoSender(fromEmail, "BankFlow"),
                List.of(new BrevoRecipient(to, recipientName)),
                subject,
                body
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

    public void sendVerificationEmail(User user, String token) {

        String verificationUrl = frontendUrl + "/verify-email?token=" + token;

        String subject = "Verify your BankFlow Account";

        String body = """
                Dear %s,
                
                Welcome to BankFlow!
                
                Thank you for creating your account.
                
                To activate your account, please verify your email address by clicking the link below:
                
                %s
                
                This verification link is valid for 24 hours.
                
                If you did not create a BankFlow account, please ignore this email.
                
                Regards,
                BankFlow Team
                """.formatted(user.getFullName(), verificationUrl);

        sendEmail(user.getEmail(), user.getFullName(), subject, body);
    }

    public void sendPasswordResetEmail(User user, String token) {

        String resetLink = frontendUrl + "/reset-password?token=" + token;

        String subject = "Reset your BankFlow password";

        String body = """
                Hello %s,
                
                We received a request to reset your BankFlow password.
                
                Click the link below to create a new password:
                
                %s
                
                This link expires in 1 hour.
                
                If you didn't request this, please ignore this email.
                
                Regards,
                BankFlow Team
                """.formatted(user.getFullName(), resetLink);

        sendEmail(user.getEmail(), user.getFullName(), subject, body);
    }

    public void sendKycApprovedEmail(User user, String documentType) {

        String subject = "BankFlow KYC Document Approved";

        String body = """
                Dear %s,
                
                Good news!
                
                Your %s document has been verified successfully.
                
                Your KYC verification process is progressing.
                
                Regards,
                BankFlow Team
                """.formatted(user.getFullName(), documentType);

        sendEmail(user.getEmail(), user.getFullName(), subject, body);
    }

    public void sendKycRejectedEmail(
            User user,
            String documentType,
            String reason
    ) {

        String subject = "BankFlow KYC Document Rejected";

        String body = """
                Dear %s,
                
                Your %s document could not be verified.
                
                Reason:
                %s
                
                Please upload a new document from your BankFlow account.
                
                Regards,
                BankFlow Team
                """.formatted(user.getFullName(), documentType, reason);

        sendEmail(user.getEmail(), user.getFullName(), subject, body);
    }
}