package com.bankflow.ai.mcp;

import com.bankflow.security.BankFlowUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class OAuthLoginConfig {

    public static final String OAUTH_LOGIN_ENDPOINT = "/oauth/login";
    private final BankFlowUserDetailsService bankFlowUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public AuthenticationManager oauthAuthenticationManager() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(bankFlowUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return provider::authenticate;
    }

    @Bean
    @Order(3)
    public SecurityFilterChain oauthLoginSecurityFilterChain(
            HttpSecurity http,
            AuthenticationManager oauthAuthenticationManager
    ) throws Exception {

        http
                .securityMatcher(OAUTH_LOGIN_ENDPOINT)
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(OAUTH_LOGIN_ENDPOINT).permitAll()
                                .anyRequest().authenticated()
                )
                .authenticationManager(oauthAuthenticationManager)
                .formLogin(form ->
                        form
                                .loginPage(OAUTH_LOGIN_ENDPOINT)
                                .loginProcessingUrl(OAUTH_LOGIN_ENDPOINT)
                                .permitAll()
                );

        return http.build();
    }
}