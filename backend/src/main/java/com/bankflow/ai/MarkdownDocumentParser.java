package com.bankflow.ai;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MarkdownDocumentParser implements RagDocumentParser {

    @Override
    public boolean supports(Path path) {
        return path.getFileName()
                .toString()
                .toLowerCase()
                .endsWith(".md");
    }

    @Override
    public RagDocument parse(Path path) {
        try {
            String content = Files.readString(path);

            String fileName = path.getFileName().toString();

            String title = fileName.endsWith(".md")
                    ? fileName.substring(0, fileName.length() - 3)
                    : fileName;

            return new RagDocument(
                    path.toString(),
                    RagSourceType.MARKDOWN,
                    title,
                    content
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read Markdown document: " + path,
                    e
            );
        }
    }
}