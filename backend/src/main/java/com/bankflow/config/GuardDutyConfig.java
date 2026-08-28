package com.bankflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.guardduty.GuardDutyClient;

@Configuration
public class GuardDutyConfig {

    @Value("${app.aws.region}")
    private String region;

    @Bean
    public GuardDutyClient guardDutyClient() {
        return GuardDutyClient.builder()
                .region(Region.of(region))
                .build();
    }
}