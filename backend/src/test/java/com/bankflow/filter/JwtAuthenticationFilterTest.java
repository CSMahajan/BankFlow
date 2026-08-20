package com.bankflow.filter;

import com.bankflow.entity.User;
import com.bankflow.repository.UserRepository;
import com.bankflow.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PrintWriter writer;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User mockUser;

    @BeforeEach
    void setUp() throws IOException {
        SecurityContextHolder.clearContext();

        mockUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .role(User.Role.CUSTOMER)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==========================================
    // MISSING OR INVALID HEADER TESTS
    // ==========================================

    @Test
    @DisplayName("Do Filter - No Authorization Header Continues Chain Unauthenticated")
    void doFilterInternal_NoAuthHeader_ContinuesChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    @DisplayName("Do Filter - Non-Bearer Header Continues Chain Unauthenticated")
    void doFilterInternal_NonBearerHeader_ContinuesChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic c29tZXRva2Vu");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);
    }

    // ==========================================
    // JWT EXTRACTION EXCEPTION TESTS
    // ==========================================

    @Test
    @DisplayName("Do Filter - JWT Extraction Throws Exception Continues Chain Unauthenticated")
    void doFilterInternal_JwtExtractionException_ContinuesChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.jwt.token");
        when(jwtService.extractEmail("invalid.jwt.token")).thenThrow(new RuntimeException("Malformed JWT"));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(response.getWriter()).thenReturn(writer);
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request,response);
        verifyNoInteractions(userRepository);
    }

    // ==========================================
    // SUCCESSFUL AUTHENTICATION TESTS
    // ==========================================

    @Test
    @DisplayName("Do Filter - Valid Token Populates SecurityContext")
    void doFilterInternal_ValidToken_PopulatesSecurityContext() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String email = "john@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(jwtService.isTokenValid(token, email)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(email, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));

        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ==========================================
    // INVALID TOKEN OR USER NOT FOUND TESTS
    // ==========================================

    @Test
    @DisplayName("Do Filter - User Not Found In DB Does Not Authenticate")
    void doFilterInternal_UserNotFound_DoesNotAuthenticate() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String email = "john@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(response.getWriter()).thenReturn(writer);
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request,response);
    }

    @Test
    @DisplayName("Do Filter - Invalid Token Does Not Authenticate")
    void doFilterInternal_InvalidToken_DoesNotAuthenticate() throws ServletException, IOException {
        String token = "expired.jwt.token";
        String email = "john@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(jwtService.isTokenValid(token, email)).thenReturn(false);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(response.getWriter()).thenReturn(writer);
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request,response);
    }

    @Test
    @DisplayName("Do Filter - Existing Security Context Is Preserved")
    void doFilterInternal_ExistingSecurityContext_Preserved() throws ServletException, IOException {
        var existingAuth = new UsernamePasswordAuthenticationToken("john@example.com", null, null);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtService.extractEmail("valid.jwt.token")).thenReturn("john@example.com");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userRepository);
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
