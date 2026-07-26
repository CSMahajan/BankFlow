package com.bankflow.service;

import com.bankflow.dto.AuthResponse;
import com.bankflow.dto.CreateAdminRequest;
import com.bankflow.dto.LoginRequest;
import com.bankflow.dto.RegisterRequest;
import com.bankflow.entity.User;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // Injected to generate the JWT token

    @Transactional
    public void registerCustomer(RegisterRequest request) {
        log.info("Attempting customer registration for email [{}]", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: Email [{}] is already registered", request.email());
            throw new IllegalArgumentException("Email is already registered");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(encodedPassword)
                .role(User.Role.CUSTOMER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Customer registered successfully. User ID: [{}], Email: [{}]", savedUser.getId(), savedUser.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting authentication for email [{}]", request.email());

        // 1. Fetch user by email or throw uniform error for security
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Authentication failed for email [{}]: Account not found", request.email());
                    return new BadCredentialsException("Invalid email or password");
                });

        // 2. Compare plain password with stored BCrypt hash
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Authentication failed for email [{}]: Password mismatch", request.email());
            throw new BadCredentialsException("Invalid email or password");
        }

        // 3. Generate JWT token
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        log.info("User [{}] authenticated successfully with role [{}]", user.getEmail(), user.getRole());

        // 4. Return token and user metadata
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    @Transactional
    public void createAdminAccount(CreateAdminRequest request) {
        log.info("Attempting ADMIN account creation for email [{}]", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Admin account creation failed: Email [{}] is already registered", request.email());
            throw new IllegalArgumentException("Email is already registered");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User adminUser = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(encodedPassword)
                .role(User.Role.ADMIN) // Explicitly assign ADMIN role
                .build();

        User savedAdmin = userRepository.save(adminUser);
        log.info("ADMIN account created successfully. User ID: [{}], Email: [{}]", savedAdmin.getId(), savedAdmin.getEmail());
    }
}
