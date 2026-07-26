package com.bankflow.service;

import com.bankflow.dto.DashboardSummaryResponse;
import com.bankflow.entity.Account;
import com.bankflow.entity.FixedDeposit;
import com.bankflow.entity.Loan;
import com.bankflow.entity.Transaction;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.FixedDepositRepository;
import com.bankflow.repository.LoanRepository;
import com.bankflow.repository.TransactionRepository;
import com.bankflow.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private FixedDepositRepository fdRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DashboardService dashboardService;

    private User mockUser;
    private Account mockAccount;
    private FixedDeposit mockFd;
    private Loan mockLoan;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .role(User.Role.CUSTOMER)
                .build();

        mockAccount = Account.builder()
                .id(10L)
                .accountNumber("BF1234567890")
                .user(mockUser)
                .currentBalance(new BigDecimal("50000.00"))
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build();

        mockFd = FixedDeposit.builder()
                .id(100L)
                .depositAmount(new BigDecimal("30000.00"))
                .status(FixedDeposit.FdStatus.ACTIVE)
                .build();

        mockLoan = Loan.builder()
                .id(200L)
                .loanNumber("LN-A1B2C3D4")
                .remainingBalance(new BigDecimal("100000.00"))
                .monthlyEmi(new BigDecimal("8721.98"))
                .nextDueDate(LocalDate.of(2026, 8, 27))
                .status(Loan.LoanStatus.ACTIVE)
                .build();

        mockTransaction = Transaction.builder()
                .id(1000L)
                .transactionId("TX-12345")
                .account(mockAccount)
                .transactionType(Transaction.TransactionType.CREDIT)
                .amount(new BigDecimal("5000.00"))
                .availableBalance(new BigDecimal("50000.00"))
                .description("Salary Deposit")
                .transactionDate(LocalDateTime.now())
                .build();

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser() {
        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
    }

    @Test
    @DisplayName("Get Dashboard Summary - Aggregates All Financial Data Correctly")
    void getDashboardSummary_Success() {
        mockAuthenticatedUser();

        when(accountRepository.findByUserId(mockUser.getId())).thenReturn(List.of(mockAccount));
        when(fdRepository.findByUserId(mockUser.getId())).thenReturn(List.of(mockFd));
        when(loanRepository.findByUserId(mockUser.getId())).thenReturn(List.of(mockLoan));
        when(transactionRepository.findByAccountIdInOrderByTransactionDateDesc(eq(List.of(10L)), any(Pageable.class)))
                .thenReturn(List.of(mockTransaction));

        DashboardSummaryResponse response = dashboardService.getDashboardSummary();

        assertNotNull(response);
        assertEquals("John Doe", response.customerName());
        assertEquals(new BigDecimal("80000.00"), response.totalNetWorth()); // Balance (50,000) + FD (30,000)
        assertEquals(new BigDecimal("50000.00"), response.totalAccountBalance());
        assertEquals(new BigDecimal("30000.00"), response.totalFdInvestment());
        assertEquals(new BigDecimal("100000.00"), response.totalOutstandingLoans());

        assertEquals(1, response.activeAccountsCount());
        assertEquals(1, response.activeFdCount());
        assertEquals(1, response.activeLoanCount());

        assertEquals(LocalDate.of(2026, 8, 27), response.nextEmiDueDate());
        assertEquals(new BigDecimal("8721.98"), response.nextEmiAmount());

        assertEquals(1, response.recentTransactions().size());
        assertEquals("TX-12345", response.recentTransactions().get(0).transactionId());
        assertEquals("BF1234567890", response.recentTransactions().get(0).accountNumber());
    }

    @Test
    @DisplayName("Get Dashboard Summary - Handles Zero Accounts/Loans/FDs Gracefully")
    void getDashboardSummary_EmptyState() {
        mockAuthenticatedUser();

        when(accountRepository.findByUserId(mockUser.getId())).thenReturn(Collections.emptyList());
        when(fdRepository.findByUserId(mockUser.getId())).thenReturn(Collections.emptyList());
        when(loanRepository.findByUserId(mockUser.getId())).thenReturn(Collections.emptyList());

        DashboardSummaryResponse response = dashboardService.getDashboardSummary();

        assertNotNull(response);
        assertEquals("John Doe", response.customerName());
        assertEquals(BigDecimal.ZERO, response.totalNetWorth());
        assertEquals(BigDecimal.ZERO, response.totalAccountBalance());
        assertEquals(BigDecimal.ZERO, response.totalFdInvestment());
        assertEquals(BigDecimal.ZERO, response.totalOutstandingLoans());

        assertEquals(0, response.activeAccountsCount());
        assertEquals(0, response.activeFdCount());
        assertEquals(0, response.activeLoanCount());

        assertNull(response.nextEmiDueDate());
        assertNull(response.nextEmiAmount());
        assertTrue(response.recentTransactions().isEmpty());

        verify(transactionRepository, never()).findByAccountIdInOrderByTransactionDateDesc(any(), any());
    }
}
