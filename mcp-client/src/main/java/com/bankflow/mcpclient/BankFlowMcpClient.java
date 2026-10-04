package com.bankflow.mcpclient;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class BankFlowMcpClient {

    private static final Logger LOG =
            LoggerFactory.getLogger(BankFlowMcpClient.class);

    private static final String DEFAULT_BANKFLOW_BASE_URL =
            "http://localhost:8080";

    private static final String MCP_ENDPOINT = "/mcp";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final String ACCOUNTS_TOOL = "get_my_accounts";
    private static final String TRANSACTIONS_TOOL = "get_my_transactions";

    private static final String TRANSACTION_TYPE_ENV =
            "BANKFLOW_TRANSACTION_TYPE";

    private static final String TRANSACTION_SIZE_ENV =
            "BANKFLOW_TRANSACTION_SIZE";

    private static final String DEFAULT_TRANSACTION_TYPE = "DEBIT";
    private static final int DEFAULT_TRANSACTION_SIZE = 5;

    private static final String BANKFLOW_BASE_URL =
            System.getenv()
                    .getOrDefault(
                            "BANKFLOW_BASE_URL",
                            DEFAULT_BANKFLOW_BASE_URL
                    );

    private BankFlowMcpClient() {
        // Utility class
    }

    public static void main(String[] args) {

        LOG.info("Starting BankFlow OAuth flow...");
        LOG.info("BankFlow base URL: {}", BANKFLOW_BASE_URL);

        try {
            run();
        } catch (Exception exception) {
            LOG.error(
                    "BankFlow MCP client failed.",
                    exception
            );
        }
    }

    private static void run() {

        OAuthClient oauthClient = new OAuthClient();

        String accessToken = oauthClient.authorize();

        LOG.info("OAuth authorization successful.");

        HttpClientStreamableHttpTransport transport =
                createTransport(accessToken);

        McpSyncClient client =
                McpClient.sync(transport)
                        .build();

        try {
            executeMcpOperations(client);
        } finally {
            client.closeGracefully();
            LOG.info("BankFlow MCP client closed.");
        }
    }

    private static HttpClientStreamableHttpTransport createTransport(
            String accessToken) {

        return HttpClientStreamableHttpTransport
                .builder(BANKFLOW_BASE_URL + MCP_ENDPOINT)
                .jsonMapper(McpJsonDefaults.getMapper())
                .httpRequestCustomizer(
                        (request, method, uri, requestId, context) ->
                                request.header(
                                        AUTHORIZATION_HEADER,
                                        BEARER_PREFIX + accessToken
                                )
                )
                .build();
    }

    private static void executeMcpOperations(McpSyncClient client) {

        LOG.info("Connecting to BankFlow MCP server...");

        client.initialize();

        LOG.info("=== MCP INITIALIZED ===");
        LOG.info("MCP server initialized successfully.");

        McpSchema.ListToolsResult toolsResult =
                client.listTools();

        logAvailableTools(toolsResult);

        McpSchema.Tool accountsTool =
                findTool(toolsResult, ACCOUNTS_TOOL);

        McpSchema.Tool transactionsTool =
                findTool(toolsResult, TRANSACTIONS_TOOL);

        callAccountsTool(client, accountsTool);
        callTransactionsTool(client, transactionsTool);
    }

    private static void logAvailableTools(
            McpSchema.ListToolsResult toolsResult) {

        LOG.info("=== AVAILABLE TOOLS ===");

        toolsResult.tools().forEach(tool ->
                LOG.info(
                        "Tool discovered: {} - {}",
                        tool.name(),
                        tool.description()
                )
        );
    }

    private static McpSchema.Tool findTool(
            McpSchema.ListToolsResult toolsResult,
            String toolName) {

        return toolsResult.tools()
                .stream()
                .filter(tool -> toolName.equals(tool.name()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Required MCP tool was not discovered: "
                                        + toolName
                        )
                );
    }

    private static void callAccountsTool(
            McpSyncClient client,
            McpSchema.Tool accountsTool) {

        LOG.info("=== CALLING {} ===", accountsTool.name());

        McpSchema.CallToolResult result =
                client.callTool(
                        new McpSchema.CallToolRequest(
                                accountsTool.name(),
                                Map.of()
                        )
                );

        LOG.info(
                "{} completed successfully.",
                accountsTool.name()
        );

        LOG.debug(
                "{} response: {}",
                accountsTool.name(),
                result
        );
    }

    private static void callTransactionsTool(
            McpSyncClient client,
            McpSchema.Tool transactionsTool) {

        String transactionType =
                System.getenv()
                        .getOrDefault(
                                TRANSACTION_TYPE_ENV,
                                DEFAULT_TRANSACTION_TYPE
                        );

        int transactionSize = getTransactionSize();

        LOG.info("=== CALLING {} ===", transactionsTool.name());
        LOG.info(
                "Transaction query: type={}, size={}",
                transactionType,
                transactionSize
        );

        McpSchema.CallToolResult result =
                client.callTool(
                        new McpSchema.CallToolRequest(
                                transactionsTool.name(),
                                Map.of(
                                        "type",
                                        transactionType,
                                        "size",
                                        transactionSize
                                )
                        )
                );

        LOG.info(
                "{} completed successfully.",
                transactionsTool.name()
        );

        LOG.debug(
                "{} response: {}",
                transactionsTool.name(),
                result
        );
    }

    private static int getTransactionSize() {

        String configuredSize =
                System.getenv(TRANSACTION_SIZE_ENV);

        if (configuredSize == null || configuredSize.isBlank()) {
            return DEFAULT_TRANSACTION_SIZE;
        }

        try {
            int size = Integer.parseInt(configuredSize);

            if (size <= 0) {
                throw new IllegalStateException(
                        TRANSACTION_SIZE_ENV
                                + " must be greater than zero"
                );
            }

            return size;

        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    TRANSACTION_SIZE_ENV
                            + " must contain a valid integer",
                    exception
            );
        }
    }
}