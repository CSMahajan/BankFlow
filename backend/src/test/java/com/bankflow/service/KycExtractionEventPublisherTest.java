package com.bankflow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KycExtractionEventPublisherTest {

    private ApplicationEventPublisher publisher;
    private KycExtractionEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        publisher = mock(ApplicationEventPublisher.class);
        eventPublisher = new KycExtractionEventPublisher(publisher);
    }

    @Test
    void publish_shouldPublishKycExtractionEvent() {

        Long documentId = 123L;

        eventPublisher.publish(documentId);

        ArgumentCaptor<Object> captor =
                ArgumentCaptor.forClass(Object.class);

        verify(publisher).publishEvent(captor.capture());

        Object publishedEvent = captor.getValue();

        assertInstanceOf(
                KycExtractionEventPublisher.KycExtractionEvent.class,
                publishedEvent
        );

        KycExtractionEventPublisher.KycExtractionEvent event =
                (KycExtractionEventPublisher.KycExtractionEvent) publishedEvent;

        assertEquals(documentId, event.documentId());
    }

    @Test
    void publish_shouldPublishOnlyOneEvent() {

        Long documentId = 123L;

        eventPublisher.publish(documentId);

        verify(publisher, times(1))
                .publishEvent(any(Object.class));
    }
}