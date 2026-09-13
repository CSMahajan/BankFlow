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

    private LimitWindowConfig login;

    private LimitWindowConfig register;

    private LimitWindowConfig forgotPassword;

    private LimitWindowConfig resendVerification;

    private LimitWindowConfig kycUpload;

    private LimitWindowConfig user;

    @Getter
    @Setter
    public static class LimitWindowConfig {

        private int limit;

        private Duration window;

    }
}