package com.example.taskmanagement.mcp;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping(value = "/mcp/rpc")
@RequiredArgsConstructor
public class McpHttpController {
    private final McpDispatcher dispatcher;
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final AtomicLong eventIdCounter = new AtomicLong(0);

    /**
     * SSE endpoint for MCP protocol - used by MCP SDK clients
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sseEndpoint() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        String emitterId = String.valueOf(System.currentTimeMillis());

        emitters.put(emitterId, emitter);

        emitter.onCompletion(() -> emitters.remove(emitterId));
        emitter.onTimeout(() -> emitters.remove(emitterId));
        emitter.onError((e) -> emitters.remove(emitterId));

        try {
            // Send initial connection established event
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                .id(String.valueOf(eventIdCounter.incrementAndGet()))
                .name("endpoint")
                .data(Map.of(
                    "endpoint", "/mcp/rpc",
                    "capabilities", Map.of(
                        "acceptsStreamingMessages", true
                    )
                ));
            emitter.send(event);
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handle(
            @RequestBody JsonRpcRequest request,
            @RequestHeader(value = "Accept", required = false) String acceptHeader) {
        try {
            Object result = dispatcher.dispatch(request.getMethod(), request.getParams());
            JsonRpcResponse response = JsonRpcResponse.success(request.getId(), result);

            // Support both regular JSON and SSE responses
            if (acceptHeader != null && acceptHeader.contains("text/event-stream")) {
                return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body("data: " + toJson(response) + "\n\n");
            }

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonRpcResponse.error(request.getId(), -32601, iae.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonRpcResponse.error(request.getId(), -32603, e.getMessage(), null));
        }
    }

    private String toJson(Object obj) {
        // Simple JSON serialization - in production use Jackson properly
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    // Provide a simple GET response so clients that probe via GET don't get 405 or 406
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> serverInfo = Map.of(
                "name", "task-management",
                "protocol", "MCP",
                "rpc", "/mcp/rpc",
                "methodsEndpoint", "/mcp/rpc/methods"
        );
        Map<String, Object> body = Map.of(
                "serverInfo", serverInfo
        );
        return ResponseEntity.ok(body);
    }

    // Allow OPTIONS requests for preflight/probes
    @RequestMapping(method = RequestMethod.OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> options() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Allow", "POST,GET,OPTIONS");
        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/methods", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> listMethods() {
        return Map.of(
                "methods", new String[]{
                        "initialize",
                        "ping","listProjects","getProject","createProject","updateProject","deleteProject",
                        "listTasks","getTask","createTask","updateTask","deleteTask","searchTasksByStatus"
                }
        );
    }
}
