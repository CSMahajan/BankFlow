package com.bankflow.service;

import com.bankflow.dto.*;
import com.bankflow.entity.*;
import com.bankflow.exception.ResourceNotFoundException;
import com.bankflow.repository.*;
import com.bankflow.security.FileNameSanitizer;
import com.bankflow.security.FileSecurityValidator;
import com.bankflow.security.VirusScanResult;
import com.bankflow.security.VirusScanService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycServiceTest {

    @Mock
    private KycDocumentRepository kycDocumentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private EmailService emailService;

    @Mock
    private VirusScanService virusScanService;

    @Mock
    private GuardDutyMalwareScanService guardDutyMalwareScanService;

    @Mock
    private FileSecurityValidator fileSecurityValidator;

    @Mock
    private FileNameSanitizer fileNameSanitizer;

    @Mock
    private KycExtractionEventPublisher kycExtractionEventPublisher;

    @Mock
    private KycExtractedDataRepository kycExtractedDataRepository;

    @Mock
    private KycPanDataRepository kycPanDataRepository;

    @Mock
    private KycAadhaarDataRepository kycAadhaarDataRepository;

    @Mock
    private KycMalwareScanRepository kycMalwareScanRepository;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private KycService kycService;

    private User user;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .role(User.Role.CUSTOMER)
                .emailVerified(true)
                .build();

        ReflectionTestUtils.setField(
                kycService,
                "kycNotificationEnabled",
                false
        );

        ReflectionTestUtils.setField(
                kycService,
                "malwareScanMaxAttempts",
                3
        );
    }

    @AfterEach
    void tearDown() {
        // Nothing required currently.
    }

    // ============================================================
    // uploadDocument()
    // ============================================================

    @Test
    void uploadDocument_shouldUploadDocumentSuccessfully() {

        KycDocument.DocumentType documentType =
                KycDocument.DocumentType.PAN;

        StoredFileMetadata metadata =
                new StoredFileMetadata(
                        "user-1/document.pdf",
                        "S3",
                        "bankflow-test-bucket",
                        "user-1/document.pdf",
                        "AES256",
                        "checksum123"
                );

        VirusScanResult scanResult = mock(VirusScanResult.class);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(file.getOriginalFilename())
                .thenReturn("my-pan.pdf");

        when(file.getContentType())
                .thenReturn("application/pdf");

        when(file.getSize())
                .thenReturn(100L);

        when(virusScanService.scan(file))
                .thenReturn(scanResult);

        when(scanResult.clean())
                .thenReturn(true);

        when(kycDocumentRepository
                .findByUserIdAndDocumentTypeOrderByUploadedAtDesc(
                        user.getId(),
                        documentType
                ))
                .thenReturn(List.of());

        when(fileNameSanitizer.sanitize("my-pan.pdf"))
                .thenReturn("my-pan.pdf");

        when(fileStorageService.store(file, user.getId()))
                .thenReturn(metadata);

        KycDocument savedDocument = KycDocument.builder()
                .id(100L)
                .user(user)
                .documentType(documentType)
                .originalFileName("my-pan.pdf")
                .storedFileName("document.pdf")
                .storagePath(metadata.path())
                .storageType(metadata.storageType())
                .s3Bucket(metadata.bucket())
                .s3ObjectKey(metadata.objectKey())
                .encryptionType(metadata.encryptionType())
                .checksum(metadata.checksum())
                .contentType("application/pdf")
                .fileSize(100L)
                .build();

        when(kycDocumentRepository.save(any(KycDocument.class)))
                .thenReturn(savedDocument);

        KycDocument result =
                kycService.uploadDocument(file, documentType);

        assertNotNull(result);
        assertEquals(100L, result.getId());

        ArgumentCaptor<KycDocument> documentCaptor =
                ArgumentCaptor.forClass(KycDocument.class);

        verify(kycDocumentRepository)
                .save(documentCaptor.capture());

        KycDocument document = documentCaptor.getValue();

        assertEquals(user, document.getUser());
        assertEquals(documentType, document.getDocumentType());
        assertEquals("my-pan.pdf", document.getOriginalFileName());
        assertEquals("document.pdf", document.getStoredFileName());
        assertEquals(metadata.path(), document.getStoragePath());
        assertEquals("S3", document.getStorageType());
        assertEquals("bankflow-test-bucket", document.getS3Bucket());
        assertEquals(metadata.objectKey(), document.getS3ObjectKey());
        assertEquals("AES256", document.getEncryptionType());
        assertEquals("checksum123", document.getChecksum());
        assertEquals("application/pdf", document.getContentType());
        assertEquals(100L, document.getFileSize());

        verify(kycDocumentRepository)
                .findByUserIdAndDocumentTypeOrderByUploadedAtDesc(
                        user.getId(),
                        documentType
                );

        verify(fileSecurityValidator)
                .validate(file);

        verify(virusScanService)
                .scan(file);

        verify(fileNameSanitizer)
                .sanitize("my-pan.pdf");

        verify(fileStorageService)
                .store(file, user.getId());

        verify(auditLogService)
                .log(
                        eq(AuditAction.KYC_DOCUMENT_UPLOADED),
                        contains("PAN")
                );

        verify(kycMalwareScanRepository)
                .save(any(KycMalwareScan.class));
    }

    @Test
    void uploadDocument_shouldThrowWhenVirusScanFails() {

        VirusScanResult scanResult = mock(VirusScanResult.class);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(virusScanService.scan(file))
                .thenReturn(scanResult);

        when(scanResult.clean())
                .thenReturn(false);

        when(scanResult.message())
                .thenReturn("Malicious file detected");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> kycService.uploadDocument(
                                file,
                                KycDocument.DocumentType.PAN
                        )
                );

        assertEquals(
                "File failed security scan: Malicious file detected",
                exception.getMessage()
        );

        verify(fileSecurityValidator)
                .validate(file);

        verify(virusScanService)
                .scan(file);

        verifyNoInteractions(fileStorageService);
        verifyNoInteractions(kycDocumentRepository);
    }

    @Test
    void uploadDocument_shouldRejectReplacementWhenExistingDocumentIsNotRejected() {

        KycDocument existingDocument =
                KycDocument.builder()
                        .id(10L)
                        .user(user)
                        .documentType(KycDocument.DocumentType.PAN)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .build();

        VirusScanResult scanResult = mock(VirusScanResult.class);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(virusScanService.scan(file))
                .thenReturn(scanResult);

        when(scanResult.clean())
                .thenReturn(true);

        when(kycDocumentRepository
                .findByUserIdAndDocumentTypeOrderByUploadedAtDesc(
                        user.getId(),
                        KycDocument.DocumentType.PAN
                ))
                .thenReturn(List.of(existingDocument));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> kycService.uploadDocument(
                                file,
                                KycDocument.DocumentType.PAN
                        )
                );

        assertEquals(
                "Document already uploaded and cannot be replaced",
                exception.getMessage()
        );

        verifyNoInteractions(fileStorageService);
        verifyNoInteractions(fileNameSanitizer);
    }

    @Test
    void uploadDocument_shouldAllowReplacementWhenExistingDocumentWasRejected() {

        KycDocument rejectedDocument =
                KycDocument.builder()
                        .id(10L)
                        .user(user)
                        .documentType(KycDocument.DocumentType.PAN)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.REJECTED
                        )
                        .build();

        VirusScanResult scanResult = mock(VirusScanResult.class);

        StoredFileMetadata metadata =
                new StoredFileMetadata(
                        "user-1/new-pan.pdf",
                        "S3",
                        "bucket",
                        "user-1/new-pan.pdf",
                        "AES256",
                        "checksum"
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(virusScanService.scan(file))
                .thenReturn(scanResult);

        when(scanResult.clean())
                .thenReturn(true);

        when(kycDocumentRepository
                .findByUserIdAndDocumentTypeOrderByUploadedAtDesc(
                        user.getId(),
                        KycDocument.DocumentType.PAN
                ))
                .thenReturn(List.of(rejectedDocument));

        when(file.getOriginalFilename())
                .thenReturn("new-pan.pdf");

        when(fileNameSanitizer.sanitize("new-pan.pdf"))
                .thenReturn("new-pan.pdf");

        when(fileStorageService.store(file, user.getId()))
                .thenReturn(metadata);

        KycDocument saved =
                KycDocument.builder()
                        .id(20L)
                        .user(user)
                        .documentType(KycDocument.DocumentType.PAN)
                        .storagePath(metadata.path())
                        .build();

        when(kycDocumentRepository.save(any(KycDocument.class)))
                .thenReturn(saved);

        KycDocument result =
                kycService.uploadDocument(
                        file,
                        KycDocument.DocumentType.PAN
                );

        assertEquals(20L, result.getId());

        verify(fileStorageService)
                .store(file, user.getId());

        verify(kycDocumentRepository)
                .save(any(KycDocument.class));
    }

    @Test
    void uploadDocument_shouldDeleteStoredFileWhenDatabaseOperationFails() {

        VirusScanResult scanResult = mock(VirusScanResult.class);

        StoredFileMetadata metadata =
                new StoredFileMetadata(
                        "user-1/document.pdf",
                        "S3",
                        "bucket",
                        "user-1/document.pdf",
                        "AES256",
                        "checksum"
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(virusScanService.scan(file))
                .thenReturn(scanResult);

        when(scanResult.clean())
                .thenReturn(true);

        when(kycDocumentRepository
                .findByUserIdAndDocumentTypeOrderByUploadedAtDesc(
                        user.getId(),
                        KycDocument.DocumentType.PAN
                ))
                .thenReturn(List.of());

        when(file.getOriginalFilename())
                .thenReturn("document.pdf");

        when(fileNameSanitizer.sanitize("document.pdf"))
                .thenReturn("document.pdf");

        when(file.getContentType())
                .thenReturn("application/pdf");

        when(file.getSize())
                .thenReturn(100L);

        when(fileStorageService.store(file, user.getId()))
                .thenReturn(metadata);

        when(kycDocumentRepository.save(any(KycDocument.class)))
                .thenThrow(new RuntimeException("Database failure"));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> kycService.uploadDocument(
                                file,
                                KycDocument.DocumentType.PAN
                        )
                );

        assertEquals(
                "Database failure",
                exception.getMessage()
        );

        verify(fileStorageService)
                .delete(metadata.path());
    }

    // ============================================================
    // getMyDocuments()
    // ============================================================

    @Test
    void getMyDocuments_shouldReturnMappedDocuments() {

        LocalDateTime uploadedAt =
                LocalDateTime.of(2026, 8, 30, 10, 30);

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .user(user)
                        .documentType(KycDocument.DocumentType.PAN)
                        .originalFileName("pan.pdf")
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.VERIFIED
                        )
                        .uploadedAt(uploadedAt)
                        .rejectionReason(null)
                        .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository
                .findByUserIdOrderByUploadedAtDesc(user.getId()))
                .thenReturn(List.of(document));

        List<KycDocumentResponse> result =
                kycService.getMyDocuments();

        assertEquals(1, result.size());

        KycDocumentResponse response =
                result.get(0);

        assertEquals(10L, response.id());
        assertEquals("PAN", response.documentType());
        assertEquals("pan.pdf", response.originalFileName());
        assertEquals("VERIFIED", response.status());
        assertEquals(uploadedAt, response.uploadedAt());
        assertNull(response.rejectionReason());
    }

    @Test
    void getMyDocuments_shouldReturnEmptyListWhenNoDocumentsExist() {

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository
                .findByUserIdOrderByUploadedAtDesc(user.getId()))
                .thenReturn(List.of());

        List<KycDocumentResponse> result =
                kycService.getMyDocuments();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ============================================================
    // getMyKycStatus()
    // ============================================================

    @Test
    void getMyKycStatus_shouldReturnNotSubmittedWhenNoDocumentsExist() {

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository
                .findByUserIdOrderByUploadedAtDesc(user.getId()))
                .thenReturn(List.of());

        KycStatusResponse result =
                kycService.getMyKycStatus();

        assertEquals(
                "NOT_SUBMITTED",
                result.overallStatus()
        );

        assertFalse(result.pan().uploaded());
        assertEquals(
                "NOT_UPLOADED",
                result.pan().status()
        );

        assertFalse(result.aadhaar().uploaded());
        assertEquals(
                "NOT_UPLOADED",
                result.aadhaar().status()
        );
    }

    @Test
    void getMyKycStatus_shouldReturnIncompleteWhenOneDocumentIsMissing() {

        KycDocument pan =
                KycDocument.builder()
                        .id(10L)
                        .documentType(KycDocument.DocumentType.PAN)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository
                .findByUserIdOrderByUploadedAtDesc(user.getId()))
                .thenReturn(List.of(pan));

        KycStatusResponse result =
                kycService.getMyKycStatus();

        assertEquals(
                "INCOMPLETE",
                result.overallStatus()
        );

        assertTrue(result.pan().uploaded());
        assertEquals("PENDING", result.pan().status());

        assertFalse(result.aadhaar().uploaded());
    }

    @Test
    void getMyKycStatus_shouldReturnRejectedWhenAnyDocumentIsRejected() {

        KycDocument pan =
                KycDocument.builder()
                        .id(10L)
                        .documentType(KycDocument.DocumentType.PAN)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.REJECTED
                        )
                        .rejectionReason("Invalid PAN")
                        .build();

        KycDocument aadhaar =
                KycDocument.builder()
                        .id(11L)
                        .documentType(KycDocument.DocumentType.AADHAAR)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.VERIFIED
                        )
                        .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository
                .findByUserIdOrderByUploadedAtDesc(user.getId()))
                .thenReturn(List.of(pan, aadhaar));

        KycStatusResponse result =
                kycService.getMyKycStatus();

        assertEquals(
                "REJECTED",
                result.overallStatus()
        );

        assertEquals(
                "Invalid PAN",
                result.pan().rejectionReason()
        );
    }

    @Test
    void getMyKycStatus_shouldReturnVerifiedWhenBothDocumentsAreVerified() {

        KycDocument pan =
                KycDocument.builder()
                        .id(10L)
                        .documentType(KycDocument.DocumentType.PAN)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.VERIFIED
                        )
                        .build();

        KycDocument aadhaar =
                KycDocument.builder()
                        .id(11L)
                        .documentType(KycDocument.DocumentType.AADHAAR)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.VERIFIED
                        )
                        .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository
                .findByUserIdOrderByUploadedAtDesc(user.getId()))
                .thenReturn(List.of(pan, aadhaar));

        KycStatusResponse result =
                kycService.getMyKycStatus();

        assertEquals(
                "VERIFIED",
                result.overallStatus()
        );
    }

    @Test
    void getMyKycStatus_shouldReturnUnderReviewWhenBothDocumentsArePending() {

        KycDocument pan =
                KycDocument.builder()
                        .documentType(KycDocument.DocumentType.PAN)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .build();

        KycDocument aadhaar =
                KycDocument.builder()
                        .documentType(KycDocument.DocumentType.AADHAAR)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository
                .findByUserIdOrderByUploadedAtDesc(user.getId()))
                .thenReturn(List.of(pan, aadhaar));

        KycStatusResponse result =
                kycService.getMyKycStatus();

        assertEquals(
                "UNDER_REVIEW",
                result.overallStatus()
        );
    }

    // ============================================================
    // Customer document access
    // ============================================================

    @Test
    void getCustomerDocumentResource_shouldReturnResourceForOwner() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .user(user)
                        .storagePath("user-1/document.pdf")
                        .build();

        Resource resource = mock(Resource.class);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(fileStorageService.load("user-1/document.pdf"))
                .thenReturn(resource);

        Resource result =
                kycService.getCustomerDocumentResource(10L);

        assertSame(resource, result);

        verify(fileStorageService)
                .load("user-1/document.pdf");
    }

    @Test
    void getCustomerDocumentResource_shouldThrowWhenDocumentDoesNotExist() {

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> kycService.getCustomerDocumentResource(10L)
        );

        verifyNoInteractions(fileStorageService);
    }

    @Test
    void getCustomerDocumentResource_shouldRejectAccessToAnotherUsersDocument() {

        User anotherUser =
                User.builder()
                        .id(2L)
                        .build();

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .user(anotherUser)
                        .storagePath("user-2/document.pdf")
                        .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> kycService.getCustomerDocumentResource(10L)
                );

        assertEquals(
                "You are not allowed to access this document",
                exception.getMessage()
        );

        verifyNoInteractions(fileStorageService);
    }

    // ============================================================
    // getExtractionResult()
    // ============================================================

    @Test
    void getExtractionResult_shouldReturnExtractionResponse() {

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 8, 30, 10, 0);

        LocalDateTime updatedAt =
                LocalDateTime.of(2026, 8, 30, 10, 30);

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .user(user)
                        .build();

        KycExtractedData extractedData =
                KycExtractedData.builder()
                        .kycDocument(document)
                        .extractedText("PAN: ABCDE1234F")
                        .extractionStatus(
                                KycExtractedData.ExtractionStatus.SUCCESS
                        )
                        .failureReason(null)
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycExtractedDataRepository
                .findByKycDocumentId(10L))
                .thenReturn(Optional.of(extractedData));

        KycExtractionResponse result =
                kycService.getExtractionResult(10L);

        assertEquals(10L, result.documentId());
        assertEquals(
                "SUCCESS",
                result.extractionStatus()
        );
        assertEquals(
                "PAN: ABCDE1234F",
                result.extractedText()
        );
        assertNull(result.failureReason());
        assertEquals(createdAt, result.createdAt());
        assertEquals(updatedAt, result.updatedAt());
    }

    @Test
    void getExtractionResult_shouldThrowWhenExtractionDataDoesNotExist() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .user(user)
                        .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycExtractedDataRepository
                .findByKycDocumentId(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> kycService.getExtractionResult(10L)
        );
    }

    // ============================================================
    // getAllDocuments()
    // ============================================================

    @Test
    void getAllDocuments_shouldReturnPaginatedDocuments() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .user(user)
                        .documentType(KycDocument.DocumentType.PAN)
                        .originalFileName("pan.pdf")
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .uploadedAt(
                                LocalDateTime.of(
                                        2026, 8, 30, 10, 0
                                )
                        )
                        .build();

        Page<KycDocument> page =
                new PageImpl<>(List.of(document));

        when(kycDocumentRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(kycExtractedDataRepository
                .findByKycDocumentId(10L))
                .thenReturn(Optional.empty());

        Page<AdminKycDocumentResponse> result =
                kycService.getAllDocuments(
                        0,
                        10,
                        null,
                        null
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        AdminKycDocumentResponse response =
                result.getContent().get(0);

        assertEquals(10L, response.id());
        assertEquals(1L, response.userId());
        assertEquals("John Doe", response.customerName());
        assertEquals("john@example.com", response.email());
        assertEquals("PAN", response.documentType());
        assertEquals("pan.pdf", response.originalFileName());
        assertEquals("PENDING", response.extractionStatus());

        verify(kycDocumentRepository).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
    }

    @Test
    void getAllDocuments_shouldUseRequestedPageAndSizeAndDescendingUploadedAt() {

        when(kycDocumentRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        kycService.getAllDocuments(
                2,
                20,
                "john",
                KycDocument.KycVerificationStatus.PENDING
        );

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(kycDocumentRepository).findAll(
                any(Specification.class),
                captor.capture()
        );

        Pageable pageable = captor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());

        Sort.Order order =
                pageable.getSort()
                        .getOrderFor("uploadedAt");

        assertNotNull(order);
        assertEquals(
                Sort.Direction.DESC,
                order.getDirection()
        );
    }

    // ============================================================
    // verifyDocument()
    // ============================================================

    @Test
    void verifyDocument_shouldVerifySuccessfully() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .user(user)
                        .documentType(KycDocument.DocumentType.PAN)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .rejectionReason("Old reason")
                        .build();

        KycExtractedData extractedData =
                KycExtractedData.builder()
                        .kycDocument(document)
                        .extractionStatus(
                                KycExtractedData.ExtractionStatus.SUCCESS
                        )
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycExtractedDataRepository
                .findByKycDocumentId(10L))
                .thenReturn(Optional.of(extractedData));

        kycService.verifyDocument(10L);

        assertEquals(
                KycDocument.KycVerificationStatus.VERIFIED,
                document.getKycVerificationStatus()
        );

        assertNull(document.getRejectionReason());

        verify(kycDocumentRepository)
                .save(document);

        verify(auditLogService)
                .log(
                        eq(AuditAction.KYC_DOCUMENT_VERIFIED),
                        contains("PAN")
                );

        verifyNoInteractions(emailService);
    }

    @Test
    void verifyDocument_shouldThrowWhenDocumentIsNotPending() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.VERIFIED
                        )
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> kycService.verifyDocument(10L)
                );

        assertEquals(
                "Only pending documents can be verified",
                exception.getMessage()
        );

        verifyNoInteractions(kycExtractedDataRepository);
        verify(kycDocumentRepository, never())
                .save(any());
    }

    @Test
    void verifyDocument_shouldThrowWhenExtractionDoesNotExist() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycExtractedDataRepository
                .findByKycDocumentId(10L))
                .thenReturn(Optional.empty());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> kycService.verifyDocument(10L)
                );

        assertEquals(
                "OCR extraction not completed",
                exception.getMessage()
        );
    }

    @Test
    void verifyDocument_shouldThrowWhenExtractionFailed() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .build();

        KycExtractedData extractedData =
                KycExtractedData.builder()
                        .kycDocument(document)
                        .extractionStatus(
                                KycExtractedData.ExtractionStatus.FAILED
                        )
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycExtractedDataRepository
                .findByKycDocumentId(10L))
                .thenReturn(Optional.of(extractedData));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> kycService.verifyDocument(10L)
                );

        assertEquals(
                "Document cannot be verified before successful OCR extraction",
                exception.getMessage()
        );

        verify(kycDocumentRepository, never())
                .save(any());
    }

    // ============================================================
    // rejectDocument()
    // ============================================================

    @Test
    void rejectDocument_shouldRejectSuccessfully() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .user(user)
                        .documentType(KycDocument.DocumentType.PAN)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        kycService.rejectDocument(
                10L,
                "  Invalid PAN details  "
        );

        assertEquals(
                KycDocument.KycVerificationStatus.REJECTED,
                document.getKycVerificationStatus()
        );

        assertEquals(
                "Invalid PAN details",
                document.getRejectionReason()
        );

        verify(kycDocumentRepository)
                .save(document);

        verify(auditLogService)
                .log(
                        eq(AuditAction.KYC_DOCUMENT_REJECTED),
                        contains("Invalid PAN details")
                );
    }

    @Test
    void rejectDocument_shouldThrowWhenReasonIsNull() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> kycService.rejectDocument(
                                10L,
                                null
                        )
                );

        assertEquals(
                "Rejection reason is required",
                exception.getMessage()
        );

        verifyNoInteractions(kycDocumentRepository);
    }

    @Test
    void rejectDocument_shouldThrowWhenReasonIsBlank() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> kycService.rejectDocument(
                                10L,
                                "   "
                        )
                );

        assertEquals(
                "Rejection reason is required",
                exception.getMessage()
        );
    }

    @Test
    void rejectDocument_shouldThrowWhenReasonExceeds500Characters() {

        String reason = "a".repeat(501);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> kycService.rejectDocument(
                                10L,
                                reason
                        )
                );

        assertEquals(
                "Rejection reason cannot exceed 500 characters",
                exception.getMessage()
        );

        verifyNoInteractions(kycDocumentRepository);
    }

    @Test
    void rejectDocument_shouldThrowWhenDocumentIsNotPending() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.VERIFIED
                        )
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> kycService.rejectDocument(
                                10L,
                                "Invalid document"
                        )
                );

        assertEquals(
                "Only pending documents can be rejected",
                exception.getMessage()
        );

        verify(kycDocumentRepository, never())
                .save(any());
    }

    // ============================================================
    // Admin document access
    // ============================================================

    @Test
    void getAdminDocumentDetails_shouldReturnDocument() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        KycDocument result =
                kycService.getAdminDocumentDetails(10L);

        assertSame(document, result);
    }

    @Test
    void getAdminDocumentDetails_shouldThrowWhenNotFound() {

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> kycService.getAdminDocumentDetails(10L)
        );
    }

    @Test
    void getAdminDocumentResource_shouldLoadDocumentFromStorage() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .storagePath("user-1/pan.pdf")
                        .build();

        Resource resource = mock(Resource.class);

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(fileStorageService.load("user-1/pan.pdf"))
                .thenReturn(resource);

        Resource result =
                kycService.getAdminDocumentResource(10L);

        assertSame(resource, result);

        verify(fileStorageService)
                .load("user-1/pan.pdf");
    }

    @Test
    void getCustomerDocumentDetails_shouldReturnOwnedDocument() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .user(user)
                        .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        KycDocument result =
                kycService.getCustomerDocumentDetails(10L);

        assertSame(document, result);
    }

    @Test
    void getCustomerDocumentDetails_shouldRejectAnotherUsersDocument() {

        User anotherUser =
                User.builder()
                        .id(2L)
                        .build();

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .user(anotherUser)
                        .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        assertThrows(
                IllegalArgumentException.class,
                () -> kycService.getCustomerDocumentDetails(10L)
        );
    }

    // ============================================================
    // KYC Summary
    // ============================================================

    @Test
    void getKycSummary_shouldReturnCorrectCounts() {

        when(kycDocumentRepository.count())
                .thenReturn(10L);

        when(kycDocumentRepository
                .countByKycVerificationStatus(
                        KycDocument.KycVerificationStatus.PENDING
                ))
                .thenReturn(4L);

        when(kycDocumentRepository
                .countByKycVerificationStatus(
                        KycDocument.KycVerificationStatus.VERIFIED
                ))
                .thenReturn(5L);

        when(kycDocumentRepository
                .countByKycVerificationStatus(
                        KycDocument.KycVerificationStatus.REJECTED
                ))
                .thenReturn(1L);

        when(kycDocumentRepository.countPendingCustomers())
                .thenReturn(3L);

        KycSummaryResponse result =
                kycService.getKycSummary();

        assertEquals(10L, result.totalDocuments());
        assertEquals(4L, result.pendingDocuments());
        assertEquals(5L, result.verifiedDocuments());
        assertEquals(1L, result.rejectedDocuments());
        assertEquals(3L, result.pendingCustomers());
    }

    // ============================================================
    // PAN data
    // ============================================================

    @Test
    void getPanData_shouldReturnPanDataForVerifiedDocument() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .user(user)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.VERIFIED
                        )
                        .build();

        KycPanData panData =
                KycPanData.builder()
                        .kycDocument(document)
                        .panNumber("ABCDE1234F")
                        .fullName("John Doe")
                        .fatherName("Robert Doe")
                        .dateOfBirth(LocalDate.of(1990, 1, 1))
                        .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycPanDataRepository
                .findByKycDocumentId(10L))
                .thenReturn(Optional.of(panData));

        PanDataResponse result =
                kycService.getPanData(10L);

        assertEquals(10L, result.documentId());
        assertEquals("ABCDE1234F", result.panNumber());
        assertEquals("John Doe", result.fullName());
        assertEquals("Robert Doe", result.fatherName());
    }

    @Test
    void getPanData_shouldRejectUnverifiedDocument() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .user(user)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> kycService.getPanData(10L)
                );

        assertEquals(
                "PAN data available only after KYC verification",
                exception.getMessage()
        );

        verifyNoInteractions(kycPanDataRepository);
    }

    @Test
    void getPanData_shouldThrowWhenPanDataDoesNotExist() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .user(user)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.VERIFIED
                        )
                        .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycPanDataRepository
                .findByKycDocumentId(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> kycService.getPanData(10L)
        );
    }

    // ============================================================
    // Aadhaar data
    // ============================================================

    @Test
    void getAadhaarData_shouldRejectUnverifiedDocument() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .user(user)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .build();

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> kycService.getAadhaarData(10L)
                );

        assertEquals(
                "Aadhaar data available only after KYC verification",
                exception.getMessage()
        );

        verifyNoInteractions(kycAadhaarDataRepository);
    }

    // ============================================================
    // Admin PAN / Aadhaar
    // ============================================================

    @Test
    void getAdminPanData_shouldReturnPanData() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .build();

        KycPanData panData =
                KycPanData.builder()
                        .kycDocument(document)
                        .panNumber("ABCDE1234F")
                        .fullName("John Doe")
                        .fatherName("Robert Doe")
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycPanDataRepository
                .findByKycDocumentId(10L))
                .thenReturn(Optional.of(panData));

        PanDataResponse result =
                kycService.getAdminPanData(10L);

        assertEquals("ABCDE1234F", result.panNumber());
        assertEquals("John Doe", result.fullName());
    }

    @Test
    void getAdminAadhaarData_shouldThrowWhenDataDoesNotExist() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycAadhaarDataRepository
                .findByKycDocumentId(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> kycService.getAdminAadhaarData(10L)
        );
    }

    // ============================================================
    // Admin extraction status
    // ============================================================

    @Test
    void getAdminExtractionStatus_shouldReturnExtractionAndMalwareStatus() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .documentType(
                                KycDocument.DocumentType.PAN
                        )
                        .build();

        KycExtractedData extractedData =
                KycExtractedData.builder()
                        .kycDocument(document)
                        .extractionStatus(
                                KycExtractedData.ExtractionStatus.SUCCESS
                        )
                        .build();

        KycMalwareScan scan =
                KycMalwareScan.builder()
                        .kycDocument(document)
                        .status(
                                KycMalwareScan.MalwareStatus.CLEAN
                        )
                        .attemptNumber(2)
                        .build();

        when(kycExtractedDataRepository
                .findByKycDocumentId(10L))
                .thenReturn(Optional.of(extractedData));

        when(kycMalwareScanRepository
                .findFirstByKycDocumentIdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(scan));

        AdminKycExtractionResponse result =
                kycService.getAdminExtractionStatus(10L);

        assertEquals(10L, result.documentId());
        assertEquals("PAN", result.documentType());
        assertEquals("SUCCESS", result.extractionStatus());
        assertEquals(2, result.malwareScanAttempt());
        assertEquals("CLEAN", result.malwareScanStatus());
    }

    @Test
    void getAdminExtractionStatus_shouldReturnNullMalwareStatusWhenNoScanExists() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .documentType(
                                KycDocument.DocumentType.AADHAAR
                        )
                        .build();

        KycExtractedData extractedData =
                KycExtractedData.builder()
                        .kycDocument(document)
                        .extractionStatus(
                                KycExtractedData.ExtractionStatus.PENDING
                        )
                        .build();

        when(kycExtractedDataRepository
                .findByKycDocumentId(10L))
                .thenReturn(Optional.of(extractedData));

        when(kycMalwareScanRepository
                .findFirstByKycDocumentIdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.empty());

        AdminKycExtractionResponse result =
                kycService.getAdminExtractionStatus(10L);

        assertEquals("PENDING", result.extractionStatus());
        assertNull(result.malwareScanAttempt());
        assertNull(result.malwareScanStatus());
    }

    // ============================================================
    // retryExtraction()
    // ============================================================

    @Test
    void retryExtraction_shouldResetFailedExtractionAndPublishEvent() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .build();

        KycMalwareScan scan =
                KycMalwareScan.builder()
                        .kycDocument(document)
                        .status(
                                KycMalwareScan.MalwareStatus.CLEAN
                        )
                        .attemptNumber(1)
                        .build();

        KycExtractedData extractedData =
                KycExtractedData.builder()
                        .kycDocument(document)
                        .extractionStatus(
                                KycExtractedData.ExtractionStatus.FAILED
                        )
                        .failureReason("OCR failed")
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycMalwareScanRepository
                .findFirstByKycDocumentIdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(scan));

        when(kycExtractedDataRepository
                .findByKycDocumentId(10L))
                .thenReturn(Optional.of(extractedData));

        kycService.retryExtraction(10L);

        assertEquals(
                KycExtractedData.ExtractionStatus.PENDING,
                extractedData.getExtractionStatus()
        );

        assertNull(extractedData.getFailureReason());
        assertNotNull(extractedData.getUpdatedAt());

        verify(kycExtractedDataRepository)
                .save(extractedData);

        verify(kycExtractionEventPublisher)
                .publish(10L);
    }

    @Test
    void retryExtraction_shouldThrowWhenMalwareScanIsNotClean() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .build();

        KycMalwareScan scan =
                KycMalwareScan.builder()
                        .status(
                                KycMalwareScan.MalwareStatus.FAILED
                        )
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycMalwareScanRepository
                .findFirstByKycDocumentIdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(scan));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> kycService.retryExtraction(10L)
                );

        assertEquals(
                "Extraction cannot be retried because malware scan has not passed",
                exception.getMessage()
        );

        verifyNoInteractions(kycExtractedDataRepository);
        verifyNoInteractions(kycExtractionEventPublisher);
    }

    @Test
    void retryExtraction_shouldThrowWhenExtractionIsNotFailed() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .build();

        KycMalwareScan scan =
                KycMalwareScan.builder()
                        .status(
                                KycMalwareScan.MalwareStatus.CLEAN
                        )
                        .build();

        KycExtractedData extractedData =
                KycExtractedData.builder()
                        .extractionStatus(
                                KycExtractedData.ExtractionStatus.SUCCESS
                        )
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycMalwareScanRepository
                .findFirstByKycDocumentIdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(scan));

        when(kycExtractedDataRepository
                .findByKycDocumentId(10L))
                .thenReturn(Optional.of(extractedData));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> kycService.retryExtraction(10L)
                );

        assertEquals(
                "Only failed extractions can be retried",
                exception.getMessage()
        );

        verify(kycExtractedDataRepository, never())
                .save(any());

        verifyNoInteractions(kycExtractionEventPublisher);
    }

    // ============================================================
    // retryMalwareScan()
    // ============================================================

    @Test
    void retryMalwareScan_shouldCreateNewScanAttempt() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .s3ObjectKey("user-1/document.pdf")
                        .build();

        KycMalwareScan latestScan =
                KycMalwareScan.builder()
                        .kycDocument(document)
                        .status(
                                KycMalwareScan.MalwareStatus.FAILED
                        )
                        .attemptNumber(1)
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycMalwareScanRepository
                .findFirstByKycDocumentIdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(latestScan));

        kycService.retryMalwareScan(10L);

        ArgumentCaptor<KycMalwareScan> captor =
                ArgumentCaptor.forClass(
                        KycMalwareScan.class
                );

        verify(kycMalwareScanRepository)
                .save(captor.capture());

        KycMalwareScan retryScan =
                captor.getValue();

        assertSame(document, retryScan.getKycDocument());

        assertEquals(
                KycMalwareScan.MalwareStatus.SCANNING,
                retryScan.getStatus()
        );

        assertEquals(
                "GUARDDUTY",
                retryScan.getProvider()
        );

        assertEquals(
                2,
                retryScan.getAttemptNumber()
        );

        assertNotNull(
                retryScan.getScanStartedAt()
        );

        verify(guardDutyMalwareScanService)
                .scanObject("user-1/document.pdf");
    }

    @Test
    void retryMalwareScan_shouldThrowWhenLatestScanIsNotFailed() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .build();

        KycMalwareScan scan =
                KycMalwareScan.builder()
                        .status(
                                KycMalwareScan.MalwareStatus.CLEAN
                        )
                        .attemptNumber(1)
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycMalwareScanRepository
                .findFirstByKycDocumentIdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(scan));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> kycService.retryMalwareScan(10L)
                );

        assertEquals(
                "Malware scan can only be retried after a failed scan",
                exception.getMessage()
        );

        verify(kycMalwareScanRepository, never())
                .save(any());

        verifyNoInteractions(guardDutyMalwareScanService);
    }

    @Test
    void retryMalwareScan_shouldThrowWhenMaximumAttemptsReached() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.PENDING
                        )
                        .build();

        KycMalwareScan scan =
                KycMalwareScan.builder()
                        .status(
                                KycMalwareScan.MalwareStatus.FAILED
                        )
                        .attemptNumber(3)
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        when(kycMalwareScanRepository
                .findFirstByKycDocumentIdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(scan));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> kycService.retryMalwareScan(10L)
                );

        assertEquals(
                "Maximum malware scan attempts reached",
                exception.getMessage()
        );

        verify(kycMalwareScanRepository, never())
                .save(any());

        verifyNoInteractions(guardDutyMalwareScanService);
    }

    @Test
    void retryMalwareScan_shouldThrowWhenDocumentIsNotPending() {

        KycDocument document =
                KycDocument.builder()
                        .id(10L)
                        .kycVerificationStatus(
                                KycDocument.KycVerificationStatus.VERIFIED
                        )
                        .build();

        when(kycDocumentRepository.findById(10L))
                .thenReturn(Optional.of(document));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> kycService.retryMalwareScan(10L)
                );

        assertEquals(
                "Malware scan can only be retried for pending KYC documents",
                exception.getMessage()
        );

        verifyNoInteractions(kycMalwareScanRepository);
        verifyNoInteractions(guardDutyMalwareScanService);
    }
}