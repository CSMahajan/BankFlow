package com.bankflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_transfers", schema = "retail_banking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "source_account_number", nullable = false, length = 20)
    private String sourceAccountNumber;

    @Column(name = "recipient_account_number", nullable = false, length = 20)
    private String recipientAccountNumber;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private Frequency frequency; // DAILY, WEEKLY, MONTHLY

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransferStatus status; // ACTIVE, COMPLETED, CANCELLED

    @Column(name = "next_execution_date", nullable = false)
    private LocalDate nextExecutionDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.nextExecutionDate == null) {
            this.nextExecutionDate = LocalDate.now();
        }
    }

    public enum Frequency {
        DAILY,
        WEEKLY,
        MONTHLY
    }

    public enum TransferStatus {
        ACTIVE,
        COMPLETED,
        CANCELLED
    }
}
