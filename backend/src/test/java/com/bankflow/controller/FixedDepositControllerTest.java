package com.bankflow.controller;

import com.bankflow.config.SecurityConfig;
import com.bankflow.dto.*;
import com.bankflow.filter.JwtAuthenticationFilter;
import com.bankflow.filter.RateLimitFilter;
import com.bankflow.filter.UserRateLimitFilter;
import com.bankflow.service.FixedDepositService;
import com.bankflow.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = FixedDepositController.class,
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
@Import(GlobalExceptionHandler.class)
class FixedDepositControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FixedDepositService fdService;

    @Test
    void calculateMaturity_shouldReturn200() throws Exception {

        FdCalculatorResponse response = new FdCalculatorResponse(
                new BigDecimal("50000.00"),
                new BigDecimal("7.50"),
                3,
                new BigDecimal("11250.00"),
                new BigDecimal("61250.00")
        );

        when(fdService.calculateMaturity(any(FdCalculatorRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/fd/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "depositAmount": 50000.00,
                                  "tenureYears": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.depositAmount").value(50000.00))
                .andExpect(jsonPath("$.interestRate").value(7.50))
                .andExpect(jsonPath("$.tenureYears").value(3))
                .andExpect(jsonPath("$.totalInterestEarned").value(11250.00))
                .andExpect(jsonPath("$.maturityAmount").value(61250.00));

        verify(fdService).calculateMaturity(any(FdCalculatorRequest.class));
    }

    @Test
    void createFixedDeposit_shouldReturn201() throws Exception {

        FdResponse response = new FdResponse(
                1L,
                "FD123456",
                "John Doe",
                "1234567890",
                new BigDecimal("50000.00"),
                new BigDecimal("7.50"),
                3,
                LocalDate.of(2026, 9, 2),
                LocalDate.of(2029, 9, 2),
                null,
                new BigDecimal("61250.00"),
                null,
                "ACTIVE"
        );

        when(fdService.createFixedDeposit(any(CreateFdRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/fd/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountNumber": "1234567890",
                                  "depositAmount": 50000.00,
                                  "tenureYears": 3
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fdNumber").value("FD123456"))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.sourceAccountNumber").value("1234567890"))
                .andExpect(jsonPath("$.depositAmount").value(50000.00))
                .andExpect(jsonPath("$.interestRate").value(7.50))
                .andExpect(jsonPath("$.tenureYears").value(3))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(fdService).createFixedDeposit(any(CreateFdRequest.class));
    }

    @Test
    void getMyFixedDeposits_shouldReturn200() throws Exception {

        FdResponse response = new FdResponse(
                1L,
                "FD123456",
                "John Doe",
                "1234567890",
                new BigDecimal("50000.00"),
                new BigDecimal("7.50"),
                3,
                LocalDate.of(2026, 9, 2),
                LocalDate.of(2029, 9, 2),
                null,
                new BigDecimal("61250.00"),
                null,
                "ACTIVE"
        );

        when(fdService.getMyFixedDeposits())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/fd/my-fds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fdNumber").value("FD123456"))
                .andExpect(jsonPath("$[0].customerName").value("John Doe"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(fdService).getMyFixedDeposits();
    }

    @Test
    void getFdByNumber_shouldReturn200() throws Exception {

        FdResponse response = new FdResponse(
                1L,
                "FD123456",
                "John Doe",
                "1234567890",
                new BigDecimal("50000.00"),
                new BigDecimal("7.50"),
                3,
                LocalDate.of(2026, 9, 2),
                LocalDate.of(2029, 9, 2),
                null,
                new BigDecimal("61250.00"),
                null,
                "ACTIVE"
        );

        when(fdService.getFdByNumber("FD123456"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/fd/FD123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fdNumber").value("FD123456"))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.sourceAccountNumber").value("1234567890"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(fdService).getFdByNumber("FD123456");
    }

    @Test
    void closeFixedDeposit_shouldReturn200() throws Exception {

        FdResponse response = new FdResponse(
                1L,
                "FD123456",
                "John Doe",
                "1234567890",
                new BigDecimal("50000.00"),
                new BigDecimal("7.50"),
                3,
                LocalDate.of(2026, 9, 2),
                LocalDate.of(2029, 9, 2),
                LocalDate.of(2027, 1, 10),
                new BigDecimal("61250.00"),
                new BigDecimal("52000.00"),
                "CLOSED"
        );

        when(fdService.closeFixedDeposit("FD123456"))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/fd/FD123456/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fdNumber").value("FD123456"))
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.creditedAmount").value(52000.00));

        verify(fdService).closeFixedDeposit("FD123456");
    }

    @Test
    void calculateMaturity_whenDepositAmountMissing_shouldReturn400() throws Exception {

        mockMvc.perform(post("/api/v1/fd/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenureYears": 3
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fdService);
    }

    @Test
    void calculateMaturity_whenDepositAmountTooLow_shouldReturn400() throws Exception {

        mockMvc.perform(post("/api/v1/fd/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "depositAmount": 10000.00,
                                  "tenureYears": 3
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fdService);
    }

    @Test
    void calculateMaturity_whenTenureMissing_shouldReturn400() throws Exception {

        mockMvc.perform(post("/api/v1/fd/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "depositAmount": 50000.00
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fdService);
    }

    @Test
    void createFixedDeposit_whenSourceAccountMissing_shouldReturn400() throws Exception {

        mockMvc.perform(post("/api/v1/fd/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "depositAmount": 50000.00,
                                  "tenureYears": 3
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fdService);
    }

    @Test
    void createFixedDeposit_whenDepositAmountTooLow_shouldReturn400() throws Exception {

        mockMvc.perform(post("/api/v1/fd/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountNumber": "1234567890",
                                  "depositAmount": 9999.99,
                                  "tenureYears": 3
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fdService);
    }

    @Test
    void createFixedDeposit_whenTenureMissing_shouldReturn400() throws Exception {

        mockMvc.perform(post("/api/v1/fd/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountNumber": "1234567890",
                                  "depositAmount": 50000.00
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fdService);
    }
}