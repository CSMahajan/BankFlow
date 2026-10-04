package com.bankflow.mcpclient;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class BankFlowMcpClient {

    private static final Logger log =
            LoggerFactory.getLogger(BankFlowMcpClient.class);

    private static final String BANKFLOW_BASE_URL =
            System.getenv().getOrDefault(
                    "BANKFLOW_BASE_URL",
                    "http://localhost:8080"
            );

    public static void main(String[] args) throws Exception {

        log.info("Starting BankFlow OAuth flow...");
        log.info("BankFlow base URL: {}", BANKFLOW_BASE_URL);

        OAuthClient oauthClient =
                new OAuthClient();

        String accessToken =
                oauthClient.authorize();

        log.info("OAuth authorization successful.");

        HttpClientStreamableHttpTransport transport =
                HttpClientStreamableHttpTransport
                        .builder(BANKFLOW_BASE_URL + "/mcp")
                        .jsonMapper(McpJsonDefaults.getMapper())
                        .httpRequestCustomizer(
                                (request, method, uri, requestId, context) ->
                                        request.header(
                                                "Authorization",
                                                "Bearer " + accessToken
                                        )
                        )
                        .build();

        McpSyncClient client =
                McpClient.sync(transport)
                        .build();

        try {

            log.info("Connecting to BankFlow MCP server...");

            McpSchema.InitializeResult initializeResult =
                    client.initialize();

            log.info("=== MCP INITIALIZED ===");
            log.info("{}", initializeResult);

            log.info("=== AVAILABLE TOOLS ===");

            McpSchema.ListToolsResult tools =
                    client.listTools();

            tools.tools().forEach(tool -> {

                log.info(
                        "Tool: {}\nDescription: {}\nInput schema: {}\nProperties: {}\nRequired: {}\n",
                        tool.name(),
                        tool.description(),
                        tool.inputSchema(),
                        tool.inputSchema().get("properties"),
                        tool.inputSchema().get("required")
                );
            });

            McpSchema.Tool accountsTool =
                    tools.tools().stream()
                            .filter(tool ->
                                    tool.name()
                                            .equals("get_my_accounts"))
                            .findFirst()
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "get_my_accounts tool was not discovered"
                                    )
                            );

            McpSchema.Tool transactionsTool =
                    tools.tools().stream()
                            .filter(tool ->
                                    tool.name()
                                            .equals("get_my_transactions"))
                            .findFirst()
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "get_my_transactions tool was not discovered"
                                    )
                            );

            log.info("=== CALLING get_my_accounts ===");

            McpSchema.CallToolResult result =
                    client.callTool(
                            new McpSchema.CallToolRequest(
                                    accountsTool.name(),
                                    Map.of()
                            )
                    );

            log.info("get_my_accounts result: {}", result);

            log.info("=== CALLING get_my_transactions ===");

            McpSchema.CallToolResult transactionsResult =
                    client.callTool(
                            new McpSchema.CallToolRequest(
                                    transactionsTool.name(),
                                    Map.of(
                                            "accountNumber",
                                            "BF7441043591",
                                            "type",
                                            "DEBIT",
                                            "size",
                                            5
                                    )
                            )
                    );

            log.info(
                    "get_my_transactions result: {}",
                    transactionsResult
            );

        } finally {

            client.closeGracefully();

            log.info("BankFlow MCP client closed.");
        }
    }
}