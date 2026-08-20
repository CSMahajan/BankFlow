package com.bankflow.filter;

import com.bankflow.repository.UserRepository;
import com.bankflow.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Check for Bearer token in header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.trace("No Bearer Authorization header found for URI: [{}]", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractEmail(jwt);
        } catch (Exception ex) {

            log.warn("Invalid JWT token for URI [{}]: {}", request.getRequestURI(), ex.getMessage());

            sendUnauthorizedResponse(response, "Invalid or expired token");
            return;
        }

        // Authenticate if email exists and security context is not already set
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.debug("Authenticating request for user [{}] at URI [{}]", userEmail, request.getRequestURI());

            var userOptional = userRepository.findByEmail(userEmail);

            if (userOptional.isPresent() && jwtService.isTokenValid(jwt, userEmail)) {
                var user = userOptional.get();
                var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

                var authToken = new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        Collections.singletonList(authority)
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("SecurityContext successfully populated with authority [ROLE_{}] for user [{}]",
                        user.getRole().name(), userEmail);
            } else {
                log.warn(
                        "SecurityContext NOT populated: " +
                                "Token invalid or user [{}] no longer exists in database", userEmail);
                sendUnauthorizedResponse(response, "Invalid token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(
            HttpServletResponse response, String message) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.getWriter()
                .write(
                        objectMapper.writeValueAsString(
                                Map.of(
                                        "status", 401,
                                        "error", "Unauthorized",
                                        "message", message
                                )
                        )
                );
    }
}
