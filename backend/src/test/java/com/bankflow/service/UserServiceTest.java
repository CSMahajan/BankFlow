package com.bankflow.service;

import com.bankflow.dto.*;
import com.bankflow.entity.*;
import com.bankflow.exception.EmailVerificationException;
import com.bankflow.exception.ResourceNotFoundException;
import com.bankflow.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private FixedDepositRepository fixedDepositRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserService userService;

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(User user) {
        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName())
                .thenReturn(user.getEmail());

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
    }

    // ============================================================
    // REGISTER CUSTOMER
    // ============================================================

    @Test
    @DisplayName("Register Customer - Success With Email Verification")
    void registerCustomer_Success() {

        RegisterRequest request =
                new RegisterRequest(
                        "John Doe",
                        "john@example.com",
                        "Secret@123"
                );

        VerificationToken verificationToken =
                VerificationToken.builder()
                        .token("verification-token")
                        .user(mockUser)
                        .build();

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Secret@123"))
                .thenReturn("encodedPassword123");

        when(userRepository.save(any(User.class)))
                .thenReturn(mockUser);

        when(verificationTokenService.createVerificationToken(mockUser))
                .thenReturn(verificationToken);

        assertDoesNotThrow(
                () -> userService.registerCustomer(request)
        );

        verify(userRepository)
                .existsByEmail("john@example.com");

        verify(passwordEncoder)
                .encode("Secret@123");

        verify(userRepository)
                .save(any(User.class));

        verify(verificationTokenService)
                .createVerificationToken(mockUser);

        verify(emailService)
                .sendVerificationEmail(
                        mockUser,
                        "verification-token"
                );

        verify(auditLogService)
                .log(
                        mockUser,
                        AuditAction.USER_REGISTERED,
                        "Customer account registered"
                );
    }

    @Test
    @DisplayName("Register Customer - Verification Disabled")
    void registerCustomer_VerificationDisabled() {

        ReflectionTestUtils.setField(
                userService,
                "emailVerificationEnabled",
                false
        );

        RegisterRequest request =
                new RegisterRequest(
                        "John Doe",
                        "john@example.com",
                        "Secret@123"
                );

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Secret@123"))
                .thenReturn("encodedPassword123");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.registerCustomer(request);

        verify(userRepository).save(
                argThat(user ->
                        user.isEmailVerified()
                                && user.getRole() == User.Role.CUSTOMER
                                && user.getEmail().equals("john@example.com")
                                && user.getPassword().equals("encodedPassword123")
                )
        );

        verify(verificationTokenService, never())
                .createVerificationToken(any());

        verify(emailService, never())
                .sendVerificationEmail(any(), anyString());

        verify(auditLogService)
                .log(
                        any(User.class),
                        eq(AuditAction.USER_REGISTERED),
                        eq("Customer account registered")
                );
    }

    @Test
    @DisplayName("Register Customer - Duplicate Email Throws Exception")
    void registerCustomer_DuplicateEmail_ThrowsException() {

        RegisterRequest request =
                new RegisterRequest(
                        "John Doe",
                        "john@example.com",
                        "Secret@123"
                );

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.registerCustomer(request)
                );

        assertEquals(
                "Email is already registered",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ============================================================
    // LOGIN
    // ============================================================

    @Test
    @DisplayName("Login - Success")
    void login_Success() {

        LoginRequest request =
                new LoginRequest(
                        "john@example.com",
                        "Secret@123"
                );

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(mockUser));

        when(passwordEncoder.matches(
                "Secret@123",
                "encodedPassword123"
        )).thenReturn(true);

        when(jwtService.generateToken(
                "john@example.com",
                "CUSTOMER"
        )).thenReturn("mock.jwt.token");

        when(refreshTokenService.createRefreshToken(mockUser))
                .thenReturn("mock.refresh.token");

        AuthResponse response =
                userService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.accessToken());
        assertEquals("mock.refresh.token", response.refreshToken());
        assertEquals("john@example.com", response.email());
        assertEquals("CUSTOMER", response.role());
        assertEquals("John Doe", response.fullName());

        verify(jwtService)
                .generateToken("john@example.com", "CUSTOMER");

        verify(refreshTokenService)
                .createRefreshToken(mockUser);

        verify(auditLogService)
                .log(
                        mockUser,
                        AuditAction.LOGIN,
                        "User logged in successfully"
                );
    }

    @Test
    @DisplayName("Login - Account Not Found")
    void login_AccountNotFound_ThrowsBadCredentialsException() {

        LoginRequest request =
                new LoginRequest(
                        "unknown@example.com",
                        "Secret@123"
                );

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        BadCredentialsException exception =
                assertThrows(
                        BadCredentialsException.class,
                        () -> userService.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateToken(anyString(), anyString());

        verify(refreshTokenService, never())
                .createRefreshToken(any());
    }

    @Test
    @DisplayName("Login - Wrong Password")
    void login_WrongPassword_ThrowsBadCredentialsException() {

        LoginRequest request =
                new LoginRequest(
                        "john@example.com",
                        "WrongPassword"
                );

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(mockUser));

        when(passwordEncoder.matches(
                "WrongPassword",
                "encodedPassword123"
        )).thenReturn(false);

        BadCredentialsException exception =
                assertThrows(
                        BadCredentialsException.class,
                        () -> userService.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(jwtService, never())
                .generateToken(anyString(), anyString());

        verify(refreshTokenService, never())
                .createRefreshToken(any());
    }

    @Test
    @DisplayName("Login - Unverified Email")
    void login_UnverifiedEmail_ThrowsEmailVerificationException() {

        mockUser.setEmailVerified(false);

        LoginRequest request =
                new LoginRequest(
                        "john@example.com",
                        "Secret@123"
                );

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(mockUser));

        when(passwordEncoder.matches(
                "Secret@123",
                "encodedPassword123"
        )).thenReturn(true);

        EmailVerificationException exception =
                assertThrows(
                        EmailVerificationException.class,
                        () -> userService.login(request)
                );

        assertEquals(
                "Please verify your email address before logging in.",
                exception.getMessage()
        );

        verify(jwtService, never())
                .generateToken(anyString(), anyString());

        verify(refreshTokenService, never())
                .createRefreshToken(any());
    }

    @Test
    @DisplayName("Login - Unverified Email Allowed When Verification Disabled")
    void login_UnverifiedEmail_VerificationDisabled_Success() {

        ReflectionTestUtils.setField(
                userService,
                "emailVerificationEnabled",
                false
        );

        mockUser.setEmailVerified(false);

        LoginRequest request =
                new LoginRequest(
                        "john@example.com",
                        "Secret@123"
                );

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(mockUser));

        when(passwordEncoder.matches(
                "Secret@123",
                "encodedPassword123"
        )).thenReturn(true);

        when(jwtService.generateToken(
                "john@example.com",
                "CUSTOMER"
        )).thenReturn("jwt");

        when(refreshTokenService.createRefreshToken(mockUser))
                .thenReturn("refresh");

        AuthResponse response =
                userService.login(request);

        assertEquals("jwt", response.accessToken());
        assertEquals("refresh", response.refreshToken());
    }

    // ============================================================
    // REFRESH TOKEN
    // ============================================================

    @Test
    @DisplayName("Refresh Token - Success")
    void refreshToken_Success() {

        RefreshTokenRequest request =
                new RefreshTokenRequest("old-refresh-token");

        RefreshToken oldToken =
                RefreshToken.builder()
                        .user(mockUser)
                        .tokenHash("hash")
                        .revoked(false)
                        .expiryDate(LocalDateTime.now().plusHours(1))
                        .build();

        when(refreshTokenService.validateRefreshToken(
                "old-refresh-token"
        )).thenReturn(oldToken);

        when(jwtService.generateToken(
                "john@example.com",
                "CUSTOMER"
        )).thenReturn("new-access-token");

        when(refreshTokenService.rotateRefreshToken(
                "old-refresh-token",
                mockUser
        )).thenReturn("new-refresh-token");

        AuthResponse response =
                userService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
        assertEquals("new-refresh-token", response.refreshToken());
        assertEquals("john@example.com", response.email());
        assertEquals("CUSTOMER", response.role());
        assertEquals("John Doe", response.fullName());

        verify(refreshTokenService)
                .validateRefreshToken("old-refresh-token");

        verify(jwtService)
                .generateToken("john@example.com", "CUSTOMER");

        verify(refreshTokenService)
                .rotateRefreshToken(
                        "old-refresh-token",
                        mockUser
                );
    }

    @Test
    @DisplayName("Refresh Token - Invalid Token Propagates Exception")
    void refreshToken_InvalidToken_ThrowsException() {

        RefreshTokenRequest request =
                new RefreshTokenRequest("invalid-token");

        when(refreshTokenService.validateRefreshToken("invalid-token"))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        assertThrows(
                RuntimeException.class,
                () -> userService.refreshToken(request)
        );

        verify(jwtService, never())
                .generateToken(anyString(), anyString());

        verify(refreshTokenService, never())
                .rotateRefreshToken(anyString(), any());
    }

    // ============================================================
    // LOGOUT
    // ============================================================

    @Test
    @DisplayName("Logout - Success")
    void logout_Success() {

        LogoutRequest request =
                new LogoutRequest("refresh-token");

        userService.logout(request);

        verify(refreshTokenService)
                .revokeToken("refresh-token");
    }

    // ============================================================
    // CHANGE PASSWORD
    // ============================================================

    @Test
    @DisplayName("Change Password - Success")
    void changePassword_Success() {

        mockAuthenticatedUser(mockUser);

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        "OldPassword",
                        "NewPassword123",
                        "NewPassword123"
                );

        when(passwordEncoder.matches(
                "OldPassword",
                "encodedPassword123"
        )).thenReturn(true);

        when(passwordEncoder.matches(
                "NewPassword123",
                "encodedPassword123"
        )).thenReturn(false);

        when(passwordEncoder.encode("NewPassword123"))
                .thenReturn("newEncodedPassword");

        userService.changePassword(request);

        assertEquals(
                "newEncodedPassword",
                mockUser.getPassword()
        );

        verify(userRepository)
                .save(mockUser);

        verify(passwordEncoder)
                .encode("NewPassword123");

        verify(auditLogService)
                .log(
                        AuditAction.PASSWORD_CHANGED,
                        "Password changed successfully"
                );
    }

    @Test
    @DisplayName("Change Password - Current Password Incorrect")
    void changePassword_CurrentPasswordIncorrect() {

        mockAuthenticatedUser(mockUser);

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        "WrongPassword",
                        "NewPassword123",
                        "NewPassword123"
                );

        when(passwordEncoder.matches(
                "WrongPassword",
                "encodedPassword123"
        )).thenReturn(false);

        BadCredentialsException exception =
                assertThrows(
                        BadCredentialsException.class,
                        () -> userService.changePassword(request)
                );

        assertEquals(
                "Current password is incorrect.",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    @DisplayName("Change Password - Confirmation Does Not Match")
    void changePassword_PasswordMismatch() {

        mockAuthenticatedUser(mockUser);

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        "OldPassword",
                        "NewPassword123",
                        "DifferentPassword"
                );

        when(passwordEncoder.matches(
                "OldPassword",
                "encodedPassword123"
        )).thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.changePassword(request)
                );

        assertEquals(
                "Passwords do not match.",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    @DisplayName("Change Password - New Password Same As Current")
    void changePassword_SamePassword() {

        mockAuthenticatedUser(mockUser);

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        "OldPassword",
                        "OldPassword",
                        "OldPassword"
                );

        when(passwordEncoder.matches(
                "OldPassword",
                "encodedPassword123"
        )).thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.changePassword(request)
                );

        assertEquals(
                "New password must be different from the current password.",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ============================================================
    // CREATE ADMIN
    // ============================================================

    @Test
    @DisplayName("Create Admin Account - Success")
    void createAdminAccount_Success() {

        CreateAdminRequest request =
                new CreateAdminRequest(
                        "Admin User",
                        "admin@bankflow.com",
                        "AdminSecret@123"
                );

        when(userRepository.existsByEmail("admin@bankflow.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("AdminSecret@123"))
                .thenReturn("encodedAdminPass123");

        when(userRepository.save(any(User.class)))
                .thenReturn(mockAdminUser);

        userService.createAdminAccount(request);

        verify(userRepository)
                .existsByEmail("admin@bankflow.com");

        verify(passwordEncoder)
                .encode("AdminSecret@123");

        verify(userRepository)
                .save(argThat(user ->
                        user.getRole() == User.Role.ADMIN
                                && user.isEmailVerified()
                                && user.getEmail().equals("admin@bankflow.com")
                ));

        verify(auditLogService)
                .log(
                        mockAdminUser,
                        AuditAction.USER_REGISTERED,
                        "Administrator account created"
                );
    }

    @Test
    @DisplayName("Create Admin Account - Duplicate Email")
    void createAdminAccount_DuplicateEmail_ThrowsException() {

        CreateAdminRequest request =
                new CreateAdminRequest(
                        "Admin User",
                        "admin@bankflow.com",
                        "AdminSecret@123"
                );

        when(userRepository.existsByEmail("admin@bankflow.com"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.createAdminAccount(request)
                );

        assertEquals(
                "Email is already registered",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ============================================================
    // RESEND VERIFICATION EMAIL
    // ============================================================

    @Test
    @DisplayName("Resend Verification Email - Success")
    void resendVerificationEmail_Success() {

        mockUser.setEmailVerified(false);

        ResendVerificationRequest request =
                new ResendVerificationRequest(
                        "john@example.com"
                );

        VerificationToken token =
                VerificationToken.builder()
                        .token("new-verification-token")
                        .user(mockUser)
                        .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(mockUser));

        when(verificationTokenService.createVerificationToken(mockUser))
                .thenReturn(token);

        userService.resendVerificationEmail(request);

        verify(verificationTokenService)
                .createVerificationToken(mockUser);

        verify(emailService)
                .sendVerificationEmail(
                        mockUser,
                        "new-verification-token"
                );

        verify(auditLogService)
                .log(
                        mockUser,
                        AuditAction.VERIFICATION_EMAIL_RESENT,
                        "Verification email resent"
                );
    }

    @Test
    @DisplayName("Resend Verification Email - User Not Found")
    void resendVerificationEmail_UserNotFound() {

        ResendVerificationRequest request =
                new ResendVerificationRequest(
                        "unknown@example.com"
                );

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.resendVerificationEmail(request)
                );

        assertEquals(
                "No account found with this email address.",
                exception.getMessage()
        );

        verify(emailService, never())
                .sendVerificationEmail(any(), anyString());
    }

    @Test
    @DisplayName("Resend Verification Email - Verification Disabled")
    void resendVerificationEmail_VerificationDisabled() {

        ReflectionTestUtils.setField(
                userService,
                "emailVerificationEnabled",
                false
        );

        ResendVerificationRequest request =
                new ResendVerificationRequest(
                        "john@example.com"
                );

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(mockUser));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> userService.resendVerificationEmail(request)
                );

        assertEquals(
                "Email verification is disabled",
                exception.getMessage()
        );

        verify(verificationTokenService, never())
                .createVerificationToken(any());

        verify(emailService, never())
                .sendVerificationEmail(any(), anyString());
    }

    @Test
    @DisplayName("Resend Verification Email - Already Verified")
    void resendVerificationEmail_AlreadyVerified() {

        mockUser.setEmailVerified(true);

        ResendVerificationRequest request =
                new ResendVerificationRequest(
                        "john@example.com"
                );

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(mockUser));

        EmailVerificationException exception =
                assertThrows(
                        EmailVerificationException.class,
                        () -> userService.resendVerificationEmail(request)
                );

        assertEquals(
                "Your email address is already verified.",
                exception.getMessage()
        );

        verify(verificationTokenService, never())
                .createVerificationToken(any());

        verify(emailService, never())
                .sendVerificationEmail(any(), anyString());
    }

    // ============================================================
    // FORGOT PASSWORD
    // ============================================================

    @Test
    @DisplayName("Forgot Password - Existing User")
    void forgotPassword_ExistingUser() {

        ForgotPasswordRequest request =
                new ForgotPasswordRequest(
                        "john@example.com"
                );

        VerificationToken token =
                VerificationToken.builder()
                        .token("password-reset-token")
                        .user(mockUser)
                        .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(mockUser));

        when(verificationTokenService.createPasswordResetToken(mockUser))
                .thenReturn(token);

        userService.forgotPassword(request);

        verify(verificationTokenService)
                .createPasswordResetToken(mockUser);

        verify(emailService)
                .sendPasswordResetEmail(
                        mockUser,
                        "password-reset-token"
                );
    }

    @Test
    @DisplayName("Forgot Password - Non Existing User Does Nothing")
    void forgotPassword_NonExistingUser() {

        ForgotPasswordRequest request =
                new ForgotPasswordRequest(
                        "unknown@example.com"
                );

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(
                () -> userService.forgotPassword(request)
        );

        verify(verificationTokenService, never())
                .createPasswordResetToken(any());

        verify(emailService, never())
                .sendPasswordResetEmail(any(), anyString());
    }

    // ============================================================
    // RESET PASSWORD
    // ============================================================

    @Test
    @DisplayName("Reset Password - Success")
    void resetPassword_Success() {

        ResetPasswordRequest request =
                new ResetPasswordRequest(
                        "reset-token",
                        "NewPassword123",
                        "NewPassword123"
                );

        VerificationToken token =
                VerificationToken.builder()
                        .token("reset-token")
                        .user(mockUser)
                        .used(false)
                        .expiryDate(LocalDateTime.now().plusHours(1))
                        .build();

        when(verificationTokenService.validatePasswordResetToken(
                "reset-token"
        )).thenReturn(token);

        when(passwordEncoder.matches(
                "NewPassword123",
                "encodedPassword123"
        )).thenReturn(false);

        when(passwordEncoder.encode("NewPassword123"))
                .thenReturn("newEncodedPassword");

        userService.resetPassword(request);

        assertEquals(
                "newEncodedPassword",
                mockUser.getPassword()
        );

        assertTrue(mockUser.isEmailVerified());
        assertTrue(token.isUsed());

        verify(userRepository)
                .save(mockUser);
    }

    @Test
    @DisplayName("Reset Password - Password Confirmation Mismatch")
    void resetPassword_PasswordMismatch() {

        ResetPasswordRequest request =
                new ResetPasswordRequest(
                        "reset-token",
                        "NewPassword123",
                        "DifferentPassword"
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.resetPassword(request)
                );

        assertEquals(
                "Passwords do not match.",
                exception.getMessage()
        );

        verify(verificationTokenService, never())
                .validatePasswordResetToken(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    @DisplayName("Reset Password - Same Password")
    void resetPassword_SamePassword() {

        ResetPasswordRequest request =
                new ResetPasswordRequest(
                        "reset-token",
                        "OldPassword",
                        "OldPassword"
                );

        VerificationToken token =
                VerificationToken.builder()
                        .token("reset-token")
                        .user(mockUser)
                        .used(false)
                        .expiryDate(LocalDateTime.now().plusHours(1))
                        .build();

        when(verificationTokenService.validatePasswordResetToken(
                "reset-token"
        )).thenReturn(token);

        when(passwordEncoder.matches(
                "OldPassword",
                "encodedPassword123"
        )).thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.resetPassword(request)
                );

        assertEquals(
                "New password must be different from the current password.",
                exception.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));

        assertFalse(token.isUsed());
    }

    @Test
    @DisplayName("Reset Password - Invalid Token Propagates Exception")
    void resetPassword_InvalidToken() {

        ResetPasswordRequest request =
                new ResetPasswordRequest(
                        "invalid-token",
                        "NewPassword123",
                        "NewPassword123"
                );

        when(verificationTokenService.validatePasswordResetToken(
                "invalid-token"
        )).thenThrow(
                new IllegalArgumentException(
                        "Invalid password reset link."
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> userService.resetPassword(request)
                );

        assertEquals(
                "Invalid password reset link.",
                exception.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ============================================================
    // GET ALL USERS
    // ============================================================

    @Test
    @DisplayName("Get All Users - Returns Paginated User Summaries")
    void getAllUsers_Success() {

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 8, 1, 10, 0);

        User user =
                User.builder()
                        .id(1L)
                        .fullName("John Doe")
                        .email("john@example.com")
                        .password("encoded")
                        .role(User.Role.CUSTOMER)
                        .emailVerified(true)
                        .createdAt(createdAt)
                        .build();

        Page<User> page =
                new PageImpl<>(
                        List.of(user),
                        PageRequest.of(
                                0,
                                10,
                                Sort.by("createdAt").descending()
                        ),
                        1
                );

        when(userRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(accountRepository.countByUserId(1L))
                .thenReturn(2L);

        Page<UserSummaryResponse> result =
                userService.getAllUsers(
                        0,
                        10,
                        "john",
                        "CUSTOMER"
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        UserSummaryResponse response =
                result.getContent().get(0);

        assertEquals(1L, response.id());
        assertEquals("John Doe", response.fullName());
        assertEquals("john@example.com", response.email());
        assertEquals("CUSTOMER", response.role());
        assertEquals(createdAt, response.createdAt());
        assertEquals(2L, response.accountCount());

        verify(userRepository)
                .findAll(
                        any(org.springframework.data.jpa.domain.Specification.class),
                        any(Pageable.class)
                );

        verify(accountRepository)
                .countByUserId(1L);
    }

    @Test
    @DisplayName("Get All Users - ALL Role Does Not Convert To Enum")
    void getAllUsers_AllRole() {

        Page<User> page =
                new PageImpl<>(List.of());

        when(userRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        Page<UserSummaryResponse> result =
                userService.getAllUsers(
                        0,
                        10,
                        "",
                        "ALL"
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository)
                .findAll(
                        any(org.springframework.data.jpa.domain.Specification.class),
                        any(Pageable.class)
                );
    }

    // ============================================================
    // UPDATE PROFILE
    // ============================================================

    @Test
    @DisplayName("Update Customer Profile - Success")
    void updateCustomerProfile_Success() {

        mockAuthenticatedUser(mockUser);

        UpdateProfileRequest request =
                new UpdateProfileRequest(
                        "John Updated Doe"
                );

        userService.updateCustomerProfile(request);

        assertEquals(
                "John Updated Doe",
                mockUser.getFullName()
        );

        verify(userRepository)
                .save(mockUser);

        verify(auditLogService)
                .log(
                        AuditAction.PROFILE_UPDATED,
                        "Updated profile information"
                );
    }

    // ============================================================
    // GET CURRENT USER
    // ============================================================

    @Test
    @DisplayName("Get Current User - Success")
    void getCurrentUser_Success() {

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(mockUser));

        UserMeResponse response =
                userService.getCurrentUser(
                        "john@example.com"
                );

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("John Doe", response.fullName());
        assertEquals("john@example.com", response.email());
        assertEquals("CUSTOMER", response.role());
    }

    @Test
    @DisplayName("Get Current User - User Not Found")
    void getCurrentUser_UserNotFound() {

        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.getCurrentUser(
                                "missing@example.com"
                        )
                );

        assertEquals(
                "User not found for email: missing@example.com",
                exception.getMessage()
        );
    }

    // ============================================================
    // GET USER DETAILS
    // ============================================================

    @Test
    @DisplayName("Get User Details - Success")
    void getUserDetails_Success() {

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 8, 1, 10, 0);

        mockUser.setCreatedAt(createdAt);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser));

        when(accountRepository.countByUserId(1L))
                .thenReturn(2L);

        when(cardRepository.countByAccountUserId(1L))
                .thenReturn(3L);

        when(loanRepository.countByUserId(1L))
                .thenReturn(1L);

        when(fixedDepositRepository.countByUserId(1L))
                .thenReturn(2L);

        when(accountRepository.getTotalBalance(1L))
                .thenReturn(new BigDecimal("15000.00"));

        when(loanRepository.getOutstandingLoanAmount(1L))
                .thenReturn(new BigDecimal("5000.00"));

        UserDetailsResponse response =
                userService.getUserDetails(1L);

        assertNotNull(response);

        assertEquals(1L, response.id());
        assertEquals("John Doe", response.fullName());
        assertEquals("john@example.com", response.email());
        assertEquals("CUSTOMER", response.role());
        assertEquals(createdAt, response.createdAt());

        assertEquals(2L, response.accountCount());
        assertEquals(3L, response.cardCount());
        assertEquals(1L, response.loanCount());
        assertEquals(2L, response.fixedDepositCount());

        assertEquals(
                new BigDecimal("15000.00"),
                response.totalBalance()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                response.outstandingLoanAmount()
        );
    }

    @Test
    @DisplayName("Get User Details - User Not Found")
    void getUserDetails_UserNotFound() {

        when(userRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserDetails(100L)
        );

        verify(accountRepository, never())
                .countByUserId(anyLong());

        verify(cardRepository, never())
                .countByAccountUserId(anyLong());
    }

    // ============================================================
    // GET USER ACCOUNTS
    // ============================================================

    @Test
    @DisplayName("Get User Accounts - Success")
    void getUserAccounts_Success() {

        Account account = mock(Account.class);

        when(account.getAccountNumber())
                .thenReturn("ACC001");

        when(account.getAccountType())
                .thenReturn(Account.AccountType.SAVINGS);

        when(account.getCurrentBalance())
                .thenReturn(new BigDecimal("10000.00"));

        when(account.getAccountStatus())
                .thenReturn(Account.AccountStatus.ACTIVE);

        when(account.getBranchName())
                .thenReturn("Mumbai Branch");

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 8, 1, 10, 0);

        when(account.getCreatedAt())
                .thenReturn(createdAt);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser));

        when(accountRepository.findByUserId(1L))
                .thenReturn(List.of(account));

        List<AdminUserAccountResponse> result =
                userService.getUserAccounts(1L);

        assertEquals(1, result.size());

        AdminUserAccountResponse response =
                result.get(0);

        assertEquals("ACC001", response.accountNumber());
        assertEquals(
                Account.AccountType.SAVINGS,
                response.accountType()
        );
        assertEquals(
                new BigDecimal("10000.00"),
                response.currentBalance()
        );
        assertEquals(
                Account.AccountStatus.ACTIVE,
                response.accountStatus()
        );
        assertEquals(
                "Mumbai Branch",
                response.branchName()
        );
        assertEquals(createdAt, response.createdAt());
    }

    @Test
    @DisplayName("Get User Accounts - User Not Found")
    void getUserAccounts_UserNotFound() {

        when(userRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserAccounts(100L)
        );

        verify(accountRepository, never())
                .findByUserId(anyLong());
    }

    // ============================================================
    // GET USER CARDS
    // ============================================================

    @Test
    @DisplayName("Get User Cards - Success")
    void getUserCards_Success() {

        Card card = mock(Card.class);

        when(card.getCardNumber())
                .thenReturn("4111111111111111");

        when(card.getCardType())
                .thenReturn(Card.CardType.DEBIT);

        when(card.getCardStatus())
                .thenReturn(Card.CardStatus.ACTIVE);

        when(card.getDailyLimit())
                .thenReturn(new BigDecimal("50000.00"));

        LocalDate expiryDate =
                LocalDate.of(2030, 12, 31);

        when(card.getExpiryDate())
                .thenReturn(expiryDate);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser));

        when(cardRepository.findByAccountUserId(1L))
                .thenReturn(List.of(card));

        List<AdminUserCardResponse> result =
                userService.getUserCards(1L);

        assertEquals(1, result.size());

        AdminUserCardResponse response =
                result.get(0);

        assertEquals(
                "4111111111111111",
                response.cardNumber()
        );

        assertEquals(
                Card.CardType.DEBIT,
                response.cardType()
        );

        assertEquals(
                Card.CardStatus.ACTIVE,
                response.cardStatus()
        );

        assertEquals(
                new BigDecimal("50000.00"),
                response.dailyLimit()
        );

        assertEquals(
                expiryDate,
                response.expiryDate()
        );
    }

    @Test
    @DisplayName("Get User Cards - User Not Found")
    void getUserCards_UserNotFound() {

        when(userRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserCards(100L)
        );

        verify(cardRepository, never())
                .findByAccountUserId(anyLong());
    }

    // ============================================================
    // GET USER LOANS
    // ============================================================

    @Test
    @DisplayName("Get User Loans - Success")
    void getUserLoans_Success() {

        Loan loan = mock(Loan.class);

        when(loan.getLoanNumber())
                .thenReturn("LOAN001");

        when(loan.getLoanType())
                .thenReturn(Loan.LoanType.PERSONAL);

        when(loan.getStatus())
                .thenReturn(Loan.LoanStatus.ACTIVE);

        when(loan.getPrincipalAmount())
                .thenReturn(new BigDecimal("100000.00"));

        when(loan.getRemainingBalance())
                .thenReturn(new BigDecimal("75000.00"));

        when(loan.getMonthlyEmi())
                .thenReturn(new BigDecimal("5000.00"));

        when(loan.getTenureMonths())
                .thenReturn(24);

        LocalDate dueDate =
                LocalDate.of(2026, 9, 15);

        when(loan.getNextDueDate())
                .thenReturn(dueDate);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser));

        when(loanRepository.findByUserId(1L))
                .thenReturn(List.of(loan));

        List<AdminUserLoanResponse> result =
                userService.getUserLoans(1L);

        assertEquals(1, result.size());

        AdminUserLoanResponse response =
                result.get(0);

        assertEquals("LOAN001", response.loanNumber());

        assertEquals(
                Loan.LoanType.PERSONAL,
                response.loanType()
        );

        assertEquals(
                Loan.LoanStatus.ACTIVE,
                response.status()
        );

        assertEquals(
                new BigDecimal("100000.00"),
                response.principalAmount()
        );

        assertEquals(
                new BigDecimal("75000.00"),
                response.remainingBalance()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                response.monthlyEmi()
        );

        assertEquals(24, response.tenureMonths());
        assertEquals(dueDate, response.nextDueDate());
    }

    @Test
    @DisplayName("Get User Loans - User Not Found")
    void getUserLoans_UserNotFound() {

        when(userRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserLoans(100L)
        );

        verify(loanRepository, never())
                .findByUserId(anyLong());
    }

    // ============================================================
    // GET USER FIXED DEPOSITS
    // ============================================================

    @Test
    @DisplayName("Get User Fixed Deposits - Success")
    void getUserFixedDeposits_Success() {

        FixedDeposit fixedDeposit =
                mock(FixedDeposit.class);

        when(fixedDeposit.getFdNumber())
                .thenReturn("FD001");

        when(fixedDeposit.getDepositAmount())
                .thenReturn(new BigDecimal("50000.00"));

        when(fixedDeposit.getInterestRate())
                .thenReturn(new BigDecimal("7.50"));

        LocalDate maturityDate =
                LocalDate.of(2027, 8, 1);

        when(fixedDeposit.getMaturityDate())
                .thenReturn(maturityDate);

        when(fixedDeposit.getMaturityAmount())
                .thenReturn(new BigDecimal("53750.00"));

        when(fixedDeposit.getStatus())
                .thenReturn(FixedDeposit.FdStatus.ACTIVE);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser));

        when(fixedDepositRepository.findByUserId(1L))
                .thenReturn(List.of(fixedDeposit));

        List<AdminUserFixedDepositResponse> result =
                userService.getUserFixedDeposits(1L);

        assertEquals(1, result.size());

        AdminUserFixedDepositResponse response =
                result.get(0);

        assertEquals("FD001", response.fdNumber());

        assertEquals(
                new BigDecimal("50000.00"),
                response.principalAmount()
        );

        assertEquals(
                new BigDecimal("7.50"),
                response.interestRate()
        );

        assertEquals(
                maturityDate,
                response.maturityDate()
        );

        assertEquals(
                new BigDecimal("53750.00"),
                response.maturityAmount()
        );

        assertEquals(
                FixedDeposit.FdStatus.ACTIVE,
                response.status()
        );
    }

    @Test
    @DisplayName("Get User Fixed Deposits - User Not Found")
    void getUserFixedDeposits_UserNotFound() {

        when(userRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserFixedDeposits(100L)
        );

        verify(fixedDepositRepository, never())
                .findByUserId(anyLong());
    }
}