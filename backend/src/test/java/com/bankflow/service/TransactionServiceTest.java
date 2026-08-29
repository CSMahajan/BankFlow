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
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
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

    @Mock
    private PdfExportService pdfExportService;

    @Mock
    private ExcelExportService excelExportService;

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

    @Test
    @DisplayName("Get My Transactions - Success")
    void getMyTransactions_Success() {
        mockAuthenticatedUser(mockUser);

        Pageable pageable = PageRequest.of(0, 20);

        Page<Transaction> transactionPage =
                new PageImpl<>(List.of(mockCreditTx, mockDebitTx), pageable, 2);

        when(transactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(transactionPage);

        Page<TransactionResponse> result =
                transactionService.getMyTransactions(
                        "BF1000000001",
                        null,
                        null,
                        null,
                        null,
                        pageable
                );

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        verify(transactionRepository, times(1))
                .findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Get My Transactions - Rejects Page Size Above 100")
    void getMyTransactions_PageSizeTooLarge() {
        mockAuthenticatedUser(mockUser);

        Pageable pageable = PageRequest.of(0, 101);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.getMyTransactions(
                        "BF1000000001",
                        null,
                        null,
                        null,
                        null,
                        pageable
                )
        );

        assertEquals("Maximum page size is 100", ex.getMessage());

        verify(transactionRepository, never())
                .findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Get My Transactions - Rejects Start Date After End Date")
    void getMyTransactions_InvalidDateRange() {
        mockAuthenticatedUser(mockUser);

        Pageable pageable = PageRequest.of(0, 20);

        LocalDate startDate = LocalDate.of(2026, 8, 20);
        LocalDate endDate = LocalDate.of(2026, 8, 10);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.getMyTransactions(
                        "BF1000000001",
                        null,
                        startDate,
                        endDate,
                        null,
                        pageable
                )
        );

        assertEquals("Start date cannot be after end date", ex.getMessage());

        verify(transactionRepository, never())
                .findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Get My Transactions - Start Date Without End Date Uses Today")
    void getMyTransactions_StartDateWithoutEndDate() {
        mockAuthenticatedUser(mockUser);

        Pageable pageable = PageRequest.of(0, 20);

        Page<Transaction> transactionPage =
                new PageImpl<>(List.of(mockDebitTx), pageable, 1);

        when(transactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(transactionPage);

        Page<TransactionResponse> result =
                transactionService.getMyTransactions(
                        "BF1000000001",
                        TransactionType.DEBIT,
                        LocalDate.now().minusDays(30),
                        null,
                        null,
                        pageable
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(transactionRepository)
                .findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Export Transactions PDF - Success")
    void exportTransactionsPdf_Success() {
        mockAuthenticatedUser(mockUser);

        byte[] expectedPdf = "PDF-DATA".getBytes();

        when(transactionRepository.findAll(
                any(Specification.class),
                any(Sort.class)
        )).thenReturn(List.of(mockCreditTx, mockDebitTx));

        when(pdfExportService.generateTransactionPdf(anyList()))
                .thenReturn(expectedPdf);

        byte[] result = transactionService.exportTransactionsPdf(
                "BF1000000001",
                TransactionType.CREDIT,
                null,
                null,
                null
        );

        assertNotNull(result);
        assertArrayEquals(expectedPdf, result);

        verify(pdfExportService, times(1))
                .generateTransactionPdf(anyList());

        verify(transactionRepository, times(1))
                .findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    @DisplayName("Export Transactions Excel - Success")
    void exportTransactionsExcel_Success() {
        mockAuthenticatedUser(mockUser);

        byte[] expectedExcel = "EXCEL-DATA".getBytes();

        when(transactionRepository.findAll(
                any(Specification.class),
                any(Sort.class)
        )).thenReturn(List.of(mockCreditTx, mockDebitTx));

        when(excelExportService.generateTransactionExcel(anyList()))
                .thenReturn(expectedExcel);

        byte[] result = transactionService.exportTransactionsExcel(
                "BF1000000001",
                null,
                null,
                null,
                null
        );

        assertNotNull(result);
        assertArrayEquals(expectedExcel, result);

        verify(excelExportService, times(1))
                .generateTransactionExcel(anyList());

        verify(transactionRepository, times(1))
                .findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    @DisplayName("Export Transactions PDF - Rejects Invalid Date Range")
    void exportTransactionsPdf_InvalidDateRange() {
        mockAuthenticatedUser(mockUser);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.exportTransactionsPdf(
                        "BF1000000001",
                        null,
                        LocalDate.of(2026, 8, 20),
                        LocalDate.of(2026, 8, 10),
                        null
                )
        );

        assertEquals("Start date cannot be after end date", ex.getMessage());

        verify(transactionRepository, never())
                .findAll(any(Specification.class), any(Sort.class));

        verify(pdfExportService, never())
                .generateTransactionPdf(anyList());
    }

    @Test
    @DisplayName("Get Account Transactions For Admin - Success")
    void getAccountTransactionsForAdmin_Success() {
        mockAuthenticatedUser(mockAdminUser);

        Pageable pageable = PageRequest.of(0, 20);

        Page<Transaction> transactionPage =
                new PageImpl<>(
                        List.of(mockCreditTx, mockDebitTx),
                        pageable,
                        2
                );

        when(transactionRepository
                .findByAccountAccountNumberOrderByTransactionDateDesc(
                        "BF1000000001",
                        pageable
                ))
                .thenReturn(transactionPage);

        Page<TransactionResponse> result =
                transactionService.getAccountTransactionsForAdmin(
                        "BF1000000001",
                        pageable
                );

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        verify(transactionRepository)
                .findByAccountAccountNumberOrderByTransactionDateDesc(
                        "BF1000000001",
                        pageable
                );
    }

    @Test
    @DisplayName("Get Account Transactions For Admin - Rejects Page Size Above 100")
    void getAccountTransactionsForAdmin_PageSizeTooLarge() {
        mockAuthenticatedUser(mockAdminUser);

        Pageable pageable = PageRequest.of(0, 101);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.getAccountTransactionsForAdmin(
                        "BF1000000001",
                        pageable
                )
        );

        assertEquals("Maximum page size is 100", ex.getMessage());

        verify(transactionRepository, never())
                .findByAccountAccountNumberOrderByTransactionDateDesc(
                        anyString(),
                        any(Pageable.class)
                );
    }

    @Test
    @DisplayName("Get Transaction Details - Customer Can View Own Transaction")
    void getTransactionDetails_OwnTransaction_Success() {
        mockAuthenticatedUser(mockUser);

        when(transactionRepository.findByTransactionId("TX100"))
                .thenReturn(Optional.of(mockDebitTx));

        TransactionResponse result =
                transactionService.getTransactionDetails("TX100");

        assertNotNull(result);
        assertEquals("TX100", result.transactionId());
        assertEquals("BF1000000001", result.accountNumber());
        assertEquals(TransactionType.DEBIT, result.transactionType());
        assertEquals(new BigDecimal("500.00"), result.amount());

        verify(transactionRepository)
                .findByTransactionId("TX100");
    }

    @Test
    @DisplayName("Get Transaction Details - Customer Cannot View Another User's Transaction")
    void getTransactionDetails_UnauthorizedUser() {
        mockAuthenticatedUser(mockOtherUser);

        when(transactionRepository.findByTransactionId("TX100"))
                .thenReturn(Optional.of(mockDebitTx));

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> transactionService.getTransactionDetails("TX100")
        );

        assertEquals(
                "You are not authorized to view this transaction",
                ex.getMessage()
        );
    }

    @Test
    @DisplayName("Get Transaction Details - Admin Can View Any Transaction")
    void getTransactionDetails_Admin_Success() {
        mockAuthenticatedUser(mockAdminUser);

        when(transactionRepository.findByTransactionId("TX100"))
                .thenReturn(Optional.of(mockDebitTx));

        TransactionResponse result =
                transactionService.getTransactionDetails("TX100");

        assertNotNull(result);
        assertEquals("TX100", result.transactionId());

        verify(transactionRepository)
                .findByTransactionId("TX100");
    }

    @Test
    @DisplayName("Get Transaction Details - Throws When Transaction Not Found")
    void getTransactionDetails_NotFound() {
        mockAuthenticatedUser(mockUser);

        when(transactionRepository.findByTransactionId("INVALID"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.getTransactionDetails("INVALID")
        );

        assertEquals("Transaction not found", ex.getMessage());

        verify(transactionRepository)
                .findByTransactionId("INVALID");
    }




}
