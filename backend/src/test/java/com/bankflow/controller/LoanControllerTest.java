package com.bankflow.controller;

import com.bankflow.config.SecurityConfig;
import com.bankflow.dto.*;
import com.bankflow.entity.Loan;
import com.bankflow.exception.GlobalExceptionHandler;
import com.bankflow.filter.JwtAuthenticationFilter;
import com.bankflow.filter.RateLimitFilter;
import com.bankflow.filter.UserRateLimitFilter;
import com.bankflow.service.LoanService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = LoanController.class,
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
class LoanControllerTest {

    @Resource
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;

    // ---------------------------------------------------------
    // POST /api/v1/loans/apply
    // ---------------------------------------------------------

    @Test
    void applyForLoan_shouldReturnCreated() throws Exception {

        LoanResponse response = sampleLoanResponse();

        when(loanService.applyForLoan(any(ApplyLoanRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/loans/apply")
                        .contentType("application/json")
                        .content("""
                                {
                                  "accountNumber": "1234567890",
                                  "loanType": "PERSONAL",
                                  "principalAmount": 500000.00,
                                  "tenureMonths": 24
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanNumber").value("LN10001"))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.loanType").value("PERSONAL"))
                .andExpect(jsonPath("$.principalAmount").value(500000.00))
                .andExpect(jsonPath("$.tenureMonths").value(24))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(loanService).applyForLoan(any(ApplyLoanRequest.class));
    }

    @Test
    void applyForLoan_shouldRejectMissingAccountNumber() throws Exception {

        mockMvc.perform(post("/api/v1/loans/apply")
                        .contentType("application/json")
                        .content("""
                                {
                                  "loanType": "PERSONAL",
                                  "principalAmount": 500000.00,
                                  "tenureMonths": 24
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(loanService);
    }

    @Test
    void applyForLoan_shouldRejectMissingLoanType() throws Exception {

        mockMvc.perform(post("/api/v1/loans/apply")
                        .contentType("application/json")
                        .content("""
                                {
                                  "accountNumber": "1234567890",
                                  "principalAmount": 500000.00,
                                  "tenureMonths": 24
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(loanService);
    }

    @Test
    void applyForLoan_shouldRejectPrincipalBelowMinimum() throws Exception {

        mockMvc.perform(post("/api/v1/loans/apply")
                        .contentType("application/json")
                        .content("""
                                {
                                  "accountNumber": "1234567890",
                                  "loanType": "PERSONAL",
                                  "principalAmount": 9999.99,
                                  "tenureMonths": 24
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(loanService);
    }

    @Test
    void applyForLoan_shouldRejectTenureBelowMinimum() throws Exception {

        mockMvc.perform(post("/api/v1/loans/apply")
                        .contentType("application/json")
                        .content("""
                                {
                                  "accountNumber": "1234567890",
                                  "loanType": "PERSONAL",
                                  "principalAmount": 500000.00,
                                  "tenureMonths": 5
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(loanService);
    }

    // ---------------------------------------------------------
    // POST /api/v1/loans/pay-emi
    // ---------------------------------------------------------

    @Test
    void payEmi_shouldReturnOk() throws Exception {

        RepaymentResponse response = new RepaymentResponse(
                1L,
                "LN10001",
                new BigDecimal("25000.00"),
                new BigDecimal("20000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("480000.00"),
                LocalDateTime.now(),
                "TXN10001"
        );

        when(loanService.payEmi(any(PayEmiRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/loans/pay-emi")
                        .contentType("application/json")
                        .content("""
                                {
                                  "loanNumber": "LN10001",
                                  "sourceAccountNumber": "1234567890"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanNumber").value("LN10001"))
                .andExpect(jsonPath("$.amountPaid").value(25000.00))
                .andExpect(jsonPath("$.principalComponent").value(20000.00))
                .andExpect(jsonPath("$.interestComponent").value(5000.00))
                .andExpect(jsonPath("$.transactionReference").value("TXN10001"));

        verify(loanService).payEmi(any(PayEmiRequest.class));
    }

    @Test
    void payEmi_shouldRejectMissingLoanNumber() throws Exception {

        mockMvc.perform(post("/api/v1/loans/pay-emi")
                        .contentType("application/json")
                        .content("""
                                {
                                  "sourceAccountNumber": "1234567890"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(loanService);
    }

    @Test
    void payEmi_shouldRejectMissingSourceAccountNumber() throws Exception {

        mockMvc.perform(post("/api/v1/loans/pay-emi")
                        .contentType("application/json")
                        .content("""
                                {
                                  "loanNumber": "LN10001"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(loanService);
    }

    // ---------------------------------------------------------
    // GET /api/v1/loans/my-loans
    // ---------------------------------------------------------

    @Test
    void getMyLoans_shouldReturnLoans() throws Exception {

        List<LoanResponse> loans = List.of(sampleLoanResponse());

        when(loanService.getMyLoans()).thenReturn(loans);

        mockMvc.perform(get("/api/v1/loans/my-loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].loanNumber").value("LN10001"))
                .andExpect(jsonPath("$[0].loanType").value("PERSONAL"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(loanService).getMyLoans();
    }

    // ---------------------------------------------------------
    // GET /api/v1/loans/{loanNumber}/repayments
    // ---------------------------------------------------------

    @Test
    void getRepaymentHistory_shouldReturnHistory() throws Exception {

        RepaymentResponse repayment = new RepaymentResponse(
                1L,
                "LN10001",
                new BigDecimal("25000.00"),
                new BigDecimal("20000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("480000.00"),
                LocalDateTime.now(),
                "TXN10001"
        );

        when(loanService.getRepaymentHistory("LN10001"))
                .thenReturn(List.of(repayment));

        mockMvc.perform(get("/api/v1/loans/LN10001/repayments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].loanNumber").value("LN10001"))
                .andExpect(jsonPath("$[0].amountPaid").value(25000.00))
                .andExpect(jsonPath("$[0].transactionReference").value("TXN10001"));

        verify(loanService).getRepaymentHistory("LN10001");
    }

    // ---------------------------------------------------------
    // GET /api/v1/loans/pending
    // ---------------------------------------------------------

    @Test
    void getPendingLoans_shouldReturnPendingLoans() throws Exception {

        LoanResponse response = sampleLoanResponse();

        PageImpl<LoanResponse> page = new PageImpl<>(
                List.of(response),
                PageRequest.of(
                        0,
                        10,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                ),
                1
        );

        when(loanService.getPendingLoans(
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/loans/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].loanNumber").value("LN10001"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10));

        verify(loanService).getPendingLoans(
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    void getPendingLoans_shouldPassSearchAndLoanType() throws Exception {

        PageImpl<LoanResponse> page = new PageImpl<>(
                List.of(sampleLoanResponse()),
                PageRequest.of(1, 5),
                6
        );

        when(loanService.getPendingLoans(
                eq("john"),
                eq(Loan.LoanType.HOME),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/loans/pending")
                        .param("search", "john")
                        .param("loanType", "HOME")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].loanType").value("PERSONAL"));

        verify(loanService).getPendingLoans(
                eq("john"),
                eq(Loan.LoanType.HOME),
                any(Pageable.class)
        );
    }

    // ---------------------------------------------------------
    // PUT /api/v1/loans/{loanId}/approve
    // ---------------------------------------------------------

    @Test
    void approveAndDisburseLoan_shouldReturnOk() throws Exception {

        LoanResponse response = sampleLoanResponse();

        when(loanService.approveAndDisburseLoan(1L))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/loans/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanNumber").value("LN10001"));

        verify(loanService).approveAndDisburseLoan(1L);
    }

    // ---------------------------------------------------------
    // PUT /api/v1/loans/{loanId}/reject
    // ---------------------------------------------------------

    @Test
    void rejectLoan_shouldReturnOk() throws Exception {

        LoanResponse response = sampleLoanResponse();

        when(loanService.rejectLoan(
                eq(1L),
                any(RejectLoanRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/v1/loans/1/reject")
                        .contentType("application/json")
                        .content("""
                                {
                                  "remarks": "Insufficient documentation"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanNumber").value("LN10001"));

        verify(loanService).rejectLoan(
                eq(1L),
                any(RejectLoanRequest.class)
        );
    }

    // ---------------------------------------------------------
    // Helper
    // ---------------------------------------------------------

    private LoanResponse sampleLoanResponse() {

        return new LoanResponse(
                1L,
                "LN10001",
                "1234567890",
                "John Doe",
                Loan.LoanType.PERSONAL,
                new BigDecimal("500000.00"),
                new BigDecimal("10.50"),
                24,
                new BigDecimal("23000.00"),
                new BigDecimal("500000.00"),
                Loan.LoanStatus.PENDING,
                null,
                LocalDate.now(),
                null,
                null
        );
    }
}