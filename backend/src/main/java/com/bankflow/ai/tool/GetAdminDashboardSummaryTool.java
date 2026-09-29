package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.AdminDashboardSummaryResponse;
import com.bankflow.service.DashboardService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetAdminDashboardSummaryTool implements AiTool {

    public static final String NAME = "get_admin_dashboard_summary";

    private final DashboardService dashboardService;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public FunctionDeclaration functionDeclaration() {
        return FunctionDeclaration.builder()
                .name(NAME)
                .description("""
                        Returns the current BankFlow administrative dashboard summary.

                        Includes:
                        - total customers
                        - total accounts
                        - active loans
                        - pending loans
                        - active fixed deposits
                        - total deposits
                        - pending KYC documents

                        This function takes no parameters.
                        """)
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.ADMIN);
    }

    @Override
    public AdminDashboardSummaryResponse execute() {
        return dashboardService.getAdminDashboardSummary();
    }
}