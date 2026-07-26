package com.bankflow.repository;

import com.bankflow.entity.Transaction;
import com.bankflow.entity.Transaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Fetch last 10 transactions for a specific account
    List<Transaction> findTop10ByAccountIdOrderByTransactionDateDesc(Long accountId);

    // Filter transactions by date range
    List<Transaction> findByAccountIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long accountId, LocalDateTime startDate, LocalDateTime endDate);

    // Sum total credits for an account
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account.id = :accountId AND t.transactionType = :type")
    BigDecimal sumAmountByAccountIdAndTransactionType(@Param("accountId") Long accountId, @Param("type") TransactionType type);

    // Global transaction search for admins by account number
    List<Transaction> findByAccountAccountNumberOrderByTransactionDateDesc(String accountNumber);
}
