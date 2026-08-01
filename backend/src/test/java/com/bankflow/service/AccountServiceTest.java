package com.bankflow.service;

import com.bankflow.dto.AccountResponse;
import com.bankflow.dto.BalanceResponse;
import com.bankflow.dto.CreateAccountRequest;
import com.bankflow.dto.UpdateProfileRequest;
import com.bankflow.entity.Account;
import com.bankflow.entity.Transaction;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

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
    private AccountService accountService;

    @Mock
    private AuditLogService auditLogService;

    private User mockUser;
    private User mockAdminUser;
    private User mockOtherUser;
    private Account mockAccount;

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
                .id(100L)
                .accountNumber("BF1234567890")
                .user(mockUser)
                .accountType(Account.AccountType.SAVINGS)
                .branchName("Main Branch")
                .currentBalance(new BigDecimal("5000.00"))
                .accountStatus(Account.AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
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
    // CREATE ACCOUNT TESTS
    // ==========================================

    @Test
    @DisplayName("Create Account with Initial Deposit > 0 Success")
    void createAccount_WithInitialDeposit_Success() {
        mockAuthenticatedUser(mockUser);
        CreateAccountRequest request = new CreateAccountRequest(Account.AccountType.SAVINGS, "Main Branch", new BigDecimal("1000.00"));

        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            acc.setId(100L);
            acc.setCreatedAt(LocalDateTime.now());
            return acc;
        });

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals("SAVINGS", response.accountType().name());
        assertEquals(new BigDecimal("1000.00"), response.currentBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Create Account with Zero Initial Deposit Success")
    void createAccount_ZeroInitialDeposit_Success() {
        mockAuthenticatedUser(mockUser);
        CreateAccountRequest request = new CreateAccountRequest(Account.AccountType.SAVINGS, "Downtown Branch", BigDecimal.ZERO);

        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            acc.setId(101L);
            acc.setCreatedAt(LocalDateTime.now());
            return acc;
        });

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.currentBalance());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // ==========================================
    // GET MY ACCOUNTS TESTS
    // ==========================================

    @Test
    @DisplayName("Get My Accounts Success")
    void getMyAccounts_Success() {
        mockAuthenticatedUser(mockUser);
        when(accountRepository.findByUserId(mockUser.getId())).thenReturn(List.of(mockAccount));

        List<AccountResponse> result = accountService.getMyAccounts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("BF1234567890", result.get(0).accountNumber());
    }

    // ==========================================
    // GET ACCOUNT BY NUMBER TESTS
    // ==========================================

    @Test
    @DisplayName("Get Account By Number Owner Success")
    void getAccountByNumber_AsOwner_Success() {
        mockAuthenticatedUser(mockUser);
        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber())).thenReturn(Optional.of(mockAccount));

        AccountResponse response = accountService.getAccountByNumber(mockAccount.getAccountNumber());

        assertNotNull(response);
        assertEquals(mockAccount.getAccountNumber(), response.accountNumber());
    }

    @Test
    @DisplayName("Get Account By Number As Admin Success")
    void getAccountByNumber_AsAdmin_Success() {
        mockAuthenticatedUser(mockAdminUser);
        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber())).thenReturn(Optional.of(mockAccount));

        AccountResponse response = accountService.getAccountByNumber(mockAccount.getAccountNumber());

        assertNotNull(response);
        assertEquals(mockAccount.getAccountNumber(), response.accountNumber());
    }

    @Test
    @DisplayName("Get Account By Number Unauthorized Throws Exception")
    void getAccountByNumber_UnauthorizedUser_ThrowsException() {
        mockAuthenticatedUser(mockOtherUser);
        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber())).thenReturn(Optional.of(mockAccount));

        assertThrows(AccessDeniedException.class, () ->
                accountService.getAccountByNumber(mockAccount.getAccountNumber())
        );
    }

    @Test
    @DisplayName("Get Account By Number Not Found Throws Exception")
    void getAccountByNumber_NotFound_ThrowsException() {
        mockAuthenticatedUser(mockUser);
        when(accountRepository.findByAccountNumber("BF9999999999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                accountService.getAccountByNumber("BF9999999999")
        );
    }

    // ==========================================
    // GET AVAILABLE BALANCE TESTS
    // ==========================================

    @Test
    @DisplayName("Get Available Balance Owner Success")
    void getAvailableBalance_AsOwner_Success() {
        mockAuthenticatedUser(mockUser);
        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber())).thenReturn(Optional.of(mockAccount));

        BalanceResponse response = accountService.getAvailableBalance(mockAccount.getAccountNumber());

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.currentBalance());
        assertEquals("ACTIVE", response.accountStatus());
    }

    @Test
    @DisplayName("Get Available Balance Unauthorized Throws Exception")
    void getAvailableBalance_UnauthorizedUser_ThrowsException() {
        mockAuthenticatedUser(mockOtherUser);
        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber())).thenReturn(Optional.of(mockAccount));

        assertThrows(AccessDeniedException.class, () ->
                accountService.getAvailableBalance(mockAccount.getAccountNumber())
        );
    }

    // ==========================================
    // ADMIN GET ALL ACCOUNTS TESTS
    // ==========================================

    @Test
    @DisplayName("Get All Accounts For Admin Success")
    void getAllAccountsForAdmin_Success() {
        mockAuthenticatedUser(mockAdminUser);
        when(accountRepository.findAll()).thenReturn(List.of(mockAccount));

        List<AccountResponse> result = accountService.getAllAccountsForAdmin();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountRepository, times(1)).findAll();
    }
}
