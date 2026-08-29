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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycExtractionProcessorServiceTest {

    @Mock
    private TextractClient textractClient;

    @Mock
    private KycExtractedDataRepository extractedDataRepository;

    @Mock
    private KycDocumentRepository kycDocumentRepository;

    @Mock
    private PanTextExtractorService panTextExtractorService;

    @Mock
    private KycPanDataRepository kycPanDataRepository;

    @Mock
    private AadhaarTextExtractorService aadhaarTextExtractorService;

    @Mock
    private KycAadhaarDataRepository kycAadhaarDataRepository;

    @Mock
    private PanExtractionValidator panExtractionValidator;

    @Mock
    private AadhaarExtractionValidator aadhaarExtractionValidator;

    private KycExtractionProcessorService service;

    @BeforeEach
    void setUp() {
        service = new KycExtractionProcessorService(
                textractClient,
                extractedDataRepository,
                kycDocumentRepository,
                panTextExtractorService,
                kycPanDataRepository,
                aadhaarTextExtractorService,
                kycAadhaarDataRepository,
                panExtractionValidator,
                aadhaarExtractionValidator
        );
    }

    @Test
    void process_shouldThrowExceptionWhenDocumentDoesNotExist() {

        Long documentId = 10L;

        when(kycDocumentRepository.findById(documentId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.process(documentId)
        );

        verify(kycDocumentRepository).findById(documentId);

        verifyNoInteractions(
                textractClient,
                extractedDataRepository,
                panTextExtractorService,
                kycPanDataRepository,
                aadhaarTextExtractorService,
                kycAadhaarDataRepository,
                panExtractionValidator,
                aadhaarExtractionValidator
        );
    }

    @Test
    void process_shouldCreateExtractionDataWhenItDoesNotExist() {

        Long documentId = 10L;

        KycDocument document = createDocument(
                documentId,
                KycDocument.DocumentType.PAN
        );

        when(kycDocumentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(extractedDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.empty());

        DetectDocumentTextResponse response =
                createTextractResponse("PAN", "JOHN DOE");

        when(textractClient.detectDocumentText(
                any(DetectDocumentTextRequest.class)
        )).thenReturn(response);

        PanExtractedData panData = new PanExtractedData(
                "ABCDE1234F",
                "JOHN DOE",
                "ROBERT DOE",
                LocalDate.of(1990, 1, 1)
        );

        when(panTextExtractorService.extract(anyString()))
                .thenReturn(panData);

        when(kycPanDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.empty());

        service.process(documentId);

        ArgumentCaptor<KycExtractedData> captor =
                ArgumentCaptor.forClass(KycExtractedData.class);

        verify(extractedDataRepository, atLeastOnce())
                .save(captor.capture());

        KycExtractedData savedData = captor.getAllValues().getLast();

        assertEquals(
                KycExtractedData.ExtractionStatus.SUCCESS,
                savedData.getExtractionStatus()
        );

        assertEquals(
                "PAN\nJOHN DOE",
                savedData.getExtractedText()
        );

        assertNotNull(savedData.getCreatedAt());
        assertNotNull(savedData.getUpdatedAt());

        verify(panTextExtractorService)
                .extract("PAN\nJOHN DOE");

        verify(panExtractionValidator)
                .validate(panData);

        verify(kycPanDataRepository)
                .save(any(KycPanData.class));
    }

    @Test
    void process_shouldReuseExistingExtractionData() {

        Long documentId = 10L;

        KycDocument document = createDocument(
                documentId,
                KycDocument.DocumentType.PAN
        );

        KycExtractedData existingData =
                KycExtractedData.builder()
                        .id(100L)
                        .kycDocument(document)
                        .extractionStatus(
                                KycExtractedData.ExtractionStatus.FAILED
                        )
                        .createdAt(LocalDateTime.now())
                        .failureReason("Previous failure")
                        .build();

        when(kycDocumentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(extractedDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.of(existingData));

        DetectDocumentTextResponse response =
                createTextractResponse("PAN", "JOHN DOE");

        when(textractClient.detectDocumentText(
                any(DetectDocumentTextRequest.class)
        )).thenReturn(response);

        PanExtractedData panData = new PanExtractedData(
                "ABCDE1234F",
                "JOHN DOE",
                "ROBERT DOE",
                LocalDate.of(1990, 1, 1)
        );

        when(panTextExtractorService.extract(anyString()))
                .thenReturn(panData);

        when(kycPanDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.empty());

        service.process(documentId);

        assertEquals(
                KycExtractedData.ExtractionStatus.SUCCESS,
                existingData.getExtractionStatus()
        );

        assertEquals(
                "PAN\nJOHN DOE",
                existingData.getExtractedText()
        );

        assertNull(existingData.getFailureReason());

        verify(extractedDataRepository, atLeastOnce())
                .save(existingData);
    }

    @Test
    void process_shouldSuccessfullyProcessPanDocument() {

        Long documentId = 20L;

        KycDocument document = createDocument(
                documentId,
                KycDocument.DocumentType.PAN
        );

        KycExtractedData extractedData =
                existingExtractionData(document);

        when(kycDocumentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(extractedDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.of(extractedData));

        when(textractClient.detectDocumentText(
                any(DetectDocumentTextRequest.class)
        )).thenReturn(
                createTextractResponse(
                        "Permanent Account Number",
                        "ABCDE1234F",
                        "JOHN DOE"
                )
        );

        PanExtractedData panData = new PanExtractedData(
                "ABCDE1234F",
                "JOHN DOE",
                "ROBERT DOE",
                LocalDate.of(1990, 1, 1)
        );

        when(panTextExtractorService.extract(anyString()))
                .thenReturn(panData);

        KycPanData panEntity =
                KycPanData.builder()
                        .kycDocument(document)
                        .createdAt(LocalDateTime.now())
                        .build();

        when(kycPanDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.of(panEntity));

        service.process(documentId);

        verify(panTextExtractorService)
                .extract(
                        "Permanent Account Number\n"
                                + "ABCDE1234F\n"
                                + "JOHN DOE"
                );

        verify(panExtractionValidator)
                .validate(panData);

        verify(kycPanDataRepository)
                .save(panEntity);

        assertEquals(
                "ABCDE1234F",
                panEntity.getPanNumber()
        );

        assertEquals(
                "JOHN DOE",
                panEntity.getFullName()
        );

        assertEquals(
                "ROBERT DOE",
                panEntity.getFatherName()
        );

        assertEquals(
                LocalDate.of(1990, 1, 1),
                panEntity.getDateOfBirth()
        );

        assertEquals(
                KycExtractedData.ExtractionStatus.SUCCESS,
                extractedData.getExtractionStatus()
        );
    }

    @Test
    void process_shouldCreatePanEntityWhenItDoesNotExist() {

        Long documentId = 21L;

        KycDocument document = createDocument(
                documentId,
                KycDocument.DocumentType.PAN
        );

        KycExtractedData extractedData =
                existingExtractionData(document);

        when(kycDocumentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(extractedDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.of(extractedData));

        when(textractClient.detectDocumentText(
                any(DetectDocumentTextRequest.class)
        )).thenReturn(
                createTextractResponse("ABCDE1234F", "JOHN DOE")
        );

        PanExtractedData panData = new PanExtractedData(
                "ABCDE1234F",
                "JOHN DOE",
                "ROBERT DOE",
                LocalDate.of(1990, 1, 1)
        );

        when(panTextExtractorService.extract(anyString()))
                .thenReturn(panData);

        when(kycPanDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.empty());

        service.process(documentId);

        ArgumentCaptor<KycPanData> captor =
                ArgumentCaptor.forClass(KycPanData.class);

        verify(kycPanDataRepository)
                .save(captor.capture());

        KycPanData savedPanData = captor.getValue();

        assertSame(document, savedPanData.getKycDocument());

        assertEquals(
                "ABCDE1234F",
                savedPanData.getPanNumber()
        );

        assertEquals(
                "JOHN DOE",
                savedPanData.getFullName()
        );

        assertEquals(
                "ROBERT DOE",
                savedPanData.getFatherName()
        );

        assertEquals(
                LocalDate.of(1990, 1, 1),
                savedPanData.getDateOfBirth()
        );

        assertNotNull(savedPanData.getCreatedAt());
        assertNotNull(savedPanData.getUpdatedAt());
    }

    @Test
    void process_shouldSuccessfullyProcessAadhaarDocument() {

        Long documentId = 30L;

        KycDocument document = createDocument(
                documentId,
                KycDocument.DocumentType.AADHAAR
        );

        KycExtractedData extractedData =
                existingExtractionData(document);

        when(kycDocumentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(extractedDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.of(extractedData));

        when(textractClient.detectDocumentText(
                any(DetectDocumentTextRequest.class)
        )).thenReturn(
                createTextractResponse(
                        "Government of India",
                        "1234 5678 9012",
                        "JOHN DOE",
                        "01/01/1990",
                        "MALE",
                        "Mumbai Maharashtra",
                        "9876543210"
                )
        );

        AadhaarExtractedData aadhaarData =
                new AadhaarExtractedData(
                        "123456789012",
                        "JOHN DOE",
                        LocalDate.of(1990, 1, 1),
                        "MALE",
                        "Mumbai Maharashtra",
                        "9876543210"
                );

        when(aadhaarTextExtractorService.extract(anyString()))
                .thenReturn(aadhaarData);

        KycAadhaarData aadhaarEntity =
                KycAadhaarData.builder()
                        .kycDocument(document)
                        .createdAt(LocalDateTime.now())
                        .build();

        when(kycAadhaarDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.of(aadhaarEntity));

        service.process(documentId);

        verify(aadhaarTextExtractorService)
                .extract(
                        "Government of India\n"
                                + "1234 5678 9012\n"
                                + "JOHN DOE\n"
                                + "01/01/1990\n"
                                + "MALE\n"
                                + "Mumbai Maharashtra\n"
                                + "9876543210"
                );

        verify(aadhaarExtractionValidator)
                .validate(aadhaarData);

        verify(kycAadhaarDataRepository)
                .save(aadhaarEntity);

        assertEquals(
                "123456789012",
                aadhaarEntity.getAadhaarNumber()
        );

        assertEquals(
                "JOHN DOE",
                aadhaarEntity.getFullName()
        );

        assertEquals(
                LocalDate.of(1990, 1, 1),
                aadhaarEntity.getDateOfBirth()
        );

        assertEquals(
                "MALE",
                aadhaarEntity.getGender()
        );

        assertEquals(
                "Mumbai Maharashtra",
                aadhaarEntity.getAddress()
        );

        assertEquals(
                "9876543210",
                aadhaarEntity.getMobileNumber()
        );

        assertEquals(
                KycExtractedData.ExtractionStatus.SUCCESS,
                extractedData.getExtractionStatus()
        );
    }

    @Test
    void process_shouldCreateAadhaarEntityWhenItDoesNotExist() {

        Long documentId = 31L;

        KycDocument document = createDocument(
                documentId,
                KycDocument.DocumentType.AADHAAR
        );

        KycExtractedData extractedData =
                existingExtractionData(document);

        when(kycDocumentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(extractedDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.of(extractedData));

        when(textractClient.detectDocumentText(
                any(DetectDocumentTextRequest.class)
        )).thenReturn(
                createTextractResponse(
                        "1234 5678 9012",
                        "JOHN DOE"
                )
        );

        AadhaarExtractedData aadhaarData =
                new AadhaarExtractedData(
                        "123456789012",
                        "JOHN DOE",
                        LocalDate.of(1990, 1, 1),
                        "MALE",
                        "Mumbai",
                        "9876543210"
                );

        when(aadhaarTextExtractorService.extract(anyString()))
                .thenReturn(aadhaarData);

        when(kycAadhaarDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.empty());

        service.process(documentId);

        ArgumentCaptor<KycAadhaarData> captor =
                ArgumentCaptor.forClass(KycAadhaarData.class);

        verify(kycAadhaarDataRepository)
                .save(captor.capture());

        KycAadhaarData savedData = captor.getValue();

        assertSame(document, savedData.getKycDocument());

        assertEquals(
                "123456789012",
                savedData.getAadhaarNumber()
        );

        assertEquals(
                "JOHN DOE",
                savedData.getFullName()
        );

        assertEquals(
                LocalDate.of(1990, 1, 1),
                savedData.getDateOfBirth()
        );

        assertEquals(
                "MALE",
                savedData.getGender()
        );

        assertEquals(
                "Mumbai",
                savedData.getAddress()
        );

        assertEquals(
                "9876543210",
                savedData.getMobileNumber()
        );

        assertNotNull(savedData.getCreatedAt());
        assertNotNull(savedData.getUpdatedAt());
    }

    @Test
    void process_shouldMarkExtractionAsFailedWhenTextractFails() {

        Long documentId = 40L;

        KycDocument document = createDocument(
                documentId,
                KycDocument.DocumentType.PAN
        );

        KycExtractedData extractedData =
                existingExtractionData(document);

        when(kycDocumentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(extractedDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.of(extractedData));

        when(textractClient.detectDocumentText(
                any(DetectDocumentTextRequest.class)
        )).thenThrow(
                new RuntimeException("Textract unavailable")
        );

        assertDoesNotThrow(
                () -> service.process(documentId)
        );

        assertEquals(
                KycExtractedData.ExtractionStatus.FAILED,
                extractedData.getExtractionStatus()
        );

        assertEquals(
                "RuntimeException: Textract unavailable",
                extractedData.getFailureReason()
        );

        assertNotNull(extractedData.getUpdatedAt());

        verify(extractedDataRepository, atLeastOnce())
                .save(extractedData);

        verifyNoInteractions(
                panTextExtractorService,
                panExtractionValidator,
                kycPanDataRepository,
                aadhaarTextExtractorService,
                aadhaarExtractionValidator,
                kycAadhaarDataRepository
        );
    }

    @Test
    void process_shouldMarkExtractionAsFailedWhenPanExtractionFails() {

        Long documentId = 41L;

        KycDocument document = createDocument(
                documentId,
                KycDocument.DocumentType.PAN
        );

        KycExtractedData extractedData =
                existingExtractionData(document);

        when(kycDocumentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(extractedDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.of(extractedData));

        when(textractClient.detectDocumentText(
                any(DetectDocumentTextRequest.class)
        )).thenReturn(
                createTextractResponse("Invalid PAN data")
        );

        when(panTextExtractorService.extract(anyString()))
                .thenThrow(
                        new IllegalArgumentException(
                                "PAN extraction failed"
                        )
                );

        assertDoesNotThrow(
                () -> service.process(documentId)
        );

        assertEquals(
                KycExtractedData.ExtractionStatus.FAILED,
                extractedData.getExtractionStatus()
        );

        assertEquals(
                "IllegalArgumentException: PAN extraction failed",
                extractedData.getFailureReason()
        );

        verify(panTextExtractorService)
                .extract("Invalid PAN data");

        verifyNoInteractions(
                panExtractionValidator,
                kycPanDataRepository
        );
    }

    @Test
    void process_shouldMarkExtractionAsFailedWhenPanValidationFails() {

        Long documentId = 42L;

        KycDocument document = createDocument(
                documentId,
                KycDocument.DocumentType.PAN
        );

        KycExtractedData extractedData =
                existingExtractionData(document);

        PanExtractedData panData = new PanExtractedData(
                "INVALID",
                "JOHN DOE",
                "ROBERT DOE",
                LocalDate.of(1990, 1, 1)
        );

        when(kycDocumentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(extractedDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.of(extractedData));

        when(textractClient.detectDocumentText(
                any(DetectDocumentTextRequest.class)
        )).thenReturn(
                createTextractResponse("Invalid PAN")
        );

        when(panTextExtractorService.extract(anyString()))
                .thenReturn(panData);

        doThrow(
                new IllegalArgumentException("Invalid PAN number")
        ).when(panExtractionValidator).validate(panData);

        assertDoesNotThrow(
                () -> service.process(documentId)
        );

        assertEquals(
                KycExtractedData.ExtractionStatus.FAILED,
                extractedData.getExtractionStatus()
        );

        assertEquals(
                "IllegalArgumentException: Invalid PAN number",
                extractedData.getFailureReason()
        );

        verify(kycPanDataRepository, never())
                .save(any());
    }

    @Test
    void process_shouldMarkExtractionAsFailedWhenAadhaarExtractionFails() {

        Long documentId = 43L;

        KycDocument document = createDocument(
                documentId,
                KycDocument.DocumentType.AADHAAR
        );

        KycExtractedData extractedData =
                existingExtractionData(document);

        when(kycDocumentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(extractedDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.of(extractedData));

        when(textractClient.detectDocumentText(
                any(DetectDocumentTextRequest.class)
        )).thenReturn(
                createTextractResponse("Invalid Aadhaar")
        );

        when(aadhaarTextExtractorService.extract(anyString()))
                .thenThrow(
                        new IllegalArgumentException(
                                "Aadhaar extraction failed"
                        )
                );

        assertDoesNotThrow(
                () -> service.process(documentId)
        );

        assertEquals(
                KycExtractedData.ExtractionStatus.FAILED,
                extractedData.getExtractionStatus()
        );

        assertEquals(
                "IllegalArgumentException: Aadhaar extraction failed",
                extractedData.getFailureReason()
        );

        verify(kycAadhaarDataRepository, never())
                .save(any());
    }

    @Test
    void process_shouldMarkExtractionAsFailedWhenAadhaarValidationFails() {

        Long documentId = 44L;

        KycDocument document = createDocument(
                documentId,
                KycDocument.DocumentType.AADHAAR
        );

        KycExtractedData extractedData =
                existingExtractionData(document);

        AadhaarExtractedData aadhaarData =
                new AadhaarExtractedData(
                        "123456789012",
                        "JOHN DOE",
                        LocalDate.of(1990, 1, 1),
                        "MALE",
                        "Mumbai",
                        "9876543210"
                );

        when(kycDocumentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(extractedDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.of(extractedData));

        when(textractClient.detectDocumentText(
                any(DetectDocumentTextRequest.class)
        )).thenReturn(
                createTextractResponse("Aadhaar data")
        );

        when(aadhaarTextExtractorService.extract(anyString()))
                .thenReturn(aadhaarData);

        doThrow(
                new IllegalArgumentException(
                        "Invalid Aadhaar number"
                )
        ).when(aadhaarExtractionValidator).validate(aadhaarData);

        assertDoesNotThrow(
                () -> service.process(documentId)
        );

        assertEquals(
                KycExtractedData.ExtractionStatus.FAILED,
                extractedData.getExtractionStatus()
        );

        assertEquals(
                "IllegalArgumentException: Invalid Aadhaar number",
                extractedData.getFailureReason()
        );

        verify(kycAadhaarDataRepository, never())
                .save(any());
    }

    @Test
    void process_shouldBuildCorrectTextractRequest() {

        Long documentId = 50L;

        KycDocument document = createDocument(
                documentId,
                KycDocument.DocumentType.PAN
        );

        KycExtractedData extractedData =
                existingExtractionData(document);

        when(kycDocumentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(extractedDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.of(extractedData));

        when(textractClient.detectDocumentText(
                any(DetectDocumentTextRequest.class)
        )).thenReturn(
                createTextractResponse("PAN data")
        );

        PanExtractedData panData =
                new PanExtractedData(
                        "ABCDE1234F",
                        "JOHN DOE",
                        "ROBERT DOE",
                        LocalDate.of(1990, 1, 1)
                );

        when(panTextExtractorService.extract(anyString()))
                .thenReturn(panData);

        ArgumentCaptor<DetectDocumentTextRequest> captor =
                ArgumentCaptor.forClass(
                        DetectDocumentTextRequest.class
                );

        service.process(documentId);

        verify(textractClient)
                .detectDocumentText(captor.capture());

        DetectDocumentTextRequest request =
                captor.getValue();

        assertNotNull(request.document());
        assertNotNull(request.document().s3Object());

        assertEquals(
                "bankflow-test-bucket",
                request.document().s3Object().bucket()
        );

        assertEquals(
                "user-10/test.pdf",
                request.document().s3Object().name()
        );
    }

    @Test
    void process_shouldJoinOnlyLineBlocksIntoExtractedText() {

        Long documentId = 60L;

        KycDocument document = createDocument(
                documentId,
                KycDocument.DocumentType.PAN
        );

        KycExtractedData extractedData =
                existingExtractionData(document);

        when(kycDocumentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(extractedDataRepository.findByKycDocumentId(documentId))
                .thenReturn(Optional.of(extractedData));

        DetectDocumentTextResponse response =
                DetectDocumentTextResponse.builder()
                        .blocks(
                                Block.builder()
                                        .blockType(BlockType.LINE)
                                        .text("Line One")
                                        .build(),

                                Block.builder()
                                        .blockType(BlockType.WORD)
                                        .text("Ignored Word")
                                        .build(),

                                Block.builder()
                                        .blockType(BlockType.LINE)
                                        .text("Line Two")
                                        .build()
                        )
                        .build();

        when(textractClient.detectDocumentText(
                any(DetectDocumentTextRequest.class)
        )).thenReturn(response);

        PanExtractedData panData =
                new PanExtractedData(
                        "ABCDE1234F",
                        "JOHN DOE",
                        "ROBERT DOE",
                        LocalDate.of(1990, 1, 1)
                );

        when(panTextExtractorService.extract(anyString()))
                .thenReturn(panData);

        service.process(documentId);

        assertEquals(
                "Line One\nLine Two",
                extractedData.getExtractedText()
        );

        verify(panTextExtractorService)
                .extract("Line One\nLine Two");
    }

    private KycDocument createDocument(
            Long id,
            KycDocument.DocumentType documentType
    ) {
        return KycDocument.builder()
                .id(id)
                .documentType(documentType)
                .s3Bucket("bankflow-test-bucket")
                .s3ObjectKey("user-10/test.pdf")
                .build();
    }

    private KycExtractedData existingExtractionData(
            KycDocument document
    ) {
        return KycExtractedData.builder()
                .id(100L)
                .kycDocument(document)
                .extractionStatus(
                        KycExtractedData.ExtractionStatus.PENDING
                )
                .createdAt(LocalDateTime.now())
                .build();
    }

    private DetectDocumentTextResponse createTextractResponse(
            String... lines
    ) {
        List<Block> blocks = java.util.Arrays.stream(lines)
                .map(text ->
                        Block.builder()
                                .blockType(BlockType.LINE)
                                .text(text)
                                .build()
                )
                .toList();

        return DetectDocumentTextResponse.builder()
                .blocks(blocks)
                .build();
    }
}