package com.bankflow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceTest {

    @Mock
    private SpringTemplateEngine templateEngine;

    private EmailTemplateService service;

    @BeforeEach
    void setUp() {
        service = new EmailTemplateService(templateEngine);
    }

    @Test
    void render_shouldReturnRenderedTemplate() {

        String templateName = "verify-email.html";

        Map<String, Object> variables = Map.of(
                "name", "Rahul",
                "verificationUrl", "https://example.com/verify"
        );

        when(templateEngine.process(
                eq("emails/verify-email.html"),
                any(Context.class)
        )).thenReturn(
                "<html><body>Hello Rahul</body></html>"
        );

        String result = service.render(
                templateName,
                variables
        );

        assertEquals(
                "<html><body>Hello Rahul</body></html>",
                result
        );

        verify(templateEngine).process(
                eq("emails/verify-email.html"),
                any(Context.class)
        );
    }

    @Test
    void render_shouldPrefixTemplateNameWithEmailsDirectory() {

        when(templateEngine.process(
                eq("emails/welcome.html"),
                any(Context.class)
        )).thenReturn("Welcome");

        String result = service.render(
                "welcome.html",
                Map.of()
        );

        assertEquals("Welcome", result);

        verify(templateEngine).process(
                eq("emails/welcome.html"),
                any(Context.class)
        );
    }

    @Test
    void render_shouldPassVariablesToTemplateContext() {

        Map<String, Object> variables = Map.of(
                "name", "Rahul",
                "verificationUrl", "https://example.com/verify",
                "expiryMinutes", 15
        );

        when(templateEngine.process(
                eq("emails/verify-email.html"),
                any(Context.class)
        )).thenReturn("Rendered email");

        service.render(
                "verify-email.html",
                variables
        );

        verify(templateEngine).process(
                eq("emails/verify-email.html"),
                argThat(context ->
                        "Rahul".equals(context.getVariable("name"))
                                && "https://example.com/verify"
                                .equals(context.getVariable("verificationUrl"))
                                && Integer.valueOf(15)
                                .equals(context.getVariable("expiryMinutes"))
                )
        );
    }

    @Test
    void render_shouldSupportEmptyVariables() {

        when(templateEngine.process(
                eq("emails/test.html"),
                any(Context.class)
        )).thenReturn("Rendered email");

        String result = service.render(
                "test.html",
                Map.of()
        );

        assertEquals(
                "Rendered email",
                result
        );

        verify(templateEngine).process(
                eq("emails/test.html"),
                any(Context.class)
        );
    }

    @Test
    void render_shouldReturnTemplateEngineResult() {

        String expected =
                "<html><body>Verification successful</body></html>";

        when(templateEngine.process(
                eq("emails/test.html"),
                any(Context.class)
        )).thenReturn(expected);

        String result = service.render(
                "test.html",
                Map.of("name", "Rahul")
        );

        assertSame(
                expected,
                result
        );
    }

    @Test
    void render_shouldPropagateTemplateEngineException() {

        RuntimeException exception =
                new RuntimeException("Template processing failed");

        when(templateEngine.process(
                eq("emails/test.html"),
                any(Context.class)
        )).thenThrow(exception);

        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> service.render(
                                "test.html",
                                Map.of()
                        )
                );

        assertSame(
                exception,
                thrown
        );
    }
}