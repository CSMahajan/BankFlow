package com.bankflow.controller;

import com.bankflow.config.SecurityConfig;
import com.bankflow.dto.AccountResponse;
import com.bankflow.dto.BalanceResponse;
import com.bankflow.dto.CreateAccountRequest;
import com.bankflow.entity.Account.AccountType;
import com.bankflow.exception.GlobalExceptionHandler;
import com.bankflow.filter.JwtAuthenticationFilter;
import com.bankflow.filter.RateLimitFilter;
import com.bankflow.filter.UserRateLimitFilter;
import com.bankflow.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AccountController.class,
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
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    @WithMockUser
    void createAccount_shouldReturnCreatedAccount() throws Exception {

        AccountResponse response = new AccountResponse(
                1L,
                "1234567890",
                "John Doe",
                "john@example.com",
                AccountType.SAVINGS,
                "Nashik",
                new BigDecimal("5000.00"),
                "ACTIVE",
                LocalDateTime.now()
        );

        when(accountService.createAccount(any(CreateAccountRequest.class)))
                .thenReturn(response);

        String requestBody = """
                {
                    "accountType": "SAVINGS",
                    "branchName": "Nashik",
                    "initialDeposit": 5000.00
                }
                """;

        mockMvc.perform(post("/api/v1/accounts/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.customerEmail").value("john@example.com"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.branchName").value("Nashik"))
                .andExpect(jsonPath("$.currentBalance").value(5000.00))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));

        verify(accountService).createAccount(any(CreateAccountRequest.class));
    }

    @Test
    @WithMockUser
    void createAccount_shouldReturnBadRequestWhenValidationFails() throws Exception {

        String requestBody = """
                {
                    "accountType": null,
                    "branchName": "",
                    "initialDeposit": 5000.00
                }
                """;

        mockMvc.perform(post("/api/v1/accounts/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Input request failed validation constraints"))
                .andExpect(jsonPath("$.details").isArray());

        verifyNoInteractions(accountService);
    }

    @Test
    @WithMockUser
    void createAccount_shouldReturnBadRequestWhenServiceThrowsIllegalArgumentException()
            throws Exception {

        when(accountService.createAccount(any(CreateAccountRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid account request"));

        String requestBody = """
                {
                    "accountType": "SAVINGS",
                    "branchName": "Nashik",
                    "initialDeposit": 5000.00
                }
                """;

        mockMvc.perform(post("/api/v1/accounts/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid account request"));

        verify(accountService).createAccount(any(CreateAccountRequest.class));
    }

    @Test
    @WithMockUser
    void getMyAccounts_shouldReturnAccounts() throws Exception {

        AccountResponse response = new AccountResponse(
                1L,
                "1234567890",
                "John Doe",
                "john@example.com",
                AccountType.SAVINGS,
                "Nashik",
                new BigDecimal("5000.00"),
                "ACTIVE",
                LocalDateTime.now()
        );

        when(accountService.getMyAccounts())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/accounts/my-accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"))
                .andExpect(jsonPath("$[0].accountType").value("SAVINGS"))
                .andExpect(jsonPath("$[0].currentBalance").value(5000.00));

        verify(accountService).getMyAccounts();
    }

    @Test
    @WithMockUser
    void getAccountByNumber_shouldReturnAccount() throws Exception {

        AccountResponse response = new AccountResponse(
                1L,
                "1234567890",
                "John Doe",
                "john@example.com",
                AccountType.SAVINGS,
                "Nashik",
                new BigDecimal("5000.00"),
                "ACTIVE",
                LocalDateTime.now()
        );

        when(accountService.getAccountByNumber("1234567890"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));

        verify(accountService).getAccountByNumber("1234567890");
    }

    @Test
    @WithMockUser
    void getAccountByNumber_shouldReturnBadRequestWhenAccountNotFound() throws Exception {

        when(accountService.getAccountByNumber("1234567890"))
                .thenThrow(new IllegalArgumentException("Account not found"));

        mockMvc.perform(get("/api/v1/accounts/1234567890"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Account not found"));

        verify(accountService).getAccountByNumber("1234567890");
    }

    @Test
    @WithMockUser
    void getAvailableBalance_shouldReturnBalance() throws Exception {

        BalanceResponse response = new BalanceResponse(
                "1234567890",
                new BigDecimal("5000.00"),
                "ACTIVE"
        );

        when(accountService.getAvailableBalance("1234567890"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/1234567890/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.currentBalance").value(5000.00))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));

        verify(accountService).getAvailableBalance("1234567890");
    }

    @Test
    @WithMockUser
    void getAvailableBalance_shouldReturnBadRequestWhenAccountNotFound() throws Exception {

        when(accountService.getAvailableBalance("1234567890"))
                .thenThrow(new IllegalArgumentException("Account not found"));

        mockMvc.perform(get("/api/v1/accounts/1234567890/balance"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Account not found"));

        verify(accountService).getAvailableBalance("1234567890");
    }

    @Test
    @WithMockUser
    void toggleAccountStatus_shouldReturnUpdatedAccount() throws Exception {

        AccountResponse response = new AccountResponse(
                1L,
                "1234567890",
                "John Doe",
                "john@example.com",
                AccountType.SAVINGS,
                "Nashik",
                new BigDecimal("5000.00"),
                "FROZEN",
                LocalDateTime.now()
        );

        when(accountService.toggleAccountStatus("1234567890"))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/accounts/1234567890/toggle-status").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.accountStatus").value("FROZEN"));

        verify(accountService).toggleAccountStatus("1234567890");
    }

    @Test
    @WithMockUser
    void toggleAccountStatus_shouldReturnBadRequestWhenServiceThrowsIllegalArgumentException()
            throws Exception {

        when(accountService.toggleAccountStatus("1234567890"))
                .thenThrow(new IllegalArgumentException("Account not found"));

        mockMvc.perform(
                        patch("/api/v1/accounts/1234567890/toggle-status")
                                .with(csrf())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Account not found"));

        verify(accountService).toggleAccountStatus("1234567890");
    }
}