package com.bankflow.service;

import com.bankflow.dto.CreateFdRequest;
import com.bankflow.dto.FdCalculatorRequest;
import com.bankflow.dto.FdCalculatorResponse;
import com.bankflow.dto.FdResponse;
import com.bankflow.entity.Account;
import com.bankflow.entity.FixedDeposit;
import com.bankflow.entity.Transaction;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.FixedDepositRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FixedDepositServiceTest {

    @Mock
    private FixedDepositRepository fdRepository;

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
    private FixedDepositService fdService;

    private User mockUser;
    private User mockAdminUser;
    private User mockOtherUser;
    private Account mockAccount;
    private FixedDeposit mockFd;

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

        mockFd = FixedDeposit.builder()
                .id(100L)
                .fdNumber("FD1234567890")
                .user(mockUser)
                .sourceAccount(mockAccount)
                .depositAmount(new BigDecimal("20000.00"))
                .interestRate(new BigDecimal("6.50"))
                .tenureYears(1)
                .depositDate(LocalDate.now())
                .maturityDate(LocalDate.now().plusYears(1))
                .maturityAmount(new BigDecimal("21332.14"))
                .status(FixedDeposit.FdStatus.ACTIVE)
                .build();

        // Setup Spring Security Context
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
    // CALCULATE MATURITY TESTS
    // ==========================================

    @Test
    @DisplayName("Calculate Maturity Success - 1 Year Tenure")
    void calculateMaturity_OneYear_Success() {
        FdCalculatorRequest request = new FdCalculatorRequest(new BigDecimal("50000.00"), 1);

        FdCalculatorResponse response = fdService.calculateMaturity(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("6.50"), response.interestRate());
        assertEquals(new BigDecimal("50000.00"), response.depositAmount());
        assertTrue(response.maturityAmount().compareTo(new BigDecimal("50000.00")) > 0);
    }

    @Test
    @DisplayName("Calculate Maturity - Default/Unsupported Tenure Throws Exception")
    void calculateMaturity_UnsupportedTenure_ThrowsIllegalArgumentException() {
        // Testing an unexpected tenure value (e.g., 10 years or 0 years) that hits the switch/if default case
        FdCalculatorRequest request = new FdCalculatorRequest(new BigDecimal("50000.00"), 10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fdService.calculateMaturity(request)
        );

        assertEquals("Invalid tenure! Allowed tenure options are 1, 3, or 5 years.", ex.getMessage());
    }

    @Test
    @DisplayName("Create FD Throws Exception - Invalid Tenure")
    void createFixedDeposit_InvalidTenure_ThrowsException() {
        mockAuthenticatedUser(mockUser);
        // Request with invalid tenure
        CreateFdRequest request = new CreateFdRequest("BF1234567890", new BigDecimal("15000.00"), 7);

        // REMOVED: when(accountRepository.findByAccountNumber(...))
        // because tenure validation happens before account lookup.

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fdService.createFixedDeposit(request)
        );

        assertEquals("Invalid tenure! Allowed tenure options are 1, 3, or 5 years.", ex.getMessage());
        verify(accountRepository, never()).findByAccountNumber(anyString());
        verify(fdRepository, never()).save(any());
    }


    @Test
    @DisplayName("Calculate Maturity Throws Exception - Invalid Tenure")
    void calculateMaturity_InvalidTenure_ThrowsException() {
        FdCalculatorRequest request = new FdCalculatorRequest(new BigDecimal("50000.00"), 2);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fdService.calculateMaturity(request)
        );

        assertEquals("Invalid tenure! Allowed tenure options are 1, 3, or 5 years.", ex.getMessage());
    }

    // ==========================================
    // CREATE FIXED DEPOSIT TESTS
    // ==========================================

    @Test
    @DisplayName("Create Fixed Deposit Success")
    void createFixedDeposit_Success() {
        mockAuthenticatedUser(mockUser);
        CreateFdRequest request = new CreateFdRequest("BF1234567890", new BigDecimal("20000.00"), 1);

        when(accountRepository.findByAccountNumber("BF1234567890")).thenReturn(Optional.of(mockAccount));
        when(fdRepository.existsByFdNumber(anyString())).thenReturn(false);
        when(fdRepository.save(any(FixedDeposit.class))).thenAnswer(invocation -> {
            FixedDeposit fd = invocation.getArgument(0);
            fd.setId(100L);
            return fd;
        });

        FdResponse response = fdService.createFixedDeposit(request);

        assertNotNull(response);
        assertEquals("BF1234567890", response.sourceAccountNumber());
        assertEquals(new BigDecimal("20000.00"), response.depositAmount());
        assertEquals(new BigDecimal("30000.00"), mockAccount.getCurrentBalance()); // Balance deducted (50000 - 20000)

        verify(accountRepository, times(1)).save(mockAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(fdRepository, times(1)).save(any(FixedDeposit.class));
    }

    @Test
    @DisplayName("Create FD Throws Exception - Amount < 10,000 Threshold")
    void createFixedDeposit_AmountLessThanMinimum_ThrowsException() {
        mockAuthenticatedUser(mockUser);
        CreateFdRequest request = new CreateFdRequest("BF1234567890", new BigDecimal("9000.00"), 1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fdService.createFixedDeposit(request)
        );

        assertEquals("Deposit amount must be minimum Rs. 10,000", ex.getMessage());
        verify(fdRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create FD Throws Exception - Account Unowned By User")
    void createFixedDeposit_UnownedAccount_ThrowsException() {
        mockAuthenticatedUser(mockOtherUser);
        CreateFdRequest request = new CreateFdRequest("BF1234567890", new BigDecimal("15000.00"), 1);

        when(accountRepository.findByAccountNumber("BF1234567890")).thenReturn(Optional.of(mockAccount));

        assertThrows(AccessDeniedException.class, () ->
                fdService.createFixedDeposit(request)
        );

        verify(fdRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create FD Throws Exception - Insufficient Balance")
    void createFixedDeposit_InsufficientBalance_ThrowsException() {
        mockAuthenticatedUser(mockUser);
        CreateFdRequest request = new CreateFdRequest("BF1234567890", new BigDecimal("100000.00"), 1);

        when(accountRepository.findByAccountNumber("BF1234567890")).thenReturn(Optional.of(mockAccount));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fdService.createFixedDeposit(request)
        );

        assertEquals("Insufficient balance in source account", ex.getMessage());
        verify(fdRepository, never()).save(any());
    }

    // ==========================================
    // GET MY FIXED DEPOSITS TESTS
    // ==========================================

    @Test
    @DisplayName("Get My Fixed Deposits Success")
    void getMyFixedDeposits_Success() {
        mockAuthenticatedUser(mockUser);
        when(fdRepository.findByUserId(mockUser.getId())).thenReturn(List.of(mockFd));

        List<FdResponse> result = fdService.getMyFixedDeposits();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("FD1234567890", result.get(0).fdNumber());
    }

    // ==========================================
    // GET FD BY NUMBER TESTS
    // ==========================================

    @Test
    @DisplayName("Get FD By Number Owner Success")
    void getFdByNumber_AsOwner_Success() {
        mockAuthenticatedUser(mockUser);
        when(fdRepository.findByFdNumber("FD1234567890")).thenReturn(Optional.of(mockFd));

        FdResponse response = fdService.getFdByNumber("FD1234567890");

        assertNotNull(response);
        assertEquals("FD1234567890", response.fdNumber());
    }

    @Test
    @DisplayName("Get FD By Number As Admin Success")
    void getFdByNumber_AsAdmin_Success() {
        mockAuthenticatedUser(mockAdminUser);
        when(fdRepository.findByFdNumber("FD1234567890")).thenReturn(Optional.of(mockFd));

        FdResponse response = fdService.getFdByNumber("FD1234567890");

        assertNotNull(response);
        assertEquals("FD1234567890", response.fdNumber());
    }

    @Test
    @DisplayName("Get FD By Number Unauthorized Throws Exception")
    void getFdByNumber_UnauthorizedUser_ThrowsException() {
        mockAuthenticatedUser(mockOtherUser);
        when(fdRepository.findByFdNumber("FD1234567890")).thenReturn(Optional.of(mockFd));

        assertThrows(AccessDeniedException.class, () ->
                fdService.getFdByNumber("FD1234567890")
        );
    }
}
