package com.bankflow.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.clamav")
public record ClamAvProperties(
        String host,
        int port
) {
}