package com.bankflow.service;

import com.bankflow.dto.AuthResponse;
import com.bankflow.dto.CreateAdminRequest;
import com.bankflow.dto.LoginRequest;
import com.bankflow.dto.RegisterRequest;
import com.bankflow.entity.User;
import com.bankflow.repository.UserRepository;
import com.bankflow.security.JwtService; // Assuming you have a JWT generator service
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // Injected to generate the JWT token

    @Transactional
    public void registerCustomer(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(encodedPassword)
                .role(User.Role.CUSTOMER)
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // 1. Fetch user by email or throw uniform error for security
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // 2. Compare plain password with stored BCrypt hash
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // 3. Generate JWT token
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        // 4. Return token and user metadata
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    @Transactional
    public void createAdminAccount(CreateAdminRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User adminUser = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(encodedPassword)
                .role(User.Role.ADMIN) // Explicitly assign ADMIN role
                .build();

        userRepository.save(adminUser);
    }
}
