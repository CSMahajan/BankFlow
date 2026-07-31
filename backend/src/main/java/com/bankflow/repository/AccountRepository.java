package com.bankflow.repository;

import com.bankflow.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserId(Long userId);

    long countByUserId(Long userId);

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    @Query("""
    SELECT COALESCE(SUM(a.currentBalance), 0)
    FROM Account a
""")
    BigDecimal getTotalDeposits();
}
