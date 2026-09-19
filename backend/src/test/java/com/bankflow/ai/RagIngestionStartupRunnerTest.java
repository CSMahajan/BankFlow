package com.bankflow.ai;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.mockito.Mockito.*;

class RagIngestionStartupRunnerTest {

    @Test
    void shouldNotIngestWhenDisabled() {

        RagIngestionRunner ingestionRunner =
                mock(RagIngestionRunner.class);

        RagIngestionStartupRunner runner =
                new RagIngestionStartupRunner(
                        ingestionRunner,
                        false,
                        ".."
                );

        runner.run();

        verifyNoInteractions(ingestionRunner);
    }

    @Test
    void shouldIngestConfiguredRepositoryWhenEnabled() {

        RagIngestionRunner ingestionRunner =
                mock(RagIngestionRunner.class);

        RagIngestionStartupRunner runner =
                new RagIngestionStartupRunner(
                        ingestionRunner,
                        true,
                        ".."
                );

        runner.run();

        verify(ingestionRunner)
                .ingest(
                        Path.of("..")
                                .toAbsolutePath()
                                .normalize()
                );
    }
}