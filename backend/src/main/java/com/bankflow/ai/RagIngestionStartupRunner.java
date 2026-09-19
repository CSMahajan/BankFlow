package com.bankflow.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class RagIngestionStartupRunner implements CommandLineRunner {

    private final RagIngestionRunner ragIngestionRunner;
    private final boolean enabled;
    private final String repositoryRoot;

    public RagIngestionStartupRunner(
            RagIngestionRunner ragIngestionRunner,
            @Value("${rag.ingestion.enabled:false}") boolean enabled,
            @Value("${rag.ingestion.repository-root:..}") String repositoryRoot) {

        this.ragIngestionRunner = ragIngestionRunner;
        this.enabled = enabled;
        this.repositoryRoot = repositoryRoot;
    }

    @Override
    public void run(String... args) {

        if (!enabled) {
            return;
        }

        Path root =
                Path.of(repositoryRoot)
                        .toAbsolutePath()
                        .normalize();

        System.out.println(
                "Starting BankFlow RAG ingestion from: " + root
        );

        ragIngestionRunner.ingest(root);

        System.out.println(
                "BankFlow RAG ingestion completed successfully."
        );
    }
}