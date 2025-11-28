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
     * List available prompts
     */
    @PostMapping(value = "/prompts/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> listPrompts() {
        log.info("MCP prompts/list request");

        List<Map<String, Object>> prompts = Arrays.asList(
            Map.of(
                "name", "project_summary",
                "description", "Generate a comprehensive summary of a project and its tasks",
                "arguments", List.of(
                    Map.of(
                        "name", "projectId",
                        "description", "The ID of the project to summarize",
                        "required", true
                    )
                )
            ),
            Map.of(
                "name", "task_report",
                "description", "Generate a detailed report of tasks by status",
                "arguments", List.of(
                    Map.of(
                        "name", "status",
                        "description", "Task status: TODO, IN_PROGRESS, or DONE",
                        "required", false
                    )
                )
            ),
            Map.of(
                "name", "project_planning",
                "description", "Help plan a new project with suggested tasks",
                "arguments", List.of(
                    Map.of(
                        "name", "projectName",
                        "description", "Name of the project to plan",
                        "required", true
                    )
                )
            )
        );

        return ResponseEntity.ok(Map.of("prompts", prompts));
    }

    /**
     * Get a specific prompt
     */
    @PostMapping(value = "/prompts/get", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getPrompt(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) request.getOrDefault("arguments", Map.of());

        log.info("MCP prompts/get request: name={}, arguments={}", name, arguments);

        String description;
        List<Map<String, Object>> messages = new ArrayList<>();

        switch (name) {
            case "project_summary" -> {
                description = "Generates a comprehensive summary of a project";
                Long projectId = getLong(arguments, "projectId");
                messages.add(Map.of(
                    "role", "user",
                    "content", Map.of(
                        "type", "text",
                        "text", String.format(
                            "Please provide a comprehensive summary of project ID %d including " +
                            "all tasks, their current status, and any notable insights.",
                            projectId
                        )
                    )
                ));
            }
            case "task_report" -> {
                description = "Generates a report of tasks by status";
                String status = (String) arguments.getOrDefault("status", "TODO");
                messages.add(Map.of(
                    "role", "user",
                    "content", Map.of(
                        "type", "text",
                        "text", String.format(
                            "Generate a detailed report of all tasks with status '%s'. " +
                            "Include task titles, descriptions, and the projects they belong to.",
                            status
                        )
                    )
                ));
            }
            case "project_planning" -> {
                description = "Helps plan a new project";
                String projectName = (String) arguments.get("projectName");
                messages.add(Map.of(
                    "role", "user",
                    "content", Map.of(
                        "type", "text",
                        "text", String.format(
                            "I'm planning a new project called '%s'. Can you help me break it down " +
                            "into manageable tasks? Please suggest appropriate task titles, descriptions, " +
                            "and recommended statuses.",
                            projectName
                        )
                    )
                ));
            }
            default -> {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Unknown prompt: " + name
                ));
            }
        }

        return ResponseEntity.ok(Map.of(
            "description", description,
            "messages", messages
        ));
    }

    /**
     * List available resources
     */
    @PostMapping(value = "/resources/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> listResources() {
        log.info("MCP resources/list request");

        List<Map<String, Object>> resources = Arrays.asList(
            Map.of(
                "uri", "task://projects",
                "name", "All Projects",
                "description", "List of all projects in the system",
                "mimeType", "application/json"
            ),
            Map.of(
                "uri", "task://tasks/todo",
                "name", "TODO Tasks",
                "description", "All tasks with TODO status",
                "mimeType", "application/json"
            ),
            Map.of(
                "uri", "task://tasks/in_progress",
                "name", "In Progress Tasks",
                "description", "All tasks currently in progress",
                "mimeType", "application/json"
            ),
            Map.of(
                "uri", "task://tasks/done",
                "name", "Completed Tasks",
                "description", "All completed tasks",
                "mimeType", "application/json"
            )
        );

        return ResponseEntity.ok(Map.of("resources", resources));
    }

    /**
     * Read a specific resource
     */
    @PostMapping(value = "/resources/read", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> readResource(@RequestBody Map<String, Object> request) {
        String uri = (String) request.get("uri");
        log.info("MCP resources/read request: uri={}", uri);

        try {
            String content;
            if (uri.equals("task://projects")) {
                McpToolsConfiguration.ListProjectsRequest req = new McpToolsConfiguration.ListProjectsRequest();
                Object result = toolRegistry.getTool("listProjects").function().apply(req);
                content = objectMapper.writeValueAsString(result);
            } else if (uri.startsWith("task://tasks/")) {
                String status = uri.substring("task://tasks/".length()).toUpperCase();
                McpToolsConfiguration.SearchTasksByStatusRequest req =
                    new McpToolsConfiguration.SearchTasksByStatusRequest(status);
                Object result = toolRegistry.getTool("searchTasksByStatus").function().apply(req);
                content = objectMapper.writeValueAsString(result);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Unknown resource URI: " + uri
                ));
            }

            return ResponseEntity.ok(Map.of(
                "contents", List.of(Map.of(
                    "uri", uri,
                    "mimeType", "application/json",
                    "text", content
                ))
            ));
        } catch (Exception e) {
            log.error("Error reading resource {}: {}", uri, e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "contents", List.of(Map.of(
                    "uri", uri,
                    "mimeType", "text/plain",
                    "text", "Error: " + e.getMessage()
                ))
            ));
        }
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

