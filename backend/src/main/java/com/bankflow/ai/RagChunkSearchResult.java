package com.bankflow.ai;

public interface RagChunkSearchResult {

    Long getId();

    String getSourcePath();

    String getSection();

    String getContent();

    String getAudience();

    Double getDistance();

    RagSourceType getSourceType();
}