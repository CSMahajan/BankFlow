package com.bankflow.controller;

import com.bankflow.config.SecurityConfig;
import com.bankflow.dto.*;
import com.bankflow.entity.Account;
import com.bankflow.entity.Card;
import com.bankflow.filter.JwtAuthenticationFilter;
import com.bankflow.filter.RateLimitFilter;
import com.bankflow.filter.UserRateLimitFilter;
import com.bankflow.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminController.class,
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
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private LoanService loanService;

    @MockitoBean
    private KycService kycService;


    @Test
    void createAdmin_shouldReturnCreated() throws Exception {

        CreateAdminRequest request =
                new CreateAdminRequest(
                        "Admin User",
                        "admin@example.com",
                        "password123"
                );

        doNothing().when(userService).createAdminAccount(request);

        mockMvc.perform(
                        post("/api/v1/admin/users/create-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "fullName": "Admin User",
                                          "email": "admin@example.com",
                                          "password": "password123"
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        verify(userService).createAdminAccount(any(CreateAdminRequest.class));
    }


    @Test
    void createAdmin_shouldReturnBadRequestWhenFullNameIsBlank()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/admin/users/create-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "fullName": "",
                                          "email": "admin@example.com",
                                          "password": "password123"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }


    @Test
    void getUserAccounts_shouldReturnAccounts() throws Exception {

        AdminUserAccountResponse response =
                new AdminUserAccountResponse(
                        "1234567890",
                        Account.AccountType.SAVINGS,
                        new BigDecimal("50000.00"),
                        Account.AccountStatus.ACTIVE,
                        "Nashik Main Branch",
                        LocalDateTime.of(2026, 1, 10, 10, 30)
                );

        when(userService.getUserAccounts(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/admin/users/1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"))
                .andExpect(jsonPath("$[0].accountType").value("SAVINGS"))
                .andExpect(jsonPath("$[0].currentBalance").value(50000.00))
                .andExpect(jsonPath("$[0].accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$[0].branchName")
                        .value("Nashik Main Branch"));

        verify(userService).getUserAccounts(1L);
    }


    @Test
    void getUserCards_shouldReturnCards() throws Exception {

        AdminUserCardResponse response =
                new AdminUserCardResponse(
                        "4111111111111111",
                        Card.CardType.DEBIT,
                        Card.CardStatus.ACTIVE,
                        new BigDecimal("25000.00"),
                        LocalDate.of(2030, 12, 31)
                );

        when(userService.getUserCards(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/admin/users/1/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardNumber")
                        .value("4111111111111111"))
                .andExpect(jsonPath("$[0].cardType").value("DEBIT"))
                .andExpect(jsonPath("$[0].cardStatus").value("ACTIVE"))
                .andExpect(jsonPath("$[0].dailyLimit").value(25000.00));

        verify(userService).getUserCards(1L);
    }


    @Test
    void getUserLoans_shouldReturnLoans() throws Exception {

        AdminUserLoanResponse response =
                new AdminUserLoanResponse(
                        "LN10001",
                        com.bankflow.entity.Loan.LoanType.PERSONAL,
                        com.bankflow.entity.Loan.LoanStatus.ACTIVE,
                        new BigDecimal("100000.00"),
                        new BigDecimal("75000.00"),
                        new BigDecimal("5000.00"),
                        24,
                        LocalDate.of(2026, 10, 15)
                );

        when(userService.getUserLoans(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/admin/users/1/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].loanNumber").value("LN10001"))
                .andExpect(jsonPath("$[0].loanType").value("PERSONAL"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].principalAmount")
                        .value(100000.00))
                .andExpect(jsonPath("$[0].remainingBalance")
                        .value(75000.00));

        verify(userService).getUserLoans(1L);
    }


    @Test
    void getUserFixedDeposits_shouldReturnFixedDeposits()
            throws Exception {

        AdminUserFixedDepositResponse response =
                new AdminUserFixedDepositResponse(
                        "FD10001",
                        new BigDecimal("50000.00"),
                        new BigDecimal("7.50"),
                        LocalDate.of(2028, 1, 15),
                        new BigDecimal("57812.50"),
                        com.bankflow.entity.FixedDeposit.FdStatus.ACTIVE
                );

        when(userService.getUserFixedDeposits(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/v1/admin/users/1/fixed-deposits")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fdNumber").value("FD10001"))
                .andExpect(jsonPath("$[0].principalAmount")
                        .value(50000.00))
                .andExpect(jsonPath("$[0].interestRate")
                        .value(7.50))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(userService).getUserFixedDeposits(1L);
    }


    @Test
    void getAllAccounts_shouldReturnPage() throws Exception {

        AccountResponse response =
                new AccountResponse(
                        1L,
                        "1234567890",
                        "John Doe",
                        "john@example.com",
                        Account.AccountType.SAVINGS,
                        "Nashik Main Branch",
                        new BigDecimal("50000.00"),
                        "ACTIVE",
                        LocalDateTime.of(2026, 1, 1, 10, 0)
                );

        when(accountService.getAllAccountsForAdmin(
                0, 20, null, null
        )).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/admin/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].accountNumber")
                        .value("1234567890"))
                .andExpect(jsonPath("$.content[0].customerName")
                        .value("John Doe"));

        verify(accountService)
                .getAllAccountsForAdmin(0, 20, null, null);
    }


    @Test
    void getAllAccounts_shouldPassFilters() throws Exception {

        when(accountService.getAllAccountsForAdmin(
                2,
                50,
                "john",
                Account.AccountStatus.FROZEN
        )).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(
                        get("/api/v1/admin/accounts")
                                .param("page", "2")
                                .param("size", "50")
                                .param("search", "john")
                                .param("status", "FROZEN")
                )
                .andExpect(status().isOk());

        verify(accountService).getAllAccountsForAdmin(
                2,
                50,
                "john",
                Account.AccountStatus.FROZEN
        );
    }


    @Test
    void freezeAccount_shouldReturnAccount() throws Exception {

        AccountResponse response =
                new AccountResponse(
                        1L,
                        "1234567890",
                        "John Doe",
                        "john@example.com",
                        Account.AccountType.SAVINGS,
                        "Nashik Main Branch",
                        new BigDecimal("50000.00"),
                        "FROZEN",
                        LocalDateTime.of(2026, 1, 1, 10, 0)
                );

        when(accountService.freezeAccountByAdmin("1234567890"))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/admin/accounts/1234567890/freeze")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountStatus")
                        .value("FROZEN"));

        verify(accountService)
                .freezeAccountByAdmin("1234567890");
    }


    @Test
    void unfreezeAccount_shouldReturnAccount() throws Exception {

        AccountResponse response =
                new AccountResponse(
                        1L,
                        "1234567890",
                        "John Doe",
                        "john@example.com",
                        Account.AccountType.SAVINGS,
                        "Nashik Main Branch",
                        new BigDecimal("50000.00"),
                        "ACTIVE",
                        LocalDateTime.of(2026, 1, 1, 10, 0)
                );

        when(accountService.unfreezeAccountByAdmin("1234567890"))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/admin/accounts/1234567890/unfreeze")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountStatus")
                        .value("ACTIVE"));

        verify(accountService)
                .unfreezeAccountByAdmin("1234567890");
    }


    @Test
    void getAccountSummary_shouldReturnSummary() throws Exception {

        AccountSummaryResponse response =
                new AccountSummaryResponse(
                        100,
                        5,
                        80,
                        20
                );

        when(accountService.getAccountSummaryForAdmin())
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/accounts/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeAccounts").value(100))
                .andExpect(jsonPath("$.frozenAccounts").value(5))
                .andExpect(jsonPath("$.savingsAccounts").value(80))
                .andExpect(jsonPath("$.currentAccounts").value(20));

        verify(accountService).getAccountSummaryForAdmin();
    }


    @Test
    void getLoanSummary_shouldReturnSummary() throws Exception {

        LoanSummaryResponse response =
                new LoanSummaryResponse(
                        10,
                        6,
                        2,
                        2
                );

        when(loanService.getLoanSummary())
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/loans/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(10))
                .andExpect(jsonPath("$.personalLoans").value(6))
                .andExpect(jsonPath("$.homeLoans").value(2))
                .andExpect(jsonPath("$.vehicleLoans").value(2));

        verify(loanService).getLoanSummary();
    }


    @Test
    void getCardSummary_shouldReturnSummary() throws Exception {

        CardSummaryResponse response =
                new CardSummaryResponse(
                        100,
                        80,
                        15,
                        5
                );

        when(cardService.getCardSummaryForAdmin())
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/cards/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCards").value(100))
                .andExpect(jsonPath("$.activeCards").value(80))
                .andExpect(jsonPath("$.blockedCards").value(15))
                .andExpect(jsonPath("$.frozenCards").value(5));

        verify(cardService).getCardSummaryForAdmin();
    }


    @Test
    void blockCard_shouldReturnCard() throws Exception {

        CardResponse response = new CardResponse(
                1L,
                "1234567890",
                "**** **** **** 1234",
                Card.CardType.DEBIT,
                Card.CardStatus.BLOCKED,
                Account.AccountStatus.ACTIVE,
                "John Doe",
                LocalDate.of(2030, 12, 31),
                "123",
                new BigDecimal("25000.00")
        );

        when(cardService.blockCardByAdmin(1L))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/cards/1/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardStatus").value("BLOCKED"));

        verify(cardService).blockCardByAdmin(1L);
    }


    @Test
    void unblockCard_shouldReturnCard() throws Exception {

        CardResponse response = new CardResponse(
                1L,
                "1234567890",
                "**** **** **** 1234",
                Card.CardType.DEBIT,
                Card.CardStatus.ACTIVE,
                Account.AccountStatus.ACTIVE,
                "John Doe",
                LocalDate.of(2030, 12, 31),
                "123",
                new BigDecimal("25000.00")
        );

        when(cardService.unblockCardByAdmin(1L))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/cards/1/unblock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardStatus").value("ACTIVE"));

        verify(cardService).unblockCardByAdmin(1L);
    }
}