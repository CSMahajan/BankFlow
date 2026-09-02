package com.bankflow.controller;

import com.bankflow.config.SecurityConfig;
import com.bankflow.dto.FundTransferResponse;
import com.bankflow.exception.GlobalExceptionHandler;
import com.bankflow.filter.JwtAuthenticationFilter;
import com.bankflow.filter.RateLimitFilter;
import com.bankflow.filter.UserRateLimitFilter;
import com.bankflow.service.FundTransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = FundTransferController.class,
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
class FundTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FundTransferService fundTransferService;

    @Test
    void transferFunds_shouldReturn200() throws Exception {

        FundTransferResponse response = new FundTransferResponse(
                "TXN123456",
                "1234567890",
                "0987654321",
                new BigDecimal("5000.00"),
                new BigDecimal("15000.00"),
                "SUCCESS",
                LocalDateTime.of(2026, 9, 2, 10, 30)
        );

        when(fundTransferService.transferFunds(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountNumber": "1234567890",
                                  "targetAccountNumber": "0987654321",
                                  "amount": 5000.00,
                                  "remark": "Test transfer"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference").value("TXN123456"))
                .andExpect(jsonPath("$.sourceAccountNumber").value("1234567890"))
                .andExpect(jsonPath("$.targetAccountNumber").value("0987654321"))
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.remainingBalance").value(15000.00))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(fundTransferService).transferFunds(any());
    }

    @Test
    void transferFunds_whenSourceAccountMissing_shouldReturn400() throws Exception {

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetAccountNumber": "0987654321",
                                  "amount": 5000.00,
                                  "remark": "Test transfer"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fundTransferService);
    }

    @Test
    void transferFunds_whenTargetAccountMissing_shouldReturn400() throws Exception {

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountNumber": "1234567890",
                                  "amount": 5000.00,
                                  "remark": "Test transfer"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fundTransferService);
    }

    @Test
    void transferFunds_whenAmountMissing_shouldReturn400() throws Exception {

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountNumber": "1234567890",
                                  "targetAccountNumber": "0987654321",
                                  "remark": "Test transfer"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fundTransferService);
    }

    @Test
    void transferFunds_whenAmountBelowMinimum_shouldReturn400() throws Exception {

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountNumber": "1234567890",
                                  "targetAccountNumber": "0987654321",
                                  "amount": 0.50,
                                  "remark": "Test transfer"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fundTransferService);
    }
}