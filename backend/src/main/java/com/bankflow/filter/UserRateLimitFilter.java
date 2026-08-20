package com.bankflow.filter;

import com.bankflow.config.RateLimitProperties;
import com.bankflow.ratelimit.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UserRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            filterChain.doFilter(request, response);
            return;
        }

        String userIdentifier = authentication.getName();

        String key =
                "USER:"
                        + userIdentifier
                        + ":"
                        + request.getRequestURI();

        RateLimitProperties.Limit limit =
                properties.getUser();

        if (limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean allowed =
                rateLimitService.isAllowed(
                        key,
                        limit.getLimit(),
                        limit.getWindow()
                );

        if (!allowed) {

            response.setStatus(
                    HttpStatus.TOO_MANY_REQUESTS.value()
            );

            response.setContentType(
                    MediaType.APPLICATION_JSON_VALUE
            );

            response.getWriter().write("""
                    {
                      "status":429,
                      "error":"Too Many Requests",
                      "message":"User request limit exceeded. Please try again later."
                    }
                    """);

            return;
        }

        filterChain.doFilter(request, response);
    }
}