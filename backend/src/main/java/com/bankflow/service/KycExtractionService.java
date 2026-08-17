package com.bankflow.service;

import com.bankflow.entity.KycDocument;
import com.bankflow.entity.KycExtractedData;
import com.bankflow.repository.KycDocumentRepository;
import com.bankflow.repository.KycExtractedDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycExtractionService {

    private final TextractClient textractClient;
    private final KycExtractedDataRepository extractedDataRepository;
    private final KycDocumentRepository kycDocumentRepository;

    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void extractAsync(
            KycExtractionEventPublisher.KycExtractionEvent event
    ) {

        KycDocument document =
                kycDocumentRepository.findById(
                                event.documentId()
                        )
                        .orElseThrow();

        KycExtractedData extractedData =
                KycExtractedData.builder()
                        .kycDocument(document)
                        .extractionStatus(
                                KycExtractedData.ExtractionStatus.PROCESSING
                        )
                        .createdAt(LocalDateTime.now())
                        .build();

        extractedDataRepository.save(extractedData);


        try {

            DetectDocumentTextRequest request =
                    DetectDocumentTextRequest.builder()
                            .document(Document.builder()
                                    .s3Object(S3Object.builder()
                                            .bucket(document.getS3Bucket())
                                            .name(document.getS3ObjectKey())
                                            .build())
                                    .build())
                            .build();


            DetectDocumentTextResponse response =
                    textractClient.detectDocumentText(request);


            String extractedText =
                    response.blocks()
                            .stream()
                            .filter(block ->
                                    block.blockType()
                                            == BlockType.LINE
                            )
                            .map(Block::text)
                            .collect(Collectors.joining("\n"));


            extractedData.setExtractedText(extractedText);
            extractedData.setExtractionStatus(
                    KycExtractedData.ExtractionStatus.SUCCESS
            );
            extractedData.setUpdatedAt(LocalDateTime.now());

            log.info(
                    "OCR extraction successful. Document id: {}, Characters extracted: {}",
                    document.getId(),
                    extractedText.length()
            );
        } catch (Exception e) {

            log.error(
                    "OCR extraction failed for document id {}",
                    document.getId(),
                    e
            );

            extractedData.setExtractionStatus(
                    KycExtractedData.ExtractionStatus.FAILED
            );

            extractedData.setFailureReason(
                    e.getMessage()
            );

            extractedData.setUpdatedAt(LocalDateTime.now());
        }


        extractedDataRepository.save(extractedData);
    }
}