package com.bankflow.service;

import com.bankflow.dto.CreateFdRequest;
import com.bankflow.dto.FdCalculatorRequest;
import com.bankflow.dto.FdCalculatorResponse;
import com.bankflow.dto.FdResponse;
import com.bankflow.entity.*;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.FixedDepositRepository;
import com.bankflow.repository.TransactionRepository;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FixedDepositService {

    private final FixedDepositRepository fdRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;

    private static final BigDecimal RATE_1_YEAR = new BigDecimal("6.50");
    private static final BigDecimal RATE_3_YEARS = new BigDecimal("7.00");
    private static final BigDecimal RATE_5_YEARS = new BigDecimal("7.50");
    private static final BigDecimal MIN_FD_AMOUNT = new BigDecimal("10000.00");

    private final SecureRandom secureRandom = new SecureRandom();

    public FdCalculatorResponse calculateMaturity(FdCalculatorRequest request) {
        log.debug("FD maturity calculation requested for Amount: [Rs. {}], Tenure: [{}] years", request.depositAmount(), request.tenureYears());

        validateTenure(request.tenureYears());

        BigDecimal rate = getInterestRateForTenure(request.tenureYears());
        BigDecimal maturityAmount = calculateCompoundInterest(request.depositAmount(), rate, request.tenureYears());
        BigDecimal interestEarned = maturityAmount.subtract(request.depositAmount());

        log.debug("FD calculation complete. Calculated Maturity: [Rs. {}], Interest Earned: [Rs. {}]", maturityAmount, interestEarned);

        return new FdCalculatorResponse(request.depositAmount(), rate, request.tenureYears(), interestEarned, maturityAmount);
    }

    @Transactional
    public FdResponse createFixedDeposit(CreateFdRequest request) {
        User currentUser = getAuthenticatedUser();
        log.info("Initiating FD booking for user [{}] from account [{}] with deposit [Rs. {}] for [{}] years", currentUser.getEmail(), request.sourceAccountNumber(), request.depositAmount(), request.tenureYears());

        // Validate minimum deposit constraint
        if (request.depositAmount().compareTo(MIN_FD_AMOUNT) < 0) {
            log.warn("FD creation failed: Requested amount [Rs. {}] is not greater than threshold [Rs. 10000.00]", request.depositAmount());
            throw new IllegalArgumentException("Deposit amount must be minimum Rs. 10,000");
        }
        validateTenure(request.tenureYears());

        Account sourceAccount = accountRepository.findByAccountNumber(request.sourceAccountNumber()).orElseThrow(() -> {
            log.error("FD creation failed: Source account [{}] not found", request.sourceAccountNumber());
            return new IllegalArgumentException("Source account not found");
        });

        if (!sourceAccount.getUser().getId().equals(currentUser.getId())) {
            log.warn("Security Alert: User [{}] attempted FD creation from unowned account [{}]", currentUser.getEmail(), request.sourceAccountNumber());
            throw new AccessDeniedException("You can only open an FD using your own account");
        }

        if (sourceAccount.getCurrentBalance().compareTo(request.depositAmount()) < 0) {
            log.warn("FD creation failed: Insufficient balance in source account [{}]. Current: {}, Requested: {}", sourceAccount.getAccountNumber(), sourceAccount.getCurrentBalance(), request.depositAmount());
            throw new IllegalArgumentException("Insufficient balance in source account");
        }

        // Deduct balance from source account
        BigDecimal newBalance = sourceAccount.getCurrentBalance().subtract(request.depositAmount());
        sourceAccount.setCurrentBalance(newBalance);
        accountRepository.save(sourceAccount);

        String txId = "FD-DEBIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Record DEBIT transaction for FD creation
        Transaction debitTx = Transaction.builder().transactionId(txId).account(sourceAccount).transactionType(Transaction.TransactionType.DEBIT).amount(request.depositAmount()).availableBalance(newBalance).description("Fixed Deposit Booking - Tenure: " + request.tenureYears() + " Yrs").build();

        transactionRepository.save(debitTx);
        log.info("Source account [{}] debited [Rs. {}] for FD creation. Ref: [{}], New Balance: [Rs. {}]", sourceAccount.getAccountNumber(), request.depositAmount(), txId, newBalance);

        // Save Fixed Deposit details
        BigDecimal interestRate = getInterestRateForTenure(request.tenureYears());
        BigDecimal maturityAmount = calculateCompoundInterest(request.depositAmount(), interestRate, request.tenureYears());
        LocalDate depositDate = LocalDate.now();
        String fdNumber = generateUniqueFdNumber();

        FixedDeposit fd = FixedDeposit.builder().fdNumber(fdNumber).user(currentUser).sourceAccount(sourceAccount).depositAmount(request.depositAmount()).interestRate(interestRate).tenureYears(request.tenureYears()).depositDate(depositDate).maturityDate(depositDate.plusYears(request.tenureYears())).maturityAmount(maturityAmount).status(FixedDeposit.FdStatus.ACTIVE).build();

        FixedDeposit savedFd = fdRepository.save(fd);
        log.info("Fixed Deposit successfully booked. FD Number: [{}], User: [{}], Maturity Amount: [Rs. {}]", fdNumber, currentUser.getEmail(), maturityAmount);
        auditLogService.log(AuditAction.FD_CREATED, "Created Fixed Deposit " + savedFd.getFdNumber() + " for ₹" + savedFd.getDepositAmount() + " (" + savedFd.getTenureYears() + " " + (savedFd.getTenureYears() == 1 ? "Year" : "Years") + ")");
        return mapToResponse(savedFd);
    }

    @Transactional(readOnly = true)
    public List<FdResponse> getMyFixedDeposits() {
        User currentUser = getAuthenticatedUser();
        log.debug("Fetching all Fixed Deposits for user [{}]", currentUser.getEmail());

        List<FdResponse> fds = fdRepository.findByUserId(currentUser.getId()).stream().map(this::mapToResponse).toList();

        log.debug("Retrieved [{}] Fixed Deposits for user [{}]", fds.size(), currentUser.getEmail());
        return fds;
    }

    @Transactional(readOnly = true)
    public FdResponse getFdByNumber(String fdNumber) {
        User currentUser = getAuthenticatedUser();
        log.debug("Fetching FD details for number [{}] requested by user [{}]", fdNumber, currentUser.getEmail());

        FixedDeposit fd = fdRepository.findByFdNumber(fdNumber).orElseThrow(() -> {
            log.error("FD lookup failed: Fixed Deposit [{}] not found", fdNumber);
            return new IllegalArgumentException("Fixed Deposit not found");
        });

        if (!currentUser.getRole().equals(User.Role.ADMIN) && !fd.getUser().getId().equals(currentUser.getId())) {
            log.warn("Security Alert: User [{}] attempted unauthorized view of Fixed Deposit [{}]", currentUser.getEmail(), fdNumber);
            throw new AccessDeniedException("You are not authorized to view this Fixed Deposit");
        }

        return mapToResponse(fd);
    }

    @Transactional
    public FdResponse closeFixedDeposit(String fdNumber) {

        User currentUser = getAuthenticatedUser();

        FixedDeposit fd = fdRepository.findByFdNumber(fdNumber).orElseThrow(() -> new IllegalArgumentException("Fixed Deposit not found"));

        validateFdClosure(fd);

        validateFdOwnership(fd, currentUser);

        creditClosureAmount(fd);

        fd.setStatus(
                LocalDate.now().isBefore(fd.getMaturityDate())
                        ? FixedDeposit.FdStatus.PREMATURELY_CLOSED
                        : FixedDeposit.FdStatus.MATURED_CLOSED
        );

        fd.setClosedDate(LocalDate.now());
        log.info("Closing Fixed Deposit [{}] requested by [{}]", fdNumber, currentUser.getEmail());
        FixedDeposit closedFd = fdRepository.save(fd);
        log.info("FD [{}] marked CLOSED", fd.getFdNumber());

        return mapToResponse(closedFd);
    }

    private void validateFdOwnership(FixedDeposit fd, User currentUser) {
        if (!fd.getUser().getId().equals(currentUser.getId())) {
            log.warn("Security Alert: User [{}] attempted to close FD [{}] belonging to another user", currentUser.getEmail(), fd.getFdNumber());
            throw new AccessDeniedException("You are not authorized to close this Fixed Deposit");
        }
    }

    private void validateFdClosure(FixedDeposit fd) {
        if (fd.getStatus() != FixedDeposit.FdStatus.ACTIVE) {
            log.warn("FD [{}] cannot be closed because current status is [{}]", fd.getFdNumber(), fd.getStatus());
            throw new IllegalArgumentException("Only active Fixed Deposits can be closed.");
        }
    }

    private void creditClosureAmount(FixedDeposit fd) {
        Account account = fd.getSourceAccount();
        LocalDate today = LocalDate.now();

        boolean premature = today.isBefore(fd.getMaturityDate());
        BigDecimal amountToCredit = premature ? fd.getDepositAmount() : fd.getMaturityAmount();
        BigDecimal updatedBalance = account.getCurrentBalance().add(amountToCredit);
        account.setCurrentBalance(updatedBalance);
        accountRepository.save(account);

        String txId = "FD-CREDIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String description =
                premature
                        ? "Premature FD Closure - "
                        : "Fixed Deposit Closure - ";

        description += fd.getFdNumber();

        Transaction creditTx = Transaction.builder()
                .transactionId(txId)
                .account(account)
                .transactionType(Transaction.TransactionType.CREDIT)
                .amount(amountToCredit)
                .availableBalance(updatedBalance)
                .description(description)
                .build();

        transactionRepository.save(creditTx);

        log.info("FD [{}] closed. Credited Rs. {} to Account [{}]. Ref [{}]", fd.getFdNumber(), amountToCredit, account.getAccountNumber(), txId);

        auditLogService.log(
                AuditAction.FD_CLOSED,
                (premature ? "Prematurely closed " : "Closed ")
                        + "Fixed Deposit "
                        + fd.getFdNumber()
                        + " for ₹"
                        + amountToCredit
        );
    }

    private void validateTenure(Integer years) {
        if (years == null || (years != 1 && years != 3 && years != 5)) {
            log.warn("Validation failed: Invalid FD tenure [{}] requested", years);
            throw new IllegalArgumentException("Invalid tenure! Allowed tenure options are 1, 3, or 5 years.");
        }
    }

    private BigDecimal getInterestRateForTenure(int years) {
        return switch (years) {
            case 1 -> RATE_1_YEAR;
            case 3 -> RATE_3_YEARS;
            case 5 -> RATE_5_YEARS;
            default -> {
                log.error("Unexpected tenure value reached rate mapping: [{}]", years);
                throw new IllegalArgumentException("Invalid tenure period");
            }
        };
    }

    private BigDecimal calculateCompoundInterest(BigDecimal principal, BigDecimal annualRate, int years) {
        double p = principal.doubleValue();
        double r = annualRate.doubleValue() / 100.0;
        int n = 4;
        double amount = p * Math.pow(1 + (r / n), n * years);

        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName()).orElseThrow(() -> {
            log.error("Authentication Context Error: User [{}] not found in database", auth.getName());
            return new IllegalArgumentException("Authenticated user not found");
        });
    }

    private String generateUniqueFdNumber() {
        String fdNum;
        do {
            long number = 1000000000L + (long) (secureRandom.nextDouble() * 9000000000L);
            fdNum = "FD" + number;
        } while (fdRepository.existsByFdNumber(fdNum));

        log.debug("Generated unique FD number: [{}]", fdNum);
        return fdNum;
    }

    private FdResponse mapToResponse(FixedDeposit fd) {

        BigDecimal creditedAmount = switch (fd.getStatus()) {
            case PREMATURELY_CLOSED -> fd.getDepositAmount();
            case MATURED_CLOSED -> fd.getMaturityAmount();
            default -> null;      // ACTIVE
        };

        return new FdResponse(
                fd.getId(),
                fd.getFdNumber(),
                fd.getUser().getFullName(),
                fd.getSourceAccount().getAccountNumber(),
                fd.getDepositAmount(),
                fd.getInterestRate(),
                fd.getTenureYears(),
                fd.getDepositDate(),
                fd.getMaturityDate(),
                fd.getClosedDate(),
                fd.getMaturityAmount(),
                creditedAmount,
                fd.getStatus().name()
        );
    }
}
