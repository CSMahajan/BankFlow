package com.bankflow.config;

import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@RequiredArgsConstructor
public class McpResourceServerConfig {

    @Value("${oauth2.jwk}")
    private String oauth2Jwk;

    @Value("${oauth2.issuer}")
    private String oauth2Issuer;

    @Bean
    @Order(2)
    public SecurityFilterChain mcpSecurityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder
    ) throws Exception {

        RequestMatcher mcpMatcher =
                new OrRequestMatcher(
                        new AntPathRequestMatcher("/mcp"),
                        new AntPathRequestMatcher("/mcp/**")
                );

        http
                .securityMatcher(mcpMatcher)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize ->
                        authorize
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.decoder(jwtDecoder)
                        )
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {

        try {
            RSAKey rsaKey = RSAKey.parse(
                    oauth2Jwk
            );

            NimbusJwtDecoder decoder =
                    NimbusJwtDecoder.withPublicKey(
                            rsaKey.toRSAPublicKey()
                    ).build();

            OAuth2TokenValidator<Jwt> validator =
                    JwtValidators.createDefaultWithIssuer(
                            oauth2Issuer
                    );

            decoder.setJwtValidator(validator);

            return decoder;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to create MCP OAuth2 JWT decoder",
                    e
            );
        }
    }
}