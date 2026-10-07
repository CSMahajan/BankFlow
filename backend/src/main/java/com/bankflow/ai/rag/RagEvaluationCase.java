package com.bankflow.ai.rag;

import java.util.List;

public record RagEvaluationCase(
        String question,
        List<RagEvaluationEvidence> expectedEvidence
) {
}