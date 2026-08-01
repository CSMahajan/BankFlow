package com.bankflow.service;

import com.bankflow.dto.CreateScheduledTransferRequest;
import com.bankflow.dto.FundTransferRequest;
import com.bankflow.dto.ScheduledTransferResponse;
import com.bankflow.entity.Account;
import com.bankflow.entity.ScheduledTransfer;
import com.bankflow.entity.ScheduledTransfer.Frequency;
import com.bankflow.entity.ScheduledTransfer.TransferStatus;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.ScheduledTransferRepository;
import com.bankflow.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledTransferServiceTest {

    @Mock
    private ScheduledTransferRepository scheduledTransferRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FundTransferService transferService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ScheduledTransferService scheduledTransferService;

    private User mockUser;
    private Account mockSourceAccount;
    private Account mockRecipientAccount;
    private ScheduledTransfer mockScheduledTransfer;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .role(User.Role.CUSTOMER)
                .build();

        mockSourceAccount = Account.builder()
                .id(10L)
                .accountNumber("SRC123456789")
                .user(mockUser)
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build();

        mockRecipientAccount = Account.builder()
                .id(11L)
                .accountNumber("REC987654321")
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build();

        mockScheduledTransfer = ScheduledTransfer.builder()
                .id(50L)
                .user(mockUser)
                .sourceAccountNumber("SRC123456789")
                .recipientAccountNumber("REC987654321")
                .amount(new BigDecimal("250.00"))
                .description("Monthly Rent")
                .frequency(Frequency.MONTHLY)
                .status(TransferStatus.ACTIVE)
                .nextExecutionDate(LocalDate.now())
                .build();

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser() {
        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
    }

    @Test
    @DisplayName("Create Scheduled Transfer - Success")
    void createScheduledTransfer_Success() {
        mockAuthenticatedUser();
        CreateScheduledTransferRequest request = new CreateScheduledTransferRequest(
                "SRC123456789",
                "REC987654321",
                new BigDecimal("250.00"),
                "Monthly Rent",
                Frequency.MONTHLY,
                LocalDate.now()
        );

        when(accountRepository.findByAccountNumber("SRC123456789")).thenReturn(Optional.of(mockSourceAccount));
        when(accountRepository.findByAccountNumber("REC987654321")).thenReturn(Optional.of(mockRecipientAccount));
        when(scheduledTransferRepository.save(any(ScheduledTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScheduledTransferResponse response = scheduledTransferService.createScheduledTransfer(request);

        assertNotNull(response);
        assertEquals("SRC123456789", response.sourceAccountNumber());
        assertEquals("REC987654321", response.recipientAccountNumber());
        assertEquals(new BigDecimal("250.00"), response.amount());
        assertEquals(Frequency.MONTHLY, response.frequency());
        assertEquals(TransferStatus.ACTIVE, response.status());

        verify(scheduledTransferRepository, times(1)).save(any(ScheduledTransfer.class));
    }

    @Test
    @DisplayName("Create Scheduled Transfer - Throws AccessDeniedException When Source Belongs To Another User")
    void createScheduledTransfer_UnauthorizedSource() {
        mockAuthenticatedUser();
        User otherUser = User.builder().id(99L).email("other@example.com").build();
        Account unauthorizedAccount = Account.builder()
                .id(20L)
                .accountNumber("SRC123456789")
                .user(otherUser)
                .build();

        CreateScheduledTransferRequest request = new CreateScheduledTransferRequest(
                "SRC123456789",
                "REC987654321",
                new BigDecimal("250.00"),
                "Monthly Rent",
                Frequency.MONTHLY,
                LocalDate.now()
        );

        when(accountRepository.findByAccountNumber("SRC123456789")).thenReturn(Optional.of(unauthorizedAccount));

        assertThrows(AccessDeniedException.class, () -> scheduledTransferService.createScheduledTransfer(request));
        verify(scheduledTransferRepository, never()).save(any(ScheduledTransfer.class));
    }

    @Test
    @DisplayName("Get My Scheduled Transfers - Success")
    void getMyScheduledTransfers_Success() {
        mockAuthenticatedUser();
        when(scheduledTransferRepository.findByUserId(mockUser.getId())).thenReturn(List.of(mockScheduledTransfer));

        List<ScheduledTransferResponse> responses = scheduledTransferService.getMyScheduledTransfers();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("SRC123456789", responses.get(0).sourceAccountNumber());
        assertEquals(Frequency.MONTHLY, responses.get(0).frequency());
    }

    @Test
    @DisplayName("Cancel Scheduled Transfer - Success")
    void cancelScheduledTransfer_Success() {
        mockAuthenticatedUser();
        when(scheduledTransferRepository.findById(50L)).thenReturn(Optional.of(mockScheduledTransfer));
        when(scheduledTransferRepository.save(any(ScheduledTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScheduledTransferResponse response = scheduledTransferService.cancelScheduledTransfer(50L);

        assertNotNull(response);
        assertEquals(TransferStatus.CANCELLED, response.status());
    }

    @Test
    @DisplayName("Process Due Transfers - Executes Funds Transfer and Updates Next Execution Date")
    void processDueTransfers_Success() {
        LocalDate today = LocalDate.now();
        when(scheduledTransferRepository.findByStatusAndNextExecutionDateLessThanEqual(TransferStatus.ACTIVE, today))
                .thenReturn(List.of(mockScheduledTransfer));

        scheduledTransferService.processDueTransfers();

        // Verify transferService was called with the right request
        ArgumentCaptor<FundTransferRequest> captor = ArgumentCaptor.forClass(FundTransferRequest.class);
        verify(transferService, times(1)).transferFunds(captor.capture());

        FundTransferRequest capturedRequest = captor.getValue();
        assertEquals("SRC123456789", capturedRequest.sourceAccountNumber());
        assertEquals("REC987654321", capturedRequest.targetAccountNumber());
        assertEquals(new BigDecimal("250.00"), capturedRequest.amount());

        // Verify next execution date was advanced by 1 month
        ArgumentCaptor<ScheduledTransfer> transferCaptor = ArgumentCaptor.forClass(ScheduledTransfer.class);
        verify(scheduledTransferRepository, times(1)).save(transferCaptor.capture());
        assertEquals(today.plusMonths(1), transferCaptor.getValue().getNextExecutionDate());
    }
}
