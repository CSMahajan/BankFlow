package com.bankflow.controller;

import com.bankflow.config.SecurityConfig;
import com.bankflow.dto.AccountDashboardSummary;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.entity.Transaction;
import com.bankflow.exception.GlobalExceptionHandler;
import com.bankflow.filter.JwtAuthenticationFilter;
import com.bankflow.filter.RateLimitFilter;
import com.bankflow.filter.UserRateLimitFilter;
import com.bankflow.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TransactionController.class,
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
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void getDashboardSummary_shouldReturnSummary() throws Exception {

        TransactionResponse transaction = new TransactionResponse(
                "TXN-001",
                "1234567890",
                LocalDateTime.of(2026, 8, 31, 10, 30),
                Transaction.TransactionType.CREDIT,
                new BigDecimal("5000.00"),
                new BigDecimal("25000.00"),
                "Salary credit"
        );

        AccountDashboardSummary response = new AccountDashboardSummary(
                "1234567890",
                new BigDecimal("25000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("1000.00"),
                List.of(transaction)
        );

        when(transactionService.getDashboardSummary("1234567890"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/dashboard/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.currentBalance").value(25000.00))
                .andExpect(jsonPath("$.totalCreditAmount").value(5000.00))
                .andExpect(jsonPath("$.totalDebitAmount").value(1000.00))
                .andExpect(jsonPath("$.recentTransactions[0].transactionId").value("TXN-001"))
                .andExpect(jsonPath("$.recentTransactions[0].transactionType").value("CREDIT"));

        verify(transactionService).getDashboardSummary("1234567890");
    }

    @Test
    void getMyTransactions_shouldUseDefaultPageableAndFilters() throws Exception {

        TransactionResponse transaction = new TransactionResponse(
                "TXN-001",
                "1234567890",
                LocalDateTime.of(2026, 8, 31, 10, 30),
                Transaction.TransactionType.CREDIT,
                new BigDecimal("5000.00"),
                new BigDecimal("25000.00"),
                "Salary credit"
        );

        PageImpl<TransactionResponse> page =
                new PageImpl<>(List.of(transaction), PageRequest.of(
                        0,
                        20,
                        Sort.by(Sort.Direction.DESC, "transactionDate")
                ), 1);

        when(transactionService.getMyTransactions(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/transactions/my-transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].transactionId").value("TXN-001"))
                .andExpect(jsonPath("$.content[0].accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.content[0].transactionType").value("CREDIT"))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(20));

        verify(transactionService).getMyTransactions(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                argThat(pageable ->
                        pageable.getPageNumber() == 0
                                && pageable.getPageSize() == 20
                                && pageable.getSort().getOrderFor("transactionDate")
                                != null
                                && pageable.getSort()
                                .getOrderFor("transactionDate")
                                .getDirection() == Sort.Direction.DESC
                )
        );
    }

    @Test
    void getMyTransactions_shouldAcceptFiltersAndPageable() throws Exception {

        PageImpl<TransactionResponse> page =
                new PageImpl<>(List.of());

        when(transactionService.getMyTransactions(
                eq("1234567890"),
                eq(Transaction.TransactionType.DEBIT),
                eq(LocalDate.of(2026, 8, 1)),
                eq(LocalDate.of(2026, 8, 31)),
                eq("grocery"),
                any()
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/transactions/my-transactions")
                        .param("accountNumber", "1234567890")
                        .param("type", "DEBIT")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31")
                        .param("search", "grocery")
                        .param("page", "2")
                        .param("size", "10")
                        .param("sort", "transactionDate,asc"))
                .andExpect(status().isOk());

        verify(transactionService).getMyTransactions(
                eq("1234567890"),
                eq(Transaction.TransactionType.DEBIT),
                eq(LocalDate.of(2026, 8, 1)),
                eq(LocalDate.of(2026, 8, 31)),
                eq("grocery"),
                argThat(pageable ->
                        pageable.getPageNumber() == 2
                                && pageable.getPageSize() == 10
                                && pageable.getSort().getOrderFor("transactionDate")
                                != null
                                && pageable.getSort()
                                .getOrderFor("transactionDate")
                                .getDirection() == Sort.Direction.ASC
                )
        );
    }

    @Test
    void exportTransactionsPdf_shouldReturnPdfFile() throws Exception {

        byte[] pdf = "fake-pdf-content".getBytes();

        when(transactionService.exportTransactionsPdf(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(pdf);

        mockMvc.perform(get("/api/v1/transactions/export/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=transaction-history.pdf"
                ))
                .andExpect(header().string(
                        "Content-Length",
                        String.valueOf(pdf.length)
                ))
                .andExpect(content().bytes(pdf));

        verify(transactionService).exportTransactionsPdf(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    void exportTransactionsExcel_shouldReturnExcelFile() throws Exception {

        byte[] excel = "fake-excel-content".getBytes();

        when(transactionService.exportTransactionsExcel(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(excel);

        mockMvc.perform(get("/api/v1/transactions/export/excel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=transaction-history.xlsx"
                ))
                .andExpect(header().string(
                        "Content-Length",
                        String.valueOf(excel.length)
                ))
                .andExpect(content().bytes(excel));

        verify(transactionService).exportTransactionsExcel(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    void getTransactionDetails_shouldReturnTransaction() throws Exception {

        TransactionResponse response = new TransactionResponse(
                "TXN-001",
                "1234567890",
                LocalDateTime.of(2026, 8, 31, 10, 30),
                Transaction.TransactionType.DEBIT,
                new BigDecimal("750.00"),
                new BigDecimal("24250.00"),
                "Grocery"
        );

        when(transactionService.getTransactionDetails("TXN-001"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/TXN-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-001"))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.transactionType").value("DEBIT"))
                .andExpect(jsonPath("$.amount").value(750.00))
                .andExpect(jsonPath("$.description").value("Grocery"));

        verify(transactionService).getTransactionDetails("TXN-001");
    }

    @Test
    void searchAccountTransactionsAdmin_shouldReturnTransactions() throws Exception {

        TransactionResponse response = new TransactionResponse(
                "TXN-001",
                "1234567890",
                LocalDateTime.of(2026, 8, 31, 10, 30),
                Transaction.TransactionType.CREDIT,
                new BigDecimal("5000.00"),
                new BigDecimal("25000.00"),
                "Salary credit"
        );

        when(transactionService.getAllTransactionsForAdmin("1234567890"))
                .thenReturn(List.of(response));

        mockMvc.perform(get(
                "/api/v1/transactions/admin/search/1234567890"
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value("TXN-001"))
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"));

        verify(transactionService)
                .getAllTransactionsForAdmin("1234567890");
    }

    @Test
    void getAccountTransactionsForAdmin_shouldReturnPagedTransactions() throws Exception {

        TransactionResponse response = new TransactionResponse(
                "TXN-001",
                "1234567890",
                LocalDateTime.of(2026, 8, 31, 10, 30),
                Transaction.TransactionType.DEBIT,
                new BigDecimal("1000.00"),
                new BigDecimal("24000.00"),
                "ATM withdrawal"
        );

        PageImpl<TransactionResponse> page =
                new PageImpl<>(List.of(response));

        when(transactionService.getAccountTransactionsForAdmin(
                eq("1234567890"),
                any()
        )).thenReturn(page);

        mockMvc.perform(get(
                        "/api/v1/transactions/admin/accounts/1234567890/transactions"
                )
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "transactionDate,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].transactionId").value("TXN-001"))
                .andExpect(jsonPath("$.content[0].transactionType").value("DEBIT"));

        verify(transactionService).getAccountTransactionsForAdmin(
                eq("1234567890"),
                argThat(pageable ->
                        pageable.getPageNumber() == 1
                                && pageable.getPageSize() == 5
                                && pageable.getSort().getOrderFor("transactionDate")
                                != null
                                && pageable.getSort()
                                .getOrderFor("transactionDate")
                                .getDirection() == Sort.Direction.ASC
                )
        );
    }

}