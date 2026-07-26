package com.bankflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_repayments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalComponent;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interestComponent;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingLoanBalance;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private String transactionReference;

    @PrePersist
    protected void onCreate() {
        this.paymentDate = LocalDateTime.now();
    }
}
