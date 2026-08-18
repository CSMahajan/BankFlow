package com.bankflow.service;

import com.bankflow.dto.AadhaarExtractedData;
import com.bankflow.dto.PanExtractedData;
import com.bankflow.entity.KycAadhaarData;
import com.bankflow.entity.KycDocument;
import com.bankflow.entity.KycExtractedData;
import com.bankflow.entity.KycPanData;
import com.bankflow.exception.ResourceNotFoundException;
import com.bankflow.repository.KycAadhaarDataRepository;
import com.bankflow.repository.KycDocumentRepository;
import com.bankflow.repository.KycExtractedDataRepository;
import com.bankflow.repository.KycPanDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycExtractionProcessorService {

    private final TextractClient textractClient;
    private final KycExtractedDataRepository extractedDataRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final PanTextExtractorService panTextExtractorService;
    private final KycPanDataRepository kycPanDataRepository;
    private final AadhaarTextExtractorService aadhaarTextExtractorService;
    private final KycAadhaarDataRepository kycAadhaarDataRepository;
    private final PanExtractionValidator panExtractionValidator;
    private final AadhaarExtractionValidator aadhaarExtractionValidator;


    @Transactional(noRollbackFor = Exception.class)
    public void process(Long documentId) {

        KycDocument document =
                kycDocumentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "KYC document not found"
                                )
                        );

        KycExtractedData extractedData =
                extractedDataRepository
                        .findByKycDocumentId(documentId)
                        .orElseGet(() ->
                                KycExtractedData.builder()
                                        .kycDocument(document)
                                        .createdAt(LocalDateTime.now())
                                        .build()
                        );


        extractedData.setExtractionStatus(KycExtractedData.ExtractionStatus.PROCESSING);

        extractedData.setUpdatedAt(LocalDateTime.now());

        extractedDataRepository.save(extractedData);


        try {

            DetectDocumentTextRequest request = DetectDocumentTextRequest.builder()
                    .document(Document.builder()
                            .s3Object(S3Object.builder()
                                    .bucket(document.getS3Bucket())
                                    .name(document.getS3ObjectKey()).build())
                            .build()).build();


            DetectDocumentTextResponse response = textractClient.detectDocumentText(request);

            String extractedText = response.blocks().stream()
                    .filter(block -> block.blockType() == BlockType.LINE)
                    .map(Block::text).collect(Collectors.joining("\n"));

            if (document.getDocumentType() == KycDocument.DocumentType.PAN) {

                savePanData(document, extractedText);

            } else if (document.getDocumentType() == KycDocument.DocumentType.AADHAAR) {

                saveAadhaarData(document, extractedText);
            }
            extractedData.setExtractedText(extractedText);
            extractedData.setExtractionStatus(KycExtractedData.ExtractionStatus.SUCCESS);
            extractedData.setUpdatedAt(LocalDateTime.now());

            log.info("OCR extraction successful. Document id: {}, Characters extracted: {}",
                    document.getId(), extractedText.length());
        } catch (Exception e) {
            log.error("OCR extraction failed for document id {}", document.getId(), e);
            extractedData.setExtractionStatus(KycExtractedData.ExtractionStatus.FAILED);
            extractedData.setFailureReason(
                    e.getClass().getSimpleName()
                            + ": "
                            + (
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unknown extraction failure"
                    )
            );
            extractedData.setUpdatedAt(LocalDateTime.now());
        }
        extractedDataRepository.save(extractedData);
    }


    private void savePanData(KycDocument document, String extractedText) {

        PanExtractedData panData = panTextExtractorService.extract(extractedText);

        panExtractionValidator.validate(panData);

        KycPanData panEntity =
                kycPanDataRepository
                        .findByKycDocumentId(document.getId())
                        .orElse(
                                KycPanData.builder()
                                        .kycDocument(document)
                                        .createdAt(LocalDateTime.now())
                                        .build()
                        );


        panEntity.setPanNumber(panData.panNumber());
        panEntity.setFullName(panData.fullName());
        panEntity.setFatherName(panData.fatherName());
        panEntity.setDateOfBirth(panData.dateOfBirth());
        panEntity.setUpdatedAt(LocalDateTime.now());


        kycPanDataRepository.save(panEntity);


        log.info("PAN extraction completed for document id {}", document.getId());
    }

    private void saveAadhaarData(KycDocument document, String extractedText) {

        AadhaarExtractedData aadhaarData = aadhaarTextExtractorService.extract(extractedText);

        aadhaarExtractionValidator.validate(aadhaarData);

        KycAadhaarData aadhaarEntity =
                kycAadhaarDataRepository
                        .findByKycDocumentId(document.getId())
                        .orElse(
                                KycAadhaarData.builder()
                                        .kycDocument(document)
                                        .createdAt(LocalDateTime.now())
                                        .build()
                        );


        aadhaarEntity.setAadhaarNumber(aadhaarData.aadhaarNumber());
        aadhaarEntity.setFullName(aadhaarData.fullName());
        aadhaarEntity.setDateOfBirth(aadhaarData.dateOfBirth());
        aadhaarEntity.setGender(aadhaarData.gender());
        aadhaarEntity.setAddress(aadhaarData.address());
        aadhaarEntity.setMobileNumber(aadhaarData.mobileNumber());
        aadhaarEntity.setUpdatedAt(LocalDateTime.now());


        kycAadhaarDataRepository.save(aadhaarEntity);


        log.info("Aadhaar extraction completed for document id {}", document.getId());
    }
}
