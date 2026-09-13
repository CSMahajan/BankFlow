package com.bankflow.controller;

import com.bankflow.config.SecurityConfig;
import com.bankflow.dto.ScheduledTransferResponse;
import com.bankflow.exception.GlobalExceptionHandler;
import com.bankflow.filter.JwtAuthenticationFilter;
import com.bankflow.filter.RateLimitFilter;
import com.bankflow.filter.UserRateLimitFilter;
import com.bankflow.service.ScheduledTransferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ScheduledTransferController.class,
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
class ScheduledTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduledTransferService scheduledTransferService;

    @Test
    void createScheduledTransfer_shouldReturn201() throws Exception {

        ScheduledTransferResponse response = new ScheduledTransferResponse(
                1L,
                "1234567890",
                "0987654321",
                new BigDecimal("5000.00"),
                "Monthly transfer",
                com.bankflow.entity.ScheduledTransfer.Frequency.MONTHLY,
                com.bankflow.entity.ScheduledTransfer.TransferStatus.ACTIVE,
                LocalDate.of(2026, 10, 2),
                LocalDateTime.of(2026, 9, 2, 10, 30)
        );

        when(scheduledTransferService.createScheduledTransfer(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/scheduled-transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountNumber": "1234567890",
                                  "recipientAccountNumber": "0987654321",
                                  "amount": 5000.00,
                                  "description": "Monthly transfer",
                                  "frequency": "MONTHLY",
                                  "startDate": "2026-10-02"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sourceAccountNumber").value("1234567890"))
                .andExpect(jsonPath("$.recipientAccountNumber").value("0987654321"))
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.description").value("Monthly transfer"))
                .andExpect(jsonPath("$.frequency").value("MONTHLY"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.nextExecutionDate").value("2026-10-02"))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(scheduledTransferService).createScheduledTransfer(any());
    }

    @Test
    void getMyScheduledTransfers_shouldReturn200() throws Exception {

        ScheduledTransferResponse response = new ScheduledTransferResponse(
                1L,
                "1234567890",
                "0987654321",
                new BigDecimal("5000.00"),
                "Monthly transfer",
                com.bankflow.entity.ScheduledTransfer.Frequency.MONTHLY,
                com.bankflow.entity.ScheduledTransfer.TransferStatus.ACTIVE,
                LocalDate.of(2026, 10, 2),
                LocalDateTime.of(2026, 9, 2, 10, 30)
        );

        when(scheduledTransferService.getMyScheduledTransfers())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/scheduled-transfers/my-transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].sourceAccountNumber").value("1234567890"))
                .andExpect(jsonPath("$[0].recipientAccountNumber").value("0987654321"))
                .andExpect(jsonPath("$[0].amount").value(5000.00))
                .andExpect(jsonPath("$[0].frequency").value("MONTHLY"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(scheduledTransferService).getMyScheduledTransfers();
    }

    @Test
    void cancelScheduledTransfer_shouldReturn200() throws Exception {

        ScheduledTransferResponse response = new ScheduledTransferResponse(
                1L,
                "1234567890",
                "0987654321",
                new BigDecimal("5000.00"),
                "Monthly transfer",
                com.bankflow.entity.ScheduledTransfer.Frequency.MONTHLY,
                com.bankflow.entity.ScheduledTransfer.TransferStatus.CANCELLED,
                LocalDate.of(2026, 10, 2),
                LocalDateTime.of(2026, 9, 2, 10, 30)
        );

        when(scheduledTransferService.cancelScheduledTransfer(1L))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/scheduled-transfers/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(scheduledTransferService).cancelScheduledTransfer(1L);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidScheduledTransferRequests")
    void createScheduledTransfer_whenInvalidRequest_shouldReturn400(
            String testCase,
            String requestBody
    ) throws Exception {

        mockMvc.perform(post("/api/v1/scheduled-transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(scheduledTransferService);
    }

    private static Stream<Arguments> invalidScheduledTransferRequests() {
        return Stream.of(
                Arguments.of(
                        "Source account missing",
                        """
                        {
                          "recipientAccountNumber": "0987654321",
                          "amount": 5000.00,
                          "frequency": "MONTHLY",
                          "startDate": "2026-10-02"
                        }
                        """
                ),
                Arguments.of(
                        "Recipient account missing",
                        """
                        {
                          "sourceAccountNumber": "1234567890",
                          "amount": 5000.00,
                          "frequency": "MONTHLY",
                          "startDate": "2026-10-02"
                        }
                        """
                ),
                Arguments.of(
                        "Amount missing",
                        """
                        {
                          "sourceAccountNumber": "1234567890",
                          "recipientAccountNumber": "0987654321",
                          "frequency": "MONTHLY",
                          "startDate": "2026-10-02"
                        }
                        """
                ),
                Arguments.of(
                        "Amount below minimum",
                        """
                        {
                          "sourceAccountNumber": "1234567890",
                          "recipientAccountNumber": "0987654321",
                          "amount": 0.50,
                          "frequency": "MONTHLY",
                          "startDate": "2026-10-02"
                        }
                        """
                ),
                Arguments.of(
                        "Frequency missing",
                        """
                        {
                          "sourceAccountNumber": "1234567890",
                          "recipientAccountNumber": "0987654321",
                          "amount": 5000.00,
                          "startDate": "2026-10-02"
                        }
                        """
                ),
                Arguments.of(
                        "Start date missing",
                        """
                        {
                          "sourceAccountNumber": "1234567890",
                          "recipientAccountNumber": "0987654321",
                          "amount": 5000.00,
                          "frequency": "MONTHLY"
                        }
                        """
                )
        );
    }
}