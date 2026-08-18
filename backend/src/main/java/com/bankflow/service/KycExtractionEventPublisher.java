package com.bankflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KycExtractionEventPublisher {

    private final ApplicationEventPublisher publisher;


    public void publish(Long documentId) {
        publisher.publishEvent(
                new KycExtractionEvent(documentId)
        );
    }


    public record KycExtractionEvent(Long documentId){}
}