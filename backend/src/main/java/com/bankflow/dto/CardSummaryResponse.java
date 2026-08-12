package com.bankflow.dto;

public record CardSummaryResponse(

        long totalCards,

        long activeCards,

        long blockedCards,

        long frozenCards

) {}