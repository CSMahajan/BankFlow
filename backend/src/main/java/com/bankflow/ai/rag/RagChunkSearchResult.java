package com.bankflow.ai.rag;

public interface RagChunkSearchResult {

    Long getId();

    String getSourcePath();

    String getSection();

    String getContent();

    String getAudience();

    Double getDistance();

    RagSourceType getSourceType();
}