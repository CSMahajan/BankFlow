package com.bankflow.ai.mcp;

import com.bankflow.ai.AiAudience;
import com.bankflow.ai.AiAudienceResolver;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

@Configuration
public class McpServerConfig {

    public static final String AUTHENTICATION = "authentication";
    public static final String AUDIENCE = "audience";
    public static final String TRACE_ID = "traceId";

    @Bean
    public HttpServletStreamableServerTransportProvider mcpTransportProvider(
            AiAudienceResolver audienceResolver) {

        return HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(McpJsonDefaults.getMapper())
                .mcpEndpoint("/mcp")
                .contextExtractor((HttpServletRequest request) -> {

                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                    AiAudience audience = audienceResolver.resolve();

                    String traceId = (String) request.getAttribute(TRACE_ID);

                    if (traceId == null || traceId.isBlank()) {
                        traceId = "unknown";
                    }

                    return McpTransportContext.create(
                            Map.of(
                                    AUTHENTICATION,
                                    authentication,
                                    AUDIENCE,
                                    audience,
                                    TRACE_ID,
                                    traceId
                            )
                    );
                })
                .build();
    }

    @Bean
    public McpSyncServer mcpServer(
            HttpServletStreamableServerTransportProvider transportProvider,
            McpAiToolAdapter toolAdapter) {

        var toolSpecifications =
                toolAdapter.createToolSpecifications();

        return McpServer.sync(transportProvider)
                .serverInfo("bankflow-mcp-server", "1.0.0")
                .capabilities(
                        ServerCapabilities.builder()
                                /*
                                 * Role-specific tools/list is evaluated per request.
                                 * Do not broadcast tools/list_changed because that
                                 * notification has no request context.
                                 */
                                .tools(false)
                                .build()
                )
                .tools(toolSpecifications)
                .addToolFilter((context, tool) -> {

                    Object audienceValue =
                            context.get(AUDIENCE);

                    if (!(audienceValue instanceof AiAudience audience)) {
                        return false;
                    }

                    return toolAdapter.isVisible(
                            tool,
                            audience
                    );
                })
                .build();
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
