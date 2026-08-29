package com.bankflow.service;

import com.bankflow.dto.AdminCardResponse;
import com.bankflow.dto.CardResponse;
import com.bankflow.dto.CardSummaryResponse;
import com.bankflow.dto.IssueCardRequest;
import com.bankflow.entity.Account;
import com.bankflow.entity.AuditAction;
import com.bankflow.entity.Card;
import com.bankflow.entity.Card.CardStatus;
import com.bankflow.entity.Card.CardType;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.CardRepository;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CardService cardService;

    private User mockUser;
    private User mockOtherUser;
    private User mockAdminUser;

    private Account mockAccount;
    private Card mockCard;

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
                .accountType(Account.AccountType.SAVINGS)
                .branchName("Mumbai Main Branch")
                .currentBalance(new BigDecimal("50000.00"))
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build();

        mockCard = Card.builder()
                .id(100L)
                .cardNumber("4111222233334444")
                .account(mockAccount)
                .cardType(CardType.DEBIT)
                .cardStatus(CardStatus.ACTIVE)
                .cardHolderName("John Doe")
                .expiryDate(LocalDate.now().plusYears(5))
                .cvv("123")
                .dailyLimit(new BigDecimal("10000.00"))
                .build();

        lenient()
                .when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(User user) {
        when(authentication.getName()).thenReturn(user.getEmail());

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
    }

    // =========================================================
    // ISSUE CARD
    // =========================================================

    @Test
    @DisplayName("Issue Card - Success")
    void issueCard_Success() {

        mockAuthenticatedUser(mockUser);

        IssueCardRequest request =
                new IssueCardRequest(
                        "BF1234567890",
                        CardType.DEBIT,
                        new BigDecimal("5000.00")
                );

        when(accountRepository.findByAccountNumber("BF1234567890"))
                .thenReturn(Optional.of(mockAccount));

        when(cardRepository.existsByAccountIdAndCardType(
                10L,
                CardType.DEBIT
        )).thenReturn(false);

        when(cardRepository.findByCardNumber(any()))
                .thenReturn(Optional.empty());

        when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response = cardService.issueCard(request);

        assertNotNull(response);

        assertEquals(
                "BF1234567890",
                response.accountNumber()
        );

        assertEquals(
                CardType.DEBIT,
                response.cardType()
        );

        assertEquals(
                CardStatus.ACTIVE,
                response.cardStatus()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                response.dailyLimit()
        );

        assertNotNull(response.maskedCardNumber());

        assertTrue(
                response.maskedCardNumber().startsWith("4")
        );

        assertTrue(
                response.maskedCardNumber().contains("********")
        );

        verify(cardRepository, times(1))
                .save(any(Card.class));

        verify(auditLogService, times(1))
                .log(
                        eq(AuditAction.CARD_ISSUED),
                        anyString()
                );
    }

    @Test
    @DisplayName("Issue Card - Account Not Found")
    void issueCard_AccountNotFound() {

        mockAuthenticatedUser(mockUser);

        IssueCardRequest request =
                new IssueCardRequest(
                        "BF9999999999",
                        CardType.DEBIT,
                        new BigDecimal("5000.00")
                );

        when(accountRepository.findByAccountNumber("BF9999999999"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> cardService.issueCard(request)
        );

        verify(cardRepository, never())
                .save(any(Card.class));
    }

    @Test
    @DisplayName("Issue Card - Unauthorized Account")
    void issueCard_UnauthorizedAccount() {

        mockAuthenticatedUser(mockUser);

        Account unauthorizedAccount = Account.builder()
                .id(20L)
                .accountNumber("BF9999999999")
                .user(mockOtherUser)
                .accountType(Account.AccountType.SAVINGS)
                .branchName("Pune Branch")
                .currentBalance(new BigDecimal("10000.00"))
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build();

        IssueCardRequest request =
                new IssueCardRequest(
                        "BF9999999999",
                        CardType.DEBIT,
                        new BigDecimal("5000.00")
                );

        when(accountRepository.findByAccountNumber("BF9999999999"))
                .thenReturn(Optional.of(unauthorizedAccount));

        assertThrows(
                AccessDeniedException.class,
                () -> cardService.issueCard(request)
        );

        verify(cardRepository, never())
                .save(any(Card.class));
    }

    @Test
    @DisplayName("Issue Card - Inactive Account")
    void issueCard_InactiveAccount() {

        mockAuthenticatedUser(mockUser);

        mockAccount.setAccountStatus(
                Account.AccountStatus.INACTIVE
        );

        IssueCardRequest request =
                new IssueCardRequest(
                        "BF1234567890",
                        CardType.DEBIT,
                        new BigDecimal("5000.00")
                );

        when(accountRepository.findByAccountNumber("BF1234567890"))
                .thenReturn(Optional.of(mockAccount));

        assertThrows(
                IllegalStateException.class,
                () -> cardService.issueCard(request)
        );

        verify(cardRepository, never())
                .save(any(Card.class));
    }

    @Test
    @DisplayName("Issue Card - Card Type Already Exists")
    void issueCard_CardAlreadyExists() {

        mockAuthenticatedUser(mockUser);

        IssueCardRequest request =
                new IssueCardRequest(
                        "BF1234567890",
                        CardType.DEBIT,
                        new BigDecimal("5000.00")
                );

        when(accountRepository.findByAccountNumber("BF1234567890"))
                .thenReturn(Optional.of(mockAccount));

        when(cardRepository.existsByAccountIdAndCardType(
                10L,
                CardType.DEBIT
        )).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> cardService.issueCard(request)
        );

        verify(cardRepository, never())
                .save(any(Card.class));
    }

    // =========================================================
    // GET MY CARDS
    // =========================================================

    @Test
    @DisplayName("Get My Cards - Success")
    void getMyCards_Success() {

        mockAuthenticatedUser(mockUser);

        when(cardRepository.findByAccountUserId(mockUser.getId()))
                .thenReturn(List.of(mockCard));

        List<CardResponse> responses =
                cardService.getMyCards();

        assertNotNull(responses);

        assertEquals(
                1,
                responses.size()
        );

        assertEquals(
                "4111********4444",
                responses.get(0).maskedCardNumber()
        );

        assertEquals(
                CardStatus.ACTIVE,
                responses.get(0).cardStatus()
        );

        assertEquals(
                CardType.DEBIT,
                responses.get(0).cardType()
        );
    }

    @Test
    @DisplayName("Get My Cards - No Cards")
    void getMyCards_NoCards() {

        mockAuthenticatedUser(mockUser);

        when(cardRepository.findByAccountUserId(mockUser.getId()))
                .thenReturn(Collections.emptyList());

        List<CardResponse> responses =
                cardService.getMyCards();

        assertNotNull(responses);

        assertTrue(responses.isEmpty());
    }

    // =========================================================
    // TOGGLE CARD STATUS
    // =========================================================

    @Test
    @DisplayName("Toggle Card Status - Active To Frozen")
    void toggleCardStatus_ActiveToFrozen() {

        mockAuthenticatedUser(mockUser);

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(mockCard));

        when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response =
                cardService.toggleCardStatus(100L);

        assertNotNull(response);

        assertEquals(
                CardStatus.FROZEN,
                response.cardStatus()
        );

        verify(auditLogService)
                .log(
                        eq(AuditAction.CARD_FROZEN),
                        anyString()
                );
    }

    @Test
    @DisplayName("Toggle Card Status - Frozen To Active")
    void toggleCardStatus_FrozenToActive() {

        mockAuthenticatedUser(mockUser);

        mockCard.setCardStatus(CardStatus.FROZEN);

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(mockCard));

        when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response =
                cardService.toggleCardStatus(100L);

        assertNotNull(response);

        assertEquals(
                CardStatus.ACTIVE,
                response.cardStatus()
        );

        verify(auditLogService)
                .log(
                        eq(AuditAction.CARD_ACTIVATED),
                        anyString()
                );
    }

    @Test
    @DisplayName("Toggle Card Status - Unauthorized User")
    void toggleCardStatus_UnauthorizedUser() {

        mockAuthenticatedUser(mockOtherUser);

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(mockCard));

        assertThrows(
                AccessDeniedException.class,
                () -> cardService.toggleCardStatus(100L)
        );

        verify(cardRepository, never())
                .save(any(Card.class));
    }

    @Test
    @DisplayName("Toggle Card Status - Blocked Card Cannot Be Modified")
    void toggleCardStatus_BlockedCard() {

        mockAuthenticatedUser(mockUser);

        mockCard.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(mockCard));

        assertThrows(
                IllegalStateException.class,
                () -> cardService.toggleCardStatus(100L)
        );

        verify(cardRepository, never())
                .save(any(Card.class));
    }

    @Test
    @DisplayName("Toggle Card Status - Inactive Account")
    void toggleCardStatus_InactiveAccount() {

        mockAuthenticatedUser(mockUser);

        mockAccount.setAccountStatus(
                Account.AccountStatus.INACTIVE
        );

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(mockCard));

        assertThrows(
                IllegalStateException.class,
                () -> cardService.toggleCardStatus(100L)
        );

        verify(cardRepository, never())
                .save(any(Card.class));
    }

    // =========================================================
    // UPDATE DAILY LIMIT
    // =========================================================

    @Test
    @DisplayName("Update Daily Limit - Success")
    void updateDailyLimit_Success() {

        mockAuthenticatedUser(mockUser);

        BigDecimal newLimit =
                new BigDecimal("25000.00");

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(mockCard));

        when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response =
                cardService.updateDailyLimit(
                        100L,
                        newLimit
                );

        assertNotNull(response);

        assertEquals(
                newLimit,
                response.dailyLimit()
        );

        verify(auditLogService)
                .log(
                        eq(AuditAction.CARD_LIMIT_UPDATED),
                        anyString()
                );
    }

    @Test
    @DisplayName("Update Daily Limit - Unauthorized User")
    void updateDailyLimit_UnauthorizedUser() {

        mockAuthenticatedUser(mockOtherUser);

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(mockCard));

        assertThrows(
                AccessDeniedException.class,
                () -> cardService.updateDailyLimit(
                        100L,
                        new BigDecimal("25000.00")
                )
        );

        verify(cardRepository, never())
                .save(any(Card.class));
    }

    @Test
    @DisplayName("Update Daily Limit - Frozen Card")
    void updateDailyLimit_FrozenCard() {

        mockAuthenticatedUser(mockUser);

        mockCard.setCardStatus(CardStatus.FROZEN);

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(mockCard));

        assertThrows(
                IllegalStateException.class,
                () -> cardService.updateDailyLimit(
                        100L,
                        new BigDecimal("25000.00")
                )
        );

        verify(cardRepository, never())
                .save(any(Card.class));
    }

    @Test
    @DisplayName("Update Daily Limit - Inactive Account")
    void updateDailyLimit_InactiveAccount() {

        mockAuthenticatedUser(mockUser);

        mockAccount.setAccountStatus(
                Account.AccountStatus.INACTIVE
        );

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(mockCard));

        assertThrows(
                IllegalStateException.class,
                () -> cardService.updateDailyLimit(
                        100L,
                        new BigDecimal("25000.00")
                )
        );

        verify(cardRepository, never())
                .save(any(Card.class));
    }

    // =========================================================
    // ADMIN - GET ALL CARDS
    // =========================================================

    @Test
    @DisplayName("Get All Cards For Admin - Success")
    void getAllCardsForAdmin_Success() {

        Page<Card> page =
                new PageImpl<>(
                        List.of(mockCard),
                        PageRequest.of(0, 10),
                        1
                );

        when(cardRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        Page<AdminCardResponse> result =
                cardService.getAllCardsForAdmin(
                        0,
                        10,
                        null,
                        null
                );

        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements()
        );

        AdminCardResponse response =
                result.getContent().get(0);

        assertEquals(
                100L,
                response.id()
        );

        assertEquals(
                "John Doe",
                response.customerName()
        );

        assertEquals(
                "BF1234567890",
                response.accountNumber()
        );

        assertEquals(
                "4111********4444",
                response.maskedCardNumber()
        );

        assertEquals(
                CardType.DEBIT,
                response.cardType()
        );

        assertEquals(
                CardStatus.ACTIVE,
                response.cardStatus()
        );

        assertEquals(
                new BigDecimal("10000.00"),
                response.dailyLimit()
        );

        assertEquals(
                mockCard.getExpiryDate(),
                response.expiryDate()
        );
    }

    @Test
    @DisplayName("Get All Cards For Admin - Empty Result")
    void getAllCardsForAdmin_EmptyResult() {

        Page<Card> emptyPage =
                new PageImpl<>(
                        Collections.emptyList(),
                        PageRequest.of(0, 10),
                        0
                );

        when(cardRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class)
        )).thenReturn(emptyPage);

        Page<AdminCardResponse> result =
                cardService.getAllCardsForAdmin(
                        0,
                        10,
                        null,
                        null
                );

        assertNotNull(result);

        assertTrue(result.isEmpty());

        assertEquals(
                0,
                result.getTotalElements()
        );
    }

    // =========================================================
    // ADMIN - CARD SUMMARY
    // =========================================================

    @Test
    @DisplayName("Get Card Summary For Admin - Success")
    void getCardSummaryForAdmin_Success() {

        when(cardRepository.count())
                .thenReturn(10L);

        when(cardRepository.countByCardStatus(CardStatus.ACTIVE))
                .thenReturn(6L);

        when(cardRepository.countByCardStatus(CardStatus.BLOCKED))
                .thenReturn(2L);

        when(cardRepository.countByCardStatus(CardStatus.FROZEN))
                .thenReturn(2L);

        CardSummaryResponse response =
                cardService.getCardSummaryForAdmin();

        assertNotNull(response);

        assertEquals(
                10L,
                response.totalCards()
        );

        assertEquals(
                6L,
                response.activeCards()
        );

        assertEquals(
                2L,
                response.blockedCards()
        );

        assertEquals(
                2L,
                response.frozenCards()
        );
    }

    // =========================================================
    // ADMIN - BLOCK CARD
    // =========================================================

    @Test
    @DisplayName("Block Card By Admin - Success")
    void blockCardByAdmin_Success() {

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(mockCard));

        when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response =
                cardService.blockCardByAdmin(100L);

        assertNotNull(response);

        assertEquals(
                CardStatus.BLOCKED,
                response.cardStatus()
        );

        verify(cardRepository)
                .save(mockCard);

        verify(auditLogService)
                .log(
                        eq(AuditAction.CARD_BLOCKED),
                        anyString()
                );
    }

    @Test
    @DisplayName("Block Card By Admin - Already Blocked")
    void blockCardByAdmin_AlreadyBlocked() {

        mockCard.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(mockCard));

        assertThrows(
                IllegalStateException.class,
                () -> cardService.blockCardByAdmin(100L)
        );

        verify(cardRepository, never())
                .save(any(Card.class));
    }

    @Test
    @DisplayName("Block Card By Admin - Card Not Found")
    void blockCardByAdmin_CardNotFound() {

        when(cardRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> cardService.blockCardByAdmin(999L)
        );

        verify(cardRepository, never())
                .save(any(Card.class));
    }

    // =========================================================
    // ADMIN - UNBLOCK CARD
    // =========================================================

    @Test
    @DisplayName("Unblock Card By Admin - Success")
    void unblockCardByAdmin_Success() {

        mockCard.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(mockCard));

        when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response =
                cardService.unblockCardByAdmin(100L);

        assertNotNull(response);

        assertEquals(
                CardStatus.ACTIVE,
                response.cardStatus()
        );

        verify(cardRepository)
                .save(mockCard);

        verify(auditLogService)
                .log(
                        eq(AuditAction.CARD_UNBLOCKED),
                        anyString()
                );
    }

    @Test
    @DisplayName("Unblock Card By Admin - Non Blocked Card")
    void unblockCardByAdmin_NotBlocked() {

        mockCard.setCardStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(mockCard));

        assertThrows(
                IllegalStateException.class,
                () -> cardService.unblockCardByAdmin(100L)
        );

        verify(cardRepository, never())
                .save(any(Card.class));
    }

    @Test
    @DisplayName("Unblock Card By Admin - Card Not Found")
    void unblockCardByAdmin_CardNotFound() {

        when(cardRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> cardService.unblockCardByAdmin(999L)
        );

        verify(cardRepository, never())
                .save(any(Card.class));
    }
}