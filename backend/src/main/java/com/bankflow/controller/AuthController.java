package com.bankflow.controller;

import com.bankflow.dto.AuthResponse;
import com.bankflow.dto.LoginRequest;
import com.bankflow.dto.RegisterRequest;
import com.bankflow.dto.ResendVerificationRequest;
import com.bankflow.service.UserService;
import com.bankflow.service.VerificationTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        userService.registerCustomer(request);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        verificationTokenService.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully.");
    }

    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendVerificationEmail(
            @Valid @RequestBody ResendVerificationRequest request) {
        userService.resendVerificationEmail(request);
    }

}
