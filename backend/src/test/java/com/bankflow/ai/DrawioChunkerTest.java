package com.bankflow.ai;

import com.bankflow.ai.rag.RagAudience;
import com.bankflow.ai.rag.RagChunk;
import com.bankflow.ai.rag.RagDocument;
import com.bankflow.ai.rag.RagSourceType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DrawioChunkerTest {

    private final DrawioChunker chunker =
            new DrawioChunker();

    @Test
    void shouldCreateSingleSemanticChunk() {

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <mxfile>
                    <diagram name="BankFlow KYC Workflow">
                        <mxGraphModel>
                            <root>
                                <mxCell id="0"/>
                                <mxCell id="1" parent="0"/>

                                <mxCell
                                    id="customer"
                                    value="Customer / React Frontend"
                                    vertex="1"
                                    parent="1"/>

                                <mxCell
                                    id="api"
                                    value="Spring Boot KYC API&lt;br&gt;Validate request + create KYC record"
                                    vertex="1"
                                    parent="1"/>

                                <mxCell
                                    id="edge1"
                                    value="HTTPS / REST"
                                    edge="1"
                                    source="customer"
                                    target="api"
                                    parent="1"/>
                            </root>
                        </mxGraphModel>
                    </diagram>
                </mxfile>
                """;

        RagDocument document =
                new RagDocument(
                        "docs/workflows/kyc.drawio.xml",
                        RagSourceType.DRAWIO_XML,
                        "BankFlow KYC Workflow",
                        xml
                );

        List<RagChunk> chunks =
                chunker.chunk(document);

        assertEquals(1, chunks.size());

        RagChunk chunk =
                chunks.getFirst();

        assertEquals(
                0,
                chunk.chunkIndex()
        );

        assertEquals(
                "Diagram > BankFlow KYC Workflow",
                chunk.section()
        );

        assertEquals(
                RagAudience.SHARED,
                chunk.audience()
        );

        assertTrue(
                chunk.content().contains(
                        "Diagram: BankFlow KYC Workflow"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "Customer / React Frontend"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "Spring Boot KYC API\nValidate request + create KYC record"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "Customer / React Frontend -> Spring Boot KYC API — Validate request + create KYC record [HTTPS / REST]"
                )
        );
    }

    @Test
    void shouldPreserveBranchLabels() {

        String xml = """
                <mxfile>
                    <diagram name="Security Workflow">
                        <mxGraphModel>
                            <root>
                                <mxCell id="0"/>
                                <mxCell id="1" parent="0"/>

                                <mxCell
                                    id="decision"
                                    value="CLEAN?"
                                    vertex="1"
                                    parent="1"/>

                                <mxCell
                                    id="yes"
                                    value="Continue processing"
                                    vertex="1"
                                    parent="1"/>

                                <mxCell
                                    id="no"
                                    value="Stop processing"
                                    vertex="1"
                                    parent="1"/>

                                <mxCell
                                    id="edgeYes"
                                    value="YES"
                                    edge="1"
                                    source="decision"
                                    target="yes"
                                    parent="1"/>

                                <mxCell
                                    id="edgeNo"
                                    value="NO"
                                    edge="1"
                                    source="decision"
                                    target="no"
                                    parent="1"/>
                            </root>
                        </mxGraphModel>
                    </diagram>
                </mxfile>
                """;

        RagDocument document =
                new RagDocument(
                        "docs/workflows/security.drawio.xml",
                        RagSourceType.DRAWIO_XML,
                        "Security Workflow",
                        xml
                );

        RagChunk chunk =
                chunker.chunk(document).getFirst();

        assertTrue(
                chunk.content().contains(
                        "CLEAN? -> Continue processing [YES]"
                )
        );

        assertTrue(
                chunk.content().contains(
                        "CLEAN? -> Stop processing [NO]"
                )
        );
    }

    @Test
    void shouldIgnoreStructuralCells() {

        String xml = """
                <mxfile>
                    <diagram name="Test">
                        <mxGraphModel>
                            <root>
                                <mxCell id="0"/>
                                <mxCell id="1" parent="0"/>
                            </root>
                        </mxGraphModel>
                    </diagram>
                </mxfile>
                """;

        RagDocument document =
                new RagDocument(
                        "test.drawio.xml",
                        RagSourceType.DRAWIO_XML,
                        "Test",
                        xml
                );

        List<RagChunk> chunks =
                chunker.chunk(document);

        assertEquals(1, chunks.size());

        assertTrue(
                chunks.getFirst()
                        .content()
                        .contains("Nodes:")
        );

        assertTrue(
                chunks.getFirst()
                        .content()
                        .contains("Flow:")
        );
    }

    @Test
    void shouldRejectNonDrawioDocument() {

        RagDocument document =
                new RagDocument(
                        "README.md",
                        RagSourceType.MARKDOWN,
                        "README",
                        "# BankFlow"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> chunker.chunk(document)
        );
    }
}