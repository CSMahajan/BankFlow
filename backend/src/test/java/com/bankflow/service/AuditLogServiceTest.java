package com.bankflow.service;

import com.bankflow.dto.AuditLogResponse;
import com.bankflow.entity.AuditAction;
import com.bankflow.entity.AuditLog;
import com.bankflow.entity.User;
import com.bankflow.repository.AuditLogRepository;
import com.bankflow.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuditLogService auditLogService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .role(User.Role.CUSTOMER)
                .emailVerified(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void log_shouldSaveAuditLogForAuthenticatedUser() {

        AuditAction action = AuditAction.LOGIN;
        String description = "User logged in";

        when(authentication.getName())
                .thenReturn(user.getEmail());

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        auditLogService.log(action, description);

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();

        assertEquals(action, savedLog.getAction());
        assertEquals(user.getEmail(), savedLog.getPerformedBy());
        assertEquals(user.getRole(), savedLog.getRole());
        assertEquals(description, savedLog.getDescription());

        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void log_shouldThrowExceptionWhenAuthenticatedUserNotFound() {

        when(authentication.getName())
                .thenReturn(user.getEmail());

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> auditLogService.log(
                                AuditAction.LOGIN,
                                "User logged in"
                        )
                );

        assertEquals(
                "Authenticated user not found",
                exception.getMessage()
        );

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void log_withUser_shouldSaveAuditLog() {

        AuditAction action = AuditAction.EMAIL_VERIFIED;
        String description = "Email address verified successfully";

        auditLogService.log(
                user,
                action,
                description
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();

        assertEquals(action, savedLog.getAction());
        assertEquals(user.getEmail(), savedLog.getPerformedBy());
        assertEquals(user.getRole(), savedLog.getRole());
        assertEquals(description, savedLog.getDescription());

        verifyNoInteractions(userRepository);
    }

    @Test
    void getAuditLogs_shouldReturnMappedAuditLogResponses() {

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 8, 30, 10, 30);

        AuditLog auditLog = AuditLog.builder()
                .id(10L)
                .action(AuditAction.LOGIN)
                .performedBy("john@example.com")
                .role(User.Role.CUSTOMER)
                .description("User logged in")
                .createdAt(createdAt)
                .build();

        Page<AuditLog> auditLogPage =
                new PageImpl<>(List.of(auditLog));

        when(auditLogRepository.findAll(
                any(Specification.class),
                any(PageRequest.class)
        )).thenReturn(auditLogPage);

        Page<AuditLogResponse> result =
                auditLogService.getAuditLogs(
                        0,
                        10,
                        null,
                        null,
                        null,
                        null
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        AuditLogResponse response =
                result.getContent().get(0);

        assertEquals(10L, response.id());
        assertEquals(AuditAction.LOGIN, response.action());
        assertEquals("john@example.com", response.performedBy());
        assertEquals(User.Role.CUSTOMER, response.role());
        assertEquals("User logged in", response.description());
        assertEquals(createdAt, response.createdAt());

        verify(auditLogRepository).findAll(
                any(Specification.class),
                any(PageRequest.class)
        );
    }

    @Test
    void getAuditLogs_shouldUseRequestedPageSizeAndDescendingCreatedAt() {

        when(auditLogRepository.findAll(
                any(Specification.class),
                any(PageRequest.class)
        )).thenReturn(Page.empty());

        auditLogService.getAuditLogs(
                2,
                20,
                null,
                null,
                null,
                null
        );

        ArgumentCaptor<PageRequest> captor =
                ArgumentCaptor.forClass(PageRequest.class);

        verify(auditLogRepository).findAll(
                any(Specification.class),
                captor.capture()
        );

        PageRequest pageable = captor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());

        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort()
                        .getOrderFor("createdAt")
                        .getDirection()
        );
    }

    @Test
    void getAuditLogs_shouldReturnEmptyPageWhenRepositoryReturnsEmptyPage() {

        when(auditLogRepository.findAll(
                any(Specification.class),
                any(PageRequest.class)
        )).thenReturn(Page.empty());

        Page<AuditLogResponse> result =
                auditLogService.getAuditLogs(
                        0,
                        10,
                        null,
                        null,
                        null,
                        null
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}