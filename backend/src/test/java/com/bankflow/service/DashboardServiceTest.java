package com.bankflow.service;

import com.bankflow.dto.AdminDashboardSummaryResponse;
import com.bankflow.dto.DashboardSummaryResponse;
import com.bankflow.dto.MonthlyAnalyticsResponse;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.entity.Account;
import com.bankflow.entity.FixedDeposit;
import com.bankflow.entity.KycDocument;
import com.bankflow.entity.Loan;
import com.bankflow.entity.Transaction;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.FixedDepositRepository;
import com.bankflow.repository.KycDocumentRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import static org.mockito.ArgumentMatchers.*;
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
    private KycDocumentRepository kycDocumentRepository;

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
                .accountType(Account.AccountType.SAVINGS)
                .branchName("Mumbai Main")
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
                .transactionDate(LocalDateTime.of(2026, 8, 20, 10, 30))
                .build();

        lenient()
                .when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser() {

        when(authentication.getName())
                .thenReturn(mockUser.getEmail());

        when(userRepository.findByEmail(mockUser.getEmail()))
                .thenReturn(Optional.of(mockUser));
    }

    // =========================================================
    // getDashboardSummary()
    // =========================================================

    @Test
    @DisplayName("Get Dashboard Summary - Aggregates All Financial Data Correctly")
    void getDashboardSummary_Success() {

        mockAuthenticatedUser();

        when(accountRepository.findByUserId(mockUser.getId()))
                .thenReturn(List.of(mockAccount));

        when(fdRepository.findByUserId(mockUser.getId()))
                .thenReturn(List.of(mockFd));

        when(loanRepository.findByUserId(mockUser.getId()))
                .thenReturn(List.of(mockLoan));

        when(transactionRepository.findByAccountIdInOrderByTransactionDateDesc(
                eq(List.of(10L)),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(mockTransaction)));

        DashboardSummaryResponse response =
                dashboardService.getDashboardSummary();

        assertNotNull(response);

        assertEquals("John Doe", response.customerName());

        assertEquals(
                new BigDecimal("80000.00"),
                response.totalNetWorth()
        );

        assertEquals(
                new BigDecimal("50000.00"),
                response.totalAccountBalance()
        );

        assertEquals(
                new BigDecimal("30000.00"),
                response.totalFdInvestment()
        );

        assertEquals(
                new BigDecimal("100000.00"),
                response.totalOutstandingLoans()
        );

        assertEquals(1, response.activeAccountsCount());
        assertEquals(1, response.activeFdCount());
        assertEquals(1, response.activeLoanCount());

        assertEquals(
                LocalDate.of(2026, 8, 27),
                response.nextEmiDueDate()
        );

        assertEquals(
                new BigDecimal("8721.98"),
                response.nextEmiAmount()
        );

        assertNotNull(response.recentTransactions());
        assertEquals(1, response.recentTransactions().size());

        TransactionResponse transaction =
                response.recentTransactions().get(0);

        assertEquals("TX-12345", transaction.transactionId());
        assertEquals("BF1234567890", transaction.accountNumber());
        assertEquals(
                Transaction.TransactionType.CREDIT,
                transaction.transactionType()
        );
        assertEquals(
                new BigDecimal("5000.00"),
                transaction.amount()
        );
        assertEquals(
                new BigDecimal("50000.00"),
                transaction.availableBalance()
        );
        assertEquals("Salary Deposit", transaction.description());
    }

    @Test
    @DisplayName("Get Dashboard Summary - Handles Empty Financial Data")
    void getDashboardSummary_EmptyState() {

        mockAuthenticatedUser();

        when(accountRepository.findByUserId(mockUser.getId()))
                .thenReturn(Collections.emptyList());

        when(fdRepository.findByUserId(mockUser.getId()))
                .thenReturn(Collections.emptyList());

        when(loanRepository.findByUserId(mockUser.getId()))
                .thenReturn(Collections.emptyList());

        DashboardSummaryResponse response =
                dashboardService.getDashboardSummary();

        assertNotNull(response);

        assertEquals("John Doe", response.customerName());

        assertEquals(
                BigDecimal.ZERO,
                response.totalNetWorth()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.totalAccountBalance()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.totalFdInvestment()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.totalOutstandingLoans()
        );

        assertEquals(0, response.activeAccountsCount());
        assertEquals(0, response.activeFdCount());
        assertEquals(0, response.activeLoanCount());

        assertNull(response.nextEmiDueDate());
        assertNull(response.nextEmiAmount());

        assertTrue(response.recentTransactions().isEmpty());

        verify(
                transactionRepository,
                never()
        ).findByAccountIdInOrderByTransactionDateDesc(any(), any());
    }

    @Test
    @DisplayName("Get Dashboard Summary - Ignores Inactive Fixed Deposits and Loans")
    void getDashboardSummary_IgnoresInactiveProducts() {

        mockAuthenticatedUser();

        FixedDeposit inactiveFd = FixedDeposit.builder()
                .id(101L)
                .depositAmount(new BigDecimal("50000.00"))
                .status(FixedDeposit.FdStatus.MATURED_CLOSED)
                .build();

        Loan inactiveLoan = Loan.builder()
                .id(201L)
                .remainingBalance(new BigDecimal("75000.00"))
                .monthlyEmi(new BigDecimal("5000.00"))
                .status(Loan.LoanStatus.PAID_OFF)
                .build();

        when(accountRepository.findByUserId(mockUser.getId()))
                .thenReturn(List.of(mockAccount));

        when(fdRepository.findByUserId(mockUser.getId()))
                .thenReturn(List.of(mockFd, inactiveFd));

        when(loanRepository.findByUserId(mockUser.getId()))
                .thenReturn(List.of(mockLoan, inactiveLoan));

        when(transactionRepository.findByAccountIdInOrderByTransactionDateDesc(
                eq(List.of(10L)),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        DashboardSummaryResponse response =
                dashboardService.getDashboardSummary();

        assertEquals(
                new BigDecimal("30000.00"),
                response.totalFdInvestment()
        );

        assertEquals(
                new BigDecimal("100000.00"),
                response.totalOutstandingLoans()
        );

        assertEquals(1, response.activeFdCount());
        assertEquals(1, response.activeLoanCount());
    }

    @Test
    @DisplayName("Get Dashboard Summary - Handles Null Account Balance")
    void getDashboardSummary_NullAccountBalance() {

        mockAuthenticatedUser();

        mockAccount.setCurrentBalance(null);

        when(accountRepository.findByUserId(mockUser.getId()))
                .thenReturn(List.of(mockAccount));

        when(fdRepository.findByUserId(mockUser.getId()))
                .thenReturn(Collections.emptyList());

        when(loanRepository.findByUserId(mockUser.getId()))
                .thenReturn(Collections.emptyList());

        when(transactionRepository.findByAccountIdInOrderByTransactionDateDesc(
                eq(List.of(10L)),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        DashboardSummaryResponse response =
                dashboardService.getDashboardSummary();

        assertEquals(
                BigDecimal.ZERO,
                response.totalAccountBalance()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.totalNetWorth()
        );
    }

    @Test
    @DisplayName("Get Dashboard Summary - Selects Loan With Earliest EMI Due Date")
    void getDashboardSummary_SelectsEarliestEmi() {

        mockAuthenticatedUser();

        Loan laterLoan = Loan.builder()
                .id(201L)
                .remainingBalance(new BigDecimal("50000.00"))
                .monthlyEmi(new BigDecimal("4000.00"))
                .nextDueDate(LocalDate.of(2026, 9, 15))
                .status(Loan.LoanStatus.ACTIVE)
                .build();

        when(accountRepository.findByUserId(mockUser.getId()))
                .thenReturn(List.of(mockAccount));

        when(fdRepository.findByUserId(mockUser.getId()))
                .thenReturn(Collections.emptyList());

        when(loanRepository.findByUserId(mockUser.getId()))
                .thenReturn(List.of(laterLoan, mockLoan));

        when(transactionRepository.findByAccountIdInOrderByTransactionDateDesc(
                eq(List.of(10L)),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        DashboardSummaryResponse response =
                dashboardService.getDashboardSummary();

        assertEquals(
                LocalDate.of(2026, 8, 27),
                response.nextEmiDueDate()
        );

        assertEquals(
                new BigDecimal("8721.98"),
                response.nextEmiAmount()
        );
    }

    // =========================================================
    // getCurrentMonthAnalytics()
    // =========================================================

    @Test
    @DisplayName("Get Current Month Analytics - Calculates Income Expense And Net Cash Flow")
    void getCurrentMonthAnalytics_Success() {

        mockAuthenticatedUser();

        when(accountRepository.findByUserId(mockUser.getId()))
                .thenReturn(List.of(mockAccount));

        when(transactionRepository
                .sumAmountByAccountIdsAndTypeAndDateRange(
                        eq(List.of(10L)),
                        eq(Transaction.TransactionType.CREDIT),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                ))
                .thenReturn(new BigDecimal("75000.00"));

        when(transactionRepository
                .sumAmountByAccountIdsAndTypeAndDateRange(
                        eq(List.of(10L)),
                        eq(Transaction.TransactionType.DEBIT),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                ))
                .thenReturn(new BigDecimal("25000.00"));

        MonthlyAnalyticsResponse response =
                dashboardService.getCurrentMonthAnalytics();

        assertNotNull(response);

        assertEquals(
                new BigDecimal("75000.00"),
                response.totalIncome()
        );

        assertEquals(
                new BigDecimal("25000.00"),
                response.totalExpense()
        );

        assertEquals(
                new BigDecimal("50000.00"),
                response.netCashFlow()
        );

        verify(
                transactionRepository,
                times(2)
        ).sumAmountByAccountIdsAndTypeAndDateRange(
                anyList(),
                any(Transaction.TransactionType.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("Get Current Month Analytics - Returns Zero When User Has No Accounts")
    void getCurrentMonthAnalytics_NoAccounts() {

        mockAuthenticatedUser();

        when(accountRepository.findByUserId(mockUser.getId()))
                .thenReturn(Collections.emptyList());

        MonthlyAnalyticsResponse response =
                dashboardService.getCurrentMonthAnalytics();

        assertNotNull(response);

        assertEquals(
                BigDecimal.ZERO,
                response.totalIncome()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.totalExpense()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.netCashFlow()
        );

        verify(
                transactionRepository,
                never()
        ).sumAmountByAccountIdsAndTypeAndDateRange(
                anyList(),
                any(),
                any(),
                any()
        );
    }

    // =========================================================
    // getAdminDashboardSummary()
    // =========================================================

    @Test
    @DisplayName("Get Admin Dashboard Summary - Aggregates All Counts And Deposits")
    void getAdminDashboardSummary_Success() {

        when(userRepository.count())
                .thenReturn(100L);

        when(accountRepository.count())
                .thenReturn(80L);

        when(loanRepository.countByStatus(Loan.LoanStatus.ACTIVE))
                .thenReturn(20L);

        when(loanRepository.countByStatus(Loan.LoanStatus.PENDING))
                .thenReturn(5L);

        when(fdRepository.countByStatus(FixedDeposit.FdStatus.ACTIVE))
                .thenReturn(15L);

        when(accountRepository.getTotalDeposits())
                .thenReturn(new BigDecimal("5000000.00"));

        when(kycDocumentRepository.countByKycVerificationStatus(
                KycDocument.KycVerificationStatus.PENDING
        )).thenReturn(8L);

        AdminDashboardSummaryResponse response =
                dashboardService.getAdminDashboardSummary();

        assertNotNull(response);

        assertEquals(100L, response.totalCustomers());
        assertEquals(80L, response.totalAccounts());
        assertEquals(20L, response.activeLoans());
        assertEquals(5L, response.pendingLoans());
        assertEquals(15L, response.activeFixedDeposits());

        assertEquals(
                new BigDecimal("5000000.00"),
                response.totalDeposits()
        );

        assertEquals(8L, response.pendingKycDocuments());
    }

    // =========================================================
    // getDashboardTransactions()
    // =========================================================

    @Test
    @DisplayName("Get Dashboard Transactions - Returns Paginated Transactions")
    void getDashboardTransactions_Success() {

        mockAuthenticatedUser();

        when(accountRepository.findByUserId(mockUser.getId()))
                .thenReturn(List.of(mockAccount));

        Page<Transaction> transactionPage =
                new PageImpl<>(List.of(mockTransaction));

        when(transactionRepository.findByAccountIdInOrderByTransactionDateDesc(
                eq(List.of(10L)),
                any(Pageable.class)
        )).thenReturn(transactionPage);

        Page<TransactionResponse> response =
                dashboardService.getDashboardTransactions(0, 5);

        assertNotNull(response);

        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());

        TransactionResponse transaction =
                response.getContent().get(0);

        assertEquals(
                "TX-12345",
                transaction.transactionId()
        );

        assertEquals(
                "BF1234567890",
                transaction.accountNumber()
        );

        assertEquals(
                Transaction.TransactionType.CREDIT,
                transaction.transactionType()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                transaction.amount()
        );

        verify(
                transactionRepository,
                times(1)
        ).findByAccountIdInOrderByTransactionDateDesc(
                eq(List.of(10L)),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("Get Dashboard Transactions - Returns Empty Page When User Has No Accounts")
    void getDashboardTransactions_NoAccounts() {

        mockAuthenticatedUser();

        when(accountRepository.findByUserId(mockUser.getId()))
                .thenReturn(Collections.emptyList());

        Page<TransactionResponse> response =
                dashboardService.getDashboardTransactions(0, 5);

        assertNotNull(response);

        assertTrue(response.isEmpty());
        assertEquals(0, response.getTotalElements());

        verify(
                transactionRepository,
                never()
        ).findByAccountIdInOrderByTransactionDateDesc(
                any(),
                any()
        );
    }

    // =========================================================
    // Authentication
    // =========================================================

    @Test
    @DisplayName("Dashboard - Throws Exception When Authenticated User Does Not Exist")
    void dashboard_AuthenticatedUserNotFound() {

        when(authentication.getName())
                .thenReturn("unknown@example.com");

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> dashboardService.getDashboardSummary()
        );

        verify(accountRepository, never()).findByUserId(any());
        verify(fdRepository, never()).findByUserId(any());
        verify(loanRepository, never()).findByUserId(any());
    }
}