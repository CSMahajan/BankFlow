package com.bankflow.config;

import com.bankflow.entity.User;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminSeederConfig {

    @Value("${app.default-admin.email:admin@bankflow.com}")
    private String adminEmail;

    @Value("${app.default-admin.password:AdminSecretPass123!}")
    private String adminPassword;

    @Bean
    public CommandLineRunner seedInitialAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = User.builder()
                        .fullName("System Administrator")
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role(User.Role.ADMIN)
                        .build();

                userRepository.save(admin);
                System.out.println(">>> Initial Admin user successfully seeded!");
            }
        };
    }
}
