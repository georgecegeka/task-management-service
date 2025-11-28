package com.example.taskmanagement.mcp;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Ensures normal REST API only served on 8080 and MCP endpoints only on 8081.
 * Requests hitting the wrong port get 404 to avoid accidental cross-access.
 */
@Component
@Order(1)
public class McpPortIsolationFilter implements Filter {

    private static final int NORMAL_PORT = 8080;
    private static final int MCP_PORT = 8081;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpReq) || !(response instanceof HttpServletResponse httpResp)) {
            chain.doFilter(request, response);
            return;
        }
        int localPort = httpReq.getLocalPort();
        String path = httpReq.getRequestURI();

        boolean isMcpPath = path.startsWith("/mcp/") || path.equals("/mcp") || path.startsWith("/mcpws");

        // If request came on 8080 but path is MCP -> block
        if (localPort == NORMAL_PORT && isMcpPath) {
            httpResp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // If request came on 8081 but path is NOT MCP -> block
        if (localPort == MCP_PORT && !isMcpPath) {
            httpResp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        chain.doFilter(request, response);
    }
}

