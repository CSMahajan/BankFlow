package com.bankflow.ai;

import com.bankflow.ai.rag.RagDocument;
import com.bankflow.ai.rag.RagDocumentParser;
import com.bankflow.ai.rag.RagSourceType;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Component
public class OpenApiDocumentParser implements RagDocumentParser {

    private final Yaml yaml = new Yaml();

    @Override
    public boolean supports(Path path) {
        String fileName = path.getFileName().toString();

        return fileName.equals("bankflow_openapi.yml")
                || fileName.equals("bankflow_openapi.yaml");
    }

    @Override
    @SuppressWarnings("unchecked")
    public RagDocument parse(Path path) {

        try {
            String content = Files.readString(path);

            Map<String, Object> specification = yaml.load(content);

            String title = extractTitle(specification);

            return new RagDocument(
                    path.toString(),
                    RagSourceType.OPENAPI,
                    title,
                    content
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read OpenAPI document: " + path,
                    e
            );
        }
    }

    private String extractTitle(Map<String, Object> specification) {

        Object infoObject = specification.get("info");

        if (infoObject instanceof Map<?, ?> info) {

            Object title = info.get("title");

            if (title instanceof String titleValue && !titleValue.isBlank()) {
                return titleValue;
            }
        }

        return "BankFlow OpenAPI Specification";
    }
}