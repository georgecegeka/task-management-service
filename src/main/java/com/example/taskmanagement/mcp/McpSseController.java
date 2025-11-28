package com.example.taskmanagement.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;

/**
 * MCP SSE Controller for Server-Sent Events streaming
 * Implements the Model Context Protocol over SSE transport
 */
@Slf4j
@RestController
@RequestMapping("/mcp/sse")
@RequiredArgsConstructor
public class McpSseController {

    private final McpServerConfiguration.McpToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    /**
     * SSE endpoint for MCP messages
     * Handles the full MCP protocol lifecycle over SSE
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> sseEndpoint() {
        log.info("MCP SSE connection established");

        // Create the initial message
        Flux<ServerSentEvent<String>> initialMessage = Flux.defer(() -> {
            try {
                Map<String, Object> endpointInfo = Map.of(
                    "jsonrpc", "2.0",
                    "method", "endpoint",
                    "params", Map.of(
                        "endpoint", "/mcp/sse/message"
                    )
                );
                
                String json = objectMapper.writeValueAsString(endpointInfo);
                log.info("Sent endpoint info");

                return Flux.just(ServerSentEvent.<String>builder()
                    .data(json)
                    .build());
            } catch (Exception e) {
                log.error("Error sending endpoint info", e);
                return Flux.error(e);
            }
        });

        // Create periodic ping messages
        Flux<ServerSentEvent<String>> pings = Flux.interval(Duration.ofSeconds(30))
            .map(tick -> {
                try {
                    Map<String, Object> ping = Map.of(
                        "jsonrpc", "2.0",
                        "method", "ping"
                    );
                    String json = objectMapper.writeValueAsString(ping);
                    return ServerSentEvent.<String>builder()
                        .event("ping")
                        .data(json)
                        .build();
                } catch (Exception e) {
                    log.error("Error creating ping", e);
                    return ServerSentEvent.<String>builder()
                        .event("error")
                        .data("ping error")
                        .build();
                }
            });

        // Combine initial message and pings
        return Flux.concat(initialMessage, pings)
            .doOnComplete(() -> log.info("MCP SSE connection closed"))
            .doOnError(error -> log.error("MCP SSE error", error));
    }

    /**
     * Handle MCP messages sent to the message endpoint
     */
    @PostMapping(value = "/message", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> handleMessage(@RequestBody Map<String, Object> message) {
        log.info("MCP SSE message received: {}", message);

        String method = (String) message.get("method");
        Object id = message.get("id");
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) message.get("params");

        try {
            Map<String, Object> result = processMethod(method, params);
            
            Map<String, Object> response = new HashMap<>();
            response.put("jsonrpc", "2.0");
            response.put("id", id);
            response.put("result", result);
            
            return response;
        } catch (Exception e) {
            log.error("Error processing method {}: {}", method, e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("code", -32603);
            error.put("message", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("jsonrpc", "2.0");
            response.put("id", id);
            response.put("error", error);
            
            return response;
        }
    }

    /**
     * Process MCP method calls
     */
    private Map<String, Object> processMethod(String method, Map<String, Object> params) {
        return switch (method) {
            case "initialize" -> handleInitialize(params);
            case "tools/list" -> handleToolsList();
            case "tools/call" -> handleToolsCall(params);
            case "prompts/list" -> handlePromptsList();
            case "prompts/get" -> handlePromptsGet(params);
            case "resources/list" -> handleResourcesList();
            case "resources/read" -> handleResourcesRead(params);
            default -> throw new IllegalArgumentException("Unknown method: " + method);
        };
    }

    private Map<String, Object> handleInitialize(Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        response.put("protocolVersion", "2024-11-05");
        response.put("serverInfo", Map.of(
            "name", "task-management-mcp",
            "version", "1.0.0"
        ));
        response.put("capabilities", Map.of(
            "tools", Map.of("listChanged", false),
            "resources", Map.of(
                "subscribe", false,
                "listChanged", false
            ),
            "prompts", Map.of("listChanged", false),
            "logging", Map.of()
        ));
        return response;
    }

    private Map<String, Object> handleToolsList() {
        List<Map<String, Object>> tools = new ArrayList<>();
        
        toolRegistry.getAllTools().forEach((name, tool) -> {
            Map<String, Object> toolDef = new HashMap<>();
            toolDef.put("name", name);
            toolDef.put("description", tool.description());
            toolDef.put("inputSchema", generateInputSchema(name));
            tools.add(toolDef);
        });
        
        return Map.of("tools", tools);
    }

    private Map<String, Object> handleToolsCall(Map<String, Object> params) {
        String name = (String) params.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
        
        McpServerConfiguration.McpToolDefinition tool = toolRegistry.getTool(name);
        if (tool == null) {
            throw new IllegalArgumentException("Tool not found: " + name);
        }
        
        try {
            Object input = convertArguments(name, arguments);
            Object result = tool.function().apply(input);
            String resultJson = objectMapper.writeValueAsString(result);
            
            return Map.of(
                "content", List.of(Map.of(
                    "type", "text",
                    "text", resultJson
                )),
                "isError", false
            );
        } catch (Exception e) {
            throw new RuntimeException("Error calling tool: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> handlePromptsList() {
        List<Map<String, Object>> prompts = Arrays.asList(
            Map.of(
                "name", "project_summary",
                "description", "Generate a comprehensive summary of a project and its tasks"
            ),
            Map.of(
                "name", "task_report",
                "description", "Generate a detailed report of tasks by status"
            ),
            Map.of(
                "name", "project_planning",
                "description", "Help plan a new project with suggested tasks"
            )
        );
        
        return Map.of("prompts", prompts);
    }

    private Map<String, Object> handlePromptsGet(Map<String, Object> params) {
        String name = (String) params.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Map.of());
        
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
            default -> throw new IllegalArgumentException("Unknown prompt: " + name);
        }
        
        return Map.of(
            "description", description,
            "messages", messages
        );
    }

    private Map<String, Object> handleResourcesList() {
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
        
        return Map.of("resources", resources);
    }

    private Map<String, Object> handleResourcesRead(Map<String, Object> params) {
        String uri = (String) params.get("uri");
        
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
                throw new IllegalArgumentException("Unknown resource URI: " + uri);
            }
            
            return Map.of(
                "contents", List.of(Map.of(
                    "uri", uri,
                    "mimeType", "application/json",
                    "text", content
                ))
            );
        } catch (Exception e) {
            throw new RuntimeException("Error reading resource: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> generateInputSchema(String toolName) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();
        
        // Reuse the schema generation logic from McpController
        switch (toolName) {
            case "listProjects" -> {}
            case "getProject" -> {
                properties.put("id", Map.of("type", "number", "description", "Project ID"));
                required.add("id");
            }
            case "createProject" -> {
                properties.put("name", Map.of("type", "string", "description", "Project name"));
                properties.put("description", Map.of("type", "string", "description", "Project description"));
                required.add("name");
            }
            case "updateProject" -> {
                properties.put("id", Map.of("type", "number", "description", "Project ID"));
                properties.put("name", Map.of("type", "string", "description", "New project name"));
                properties.put("description", Map.of("type", "string", "description", "New project description"));
                required.add("id");
                required.add("name");
            }
            case "deleteProject" -> {
                properties.put("id", Map.of("type", "number", "description", "Project ID to delete"));
                required.add("id");
            }
            case "listTasks" -> {
                properties.put("projectId", Map.of("type", "number", "description", "Project ID"));
                required.add("projectId");
            }
            case "getTask" -> {
                properties.put("id", Map.of("type", "number", "description", "Task ID"));
                required.add("id");
            }
            case "createTask" -> {
                properties.put("projectId", Map.of("type", "number", "description", "Project ID"));
                properties.put("title", Map.of("type", "string", "description", "Task title"));
                properties.put("description", Map.of("type", "string", "description", "Task description"));
                properties.put("status", Map.of("type", "string", "description", "Task status (TODO, IN_PROGRESS, DONE)"));
                required.add("projectId");
                required.add("title");
            }
            case "updateTask" -> {
                properties.put("id", Map.of("type", "number", "description", "Task ID"));
                properties.put("title", Map.of("type", "string", "description", "New task title"));
                properties.put("description", Map.of("type", "string", "description", "New task description"));
                properties.put("status", Map.of("type", "string", "description", "New task status"));
                required.add("id");
                required.add("title");
            }
            case "deleteTask" -> {
                properties.put("id", Map.of("type", "number", "description", "Task ID to delete"));
                required.add("id");
            }
            case "searchTasksByStatus" -> {
                properties.put("status", Map.of("type", "string", "description", "Status to search for (TODO, IN_PROGRESS, DONE)"));
                required.add("status");
            }
        }
        
        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        
        return schema;
    }

    private Object convertArguments(String toolName, Map<String, Object> arguments) {
        if (arguments == null) {
            arguments = new HashMap<>();
        }
        
        return switch (toolName) {
            case "listProjects" -> new McpToolsConfiguration.ListProjectsRequest();
            case "getProject" -> new McpToolsConfiguration.GetProjectRequest(getLong(arguments, "id"));
            case "createProject" -> new McpToolsConfiguration.CreateProjectRequest(
                (String) arguments.get("name"),
                (String) arguments.get("description")
            );
            case "updateProject" -> new McpToolsConfiguration.UpdateProjectRequest(
                getLong(arguments, "id"),
                (String) arguments.get("name"),
                (String) arguments.get("description")
            );
            case "deleteProject" -> new McpToolsConfiguration.DeleteProjectRequest(getLong(arguments, "id"));
            case "listTasks" -> new McpToolsConfiguration.ListTasksRequest(getLong(arguments, "projectId"));
            case "getTask" -> new McpToolsConfiguration.GetTaskRequest(getLong(arguments, "id"));
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
            case "deleteTask" -> new McpToolsConfiguration.DeleteTaskRequest(getLong(arguments, "id"));
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

