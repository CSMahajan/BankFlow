package com.bankflow.repository;

import com.bankflow.entity.ScheduledTransfer;
import com.bankflow.entity.ScheduledTransfer.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduledTransferRepository extends JpaRepository<ScheduledTransfer, Long> {

    List<ScheduledTransfer> findByUserId(Long userId);

    List<ScheduledTransfer> findByStatusAndNextExecutionDateLessThanEqual(TransferStatus status, LocalDate date);
}
