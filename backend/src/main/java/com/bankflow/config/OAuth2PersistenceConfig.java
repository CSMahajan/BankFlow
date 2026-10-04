package com.bankflow.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Configuration
public class OAuth2PersistenceConfig {

    @Bean
    public JdbcTemplate oauth2JdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(
            JdbcTemplate oauth2JdbcTemplate) {

        return new JdbcRegisteredClientRepository(
                oauth2JdbcTemplate
        );
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(
            JdbcTemplate oauth2JdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {

        return new JdbcOAuth2AuthorizationService(
                oauth2JdbcTemplate,
                registeredClientRepository
        );
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(
            JdbcTemplate oauth2JdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {

        return new JdbcOAuth2AuthorizationConsentService(
                oauth2JdbcTemplate,
                registeredClientRepository
        );
    }
}