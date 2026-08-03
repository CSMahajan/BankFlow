package com.bankflow.repository;

import com.bankflow.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByAccountId(Long accountId);

    List<Card> findByAccountUserId(Long userId);

    boolean existsByAccountIdAndCardType(Long accountId, Card.CardType cardType);

    long countByAccountUserId(Long userId);
}
