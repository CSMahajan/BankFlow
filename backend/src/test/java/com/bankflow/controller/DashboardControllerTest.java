package com.bankflow.controller;

import com.bankflow.config.SecurityConfig;
import com.bankflow.dto.AdminDashboardSummaryResponse;
import com.bankflow.dto.DashboardSummaryResponse;
import com.bankflow.dto.MonthlyAnalyticsResponse;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.entity.Transaction.TransactionType;
import com.bankflow.exception.GlobalExceptionHandler;
import com.bankflow.filter.JwtAuthenticationFilter;
import com.bankflow.filter.RateLimitFilter;
import com.bankflow.filter.UserRateLimitFilter;
import com.bankflow.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = DashboardController.class,
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
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void getDashboardSummary_shouldReturnSummary() throws Exception {

        TransactionResponse transaction = new TransactionResponse(
                "TXN001",
                "1234567890",
                LocalDateTime.of(2026, 9, 1, 10, 30),
                TransactionType.CREDIT,
                new BigDecimal("5000.00"),
                new BigDecimal("55000.00"),
                "Salary credit"
        );

        DashboardSummaryResponse response = new DashboardSummaryResponse(
                "John Doe",
                new BigDecimal("150000.00"),
                new BigDecimal("100000.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("25000.00"),
                2,
                1,
                1,
                LocalDate.of(2026, 9, 15),
                new BigDecimal("5000.00"),
                List.of(transaction)
        );

        when(dashboardService.getDashboardSummary())
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.totalNetWorth").value(150000.00))
                .andExpect(jsonPath("$.totalAccountBalance").value(100000.00))
                .andExpect(jsonPath("$.totalFdInvestment").value(50000.00))
                .andExpect(jsonPath("$.totalOutstandingLoans").value(25000.00))
                .andExpect(jsonPath("$.activeAccountsCount").value(2))
                .andExpect(jsonPath("$.activeFdCount").value(1))
                .andExpect(jsonPath("$.activeLoanCount").value(1))
                .andExpect(jsonPath("$.nextEmiDueDate").value("2026-09-15"))
                .andExpect(jsonPath("$.nextEmiAmount").value(5000.00))
                .andExpect(jsonPath("$.recentTransactions.length()").value(1))
                .andExpect(jsonPath("$.recentTransactions[0].transactionId").value("TXN001"))
                .andExpect(jsonPath("$.recentTransactions[0].transactionType").value("CREDIT"));

        verify(dashboardService).getDashboardSummary();
    }

    @Test
    void getCurrentMonthAnalytics_shouldReturnAnalytics() throws Exception {

        MonthlyAnalyticsResponse response = new MonthlyAnalyticsResponse(
                new BigDecimal("100000.00"),
                new BigDecimal("40000.00"),
                new BigDecimal("60000.00")
        );

        when(dashboardService.getCurrentMonthAnalytics())
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard/analytics/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(100000.00))
                .andExpect(jsonPath("$.totalExpense").value(40000.00))
                .andExpect(jsonPath("$.netCashFlow").value(60000.00));

        verify(dashboardService).getCurrentMonthAnalytics();
    }

    @Test
    void getAdminDashboardSummary_shouldReturnSummary() throws Exception {

        AdminDashboardSummaryResponse response =
                new AdminDashboardSummaryResponse(
                        100L,
                        150L,
                        25L,
                        5L,
                        40L,
                        new BigDecimal("5000000.00"),
                        10L
                );

        when(dashboardService.getAdminDashboardSummary())
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard/admin-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(100))
                .andExpect(jsonPath("$.totalAccounts").value(150))
                .andExpect(jsonPath("$.activeLoans").value(25))
                .andExpect(jsonPath("$.pendingLoans").value(5))
                .andExpect(jsonPath("$.activeFixedDeposits").value(40))
                .andExpect(jsonPath("$.totalDeposits").value(5000000.00))
                .andExpect(jsonPath("$.pendingKycDocuments").value(10));

        verify(dashboardService).getAdminDashboardSummary();
    }

    @Test
    void getDashboardTransactions_shouldReturnTransactions() throws Exception {

        TransactionResponse transaction = new TransactionResponse(
                "TXN001",
                "1234567890",
                LocalDateTime.of(2026, 9, 1, 10, 30),
                TransactionType.CREDIT,
                new BigDecimal("5000.00"),
                new BigDecimal("55000.00"),
                "Salary credit"
        );

        PageImpl<TransactionResponse> response =
                new PageImpl<>(List.of(transaction));

        when(dashboardService.getDashboardTransactions(0, 10))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].transactionId").value("TXN001"))
                .andExpect(jsonPath("$.content[0].accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.content[0].transactionType").value("CREDIT"))
                .andExpect(jsonPath("$.content[0].amount").value(5000.00))
                .andExpect(jsonPath("$.content[0].description").value("Salary credit"));

        verify(dashboardService).getDashboardTransactions(0, 10);
    }

    @Test
    void getDashboardTransactions_shouldPassPageAndSize() throws Exception {

        PageImpl<TransactionResponse> response =
                new PageImpl<>(List.of());

        when(dashboardService.getDashboardTransactions(2, 20))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard/transactions")
                        .param("page", "2")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        verify(dashboardService).getDashboardTransactions(2, 20);
    }
}