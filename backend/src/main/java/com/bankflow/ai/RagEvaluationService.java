package com.bankflow.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagEvaluationService {

    private final RagRetrievalService ragRetrievalService;

    public void evaluate() {

        List<RagEvaluationCase> evaluationCases =
                RagEvaluationDataset.cases();

        int recallAt5Hits = 0;
        int recallAt10Hits = 0;
        double reciprocalRankSum = 0.0;

        log.info("==============START==========================");
        log.info("Starting RAG evaluation");
        log.info("Evaluation cases: {}", evaluationCases.size());
        log.info("================END========================");

        for (int i = 0; i < evaluationCases.size(); i++) {

            RagEvaluationCase evaluationCase =
                    evaluationCases.get(i);

            List<RagChunkSearchResult> results =
                    ragRetrievalService.retrieveCandidates(
                            evaluationCase.question(),
                            RagAudience.SHARED
                    );

            int firstRelevantRank = findFirstRelevantRank(
                    results,
                    evaluationCase.expectedEvidence()
            );

            boolean recallAt5 =
                    firstRelevantRank > 0 && firstRelevantRank <= 5;

            boolean recallAt10 =
                    firstRelevantRank > 0 && firstRelevantRank <= 10;

            if (recallAt5) {
                recallAt5Hits++;
            }

            if (recallAt10) {
                recallAt10Hits++;
            }

            double reciprocalRank =
                    firstRelevantRank > 0
                            ? 1.0 / firstRelevantRank
                            : 0.0;

            reciprocalRankSum += reciprocalRank;

            logResult(
                    i + 1,
                    evaluationCase,
                    results,
                    firstRelevantRank,
                    recallAt5,
                    recallAt10,
                    reciprocalRank
            );
        }

        int totalCases = evaluationCases.size();

        double recallAt5 =
                (double) recallAt5Hits / totalCases;

        double recallAt10 =
                (double) recallAt10Hits / totalCases;

        double mrr =
                reciprocalRankSum / totalCases;

        log.info("================START========================");
        log.info("RAG EVALUATION SUMMARY");
        log.info("=================END=======================");
        log.info(
                "Recall@5  : {} / {} = {}",
                recallAt5Hits,
                totalCases,
                formatMetric(recallAt5)
        );
        log.info(
                "Recall@10 : {} / {} = {}",
                recallAt10Hits,
                totalCases,
                formatMetric(recallAt10)
        );
        log.info(
                "MRR       : {}",
                formatMetric(mrr)
        );
        log.info("==================EVALUATION END======================");
    }

    private int findFirstRelevantRank(
            List<RagChunkSearchResult> results,
            List<RagEvaluationEvidence> expectedEvidence) {

        for (int i = 0; i < results.size(); i++) {

            RagChunkSearchResult result = results.get(i);

            if (isRelevant(result, expectedEvidence)) {
                return i + 1;
            }
        }

        return 0;
    }

    private boolean isRelevant(
            RagChunkSearchResult result,
            List<RagEvaluationEvidence> expectedEvidence) {

        return expectedEvidence.stream()
                .anyMatch(expected ->
                        expected.sourcePath().equals(
                                result.getSourcePath()
                        )
                                && expected.section().equals(
                                result.getSection()
                        )
                );
    }

    private void logResult(
            int caseNumber,
            RagEvaluationCase evaluationCase,
            List<RagChunkSearchResult> results,
            int firstRelevantRank,
            boolean recallAt5,
            boolean recallAt10,
            double reciprocalRank) {

        log.info("----------------------------------------");
        log.info(
                "Case {}: {}",
                caseNumber,
                evaluationCase.question()
        );

        log.info(
                "First relevant rank: {}",
                firstRelevantRank == 0
                        ? "NOT FOUND"
                        : firstRelevantRank
        );

        log.info(
                "Recall@5: {} | Recall@10: {} | MRR: {}",
                recallAt5,
                recallAt10,
                formatMetric(reciprocalRank)
        );

        for (int i = 0; i < results.size(); i++) {

            RagChunkSearchResult result = results.get(i);

            log.info(
                    "Rank {} | distance={} | source={} | section={}",
                    i + 1,
                    result.getDistance(),
                    result.getSourcePath(),
                    result.getSection()
            );
        }
    }

    private String formatMetric(double value) {
        return String.format("%.3f", value);
    }
}