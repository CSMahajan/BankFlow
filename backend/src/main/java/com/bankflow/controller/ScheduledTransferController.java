package com.bankflow.controller;

import com.bankflow.dto.CreateScheduledTransferRequest;
import com.bankflow.dto.ScheduledTransferResponse;
import com.bankflow.service.ScheduledTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scheduled-transfers")
@RequiredArgsConstructor
public class ScheduledTransferController {

    private final ScheduledTransferService scheduledTransferService;

    @PostMapping
    public ResponseEntity<ScheduledTransferResponse> createScheduledTransfer(
            @Valid @RequestBody CreateScheduledTransferRequest request) {
        ScheduledTransferResponse response = scheduledTransferService.createScheduledTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-transfers")
    public ResponseEntity<List<ScheduledTransferResponse>> getMyScheduledTransfers() {
        List<ScheduledTransferResponse> response = scheduledTransferService.getMyScheduledTransfers();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{transferId}/cancel")
    public ResponseEntity<ScheduledTransferResponse> cancelScheduledTransfer(@PathVariable Long transferId) {
        ScheduledTransferResponse response = scheduledTransferService.cancelScheduledTransfer(transferId);
        return ResponseEntity.ok(response);
    }
}
