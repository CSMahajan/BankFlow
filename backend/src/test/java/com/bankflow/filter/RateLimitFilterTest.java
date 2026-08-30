package com.bankflow.filter;

import com.bankflow.config.RateLimitProperties;
import com.bankflow.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitService rateLimitService;
    private RateLimitProperties properties;
    private FilterChain filterChain;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private RateLimitFilter filter;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {

        rateLimitService = mock(RateLimitService.class);
        properties = mock(RateLimitProperties.class);
        filterChain = mock(FilterChain.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        responseWriter = new StringWriter();

        when(response.getWriter())
                .thenReturn(new PrintWriter(responseWriter));

        filter = new RateLimitFilter(
                rateLimitService,
                properties
        );
    }

    @Test
    void doFilter_shouldContinueWhenRateLimitingIsDisabled()
            throws ServletException, IOException {

        when(properties.isEnabled()).thenReturn(false);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(
                request,
                response
        );

        verifyNoInteractions(rateLimitService);
    }

    @Test
    void doFilter_shouldApplyLoginRateLimit()
            throws ServletException, IOException {

        RateLimitProperties.Limit limit =
                mock(RateLimitProperties.Limit.class);

        when(properties.isEnabled()).thenReturn(true);
        when(request.getRequestURI())
                .thenReturn("/api/v1/auth/login");
        when(request.getRemoteAddr())
                .thenReturn("127.0.0.1");

        when(properties.getLogin())
                .thenReturn(limit);

        when(limit.getLimit())
                .thenReturn(5);

        when(limit.getWindow())
                .thenReturn(Duration.ofMinutes(1));

        when(rateLimitService.isAllowed(
                "LOGIN:127.0.0.1",
                5,
                Duration.ofMinutes(1)
        )).thenReturn(true);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(rateLimitService).isAllowed(
                "LOGIN:127.0.0.1",
                5,
                Duration.ofMinutes(1)
        );

        verify(filterChain).doFilter(
                request,
                response
        );
    }

    @Test
    void doFilter_shouldApplyRegisterRateLimit()
            throws ServletException, IOException {

        RateLimitProperties.Limit limit =
                mock(RateLimitProperties.Limit.class);

        when(properties.isEnabled()).thenReturn(true);
        when(request.getRequestURI())
                .thenReturn("/api/v1/auth/register");
        when(request.getRemoteAddr())
                .thenReturn("127.0.0.1");

        when(properties.getRegister())
                .thenReturn(limit);

        when(limit.getLimit()).thenReturn(3);
        when(limit.getWindow())
                .thenReturn(Duration.ofMinutes(10));

        when(rateLimitService.isAllowed(
                "REGISTER:127.0.0.1",
                3,
                Duration.ofMinutes(10)
        )).thenReturn(true);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(rateLimitService).isAllowed(
                "REGISTER:127.0.0.1",
                3,
                Duration.ofMinutes(10)
        );

        verify(filterChain).doFilter(
                request,
                response
        );
    }

    @Test
    void doFilter_shouldApplyForgotPasswordRateLimit()
            throws ServletException, IOException {

        RateLimitProperties.Limit limit =
                mock(RateLimitProperties.Limit.class);

        when(properties.isEnabled()).thenReturn(true);
        when(request.getRequestURI())
                .thenReturn("/api/v1/auth/forgot-password");
        when(request.getRemoteAddr())
                .thenReturn("10.0.0.1");

        when(properties.getForgotPassword())
                .thenReturn(limit);

        when(limit.getLimit()).thenReturn(3);
        when(limit.getWindow())
                .thenReturn(Duration.ofMinutes(10));

        when(rateLimitService.isAllowed(
                "FORGOT_PASSWORD:10.0.0.1",
                3,
                Duration.ofMinutes(10)
        )).thenReturn(true);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(rateLimitService).isAllowed(
                "FORGOT_PASSWORD:10.0.0.1",
                3,
                Duration.ofMinutes(10)
        );

        verify(filterChain).doFilter(
                request,
                response
        );
    }

    @Test
    void doFilter_shouldApplyResendVerificationRateLimit()
            throws ServletException, IOException {

        RateLimitProperties.Limit limit =
                mock(RateLimitProperties.Limit.class);

        when(properties.isEnabled()).thenReturn(true);
        when(request.getRequestURI())
                .thenReturn("/api/v1/auth/resend-verification");
        when(request.getRemoteAddr())
                .thenReturn("10.0.0.2");

        when(properties.getResendVerification())
                .thenReturn(limit);

        when(limit.getLimit()).thenReturn(2);
        when(limit.getWindow())
                .thenReturn(Duration.ofMinutes(5));

        when(rateLimitService.isAllowed(
                "RESEND_VERIFICATION:10.0.0.2",
                2,
                Duration.ofMinutes(5)
        )).thenReturn(true);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(rateLimitService).isAllowed(
                "RESEND_VERIFICATION:10.0.0.2",
                2,
                Duration.ofMinutes(5)
        );

        verify(filterChain).doFilter(
                request,
                response
        );
    }

    @Test
    void doFilter_shouldContinueForUnknownEndpoint()
            throws ServletException, IOException {

        when(properties.isEnabled()).thenReturn(true);
        when(request.getRequestURI())
                .thenReturn("/api/v1/documents");

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verifyNoInteractions(rateLimitService);

        verify(filterChain).doFilter(
                request,
                response
        );
    }

    @Test
    void doFilter_shouldStopRequestWhenRateLimitExceeded()
            throws ServletException, IOException {

        RateLimitProperties.Limit limit =
                mock(RateLimitProperties.Limit.class);

        when(properties.isEnabled()).thenReturn(true);
        when(request.getRequestURI())
                .thenReturn("/api/v1/auth/login");
        when(request.getRemoteAddr())
                .thenReturn("127.0.0.1");

        when(properties.getLogin())
                .thenReturn(limit);

        when(limit.getLimit()).thenReturn(5);
        when(limit.getWindow())
                .thenReturn(Duration.ofMinutes(1));

        when(rateLimitService.isAllowed(
                "LOGIN:127.0.0.1",
                5,
                Duration.ofMinutes(1)
        )).thenReturn(false);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(rateLimitService).isAllowed(
                "LOGIN:127.0.0.1",
                5,
                Duration.ofMinutes(1)
        );

        verify(response).setStatus(
                HttpStatus.TOO_MANY_REQUESTS.value()
        );

        verify(response).setContentType(
                "application/json"
        );

        verify(response).setHeader(
                "Retry-After",
                "PT1M"
        );

        verify(response).getWriter();

        verify(filterChain, never())
                .doFilter(request, response);

        assertTrue(
                responseWriter.toString()
                        .contains("\"status\":429")
        );

        assertTrue(
                responseWriter.toString()
                        .contains("Too Many Requests")
        );
    }

    @Test
    void doFilter_shouldUseFirstForwardedIp()
            throws ServletException, IOException {

        RateLimitProperties.Limit limit =
                mock(RateLimitProperties.Limit.class);

        when(properties.isEnabled()).thenReturn(true);
        when(request.getRequestURI())
                .thenReturn("/api/v1/auth/login");

        when(request.getHeader("X-Forwarded-For"))
                .thenReturn("192.168.1.10, 10.0.0.1, 10.0.0.2");

        when(properties.getLogin())
                .thenReturn(limit);

        when(limit.getLimit()).thenReturn(5);
        when(limit.getWindow())
                .thenReturn(Duration.ofMinutes(1));

        when(rateLimitService.isAllowed(
                "LOGIN:192.168.1.10",
                5,
                Duration.ofMinutes(1)
        )).thenReturn(true);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(rateLimitService).isAllowed(
                "LOGIN:192.168.1.10",
                5,
                Duration.ofMinutes(1)
        );
    }

    @Test
    void doFilter_shouldUseRemoteAddressWhenForwardedHeaderIsMissing()
            throws ServletException, IOException {

        RateLimitProperties.Limit limit =
                mock(RateLimitProperties.Limit.class);

        when(properties.isEnabled()).thenReturn(true);
        when(request.getRequestURI())
                .thenReturn("/api/v1/auth/login");

        when(request.getHeader("X-Forwarded-For"))
                .thenReturn(null);

        when(request.getRemoteAddr())
                .thenReturn("192.168.1.50");

        when(properties.getLogin())
                .thenReturn(limit);

        when(limit.getLimit()).thenReturn(5);
        when(limit.getWindow())
                .thenReturn(Duration.ofMinutes(1));

        when(rateLimitService.isAllowed(
                "LOGIN:192.168.1.50",
                5,
                Duration.ofMinutes(1)
        )).thenReturn(true);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(rateLimitService).isAllowed(
                "LOGIN:192.168.1.50",
                5,
                Duration.ofMinutes(1)
        );
    }

    @Test
    void doFilter_shouldUseRemoteAddressWhenForwardedHeaderIsBlank()
            throws ServletException, IOException {

        RateLimitProperties.Limit limit =
                mock(RateLimitProperties.Limit.class);

        when(properties.isEnabled()).thenReturn(true);
        when(request.getRequestURI())
                .thenReturn("/api/v1/auth/login");

        when(request.getHeader("X-Forwarded-For"))
                .thenReturn("   ");

        when(request.getRemoteAddr())
                .thenReturn("192.168.1.50");

        when(properties.getLogin())
                .thenReturn(limit);

        when(limit.getLimit()).thenReturn(5);
        when(limit.getWindow())
                .thenReturn(Duration.ofMinutes(1));

        when(rateLimitService.isAllowed(
                "LOGIN:192.168.1.50",
                5,
                Duration.ofMinutes(1)
        )).thenReturn(true);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(rateLimitService).isAllowed(
                "LOGIN:192.168.1.50",
                5,
                Duration.ofMinutes(1)
        );
    }
}