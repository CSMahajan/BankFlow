package com.bankflow.controller;

import com.bankflow.config.SecurityConfig;
import com.bankflow.dto.*;
import com.bankflow.entity.KycDocument;
import com.bankflow.exception.GlobalExceptionHandler;
import com.bankflow.filter.JwtAuthenticationFilter;
import com.bankflow.filter.RateLimitFilter;
import com.bankflow.filter.UserRateLimitFilter;
import com.bankflow.service.KycService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(
        controllers = KycController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = RateLimitFilter.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = UserRateLimitFilter.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = SecurityConfig.class
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class KycControllerTest {

    @jakarta.annotation.Resource
    private MockMvc mockMvc;

    @MockitoBean
    private KycService kycService;

    // ---------------------------------------------------------
    // POST /api/v1/kyc/upload
    // ---------------------------------------------------------

    @Test
    void upload_shouldReturnDocumentResponse() throws Exception {

        LocalDateTime uploadedAt = LocalDateTime.of(2026, 9, 2, 10, 30);

        KycDocument document = new KycDocument();
        document.setId(1L);
        document.setDocumentType(KycDocument.DocumentType.PAN);
        document.setOriginalFileName("pan-card.pdf");
        document.setKycVerificationStatus(
                KycDocument.KycVerificationStatus.PENDING
        );
        document.setUploadedAt(uploadedAt);
        document.setRejectionReason(null);

        when(kycService.uploadDocument(
                any(),
                eq(KycDocument.DocumentType.PAN)
        )).thenReturn(document);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "pan-card.pdf",
                "application/pdf",
                "dummy pdf content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/kyc/upload")
                        .file(file)
                        .param("documentType", "PAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.documentType").value("PAN"))
                .andExpect(jsonPath("$.originalFileName").value("pan-card.pdf"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.rejectionReason").doesNotExist());

        verify(kycService).uploadDocument(
                any(),
                eq(KycDocument.DocumentType.PAN)
        );
    }

    @Test
    void upload_shouldReturnRejectedDocumentWithReason() throws Exception {

        KycDocument document = new KycDocument();
        document.setId(2L);
        document.setDocumentType(KycDocument.DocumentType.AADHAAR);
        document.setOriginalFileName("aadhaar.pdf");
        document.setKycVerificationStatus(
                KycDocument.KycVerificationStatus.REJECTED
        );
        document.setUploadedAt(LocalDateTime.of(2026, 9, 2, 11, 0));
        document.setRejectionReason("Document verification failed");

        when(kycService.uploadDocument(
                any(),
                eq(KycDocument.DocumentType.AADHAAR)
        )).thenReturn(document);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "aadhaar.pdf",
                "application/pdf",
                "dummy pdf content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/kyc/upload")
                        .file(file)
                        .param("documentType", "AADHAAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.documentType").value("AADHAAR"))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason")
                        .value("Document verification failed"));

        verify(kycService).uploadDocument(
                any(),
                eq(KycDocument.DocumentType.AADHAAR)
        );
    }

    // ---------------------------------------------------------
    // GET /api/v1/kyc/my-documents
    // ---------------------------------------------------------

    @Test
    void getMyDocuments_shouldReturnDocuments() throws Exception {

        KycDocumentResponse response = new KycDocumentResponse(
                1L,
                "PAN",
                "pan-card.pdf",
                "VERIFIED",
                LocalDateTime.of(2026, 9, 2, 10, 30),
                null
        );

        when(kycService.getMyDocuments())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/kyc/my-documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].documentType").value("PAN"))
                .andExpect(jsonPath("$[0].originalFileName")
                        .value("pan-card.pdf"))
                .andExpect(jsonPath("$[0].status").value("VERIFIED"));

        verify(kycService).getMyDocuments();
    }

    // ---------------------------------------------------------
    // GET /api/v1/kyc/status
    // ---------------------------------------------------------

    @Test
    void getKycStatus_shouldReturnStatus() throws Exception {

        KycStatusResponse response = new KycStatusResponse(
                "VERIFIED",
                new KycStatusResponse.DocumentStatus(
                        true,
                        "VERIFIED",
                        null
                ),
                new KycStatusResponse.DocumentStatus(
                        true,
                        "VERIFIED",
                        null
                )
        );

        when(kycService.getMyKycStatus())
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/kyc/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.pan.uploaded").value(true))
                .andExpect(jsonPath("$.pan.status").value("VERIFIED"))
                .andExpect(jsonPath("$.aadhaar.uploaded").value(true))
                .andExpect(jsonPath("$.aadhaar.status").value("VERIFIED"));

        verify(kycService).getMyKycStatus();
    }

    // ---------------------------------------------------------
    // GET /api/v1/kyc/documents/{documentId}
    // ---------------------------------------------------------

    @Test
    void viewDocument_shouldReturnResourceWithHeaders() throws Exception {

        KycDocument document = new KycDocument();
        document.setId(1L);
        document.setDocumentType(KycDocument.DocumentType.PAN);
        document.setOriginalFileName("pan-card.pdf");
        document.setContentType("application/pdf");

        Resource resource = new ByteArrayResource(
                "pdf content".getBytes()
        );

        when(kycService.getCustomerDocumentDetails(1L))
                .thenReturn(document);

        when(kycService.getCustomerDocumentResource(1L))
                .thenReturn(resource);

        mockMvc.perform(get("/api/v1/kyc/documents/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string(
                        "X-Content-Type-Options",
                        "nosniff"
                ))
                .andExpect(header().string(
                        "Cache-Control",
                        "no-store"
                ))
                .andExpect(header().string(
                        "Content-Disposition",
                        "inline; filename=\"pan-card.pdf\""
                ))
                .andExpect(content().bytes(
                        "pdf content".getBytes()
                ));

        verify(kycService).getCustomerDocumentDetails(1L);
        verify(kycService).getCustomerDocumentResource(1L);
    }

    // ---------------------------------------------------------
    // GET /api/v1/kyc/documents/{documentId}/extraction
    // ---------------------------------------------------------

    @Test
    void getExtraction_shouldReturnExtractionResult() throws Exception {

        KycExtractionResponse response = new KycExtractionResponse(
                1L,
                "COMPLETED",
                "John Doe\nABCDE1234F",
                null,
                LocalDateTime.of(2026, 9, 2, 10, 0),
                LocalDateTime.of(2026, 9, 2, 10, 5)
        );

        when(kycService.getExtractionResult(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/kyc/documents/1/extraction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(1))
                .andExpect(jsonPath("$.extractionStatus")
                        .value("COMPLETED"))
                .andExpect(jsonPath("$.extractedText")
                        .value("John Doe\nABCDE1234F"))
                .andExpect(jsonPath("$.failureReason").doesNotExist());

        verify(kycService).getExtractionResult(1L);
    }

    // ---------------------------------------------------------
    // GET /api/v1/kyc/documents/{documentId}/pan-data
    // ---------------------------------------------------------

    @Test
    void getPanData_shouldReturnPanData() throws Exception {

        PanDataResponse response = new PanDataResponse(
                1L,
                "ABCDE1234F",
                "John Doe",
                "Richard Doe",
                LocalDate.of(1995, 5, 10),
                LocalDateTime.of(2026, 9, 2, 10, 10)
        );

        when(kycService.getPanData(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/kyc/documents/1/pan-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(1))
                .andExpect(jsonPath("$.panNumber")
                        .value("ABCDE1234F"))
                .andExpect(jsonPath("$.fullName")
                        .value("John Doe"))
                .andExpect(jsonPath("$.fatherName")
                        .value("Richard Doe"))
                .andExpect(jsonPath("$.dateOfBirth")
                        .value("1995-05-10"));

        verify(kycService).getPanData(1L);
    }

    // ---------------------------------------------------------
    // GET /api/v1/kyc/documents/{documentId}/aadhaar-data
    // ---------------------------------------------------------

    @Test
    void getAadhaarData_shouldReturnAadhaarData() throws Exception {

        AadhaarDataResponse response = new AadhaarDataResponse(
                2L,
                "123456789012",
                "John Doe",
                LocalDate.of(1995, 5, 10),
                "MALE",
                "Nashik, Maharashtra",
                "9876543210",
                LocalDateTime.of(2026, 9, 2, 10, 15)
        );

        when(kycService.getAadhaarData(2L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/kyc/documents/2/aadhaar-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(2))
                .andExpect(jsonPath("$.aadhaarNumber")
                        .value("123456789012"))
                .andExpect(jsonPath("$.fullName")
                        .value("John Doe"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.address")
                        .value("Nashik, Maharashtra"))
                .andExpect(jsonPath("$.mobileNumber")
                        .value("9876543210"))
                .andExpect(jsonPath("$.dateOfBirth")
                        .value("1995-05-10"));

        verify(kycService).getAadhaarData(2L);
    }
}