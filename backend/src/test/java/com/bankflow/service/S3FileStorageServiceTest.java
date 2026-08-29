package com.bankflow.service;

import com.bankflow.dto.StoredFileMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3FileStorageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile file;

    private S3FileStorageService s3FileStorageService;

    private static final String BUCKET_NAME = "bankflow-test-bucket";

    @BeforeEach
    void setUp() {

        s3FileStorageService =
                new S3FileStorageService(s3Client);

        ReflectionTestUtils.setField(
                s3FileStorageService,
                "bucketName",
                BUCKET_NAME
        );
    }

    @Test
    void store_shouldUploadFileAndReturnMetadata() throws Exception {

        byte[] content = "test document".getBytes();

        when(file.getOriginalFilename())
                .thenReturn("document.pdf");

        when(file.getContentType())
                .thenReturn("application/pdf");

        when(file.getSize())
                .thenReturn((long) content.length);

        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream(content));

        PutObjectResponse response =
                PutObjectResponse.builder()
                        .eTag("\"abc123\"")
                        .build();

        when(s3Client.putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        )).thenReturn(response);

        StoredFileMetadata result =
                s3FileStorageService.store(file, 10L);

        assertNotNull(result);

        assertEquals("S3", result.storageType());
        assertEquals(BUCKET_NAME, result.bucket());
        assertEquals("AES256", result.encryptionType());
        assertEquals("abc123", result.checksum());

        assertNotNull(result.path());
        assertTrue(result.path().startsWith("user-10/"));
        assertTrue(result.path().endsWith(".pdf"));

        assertEquals(result.path(), result.objectKey());

        verify(s3Client).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );
    }

    @Test
    void store_shouldBuildCorrectPutObjectRequest() throws Exception {

        byte[] content = "test".getBytes();

        when(file.getOriginalFilename())
                .thenReturn("statement.pdf");

        when(file.getContentType())
                .thenReturn("application/pdf");

        when(file.getSize())
                .thenReturn((long) content.length);

        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream(content));

        when(s3Client.putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        )).thenReturn(
                PutObjectResponse.builder()
                        .eTag("\"etag-value\"")
                        .build()
        );

        s3FileStorageService.store(file, 25L);

        ArgumentCaptor<PutObjectRequest> captor =
                ArgumentCaptor.forClass(PutObjectRequest.class);

        verify(s3Client).putObject(
                captor.capture(),
                any(RequestBody.class)
        );

        PutObjectRequest request = captor.getValue();

        assertEquals(BUCKET_NAME, request.bucket());

        assertTrue(request.key().startsWith("user-25/"));
        assertTrue(request.key().endsWith(".pdf"));

        assertEquals(
                "application/pdf",
                request.contentType()
        );

        assertEquals(
                content.length,
                request.contentLength()
        );

        assertEquals(
                ServerSideEncryption.AES256,
                request.serverSideEncryption()
        );

        assertEquals(
                "bankflow",
                request.metadata().get("uploaded-by")
        );
    }

    @Test
    void store_shouldHandleFileWithoutExtension() throws Exception {

        byte[] content = "test".getBytes();

        when(file.getOriginalFilename())
                .thenReturn("document");

        when(file.getContentType())
                .thenReturn("application/pdf");

        when(file.getSize())
                .thenReturn((long) content.length);

        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream(content));

        when(s3Client.putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        )).thenReturn(
                PutObjectResponse.builder()
                        .eTag("\"etag\"")
                        .build()
        );

        StoredFileMetadata result =
                s3FileStorageService.store(file, 5L);

        assertNotNull(result);

        assertTrue(
                result.path().startsWith("user-5/")
        );

        assertFalse(
                result.path().endsWith(".pdf")
        );
    }

    @Test
    void store_shouldHandleNullFilename() throws Exception {

        byte[] content = "test".getBytes();

        when(file.getOriginalFilename())
                .thenReturn(null);

        when(file.getSize())
                .thenReturn((long) content.length);

        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream(content));

        when(s3Client.putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        )).thenReturn(
                PutObjectResponse.builder()
                        .eTag("\"etag\"")
                        .build()
        );

        StoredFileMetadata result =
                s3FileStorageService.store(file, 5L);

        assertNotNull(result);

        assertTrue(
                result.path().startsWith("user-5/")
        );
    }

    @Test
    void store_shouldThrowRuntimeExceptionWhenInputStreamFails()
            throws Exception {

        when(file.getOriginalFilename())
                .thenReturn("document.pdf");

        when(file.getSize())
                .thenReturn(100L);

        when(file.getInputStream())
                .thenThrow(new IOException("Unable to read file"));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> s3FileStorageService.store(file, 10L)
                );

        assertEquals(
                "S3 file upload failed",
                exception.getMessage()
        );

        assertInstanceOf(
                IOException.class,
                exception.getCause()
        );

        verify(s3Client, never()).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );
    }

    @Test
    void delete_shouldDeleteFileFromS3() {

        String filePath =
                "user-10/document.pdf";

        s3FileStorageService.delete(filePath);

        ArgumentCaptor<DeleteObjectRequest> captor =
                ArgumentCaptor.forClass(
                        DeleteObjectRequest.class
                );

        verify(s3Client).deleteObject(captor.capture());

        DeleteObjectRequest request =
                captor.getValue();

        assertEquals(BUCKET_NAME, request.bucket());
        assertEquals(filePath, request.key());
    }

    @Test
    void delete_shouldDoNothingWhenFilePathIsNull() {

        s3FileStorageService.delete(null);

        verifyNoInteractions(s3Client);
    }

    @Test
    void delete_shouldDoNothingWhenFilePathIsBlank() {

        s3FileStorageService.delete("   ");

        verifyNoInteractions(s3Client);
    }

    @Test
    void load_shouldReturnResourceFromS3() {

        String filePath =
                "user-10/document.pdf";

        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> response =
                mock(ResponseInputStream.class);

        when(s3Client.getObject(
                any(GetObjectRequest.class)
        )).thenReturn(response);

        Resource result =
                s3FileStorageService.load(filePath);

        assertNotNull(result);

        assertInstanceOf(
                InputStreamResource.class,
                result
        );

        ArgumentCaptor<GetObjectRequest> captor =
                ArgumentCaptor.forClass(
                        GetObjectRequest.class
                );

        verify(s3Client).getObject(
                captor.capture()
        );

        GetObjectRequest request =
                captor.getValue();

        assertEquals(
                BUCKET_NAME,
                request.bucket()
        );

        assertEquals(
                filePath,
                request.key()
        );
    }
}