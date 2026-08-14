package com.bankflow.service;

import com.bankflow.entity.User;
import com.bankflow.exception.ResourceNotFoundException;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(
                        authentication.getName()).
                orElseThrow(() -> new ResourceNotFoundException("Authenticated xUser not found")
                );
    }
}