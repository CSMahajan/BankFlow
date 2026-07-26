package com.bankflow.service;

import com.bankflow.dto.AccountResponse;
import com.bankflow.dto.CreateAccountRequest;
import com.bankflow.entity.Account;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.TransactionRepository;
import com.bankflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@bankflow.com")
                .fullName("Test User")
                .role(User.Role.CUSTOMER)
                .build();

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("user@bankflow.com");
        SecurityContextHolder.setContext(securityContext);
        lenient().when(userRepository.findByEmail("user@bankflow.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    void createAccount_WithInitialDeposit_Success() {
        CreateAccountRequest request = new CreateAccountRequest(
                Account.AccountType.SAVINGS,
                "Main Branch",
                new BigDecimal("15000.00")
        );

        Account savedAccount = Account.builder()
                .id(10L)
                .accountNumber("BF1234567890")
                .user(testUser)
                .accountType(Account.AccountType.SAVINGS)
                .branchName("Main Branch")
                .currentBalance(new BigDecimal("15000.00"))
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build();

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals("BF1234567890", response.accountNumber());
        assertEquals(new BigDecimal("15000.00"), response.currentBalance());

        verify(accountRepository, times(1)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any());
    }
}
