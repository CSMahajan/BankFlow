package com.bankflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fixed_deposits", schema = "retail_banking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fd_number", nullable = false, unique = true, length = 20)
    private String fdNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @Column(name = "deposit_amount", nullable = false)
    private BigDecimal depositAmount;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate; // Annual percentage (e.g., 6.50 for 6.5%)

    @Column(name = "tenure_years", nullable = false)
    private Integer tenureYears; // 1, 3, or 5

    @Column(name = "deposit_date", nullable = false)
    private LocalDate depositDate;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @Column(name = "maturity_amount", nullable = false)
    private BigDecimal maturityAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FdStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = FdStatus.ACTIVE;
        }
    }

    public enum FdStatus {
        ACTIVE,
        CLOSED,
        MATURED
    }
}
