    package com.bankflow.ai.mcp;

    import com.nimbusds.jose.jwk.JWKSet;
    import com.nimbusds.jose.jwk.RSAKey;
    import com.nimbusds.jose.proc.SecurityContext;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import com.nimbusds.jose.jwk.source.JWKSource;

    @Configuration
    public class OAuth2JwkConfig {

        @Value("${oauth2.jwk}")
        private String jwkJson;

        @Bean
        public JWKSource<SecurityContext> jwkSource() {

            try {
                RSAKey rsaKey = RSAKey.parse(jwkJson);

                JWKSet jwkSet = new JWKSet(rsaKey);

                return (selector, context) ->
                        selector.select(jwkSet);

            } catch (Exception e) {
                throw new IllegalStateException(
                        "Failed to load BankFlow OAuth2 JWK",
                        e
                );
            }
        }
    }