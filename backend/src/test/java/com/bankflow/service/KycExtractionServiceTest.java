package com.bankflow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycExtractionServiceTest {

    @Mock
    private KycExtractionProcessorService processor;

    private KycExtractionService service;

    @BeforeEach
    void setUp() {
        service = new KycExtractionService(processor);
    }

    @Test
    void extractAsync_shouldProcessDocument() {

        Long documentId = 123L;

        KycExtractionEventPublisher.KycExtractionEvent event =
                new KycExtractionEventPublisher.KycExtractionEvent(
                        documentId
                );

        service.extractAsync(event);

        verify(processor).process(documentId);
    }

    @Test
    void extractAsync_shouldProcessCorrectDocumentId() {

        Long documentId = 456L;

        KycExtractionEventPublisher.KycExtractionEvent event =
                new KycExtractionEventPublisher.KycExtractionEvent(
                        documentId
                );

        service.extractAsync(event);

        verify(processor, times(1))
                .process(456L);
    }

    @Test
    void extractAsync_shouldNotProcessAnyOtherDocument() {

        Long documentId = 123L;

        KycExtractionEventPublisher.KycExtractionEvent event =
                new KycExtractionEventPublisher.KycExtractionEvent(
                        documentId
                );

        service.extractAsync(event);

        verify(processor).process(123L);

        verify(processor, never())
                .process(456L);
    }

    @Test
    void extractAsync_shouldPropagateProcessorException() {

        Long documentId = 123L;

        KycExtractionEventPublisher.KycExtractionEvent event =
                new KycExtractionEventPublisher.KycExtractionEvent(
                        documentId
                );

        RuntimeException exception =
                new RuntimeException("Extraction failed");

        doThrow(exception)
                .when(processor)
                .process(documentId);

        RuntimeException thrown =
                org.junit.jupiter.api.Assertions.assertThrows(
                        RuntimeException.class,
                        () -> service.extractAsync(event)
                );

        org.junit.jupiter.api.Assertions.assertSame(
                exception,
                thrown
        );

        verify(processor).process(documentId);
    }
}