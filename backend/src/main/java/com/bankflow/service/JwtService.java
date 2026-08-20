package com.bankflow.service;

import com.bankflow.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;


    /**
     * Generate JWT Token for user
     */
    public String generateToken(String email, String role) {
        log.info("Generating JWT token for user [{}] with role [{}]", email, role);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", role);

        String token = Jwts.builder()
                .claims(extraClaims)
                .subject(email)
                .id(UUID.randomUUID().toString())
                .issuer(jwtProperties.getIssuer())
                .audience()
                .add(jwtProperties.getAudience())
                .and()
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + jwtProperties.getExpiration().toMillis()
                        )
                )
                .signWith(getSigningKey())
                .compact();

        log.debug("JWT token generated successfully for user [{}]", email);
        return token;
    }

    /**
     * Extract username (email) from token
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    /**
     * Extract JWT ID from token
     */
    public String extractTokenId(String token) {
        return extractClaim(
                token,
                Claims::getId
        );
    }

    /**
     * Validate token against email and check expiration
     */
    public boolean isTokenValid(String token, String email) {
        try {
            final String tokenEmail = extractEmail(token);
            boolean isValid = tokenEmail.equalsIgnoreCase(email) && !isTokenExpired(token);

            if (!isValid) {
                log.warn("JWT Token validation failed for user [{}]: Mismatched subject or expired token", email);
            }
            return isValid;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT Token validation failed for user [{}]: Token has expired", email);
            return false;
        } catch (SignatureException | MalformedJwtException ex) {
            log.warn("JWT Token validation failed for user [{}]: Invalid token signature or format", email);
            return false;
        } catch (Exception ex) {
            log.error("Unexpected error during JWT token validation for user [{}]", email, ex);
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer(jwtProperties.getIssuer())
                    .requireAudience(jwtProperties.getAudience())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            log.warn("JWT parsing error: Token is expired");
            throw ex;
        } catch (SignatureException | MalformedJwtException ex) {
            log.warn("JWT parsing error: Invalid signature or malformed token structure");
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to parse JWT claims", ex);
            throw ex;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
