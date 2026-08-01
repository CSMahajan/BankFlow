package com.bankflow.service;

import com.bankflow.dto.AccountDashboardSummary;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.entity.Account;
import com.bankflow.entity.Transaction;
import com.bankflow.entity.Transaction.TransactionType;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
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
import org.springframework.security.access.AccessDeniedException;
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
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransactionService transactionService;

    private User mockUser;
    private User mockOtherUser;
    private User mockAdminUser;
    private Account mockAccount;
    private Transaction mockDebitTx;
    private Transaction mockCreditTx;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .role(User.Role.CUSTOMER)
                .build();

        mockOtherUser = User.builder()
                .id(2L)
                .fullName("Jane Smith")
                .email("jane@example.com")
                .role(User.Role.CUSTOMER)
                .build();

        mockAdminUser = User.builder()
                .id(99L)
                .fullName("Admin User")
                .email("admin@bankflow.com")
                .role(User.Role.ADMIN)
                .build();

        mockAccount = Account.builder()
                .id(10L)
                .accountNumber("BF1000000001")
                .user(mockUser)
                .currentBalance(new BigDecimal("5000.00"))
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build();

        mockDebitTx = Transaction.builder()
                .id(100L)
                .transactionId("TX100")
                .account(mockAccount)
                .transactionDate(LocalDateTime.now().minusDays(1))
                .transactionType(TransactionType.DEBIT)
                .amount(new BigDecimal("500.00"))
                .availableBalance(new BigDecimal("5000.00"))
                .description("ATM Withdrawal")
                .build();

        mockCreditTx = Transaction.builder()
                .id(101L)
                .transactionId("TX101")
                .account(mockAccount)
                .transactionDate(LocalDateTime.now())
                .transactionType(TransactionType.CREDIT)
                .amount(new BigDecimal("2000.00"))
                .availableBalance(new BigDecimal("5500.00"))
                .description("Salary Deposit")
                .build();

        // Setup Spring Security Context Mock
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(User user) {
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    // ==========================================
    // DASHBOARD SUMMARY TESTS
    // ==========================================

    @Test
    @DisplayName("Get Dashboard Summary - Success With Non-Zero Totals")
    void getDashboardSummary_Success_WithTotals() {
        mockAuthenticatedUser(mockUser);
        when(accountRepository.findByAccountNumber("BF1000000001")).thenReturn(Optional.of(mockAccount));
        when(transactionRepository.findTop10ByAccountIdOrderByTransactionDateDesc(10L))
                .thenReturn(List.of(mockCreditTx, mockDebitTx));
        when(transactionRepository.sumAmountByAccountIdAndTransactionType(10L, TransactionType.CREDIT))
                .thenReturn(new BigDecimal("2000.00"));
        when(transactionRepository.sumAmountByAccountIdAndTransactionType(10L, TransactionType.DEBIT))
                .thenReturn(new BigDecimal("500.00"));

        AccountDashboardSummary summary = transactionService.getDashboardSummary("BF1000000001");

        assertNotNull(summary);
        assertEquals("BF1000000001", summary.accountNumber());
        assertEquals(new BigDecimal("5000.00"), summary.currentBalance());
        assertEquals(new BigDecimal("2000.00"), summary.totalCreditAmount());
        assertEquals(new BigDecimal("500.00"), summary.totalDebitAmount());
        assertEquals(2, summary.recentTransactions().size());
    }

    @Test
    @DisplayName("Get Dashboard Summary - Null Totals Default To Zero")
    void getDashboardSummary_NullTotals_DefaultsToZero() {
        mockAuthenticatedUser(mockUser);
        when(accountRepository.findByAccountNumber("BF1000000001")).thenReturn(Optional.of(mockAccount));
        when(transactionRepository.findTop10ByAccountIdOrderByTransactionDateDesc(10L))
                .thenReturn(Collections.emptyList());
        when(transactionRepository.sumAmountByAccountIdAndTransactionType(10L, TransactionType.CREDIT))
                .thenReturn(null);
        when(transactionRepository.sumAmountByAccountIdAndTransactionType(10L, TransactionType.DEBIT))
                .thenReturn(null);

        AccountDashboardSummary summary = transactionService.getDashboardSummary("BF1000000001");

        assertNotNull(summary);
        assertEquals(BigDecimal.ZERO, summary.totalCreditAmount());
        assertEquals(BigDecimal.ZERO, summary.totalDebitAmount());
        assertTrue(summary.recentTransactions().isEmpty());
    }

    @Test
    @DisplayName("Get Dashboard Summary - Unauthorized User Throws AccessDeniedException")
    void getDashboardSummary_UnauthorizedUser_ThrowsException() {
        mockAuthenticatedUser(mockOtherUser);
        when(accountRepository.findByAccountNumber("BF1000000001")).thenReturn(Optional.of(mockAccount));

        assertThrows(AccessDeniedException.class, () ->
                transactionService.getDashboardSummary("BF1000000001")
        );
    }

    // ==========================================
    // TRANSACTIONS BY DATE RANGE TESTS
    // ==========================================

    @Test
    @DisplayName("Get Transactions By Date Range - Success")
    void getTransactionsByDateRange_Success() {
        mockAuthenticatedUser(mockUser);
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(accountRepository.findByAccountNumber("BF1000000001")).thenReturn(Optional.of(mockAccount));
        when(transactionRepository.findByAccountIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                eq(10L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(mockCreditTx, mockDebitTx));

        List<TransactionResponse> result = transactionService.getTransactionsByDateRange("BF1000000001", startDate, endDate);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("TX101", result.get(0).transactionId());
        assertEquals("TX100", result.get(1).transactionId());
    }

    @Test
    @DisplayName("Get Transactions By Date Range - Account Not Found Throws Exception")
    void getTransactionsByDateRange_AccountNotFound_ThrowsException() {
        mockAuthenticatedUser(mockUser);
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();

        when(accountRepository.findByAccountNumber("BF9999999999")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transactionService.getTransactionsByDateRange("BF9999999999", startDate, endDate)
        );

        assertEquals("Account not found", ex.getMessage());
    }

    // ==========================================
    // ADMIN TRANSACTIONS LOOKUP TESTS
    // ==========================================

    @Test
    @DisplayName("Get All Transactions For Admin - Success")
    void getAllTransactionsForAdmin_Success() {
        mockAuthenticatedUser(mockAdminUser);
        when(transactionRepository.findByAccountAccountNumberOrderByTransactionDateDesc("BF1000000001"))
                .thenReturn(List.of(mockCreditTx, mockDebitTx));

        List<TransactionResponse> result = transactionService.getAllTransactionsForAdmin("BF1000000001");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(transactionRepository, times(1)).findByAccountAccountNumberOrderByTransactionDateDesc("BF1000000001");
    }
}
