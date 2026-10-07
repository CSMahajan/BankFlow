package com.bankflow.ai.mcp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class OAuth2TokenConfig {

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {

        return context -> {

            if (!"access_token".equals(
                    context.getTokenType().getValue())) {
                return;
            }

            context.getPrincipal()
                    .getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority ->
                            authority.startsWith("ROLE_"))
                    .map(authority ->
                            authority.substring("ROLE_".length()))
                    .findFirst()
                    .ifPresent(role ->
                            context.getClaims()
                                    .claim("role", role)
                    );
        };
    }
}