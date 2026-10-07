package com.bankflow.ai.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RagEvaluationRunner implements CommandLineRunner {

    private final RagEvaluationService ragEvaluationService;

    @Value("${rag.evaluation.enabled:false}")
    private boolean enabled;

    @Override
    public void run(String... args) {

        if (!enabled) {
            return;
        }

        ragEvaluationService.evaluate();
    }
}