package com.bankflow.service;

import com.bankflow.dto.CreateFdRequest;
import com.bankflow.dto.FdCalculatorRequest;
import com.bankflow.dto.FdCalculatorResponse;
import com.bankflow.dto.FdResponse;
import com.bankflow.entity.Account;
import com.bankflow.entity.FixedDeposit;
import com.bankflow.entity.Transaction;
import com.bankflow.entity.User;
import com.bankflow.repository.AccountRepository;
import com.bankflow.repository.FixedDepositRepository;
import com.bankflow.repository.TransactionRepository;
import com.bankflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FixedDepositService {

    private final FixedDepositRepository fdRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    private static final BigDecimal RATE_1_YEAR = new BigDecimal("6.50");
    private static final BigDecimal RATE_3_YEARS = new BigDecimal("7.00");
    private static final BigDecimal RATE_5_YEARS = new BigDecimal("7.50");

    public FdCalculatorResponse calculateMaturity(FdCalculatorRequest request) {
        validateTenure(request.tenureYears());

        BigDecimal rate = getInterestRateForTenure(request.tenureYears());
        BigDecimal maturityAmount = calculateCompoundInterest(request.depositAmount(), rate, request.tenureYears());
        BigDecimal interestEarned = maturityAmount.subtract(request.depositAmount());

        return new FdCalculatorResponse(
                request.depositAmount(),
                rate,
                request.tenureYears(),
                interestEarned,
                maturityAmount
        );
    }

    @Transactional
    public FdResponse createFixedDeposit(CreateFdRequest request) {
        User currentUser = getAuthenticatedUser();

        // Validate minimum deposit constraint
        if (request.depositAmount().compareTo(new BigDecimal("10000.00")) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be strictly greater than Rs. 10,000");
        }
        validateTenure(request.tenureYears());

        Account sourceAccount = accountRepository.findByAccountNumber(request.sourceAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));

        if (!sourceAccount.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only open an FD using your own account");
        }

        if (sourceAccount.getCurrentBalance().compareTo(request.depositAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance in source account");
        }

        // Deduct balance from source account
        BigDecimal newBalance = sourceAccount.getCurrentBalance().subtract(request.depositAmount());
        sourceAccount.setCurrentBalance(newBalance);
        accountRepository.save(sourceAccount);

        // Record DEBIT transaction for FD creation
        Transaction debitTx = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .account(sourceAccount)
                .transactionType(Transaction.TransactionType.DEBIT)
                .amount(request.depositAmount())
                .availableBalance(newBalance)
                .description("Fixed Deposit Booking - Tenure: " + request.tenureYears() + " Yrs")
                .build();

        transactionRepository.save(debitTx);

        // Save Fixed Deposit details
        BigDecimal interestRate = getInterestRateForTenure(request.tenureYears());
        BigDecimal maturityAmount = calculateCompoundInterest(request.depositAmount(), interestRate, request.tenureYears());
        LocalDate depositDate = LocalDate.now();

        FixedDeposit fd = FixedDeposit.builder()
                .fdNumber(generateUniqueFdNumber())
                .user(currentUser)
                .sourceAccount(sourceAccount)
                .depositAmount(request.depositAmount())
                .interestRate(interestRate)
                .tenureYears(request.tenureYears())
                .depositDate(depositDate)
                .maturityDate(depositDate.plusYears(request.tenureYears()))
                .maturityAmount(maturityAmount)
                .status(FixedDeposit.FdStatus.ACTIVE)
                .build();

        return mapToResponse(fdRepository.save(fd));
    }

    @Transactional(readOnly = true)
    public List<FdResponse> getMyFixedDeposits() {
        User currentUser = getAuthenticatedUser();
        return fdRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FdResponse getFdByNumber(String fdNumber) {
        User currentUser = getAuthenticatedUser();
        FixedDeposit fd = fdRepository.findByFdNumber(fdNumber)
                .orElseThrow(() -> new IllegalArgumentException("Fixed Deposit not found"));

        if (!currentUser.getRole().equals(User.Role.ADMIN) && !fd.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to view this Fixed Deposit");
        }

        return mapToResponse(fd);
    }

    private void validateTenure(Integer years) {
        if (years == null || (years != 1 && years != 3 && years != 5)) {
            throw new IllegalArgumentException("Invalid tenure! Allowed tenure options are 1, 3, or 5 years.");
        }
    }

    private BigDecimal getInterestRateForTenure(int years) {
        return switch (years) {
            case 1 -> RATE_1_YEAR;
            case 3 -> RATE_3_YEARS;
            case 5 -> RATE_5_YEARS;
            default -> throw new IllegalArgumentException("Invalid tenure period");
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
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }

    private String generateUniqueFdNumber() {
        String fdNum;
        Random random = new Random();
        do {
            long number = 1000000000L + (long) (random.nextDouble() * 9000000000L);
            fdNum = "FD" + number;
        } while (fdRepository.existsByFdNumber(fdNum));
        return fdNum;
    }

    private FdResponse mapToResponse(FixedDeposit fd) {
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
                fd.getMaturityAmount(),
                fd.getStatus().name()
        );
    }
}
