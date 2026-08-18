package com.bankflow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycExtractionService {

    private final KycExtractionProcessorService processor;


    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void extractAsync(
            KycExtractionEventPublisher.KycExtractionEvent event
    ) {

        processor.process(event.documentId());

    }
}
