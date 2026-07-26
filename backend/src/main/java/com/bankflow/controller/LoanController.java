package com.bankflow.controller;

import com.bankflow.dto.ApplyLoanRequest;
import com.bankflow.dto.LoanResponse;
import com.bankflow.dto.PayEmiRequest;
import com.bankflow.dto.RepaymentResponse;
import com.bankflow.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    // ADMIN Endpoint: Approve & Disburse Loan
    @PutMapping("/{loanId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> approveAndDisburseLoan(@PathVariable Long loanId) {
        LoanResponse response = loanService.approveAndDisburseLoan(loanId);
        return ResponseEntity.ok(response);
    }
}
