package com.bankflow.service;

import com.bankflow.entity.AuditAction;
import com.bankflow.entity.TokenType;
import com.bankflow.entity.User;
import com.bankflow.entity.VerificationToken;
import com.bankflow.exception.EmailVerificationException;
import com.bankflow.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceTest {

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private VerificationTokenService verificationTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .role(User.Role.CUSTOMER)
                .emailVerified(false)
                .build();
    }

    // -------------------------------------------------------------------------
    // createVerificationToken()
    // -------------------------------------------------------------------------

    @Test
    void createVerificationToken_shouldDeleteExistingTokenAndCreateNewToken() {

        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VerificationToken result =
                verificationTokenService.createVerificationToken(user);

        assertNotNull(result);
        assertNotNull(result.getToken());

        assertEquals(user, result.getUser());
        assertEquals(TokenType.EMAIL_VERIFICATION, result.getTokenType());
        assertFalse(result.isUsed());

        assertNotNull(result.getExpiryDate());

        assertTrue(
                result.getExpiryDate().isAfter(
                        LocalDateTime.now().plusHours(23)
                )
        );

        verify(verificationTokenRepository)
                .deleteByUserAndTokenType(
                        user,
                        TokenType.EMAIL_VERIFICATION
                );

        verify(verificationTokenRepository).flush();

        verify(verificationTokenRepository)
                .save(any(VerificationToken.class));
    }

    @Test
    void createVerificationToken_shouldCreateTokenWithApproximately24HourExpiry() {

        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime before = LocalDateTime.now().plusHours(24);

        VerificationToken result =
                verificationTokenService.createVerificationToken(user);

        LocalDateTime after = LocalDateTime.now().plusHours(24);

        assertFalse(result.getExpiryDate().isBefore(before));
        assertFalse(result.getExpiryDate().isAfter(after));
    }

    // -------------------------------------------------------------------------
    // verifyEmail()
    // -------------------------------------------------------------------------

    @Test
    void verifyEmail_shouldVerifyUserAndMarkTokenAsUsed() {

        VerificationToken token = VerificationToken.builder()
                .id(1L)
                .token("verification-token")
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        when(verificationTokenRepository.findByToken("verification-token"))
                .thenReturn(Optional.of(token));

        verificationTokenService.verifyEmail("verification-token");

        assertTrue(user.isEmailVerified());
        assertTrue(token.isUsed());

        verify(auditLogService).log(
                user,
                AuditAction.EMAIL_VERIFIED,
                "Email address verified successfully"
        );

        verify(verificationTokenRepository).save(token);
    }

    @Test
    void verifyEmail_shouldThrowExceptionWhenTokenDoesNotExist() {

        when(verificationTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        EmailVerificationException exception = assertThrows(
                EmailVerificationException.class,
                () -> verificationTokenService.verifyEmail("invalid-token")
        );

        assertEquals(
                "Invalid verification link.",
                exception.getMessage()
        );

        verify(auditLogService, never())
                .log(any(User.class), any(AuditAction.class), anyString());

        verify(verificationTokenRepository, never())
                .save(any());
    }

    @Test
    void verifyEmail_shouldThrowExceptionWhenTokenAlreadyUsed() {

        VerificationToken token = VerificationToken.builder()
                .token("used-token")
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(true)
                .build();

        when(verificationTokenRepository.findByToken("used-token"))
                .thenReturn(Optional.of(token));

        EmailVerificationException exception = assertThrows(
                EmailVerificationException.class,
                () -> verificationTokenService.verifyEmail("used-token")
        );

        assertEquals(
                "This verification link has already been used.",
                exception.getMessage()
        );

        assertFalse(user.isEmailVerified());

        verify(auditLogService, never())
                .log(any(User.class), any(AuditAction.class), anyString());

        verify(verificationTokenRepository, never())
                .save(any());
    }

    @Test
    void verifyEmail_shouldThrowExceptionWhenTokenExpired() {

        VerificationToken token = VerificationToken.builder()
                .token("expired-token")
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .user(user)
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        when(verificationTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(token));

        EmailVerificationException exception = assertThrows(
                EmailVerificationException.class,
                () -> verificationTokenService.verifyEmail("expired-token")
        );

        assertEquals(
                "This verification link has expired.",
                exception.getMessage()
        );

        assertFalse(user.isEmailVerified());
        assertFalse(token.isUsed());

        verify(auditLogService, never())
                .log(any(User.class), any(AuditAction.class), anyString());

        verify(verificationTokenRepository, never())
                .save(any());
    }

    // -------------------------------------------------------------------------
    // createPasswordResetToken()
    // -------------------------------------------------------------------------

    @Test
    void createPasswordResetToken_shouldDeleteExistingResetTokenAndCreateNewToken() {

        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VerificationToken result =
                verificationTokenService.createPasswordResetToken(user);

        assertNotNull(result);
        assertNotNull(result.getToken());

        assertEquals(user, result.getUser());
        assertEquals(TokenType.PASSWORD_RESET, result.getTokenType());
        assertFalse(result.isUsed());

        assertNotNull(result.getExpiryDate());

        verify(verificationTokenRepository)
                .deleteByUserAndTokenType(
                        user,
                        TokenType.PASSWORD_RESET
                );

        verify(verificationTokenRepository)
                .save(any(VerificationToken.class));
    }

    @Test
    void createPasswordResetToken_shouldCreateTokenWithApproximatelyOneHourExpiry() {

        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime before = LocalDateTime.now().plusHours(1);

        VerificationToken result =
                verificationTokenService.createPasswordResetToken(user);

        LocalDateTime after = LocalDateTime.now().plusHours(1);

        assertFalse(result.getExpiryDate().isBefore(before));
        assertFalse(result.getExpiryDate().isAfter(after));
    }

    // -------------------------------------------------------------------------
    // validatePasswordResetToken()
    // -------------------------------------------------------------------------

    @Test
    void validatePasswordResetToken_shouldReturnValidToken() {

        VerificationToken token = VerificationToken.builder()
                .token("reset-token")
                .tokenType(TokenType.PASSWORD_RESET)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        when(verificationTokenRepository.findByTokenAndTokenType(
                "reset-token",
                TokenType.PASSWORD_RESET
        )).thenReturn(Optional.of(token));

        VerificationToken result =
                verificationTokenService.validatePasswordResetToken(
                        "reset-token"
                );

        assertSame(token, result);

        verify(verificationTokenRepository)
                .findByTokenAndTokenType(
                        "reset-token",
                        TokenType.PASSWORD_RESET
                );
    }

    @Test
    void validatePasswordResetToken_shouldThrowExceptionWhenTokenDoesNotExist() {

        when(verificationTokenRepository.findByTokenAndTokenType(
                "invalid-token",
                TokenType.PASSWORD_RESET
        )).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> verificationTokenService.validatePasswordResetToken(
                        "invalid-token"
                )
        );

        assertEquals(
                "Invalid password reset link.",
                exception.getMessage()
        );
    }

    @Test
    void validatePasswordResetToken_shouldThrowExceptionWhenTokenAlreadyUsed() {

        VerificationToken token = VerificationToken.builder()
                .token("used-reset-token")
                .tokenType(TokenType.PASSWORD_RESET)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(true)
                .build();

        when(verificationTokenRepository.findByTokenAndTokenType(
                "used-reset-token",
                TokenType.PASSWORD_RESET
        )).thenReturn(Optional.of(token));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> verificationTokenService.validatePasswordResetToken(
                        "used-reset-token"
                )
        );

        assertEquals(
                "This password reset link has already been used.",
                exception.getMessage()
        );
    }

    @Test
    void validatePasswordResetToken_shouldThrowExceptionWhenTokenExpired() {

        VerificationToken token = VerificationToken.builder()
                .token("expired-reset-token")
                .tokenType(TokenType.PASSWORD_RESET)
                .user(user)
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        when(verificationTokenRepository.findByTokenAndTokenType(
                "expired-reset-token",
                TokenType.PASSWORD_RESET
        )).thenReturn(Optional.of(token));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> verificationTokenService.validatePasswordResetToken(
                        "expired-reset-token"
                )
        );

        assertEquals(
                "This password reset link has expired.",
                exception.getMessage()
        );
    }
}