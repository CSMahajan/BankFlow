package com.bankflow.service;

import com.bankflow.dto.*;
import com.bankflow.entity.AuditAction;
import com.bankflow.entity.KycDocument;
import com.bankflow.entity.User;
import com.bankflow.exception.ResourceNotFoundException;
import com.bankflow.repository.KycDocumentRepository;
import com.bankflow.security.VirusScanResult;
import com.bankflow.security.VirusScanService;
import com.bankflow.specification.KycDocumentSpecification;
import com.bankflow.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycService {

    private final KycDocumentRepository kycDocumentRepository;
    private final FileStorageService fileStorageService;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final VirusScanService virusScanService;

    @Value("${app.kyc.notification-enabled:false}")
    private boolean kycNotificationEnabled;

    @Transactional
    public KycDocument uploadDocument(MultipartFile file, KycDocument.DocumentType documentType) {
        User user = currentUserService.getCurrentUser();
        validateFile(file);
        VirusScanResult scanResult =
                virusScanService.scan(file);


        if (!scanResult.clean()) {
            log.error("File scan found issue in security verification: {}", scanResult.message());
            throw new IllegalArgumentException("File failed security scan: ");
        }

        List<KycDocument> existingDocuments =
                kycDocumentRepository
                        .findByUserIdAndDocumentTypeOrderByUploadedAtDesc(
                                user.getId(),
                                documentType
                        );

        if (!existingDocuments.isEmpty()) {

            KycDocument latest = existingDocuments.getFirst();

            if (latest.getKycVerificationStatus()
                    != KycDocument.KycVerificationStatus.REJECTED) {

                throw new IllegalArgumentException(
                        "Document already uploaded and cannot be replaced"
                );
            }
        }
        StoredFileMetadata metadata =
                fileStorageService.store(file, user.getId());

        try {

            KycDocument document = KycDocument.builder()
                    .user(user)
                    .documentType(documentType)
                    .originalFileName(file.getOriginalFilename())
                    .storedFileName(
                            Paths.get(metadata.path())
                                    .getFileName()
                                    .toString()
                    )
                    .storagePath(metadata.path())
                    .storageType(metadata.storageType())
                    .s3Bucket(metadata.bucket())
                    .s3ObjectKey(metadata.objectKey())
                    .encryptionType(metadata.encryptionType())
                    .checksum(metadata.checksum())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();

            KycDocument saved =
                    kycDocumentRepository.save(document);

            auditLogService.log(
                    AuditAction.KYC_DOCUMENT_UPLOADED,
                    "KYC document uploaded: "
                            + documentType
                            + " for user ID "
                            + user.getId()
            );

            return saved;

        } catch (Exception e) {

            fileStorageService.delete(metadata.path());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getMyDocuments() {
        User user =
                currentUserService.getCurrentUser();
        return kycDocumentRepository
                .findByUserIdOrderByUploadedAtDesc(user.getId())
                .stream()
                .map(document ->
                        new KycDocumentResponse(
                                document.getId(),
                                document.getDocumentType().name(),
                                document.getOriginalFileName(),
                                document.getKycVerificationStatus().name(),
                                document.getUploadedAt(),
                                document.getRejectionReason()
                        )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public KycStatusResponse getMyKycStatus() {

        User user = currentUserService.getCurrentUser();

        List<KycDocument> documents =
                kycDocumentRepository
                        .findByUserIdOrderByUploadedAtDesc(user.getId());

        KycDocument panDocument =
                documents.stream()
                        .filter(document ->
                                document.getDocumentType()
                                        == KycDocument.DocumentType.PAN)
                        .findFirst()
                        .orElse(null);

        KycDocument aadhaarDocument =
                documents.stream()
                        .filter(document ->
                                document.getDocumentType()
                                        == KycDocument.DocumentType.AADHAAR)
                        .findFirst()
                        .orElse(null);

        KycStatusResponse.DocumentStatus pan = mapDocumentStatus(panDocument);

        KycStatusResponse.DocumentStatus aadhaar = mapDocumentStatus(aadhaarDocument);

        String overallStatus = getOverallStatus(panDocument, aadhaarDocument);

        return new KycStatusResponse(overallStatus, pan, aadhaar);
    }

    private static @NonNull String getOverallStatus(
            KycDocument panDocument,
            KycDocument aadhaarDocument
    ) {
        // User has not uploaded anything
        if (panDocument == null && aadhaarDocument == null) {
            return "NOT_SUBMITTED";
        }
        // Any mandatory document missing
        if (panDocument == null || aadhaarDocument == null) {
            return "INCOMPLETE";
        }
        // Any document rejected
        if (
                panDocument.getKycVerificationStatus()
                        == KycDocument.KycVerificationStatus.REJECTED
                        ||
                        aadhaarDocument.getKycVerificationStatus()
                                == KycDocument.KycVerificationStatus.REJECTED
        ) {
            return "REJECTED";
        }
        // Both documents verified
        if (
                panDocument.getKycVerificationStatus() == KycDocument.KycVerificationStatus.VERIFIED
                        &&
                        aadhaarDocument.getKycVerificationStatus() == KycDocument.KycVerificationStatus.VERIFIED
        ) {
            return "VERIFIED";
        }
        // Both uploaded but admin verification pending
        return "UNDER_REVIEW";
    }

    @Transactional(readOnly = true)
    public Resource getCustomerDocumentResource(Long documentId) {

        User user = currentUserService.getCurrentUser();

        KycDocument document = kycDocumentRepository.findById(
                documentId).orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        // Important security check
        if (!document.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not allowed to access this document");
        }

        return fileStorageService.load(document.getStoragePath());
    }

    @Transactional(readOnly = true)
    public Page<AdminKycDocumentResponse> getAllDocuments(
            int page,
            int size,
            String search,
            KycDocument.KycVerificationStatus status
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("uploadedAt")
                                .descending()
                );


        Specification<KycDocument> specification =
                Specification
                        .where(
                                KycDocumentSpecification.search(search)
                        )
                        .and(
                                KycDocumentSpecification.status(status)
                        );


        return kycDocumentRepository
                .findAll(
                        specification,
                        pageable
                )
                .map(document ->
                        new AdminKycDocumentResponse(
                                document.getId(),
                                document.getUser().getId(),
                                document.getUser().getFullName(),
                                document.getUser().getEmail(),
                                document.getDocumentType().name(),
                                document.getOriginalFileName(),
                                document.getKycVerificationStatus().name(),
                                document.getRejectionReason(),
                                document.getUploadedAt()
                        )
                );
    }

    @Transactional
    public void verifyDocument(Long documentId) {

        KycDocument document = kycDocumentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("KYC document not found"));

        if (document.getKycVerificationStatus() != KycDocument.KycVerificationStatus.PENDING) {
            throw new IllegalStateException("Only pending documents can be verified");
        }

        document.setKycVerificationStatus(KycDocument.KycVerificationStatus.VERIFIED);

        document.setRejectionReason(null);

        kycDocumentRepository.save(document);

        if (kycNotificationEnabled) {
            emailService.sendKycApprovedEmail(
                    document.getUser(), document.getDocumentType().name());
        }

        auditLogService.log(
                AuditAction.KYC_DOCUMENT_VERIFIED,
                "KYC document verified: "
                        + document.getDocumentType()
                        + " for user ID "
                        + document.getUser().getId()
        );
    }

    @Transactional
    public void rejectDocument(Long documentId, String reason) {

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Rejection reason is required");
        }

        if (reason.length() > 500) {
            throw new IllegalArgumentException(
                    "Rejection reason cannot exceed 500 characters");
        }

        KycDocument document = kycDocumentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("KYC document not found"));

        if (document.getKycVerificationStatus() != KycDocument.KycVerificationStatus.PENDING) {
            throw new IllegalStateException("Only pending documents can be rejected");
        }

        document.setKycVerificationStatus(KycDocument.KycVerificationStatus.REJECTED);

        document.setRejectionReason(reason.trim());

        kycDocumentRepository.save(document);

        if (kycNotificationEnabled) {
            emailService.sendKycRejectedEmail(
                    document.getUser(), document.getDocumentType().name(), reason.trim());
        }

        auditLogService.log(
                AuditAction.KYC_DOCUMENT_REJECTED,
                "KYC document rejected: "
                        + document.getDocumentType()
                        + " for user ID "
                        + document.getUser().getId()
                        + ". Reason: "
                        + reason.trim()
        );
    }

    @Transactional(readOnly = true)
    public Resource getAdminDocumentResource(Long documentId) {

        KycDocument document =
                getAdminDocumentDetails(documentId);

        return fileStorageService.load(
                document.getStoragePath()
        );
    }

    public KycDocument getAdminDocumentDetails(Long documentId) {

        return kycDocumentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "KYC document not found"
                        ));
    }

    public KycDocument getCustomerDocumentDetails(Long documentId) {
        User user = currentUserService.getCurrentUser();
        KycDocument document =
                kycDocumentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Document not found"
                                )
                        );
        if (!document.getUser().getId()
                .equals(user.getId())) {
            throw new IllegalArgumentException(
                    "You are not allowed to access this document"
            );
        }
        return document;
    }

    @Transactional(readOnly = true)
    public KycSummaryResponse getKycSummary() {


        long total =
                kycDocumentRepository.count();


        long pending =
                kycDocumentRepository
                        .countByKycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        );


        long verified =
                kycDocumentRepository
                        .countByKycVerificationStatus(
                                KycDocument.KycVerificationStatus.VERIFIED
                        );


        long rejected =
                kycDocumentRepository
                        .countByKycVerificationStatus(
                                KycDocument.KycVerificationStatus.REJECTED
                        );


        long pendingCustomers =
                kycDocumentRepository.countPendingCustomers();


        return new KycSummaryResponse(
                total,
                pending,
                verified,
                rejected,
                pendingCustomers
        );
    }

    private KycStatusResponse.DocumentStatus mapDocumentStatus(KycDocument document) {
        if (document == null) {
            return new KycStatusResponse.DocumentStatus(
                    false, "NOT_UPLOADED", null);
        }

        return new KycStatusResponse.DocumentStatus(
                true, document.getKycVerificationStatus().name(), document.getRejectionReason());
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size cannot exceed 5 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("application/pdf") ||
                contentType.equals("image/png") || contentType.equals("image/jpeg"))) {
            throw new IllegalArgumentException("Only PDF, PNG and JPG files are allowed");
        }
    }
}
