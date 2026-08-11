package com.bankflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BrevoClientConfig {

    @Bean
    public RestClient brevoRestClient(@Value("${app.brevo.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}