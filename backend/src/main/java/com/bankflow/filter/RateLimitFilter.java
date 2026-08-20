package com.bankflow.filter;

import com.bankflow.config.RateLimitProperties;
import com.bankflow.ratelimit.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {


    private final RateLimitService rateLimitService;

    private final RateLimitProperties properties;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        boolean allowed = true;

        if (path.equals("/api/v1/auth/login")) {

            allowed = checkLimit(
                    "LOGIN:" + getClientIp(request),
                    properties.getLogin(),
                    response
            );

        }

        else if (path.equals("/api/v1/auth/register")) {

            allowed = checkLimit(
                    "REGISTER:" + getClientIp(request),
                    properties.getRegister(),
                    response
            );

        }

        else if (path.equals("/api/v1/auth/forgot-password")) {

            allowed = checkLimit(
                    "FORGOT_PASSWORD:" + getClientIp(request),
                    properties.getForgotPassword(),
                    response
            );

        }

        else if (path.equals("/api/v1/auth/resend-verification")) {

            allowed = checkLimit(
                    "RESEND_VERIFICATION:" + getClientIp(request),
                    properties.getResendVerification(),
                    response
            );

        }

        if (!allowed) {
            return;
        }

        filterChain.doFilter(request,response);

    }


    private boolean checkLimit(String key, RateLimitProperties.Limit limit, HttpServletResponse response) throws IOException {

        boolean allowed = rateLimitService.isAllowed(key, limit.getLimit(), limit.getWindow());

        if (!allowed) {

            log.warn(
                    "Rate limit exceeded for key: {}",
                    key
            );
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader(
                    "Retry-After",
                    String.valueOf(limit.getWindow())
            );
            response.getWriter()
                    .write("""
                    {
                      "status":429,
                      "error":"Too Many Requests",
                      "message":"Too many requests. Please try again later."
                    }
                    """);

            return false;
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {

        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0];
        }

        return request.getRemoteAddr();
    }
}