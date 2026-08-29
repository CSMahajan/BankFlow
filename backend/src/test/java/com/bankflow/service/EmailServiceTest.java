package com.bankflow.service;

import com.bankflow.dto.BrevoEmailRequest;
import com.bankflow.dto.BrevoEmailResponse;
import com.bankflow.dto.BrevoRecipient;
import com.bankflow.dto.BrevoSender;
import com.bankflow.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RestClient brevoRestClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private EmailTemplateService emailTemplateService;

    private EmailService emailService;

    private User user;

    @BeforeEach
    void setUp() {

        emailService = new EmailService(
                brevoRestClient,
                emailTemplateService
        );

        ReflectionTestUtils.setField(
                emailService,
                "brevoApiKey",
                "test-api-key"
        );

        ReflectionTestUtils.setField(
                emailService,
                "fromEmail",
                "noreply@bankflow.com"
        );

        ReflectionTestUtils.setField(
                emailService,
                "frontendUrl",
                "https://bankflow.example.com"
        );

        user = new User();
        user.setId(10L);
        user.setFullName("John Doe");
        user.setEmail("john@example.com");

        when(brevoRestClient.post())
                .thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.uri("/v3/smtp/email"))
                .thenReturn(requestBodySpec);

        when(requestBodySpec.header(anyString(), anyString()))
                .thenReturn(requestBodySpec);

        when(requestBodySpec.body(any(BrevoEmailRequest.class)))
                .thenReturn(requestBodySpec);

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.body(BrevoEmailResponse.class))
                .thenReturn(null);
    }

    @Test
    void sendVerificationEmail_shouldRenderTemplateAndSendEmail() {

        String token = "verification-token";

        String renderedHtml = "<html>verification email</html>";

        when(emailTemplateService.render(
                eq("verify-email"),
                anyMap()
        )).thenReturn(renderedHtml);

        emailService.sendVerificationEmail(user, token);

        ArgumentCaptor<Map<String, Object>> variablesCaptor =
                ArgumentCaptor.forClass(Map.class);

        verify(emailTemplateService).render(
                eq("verify-email"),
                variablesCaptor.capture()
        );

        Map<String, Object> variables = variablesCaptor.getValue();

        assertEquals(
                "John Doe",
                variables.get("name")
        );

        assertEquals(
                "https://bankflow.example.com/verify-email?token=verification-token",
                variables.get("verificationUrl")
        );

        verifyBrevoEmail(
                "john@example.com",
                "John Doe",
                "Verify your BankFlow Account",
                renderedHtml
        );
    }

    @Test
    void sendPasswordResetEmail_shouldRenderTemplateAndSendEmail() {

        String token = "reset-token";

        String renderedHtml = "<html>password reset email</html>";

        when(emailTemplateService.render(
                eq("password-reset"),
                anyMap()
        )).thenReturn(renderedHtml);

        emailService.sendPasswordResetEmail(user, token);

        ArgumentCaptor<Map<String, Object>> variablesCaptor =
                ArgumentCaptor.forClass(Map.class);

        verify(emailTemplateService).render(
                eq("password-reset"),
                variablesCaptor.capture()
        );

        Map<String, Object> variables = variablesCaptor.getValue();

        assertEquals(
                "John Doe",
                variables.get("name")
        );

        assertEquals(
                "https://bankflow.example.com/reset-password?token=reset-token",
                variables.get("resetUrl")
        );

        verifyBrevoEmail(
                "john@example.com",
                "John Doe",
                "Reset your BankFlow password",
                renderedHtml
        );
    }

    @Test
    void sendKycApprovedEmail_shouldRenderTemplateAndSendEmail() {

        String documentType = "PAN";

        String renderedHtml = "<html>KYC approved email</html>";

        when(emailTemplateService.render(
                eq("kyc-approved"),
                anyMap()
        )).thenReturn(renderedHtml);

        emailService.sendKycApprovedEmail(
                user,
                documentType
        );

        ArgumentCaptor<Map<String, Object>> variablesCaptor =
                ArgumentCaptor.forClass(Map.class);

        verify(emailTemplateService).render(
                eq("kyc-approved"),
                variablesCaptor.capture()
        );

        Map<String, Object> variables = variablesCaptor.getValue();

        assertEquals(
                "John Doe",
                variables.get("name")
        );

        assertEquals(
                "PAN",
                variables.get("documentType")
        );

        assertEquals(
                "https://bankflow.example.com?open=dashboard",
                variables.get("dashboardUrl")
        );

        verifyBrevoEmail(
                "john@example.com",
                "John Doe",
                "BankFlow KYC Document Approved",
                renderedHtml
        );
    }

    @Test
    void sendKycRejectedEmail_shouldRenderTemplateAndSendEmail() {

        String documentType = "AADHAAR";
        String reason = "Document image is unclear";

        String renderedHtml = "<html>KYC rejected email</html>";

        when(emailTemplateService.render(
                eq("kyc-rejected"),
                anyMap()
        )).thenReturn(renderedHtml);

        emailService.sendKycRejectedEmail(
                user,
                documentType,
                reason
        );

        ArgumentCaptor<Map<String, Object>> variablesCaptor =
                ArgumentCaptor.forClass(Map.class);

        verify(emailTemplateService).render(
                eq("kyc-rejected"),
                variablesCaptor.capture()
        );

        Map<String, Object> variables = variablesCaptor.getValue();

        assertEquals(
                "John Doe",
                variables.get("name")
        );

        assertEquals(
                "AADHAAR",
                variables.get("documentType")
        );

        assertEquals(
                "Document image is unclear",
                variables.get("reason")
        );

        assertEquals(
                "https://bankflow.example.com?open=kyc",
                variables.get("uploadUrl")
        );

        verifyBrevoEmail(
                "john@example.com",
                "John Doe",
                "BankFlow KYC Document Rejected",
                renderedHtml
        );
    }

    @Test
    void sendVerificationEmail_shouldSendCorrectBrevoRequest() {

        String token = "abc123";
        String renderedHtml = "<html>verification</html>";

        when(emailTemplateService.render(
                eq("verify-email"),
                anyMap()
        )).thenReturn(renderedHtml);

        emailService.sendVerificationEmail(user, token);

        ArgumentCaptor<BrevoEmailRequest> requestCaptor =
                ArgumentCaptor.forClass(BrevoEmailRequest.class);

        verify(requestBodySpec)
                .body(requestCaptor.capture());

        BrevoEmailRequest request = requestCaptor.getValue();

        assertNotNull(request);

        assertEquals(
                "noreply@bankflow.com",
                request.sender().email()
        );

        assertEquals(
                "BankFlow",
                request.sender().name()
        );

        assertEquals(
                "Verify your BankFlow Account",
                request.subject()
        );

        assertEquals(
                renderedHtml,
                request.htmlContent()
        );

        assertEquals(
                1,
                request.to().size()
        );

        BrevoRecipient recipient = request.to().get(0);

        assertEquals(
                "john@example.com",
                recipient.email()
        );

        assertEquals(
                "John Doe",
                recipient.name()
        );
    }

    @Test
    void sendVerificationEmail_shouldSetBrevoHeaders() {

        when(emailTemplateService.render(
                eq("verify-email"),
                anyMap()
        )).thenReturn("<html>verification</html>");

        emailService.sendVerificationEmail(
                user,
                "token"
        );

        verify(requestBodySpec)
                .header("api-key", "test-api-key");

        verify(requestBodySpec)
                .header("accept", "application/json");
    }

    @Test
    void sendVerificationEmail_shouldSendRequestToCorrectBrevoEndpoint() {

        when(emailTemplateService.render(
                eq("verify-email"),
                anyMap()
        )).thenReturn("<html>verification</html>");

        emailService.sendVerificationEmail(
                user,
                "token"
        );

        verify(requestBodyUriSpec)
                .uri("/v3/smtp/email");
    }

    @Test
    void sendVerificationEmail_shouldHandleNullBrevoResponse() {

        when(emailTemplateService.render(
                eq("verify-email"),
                anyMap()
        )).thenReturn("<html>verification</html>");

        when(responseSpec.body(BrevoEmailResponse.class))
                .thenReturn(null);

        assertDoesNotThrow(() ->
                emailService.sendVerificationEmail(
                        user,
                        "token"
                )
        );
    }

    private void verifyBrevoEmail(
            String expectedEmail,
            String expectedName,
            String expectedSubject,
            String expectedHtml
    ) {

        ArgumentCaptor<BrevoEmailRequest> requestCaptor =
                ArgumentCaptor.forClass(BrevoEmailRequest.class);

        verify(requestBodySpec)
                .body(requestCaptor.capture());

        BrevoEmailRequest request =
                requestCaptor.getValue();

        assertEquals(
                "noreply@bankflow.com",
                request.sender().email()
        );

        assertEquals(
                "BankFlow",
                request.sender().name()
        );

        assertEquals(
                expectedSubject,
                request.subject()
        );

        assertEquals(
                expectedHtml,
                request.htmlContent()
        );

        assertEquals(
                1,
                request.to().size()
        );

        BrevoRecipient recipient =
                request.to().get(0);

        assertEquals(
                expectedEmail,
                recipient.email()
        );

        assertEquals(
                expectedName,
                recipient.name()
        );
    }
}