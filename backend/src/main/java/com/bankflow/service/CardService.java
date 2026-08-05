package com.bankflow.service;

import com.bankflow.dto.AdminCardResponse;
import com.bankflow.dto.CardResponse;
import com.bankflow.dto.IssueCardRequest;
import com.bankflow.entity.Account;
import com.bankflow.entity.AuditAction;
import com.bankflow.entity.Card;
import com.bankflow.entity.Card.CardStatus;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.CardRepository;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final Random random = new Random();

    @Transactional
    public CardResponse issueCard(IssueCardRequest request) {
        User currentUser = getAuthenticatedUser();
        log.info("Issuing new card for user [{}] and account [{}]", currentUser.getEmail(), request.accountNumber());

        Account account = accountRepository.findByAccountNumber(request.accountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Account not found with number: " + request.accountNumber()));

        // Verify account ownership
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to issue a card for this account");
        }

        if (account.getAccountStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot issue a card for a non-active account");
        }

        if (cardRepository.existsByAccountIdAndCardType(
                account.getId(),
                request.cardType())) {

            throw new IllegalStateException(
                    request.cardType() + " card already exists for this account.");
        }

        String cardNumber = generateUniqueCardNumber();
        String cvv = String.format("%03d", random.nextInt(1000));
        LocalDate expiryDate = LocalDate.now().plusYears(5);

        Card card = Card.builder()
                .cardNumber(cardNumber)
                .account(account)
                .cardType(request.cardType())
                .cardStatus(CardStatus.ACTIVE)
                .cardHolderName(currentUser.getFullName())
                .expiryDate(expiryDate)
                .cvv(cvv)
                .dailyLimit(request.dailyLimit())
                .build();

        Card savedCard = cardRepository.save(card);
        log.info("Successfully issued card with number ending in [{}]", cardNumber.substring(12));

        auditLogService.log(
                AuditAction.CARD_ISSUED,
                request.cardType() +
                        " card issued for account " +
                        account.getAccountNumber()
        );
        return mapToResponse(savedCard);
    }

    @Transactional(readOnly = true)
    public List<CardResponse> getMyCards() {
        User currentUser = getAuthenticatedUser();
        log.info("Fetching all cards for user [{}]", currentUser.getEmail());

        List<Card> cards = cardRepository.findByAccountUserId(currentUser.getId());
        return cards.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public CardResponse toggleCardStatus(Long cardId) {
        User currentUser = getAuthenticatedUser();
        log.info("Toggling card status for card ID [{}] by user [{}]", cardId, currentUser.getEmail());

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found with ID: " + cardId));

        if (!card.getAccount().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to modify this card");
        }

        if (card.getCardStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Blocked cards cannot be modified");
        }

        if (card.getCardStatus() == CardStatus.ACTIVE) {
            card.setCardStatus(CardStatus.FROZEN);
            log.info("Card [{}] has been FROZEN", cardId);
            auditLogService.log(
                    AuditAction.CARD_FROZEN,
                    "Card " + maskCardNumber(card.getCardNumber()) + " frozen"
            );
        } else {
            card.setCardStatus(CardStatus.ACTIVE);
            log.info("Card [{}] has been UNFROZEN/ACTIVATED", cardId);
            auditLogService.log(
                    AuditAction.CARD_ACTIVATED,
                    "Card " + maskCardNumber(card.getCardNumber()) + " activated"
            );
        }

        Card updatedCard = cardRepository.save(card);
        return mapToResponse(updatedCard);
    }

    @Transactional
    public CardResponse updateDailyLimit(Long cardId, BigDecimal newLimit) {
        User currentUser = getAuthenticatedUser();
        log.info("Updating daily limit for card ID [{}]", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found with ID: " + cardId));

        if (!card.getAccount().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to modify this card");
        }
        if (card.getCardStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Daily limit can only be updated for active cards."
            );
        }

        card.setDailyLimit(newLimit);
        Card updatedCard = cardRepository.save(card);
        log.info("Daily limit updated to [{}] for card [{}]", newLimit, cardId);
        auditLogService.log(
                AuditAction.CARD_LIMIT_UPDATED,
                "Daily limit updated to ₹" + newLimit
        );
        return mapToResponse(updatedCard);
    }

    @Transactional(readOnly = true)
    public List<AdminCardResponse> getAllCardsForAdmin() {
        log.info("ADMIN action: Fetching all cards");
        return cardRepository.findAll()
                .stream()
                .map(card -> new AdminCardResponse(

                        card.getId(),
                        card.getAccount().getUser().getFullName(),
                        card.getAccount().getAccountNumber(),
                        maskCardNumber(card.getCardNumber()),
                        card.getCardType(),
                        card.getCardStatus(),
                        card.getDailyLimit(),
                        card.getExpiryDate()

                ))
                .toList();
    }

    @Transactional
    public CardResponse blockCardByAdmin(Long cardId) {
        log.info("ADMIN action: Blocking card [{}]", cardId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Card not found with ID: " + cardId));

        if (card.getCardStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Card is already blocked.");
        }
        card.setCardStatus(CardStatus.BLOCKED);
        Card updatedCard = cardRepository.save(card);
        auditLogService.log(
                AuditAction.CARD_BLOCKED,
                "Card " + maskCardNumber(updatedCard.getCardNumber()) + " blocked"
        );
        return mapToResponse(updatedCard);
    }

    @Transactional
    public CardResponse unblockCardByAdmin(Long cardId) {
        log.info("ADMIN action: Unblocking card [{}]", cardId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Card not found with ID: " + cardId));
        if (card.getCardStatus() != CardStatus.BLOCKED) {
            throw new IllegalStateException("Only blocked cards can be unblocked.");
        }
        card.setCardStatus(CardStatus.ACTIVE);
        Card updatedCard = cardRepository.save(card);
        auditLogService.log(
                AuditAction.CARD_UNBLOCKED,
                "Card " + maskCardNumber(updatedCard.getCardNumber()) + " unblocked"
        );
        return mapToResponse(updatedCard);
    }

    private String generateUniqueCardNumber() {
        String cardNumber;
        do {
            // Generates a mock 16-digit card starting with standard prefix 4 (Visa-style)
            StringBuilder sb = new StringBuilder("4");
            for (int i = 0; i < 15; i++) {
                sb.append(random.nextInt(10));
            }
            cardNumber = sb.toString();
        } while (cardRepository.findByCardNumber(cardNumber).isPresent());
        return cardNumber;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) {
            return "****";
        }
        return cardNumber.substring(0, 4) + "********" + cardNumber.substring(12);
    }

    private CardResponse mapToResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getAccount().getAccountNumber(),
                maskCardNumber(card.getCardNumber()),
                card.getCardType(),
                card.getCardStatus(),
                card.getCardHolderName(),
                card.getExpiryDate(),
                card.getCvv(),
                card.getDailyLimit()
        );
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }
}
