package com.bankflow.controller;

import com.bankflow.dto.CardResponse;
import com.bankflow.dto.IssueCardRequest;
import com.bankflow.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping("/issue")
    public ResponseEntity<CardResponse> issueCard(@Valid @RequestBody IssueCardRequest request) {
        CardResponse response = cardService.issueCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-cards")
    public ResponseEntity<List<CardResponse>> getMyCards() {
        List<CardResponse> response = cardService.getMyCards();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{cardId}/toggle-status")
    public ResponseEntity<CardResponse> toggleCardStatus(@PathVariable Long cardId) {
        CardResponse response = cardService.toggleCardStatus(cardId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{cardId}/limit")
    public ResponseEntity<CardResponse> updateDailyLimit(
            @PathVariable Long cardId,
            @RequestParam BigDecimal newLimit) {
        CardResponse response = cardService.updateDailyLimit(cardId, newLimit);
        return ResponseEntity.ok(response);
    }
}
