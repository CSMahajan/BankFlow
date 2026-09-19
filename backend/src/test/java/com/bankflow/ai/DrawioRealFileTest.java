package com.bankflow.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DrawioRealFileTest {

    private DrawioDocumentParser parser;
    private DrawioChunker chunker;

    @BeforeEach
    void setUp() {
        parser = new DrawioDocumentParser();
        chunker = new DrawioChunker();
    }

    @Test
    void shouldChunkKycWorkflow() throws IOException {

        RagChunk chunk = loadAndChunk(
                "drawio/BankFlow_KYC_Workflow.drawio.xml"
        );

        assertTrue(
                chunk.content().contains(
                        "BankFlow KYC Document Processing Workflow"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "Customer / React Frontend"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "Amazon GuardDuty"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "Amazon Textract"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "CLEAN? -> Publish existing KycExtractionEvent — via ApplicationEventPublisher [YES]"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "CLEAN? -> INFECTED / threat detected — Stop OCR processing [NO → stop processing]"
                )
        );
    }

    @Test
    void shouldChunkAuthenticationWorkflow()
            throws IOException {

        RagChunk chunk = loadAndChunk(
                "drawio/BankFlow_Authentication_Workflows.drawio.xml"
        );

        assertTrue(
                chunk.content().contains(
                        "BankFlow Authentication Workflows"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "POST /auth/register"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "POST /auth/login"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "BCrypt password hash"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "Generate JWT access token"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "Create refresh token"
                )
        );
    }

    @Test
    void shouldChunkEmailPasswordWorkflow()
            throws IOException {

        RagChunk chunk = loadAndChunk(
                "drawio/BankFlow_Email_Password_Workflows.drawio.xml"
        );

        assertTrue(
                chunk.content().contains(
                        "BankFlow Email Verification and Password Recovery"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "Registration"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "Create verification token"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "Forgot password"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "POST /auth/reset-password"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "BCrypt new password"
                )
        );
    }

    private RagChunk loadAndChunk(String resource)
            throws IOException {

        String xml = readResource(resource);

        RagDocument document =
                parser.parse(
                        writeTemporaryFile(resource, xml)
                );

        List<RagChunk> chunks =
                chunker.chunk(document);

        assertEquals(1, chunks.size());

        RagChunk chunk = chunks.getFirst();

        assertFalse(chunk.content().isBlank());
        assertEquals(
                "SHARED",
                chunk.audience().name()
        );

        return chunk;
    }

    private String readResource(String resource)
            throws IOException {

        try (InputStream inputStream =
                     getClass()
                             .getClassLoader()
                             .getResourceAsStream(resource)) {

            assertNotNull(
                    inputStream,
                    "Missing test resource: " + resource
            );

            return new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
    }

    private java.nio.file.Path writeTemporaryFile(
            String resource,
            String content)
            throws IOException {

        String fileName =
                resource.substring(
                        resource.lastIndexOf('/') + 1
                );

        java.nio.file.Path file =
                java.nio.file.Files.createTempFile(
                        "bankflow-",
                        "-" + fileName
                );

        java.nio.file.Files.writeString(
                file,
                content
        );

        file.toFile().deleteOnExit();

        return file;
    }
}