package com.bankflow.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;

    private Duration expiration;


    @PostConstruct
    public void validate() {

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret must not be empty"
            );
        }

        if (secret.getBytes().length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 256 bits (32 bytes)"
            );
        }

        if (expiration == null) {
            throw new IllegalStateException(
                    "JWT expiration must not be empty"
            );
        }
    }
}