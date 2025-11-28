package com.example.taskmanagement.mcp;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mcp/rpc")
@RequiredArgsConstructor
public class McpHttpController {
    private final McpDispatcher dispatcher;

    @PostMapping
    public ResponseEntity<?> handle(@RequestBody JsonRpcRequest request) {
        try {
            Object result = dispatcher.dispatch(request.getMethod(), request.getParams());
            return ResponseEntity.ok(JsonRpcResponse.success(request.getId(), result));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(JsonRpcResponse.error(request.getId(), -32601, iae.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(JsonRpcResponse.error(request.getId(), -32603, e.getMessage(), null));
        }
    }

    @GetMapping("/methods")
    public Map<String, Object> listMethods() {
        return Map.of(
                "methods", new String[]{
                        "ping","listProjects","getProject","createProject","updateProject","deleteProject",
                        "listTasks","getTask","createTask","updateTask","deleteTask","searchTasksByStatus"
                }
        );
    }
}

