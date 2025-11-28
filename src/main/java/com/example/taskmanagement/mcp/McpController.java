package com.example.taskmanagement.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MCP HTTP Controller
 * Implements the Model Context Protocol over HTTP/SSE
 */
@Slf4j
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpServerConfiguration.McpToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    /**
     * Initialize the MCP connection
     */
    @PostMapping(value = "/initialize", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> initialize(@RequestBody Map<String, Object> request) {
        log.info("MCP Initialize request: {}", request);

        Map<String, Object> response = new HashMap<>();
        response.put("protocolVersion", "2024-11-05");
        response.put("serverInfo", Map.of(
                "name", "task-management",
                "version", "1.0.0"
        ));
        response.put("capabilities", Map.of(
                "tools", Map.of("listChanged", false),
                "resources", Map.of("subscribe", false, "listChanged", false),
                "prompts", Map.of("listChanged", false),
                "logging", Map.of()
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * Handle notifications/initialized
     */
    @PostMapping(value = "/notifications/initialized", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> notificationsInitialized() {
        log.info("MCP notifications/initialized");
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * List available tools
     */
    @PostMapping(value = "/tools/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> listTools() {
        log.info("MCP tools/list request");

        List<Map<String, Object>> tools = toolRegistry.getAllTools().values().stream()
                .map(tool -> {
                    Map<String, Object> toolDef = new HashMap<>();
                    toolDef.put("name", tool.name());
                    toolDef.put("description", tool.description());
                    toolDef.put("inputSchema", generateInputSchema(tool.name()));
                    return toolDef;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("tools", tools));
    }

    /**
     * Call a specific tool
     */
    @PostMapping(value = "/tools/call", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> callTool(@RequestBody Map<String, Object> request) {
        String toolName = (String) request.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) request.get("arguments");

        log.info("MCP tools/call request: tool={}, arguments={}", toolName, arguments);

        McpServerConfiguration.McpToolDefinition tool = toolRegistry.getTool(toolName);
        if (tool == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Tool not found: " + toolName
            ));
        }

        try {
            // Convert arguments to the appropriate request type
            Object input = convertArguments(toolName, arguments);
            Object result = tool.function().apply(input);

            List<Map<String, Object>> content = new ArrayList<>();
            content.add(Map.of(
                    "type", "text",
                    "text", objectMapper.writeValueAsString(result)
            ));

            return ResponseEntity.ok(Map.of(
                    "content", content,
                    "isError", false
            ));
        } catch (Exception e) {
            log.error("Error calling tool {}: {}", toolName, e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "content", List.of(Map.of(
                            "type", "text",
                            "text", "Error: " + e.getMessage()
                    )),
                    "isError", true
            ));
        }
    }

    /**
     * Ping endpoint for health check
     */
    @GetMapping(value = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Server info endpoint
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> serverInfo() {
        return ResponseEntity.ok(Map.of(
                "name", "task-management",
                "version", "1.0.0",
                "protocol", "mcp",
                "capabilities", Map.of("tools", true)
        ));
    }

    /**
     * Generate input schema for a tool
     */
    private Map<String, Object> generateInputSchema(String toolName) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();

        // Define schemas for each tool
        switch (toolName) {
            case "listProjects":
                // No parameters needed
                break;
            case "getProject":
                properties.put("id", Map.of("type", "number", "description", "Project ID"));
                required.add("id");
                break;
            case "createProject":
                properties.put("name", Map.of("type", "string", "description", "Project name"));
                properties.put("description", Map.of("type", "string", "description", "Project description"));
                required.add("name");
                break;
            case "updateProject":
                properties.put("id", Map.of("type", "number", "description", "Project ID"));
                properties.put("name", Map.of("type", "string", "description", "New project name"));
                properties.put("description", Map.of("type", "string", "description", "New project description"));
                required.add("id");
                required.add("name");
                break;
            case "deleteProject":
                properties.put("id", Map.of("type", "number", "description", "Project ID to delete"));
                required.add("id");
                break;
            case "listTasks":
                properties.put("projectId", Map.of("type", "number", "description", "Project ID"));
                required.add("projectId");
                break;
            case "getTask":
                properties.put("id", Map.of("type", "number", "description", "Task ID"));
                required.add("id");
                break;
            case "createTask":
                properties.put("projectId", Map.of("type", "number", "description", "Project ID"));
                properties.put("title", Map.of("type", "string", "description", "Task title"));
                properties.put("description", Map.of("type", "string", "description", "Task description"));
                properties.put("status", Map.of("type", "string", "description", "Task status (TODO, IN_PROGRESS, DONE)"));
                required.add("projectId");
                required.add("title");
                break;
            case "updateTask":
                properties.put("id", Map.of("type", "number", "description", "Task ID"));
                properties.put("title", Map.of("type", "string", "description", "New task title"));
                properties.put("description", Map.of("type", "string", "description", "New task description"));
                properties.put("status", Map.of("type", "string", "description", "New task status"));
                required.add("id");
                required.add("title");
                break;
            case "deleteTask":
                properties.put("id", Map.of("type", "number", "description", "Task ID to delete"));
                required.add("id");
                break;
            case "searchTasksByStatus":
                properties.put("status", Map.of("type", "string", "description", "Status to search for (TODO, IN_PROGRESS, DONE)"));
                required.add("status");
                break;
        }

        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }

        return schema;
    }

    /**
     * Convert raw arguments map to typed request object
     */
    private Object convertArguments(String toolName, Map<String, Object> arguments) {
        if (arguments == null) {
            arguments = new HashMap<>();
        }

        return switch (toolName) {
            case "listProjects" -> new McpToolsConfiguration.ListProjectsRequest();
            case "getProject" -> new McpToolsConfiguration.GetProjectRequest(
                    getLong(arguments, "id")
            );
            case "createProject" -> new McpToolsConfiguration.CreateProjectRequest(
                    (String) arguments.get("name"),
                    (String) arguments.get("description")
            );
            case "updateProject" -> new McpToolsConfiguration.UpdateProjectRequest(
                    getLong(arguments, "id"),
                    (String) arguments.get("name"),
                    (String) arguments.get("description")
            );
            case "deleteProject" -> new McpToolsConfiguration.DeleteProjectRequest(
                    getLong(arguments, "id")
            );
            case "listTasks" -> new McpToolsConfiguration.ListTasksRequest(
                    getLong(arguments, "projectId")
            );
            case "getTask" -> new McpToolsConfiguration.GetTaskRequest(
                    getLong(arguments, "id")
            );
            case "createTask" -> new McpToolsConfiguration.CreateTaskRequest(
                    getLong(arguments, "projectId"),
                    (String) arguments.get("title"),
                    (String) arguments.get("description"),
                    (String) arguments.get("status")
            );
            case "updateTask" -> new McpToolsConfiguration.UpdateTaskRequest(
                    getLong(arguments, "id"),
                    (String) arguments.get("title"),
                    (String) arguments.get("description"),
                    (String) arguments.get("status")
            );
            case "deleteTask" -> new McpToolsConfiguration.DeleteTaskRequest(
                    getLong(arguments, "id")
            );
            case "searchTasksByStatus" -> new McpToolsConfiguration.SearchTasksByStatusRequest(
                    (String) arguments.get("status")
            );
            default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
        };
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }
}

