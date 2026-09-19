package com.bankflow.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RagIngestionRunnerTest {

    @Mock
    private RagDocumentParser markdownParser;

    @Mock
    private RagDocumentParser openApiParser;

    @Mock
    private RagDocumentParser drawioParser;

    @Mock
    private MarkdownChunker markdownChunker;

    @Mock
    private OpenApiChunker openApiChunker;

    @Mock
    private DrawioChunker drawioChunker;

    @Mock
    private RagIngestionService ingestionService;

    private RagIngestionRunner runner;

    @TempDir
    Path tempDirectory;

    @BeforeEach
    void setUp() {
        runner = new RagIngestionRunner(
                List.of(markdownParser, openApiParser, drawioParser),
                markdownChunker,
                openApiChunker,
                drawioChunker,
                ingestionService
        );
    }

    @Test
    void shouldRouteMarkdownFileToMarkdownParserAndChunker()
            throws Exception {

        Path markdownFile =
                tempDirectory.resolve("README.md");

        Files.writeString(
                markdownFile,
                "# BankFlow\n\nBanking application."
        );

        RagDocument parsedDocument =
                new RagDocument(
                        markdownFile.toString(),
                        RagSourceType.MARKDOWN,
                        "README",
                        "# BankFlow\n\nBanking application."
                );

        RagChunk chunk =
                new RagChunk(
                        0,
                        "BankFlow",
                        "Banking application.",
                        RagAudience.SHARED
                );

        when(markdownParser.supports(markdownFile))
                .thenReturn(true);

        when(markdownParser.parse(markdownFile))
                .thenReturn(parsedDocument);

        when(markdownChunker.chunk(any(RagDocument.class)))
                .thenReturn(List.of(chunk));

        runner.ingest(tempDirectory);

        verify(markdownParser)
                .parse(markdownFile);

        verify(markdownChunker)
                .chunk(any(RagDocument.class));

        ArgumentCaptor<RagDocument> documentCaptor =
                ArgumentCaptor.forClass(RagDocument.class);

        verify(ingestionService)
                .ingest(
                        documentCaptor.capture(),
                        eq(List.of(chunk))
                );

        RagDocument actual =
                documentCaptor.getValue();

        assertEquals(
                "README.md",
                actual.sourcePath()
        );

        assertEquals(
                RagSourceType.MARKDOWN,
                actual.sourceType()
        );

        assertEquals(
                "README",
                actual.title()
        );

        assertEquals(
                "# BankFlow\n\nBanking application.",
                actual.content()
        );

        verifyNoInteractions(openApiChunker);
    }

    @Test
    void shouldRouteOpenApiFileToOpenApiParserAndChunker()
            throws Exception {

        Path apiDirectory =
                tempDirectory.resolve("docs/api");

        Files.createDirectories(apiDirectory);

        Path openApiFile =
                apiDirectory.resolve("bankflow_openapi.yml");

        Files.writeString(
                openApiFile,
                "openapi: 3.1.0"
        );

        RagDocument parsedDocument =
                new RagDocument(
                        openApiFile.toString(),
                        RagSourceType.OPENAPI,
                        "BankFlow API",
                        "openapi: 3.1.0"
                );

        RagChunk chunk =
                new RagChunk(
                        0,
                        "API > GET /api/v1/accounts",
                        "HTTP Method: GET",
                        RagAudience.SHARED
                );

        when(markdownParser.supports(openApiFile))
                .thenReturn(false);

        when(openApiParser.supports(openApiFile))
                .thenReturn(true);

        when(openApiParser.parse(openApiFile))
                .thenReturn(parsedDocument);

        when(openApiChunker.chunk(any(RagDocument.class)))
                .thenReturn(List.of(chunk));

        runner.ingest(tempDirectory);

        verify(openApiParser)
                .parse(openApiFile);

        verify(openApiChunker)
                .chunk(any(RagDocument.class));

        ArgumentCaptor<RagDocument> documentCaptor =
                ArgumentCaptor.forClass(RagDocument.class);

        verify(ingestionService)
                .ingest(
                        documentCaptor.capture(),
                        eq(List.of(chunk))
                );

        assertEquals(
                "docs/api/bankflow_openapi.yml",
                documentCaptor.getValue().sourcePath()
        );

        verifyNoInteractions(markdownChunker);
    }

    @Test
    void shouldRouteDrawioFileToDrawioParserAndChunker()
            throws Exception {

        Path workflowsDirectory =
                tempDirectory.resolve("docs/workflows");

        Files.createDirectories(workflowsDirectory);

        Path drawioFile =
                workflowsDirectory.resolve(
                        "BankFlow_KYC_Workflow.drawio.xml"
                );

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <mxfile>
                    <diagram name="BankFlow KYC Workflow">
                        <mxGraphModel>
                            <root>
                                <mxCell id="0"/>
                                <mxCell id="1" parent="0"/>
                            </root>
                        </mxGraphModel>
                    </diagram>
                </mxfile>
                """;

        Files.writeString(
                drawioFile,
                xml
        );

        RagDocument parsedDocument =
                new RagDocument(
                        drawioFile.toString(),
                        RagSourceType.DRAWIO_XML,
                        "BankFlow KYC Workflow",
                        xml
                );

        RagChunk chunk =
                new RagChunk(
                        0,
                        "Diagram > BankFlow KYC Workflow",
                        "Diagram: BankFlow KYC Workflow",
                        RagAudience.SHARED
                );

        when(markdownParser.supports(drawioFile))
                .thenReturn(false);

        when(openApiParser.supports(drawioFile))
                .thenReturn(false);

        when(drawioParser.supports(drawioFile))
                .thenReturn(true);

        when(drawioParser.parse(drawioFile))
                .thenReturn(parsedDocument);

        when(drawioChunker.chunk(any(RagDocument.class)))
                .thenReturn(List.of(chunk));

        runner.ingest(tempDirectory);

        verify(drawioParser)
                .parse(drawioFile);

        verify(drawioChunker)
                .chunk(any(RagDocument.class));

        ArgumentCaptor<RagDocument> documentCaptor =
                ArgumentCaptor.forClass(RagDocument.class);

        verify(ingestionService)
                .ingest(
                        documentCaptor.capture(),
                        eq(List.of(chunk))
                );

        RagDocument actual =
                documentCaptor.getValue();

        assertEquals(
                "docs/workflows/BankFlow_KYC_Workflow.drawio.xml",
                actual.sourcePath()
        );

        assertEquals(
                RagSourceType.DRAWIO_XML,
                actual.sourceType()
        );

        assertEquals(
                "BankFlow KYC Workflow",
                actual.title()
        );

        assertEquals(
                xml,
                actual.content()
        );

        verifyNoInteractions(markdownChunker);
        verifyNoInteractions(openApiChunker);
    }

    @Test
    void shouldIgnoreUnsupportedFiles()
            throws Exception {

        Path textFile =
                tempDirectory.resolve("notes.txt");

        Files.writeString(
                textFile,
                "This should not be ingested."
        );

        runner.ingest(tempDirectory);

        verifyNoInteractions(
                markdownParser,
                openApiParser,
                drawioParser,
                markdownChunker,
                openApiChunker,
                drawioChunker,
                ingestionService
        );
    }

    @Test
    void shouldRejectInvalidRepositoryRoot() {

        Path missingDirectory =
                tempDirectory.resolve("does-not-exist");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> runner.ingest(missingDirectory)
                );

        assertTrue(
                exception.getMessage()
                        .contains("Repository root")
        );
    }

    @Test
    void shouldDiscoverAndRouteAllSupportedDocumentTypes()
            throws Exception {

        Path readme =
                tempDirectory.resolve("README.md");

        Path apiDirectory =
                tempDirectory.resolve("docs/api");

        Path workflowsDirectory =
                tempDirectory.resolve("docs/workflows");

        Files.createDirectories(apiDirectory);
        Files.createDirectories(workflowsDirectory);

        Path openApi =
                apiDirectory.resolve("bankflow_openapi.yml");

        Path kyc =
                workflowsDirectory.resolve(
                        "BankFlow_KYC_Workflow.drawio.xml"
                );

        Files.writeString(
                readme,
                "# BankFlow\n"
        );

        Files.writeString(
                openApi,
                "openapi: 3.1.0\n"
        );

        String kycXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <mxfile>
                    <diagram name="BankFlow KYC Workflow">
                        <mxGraphModel>
                            <root>
                                <mxCell id="0"/>
                                <mxCell id="1" parent="0"/>
                            </root>
                        </mxGraphModel>
                    </diagram>
                </mxfile>
                """;

        Files.writeString(
                kyc,
                kycXml
        );

        RagDocument markdownDocument =
                new RagDocument(
                        "README.md",
                        RagSourceType.MARKDOWN,
                        "README",
                        "# BankFlow\n"
                );

        RagDocument openApiDocument =
                new RagDocument(
                        "docs/api/bankflow_openapi.yml",
                        RagSourceType.OPENAPI,
                        "BankFlow API",
                        "openapi: 3.1.0\n"
                );

        RagDocument drawioDocument =
                new RagDocument(
                        "docs/workflows/BankFlow_KYC_Workflow.drawio.xml",
                        RagSourceType.DRAWIO_XML,
                        "BankFlow KYC Workflow",
                        kycXml
                );

        RagChunk markdownChunk =
                new RagChunk(
                        0,
                        "README",
                        "BankFlow",
                        RagAudience.SHARED
                );

        RagChunk openApiChunk =
                new RagChunk(
                        0,
                        "API",
                        "BankFlow API",
                        RagAudience.SHARED
                );

        RagChunk drawioChunk =
                new RagChunk(
                        0,
                        "Diagram",
                        "BankFlow KYC Workflow",
                        RagAudience.SHARED
                );

        when(markdownParser.supports(readme))
                .thenReturn(true);

        when(markdownParser.parse(readme))
                .thenReturn(markdownDocument);

        when(markdownChunker.chunk(any(RagDocument.class)))
                .thenReturn(List.of(markdownChunk));

        when(markdownParser.supports(openApi))
                .thenReturn(false);

        when(openApiParser.supports(openApi))
                .thenReturn(true);

        when(openApiParser.parse(openApi))
                .thenReturn(openApiDocument);

        when(openApiChunker.chunk(any(RagDocument.class)))
                .thenReturn(List.of(openApiChunk));

        when(markdownParser.supports(kyc))
                .thenReturn(false);

        when(openApiParser.supports(kyc))
                .thenReturn(false);

        when(drawioParser.supports(kyc))
                .thenReturn(true);

        when(drawioParser.parse(kyc))
                .thenReturn(drawioDocument);

        when(drawioChunker.chunk(any(RagDocument.class)))
                .thenReturn(List.of(drawioChunk));

        runner.ingest(tempDirectory);

        verify(markdownChunker)
                .chunk(markdownDocument);

        verify(openApiChunker)
                .chunk(openApiDocument);

        verify(drawioChunker)
                .chunk(drawioDocument);

        verify(ingestionService, times(3))
                .ingest(
                        any(RagDocument.class),
                        anyList()
                );
    }
}