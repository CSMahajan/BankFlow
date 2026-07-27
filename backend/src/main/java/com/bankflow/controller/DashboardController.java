package com.bankflow.controller;

import com.bankflow.dto.DashboardSummaryResponse;
import com.bankflow.dto.MonthlyAnalyticsResponse;
import com.bankflow.dto.TransactionResponse;
import com.bankflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        DashboardSummaryResponse response = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics/monthly")
    public ResponseEntity<MonthlyAnalyticsResponse> getCurrentMonthAnalytics() {
        return ResponseEntity.ok(dashboardService.getCurrentMonthAnalytics());
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponse>> getDashboardTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(dashboardService.getDashboardTransactions(page, size));
    }
}
