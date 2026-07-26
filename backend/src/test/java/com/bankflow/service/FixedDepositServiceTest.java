package com.bankflow.service;

import com.bankflow.dto.CreateFdRequest;
import com.bankflow.dto.FdResponse;
import com.bankflow.entity.Account;
import com.bankflow.entity.FixedDeposit;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.FixedDepositRepository;
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
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@bankflow.com")
                .fullName("Test User")
                .role(User.Role.CUSTOMER)
                .build();

        testAccount = Account.builder()
                .id(100L)
                .accountNumber("BF9999999999")
                .user(testUser)
                .currentBalance(new BigDecimal("50000.00"))
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build();

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("user@bankflow.com");
        SecurityContextHolder.setContext(securityContext);
        lenient().when(userRepository.findByEmail("user@bankflow.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    void createFixedDeposit_BelowMinimum_ThrowsException() {
        CreateFdRequest request = new CreateFdRequest(
                "BF9999999999",
                new BigDecimal("5000.00"), // Under Rs. 10,000 threshold
                1
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fdService.createFixedDeposit(request));

        assertTrue(ex.getMessage().contains("greater than Rs. 10,000"));
        verify(fdRepository, never()).save(any());
    }

    @Test
    void createFixedDeposit_Success() {
        CreateFdRequest request = new CreateFdRequest(
                "BF9999999999",
                new BigDecimal("20000.00"),
                3
        );

        when(accountRepository.findByAccountNumber("BF9999999999")).thenReturn(Optional.of(testAccount));

        FixedDeposit savedFd = FixedDeposit.builder()
                .id(50L)
                .fdNumber("FD12345678")
                .user(testUser)
                .sourceAccount(testAccount)
                .depositAmount(new BigDecimal("20000.00"))
                .interestRate(new BigDecimal("7.00"))
                .tenureYears(3)
                .depositDate(LocalDate.now())
                .maturityDate(LocalDate.now().plusYears(3))
                .maturityAmount(new BigDecimal("24628.80"))
                .status(FixedDeposit.FdStatus.ACTIVE)
                .build();

        when(fdRepository.save(any(FixedDeposit.class))).thenReturn(savedFd);

        FdResponse response = fdService.createFixedDeposit(request);

        assertNotNull(response);
        assertEquals("FD12345678", response.fdNumber());
        assertEquals(new BigDecimal("30000.00"), testAccount.getCurrentBalance()); // 50,000 - 20,000

        verify(transactionRepository, times(1)).save(any());
        verify(fdRepository, times(1)).save(any(FixedDeposit.class));
    }
}
