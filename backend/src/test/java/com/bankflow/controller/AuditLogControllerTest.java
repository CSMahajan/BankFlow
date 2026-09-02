package com.bankflow.controller;

import com.bankflow.config.SecurityConfig;
import com.bankflow.dto.AuditLogResponse;
import com.bankflow.entity.AuditAction;
import com.bankflow.entity.User;
import com.bankflow.filter.JwtAuthenticationFilter;
import com.bankflow.filter.RateLimitFilter;
import com.bankflow.filter.UserRateLimitFilter;
import com.bankflow.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuditLogController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = SecurityConfig.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = RateLimitFilter.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = UserRateLimitFilter.class
                )
        }
)
@Import(AuditLogControllerTest.MethodSecurityTestConfig.class)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditLogs_shouldReturnAuditLogsForAdmin() throws Exception {

        AuditLogResponse response = new AuditLogResponse(
                1L,
                AuditAction.LOGIN,
                "john@example.com",
                User.Role.CUSTOMER,
                "User logged in",
                LocalDateTime.now()
        );

        Page<AuditLogResponse> page = new PageImpl<>(
                List.of(response),
                PageRequest.of(0, 20),
                1
        );

        when(auditLogService.getAuditLogs(
                0,
                20,
                null,
                null,
                null,
                null
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].action").value("LOGIN"))
                .andExpect(jsonPath("$.content[0].performedBy").value("john@example.com"))
                .andExpect(jsonPath("$.content[0].role").value("CUSTOMER"))
                .andExpect(jsonPath("$.content[0].description").value("User logged in"));

        verify(auditLogService).getAuditLogs(
                0,
                20,
                null,
                null,
                null,
                null
        );
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAuditLogs_shouldReturnForbiddenForCustomer() throws Exception {

        mockMvc.perform(get("/api/v1/admin/audit-logs"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(auditLogService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditLogs_shouldPassQueryParametersToService() throws Exception {

        AuditLogResponse response = new AuditLogResponse(
                1L,
                AuditAction.LOGIN,
                "john@example.com",
                User.Role.CUSTOMER,
                "User logged in",
                LocalDateTime.now()
        );

        Page<AuditLogResponse> page = new PageImpl<>(
                List.of(response),
                PageRequest.of(1, 10),
                1
        );

        when(auditLogService.getAuditLogs(
                1,
                10,
                "john",
                User.Role.CUSTOMER,
                AuditAction.LOGIN,
                List.of(AuditAction.LOGIN, AuditAction.USER_REGISTERED)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .param("page", "1")
                        .param("size", "10")
                        .param("search", "john")
                        .param("role", "CUSTOMER")
                        .param("action", "LOGIN")
                        .param("actions", "LOGIN", "USER_REGISTERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(auditLogService).getAuditLogs(
                1,
                10,
                "john",
                User.Role.CUSTOMER,
                AuditAction.LOGIN,
                List.of(AuditAction.LOGIN, AuditAction.USER_REGISTERED)
        );
    }

    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }
}