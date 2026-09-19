package com.bankflow.ai;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MarkdownChunker {

    private static final Pattern HEADING_PATTERN =
            Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);

    public List<RagChunk> chunk(RagDocument document) {

        List<RagChunk> chunks = new ArrayList<>();

        Matcher matcher = HEADING_PATTERN.matcher(document.content());

        List<String> headingStack = new ArrayList<>();

        int currentContentStart = 0;
        int chunkIndex = 0;

        while (matcher.find()) {

            if (matcher.start() > currentContentStart) {

                String content = document.content()
                        .substring(currentContentStart, matcher.start())
                        .trim();

                if (isMeaningfulContent(content)) {

                    String section = buildSectionPath(headingStack);

                    chunks.add(new RagChunk(
                            chunkIndex++,
                            section,
                            content,
                            RagAudience.SHARED
                    ));
                }
            }

            int headingLevel = matcher.group(1).length();
            String heading = matcher.group(2).trim();

            while (headingStack.size() >= headingLevel) {
                headingStack.remove(headingStack.size() - 1);
            }

            headingStack.add(heading);

            currentContentStart = matcher.end();
        }

        if (currentContentStart < document.content().length()) {

            String content = document.content()
                    .substring(currentContentStart)
                    .trim();

            if (isMeaningfulContent(content)) {

                String section = buildSectionPath(headingStack);

                chunks.add(new RagChunk(
                        chunkIndex,
                        section,
                        content,
                        RagAudience.SHARED
                ));
            }
        }

        return chunks;
    }

    private String buildSectionPath(List<String> headingStack) {

        if (headingStack.isEmpty()) {
            return "Document";
        }

        return String.join(" > ", headingStack);
    }

    private boolean isMeaningfulContent(String content) {

        if (content.isBlank()) {
            return false;
        }

        /*
         * These are references to documents that will be ingested
         * separately as their authoritative sources.
         */
        if (content.matches(
                "\\[.*?\\]\\(docs/(architecture|data-model|workflows)/.*?\\.drawio\\.(xml|png)\\)"
        )) {
            return false;
        }

        if (content.matches(
                "(?s).*\\[.*?\\]\\(docs/api/bankflow_openapi\\.(yml|json)\\).*"
        )) {
            return false;
        }

        return true;
    }
}