package com.bankflow.service;

import com.bankflow.dto.AadhaarExtractedData;
import com.bankflow.dto.PanExtractedData;
import com.bankflow.entity.KycAadhaarData;
import com.bankflow.entity.KycDocument;
import com.bankflow.entity.KycExtractedData;
import com.bankflow.entity.KycPanData;
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

        KycDocument document = kycDocumentRepository.findById(documentId).orElseThrow();

        KycExtractedData extractedData = KycExtractedData
                .builder()
                .kycDocument(document)
                .extractionStatus(KycExtractedData.ExtractionStatus.PROCESSING)
                .createdAt(LocalDateTime.now())
                .build();

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
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Unknown extraction failure"
            );
            extractedData.setUpdatedAt(LocalDateTime.now());
            extractedDataRepository.save(extractedData);
        }
    }


    private void savePanData(KycDocument document, String extractedText) {

        PanExtractedData panData = panTextExtractorService.extract(extractedText);

        panExtractionValidator.validate(panData);

        KycPanData panEntity = KycPanData.builder()
                .kycDocument(document)
                .panNumber(panData.panNumber())
                .fullName(panData.fullName())
                .fatherName(panData.fatherName())
                .dateOfBirth(panData.dateOfBirth())
                .createdAt(LocalDateTime.now()).build();


        kycPanDataRepository.save(panEntity);


        log.info("PAN extraction completed for document id {} : {}", document.getId(), panData);
    }

    private void saveAadhaarData(KycDocument document, String extractedText) {

        AadhaarExtractedData aadhaarData = aadhaarTextExtractorService.extract(extractedText);

        aadhaarExtractionValidator.validate(aadhaarData);

        KycAadhaarData aadhaarEntity = KycAadhaarData.builder()
                .kycDocument(document)
                .aadhaarNumber(aadhaarData.aadhaarNumber())
                .fullName(aadhaarData.fullName())
                .dateOfBirth(aadhaarData.dateOfBirth())
                .gender(aadhaarData.gender())
                .address(aadhaarData.address())
                .mobileNumber(aadhaarData.mobileNumber())
                .createdAt(LocalDateTime.now()).build();


        kycAadhaarDataRepository.save(aadhaarEntity);


        log.info("Aadhaar extraction completed for document id {} : {}", document.getId(), aadhaarData);
    }
}
