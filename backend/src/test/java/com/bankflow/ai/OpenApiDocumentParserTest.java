package com.bankflow.ai;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiDocumentParserTest {

    private final OpenApiDocumentParser parser =
            new OpenApiDocumentParser();

    @Test
    void shouldSupportBankFlowOpenApiYaml() {

        assertTrue(
                parser.supports(
                        Path.of("docs/api/bankflow_openapi.yml")
                )
        );
    }

    @Test
    void shouldNotSupportOpenApiJson() {

        assertTrue(
                !parser.supports(
                        Path.of("docs/api/bankflow_openapi.json")
                )
        );
    }

    @Test
    void shouldParseBankFlowOpenApiYaml() {

        Path path = Path.of("../docs/api/bankflow_openapi.yml");

        RagDocument document = parser.parse(path);

        assertEquals(
                RagSourceType.OPENAPI,
                document.sourceType()
        );

        assertTrue(document.title() != null);
        assertTrue(!document.title().isBlank());

        assertTrue(document.content().contains("openapi"));
        assertTrue(document.content().contains("paths"));
    }
}