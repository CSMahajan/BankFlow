package com.bankflow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private boolean enabled;

    private Limit login;

    private Limit register;

    private Limit forgotPassword;

    private Limit resendVerification;

    private Limit kycUpload;

    private Limit user;

    @Getter
    @Setter
    public static class Limit {

        private int limit;

        private Duration window;

    }
}