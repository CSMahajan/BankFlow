package com.bankflow.controller;

import com.bankflow.dto.FundTransferRequest;
import com.bankflow.dto.FundTransferResponse;
import com.bankflow.service.FundTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class FundTransferController {

    private final FundTransferService fundTransferService;

    @PostMapping
    public ResponseEntity<FundTransferResponse> transferFunds(@Valid @RequestBody FundTransferRequest request) {
        return ResponseEntity.ok(fundTransferService.transferFunds(request));
    }
}
