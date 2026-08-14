package com.bankflow.controller;

import com.bankflow.dto.*;
import com.bankflow.entity.Account;
import com.bankflow.entity.Card;
import com.bankflow.entity.KycDocument;
import com.bankflow.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AccountService accountService;
    private final CardService cardService;
    private final LoanService loanService;
    private final KycService kycService;

    @PostMapping("/users/create-admin")
    @PreAuthorize("hasRole('ADMIN')") // Blocks non-admins (HTTP 403 Forbidden)
    @ResponseStatus(HttpStatus.CREATED)
    public void createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        userService.createAdminAccount(request);
    }

    @GetMapping("/users/{userId}/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserAccountResponse>> getUserAccounts(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userService.getUserAccounts(userId)
        );
    }

    @GetMapping("/users/{userId}/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserCardResponse>> getUserCards(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userService.getUserCards(userId)
        );
    }

    @GetMapping("/users/{userId}/loans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserLoanResponse>> getUserLoans(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userService.getUserLoans(userId)
        );
    }

    @GetMapping("/users/{userId}/fixed-deposits")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserFixedDepositResponse>> getUserFixedDeposits(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userService.getUserFixedDeposits(userId)
        );
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Account.AccountStatus status) {

        return ResponseEntity.ok(
                accountService.getAllAccountsForAdmin(
                        page,
                        size,
                        search,
                        status
                )
        );
    }

    @PatchMapping("/accounts/{accountNumber}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> freezeAccount(
            @PathVariable String accountNumber) {

        return ResponseEntity.ok(
                accountService.freezeAccountByAdmin(accountNumber)
        );
    }

    @PatchMapping("/accounts/{accountNumber}/unfreeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> unfreezeAccount(
            @PathVariable String accountNumber) {

        return ResponseEntity.ok(
                accountService.unfreezeAccountByAdmin(accountNumber)
        );
    }

    @GetMapping("/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminCardResponse>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Card.CardStatus status
    ) {

        return ResponseEntity.ok(
                cardService.getAllCardsForAdmin(
                        page,
                        size,
                        search,
                        status
                )
        );
    }

    @PatchMapping("/cards/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> blockCard(
            @PathVariable Long cardId) {

        return ResponseEntity.ok(
                cardService.blockCardByAdmin(cardId)
        );
    }

    @PatchMapping("/cards/{cardId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> unblockCard(
            @PathVariable Long cardId) {

        return ResponseEntity.ok(
                cardService.unblockCardByAdmin(cardId)
        );
    }

    @GetMapping("/accounts/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountSummaryResponse> getAccountSummary() {

        return ResponseEntity.ok(
                accountService.getAccountSummaryForAdmin()
        );
    }

    @GetMapping("/loans/summary")
    public LoanSummaryResponse getLoanSummary(){

        return loanService.getLoanSummary();

    }

    @GetMapping("/cards/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardSummaryResponse> getCardSummary(){

        return ResponseEntity.ok(
                cardService.getCardSummaryForAdmin()
        );
    }

    @GetMapping("/kyc/documents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminKycDocumentResponse>> getAllKycDocuments(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size,

            @RequestParam(required = false) String search,

            @RequestParam(required = false)
            KycDocument.KycVerificationStatus status

    ) {

        return ResponseEntity.ok(
                kycService.getAllDocuments(
                        page,
                        size,
                        search,
                        status
                )
        );
    }

    @GetMapping("/kyc/documents/{documentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> viewKycDocument(
            @PathVariable Long documentId) {

        KycDocument document =
                kycService.getAdminDocumentDetails(documentId);

        Resource resource =
                kycService.getAdminDocumentResource(documentId);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                document.getContentType()
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                document.getOriginalFileName()
                                + "\""
                )
                .body(resource);
    }

    @PatchMapping("/kyc/documents/{documentId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyKycDocument(
            @PathVariable Long documentId) {

        kycService.verifyDocument(documentId);
    }

    @PatchMapping("/kyc/documents/{documentId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectKycDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody KycRejectRequest request) {

        kycService.rejectDocument(
                documentId,
                request.reason()
        );
    }

    @GetMapping("/kyc/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KycSummaryResponse> getKycSummary(){

        return ResponseEntity.ok(kycService.getKycSummary());
    }
}
