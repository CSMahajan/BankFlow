package com.bankflow.controller;

import com.bankflow.dto.*;
import com.bankflow.service.FixedDepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fd")
@RequiredArgsConstructor
public class FixedDepositController {

    private final FixedDepositService fdService;

    @PostMapping("/calculate")
    public ResponseEntity<FdCalculatorResponse> calculateMaturity(@Valid @RequestBody FdCalculatorRequest request) {
        return ResponseEntity.ok(fdService.calculateMaturity(request));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FdResponse createFixedDeposit(@Valid @RequestBody CreateFdRequest request) {
        return fdService.createFixedDeposit(request);
    }

    @GetMapping("/my-fds")
    public ResponseEntity<List<FdResponse>> getMyFixedDeposits() {
        return ResponseEntity.ok(fdService.getMyFixedDeposits());
    }

    @GetMapping("/{fdNumber}")
    public ResponseEntity<FdResponse> getFdByNumber(@PathVariable String fdNumber) {
        return ResponseEntity.ok(fdService.getFdByNumber(fdNumber));
    }
}
