package com.bankflow.service;

import com.bankflow.dto.*;
import com.bankflow.entity.*;
import com.bankflow.entity.Loan.LoanStatus;
import com.bankflow.entity.Loan.LoanType;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.LoanRepaymentRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanRepaymentRepository repaymentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LoanService loanService;

    private User mockUser;
    private User mockAdminUser;
    private User mockOtherUser;
    private Account mockAccount;
    private Loan mockLoan;

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
                .accountNumber("BF1234567890")
                .user(mockUser)
                .currentBalance(new BigDecimal("50000.00"))
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build();

        mockLoan = Loan.builder()
                .id(100L)
                .loanNumber("LN-A1B2C3D4")
                .user(mockUser)
                .disbursementAccount(mockAccount)
                .loanType(LoanType.VEHICLE)
                .principalAmount(new BigDecimal("100000.00"))
                .annualInterestRate(new BigDecimal("8.50"))
                .tenureMonths(12)
                .monthlyEmi(new BigDecimal("8721.98"))
                .remainingBalance(new BigDecimal("100000.00"))
                .status(LoanStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

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
    // APPLY LOAN TESTS
    // ==========================================

    @Test
    @DisplayName("Apply Loan - Success")
    void applyForLoan_Success() {
        mockAuthenticatedUser(mockUser);
        ApplyLoanRequest request = new ApplyLoanRequest("BF1234567890", LoanType.VEHICLE, new BigDecimal("100000.00"), 12);

        when(accountRepository.findByAccountNumber("BF1234567890")).thenReturn(Optional.of(mockAccount));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan l = invocation.getArgument(0);
            l.setId(100L);
            if (l.getCreatedAt() == null) {
                l.setCreatedAt(LocalDateTime.now()); // Ensure newly saved entity isn't null
            }
            return l;
        });


        LoanResponse response = loanService.applyForLoan(request);

        assertNotNull(response);
        assertEquals(LoanType.VEHICLE, response.loanType());
        assertEquals(new BigDecimal("100000.00"), response.principalAmount());
        assertEquals(LoanStatus.PENDING, response.status());
        assertNotNull(response.monthlyEmi());

        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    @DisplayName("Apply Loan - Throws Exception when Account Unowned")
    void applyForLoan_UnownedAccount_ThrowsException() {
        mockAuthenticatedUser(mockOtherUser);
        ApplyLoanRequest request = new ApplyLoanRequest("BF1234567890", LoanType.PERSONAL, new BigDecimal("20000.00"), 12);

        when(accountRepository.findByAccountNumber("BF1234567890")).thenReturn(Optional.of(mockAccount));

        assertThrows(AccessDeniedException.class, () -> loanService.applyForLoan(request));
        verify(loanRepository, never()).save(any());
    }

    // ==========================================
    // APPROVE AND DISBURSE TESTS
    // ==========================================

    @Test
    @DisplayName("Approve and Disburse Loan - Success")
    void approveAndDisburseLoan_Success() {
        mockAuthenticatedUser(mockAdminUser);

        when(loanRepository.findById(100L)).thenReturn(Optional.of(mockLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

        LoanResponse response = loanService.approveAndDisburseLoan(100L);

        assertNotNull(response);
        assertEquals(LoanStatus.ACTIVE, response.status());
        assertEquals(new BigDecimal("150000.00"), mockAccount.getCurrentBalance()); // Balance credited (50000 + 100000)
        assertNotNull(response.startDate());
        assertNotNull(response.nextDueDate());

        verify(accountRepository, times(1)).save(mockAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(loanRepository, times(1)).save(mockLoan);
    }

    @Test
    @DisplayName("Approve Loan - Throws Exception if Not Pending")
    void approveLoan_NotPending_ThrowsException() {
        mockAuthenticatedUser(mockAdminUser);
        mockLoan.setStatus(LoanStatus.ACTIVE);

        when(loanRepository.findById(100L)).thenReturn(Optional.of(mockLoan));

        assertThrows(IllegalStateException.class, () -> loanService.approveAndDisburseLoan(100L));
        verify(accountRepository, never()).save(any());
    }

    // ==========================================
    // PAY EMI TESTS
    // ==========================================

    @Test
    @DisplayName("Pay EMI - Success")
    void payEmi_Success() {
        mockAuthenticatedUser(mockUser);
        mockLoan.setStatus(LoanStatus.ACTIVE);
        mockLoan.setNextDueDate(LocalDate.now());

        PayEmiRequest request = new PayEmiRequest("LN-A1B2C3D4", "BF1234567890");

        when(loanRepository.findByLoanNumber("LN-A1B2C3D4")).thenReturn(Optional.of(mockLoan));
        when(accountRepository.findByAccountNumber("BF1234567890")).thenReturn(Optional.of(mockAccount));
        when(repaymentRepository.save(any(LoanRepayment.class))).thenAnswer(i -> {
            LoanRepayment lr = i.getArgument(0);
            lr.setId(500L);
            return lr;
        });

        RepaymentResponse response = loanService.payEmi(request);

        assertNotNull(response);
        assertEquals("LN-A1B2C3D4", response.loanNumber());
        assertEquals(mockLoan.getMonthlyEmi(), response.amountPaid());
        assertNotNull(response.principalComponent());
        assertNotNull(response.interestComponent());

        verify(accountRepository, times(1)).save(mockAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(repaymentRepository, times(1)).save(any(LoanRepayment.class));
    }

    @Test
    @DisplayName("Pay EMI - Throws Exception for Insufficient Balance")
    void payEmi_InsufficientBalance_ThrowsException() {
        mockAuthenticatedUser(mockUser);
        mockLoan.setStatus(LoanStatus.ACTIVE);
        mockAccount.setCurrentBalance(new BigDecimal("100.00")); // Balance less than EMI

        PayEmiRequest request = new PayEmiRequest("LN-A1B2C3D4", "BF1234567890");

        when(loanRepository.findByLoanNumber("LN-A1B2C3D4")).thenReturn(Optional.of(mockLoan));
        when(accountRepository.findByAccountNumber("BF1234567890")).thenReturn(Optional.of(mockAccount));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                loanService.payEmi(request)
        );

        assertEquals("Insufficient balance in source account for EMI payment", ex.getMessage());
        verify(repaymentRepository, never()).save(any());
    }

    // ==========================================
    // GET MY LOANS TESTS
    // ==========================================

    @Test
    @DisplayName("Get My Loans - Success")
    void getMyLoans_Success() {
        mockAuthenticatedUser(mockUser);
        when(loanRepository.findByUserId(mockUser.getId())).thenReturn(List.of(mockLoan));

        List<LoanResponse> result = loanService.getMyLoans();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("LN-A1B2C3D4", result.get(0).loanNumber());
    }

    @Test
    @DisplayName("Apply Loan - Throws Exception when Account Not Found")
    void applyForLoan_AccountNotFound_ThrowsException() {
        mockAuthenticatedUser(mockUser);

        ApplyLoanRequest request = new ApplyLoanRequest(
                "INVALID",
                LoanType.PERSONAL,
                new BigDecimal("20000.00"),
                12
        );

        when(accountRepository.findByAccountNumber("INVALID"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanService.applyForLoan(request)
        );

        assertEquals("Disbursement account not found", ex.getMessage());

        verify(loanRepository, never()).save(any());
        verify(auditLogService, never()).log(any(), anyString());
    }

    @Test
    @DisplayName("Apply Loan - Throws Exception when Account Inactive")
    void applyForLoan_InactiveAccount_ThrowsException() {
        mockAuthenticatedUser(mockUser);

        mockAccount.setAccountStatus(Account.AccountStatus.FROZEN);

        ApplyLoanRequest request = new ApplyLoanRequest(
                "BF1234567890",
                LoanType.PERSONAL,
                new BigDecimal("20000.00"),
                12
        );

        when(accountRepository.findByAccountNumber("BF1234567890"))
                .thenReturn(Optional.of(mockAccount));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> loanService.applyForLoan(request)
        );

        assertEquals(
                "Loans can only be applied using an active account.",
                ex.getMessage()
        );

        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Apply Loan - Personal Loan Uses 12 Percent Interest")
    void applyForLoan_PersonalLoan_UsesCorrectInterestRate() {
        mockAuthenticatedUser(mockUser);

        ApplyLoanRequest request = new ApplyLoanRequest(
                "BF1234567890",
                LoanType.PERSONAL,
                new BigDecimal("100000.00"),
                12
        );

        when(accountRepository.findByAccountNumber("BF1234567890"))
                .thenReturn(Optional.of(mockAccount));

        mockLoanSave();

        LoanResponse response = loanService.applyForLoan(request);

        assertEquals(new BigDecimal("12.00"), response.annualInterestRate());
    }

    @Test
    @DisplayName("Apply Loan - Home Loan Uses 7 Percent Interest")
    void applyForLoan_HomeLoan_UsesCorrectInterestRate() {
        mockAuthenticatedUser(mockUser);

        ApplyLoanRequest request = new ApplyLoanRequest(
                "BF1234567890",
                LoanType.HOME,
                new BigDecimal("100000.00"),
                12
        );

        when(accountRepository.findByAccountNumber("BF1234567890"))
                .thenReturn(Optional.of(mockAccount));

        mockLoanSave();

        LoanResponse response = loanService.applyForLoan(request);

        assertEquals(new BigDecimal("7.00"), response.annualInterestRate());
    }

    @Test
    @DisplayName("Apply Loan - Calculates EMI Correctly")
    void applyForLoan_CalculatesEmiCorrectly() {
        mockAuthenticatedUser(mockUser);

        ApplyLoanRequest request = new ApplyLoanRequest(
                "BF1234567890",
                LoanType.VEHICLE,
                new BigDecimal("100000.00"),
                12
        );

        when(accountRepository.findByAccountNumber("BF1234567890"))
                .thenReturn(Optional.of(mockAccount));

        mockLoanSave();

        LoanResponse response = loanService.applyForLoan(request);

        assertEquals(
                new BigDecimal("8721.98"),
                response.monthlyEmi()
        );
    }

    @Test
    @DisplayName("Approve Loan - Throws Exception when Loan Not Found")
    void approveAndDisburseLoan_NotFound_ThrowsException() {
        mockAuthenticatedUser(mockAdminUser);

        when(loanRepository.findById(999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanService.approveAndDisburseLoan(999L)
        );

        assertEquals("Loan application not found", ex.getMessage());

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reject Loan - Success")
    void rejectLoan_Success() {
        mockAuthenticatedUser(mockAdminUser);

        RejectLoanRequest request =
                new RejectLoanRequest("Income verification failed");

        when(loanRepository.findById(100L))
                .thenReturn(Optional.of(mockLoan));

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(i -> i.getArgument(0));

        LoanResponse response =
                loanService.rejectLoan(100L, request);

        assertNotNull(response);
        assertEquals(LoanStatus.REJECTED, response.status());
        assertEquals(
                "Income verification failed",
                response.rejectionRemarks()
        );

        verify(loanRepository).save(mockLoan);

        verify(auditLogService).log(
                eq(AuditAction.LOAN_REJECTED),
                contains("Rejected loan LN-A1B2C3D4")
        );
    }

    @Test
    @DisplayName("Reject Loan - Throws Exception if Not Pending")
    void rejectLoan_NotPending_ThrowsException() {
        mockAuthenticatedUser(mockAdminUser);

        mockLoan.setStatus(LoanStatus.ACTIVE);

        RejectLoanRequest request =
                new RejectLoanRequest("Rejected");

        when(loanRepository.findById(100L))
                .thenReturn(Optional.of(mockLoan));

        assertThrows(
                IllegalStateException.class,
                () -> loanService.rejectLoan(100L, request)
        );

        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Pay EMI - Throws Exception when Loan Not Found")
    void payEmi_LoanNotFound_ThrowsException() {
        mockAuthenticatedUser(mockUser);

        PayEmiRequest request =
                new PayEmiRequest("INVALID", "BF1234567890");

        when(loanRepository.findByLoanNumber("INVALID"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanService.payEmi(request)
        );

        assertEquals("Loan not found", ex.getMessage());

        verify(accountRepository, never()).save(any());
        verify(repaymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Pay EMI - Throws Exception when User Does Not Own Loan")
    void payEmi_UnownedLoan_ThrowsException() {
        mockAuthenticatedUser(mockOtherUser);

        PayEmiRequest request =
                new PayEmiRequest("LN-A1B2C3D4", "BF1234567890");

        when(loanRepository.findByLoanNumber("LN-A1B2C3D4"))
                .thenReturn(Optional.of(mockLoan));

        assertThrows(
                AccessDeniedException.class,
                () -> loanService.payEmi(request)
        );

        verify(accountRepository, never()).save(any());
        verify(repaymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Pay EMI - Throws Exception when Loan Not Active")
    void payEmi_NonActiveLoan_ThrowsException() {
        mockAuthenticatedUser(mockUser);

        mockLoan.setStatus(LoanStatus.PENDING);

        PayEmiRequest request =
                new PayEmiRequest("LN-A1B2C3D4", "BF1234567890");

        when(loanRepository.findByLoanNumber("LN-A1B2C3D4"))
                .thenReturn(Optional.of(mockLoan));

        assertThrows(
                IllegalStateException.class,
                () -> loanService.payEmi(request)
        );

        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Pay EMI - Throws Exception when Source Account Not Found")
    void payEmi_SourceAccountNotFound_ThrowsException() {
        mockAuthenticatedUser(mockUser);

        mockLoan.setStatus(LoanStatus.ACTIVE);

        PayEmiRequest request =
                new PayEmiRequest("LN-A1B2C3D4", "INVALID");

        when(loanRepository.findByLoanNumber("LN-A1B2C3D4"))
                .thenReturn(Optional.of(mockLoan));

        when(accountRepository.findByAccountNumber("INVALID"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanService.payEmi(request)
        );

        assertEquals("Source account not found", ex.getMessage());
    }

    @Test
    @DisplayName("Pay EMI - Throws Exception when Source Account Unowned")
    void payEmi_UnownedSourceAccount_ThrowsException() {
        mockAuthenticatedUser(mockUser);

        mockLoan.setStatus(LoanStatus.ACTIVE);

        Account otherAccount = Account.builder()
                .id(20L)
                .accountNumber("OTHER123")
                .user(mockOtherUser)
                .currentBalance(new BigDecimal("50000.00"))
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build();

        PayEmiRequest request =
                new PayEmiRequest("LN-A1B2C3D4", "OTHER123");

        when(loanRepository.findByLoanNumber("LN-A1B2C3D4"))
                .thenReturn(Optional.of(mockLoan));

        when(accountRepository.findByAccountNumber("OTHER123"))
                .thenReturn(Optional.of(otherAccount));

        assertThrows(
                AccessDeniedException.class,
                () -> loanService.payEmi(request)
        );

        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Pay EMI - Throws Exception when Source Account Inactive")
    void payEmi_InactiveSourceAccount_ThrowsException() {
        mockAuthenticatedUser(mockUser);

        mockLoan.setStatus(LoanStatus.ACTIVE);
        mockAccount.setAccountStatus(Account.AccountStatus.FROZEN);

        PayEmiRequest request =
                new PayEmiRequest("LN-A1B2C3D4", "BF1234567890");

        when(loanRepository.findByLoanNumber("LN-A1B2C3D4"))
                .thenReturn(Optional.of(mockLoan));

        when(accountRepository.findByAccountNumber("BF1234567890"))
                .thenReturn(Optional.of(mockAccount));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> loanService.payEmi(request)
        );

        assertEquals(
                "EMI payments can only be made from an active account.",
                ex.getMessage()
        );

        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Pay EMI - Final Payment Marks Loan as Paid Off")
    void payEmi_FinalPayment_MarksLoanPaidOff() {
        mockAuthenticatedUser(mockUser);

        mockLoan.setStatus(LoanStatus.ACTIVE);
        mockLoan.setRemainingBalance(new BigDecimal("1000.00"));
        mockLoan.setNextDueDate(LocalDate.now());

        PayEmiRequest request =
                new PayEmiRequest("LN-A1B2C3D4", "BF1234567890");

        when(loanRepository.findByLoanNumber("LN-A1B2C3D4"))
                .thenReturn(Optional.of(mockLoan));

        when(accountRepository.findByAccountNumber("BF1234567890"))
                .thenReturn(Optional.of(mockAccount));

        when(repaymentRepository.save(any(LoanRepayment.class)))
                .thenAnswer(i -> {
                    LoanRepayment repayment = i.getArgument(0);
                    repayment.setId(500L);
                    return repayment;
                });

        loanService.payEmi(request);

        assertEquals(LoanStatus.PAID_OFF, mockLoan.getStatus());
        assertEquals(BigDecimal.ZERO, mockLoan.getRemainingBalance());
        assertNull(mockLoan.getNextDueDate());
    }

    @Test
    @DisplayName("Get Repayment History - Owner Can View")
    void getRepaymentHistory_Owner_Success() {
        mockAuthenticatedUser(mockUser);

        when(loanRepository.findByLoanNumber("LN-A1B2C3D4"))
                .thenReturn(Optional.of(mockLoan));

        LoanRepayment repayment = LoanRepayment.builder()
                .id(500L)
                .loan(mockLoan)
                .amountPaid(new BigDecimal("8721.98"))
                .principalComponent(new BigDecimal("8013.81"))
                .interestComponent(new BigDecimal("708.33"))
                .remainingLoanBalance(new BigDecimal("91986.19"))
                .transactionReference("TX-EMI-12345678")
                .build();

        when(repaymentRepository.findByLoanIdOrderByPaymentDateDesc(100L))
                .thenReturn(List.of(repayment));

        List<RepaymentResponse> result =
                loanService.getRepaymentHistory("LN-A1B2C3D4");

        assertEquals(1, result.size());
        assertEquals("LN-A1B2C3D4", result.get(0).loanNumber());
    }

    @Test
    @DisplayName("Get Repayment History - Non Owner Denied")
    void getRepaymentHistory_NonOwner_ThrowsException() {
        mockAuthenticatedUser(mockOtherUser);

        when(loanRepository.findByLoanNumber("LN-A1B2C3D4"))
                .thenReturn(Optional.of(mockLoan));

        assertThrows(
                AccessDeniedException.class,
                () -> loanService.getRepaymentHistory("LN-A1B2C3D4")
        );

        verify(repaymentRepository, never())
                .findByLoanIdOrderByPaymentDateDesc(anyLong());
    }

    @Test
    @DisplayName("Get Repayment History - Admin Can View Any Loan")
    void getRepaymentHistory_Admin_Success() {
        mockAuthenticatedUser(mockAdminUser);

        when(loanRepository.findByLoanNumber("LN-A1B2C3D4"))
                .thenReturn(Optional.of(mockLoan));

        when(repaymentRepository.findByLoanIdOrderByPaymentDateDesc(100L))
                .thenReturn(List.of());

        List<RepaymentResponse> result =
                loanService.getRepaymentHistory("LN-A1B2C3D4");

        assertNotNull(result);

        verify(repaymentRepository)
                .findByLoanIdOrderByPaymentDateDesc(100L);
    }

    @Test
    @DisplayName("Get My Loans - Returns Empty List when No Loans")
    void getMyLoans_NoLoans_ReturnsEmptyList() {
        mockAuthenticatedUser(mockUser);

        when(loanRepository.findByUserId(mockUser.getId()))
                .thenReturn(List.of());

        List<LoanResponse> result = loanService.getMyLoans();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Get Pending Loans - Returns Paginated Results")
    void getPendingLoans_Success() {
        Pageable pageable = PageRequest.of(0, 10);

        Loan pendingLoan = mockLoan;

        Page<Loan> page =
                new PageImpl<>(List.of(pendingLoan), pageable, 1);

        when(loanRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(page);

        Page<LoanResponse> result =
                loanService.getPendingLoans(
                        null,
                        null,
                        pageable
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(loanRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    @DisplayName("Get Loan Summary - Returns Correct Counts")
    void getLoanSummary_ReturnsCorrectCounts() {
        when(loanRepository.countByStatus(LoanStatus.PENDING))
                .thenReturn(10L);

        when(loanRepository.countByStatusAndLoanType(
                LoanStatus.PENDING,
                LoanType.PERSONAL
        )).thenReturn(4L);

        when(loanRepository.countByStatusAndLoanType(
                LoanStatus.PENDING,
                LoanType.HOME
        )).thenReturn(3L);

        when(loanRepository.countByStatusAndLoanType(
                LoanStatus.PENDING,
                LoanType.VEHICLE
        )).thenReturn(3L);

        LoanSummaryResponse result =
                loanService.getLoanSummary();

        assertEquals(10L, result.totalPending());
        assertEquals(4L, result.personalLoans());
        assertEquals(3L, result.homeLoans());
        assertEquals(3L, result.vehicleLoans());
    }

    private void mockLoanSave() {
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);

            if (loan.getId() == null) {
                loan.setId(100L);
            }

            if (loan.getCreatedAt() == null) {
                loan.setCreatedAt(LocalDateTime.now());
            }

            return loan;
        });
    }


}
