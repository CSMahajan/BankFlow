package com.bankflow.service;

import com.bankflow.dto.FundTransferRequest;
import com.bankflow.dto.FundTransferResponse;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundTransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FundTransferService fundTransferService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private User mockSourceUser;
    private User mockTargetUser;
    private Account mockSourceAccount;
    private Account mockTargetAccount;

    @BeforeEach
    void setUp() {
        mockSourceUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .role(User.Role.CUSTOMER)
                .build();

        mockTargetUser = User.builder()
                .id(2L)
                .fullName("Jane Smith")
                .email("jane@example.com")
                .role(User.Role.CUSTOMER)
                .build();

        mockSourceAccount = Account.builder()
                .id(10L)
                .accountNumber("BF1000000001")
                .user(mockSourceUser)
                .currentBalance(new BigDecimal("10000.00"))
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build();

        mockTargetAccount = Account.builder()
                .id(20L)
                .accountNumber("BF2000000002")
                .user(mockTargetUser)
                .currentBalance(new BigDecimal("2000.00"))
                .accountStatus(Account.AccountStatus.ACTIVE)
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
    // SUCCESSFUL TRANSFER TESTS
    // ==========================================

    @Test
    @DisplayName("Transfer Funds - Success With Remark")
    void transferFunds_Success_WithRemark() {
        mockAuthenticatedUser(mockSourceUser);
        FundTransferRequest request = new FundTransferRequest(
                "BF1000000001",
                "BF2000000002",
                new BigDecimal("3000.00"),
                "Rent Payment"
        );

        when(accountRepository.findByAccountNumber("BF1000000001")).thenReturn(Optional.of(mockSourceAccount));
        when(accountRepository.findByAccountNumber("BF2000000002")).thenReturn(Optional.of(mockTargetAccount));

        FundTransferResponse response = fundTransferService.transferFunds(request);

        assertNotNull(response);
        assertEquals("SUCCESS", response.status());
        assertEquals("BF1000000001", response.sourceAccountNumber());
        assertEquals("BF2000000002", response.targetAccountNumber());
        assertEquals(new BigDecimal("3000.00"), response.amount());

        // Verify account balances updated directly on entities
        assertEquals(new BigDecimal("7000.00"), mockSourceAccount.getCurrentBalance());
        assertEquals(new BigDecimal("5000.00"), mockTargetAccount.getCurrentBalance());

        verify(accountRepository, times(1)).save(mockSourceAccount);
        verify(accountRepository, times(1)).save(mockTargetAccount);

        // Verify debit & credit transactions were saved
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        List<Transaction> savedTxs = transactionCaptor.getAllValues();

        Transaction debitTx = savedTxs.stream()
                .filter(t -> t.getTransactionType() == Transaction.TransactionType.DEBIT)
                .findFirst().orElseThrow();
        Transaction creditTx = savedTxs.stream()
                .filter(t -> t.getTransactionType() == Transaction.TransactionType.CREDIT)
                .findFirst().orElseThrow();

        assertTrue(debitTx.getDescription().contains("Transfer to BF2000000002 | Rent Payment"));
        assertTrue(creditTx.getDescription().contains("Transfer from BF1000000001 | Rent Payment"));
    }


    @Test
    @DisplayName("Transfer Funds - Success Without Remark")
    void transferFunds_Success_WithoutRemark() {
        mockAuthenticatedUser(mockSourceUser);
        FundTransferRequest request = new FundTransferRequest(
                "BF1000000001",
                "BF2000000002",
                new BigDecimal("1000.00"),
                null
        );

        when(accountRepository.findByAccountNumber("BF1000000001")).thenReturn(Optional.of(mockSourceAccount));
        when(accountRepository.findByAccountNumber("BF2000000002")).thenReturn(Optional.of(mockTargetAccount));

        FundTransferResponse response = fundTransferService.transferFunds(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("9000.00"), mockSourceAccount.getCurrentBalance());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }


    // ==========================================
    // VALIDATION & FAILURE TESTS
    // ==========================================

    @Test
    @DisplayName("Transfer Funds Throws Exception - Same Source and Target Account")
    void transferFunds_SameAccount_ThrowsException() {
        mockAuthenticatedUser(mockSourceUser);
        FundTransferRequest request = new FundTransferRequest(
                "BF1000000001",
                "BF1000000001",
                new BigDecimal("1000.00"),
                "Self Transfer"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fundTransferService.transferFunds(request)
        );

        assertEquals("Source and destination accounts cannot be the same", ex.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Transfer Funds Throws Exception - Source Account Not Found")
    void transferFunds_SourceAccountNotFound_ThrowsException() {
        mockAuthenticatedUser(mockSourceUser);
        FundTransferRequest request = new FundTransferRequest(
                "BF9999999999",
                "BF2000000002",
                new BigDecimal("1000.00"),
                "Test"
        );

        when(accountRepository.findByAccountNumber("BF9999999999")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fundTransferService.transferFunds(request)
        );

        assertEquals("Source account not found", ex.getMessage());
    }

    @Test
    @DisplayName("Transfer Funds Throws Exception - Unauthorized Source Account Ownership")
    void transferFunds_UnownedSourceAccount_ThrowsException() {
        mockAuthenticatedUser(mockTargetUser); // Authenticated as Jane trying to transfer from John's account
        FundTransferRequest request = new FundTransferRequest(
                "BF1000000001",
                "BF2000000002",
                new BigDecimal("1000.00"),
                "Unauthorized"
        );

        when(accountRepository.findByAccountNumber("BF1000000001")).thenReturn(Optional.of(mockSourceAccount));

        assertThrows(AccessDeniedException.class, () ->
                fundTransferService.transferFunds(request)
        );
    }

    @Test
    @DisplayName("Transfer Funds Throws Exception - Source Account Inactive")
    void transferFunds_SourceAccountInactive_ThrowsException() {
        mockAuthenticatedUser(mockSourceUser);
        mockSourceAccount.setAccountStatus(Account.AccountStatus.INACTIVE);

        FundTransferRequest request = new FundTransferRequest(
                "BF1000000001",
                "BF2000000002",
                new BigDecimal("1000.00"),
                "Test"
        );

        when(accountRepository.findByAccountNumber("BF1000000001")).thenReturn(Optional.of(mockSourceAccount));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fundTransferService.transferFunds(request)
        );

        assertEquals("Source account is not active", ex.getMessage());
    }

    @Test
    @DisplayName("Transfer Funds Throws Exception - Insufficient Funds")
    void transferFunds_InsufficientFunds_ThrowsException() {
        mockAuthenticatedUser(mockSourceUser);
        FundTransferRequest request = new FundTransferRequest(
                "BF1000000001",
                "BF2000000002",
                new BigDecimal("50000.00"), // Exceeds balance of 10,000
                "Overdraw"
        );

        when(accountRepository.findByAccountNumber("BF1000000001")).thenReturn(Optional.of(mockSourceAccount));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fundTransferService.transferFunds(request)
        );

        assertEquals("Insufficient funds in source account", ex.getMessage());
    }

    @Test
    @DisplayName("Transfer Funds Throws Exception - Target Account Not Found")
    void transferFunds_TargetAccountNotFound_ThrowsException() {
        mockAuthenticatedUser(mockSourceUser);
        FundTransferRequest request = new FundTransferRequest(
                "BF1000000001",
                "BF8888888888",
                new BigDecimal("1000.00"),
                "Test"
        );

        when(accountRepository.findByAccountNumber("BF1000000001")).thenReturn(Optional.of(mockSourceAccount));
        when(accountRepository.findByAccountNumber("BF8888888888")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fundTransferService.transferFunds(request)
        );

        assertEquals("Destination account not found", ex.getMessage());
    }

    @Test
    @DisplayName("Transfer Funds Throws Exception - Target Account Inactive")
    void transferFunds_TargetAccountInactive_ThrowsException() {
        mockAuthenticatedUser(mockSourceUser);
        mockTargetAccount.setAccountStatus(Account.AccountStatus.INACTIVE);

        FundTransferRequest request = new FundTransferRequest(
                "BF1000000001",
                "BF2000000002",
                new BigDecimal("1000.00"),
                "Test"
        );

        when(accountRepository.findByAccountNumber("BF1000000001")).thenReturn(Optional.of(mockSourceAccount));
        when(accountRepository.findByAccountNumber("BF2000000002")).thenReturn(Optional.of(mockTargetAccount));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fundTransferService.transferFunds(request)
        );

        assertEquals("Destination account is inactive or frozen", ex.getMessage());
    }
}
