package com.bankflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "kyc_documents",
        schema = "retail_banking"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, unique = true)
    private String storedFileName;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_verification_status", nullable = false)
    private KycVerificationStatus kycVerificationStatus;

    @Column(
            name = "uploaded_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime uploadedAt;

    @Column(length = 500)
    private String rejectionReason;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();

        if (kycVerificationStatus == null) {
            kycVerificationStatus = KycVerificationStatus.PENDING;
        }
    }

    public enum DocumentType {
        PAN,
        AADHAAR
    }

    public enum KycVerificationStatus {
        PENDING,
        VERIFIED,
        REJECTED
    }
}