package com.bankflow.controller;

import com.bankflow.dto.*;
import com.bankflow.entity.Loan;
import com.bankflow.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<LoanResponse> applyForLoan(@Valid @RequestBody ApplyLoanRequest request) {
        LoanResponse response = loanService.applyForLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/pay-emi")
    public ResponseEntity<RepaymentResponse> payEmi(@Valid @RequestBody PayEmiRequest request) {
        RepaymentResponse response = loanService.payEmi(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-loans")
    public ResponseEntity<List<LoanResponse>> getMyLoans() {
        List<LoanResponse> loans = loanService.getMyLoans();
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/{loanNumber}/repayments")
    public ResponseEntity<List<RepaymentResponse>> getRepaymentHistory(@PathVariable String loanNumber) {
        List<RepaymentResponse> history = loanService.getRepaymentHistory(loanNumber);
        return ResponseEntity.ok(history);
    }

    // ADMIN Endpoint: View Pending Loan Applications
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<LoanResponse>> getPendingLoans(

            @RequestParam(required = false) String search,

            @RequestParam(required = false) Loan.LoanType loanType,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable

    ) {

        return ResponseEntity.ok(
                loanService.getPendingLoans(
                        search,
                        loanType,
                        pageable
                )
        );
    }

    // ADMIN Endpoint: Approve & Disburse Loan
    @PutMapping("/{loanId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> approveAndDisburseLoan(@PathVariable Long loanId) {
        LoanResponse response = loanService.approveAndDisburseLoan(loanId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{loanId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> rejectLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody RejectLoanRequest request) {

        LoanResponse response = loanService.rejectLoan(loanId, request);
        return ResponseEntity.ok(response);
    }
}
