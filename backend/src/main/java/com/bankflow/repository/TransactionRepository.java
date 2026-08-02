package com.bankflow.repository;

import com.bankflow.entity.Transaction;
import com.bankflow.entity.Transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // --- By Account Number ---

    // Fetch transactions using account number string directly
    List<Transaction> findByAccountAccountNumberOrderByTransactionDateDesc(String accountNumber);

    // --- Single-Account ID Queries ---

    List<Transaction> findTop10ByAccountIdOrderByTransactionDateDesc(Long accountId);

    List<Transaction> findByAccountIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long accountId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.account.id = :accountId
          AND t.transactionType = :type
    """)
    BigDecimal sumAmountByAccountIdAndTransactionType(
            @Param("accountId") Long accountId,
            @Param("type") TransactionType type
    );

    // --- Multi-Account Queries (Aggregated Dashboard) ---

    Page<Transaction> findByAccountIdInOrderByTransactionDateDesc(List<Long> accountIds, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.account.id IN :accountIds
          AND t.transactionType = :type
          AND t.transactionDate >= :startDate
          AND t.transactionDate <= :endDate
    """)
    BigDecimal sumAmountByAccountIdsAndTypeAndDateRange(
            @Param("accountIds") List<Long> accountIds,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    Page<Transaction> findByAccountUserIdOrderByTransactionDateDesc(
            Long userId,
            Pageable pageable
    );

    Page<Transaction> findByAccountUserIdAndTransactionTypeOrderByTransactionDateDesc(
            Long userId,
            TransactionType transactionType,
            Pageable pageable
    );

    Optional<Transaction> findByTransactionId(String transactionId);
}
