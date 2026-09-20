package com.bankflow.ai;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class OpenApiChunker {

    private static final List<String> HTTP_METHODS = List.of(
            "get",
            "post",
            "put",
            "patch",
            "delete",
            "head",
            "options"
    );

    private static final Set<String> ADMIN_ENDPOINTS = Set.of(
            // Loan administration
            "GET /api/v1/loans/pending",
            "PUT /api/v1/loans/{loanId}/approve",
            "PUT /api/v1/loans/{loanId}/reject",

            // Transaction administration
            "GET /api/v1/transactions/admin/search/{accountNumber}",
            "GET /api/v1/transactions/admin/accounts/{accountNumber}/transactions",

            //Dashboard administration
            "GET /api/v1/dashboard/admin-summary",

            //User administration
            "GET /api/v1/users",
            "GET /api/v1/users/{userId}",

            //Audit administration
            "GET /api/v1/admin/audit-logs"
    );
    public static final String SCHEMA = "schema";

    private final Yaml yaml = new Yaml();

    public List<RagChunk> chunk(RagDocument document) {

        Map<String, Object> specification = yaml.load(document.content());

        Object pathsObject = specification.get("paths");

        if (!(pathsObject instanceof Map<?, ?> paths)) {
            return List.of();
        }

        List<RagChunk> chunks = new ArrayList<>();

        int chunkIndex = 0;

        for (Map.Entry<?, ?> pathEntry : paths.entrySet()) {

            String path = String.valueOf(pathEntry.getKey());

            if (!(pathEntry.getValue() instanceof Map<?, ?> pathDefinition)) {
                continue;
            }

            for (String method : HTTP_METHODS) {

                Object operationObject = pathDefinition.get(method);

                if (!(operationObject instanceof Map<?, ?> operation)) {
                    continue;
                }

                String content = buildOperationContent(
                        method,
                        path,
                        operation
                );

                if (!content.isBlank()) {

                    RagAudience audience = determineAudience(method, path);

                    chunks.add(new RagChunk(
                            chunkIndex++,
                            "API > " + method.toUpperCase() + " " + path,
                            content,
                            audience
                    ));
                }
            }
        }

        return chunks;
    }

    private String buildOperationContent(
            String method,
            String path,
            Map<?, ?> operation) {

        StringBuilder content = new StringBuilder();

        content.append("HTTP Method: ")
                .append(method.toUpperCase())
                .append("\n");

        content.append("Path: ")
                .append(path)
                .append("\n");

        appendValue(
                content,
                "Operation ID",
                operation.get("operationId")
        );

        appendList(
                content,
                "Tags",
                operation.get("tags")
        );

        appendParameters(
                content,
                operation.get("parameters")
        );

        appendRequestBody(
                content,
                operation.get("requestBody")
        );

        appendResponses(
                content,
                operation.get("responses")
        );

        return content.toString().trim();
    }

    private void appendValue(
            StringBuilder content,
            String label,
            Object value) {

        if (value != null && !String.valueOf(value).isBlank()) {

            content.append(label)
                    .append(": ")
                    .append(value)
                    .append("\n");
        }
    }

    private void appendList(
            StringBuilder content,
            String label,
            Object value) {

        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return;
        }

        content.append(label)
                .append(": ")
                .append(String.join(
                        ", ",
                        list.stream()
                                .map(String::valueOf)
                                .toList()
                ))
                .append("\n");
    }

    private void appendParameters(
            StringBuilder content,
            Object parametersObject) {

        if (!(parametersObject instanceof List<?> parameters)
                || parameters.isEmpty()) {
            return;
        }

        content.append("Parameters:\n");

        for (Object parameterObject : parameters) {

            if (!(parameterObject instanceof Map<?, ?> parameter)) {
                continue;
            }

            String name = String.valueOf(parameter.get("name"));
            String location = String.valueOf(parameter.get("in"));

            Object required = parameter.get("required");

            content.append("- ")
                    .append(name)
                    .append(" (")
                    .append(location)
                    .append(")");

            if (required != null) {
                content.append(", required=")
                        .append(required);
            }

            Object schema = parameter.get(SCHEMA);

            if (schema != null) {
                content.append(", schema=")
                        .append(formatSchemaReference(schema));
            }

            content.append("\n");
        }
    }

    private void appendRequestBody(
            StringBuilder content,
            Object requestBodyObject) {

        if (!(requestBodyObject instanceof Map<?, ?> requestBody)) {
            return;
        }

        content.append("Request Body:\n");

        Object required = requestBody.get("required");

        if (required != null) {
            content.append("- required=")
                    .append(required)
                    .append("\n");
        }

        Object contentObject = requestBody.get("content");

        if (!(contentObject instanceof Map<?, ?> contentMap)) {
            return;
        }

        for (Map.Entry<?, ?> entry : contentMap.entrySet()) {

            String mediaType = String.valueOf(entry.getKey());

            content.append("- media type: ")
                    .append(mediaType)
                    .append("\n");

            if (entry.getValue() instanceof Map<?, ?> mediaDefinition) {

                Object schema = mediaDefinition.get(SCHEMA);

                if (schema != null) {
                    content.append("  schema: ")
                            .append(formatSchemaReference(schema))
                            .append("\n");
                }
            }
        }
    }

    private void appendResponses(
            StringBuilder content,
            Object responsesObject) {

        if (!(responsesObject instanceof Map<?, ?> responses)
                || responses.isEmpty()) {
            return;
        }

        content.append("Responses:\n");

        for (Map.Entry<?, ?> entry : responses.entrySet()) {

            String statusCode = String.valueOf(entry.getKey());

            content.append("- ")
                    .append(statusCode);

            if (!(entry.getValue() instanceof Map<?, ?> response)) {
                content.append("\n");
                continue;
            }

            Object description = response.get("description");

            if (description != null) {
                content.append(": ")
                        .append(description);
            }

            Object responseContent = response.get("content");

            if (responseContent instanceof Map<?, ?> responseContentMap) {

                for (Map.Entry<?, ?> mediaEntry :
                        responseContentMap.entrySet()) {

                    String mediaType =
                            String.valueOf(mediaEntry.getKey());

                    content.append("\n  media type: ")
                            .append(mediaType);

                    if (mediaEntry.getValue()
                            instanceof Map<?, ?> mediaDefinition) {

                        Object schema =
                                mediaDefinition.get(SCHEMA);

                        if (schema != null) {
                            content.append("\n  schema: ")
                                    .append(formatSchemaReference(schema));
                        }
                    }
                }
            }

            content.append("\n");
        }
    }

    private String formatSchemaReference(Object schemaObject) {

        if (!(schemaObject instanceof Map<?, ?> schema)) {
            return String.valueOf(schemaObject);
        }

        Object reference = schema.get("$ref");

        if (reference != null) {
            return String.valueOf(reference);
        }

        Object type = schema.get("type");

        if (type != null) {
            return String.valueOf(type);
        }

        return schema.toString();
    }

    private RagAudience determineAudience(String method, String path) {

        String endpoint = method.toUpperCase() + " " + path;

        if (ADMIN_ENDPOINTS.contains(endpoint)) {
            return RagAudience.ADMIN;
        }

        if (path.startsWith("/api/v1/admin/")) {
            return RagAudience.ADMIN;
        }

        return RagAudience.SHARED;
    }
}