package com.bankflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;

@Configuration
public class TextractConfig {

    @Value("${app.aws.region}")
    private String region;

    @Bean
    public TextractClient textractClient() {

        return TextractClient.builder()
                .region(Region.of(region))
                .build();
    }
}