package com.bankflow.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DrawioDocumentParserTest {

    private final DrawioDocumentParser parser =
            new DrawioDocumentParser();

    @TempDir
    Path tempDirectory;

    @Test
    void shouldSupportDrawioXmlFile() {

        assertTrue(
                parser.supports(
                        Path.of("drawio/BankFlow_KYC_Workflow.drawio.xml")
                )
        );
    }

    @Test
    void shouldSupportDrawioXmlFileCaseInsensitively() {

        assertTrue(
                parser.supports(
                        Path.of("BankFlow_KYC_Workflow.DRAWIO.XML")
                )
        );
    }

    @Test
    void shouldNotSupportOtherFileTypes() {

        assertFalse(
                parser.supports(
                        Path.of("README.md")
                )
        );

        assertFalse(
                parser.supports(
                        Path.of("bankflow_openapi.yml")
                )
        );

        assertFalse(
                parser.supports(
                        Path.of("architecture.xml")
                )
        );
    }

    @Test
    void shouldParseDiagramTitleAndPreserveContent()
            throws Exception {

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <mxfile>
                    <diagram name="BankFlow System Architecture">
                        <mxGraphModel>
                            <root>
                                <mxCell id="0"/>
                                <mxCell id="1" parent="0"/>
                            </root>
                        </mxGraphModel>
                    </diagram>
                </mxfile>
                """;

        Path file =
                tempDirectory.resolve(
                        "BankFlow_System_Architecture.drawio.xml"
                );

        Files.writeString(file, xml);

        RagDocument document =
                parser.parse(file);

        assertEquals(
                "BankFlow System Architecture",
                document.title()
        );

        assertEquals(
                RagSourceType.DRAWIO_XML,
                document.sourceType()
        );

        assertEquals(
                file.toString(),
                document.sourcePath()
        );

        assertEquals(
                xml,
                document.content()
        );
    }

    @Test
    void shouldFallbackToFileNameWhenDiagramNameIsMissing()
            throws Exception {

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <mxfile>
                    <diagram>
                        <mxGraphModel>
                            <root/>
                        </mxGraphModel>
                    </diagram>
                </mxfile>
                """;

        Path file =
                tempDirectory.resolve(
                        "BankFlow_Architecture.drawio.xml"
                );

        Files.writeString(file, xml);

        RagDocument document =
                parser.parse(file);

        assertEquals(
                "BankFlow_Architecture",
                document.title()
        );
    }

    @Test
    void shouldRejectMalformedXml()
            throws Exception {

        String invalidXml = """
                <mxfile>
                    <diagram name="Broken">
                """;

        Path file =
                tempDirectory.resolve(
                        "broken.drawio.xml"
                );

        Files.writeString(file, invalidXml);

        assertThrows(
                IllegalStateException.class,
                () -> parser.parse(file)
        );
    }
}