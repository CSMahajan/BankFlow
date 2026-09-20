package com.bankflow.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RagIngestionRunner {

    private final List<RagDocumentParser> parsers;
    private final MarkdownChunker markdownChunker;
    private final OpenApiChunker openApiChunker;
    private final DrawioChunker drawioChunker;
    private final RagIngestionService ingestionService;

    public void ingest(Path repositoryRoot) {

        if (repositoryRoot == null
                || !Files.isDirectory(repositoryRoot)) {

            throw new IllegalArgumentException(
                    "Repository root must be an existing directory: "
                            + repositoryRoot
            );
        }

        try (var paths = Files.walk(repositoryRoot)) {

            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> isSupportedFile(repositoryRoot, path))
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(path ->
                            ingestFile(repositoryRoot, path)
                    );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to discover RAG documents under: "
                            + repositoryRoot,
                    e
            );
        }
    }

    private void ingestFile(
            Path repositoryRoot,
            Path file) {

        RagDocumentParser parser =
                parsers.stream()
                        .filter(candidate -> candidate.supports(file))
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException("No parser found for supported file: " + file)
                        );

        RagDocument parsedDocument =
                parser.parse(file);

        String relativePath =
                repositoryRoot
                        .relativize(file)
                        .toString()
                        .replace('\\', '/');

        RagDocument document =
                new RagDocument(
                        relativePath,
                        parsedDocument.sourceType(),
                        parsedDocument.title(),
                        parsedDocument.content()
                );

        List<RagChunk> chunks =
                chunk(document);

        ingestionService.ingest(
                document,
                chunks
        );
    }

    private List<RagChunk> chunk(RagDocument document) {

        return switch (document.sourceType()) {
            case MARKDOWN -> markdownChunker.chunk(document);
            case OPENAPI -> openApiChunker.chunk(document);
            case DRAWIO_XML -> drawioChunker.chunk(document);
        };
    }

    private boolean isSupportedFile(
            Path repositoryRoot,
            Path path) {

        Path relativePath =
                repositoryRoot.relativize(path);

        String normalizedPath =
                relativePath
                        .toString()
                        .replace('\\', '/');

        String lowerCasePath =
                normalizedPath.toLowerCase();

        if (lowerCasePath.equals("readme.md")) {
            return true;
        }

        if (lowerCasePath.startsWith("docs/")
                && lowerCasePath.endsWith(".md")) {
            return true;
        }

        if (lowerCasePath.startsWith("docs/")
                && lowerCasePath.endsWith(".drawio.xml")) {
            return true;
        }

        return lowerCasePath.equals(
                "docs/api/bankflow_openapi.yml"
        ) || lowerCasePath.equals(
                "docs/api/bankflow_openapi.yaml"
        );
    }
}