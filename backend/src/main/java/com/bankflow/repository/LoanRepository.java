package com.bankflow.repository;

import com.bankflow.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserId(Long userId);

    Optional<Loan> findByLoanNumber(String loanNumber);

    boolean existsByLoanNumber(String loanNumber);

    List<Loan> findByStatus(Loan.LoanStatus status);
}
