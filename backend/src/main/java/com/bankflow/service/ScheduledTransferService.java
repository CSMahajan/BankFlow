package com.bankflow.service;

import com.bankflow.dto.CreateScheduledTransferRequest;
import com.bankflow.dto.FundTransferRequest;
import com.bankflow.dto.ScheduledTransferResponse;
import com.bankflow.entity.Account;
import com.bankflow.entity.AuditAction;
import com.bankflow.entity.ScheduledTransfer;
import com.bankflow.entity.ScheduledTransfer.Frequency;
import com.bankflow.entity.ScheduledTransfer.TransferStatus;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.ScheduledTransferRepository;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTransferService {

    private final ScheduledTransferRepository scheduledTransferRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final FundTransferService fundTransferService;
    private final AuditLogService auditLogService;

    @Transactional
    public ScheduledTransferResponse createScheduledTransfer(CreateScheduledTransferRequest request) {
        User currentUser = getAuthenticatedUser();
        log.info("Creating scheduled transfer for user [{}] from account [{}]", currentUser.getEmail(), request.sourceAccountNumber());

        Account sourceAccount = accountRepository.findByAccountNumber(request.sourceAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Source account not found: " + request.sourceAccountNumber()));

        if (!sourceAccount.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to schedule transfers from this account");
        }

        if (sourceAccount.getAccountStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Scheduled transfers can only be created from active accounts."
            );
        }

        accountRepository.findByAccountNumber(request.recipientAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Recipient account not found: " + request.recipientAccountNumber()));

        if (request.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Scheduled transfer start date cannot be in the past.");
        }

        ScheduledTransfer scheduledTransfer = ScheduledTransfer.builder()
                .user(currentUser)
                .sourceAccountNumber(request.sourceAccountNumber())
                .recipientAccountNumber(request.recipientAccountNumber())
                .amount(request.amount())
                .description(request.description())
                .frequency(request.frequency())
                .status(TransferStatus.ACTIVE)
                .nextExecutionDate(request.startDate())
                .build();

        ScheduledTransfer saved = scheduledTransferRepository.save(scheduledTransfer);
        log.info("Successfully scheduled transfer ID [{}] due on [{}]", saved.getId(), saved.getNextExecutionDate());
        auditLogService.log(AuditAction.SCHEDULED_TRANSFER_CREATED,
                "Created scheduled transfer of ₹"
                        + saved.getAmount() + " to " + saved.getRecipientAccountNumber() + " (" + saved.getFrequency() + ")"
        );
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ScheduledTransferResponse> getMyScheduledTransfers() {
        User currentUser = getAuthenticatedUser();
        return scheduledTransferRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ScheduledTransferResponse cancelScheduledTransfer(Long transferId) {
        User currentUser = getAuthenticatedUser();
        ScheduledTransfer transfer = scheduledTransferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Scheduled transfer not found with ID: " + transferId));

        if (!transfer.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to cancel this scheduled transfer");
        }

        if (transfer.getStatus() == TransferStatus.CANCELLED) {
            throw new IllegalStateException("Transfer is already cancelled");
        }

        transfer.setStatus(TransferStatus.CANCELLED);
        ScheduledTransfer updated = scheduledTransferRepository.save(transfer);
        log.info("Cancelled scheduled transfer ID [{}]", transferId);
        auditLogService.log(AuditAction.SCHEDULED_TRANSFER_CANCELLED,
                "Cancelled scheduled transfer to " + updated.getRecipientAccountNumber()
        );
        return mapToResponse(updated);
    }

    /**
     * Automated cron job running every day at 1:00 AM to process due transfers.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void processDueTransfers() {
        LocalDate today = LocalDate.now();
        log.info("Starting batch execution of scheduled transfers for date: [{}]", today);

        List<ScheduledTransfer> dueTransfers = scheduledTransferRepository
                .findByStatusAndNextExecutionDateLessThanEqual(TransferStatus.ACTIVE, today);

        log.info("Found [{}] due transfers to process", dueTransfers.size());

        for (ScheduledTransfer transfer : dueTransfers) {
            try {
                // Use existing FundTransferRequest & TransferService
                FundTransferRequest transferRequest = new FundTransferRequest(
                        transfer.getSourceAccountNumber(),
                        transfer.getRecipientAccountNumber(),
                        transfer.getAmount(),
                        "[AUTOMATED] " + (transfer.getDescription() != null ? transfer.getDescription() : "Recurring Transfer")
                );

                fundTransferService.executeScheduledTransfer(
                        transfer.getUser(),
                        transferRequest
                );
                log.info("Successfully executed scheduled transfer ID [{}]", transfer.getId());
                auditLogService.log(AuditAction.SCHEDULED_TRANSFER_EXECUTED,
                        "Executed " + transfer.getFrequency() + " scheduled transfer of ₹" + transfer.getAmount() + " to " + transfer.getRecipientAccountNumber());
                // Advance next execution date based on frequency
                LocalDate nextDate = calculateNextExecutionDate(transfer.getNextExecutionDate(), transfer.getFrequency());
                transfer.setNextExecutionDate(nextDate);
                scheduledTransferRepository.save(transfer);

            } catch (Exception e) {
                log.error("Failed to execute scheduled transfer ID [{}]", transfer.getId(), e);
            }
        }
    }

    private LocalDate calculateNextExecutionDate(LocalDate currentDate, Frequency frequency) {
        return switch (frequency) {
            case DAILY -> currentDate.plusDays(1);
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> currentDate.plusMonths(1);
        };
    }

    private ScheduledTransferResponse mapToResponse(ScheduledTransfer transfer) {
        return new ScheduledTransferResponse(
                transfer.getId(),
                transfer.getSourceAccountNumber(),
                transfer.getRecipientAccountNumber(),
                transfer.getAmount(),
                transfer.getDescription(),
                transfer.getFrequency(),
                transfer.getStatus(),
                transfer.getNextExecutionDate(),
                transfer.getCreatedAt()
        );
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }
}
