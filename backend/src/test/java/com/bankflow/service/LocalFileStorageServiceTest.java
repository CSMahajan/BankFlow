package com.bankflow.service;

import com.bankflow.dto.StoredFileMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileStorageServiceTest {

    @TempDir
    Path tempDirectory;

    private LocalFileStorageService service;

    @BeforeEach
    void setUp() {
        service = new LocalFileStorageService(
                tempDirectory.toString()
        );
    }

    @Test
    void shouldCreateStorageDirectory() {
        assertTrue(
                Files.exists(tempDirectory)
        );

        assertTrue(
                Files.isDirectory(tempDirectory)
        );
    }

    @Test
    void store_shouldStoreFileSuccessfully() throws IOException {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "test file content".getBytes()
        );

        StoredFileMetadata result =
                service.store(file, 123L);

        assertNotNull(result);

        assertNotNull(result.path());

        assertEquals(
                "LOCAL",
                result.storageType()
        );

        assertNull(result.bucket());
        assertNull(result.objectKey());
        assertNull(result.encryptionType());
        assertNull(result.checksum());

        assertTrue(
                result.path().startsWith("user-123/")
        );

        Path storedFile =
                tempDirectory.resolve(result.path());

        assertTrue(
                Files.exists(storedFile)
        );

        assertEquals(
                "test file content",
                Files.readString(storedFile)
        );
    }

    @Test
    void store_shouldCreateUserSpecificDirectory() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "content".getBytes()
        );

        StoredFileMetadata result =
                service.store(file, 456L);

        Path userDirectory =
                tempDirectory.resolve("user-456");

        assertTrue(
                Files.exists(userDirectory)
        );

        assertTrue(
                Files.isDirectory(userDirectory)
        );

        assertTrue(
                result.path().startsWith("user-456/")
        );
    }

    @Test
    void store_shouldPreserveFileExtension() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "content".getBytes()
        );

        StoredFileMetadata result =
                service.store(file, 1L);

        assertTrue(
                result.path().endsWith(".pdf")
        );
    }

    @Test
    void store_shouldWorkWithoutFileExtension() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document",
                "text/plain",
                "content".getBytes()
        );

        StoredFileMetadata result =
                service.store(file, 1L);

        assertNotNull(result);

        assertFalse(
                result.path().endsWith(".")
        );

        Path storedFile =
                tempDirectory.resolve(result.path());

        assertTrue(
                Files.exists(storedFile)
        );
    }

    @Test
    void store_shouldGenerateUniqueFileNames() {

        MultipartFile file1 = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "content 1".getBytes()
        );

        MultipartFile file2 = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "content 2".getBytes()
        );

        StoredFileMetadata result1 =
                service.store(file1, 1L);

        StoredFileMetadata result2 =
                service.store(file2, 1L);

        assertNotEquals(
                result1.path(),
                result2.path()
        );
    }

    @Test
    void store_shouldAllowDifferentUsersToStoreFiles() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "content".getBytes()
        );

        StoredFileMetadata user1File =
                service.store(file, 1L);

        StoredFileMetadata user2File =
                service.store(file, 2L);

        assertTrue(
                user1File.path().startsWith("user-1/")
        );

        assertTrue(
                user2File.path().startsWith("user-2/")
        );

        assertNotEquals(
                user1File.path(),
                user2File.path()
        );
    }

    @Test
    void load_shouldReturnStoredFile() throws IOException {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "hello bankflow".getBytes()
        );

        StoredFileMetadata metadata =
                service.store(file, 123L);

        Resource resource =
                service.load(metadata.path());

        assertNotNull(resource);

        assertTrue(
                resource.exists()
        );

        assertTrue(
                resource.isReadable()
        );

        assertEquals(
                "hello bankflow",
                new String(
                        resource.getInputStream().readAllBytes()
                )
        );
    }

    @Test
    void load_shouldThrowExceptionWhenFileDoesNotExist() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> service.load(
                                "user-123/missing.pdf"
                        )
                );

        assertEquals(
                "Unable to read file",
                exception.getMessage()
        );
    }

    @Test
    void load_shouldRejectPathTraversal() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> service.load(
                                "../outside.txt"
                        )
                );

        assertEquals(
                "Unable to read file",
                exception.getMessage()
        );

        assertInstanceOf(
                RuntimeException.class,
                exception.getCause()
        );

        assertEquals(
                "Invalid file path",
                exception.getCause().getMessage()
        );
    }

    @Test
    void delete_shouldDeleteExistingFile() throws IOException {

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "delete me".getBytes()
        );

        StoredFileMetadata metadata =
                service.store(file, 123L);

        Path storedFile =
                tempDirectory.resolve(metadata.path());

        assertTrue(
                Files.exists(storedFile)
        );

        service.delete(metadata.path());

        assertFalse(
                Files.exists(storedFile)
        );
    }

    @Test
    void delete_shouldNotFailWhenFileDoesNotExist() {

        assertDoesNotThrow(
                () -> service.delete(
                        "user-123/missing.txt"
                )
        );
    }

    @Test
    void delete_shouldRejectPathTraversal() {

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> service.delete(
                                "../outside.txt"
                        )
                );

        assertEquals(
                "Invalid file path",
                exception.getMessage()
        );
    }

    @Test
    void store_shouldStoreEmptyFile() throws IOException {

        MultipartFile file = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        StoredFileMetadata metadata =
                service.store(file, 123L);

        assertNotNull(metadata);

        Path storedFile =
                tempDirectory.resolve(metadata.path());

        assertTrue(
                Files.exists(storedFile)
        );

        assertEquals(
                0,
                Files.size(storedFile)
        );
    }

    @Test
    void store_shouldHandleFilenameWithMultipleDots() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "bank.statement.final.pdf",
                "application/pdf",
                "content".getBytes()
        );

        StoredFileMetadata metadata =
                service.store(file, 123L);

        assertTrue(
                metadata.path().endsWith(".pdf")
        );
    }

    @Test
    void storedMetadata_shouldContainExpectedLocalStorageValues() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "image".getBytes()
        );

        StoredFileMetadata metadata =
                service.store(file, 999L);

        assertNotNull(metadata.path());

        assertEquals(
                "LOCAL",
                metadata.storageType()
        );

        assertNull(metadata.bucket());
        assertNull(metadata.objectKey());
        assertNull(metadata.encryptionType());
        assertNull(metadata.checksum());
    }
}