package com.bankflow.config;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

@Configuration
public class OAuth2ClientConfig {

    @Value("${oauth2.claude-client-id:bankflow-claude}")
    private String claudeClientId;

    @Value("${oauth2.claude-redirect-uri:https://claude.ai/api/mcp/auth_callback}")
    private String claudeRedirectUri;

    @Bean
    public RegisteredClient mcpClient(
            RegisteredClientRepository registeredClientRepository) {

        RegisteredClient existingClient =
                registeredClientRepository.findByClientId("bankflow-mcp");

        if (existingClient != null) {
            return existingClient;
        }

        RegisteredClient client = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("bankflow-mcp")
                .clientName("BankFlow MCP")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:3334/oauth/callback")
                .scope("openid")
                .scope("profile")
                .scope("email")
                .clientSettings(
                        ClientSettings.builder()
                                .requireProofKey(true)
                                .requireAuthorizationConsent(true)
                                .build()
                )
                .build();

        registeredClientRepository.save(client);

        return client;
    }

    @Bean
    public RegisteredClient claudeMcpClient(
            RegisteredClientRepository registeredClientRepository) {

        RegisteredClient existingClient =
                registeredClientRepository.findByClientId(claudeClientId);

        if (existingClient != null) {
            return existingClient;
        }

        RegisteredClient client = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId(claudeClientId)
                .clientName("BankFlow Claude")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(claudeRedirectUri)
                .scope("openid")
                .scope("profile")
                .scope("email")
                .clientSettings(
                        ClientSettings.builder()
                                .requireProofKey(true)
                                .requireAuthorizationConsent(true)
                                .build()
                )
                .build();

        registeredClientRepository.save(client);

        return client;
    }
}