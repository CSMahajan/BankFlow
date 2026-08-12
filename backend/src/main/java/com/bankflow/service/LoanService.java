package com.bankflow.service;

import com.bankflow.dto.*;
import com.bankflow.entity.*;
import com.bankflow.entity.Loan.LoanStatus;
import com.bankflow.entity.Loan.LoanType;
import com.bankflow.entity.Transaction.TransactionType;
import com.bankflow.repository.*;
import com.bankflow.specification.LoanSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository repaymentRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;

    // Default Annual Interest Rates by Loan Type
    private static final BigDecimal PERSONAL_LOAN_RATE = new BigDecimal("12.00");
    private static final BigDecimal VEHICLE_LOAN_RATE = new BigDecimal("8.50");
    private static final BigDecimal HOME_LOAN_RATE = new BigDecimal("7.00");

    @Transactional
    public LoanResponse applyForLoan(ApplyLoanRequest request) {
        User currentUser = getAuthenticatedUser();
        log.info("User [{}] applying for [{}] loan of amount [Rs. {}]",
                currentUser.getEmail(), request.loanType(), request.principalAmount());

        Account account = accountRepository.findByAccountNumber(request.accountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Disbursement account not found"));

        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only apply for loans linked to your own account");
        }

        if (account.getAccountStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalStateException("Loans can only be applied using an active account.");
        }

        BigDecimal interestRate = getInterestRateForType(request.loanType());
        BigDecimal monthlyEmi = calculateEmi(request.principalAmount(), interestRate, request.tenureMonths());

        String tempLoanNumber = "LN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Loan loan = Loan.builder()
                .loanNumber(tempLoanNumber)
                .user(currentUser)
                .disbursementAccount(account)
                .loanType(request.loanType())
                .principalAmount(request.principalAmount())
                .annualInterestRate(interestRate)
                .tenureMonths(request.tenureMonths())
                .monthlyEmi(monthlyEmi)
                .remainingBalance(request.principalAmount())
                .status(LoanStatus.PENDING)
                .build();

        Loan savedLoan = loanRepository.save(loan);
        auditLogService.log(
                AuditAction.LOAN_APPLIED,
                "Applied for " + savedLoan.getLoanType()
                        + " loan (" + savedLoan.getLoanNumber() + ")"
        );
        log.info("Loan application submitted successfully. Loan ID: [{}], Status: [PENDING]", savedLoan.getId());
        return mapToLoanResponse(savedLoan);
    }

    @Transactional
    public LoanResponse approveAndDisburseLoan(Long loanId) {
        User admin = getAuthenticatedUser();
        log.info("ADMIN [{}] approving loan ID [{}]", admin.getEmail(), loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Only loans in PENDING status can be approved");
        }

        Account account = loan.getDisbursementAccount();

        // 1. Credit principal amount to account
        BigDecimal newBalance = account.getCurrentBalance().add(loan.getPrincipalAmount());
        account.setCurrentBalance(newBalance);
        accountRepository.save(account);

        // 2. Log disbursement transaction
        Transaction disbursementTx = Transaction.builder()
                .transactionId("TX-DISB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .account(account)
                .transactionType(TransactionType.CREDIT)
                .amount(loan.getPrincipalAmount())
                .availableBalance(newBalance)
                .description("Loan Disbursement - " + loan.getLoanNumber())
                .build();
        transactionRepository.save(disbursementTx);

        // 3. Update loan status and dates
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setStartDate(LocalDate.now());
        loan.setNextDueDate(LocalDate.now().plusMonths(1));
        Loan updatedLoan = loanRepository.save(loan);
        auditLogService.log(
                AuditAction.LOAN_APPROVED,
                "Approved loan " + loan.getLoanNumber()
        );
        log.info("Loan [{}] approved and disbursed [Rs. {}] to account [{}]",
                loan.getLoanNumber(), loan.getPrincipalAmount(), account.getAccountNumber());

        return mapToLoanResponse(updatedLoan);
    }

    @Transactional
    public LoanResponse rejectLoan(Long loanId, RejectLoanRequest request) {
        User admin = getAuthenticatedUser();
        log.info("ADMIN [{}] rejecting loan ID [{}]", admin.getEmail(), loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Only loans in PENDING status can be rejected");
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectionRemarks(request.remarks());

        Loan updatedLoan = loanRepository.save(loan);
        auditLogService.log(
                AuditAction.LOAN_REJECTED,
                "Rejected loan " + loan.getLoanNumber()
        );
        log.info("Loan [{}] rejected", loan.getLoanNumber());

        return mapToLoanResponse(updatedLoan);
    }

    @Transactional
    public RepaymentResponse payEmi(PayEmiRequest request) {
        User currentUser = getAuthenticatedUser();
        log.info("User [{}] attempting EMI payment for loan [{}]", currentUser.getEmail(), request.loanNumber());

        Loan loan = loanRepository.findByLoanNumber(request.loanNumber())
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (!loan.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Unauthorized: You do not own this loan");
        }

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new IllegalStateException("EMI payment is only allowed for ACTIVE loans");
        }

        Account sourceAccount = accountRepository.findByAccountNumber(request.sourceAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));

        if (!sourceAccount.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Unauthorized: Source account does not belong to user");
        }

        if (sourceAccount.getAccountStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "EMI payments can only be made from an active account."
            );
        }

        BigDecimal emiAmount = loan.getMonthlyEmi();

        // Check sufficient balance
        if (sourceAccount.getCurrentBalance().compareTo(emiAmount) < 0) {
            throw new IllegalArgumentException("Insufficient balance in source account for EMI payment");
        }

        // Deduct EMI from source account
        BigDecimal newBalance = sourceAccount.getCurrentBalance().subtract(emiAmount);
        sourceAccount.setCurrentBalance(newBalance);
        accountRepository.save(sourceAccount);

        // Calculate Interest vs Principal component for this installment
        // Monthly Interest = (Remaining Balance * Annual Interest Rate) / (12 * 100)
        BigDecimal monthlyRate = loan.getAnnualInterestRate().divide(new BigDecimal("1200"), 8, RoundingMode.HALF_UP);
        BigDecimal interestComponent = loan.getRemainingBalance().multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal principalComponent = emiAmount.subtract(interestComponent);

        // Prevent overpayment on final installment
        if (principalComponent.compareTo(loan.getRemainingBalance()) > 0) {
            principalComponent = loan.getRemainingBalance();
        }

        BigDecimal newRemainingBalance = loan.getRemainingBalance().subtract(principalComponent);
        loan.setRemainingBalance(newRemainingBalance);

        if (newRemainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.PAID_OFF);
            loan.setRemainingBalance(BigDecimal.ZERO);
            loan.setNextDueDate(null);
            log.info("Loan [{}] fully paid off!", loan.getLoanNumber());
        } else {
            loan.setNextDueDate(loan.getNextDueDate().plusMonths(1));
        }
        loanRepository.save(loan);

        // Record debit transaction
        String txId = "TX-EMI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction emiTx = Transaction.builder()
                .transactionId(txId)
                .account(sourceAccount)
                .transactionType(TransactionType.DEBIT)
                .amount(emiAmount)
                .availableBalance(newBalance)
                .description("EMI Payment for Loan " + loan.getLoanNumber())
                .build();
        transactionRepository.save(emiTx);

        // Save Loan Repayment Record
        LoanRepayment repayment = LoanRepayment.builder()
                .loan(loan)
                .amountPaid(emiAmount)
                .principalComponent(principalComponent)
                .interestComponent(interestComponent)
                .remainingLoanBalance(loan.getRemainingBalance())
                .transactionReference(txId)
                .build();

        LoanRepayment savedRepayment = repaymentRepository.save(repayment);

        auditLogService.log(
                AuditAction.EMI_PAID,
                "Paid EMI for loan " + loan.getLoanNumber()
        );
        return new RepaymentResponse(
                savedRepayment.getId(),
                loan.getLoanNumber(),
                savedRepayment.getAmountPaid(),
                savedRepayment.getPrincipalComponent(),
                savedRepayment.getInterestComponent(),
                savedRepayment.getRemainingLoanBalance(),
                savedRepayment.getPaymentDate(),
                savedRepayment.getTransactionReference()
        );
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getMyLoans() {
        User currentUser = getAuthenticatedUser();
        return loanRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::mapToLoanResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<LoanResponse> getPendingLoans(
            String search,
            LoanType loanType,
            Pageable pageable) {


        Specification<Loan> specification =
                Specification.where(
                        LoanSpecification.status(LoanStatus.PENDING)
                );


        specification =
                specification.and(
                        LoanSpecification.search(search)
                );


        specification =
                specification.and(
                        LoanSpecification.loanType(loanType)
                );


        return loanRepository
                .findAll(specification, pageable)
                .map(this::mapToLoanResponse);
    }

    @Transactional(readOnly = true)
    public LoanSummaryResponse getLoanSummary() {

        long totalPending =
                loanRepository.countByStatus(
                        LoanStatus.PENDING
                );

        long personal =
                loanRepository.countByStatusAndLoanType(
                        LoanStatus.PENDING,
                        LoanType.PERSONAL
                );

        long home =
                loanRepository.countByStatusAndLoanType(
                        LoanStatus.PENDING,
                        LoanType.HOME
                );

        long vehicle =
                loanRepository.countByStatusAndLoanType(
                        LoanStatus.PENDING,
                        LoanType.VEHICLE
                );


        return new LoanSummaryResponse(
                totalPending,
                personal,
                home,
                vehicle
        );
    }

    @Transactional(readOnly = true)
    public List<RepaymentResponse> getRepaymentHistory(String loanNumber) {
        User currentUser = getAuthenticatedUser();
        Loan loan = loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (!currentUser.getRole().equals(User.Role.ADMIN) && !loan.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Unauthorized to view repayment history for this loan");
        }

        return repaymentRepository.findByLoanIdOrderByPaymentDateDesc(loan.getId())
                .stream()
                .map(r -> new RepaymentResponse(
                        r.getId(),
                        loan.getLoanNumber(),
                        r.getAmountPaid(),
                        r.getPrincipalComponent(),
                        r.getInterestComponent(),
                        r.getRemainingLoanBalance(),
                        r.getPaymentDate(),
                        r.getTransactionReference()
                ))
                .toList();
    }

    // EMI Calculation: [P * r * (1+r)^n] / [(1+r)^n - 1]
    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int tenureMonths) {
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), 8, RoundingMode.HALF_UP);

        // (1 + r)^n
        BigDecimal onePlusRPowN = BigDecimal.ONE.add(monthlyRate).pow(tenureMonths, MathContext.DECIMAL128);

        // Numerator: P * r * (1+r)^n
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowN);

        // Denominator: (1+r)^n - 1
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getInterestRateForType(LoanType type) {
        return switch (type) {
            case PERSONAL -> PERSONAL_LOAN_RATE;
            case VEHICLE -> VEHICLE_LOAN_RATE;
            case HOME -> HOME_LOAN_RATE;
        };
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }

    private LoanResponse mapToLoanResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getLoanNumber(),
                loan.getDisbursementAccount().getAccountNumber(),
                loan.getUser().getFullName(),
                loan.getLoanType(),
                loan.getPrincipalAmount(),
                loan.getAnnualInterestRate(),
                loan.getTenureMonths(),
                loan.getMonthlyEmi(),
                loan.getRemainingBalance(),
                loan.getStatus(),
                loan.getRejectionRemarks(),
                loan.getCreatedAt().toLocalDate(),
                loan.getStartDate(),
                loan.getNextDueDate()
        );
    }
}
