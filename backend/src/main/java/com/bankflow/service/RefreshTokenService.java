package com.bankflow.service;

import com.bankflow.config.JwtProperties;
import com.bankflow.entity.RefreshToken;
import com.bankflow.entity.User;
import com.bankflow.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public String createRefreshToken(User user) {

        String refreshToken = generateToken();

        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .user(user)
                        .tokenHash(hashToken(refreshToken))
                        .expiryDate(
                                LocalDateTime.now()
                                        .plus(jwtProperties.getRefreshTokenExpiration())
                        )
                        .revoked(false)
                        .createdAt(LocalDateTime.now())
                        .build();


        refreshTokenRepository.save(refreshTokenEntity);


        return refreshToken;
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {

        String tokenHash = hashToken(token);

        RefreshToken refreshToken =
                refreshTokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid refresh token"
                                )
                        );


        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException(
                    "Refresh token has been revoked"
            );
        }


        if (refreshToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Refresh token expired"
            );
        }


        return refreshToken;
    }

    @Transactional
    public void revokeToken(String token) {

        RefreshToken refreshToken =
                validateRefreshToken(token);

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public String rotateRefreshToken(String oldToken, User user) {

        RefreshToken existingToken =
                validateRefreshToken(oldToken);

        existingToken.setRevoked(true);

        refreshTokenRepository.save(existingToken);


        return createRefreshToken(user);
    }

    private String generateToken() {

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        UUID.randomUUID()
                                .toString()
                                .getBytes(StandardCharsets.UTF_8)
                );
    }


    private String hashToken(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            return bytesToHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "Unable to hash refresh token",
                    e
            );
        }
    }


    private String bytesToHex(byte[] bytes) {

        StringBuilder result = new StringBuilder();

        for (byte b : bytes) {
            result.append(
                    String.format("%02x", b)
            );
        }

        return result.toString();
    }
}