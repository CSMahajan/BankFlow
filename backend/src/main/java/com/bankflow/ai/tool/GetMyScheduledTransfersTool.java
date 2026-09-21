package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.ScheduledTransferResponse;
import com.bankflow.service.ScheduledTransferService;
import com.google.genai.types.FunctionDeclaration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetMyScheduledTransfersTool implements AiTool {

    public static final String NAME = "get_my_scheduled_transfers";

    private final ScheduledTransferService scheduledTransferService;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public FunctionDeclaration functionDeclaration() {
        return FunctionDeclaration.builder()
                .name(NAME)
                .description("""
                        Returns the authenticated customer's scheduled transfers.

                        The customer is determined by the server-side
                        authentication context. This function takes no parameters.
                        """)
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.CUSTOMER);
    }

    @Override
    public List<ScheduledTransferResponse> execute() {
        return scheduledTransferService.getMyScheduledTransfers();
    }
}