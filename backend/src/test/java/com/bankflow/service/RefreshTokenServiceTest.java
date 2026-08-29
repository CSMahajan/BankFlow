package com.bankflow.service;

import com.bankflow.config.JwtProperties;
import com.bankflow.entity.RefreshToken;
import com.bankflow.entity.User;
import com.bankflow.exception.InvalidRefreshTokenException;
import com.bankflow.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .role(User.Role.CUSTOMER)
                .emailVerified(true)
                .build();
    }

    // -------------------------------------------------------------------------
    // createRefreshToken()
    // -------------------------------------------------------------------------

    @Test
    void createRefreshToken_shouldCreateAndSaveRefreshToken() {

        Duration expiration = Duration.ofDays(7);

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(expiration);

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String result = refreshTokenService.createRefreshToken(user);

        assertNotNull(result);
        assertFalse(result.isBlank());

        ArgumentCaptor<RefreshToken> captor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken savedToken = captor.getValue();

        assertEquals(user, savedToken.getUser());
        assertFalse(savedToken.isRevoked());

        assertNotNull(savedToken.getTokenHash());
        assertFalse(savedToken.getTokenHash().isBlank());

        assertNotNull(savedToken.getCreatedAt());
        assertNotNull(savedToken.getExpiryDate());

        assertTrue(
                savedToken.getExpiryDate()
                        .isAfter(savedToken.getCreatedAt())
        );
    }

    @Test
    void createRefreshToken_shouldStoreHashInsteadOfRawToken() {

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(Duration.ofDays(7));

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String rawToken =
                refreshTokenService.createRefreshToken(user);

        ArgumentCaptor<RefreshToken> captor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken savedToken = captor.getValue();

        assertNotEquals(
                rawToken,
                savedToken.getTokenHash()
        );

        // SHA-256 produces a 64-character hexadecimal hash.
        assertEquals(64, savedToken.getTokenHash().length());
    }

    @Test
    void createRefreshToken_shouldUseConfiguredExpiration() {

        Duration expiration = Duration.ofHours(12);

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(expiration);

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        refreshTokenService.createRefreshToken(user);

        ArgumentCaptor<RefreshToken> captor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken savedToken = captor.getValue();

        long secondsBetween =
                Duration.between(
                        savedToken.getCreatedAt(),
                        savedToken.getExpiryDate()
                ).getSeconds();

        assertTrue(
                Math.abs(expiration.getSeconds() - secondsBetween) <= 1
        );
    }
    // -------------------------------------------------------------------------
    // validateRefreshToken()
    // -------------------------------------------------------------------------

    @Test
    void validateRefreshToken_shouldReturnValidToken() {

        String rawToken = "valid-refresh-token";

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .tokenHash(hashForTest(rawToken))
                .createdAt(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(hashForTest(rawToken)))
                .thenReturn(Optional.of(refreshToken));

        RefreshToken result =
                refreshTokenService.validateRefreshToken(rawToken);

        assertSame(refreshToken, result);

        verify(refreshTokenRepository)
                .findByTokenHash(hashForTest(rawToken));
    }

    @Test
    void validateRefreshToken_shouldThrowExceptionWhenTokenDoesNotExist() {

        String rawToken = "invalid-token";

        when(refreshTokenRepository.findByTokenHash(hashForTest(rawToken)))
                .thenReturn(Optional.empty());

        InvalidRefreshTokenException exception =
                assertThrows(
                        InvalidRefreshTokenException.class,
                        () -> refreshTokenService.validateRefreshToken(rawToken)
                );

        assertEquals(
                "Invalid refresh token",
                exception.getMessage()
        );
    }

    @Test
    void validateRefreshToken_shouldThrowExceptionWhenTokenIsRevoked() {

        String rawToken = "revoked-token";

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .tokenHash(hashForTest(rawToken))
                .createdAt(LocalDateTime.now().minusDays(1))
                .expiryDate(LocalDateTime.now().plusDays(6))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByTokenHash(hashForTest(rawToken)))
                .thenReturn(Optional.of(refreshToken));

        InvalidRefreshTokenException exception =
                assertThrows(
                        InvalidRefreshTokenException.class,
                        () -> refreshTokenService.validateRefreshToken(rawToken)
                );

        assertEquals(
                "Refresh token has been revoked",
                exception.getMessage()
        );
    }

    @Test
    void validateRefreshToken_shouldThrowExceptionWhenTokenExpired() {

        String rawToken = "expired-token";

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .tokenHash(hashForTest(rawToken))
                .createdAt(LocalDateTime.now().minusDays(8))
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(hashForTest(rawToken)))
                .thenReturn(Optional.of(refreshToken));

        InvalidRefreshTokenException exception =
                assertThrows(
                        InvalidRefreshTokenException.class,
                        () -> refreshTokenService.validateRefreshToken(rawToken)
                );

        assertEquals(
                "Refresh token expired",
                exception.getMessage()
        );
    }

    // -------------------------------------------------------------------------
    // revokeToken()
    // -------------------------------------------------------------------------

    @Test
    void revokeToken_shouldMarkTokenAsRevokedAndSaveIt() {

        String rawToken = "valid-token";

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .tokenHash(hashForTest(rawToken))
                .createdAt(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(hashForTest(rawToken)))
                .thenReturn(Optional.of(refreshToken));

        refreshTokenService.revokeToken(rawToken);

        assertTrue(refreshToken.isRevoked());

        verify(refreshTokenRepository)
                .save(refreshToken);
    }

    @Test
    void revokeToken_shouldNotSaveWhenTokenIsInvalid() {

        String rawToken = "invalid-token";

        when(refreshTokenRepository.findByTokenHash(hashForTest(rawToken)))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> refreshTokenService.revokeToken(rawToken)
        );

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }

    // -------------------------------------------------------------------------
    // rotateRefreshToken()
    // -------------------------------------------------------------------------

    @Test
    void rotateRefreshToken_shouldRevokeOldTokenAndCreateNewToken() {

        String oldToken = "old-refresh-token";

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(Duration.ofDays(7));

        RefreshToken existingToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .tokenHash(hashForTest(oldToken))
                .createdAt(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(hashForTest(oldToken)))
                .thenReturn(Optional.of(existingToken));

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String newToken =
                refreshTokenService.rotateRefreshToken(oldToken, user);

        assertNotNull(newToken);
        assertNotEquals(oldToken, newToken);

        assertTrue(existingToken.isRevoked());

        verify(refreshTokenRepository)
                .save(existingToken);

        // The second save is for the newly-created refresh token.
        verify(refreshTokenRepository, times(2))
                .save(any(RefreshToken.class));
    }

    @Test
    void rotateRefreshToken_shouldFailWhenOldTokenIsInvalid() {

        String oldToken = "invalid-old-token";

        when(refreshTokenRepository.findByTokenHash(hashForTest(oldToken)))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> refreshTokenService.rotateRefreshToken(oldToken, user)
        );

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }

    @Test
    void rotateRefreshToken_shouldFailWhenOldTokenIsAlreadyRevoked() {

        String oldToken = "revoked-old-token";

        RefreshToken existingToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .tokenHash(hashForTest(oldToken))
                .createdAt(LocalDateTime.now().minusDays(1))
                .expiryDate(LocalDateTime.now().plusDays(6))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByTokenHash(hashForTest(oldToken)))
                .thenReturn(Optional.of(existingToken));

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> refreshTokenService.rotateRefreshToken(oldToken, user)
        );

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }

    // -------------------------------------------------------------------------
    // Test helper
    // -------------------------------------------------------------------------

    private String hashForTest(String token) {

        try {
            byte[] hash =
                    java.security.MessageDigest
                            .getInstance("SHA-256")
                            .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();

            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }

            return result.toString();

        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}