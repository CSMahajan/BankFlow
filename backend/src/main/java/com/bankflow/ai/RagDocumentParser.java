package com.bankflow.ai;

import java.nio.file.Path;

public interface RagDocumentParser {

    boolean supports(Path path);

    RagDocument parse(Path path);
}