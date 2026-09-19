package com.bankflow.ai;

import java.util.List;

public interface EmbeddingService {

    List<Float> generateEmbedding(String text);
}