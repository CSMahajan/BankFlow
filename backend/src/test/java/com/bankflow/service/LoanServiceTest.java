package com.bankflow.service;

import com.bankflow.dto.ApplyLoanRequest;
import com.bankflow.dto.LoanResponse;
import com.bankflow.dto.PayEmiRequest;
import com.bankflow.dto.RepaymentResponse;
import com.bankflow.entity.Account;
import com.bankflow.entity.Loan;
import com.bankflow.entity.Loan.LoanStatus;
import com.bankflow.entity.Loan.LoanType;
import com.bankflow.entity.LoanRepayment;
import com.bankflow.entity.Transaction;
import com.bankflow.entity.User;
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
}
