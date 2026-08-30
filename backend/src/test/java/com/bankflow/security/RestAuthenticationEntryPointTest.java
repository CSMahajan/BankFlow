package com.bankflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;

class RestAuthenticationEntryPointTest {

    private RestAuthenticationEntryPoint entryPoint;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        entryPoint = new RestAuthenticationEntryPoint(
                new ObjectMapper()
        );

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void commence_shouldReturnUnauthorizedStatus() throws Exception {

        entryPoint.commence(
                request,
                response,
                new BadCredentialsException("Invalid credentials")
        );

        assertEquals(
                HttpServletResponse.SC_UNAUTHORIZED,
                response.getStatus()
        );
    }

    @Test
    void commence_shouldSetJsonContentType() throws Exception {

        entryPoint.commence(
                request,
                response,
                new BadCredentialsException("Invalid credentials")
        );

        assertEquals(
                "application/json",
                response.getContentType()
        );
    }

    @Test
    void commence_shouldReturnExpectedJsonResponse() throws Exception {

        entryPoint.commence(
                request,
                response,
                new BadCredentialsException("Invalid credentials")
        );

        String responseBody = response.getContentAsString();

        assertTrue(responseBody.contains("\"status\":401"));
        assertTrue(responseBody.contains("\"error\":\"Unauthorized\""));
        assertTrue(
                responseBody.contains(
                        "\"message\":\"Authentication required\""
                )
        );
    }

    @Test
    void commence_shouldReturnAllExpectedFields() throws Exception {

        entryPoint.commence(
                request,
                response,
                new BadCredentialsException("Authentication failed")
        );

        ObjectMapper objectMapper = new ObjectMapper();

        var json = objectMapper.readTree(
                response.getContentAsString()
        );

        assertEquals(401, json.get("status").asInt());
        assertEquals(
                "Unauthorized",
                json.get("error").asText()
        );
        assertEquals(
                "Authentication required",
                json.get("message").asText()
        );
    }

    @Test
    void commence_shouldNotExposeAuthenticationExceptionMessage()
            throws Exception {

        entryPoint.commence(
                request,
                response,
                new BadCredentialsException(
                        "Sensitive authentication failure"
                )
        );

        String responseBody = response.getContentAsString();

        assertFalse(
                responseBody.contains(
                        "Sensitive authentication failure"
                )
        );

        assertTrue(
                responseBody.contains(
                        "Authentication required"
                )
        );
    }
}