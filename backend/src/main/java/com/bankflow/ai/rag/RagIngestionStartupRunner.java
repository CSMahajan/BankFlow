package com.bankflow.ai.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@Slf4j
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
            log.info("Rag Ingestion has been disabled, skipping");
            return;
        }

        Path root =
                Path.of(repositoryRoot)
                        .toAbsolutePath()
                        .normalize();

        log.info(
                "Starting BankFlow RAG ingestion from: {}",
                root
        );

        ragIngestionRunner.ingest(root);

        log.info(
                "BankFlow RAG ingestion completed successfully."
        );
    }
}