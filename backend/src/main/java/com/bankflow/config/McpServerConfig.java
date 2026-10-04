package com.bankflow.config;

import com.bankflow.dto.TransactionResponse;
import com.bankflow.entity.Transaction;
import com.bankflow.service.AccountService;
import com.bankflow.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Configuration
public class McpServerConfig {

    @Bean
    public HttpServletStreamableServerTransportProvider mcpTransportProvider() {

        return HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(McpJsonDefaults.getMapper())
                .mcpEndpoint("/mcp")
                .contextExtractor((HttpServletRequest request) -> {

                    Authentication authentication =
                            SecurityContextHolder.getContext().getAuthentication();

                    return McpTransportContext.create(
                            Map.of("authentication", authentication)
                    );
                })
                .build();
    }

    @Bean
    public McpSyncServer mcpServer(
            HttpServletStreamableServerTransportProvider transportProvider,
            AccountService accountService,
            TransactionService transactionService,
            ObjectMapper objectMapper) {

        var tool = McpServerFeatures.SyncToolSpecification.builder()
                .tool(
                        Tool.builder(
                                        "get_my_accounts",
                                        Map.of(
                                                "type", "object"
                                        )
                                )
                                .description("""
                                        Returns the authenticated customer's bank accounts.
                                        
                                        The customer is determined by the server-side
                                        authentication context. This tool takes no parameters.
                                        """)
                                .build()
                )
                .callHandler((exchange, request) -> {

                    Authentication authentication =
                            (Authentication) exchange.transportContext()
                                    .get("authentication");

                    SecurityContext previousContext =
                            SecurityContextHolder.getContext();

                    try {
                        SecurityContext context =
                                SecurityContextHolder.createEmptyContext();

                        context.setAuthentication(authentication);
                        SecurityContextHolder.setContext(context);

                        List<?> accounts = accountService.getMyAccounts();

                        String result;

                        try {
                            result = objectMapper.writeValueAsString(accounts);
                        } catch (Exception e) {
                            throw new RuntimeException(
                                    "Failed to serialize account data",
                                    e
                            );
                        }

                        return CallToolResult.builder()
                                .content(
                                        List.of(
                                                new TextContent(result)
                                        )
                                )
                                .build();

                    } finally {
                        SecurityContextHolder.setContext(previousContext);
                    }
                })
                .build();

        var transactionsTool =
                McpServerFeatures.SyncToolSpecification.builder()
                        .tool(
                                Tool.builder(
                                                "get_my_transactions",
                                                Map.of(
                                                        "type", "object",
                                                        "properties", Map.of(
                                                                "accountNumber", Map.of(
                                                                        "type", "string",
                                                                        "description", "Optional bank account number"
                                                                ),
                                                                "type", Map.of(
                                                                        "type", "string",
                                                                        "description", "Optional transaction type: CREDIT or DEBIT"
                                                                ),
                                                                "startDate", Map.of(
                                                                        "type", "string",
                                                                        "description", "Optional start date in yyyy-MM-dd format"
                                                                ),
                                                                "endDate", Map.of(
                                                                        "type", "string",
                                                                        "description", "Optional end date in yyyy-MM-dd format"
                                                                ),
                                                                "search", Map.of(
                                                                        "type", "string",
                                                                        "description", "Optional search text for transaction description"
                                                                ),
                                                                "page", Map.of(
                                                                        "type", "integer",
                                                                        "description", "Optional zero-based page number"
                                                                ),
                                                                "size", Map.of(
                                                                        "type", "integer",
                                                                        "description", "Optional page size, maximum 100"
                                                                )
                                                        )
                                                )
                                        )
                                        .description("""
                                        Returns the authenticated customer's transactions.

                                        The customer is determined by the server-side
                                        authentication context.

                                        Optional filters include account number,
                                        transaction type, date range, search text,
                                        page number and page size.
                                        """)
                                        .build()
                        )
                        .callHandler((exchange, request) -> {

                            Authentication authentication =
                                    (Authentication) exchange.transportContext().get("authentication");

                            SecurityContext previousContext =
                                    SecurityContextHolder.getContext();

                            try {
                                SecurityContext context =
                                        SecurityContextHolder.createEmptyContext();

                                context.setAuthentication(authentication);
                                SecurityContextHolder.setContext(context);

                                Map<String, Object> arguments = request.arguments();

                                String accountNumber =
                                        (String) arguments.get("accountNumber");

                                String typeValue =
                                        (String) arguments.get("type");

                                String startDateValue =
                                        (String) arguments.get("startDate");

                                String endDateValue =
                                        (String) arguments.get("endDate");

                                String search =
                                        (String) arguments.get("search");

                                Integer page =
                                        arguments.get("page") == null
                                                ? 0
                                                : ((Number) arguments.get("page")).intValue();

                                Integer size =
                                        arguments.get("size") == null
                                                ? 20
                                                : ((Number) arguments.get("size")).intValue();

                                Transaction.TransactionType type =
                                        typeValue == null
                                                ? null
                                                : Transaction.TransactionType.valueOf(
                                                typeValue.toUpperCase(Locale.ROOT)
                                        );

                                LocalDate startDate =
                                        startDateValue == null
                                                ? null
                                                : LocalDate.parse(startDateValue);

                                LocalDate endDate =
                                        endDateValue == null
                                                ? null
                                                : LocalDate.parse(endDateValue);

                                Pageable pageable =
                                        PageRequest.of(
                                                page,
                                                size,
                                                Sort.by(
                                                        Sort.Direction.DESC,
                                                        "transactionDate"
                                                )
                                        );

                                Page<TransactionResponse> transactions =
                                        transactionService.getMyTransactions(
                                                accountNumber,
                                                type,
                                                startDate,
                                                endDate,
                                                search,
                                                pageable
                                        );

                                String result;

                                try {
                                    result = objectMapper.writeValueAsString(transactions);
                                } catch (Exception e) {
                                    throw new RuntimeException(
                                            "Failed to serialize transaction data",
                                            e
                                    );
                                }

                                return CallToolResult.builder()
                                        .content(
                                                List.of(
                                                        new TextContent(result)
                                                )
                                        )
                                        .build();

                            } finally {
                                SecurityContextHolder.setContext(previousContext);
                            }
                        })
                        .build();

        McpSyncServer server = McpServer.sync(transportProvider)
                .serverInfo("bankflow-mcp-server", "1.0.0")
                .capabilities(
                        ServerCapabilities.builder()
                                .tools(false)
                                .build()
                )
                .build();

        server.addTool(tool);
        server.addTool(transactionsTool);
        return server;
    }

    @Bean
    public ServletRegistrationBean<?> mcpServlet(
            HttpServletStreamableServerTransportProvider transportProvider) {

        return new ServletRegistrationBean<>(
                transportProvider,
                "/mcp/*"
        );
    }
}