package com.bankflow.controller;

import com.bankflow.dto.CreateAdminRequest;
import com.bankflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @PostMapping("/users/create-admin")
    @PreAuthorize("hasRole('ADMIN')") // Blocks non-admins (HTTP 403 Forbidden)
    @ResponseStatus(HttpStatus.CREATED)
    public void createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        userService.createAdminAccount(request);
    }
}
