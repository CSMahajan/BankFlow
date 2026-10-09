package com.bankflow.ai.tool;

import com.bankflow.ai.AiAudience;
import com.bankflow.dto.AuditLogResponse;
import com.bankflow.entity.AuditAction;
import com.bankflow.entity.User;
import com.bankflow.service.AuditLogService;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetAdminAuditLogsTool implements AiTool {

    public static final String NAME = "get_admin_audit_logs";

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    public static final String SCHEMA_TYPE_STRING = "STRING";

    private final AuditLogService auditLogService;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public FunctionDeclaration functionDeclaration() {
        return FunctionDeclaration.builder()
                .name(NAME)
                .description("""
                        Returns BankFlow audit logs for administrators.
                        
                        Optional filters:
                        - search: searches performed-by user or audit description
                        - role: filter by CUSTOMER or ADMIN
                        - action: filter by one audit action
                        - actions: filter by multiple audit actions
                        
                        Logs are returned with the most recent entries first.
                        
                        This function is available only to administrators.
                        """)
                .parameters(
                        Schema.builder()
                                .type("OBJECT")
                                .properties(
                                        Map.of(
                                                "search",
                                                Schema.builder()
                                                        .type(SCHEMA_TYPE_STRING)
                                                        .description(
                                                                "Optional text used to search the person who performed the action or the audit description"
                                                        )
                                                        .build(),

                                                "role",
                                                Schema.builder()
                                                        .type(SCHEMA_TYPE_STRING)
                                                        .enum_(List.of(
                                                                "CUSTOMER",
                                                                "ADMIN"
                                                        ))
                                                        .description(
                                                                "Optional role filter"
                                                        )
                                                        .build(),

                                                "action",
                                                Schema.builder()
                                                        .type(SCHEMA_TYPE_STRING)
                                                        .enum_(Arrays.stream(AuditAction.values())
                                                                .map(Enum::name)
                                                                .toList())
                                                        .description(
                                                                "Optional single audit action"
                                                        )
                                                        .build(),

                                                "actions",
                                                Schema.builder()
                                                        .type("ARRAY")
                                                        .items(
                                                                Schema.builder()
                                                                        .type(SCHEMA_TYPE_STRING)
                                                                        .enum_(Arrays.stream(AuditAction.values())
                                                                                .map(Enum::name)
                                                                                .toList())
                                                                        .build()
                                                        )
                                                        .description(
                                                                "Optional list of audit actions"
                                                        )
                                                        .build()
                                        )
                                )
                                .build()
                )
                .build();
    }

    @Override
    public Set<AiAudience> supportedAudiences() {
        return Set.of(AiAudience.ADMIN);
    }

    @Override
    public Page<AuditLogResponse> execute(
            Map<String, Object> arguments) {

        String search = parseString(arguments.get("search"));

        User.Role role = parseRole(arguments.get("role"));

        AuditAction action =
                parseAuditAction(arguments.get("action"));

        List<AuditAction> actions =
                parseAuditActions(arguments.get("actions"));

        return auditLogService.getAuditLogs(
                DEFAULT_PAGE,
                DEFAULT_SIZE,
                search,
                role,
                action,
                actions
        );
    }

    private String parseString(Object value) {

        if (value == null) {
            return null;
        }

        String result = value.toString().trim();

        return result.isBlank() ? null : result;
    }

    private User.Role parseRole(Object value) {
        String role = parseString(value);

        if (role == null) {
            return null;
        }

        try {
            return User.Role.valueOf(role.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid role: " + role
                            + ". Allowed values: CUSTOMER, ADMIN"
            );
        }
    }

    private AuditAction parseAuditAction(Object value) {
        String action = parseString(value);

        if (action == null) {
            return null;
        }

        try {
            return AuditAction.valueOf(
                    action.toUpperCase(java.util.Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid audit action: " + action
                            + ". Allowed values: "
                            + Arrays.toString(AuditAction.values())
            );
        }
    }

    private List<AuditAction> parseAuditActions(Object value) {
        if (value == null) {
            return List.of();
        }

        if (!(value instanceof List<?> values)) {
            throw new IllegalArgumentException(
                    "'actions' must be a list of valid audit actions"
            );
        }

        return values.stream()
                .map(item -> {
                    if (item == null) {
                        throw new IllegalArgumentException(
                                "Audit action must not be null"
                        );
                    }

                    String action = item.toString().trim();

                    if (action.isBlank()) {
                        throw new IllegalArgumentException(
                                "Audit action must not be blank"
                        );
                    }

                    try {
                        return AuditAction.valueOf(
                                action.toUpperCase(java.util.Locale.ROOT)
                        );
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(
                                "Invalid audit action: " + action
                                        + ". Allowed values: "
                                        + Arrays.toString(AuditAction.values())
                        );
                    }
                })
                .toList();
    }
}