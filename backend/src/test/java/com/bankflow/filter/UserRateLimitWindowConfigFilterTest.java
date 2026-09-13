package com.bankflow.filter;

import com.bankflow.config.RateLimitProperties;
import com.bankflow.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class UserRateLimitWindowConfigFilterTest {

    private RateLimitService rateLimitService;
    private RateLimitProperties properties;
    private FilterChain filterChain;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private UserRateLimitFilter filter;

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

        filter = new UserRateLimitFilter(
                rateLimitService,
                properties
        );

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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

    @ParameterizedTest(name = "should skip rate limiting for {0}")
    @MethodSource("excludedPaths")
    void doFilter_shouldSkipExcludedPaths(String path)
            throws ServletException, IOException {

        when(properties.isEnabled()).thenReturn(true);
        when(request.getRequestURI()).thenReturn(path);

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

    private static Stream<String> excludedPaths() {
        return Stream.of(
                "/swagger-ui/index.html",
                "/swagger-ui/some-resource.js",
                "/v3/api-docs",
                "/v3/api-docs/swagger-config",
                "/actuator/health"
        );
    }
    @Test
    void doFilter_shouldContinueWhenAuthenticationIsNull()
            throws ServletException, IOException {

        when(properties.isEnabled()).thenReturn(true);

        when(request.getRequestURI())
                .thenReturn("/api/v1/documents");

        SecurityContextHolder.clearContext();

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
    void doFilter_shouldContinueWhenUserIsNotAuthenticated()
            throws ServletException, IOException {

        when(properties.isEnabled()).thenReturn(true);

        when(request.getRequestURI())
                .thenReturn("/api/v1/documents");

        var authentication = mock(
                org.springframework.security.core.Authentication.class
        );

        when(authentication.isAuthenticated())
                .thenReturn(false);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

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
    void doFilter_shouldContinueForAnonymousUser()
            throws ServletException, IOException {

        when(properties.isEnabled()).thenReturn(true);

        when(request.getRequestURI())
                .thenReturn("/api/v1/documents");

        var authentication = mock(
                org.springframework.security.core.Authentication.class
        );

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getPrincipal())
                .thenReturn("anonymousUser");

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

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
    void doFilter_shouldContinueWhenUserLimitIsNotConfigured()
            throws ServletException, IOException {

        when(properties.isEnabled()).thenReturn(true);

        when(request.getRequestURI())
                .thenReturn("/api/v1/documents");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "user@example.com",
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(properties.getUser())
                .thenReturn(null);

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
    void doFilter_shouldAllowAuthenticatedUserWhenWithinLimit()
            throws ServletException, IOException {

        RateLimitProperties.LimitWindowConfig limitWindowConfig =
                mock(RateLimitProperties.LimitWindowConfig.class);

        when(properties.isEnabled()).thenReturn(true);

        when(request.getRequestURI())
                .thenReturn("/api/v1/documents");

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "user@example.com",
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(properties.getUser())
                .thenReturn(limitWindowConfig);

        when(limitWindowConfig.getLimit())
                .thenReturn(20);

        when(limitWindowConfig.getWindow())
                .thenReturn(Duration.ofMinutes(1));

        when(rateLimitService.isAllowed(
                "USER:user@example.com:/api/v1/documents",
                20,
                Duration.ofMinutes(1)
        )).thenReturn(true);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(rateLimitService).isAllowed(
                "USER:user@example.com:/api/v1/documents",
                20,
                Duration.ofMinutes(1)
        );

        verify(filterChain).doFilter(
                request,
                response
        );

        verifyNoInteractions(response);
    }

    @Test
    void doFilter_shouldRejectAuthenticatedUserWhenLimitExceeded()
            throws ServletException, IOException {

        RateLimitProperties.LimitWindowConfig limitWindowConfig =
                mock(RateLimitProperties.LimitWindowConfig.class);

        when(properties.isEnabled()).thenReturn(true);

        when(request.getRequestURI())
                .thenReturn("/api/v1/documents");

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "user@example.com",
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(properties.getUser())
                .thenReturn(limitWindowConfig);

        when(limitWindowConfig.getLimit())
                .thenReturn(20);

        when(limitWindowConfig.getWindow())
                .thenReturn(Duration.ofMinutes(1));

        when(rateLimitService.isAllowed(
                "USER:user@example.com:/api/v1/documents",
                20,
                Duration.ofMinutes(1)
        )).thenReturn(false);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(rateLimitService).isAllowed(
                "USER:user@example.com:/api/v1/documents",
                20,
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
                        .contains("\"error\":\"Too Many Requests\"")
        );

        assertTrue(
                responseWriter.toString()
                        .contains(
                                "\"message\":\"User request limitWindowConfig exceeded. Please try again later.\""
                        )
        );
    }

    @Test
    void doFilter_shouldCreateRateLimitKeyUsingUsernameAndRequestPath()
            throws ServletException, IOException {

        RateLimitProperties.LimitWindowConfig limitWindowConfig =
                mock(RateLimitProperties.LimitWindowConfig.class);

        when(properties.isEnabled()).thenReturn(true);

        when(request.getRequestURI())
                .thenReturn("/api/v1/documents/123");

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "chaitanya@example.com",
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(properties.getUser())
                .thenReturn(limitWindowConfig);

        when(limitWindowConfig.getLimit()).thenReturn(10);

        when(limitWindowConfig.getWindow())
                .thenReturn(Duration.ofMinutes(5));

        when(rateLimitService.isAllowed(
                "USER:chaitanya@example.com:/api/v1/documents/123",
                10,
                Duration.ofMinutes(5)
        )).thenReturn(true);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(rateLimitService).isAllowed(
                "USER:chaitanya@example.com:/api/v1/documents/123",
                10,
                Duration.ofMinutes(5)
        );
    }
}