package com.bankflow.config;

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
                .securityMatcher("/oauth/login")
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/oauth/login").permitAll()
                                .anyRequest().authenticated()
                )
                .authenticationManager(oauthAuthenticationManager)
                .formLogin(form ->
                        form
                                .loginPage("/oauth/login")
                                .loginProcessingUrl("/oauth/login")
                                .permitAll()
                );

        return http.build();
    }
}