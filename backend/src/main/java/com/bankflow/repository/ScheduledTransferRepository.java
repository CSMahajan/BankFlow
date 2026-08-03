package com.bankflow.repository;

import com.bankflow.entity.ScheduledTransfer;
import com.bankflow.entity.ScheduledTransfer.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduledTransferRepository extends JpaRepository<ScheduledTransfer, Long> {

    @Query("""
            SELECT s
            FROM ScheduledTransfer s
            WHERE s.user.id = :userId
            ORDER BY
            CASE
                WHEN s.status='ACTIVE' THEN 0
                WHEN s.status='PAUSED' THEN 1
                WHEN s.status='FAILED' THEN 2
                WHEN s.status='COMPLETED' THEN 3
                WHEN s.status='CANCELLED' THEN 4
            END,
            s.createdAt DESC
            """)
    List<ScheduledTransfer> findByUserIdOrderByActiveFirst(@Param("userId") Long userId);

    List<ScheduledTransfer> findByStatusAndNextExecutionDateLessThanEqual(TransferStatus status, LocalDate date);
}
