package com.example.taskmanagement.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpWebSocketHandler extends TextWebSocketHandler {
    private final McpDispatcher dispatcher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("MCP WebSocket connected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        try {
            JsonRpcRequest req = objectMapper.readValue(payload, JsonRpcRequest.class);
            Object result = dispatcher.dispatch(req.getMethod(), req.getParams());
            JsonRpcResponse resp = JsonRpcResponse.success(req.getId(), result);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
        } catch (Exception e) {
            log.error("Error handling MCP message", e);
            JsonRpcResponse error = JsonRpcResponse.error("null", -32603, e.getMessage(), null);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        }
    }
}

