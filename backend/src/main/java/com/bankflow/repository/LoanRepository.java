package com.bankflow.repository;

import com.bankflow.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends
        JpaRepository<Loan, Long>,
        JpaSpecificationExecutor<Loan> {

    List<Loan> findByUserId(Long userId);

    Optional<Loan> findByLoanNumber(String loanNumber);

    boolean existsByLoanNumber(String loanNumber);

    List<Loan> findByStatus(Loan.LoanStatus status);

    long countByStatus(Loan.LoanStatus status);

    long countByUserId(Long userId);

    @Query("""
            SELECT COALESCE(SUM(l.remainingBalance), 0)
            FROM Loan l
            WHERE l.user.id = :userId
              AND l.status = com.bankflow.entity.Loan.LoanStatus.ACTIVE
            """)
    BigDecimal getOutstandingLoanAmount(Long userId);


        @EntityGraph(attributePaths = {
                "disbursementAccount",
                "user"
        })
        Page<Loan> findAll(
                Specification<Loan> specification,
                Pageable pageable
        );

    long countByStatusAndLoanType(
            Loan.LoanStatus status,
            Loan.LoanType loanType
    );
}
