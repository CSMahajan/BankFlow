package com.bankflow.service;

import com.bankflow.dto.*;
import com.bankflow.entity.User;
import com.bankflow.entity.VerificationToken;
import com.bankflow.exception.EmailVerificationException;
import com.bankflow.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @AfterEach
    void tearDown() {
        // Clear context after each test to avoid polluting other tests
        SecurityContextHolder.clearContext();
    }

    private User mockUser;
    private User mockAdminUser;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(
                userService,
                "emailVerificationEnabled",
                true
        );

        mockUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .password("encodedPassword123")
                .role(User.Role.CUSTOMER)
                .emailVerified(true)
                .build();

        mockAdminUser = User.builder()
                .id(99L)
                .fullName("Admin User")
                .email("admin@bankflow.com")
                .password("encodedAdminPass123")
                .role(User.Role.ADMIN)
                .emailVerified(true)
                .build();
    }

    private void mockAuthenticatedUser(User user) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }


    // ==========================================
    // REGISTER CUSTOMER TESTS
    // ==========================================

    @Test
    @DisplayName("Register Customer When Email Verification Disabled - Success")
    void registerCustomer_Success() {
        RegisterRequest request =
                new RegisterRequest("John Doe", "john@example.com", "Secret@123");

        VerificationToken verificationToken = VerificationToken.builder()
                .token("verification-token")
                .user(mockUser)
                .build();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(verificationTokenService.createVerificationToken(mockUser))
                .thenReturn(verificationToken);

        assertDoesNotThrow(() -> userService.registerCustomer(request));

        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(passwordEncoder, times(1)).encode("Secret@123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(verificationTokenService, times(1))
                .createVerificationToken(mockUser);
        verify(emailService, times(1))
                .sendVerificationEmail(mockUser, "verification-token");
    }

    @Test
    @DisplayName("Login - Unverified Email Throws EmailVerificationException")
    void login_UnverifiedEmail_ThrowsEmailVerificationException() {
        LoginRequest request =
                new LoginRequest("john@example.com", "Secret@123");

        mockUser.setEmailVerified(false);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(mockUser));

        when(passwordEncoder.matches(
                "Secret@123",
                "encodedPassword123"
        )).thenReturn(true);

        EmailVerificationException ex =
                assertThrows(
                        EmailVerificationException.class,
                        () -> userService.login(request)
                );

        assertEquals(
                "Please verify your email address before logging in.",
                ex.getMessage()
        );

        verify(jwtService, never())
                .generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Register Customer - Duplicate Email Throws IllegalArgumentException")
    void registerCustomer_DuplicateEmail_ThrowsException() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "Secret@123");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.registerCustomer(request)
        );

        assertEquals("Email is already registered", ex.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Update Customer Profile Success")
    void updateCustomerProfile_Success() {
        mockAuthenticatedUser(mockUser);
        UpdateProfileRequest request = new UpdateProfileRequest("John Updated Doe");

        userService.updateCustomerProfile(request);

        assertEquals("John Updated Doe", mockUser.getFullName());
        verify(userRepository, times(1)).save(mockUser);
    }

    // ==========================================
    // LOGIN TESTS
    // ==========================================

    @Test
    @DisplayName("Login - Success")
    void login_Success() {
        LoginRequest request = new LoginRequest("john@example.com", "Secret@123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("Secret@123", "encodedPassword123")).thenReturn(true);
        when(jwtService.generateToken("john@example.com", "CUSTOMER")).thenReturn("mock.jwt.token");
        when(refreshTokenService.createRefreshToken(mockUser)).thenReturn("mock.refresh.token");
        AuthResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("mock.refresh.token", response.refreshToken());
        assertEquals("mock.jwt.token", response.accessToken());
        assertEquals("john@example.com", response.email());
        assertEquals("CUSTOMER", response.role());

        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(passwordEncoder, times(1)).matches("Secret@123", "encodedPassword123");
        verify(jwtService, times(1)).generateToken("john@example.com", "CUSTOMER");
        verify(refreshTokenService, times(1)).createRefreshToken(mockUser);
    }

    @Test
    @DisplayName("Login - Account Not Found Throws BadCredentialsException")
    void login_AccountNotFound_ThrowsBadCredentialsException() {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "Secret@123");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () ->
                userService.login(request)
        );

        assertEquals("Invalid email or password", ex.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Login - Password Mismatch Throws BadCredentialsException")
    void login_PasswordMismatch_ThrowsBadCredentialsException() {
        LoginRequest request = new LoginRequest("john@example.com", "WrongPassword");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("WrongPassword", "encodedPassword123")).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () ->
                userService.login(request)
        );

        assertEquals("Invalid email or password", ex.getMessage());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    // ==========================================
    // CREATE ADMIN ACCOUNT TESTS
    // ==========================================

    @Test
    @DisplayName("Create Admin Account - Success")
    void createAdminAccount_Success() {
        CreateAdminRequest request = new CreateAdminRequest("Admin User", "admin@bankflow.com", "AdminSecret@123");

        when(userRepository.existsByEmail("admin@bankflow.com")).thenReturn(false);
        when(passwordEncoder.encode("AdminSecret@123")).thenReturn("encodedAdminPass123");
        when(userRepository.save(any(User.class))).thenReturn(mockAdminUser);

        assertDoesNotThrow(() -> userService.createAdminAccount(request));

        verify(userRepository, times(1)).existsByEmail("admin@bankflow.com");
        verify(passwordEncoder, times(1)).encode("AdminSecret@123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Create Admin Account - Duplicate Email Throws IllegalArgumentException")
    void createAdminAccount_DuplicateEmail_ThrowsException() {
        CreateAdminRequest request = new CreateAdminRequest("Admin User", "admin@bankflow.com", "AdminSecret@123");

        when(userRepository.existsByEmail("admin@bankflow.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.createAdminAccount(request)
        );

        assertEquals("Email is already registered", ex.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
