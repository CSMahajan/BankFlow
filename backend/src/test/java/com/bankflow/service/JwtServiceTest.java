package com.bankflow.service;

import com.bankflow.config.JwtProperties;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private JwtProperties jwtProperties;

    private final String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {

        jwtProperties = new JwtProperties();

        jwtProperties.setSecret(testSecret);
        jwtProperties.setExpiration(Duration.ofHours(1));

        jwtService = new JwtService(jwtProperties);
    }

    // ==========================================
    // TOKEN GENERATION & EXTRACTION TESTS
    // ==========================================

    @Test
    @DisplayName("Generate Token and Extract Email Success")
    void generateToken_ExtractEmail_Success() {
        String email = "john.doe@example.com";
        String role = "CUSTOMER";

        String token = jwtService.generateToken(email, role);

        assertNotNull(token);
        assertFalse(token.isBlank());

        String extractedEmail = jwtService.extractEmail(token);
        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Generate Token and Extract Role Success")
    void generateToken_ExtractRole_Success() {
        String email = "admin@bankflow.com";
        String role = "ADMIN";

        String token = jwtService.generateToken(email, role);

        String extractedRole = jwtService.extractRole(token);
        assertEquals(role, extractedRole);
    }

    @Test
    @DisplayName("Generate Token and Extract JWT ID Success")
    void generateToken_ExtractTokenId_Success() {

        String token =
                jwtService.generateToken(
                        "user@example.com",
                        "CUSTOMER"
                );

        String tokenId =
                jwtService.extractTokenId(token);

        assertNotNull(tokenId);
        assertFalse(tokenId.isBlank());
    }

    // ==========================================
    // TOKEN VALIDATION TESTS
    // ==========================================

    @Test
    @DisplayName("Is Token Valid - Valid Token Returns True")
    void isTokenValid_ValidToken_ReturnsTrue() {
        String email = "user@example.com";
        String token = jwtService.generateToken(email, "CUSTOMER");

        boolean isValid = jwtService.isTokenValid(token, email);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Is Token Valid - Case Insensitive Email Match Success")
    void isTokenValid_CaseInsensitiveEmail_ReturnsTrue() {
        String email = "user@example.com";
        String token = jwtService.generateToken(email, "CUSTOMER");

        boolean isValid = jwtService.isTokenValid(token, "USER@EXAMPLE.COM");

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Is Token Valid - Mismatched Email Returns False")
    void isTokenValid_MismatchedEmail_ReturnsFalse() {
        String email = "user@example.com";
        String token = jwtService.generateToken(email, "CUSTOMER");

        boolean isValid = jwtService.isTokenValid(token, "otheruser@example.com");

        assertFalse(isValid);
    }

    // ==========================================
    // EXCEPTION & MALFORMED TOKEN TESTS
    // ==========================================

    @Test
    @DisplayName("Is Token Valid - Malformed Token Returns False")
    void isTokenValid_MalformedToken_ReturnsFalse() {
        String malformedToken = "invalid.jwt.token";

        boolean isValid = jwtService.isTokenValid(malformedToken, "user@example.com");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Extract Claim - Malformed Token Throws Exception")
    void extractClaim_MalformedToken_ThrowsException() {
        String malformedToken = "invalid.jwt.token";

        assertThrows(MalformedJwtException.class, () ->
                jwtService.extractEmail(malformedToken)
        );
    }

    @Test
    @DisplayName("Extract Claim - Signature Mismatch Throws Exception")
    void extractClaim_InvalidSignature_ThrowsException() {
        String email = "user@example.com";
        String token = jwtService.generateToken(email, "CUSTOMER");

        // Secret key must be >= 64 characters (512 bits) to meet JJWT security requirements
        String strongForeignSecret = "DifferentSecretKeyForTamperedTokenCheck12345678901234567890123456789012";

        JwtProperties foreignProperties = new JwtProperties();

        foreignProperties.setSecret(strongForeignSecret);
        foreignProperties.setExpiration(Duration.ofHours(1));

        JwtService foreignJwtService = new JwtService(foreignProperties);

        assertThrows(SignatureException.class, () ->
                foreignJwtService.extractEmail(token)
        );
    }

    @Test
    @DisplayName("Is Token Valid - Signature Mismatch Returns False")
    void isTokenValid_InvalidSignature_ReturnsFalse() {
        String email = "user@example.com";
        String token = jwtService.generateToken(email, "CUSTOMER");

        // Secret key must be >= 64 characters (512 bits) to meet JJWT security requirements
        String strongForeignSecret = "DifferentSecretKeyForTamperedTokenCheck12345678901234567890123456789012";

        JwtProperties foreignProperties = new JwtProperties();

        foreignProperties.setSecret(strongForeignSecret);
        foreignProperties.setExpiration(Duration.ofHours(1));

        JwtService foreignJwtService = new JwtService(foreignProperties);

        boolean isValid = foreignJwtService.isTokenValid(token, email);

        assertFalse(isValid);
    }

}
