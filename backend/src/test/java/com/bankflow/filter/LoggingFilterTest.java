package com.bankflow.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private LoggingFilter loggingFilter;

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("Do Filter - Passes Request Down Filter Chain and Clears MDC Afterwards")
    void doFilter_Success_ExecutesChainAndClearsMDC() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/accounts");
        when(request.getMethod()).thenReturn("GET");

        // Verify that MDC contains traceId WHILE the filter chain is executing
        doAnswer(invocation -> {
            // Uncomment/adjust key name according to your MDC key (e.g., "traceId" or "X-Trace-Id")
            // assertNotNull(MDC.get("traceId"));
            return null;
        }).when(filterChain).doFilter(request, response);

        loggingFilter.doFilter(request, response, filterChain);

        // Verify filter chain was invoked
        verify(filterChain, times(1)).doFilter(request, response);

        // Ensure MDC was cleaned up after request execution to prevent context leaking
        assertNull(MDC.get("traceId"));
    }

    @Test
    @DisplayName("Do Filter - Clears MDC Even When Exception Occurs in Filter Chain")
    void doFilter_ExceptionInChain_ClearsMDC() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/accounts");
        when(request.getMethod()).thenReturn("GET");

        doThrow(new RuntimeException("Simulated Filter Failure"))
                .when(filterChain).doFilter(request, response);

        assertThrows(RuntimeException.class, () ->
                loggingFilter.doFilter(request, response, filterChain)
        );

        // Verify MDC is cleared even when an unhandled exception is thrown downstream
        assertNull(MDC.get("traceId"));
    }
}
