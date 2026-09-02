package com.bankflow.controller;

import com.bankflow.config.SecurityConfig;
import com.bankflow.dto.*;
import com.bankflow.exception.GlobalExceptionHandler;
import com.bankflow.filter.JwtAuthenticationFilter;
import com.bankflow.filter.RateLimitFilter;
import com.bankflow.filter.UserRateLimitFilter;
import com.bankflow.service.UserService;
import com.bankflow.service.VerificationTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = RateLimitFilter.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = UserRateLimitFilter.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = SecurityConfig.class
                )
        }
)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private VerificationTokenService verificationTokenService;

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                    )
                    .build();
        }
    }

    @Test
    void login_shouldReturnOk() throws Exception {

        LoginRequest request = new LoginRequest(
                "john@example.com",
                "Password123!"
        );

        AuthResponse response = new AuthResponse(
                "access-token",
                "refresh-token",
                "john@example.com",
                "CUSTOMER",
                "John Doe"
        );

        when(userService.login(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.fullName").value("John Doe"));

        verify(userService).login(request);
    }

    @Test
    void register_shouldReturnCreated() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "John Doe",
                "john@example.com",
                "Password123!"
        );

        doNothing().when(userService).registerCustomer(request);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(userService).registerCustomer(request);
    }

    @Test
    void refresh_shouldReturnOk() throws Exception {

        RefreshTokenRequest request = new RefreshTokenRequest(
                "refresh-token"
        );

        AuthResponse response = new AuthResponse(
                "new-access-token",
                "new-refresh-token",
                "john@example.com",
                "CUSTOMER",
                "John Doe"
        );

        when(userService.refreshToken(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.fullName").value("John Doe"));

        verify(userService).refreshToken(request);
    }

    @Test
    void logout_shouldReturnNoContent() throws Exception {

        LogoutRequest request = new LogoutRequest(
                "refresh-token"
        );

        doNothing().when(userService).logout(request);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).logout(request);
    }

    @Test
    void verifyEmail_shouldReturnSuccessMessage() throws Exception {

        String token = "verification-token";

        doNothing().when(verificationTokenService).verifyEmail(token);

        mockMvc.perform(get("/api/v1/auth/verify-email")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Email verified successfully."));

        verify(verificationTokenService).verifyEmail(token);
    }

    @Test
    void resendVerificationEmail_shouldReturnNoContent() throws Exception {

        ResendVerificationRequest request = new ResendVerificationRequest(
                "john@example.com"
        );

        doNothing().when(userService).resendVerificationEmail(request);

        mockMvc.perform(post("/api/v1/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).resendVerificationEmail(request);
    }

    @Test
    void forgotPassword_shouldReturnNoContent() throws Exception {

        ForgotPasswordRequest request = new ForgotPasswordRequest(
                "john@example.com"
        );

        doNothing().when(userService).forgotPassword(request);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).forgotPassword(request);
    }

    @Test
    void resetPassword_shouldReturnNoContent() throws Exception {

        ResetPasswordRequest request = new ResetPasswordRequest(
                "reset-token",
                "NewPassword123!",
                "NewPassword123!"
        );

        doNothing().when(userService).resetPassword(request);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).resetPassword(request);
    }

    @Test
    void login_shouldReturnBadRequestWhenValidationFails() throws Exception {

        LoginRequest request = new LoginRequest(
                "",
                ""
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void register_shouldReturnBadRequestWhenValidationFails() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "",
                "invalid-email",
                "short"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void resetPassword_shouldReturnBadRequestWhenValidationFails() throws Exception {

        ResetPasswordRequest request = new ResetPasswordRequest(
                "",
                "short",
                ""
        );

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }
}