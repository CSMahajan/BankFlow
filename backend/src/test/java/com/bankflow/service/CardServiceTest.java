package com.bankflow.service;

import com.bankflow.dto.CardResponse;
import com.bankflow.dto.IssueCardRequest;
import com.bankflow.entity.Account;
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
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CardService cardService;

    private User mockUser;
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

        mockAccount = Account.builder()
                .id(10L)
                .accountNumber("BF1234567890")
                .user(mockUser)
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
    @DisplayName("Issue Card - Success")
    void issueCard_Success() {
        mockAuthenticatedUser();
        IssueCardRequest request = new IssueCardRequest("BF1234567890", CardType.DEBIT, new BigDecimal("5000.00"));

        when(accountRepository.findByAccountNumber("BF1234567890")).thenReturn(Optional.of(mockAccount));
        when(cardRepository.findByCardNumber(any())).thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response = cardService.issueCard(request);

        assertNotNull(response);
        assertEquals("BF1234567890", response.accountNumber());
        assertEquals(CardType.DEBIT, response.cardType());
        assertEquals(CardStatus.ACTIVE, response.cardStatus());

        // Assert that the masked card number follows the correct format (starts with 4, ends with 4 digits)
        assertNotNull(response.maskedCardNumber());
        assertTrue(response.maskedCardNumber().startsWith("4"));
        assertTrue(response.maskedCardNumber().contains("********"));

        assertEquals(new BigDecimal("5000.00"), response.dailyLimit());

        verify(cardRepository, times(1)).save(any(Card.class));
    }


    @Test
    @DisplayName("Issue Card - Throws Exception When Account Belongs To Another User")
    void issueCard_UnauthorizedAccount() {
        mockAuthenticatedUser();
        User anotherUser = User.builder().id(99L).email("other@example.com").build();
        Account unauthorizedAccount = Account.builder()
                .id(20L)
                .accountNumber("BF9999999999")
                .user(anotherUser)
                .accountStatus(Account.AccountStatus.ACTIVE)
                .build();

        IssueCardRequest request = new IssueCardRequest("BF9999999999", CardType.DEBIT, new BigDecimal("5000.00"));

        when(accountRepository.findByAccountNumber("BF9999999999")).thenReturn(Optional.of(unauthorizedAccount));

        assertThrows(AccessDeniedException.class, () -> cardService.issueCard(request));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Get My Cards - Success")
    void getMyCards_Success() {
        mockAuthenticatedUser();
        when(cardRepository.findByAccountUserId(mockUser.getId())).thenReturn(List.of(mockCard));

        List<CardResponse> responses = cardService.getMyCards();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("4111********4444", responses.get(0).maskedCardNumber());
        assertEquals(CardStatus.ACTIVE, responses.get(0).cardStatus());
    }

    @Test
    @DisplayName("Toggle Card Status - Active to Frozen")
    void toggleCardStatus_ActiveToFrozen() {
        mockAuthenticatedUser();
        when(cardRepository.findById(100L)).thenReturn(Optional.of(mockCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response = cardService.toggleCardStatus(100L);

        assertNotNull(response);
        assertEquals(CardStatus.FROZEN, response.cardStatus());
    }

    @Test
    @DisplayName("Toggle Card Status - Frozen to Active")
    void toggleCardStatus_FrozenToActive() {
        mockAuthenticatedUser();
        mockCard.setCardStatus(CardStatus.FROZEN);
        when(cardRepository.findById(100L)).thenReturn(Optional.of(mockCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response = cardService.toggleCardStatus(100L);

        assertNotNull(response);
        assertEquals(CardStatus.ACTIVE, response.cardStatus());
    }

    @Test
    @DisplayName("Update Daily Limit - Success")
    void updateDailyLimit_Success() {
        mockAuthenticatedUser();
        BigDecimal newLimit = new BigDecimal("25000.00");
        when(cardRepository.findById(100L)).thenReturn(Optional.of(mockCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response = cardService.updateDailyLimit(100L, newLimit);

        assertNotNull(response);
        assertEquals(newLimit, response.dailyLimit());
    }
}
