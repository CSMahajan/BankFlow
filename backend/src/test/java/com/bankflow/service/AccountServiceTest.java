package com.bankflow.service;

import com.bankflow.dto.*;
import com.bankflow.entity.Account;
import com.bankflow.entity.AuditAction;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
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

        when(accountRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(mockAccount)));

        Page<AccountResponse> result =
                accountService.getAllAccountsForAdmin(
                        0,
                        20,
                        null,
                        null
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(accountRepository, times(1))
                .findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("Create Account with Null Initial Deposit Defaults to Zero")
    void createAccount_NullInitialDeposit_Success() {
        mockAuthenticatedUser(mockUser);

        CreateAccountRequest request = new CreateAccountRequest(
                Account.AccountType.SAVINGS,
                "Main Branch",
                null
        );

        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(102L);
            account.setCreatedAt(LocalDateTime.now());
            return account;
        });

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.currentBalance());

        verify(accountRepository, times(1)).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Get My Accounts When User Has No Accounts Returns Empty List")
    void getMyAccounts_NoAccounts_ReturnsEmptyList() {
        mockAuthenticatedUser(mockUser);

        when(accountRepository.findByUserId(mockUser.getId()))
                .thenReturn(List.of());

        List<AccountResponse> result = accountService.getMyAccounts();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(accountRepository, times(1))
                .findByUserId(mockUser.getId());
    }

    @Test
    @DisplayName("Get Available Balance Account Not Found Throws Exception")
    void getAvailableBalance_NotFound_ThrowsException() {
        mockAuthenticatedUser(mockUser);

        when(accountRepository.findByAccountNumber("BF9999999999"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.getAvailableBalance("BF9999999999")
        );

        assertEquals("Account not found", exception.getMessage());

        verify(accountRepository, times(1))
                .findByAccountNumber("BF9999999999");
    }

    @Test
    @DisplayName("Toggle Account Status Active Account Freezes Account")
    void toggleAccountStatus_ActiveAccount_FreezesAccount() {
        mockAuthenticatedUser(mockUser);

        mockAccount.setAccountStatus(Account.AccountStatus.ACTIVE);

        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber()))
                .thenReturn(Optional.of(mockAccount));

        when(accountRepository.save(mockAccount))
                .thenReturn(mockAccount);

        AccountResponse response =
                accountService.toggleAccountStatus(mockAccount.getAccountNumber());

        assertNotNull(response);
        assertEquals(Account.AccountStatus.FROZEN.name(), response.accountStatus());
        assertEquals(Account.AccountStatus.FROZEN, mockAccount.getAccountStatus());

        verify(accountRepository, times(1)).save(mockAccount);

        verify(auditLogService, times(1)).log(
                AuditAction.ACCOUNT_FROZEN,
                "Account " + mockAccount.getAccountNumber() + " frozen"
        );
    }

    @Test
    @DisplayName("Toggle Account Status Frozen Account Activates Account")
    void toggleAccountStatus_FrozenAccount_ActivatesAccount() {
        mockAuthenticatedUser(mockUser);

        mockAccount.setAccountStatus(Account.AccountStatus.FROZEN);

        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber()))
                .thenReturn(Optional.of(mockAccount));

        when(accountRepository.save(mockAccount))
                .thenReturn(mockAccount);

        AccountResponse response =
                accountService.toggleAccountStatus(mockAccount.getAccountNumber());

        assertNotNull(response);
        assertEquals(Account.AccountStatus.ACTIVE.name(), response.accountStatus());
        assertEquals(Account.AccountStatus.ACTIVE, mockAccount.getAccountStatus());

        verify(accountRepository, times(1)).save(mockAccount);

        verify(auditLogService, times(1)).log(
                AuditAction.ACCOUNT_ACTIVATED,
                "Account " + mockAccount.getAccountNumber() + " activated"
        );
    }

    @Test
    @DisplayName("Toggle Account Status Another User Account Throws Access Denied")
    void toggleAccountStatus_OtherUsersAccount_ThrowsAccessDenied() {
        mockAuthenticatedUser(mockOtherUser);

        mockAccount.setAccountStatus(Account.AccountStatus.ACTIVE);

        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber()))
                .thenReturn(Optional.of(mockAccount));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> accountService.toggleAccountStatus(mockAccount.getAccountNumber())
        );

        assertEquals(
                "You are not authorized to modify this account",
                exception.getMessage()
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(auditLogService, never()).log(any(), anyString());
    }

    @Test
    @DisplayName("Toggle Account Status Inactive Account Throws Exception")
    void toggleAccountStatus_InactiveAccount_ThrowsException() {
        mockAuthenticatedUser(mockUser);

        mockAccount.setAccountStatus(Account.AccountStatus.INACTIVE);

        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber()))
                .thenReturn(Optional.of(mockAccount));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> accountService.toggleAccountStatus(mockAccount.getAccountNumber())
        );

        assertEquals(
                "Inactive accounts cannot be activated or frozen.",
                exception.getMessage()
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(auditLogService, never()).log(any(), anyString());
    }

    @Test
    @DisplayName("Toggle Account Status Account Not Found Throws Exception")
    void toggleAccountStatus_AccountNotFound_ThrowsException() {
        mockAuthenticatedUser(mockUser);

        when(accountRepository.findByAccountNumber("BF9999999999"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.toggleAccountStatus("BF9999999999")
        );

        assertEquals(
                "Account not found with number: BF9999999999",
                exception.getMessage()
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(auditLogService, never()).log(any(), anyString());
    }

    @Test
    @DisplayName("Get All Accounts For Admin Size Greater Than 100 Throws Exception")
    void getAllAccountsForAdmin_SizeGreaterThan100_ThrowsException() {
        mockAuthenticatedUser(mockAdminUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.getAllAccountsForAdmin(
                        0,
                        101,
                        null,
                        null
                )
        );

        assertEquals("Maximum page size is 100", exception.getMessage());

        verify(accountRepository, never())
                .findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("Get Account Summary For Admin Success")
    void getAccountSummaryForAdmin_Success() {
        mockAuthenticatedUser(mockAdminUser);

        when(accountRepository.countByAccountStatus(Account.AccountStatus.ACTIVE))
                .thenReturn(10L);

        when(accountRepository.countByAccountStatus(Account.AccountStatus.FROZEN))
                .thenReturn(3L);

        when(accountRepository.countByAccountType(Account.AccountType.SAVINGS))
                .thenReturn(8L);

        when(accountRepository.countByAccountType(Account.AccountType.CURRENT))
                .thenReturn(5L);

        AccountSummaryResponse response =
                accountService.getAccountSummaryForAdmin();

        assertNotNull(response);

        assertEquals(10L, response.activeAccounts());
        assertEquals(3L, response.frozenAccounts());
        assertEquals(8L, response.savingsAccounts());
        assertEquals(5L, response.currentAccounts());

        verify(accountRepository, times(1))
                .countByAccountStatus(Account.AccountStatus.ACTIVE);

        verify(accountRepository, times(1))
                .countByAccountStatus(Account.AccountStatus.FROZEN);

        verify(accountRepository, times(1))
                .countByAccountType(Account.AccountType.SAVINGS);

        verify(accountRepository, times(1))
                .countByAccountType(Account.AccountType.CURRENT);
    }

    @Test
    @DisplayName("Freeze Account By Admin Active Account Success")
    void freezeAccountByAdmin_ActiveAccount_Success() {
        mockAccount.setAccountStatus(Account.AccountStatus.ACTIVE);

        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber()))
                .thenReturn(Optional.of(mockAccount));

        when(accountRepository.save(mockAccount))
                .thenReturn(mockAccount);

        AccountResponse response =
                accountService.freezeAccountByAdmin(mockAccount.getAccountNumber());

        assertNotNull(response);
        assertEquals(Account.AccountStatus.FROZEN.name(), response.accountStatus());
        assertEquals(Account.AccountStatus.FROZEN, mockAccount.getAccountStatus());

        verify(accountRepository, times(1)).save(mockAccount);

        verify(auditLogService, times(1)).log(
                AuditAction.ACCOUNT_FROZEN,
                "Account " + mockAccount.getAccountNumber() + " frozen"
        );
    }

    @Test
    @DisplayName("Freeze Account By Admin Already Frozen Throws Exception")
    void freezeAccountByAdmin_AlreadyFrozen_ThrowsException() {
        mockAccount.setAccountStatus(Account.AccountStatus.FROZEN);

        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber()))
                .thenReturn(Optional.of(mockAccount));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> accountService.freezeAccountByAdmin(mockAccount.getAccountNumber())
        );

        assertEquals("Account is already frozen.", exception.getMessage());

        verify(accountRepository, never()).save(any(Account.class));
        verify(auditLogService, never()).log(any(), anyString());
    }

    @Test
    @DisplayName("Freeze Account By Admin Inactive Account Throws Exception")
    void freezeAccountByAdmin_InactiveAccount_ThrowsException() {
        mockAccount.setAccountStatus(Account.AccountStatus.INACTIVE);

        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber()))
                .thenReturn(Optional.of(mockAccount));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> accountService.freezeAccountByAdmin(mockAccount.getAccountNumber())
        );

        assertEquals("Inactive account cannot be frozen.", exception.getMessage());

        verify(accountRepository, never()).save(any(Account.class));
        verify(auditLogService, never()).log(any(), anyString());
    }

    @Test
    @DisplayName("Freeze Account By Admin Account Not Found Throws Exception")
    void freezeAccountByAdmin_AccountNotFound_ThrowsException() {
        when(accountRepository.findByAccountNumber("BF9999999999"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.freezeAccountByAdmin("BF9999999999")
        );

        assertEquals(
                "Account not found with number: BF9999999999",
                exception.getMessage()
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(auditLogService, never()).log(any(), anyString());
    }

    @Test
    @DisplayName("Unfreeze Account By Admin Frozen Account Success")
    void unfreezeAccountByAdmin_FrozenAccount_Success() {
        mockAccount.setAccountStatus(Account.AccountStatus.FROZEN);

        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber()))
                .thenReturn(Optional.of(mockAccount));

        when(accountRepository.save(mockAccount))
                .thenReturn(mockAccount);

        AccountResponse response =
                accountService.unfreezeAccountByAdmin(mockAccount.getAccountNumber());

        assertNotNull(response);
        assertEquals(Account.AccountStatus.ACTIVE.name(), response.accountStatus());
        assertEquals(Account.AccountStatus.ACTIVE, mockAccount.getAccountStatus());

        verify(accountRepository, times(1)).save(mockAccount);

        verify(auditLogService, times(1)).log(
                AuditAction.ACCOUNT_ACTIVATED,
                "Account " + mockAccount.getAccountNumber() + " activated"
        );
    }

    @Test
    @DisplayName("Unfreeze Account By Admin Already Active Throws Exception")
    void unfreezeAccountByAdmin_AlreadyActive_ThrowsException() {
        mockAccount.setAccountStatus(Account.AccountStatus.ACTIVE);

        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber()))
                .thenReturn(Optional.of(mockAccount));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> accountService.unfreezeAccountByAdmin(mockAccount.getAccountNumber())
        );

        assertEquals("Account is already active.", exception.getMessage());

        verify(accountRepository, never()).save(any(Account.class));
        verify(auditLogService, never()).log(any(), anyString());
    }

    @Test
    @DisplayName("Unfreeze Account By Admin Inactive Account Throws Exception")
    void unfreezeAccountByAdmin_InactiveAccount_ThrowsException() {
        mockAccount.setAccountStatus(Account.AccountStatus.INACTIVE);

        when(accountRepository.findByAccountNumber(mockAccount.getAccountNumber()))
                .thenReturn(Optional.of(mockAccount));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> accountService.unfreezeAccountByAdmin(mockAccount.getAccountNumber())
        );

        assertEquals(
                "Inactive account cannot be activated.",
                exception.getMessage()
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(auditLogService, never()).log(any(), anyString());
    }

    @Test
    @DisplayName("Unfreeze Account By Admin Account Not Found Throws Exception")
    void unfreezeAccountByAdmin_AccountNotFound_ThrowsException() {
        when(accountRepository.findByAccountNumber("BF9999999999"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.unfreezeAccountByAdmin("BF9999999999")
        );

        assertEquals(
                "Account not found with number: BF9999999999",
                exception.getMessage()
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(auditLogService, never()).log(any(), anyString());
    }


}
