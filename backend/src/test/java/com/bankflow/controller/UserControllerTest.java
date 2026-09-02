package com.bankflow.controller;

import com.bankflow.config.SecurityConfig;
import com.bankflow.dto.*;
import com.bankflow.exception.GlobalExceptionHandler;
import com.bankflow.filter.JwtAuthenticationFilter;
import com.bankflow.filter.RateLimitFilter;
import com.bankflow.filter.UserRateLimitFilter;
import com.bankflow.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = {
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
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = SecurityConfig.class
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
//@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    // ---------------------------------------------------------
    // GET /api/v1/users/me
    // ---------------------------------------------------------

    @Test
    void getCurrentUser_shouldReturnCurrentUser() throws Exception {

        UserMeResponse response = new UserMeResponse(
                1L,
                "John Doe",
                "john@example.com",
                "CUSTOMER"
        );

        when(userService.getCurrentUser("john@example.com"))
                .thenReturn(response);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "john@example.com",
                        null,
                        List.of()
                );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        try {
            mockMvc.perform(get("/api/v1/users/me"))
                    .andDo(print());

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ---------------------------------------------------------
    // PUT /api/v1/users/profile
    // ---------------------------------------------------------

    @Test
    void updateProfile_shouldReturnOk() throws Exception {

        mockMvc.perform(
                        put("/api/v1/users/profile")
                                .contentType("application/json")
                                .content("""
                                        {
                                            "fullName": "John Updated"
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        verify(userService).updateCustomerProfile(
                eq(new UpdateProfileRequest("John Updated"))
        );
    }

    @Test
    void updateProfile_shouldReturnBadRequestWhenFullNameIsBlank()
            throws Exception {

        mockMvc.perform(
                        put("/api/v1/users/profile")
                                .contentType("application/json")
                                .content("""
                                        {
                                            "fullName": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------
    // PATCH /api/v1/users/change-password
    // ---------------------------------------------------------

    @Test
    void changePassword_shouldReturnNoContent() throws Exception {

        mockMvc.perform(
                        patch("/api/v1/users/change-password")
                                .contentType("application/json")
                                .content("""
                                        {
                                            "currentPassword": "OldPassword123",
                                            "newPassword": "NewPassword123",
                                            "confirmPassword": "NewPassword123"
                                        }
                                        """)
                )
                .andExpect(status().isNoContent());

        verify(userService).changePassword(
                eq(new ChangePasswordRequest(
                        "OldPassword123",
                        "NewPassword123",
                        "NewPassword123"
                ))
        );
    }

    @Test
    void changePassword_shouldReturnBadRequestWhenNewPasswordTooShort()
            throws Exception {

        mockMvc.perform(
                        patch("/api/v1/users/change-password")
                                .contentType("application/json")
                                .content("""
                                        {
                                            "currentPassword": "OldPassword123",
                                            "newPassword": "short",
                                            "confirmPassword": "short"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_shouldReturnBadRequestWhenRequiredFieldIsMissing()
            throws Exception {

        mockMvc.perform(
                        patch("/api/v1/users/change-password")
                                .contentType("application/json")
                                .content("""
                                        {
                                            "currentPassword": "",
                                            "newPassword": "NewPassword123",
                                            "confirmPassword": "NewPassword123"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------
    // GET /api/v1/users
    // ---------------------------------------------------------

    @Test
    void getAllUsers_shouldReturnUsers() throws Exception {

        UserSummaryResponse user = new UserSummaryResponse(
                1L,
                "John Doe",
                "john@example.com",
                "CUSTOMER",
                LocalDateTime.of(2026, 9, 2, 10, 0),
                2L
        );

        Page<UserSummaryResponse> page =
                new org.springframework.data.domain.PageImpl<>(
                        List.of(user)
                );

        when(userService.getAllUsers(
                0,
                10,
                null,
                "ALL"
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/users")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].fullName")
                        .value("John Doe"))
                .andExpect(jsonPath("$.content[0].email")
                        .value("john@example.com"))
                .andExpect(jsonPath("$.content[0].role")
                        .value("CUSTOMER"))
                .andExpect(jsonPath("$.content[0].accountCount")
                        .value(2));

        verify(userService).getAllUsers(
                0,
                10,
                null,
                "ALL"
        );
    }

    @Test
    void getAllUsers_shouldPassPaginationSearchAndRole()
            throws Exception {

        Page<UserSummaryResponse> page =
                new org.springframework.data.domain.PageImpl<>(
                        List.of()
                );

        when(userService.getAllUsers(
                2,
                20,
                "john",
                "CUSTOMER"
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/users")
                                .param("page", "2")
                                .param("size", "20")
                                .param("search", "john")
                                .param("role", "CUSTOMER")
                )
                .andExpect(status().isOk());

        verify(userService).getAllUsers(
                2,
                20,
                "john",
                "CUSTOMER"
        );
    }

    // ---------------------------------------------------------
    // GET /api/v1/users/{userId}
    // ---------------------------------------------------------

    @Test
    void getUserDetails_shouldReturnUserDetails() throws Exception {

        UserDetailsResponse response = new UserDetailsResponse(
                1L,
                "John Doe",
                "john@example.com",
                "CUSTOMER",
                LocalDateTime.of(2026, 9, 2, 10, 0),
                2L,
                1L,
                1L,
                1L,
                new BigDecimal("25000.00"),
                new BigDecimal("150000.00")
        );

        when(userService.getUserDetails(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/users/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email")
                        .value("john@example.com"))
                .andExpect(jsonPath("$.role")
                        .value("CUSTOMER"))
                .andExpect(jsonPath("$.accountCount").value(2))
                .andExpect(jsonPath("$.cardCount").value(1))
                .andExpect(jsonPath("$.loanCount").value(1))
                .andExpect(jsonPath("$.fixedDepositCount").value(1))
                .andExpect(jsonPath("$.totalBalance")
                        .value(25000.00))
                .andExpect(jsonPath("$.outstandingLoanAmount")
                        .value(150000.00));

        verify(userService).getUserDetails(1L);
    }
}