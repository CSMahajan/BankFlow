package com.bankflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "kyc_extracted_data",
        schema = "retail_banking"
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycExtractedData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "kyc_document_id",
            nullable = false,
            unique = true
    )
    private KycDocument kycDocument;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExtractionStatus extractionStatus;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    public enum ExtractionStatus {
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED
    }
}