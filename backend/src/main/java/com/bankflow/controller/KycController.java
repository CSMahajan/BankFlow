package com.bankflow.controller;

import com.bankflow.dto.KycDocumentResponse;
import com.bankflow.dto.KycStatusResponse;
import com.bankflow.entity.KycDocument;
import com.bankflow.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<KycDocumentResponse> upload(
            @RequestParam MultipartFile file,
            @RequestParam KycDocument.DocumentType documentType) {

        KycDocument document = kycService.uploadDocument(file, documentType);

        return ResponseEntity.ok(
                new KycDocumentResponse(
                        document.getId(),
                        document.getDocumentType().name(),
                        document.getOriginalFileName(),
                        document.getKycVerificationStatus().name(),
                        document.getUploadedAt()
                )
        );
    }

    @GetMapping("/my-documents")
    public ResponseEntity<List<KycDocumentResponse>> getMyDocuments() {

        return ResponseEntity.ok(kycService.getMyDocuments());
    }

    @GetMapping("/status")
    public ResponseEntity<KycStatusResponse> getKycStatus() {

        return ResponseEntity.ok(kycService.getMyKycStatus());
    }

    @GetMapping("/documents/{documentId}")
    public ResponseEntity<Resource> viewDocument(@PathVariable Long documentId) {

        KycDocument document = kycService.getCustomerDocumentDetails(documentId);

        Resource resource = kycService.getCustomerDocumentResource(documentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + document.getOriginalFileName() + "\"")
                .body(resource);
    }

}