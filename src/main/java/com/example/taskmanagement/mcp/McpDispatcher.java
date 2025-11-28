package com.example.taskmanagement.mcp;

import com.example.taskmanagement.model.Project;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.service.ProjectService;
import com.example.taskmanagement.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class McpDispatcher {
    private final ProjectService projectService;
    private final TaskService taskService;

    public Object dispatch(String method, Map<String, Object> params) {
        switch (method) {
            case "initialize":
                // MCP initialize response - must include serverInfo and capabilities
                return Map.of(
                        "protocolVersion", "2024-11-05",
                        "serverInfo", Map.of(
                                "name", "task-management",
                                "version", "0.1.0"
                        ),
                        "capabilities", Map.of(
                                // Advertise tools capability (list + call)
                                "tools", Map.of(
                                        "list", true,
                                        "call", true
                                ),
                                // Resources not used yet; set to false
                                "resources", Map.of(
                                        "list", false,
                                        "read", false
                                ),
                                // Prompts not used yet; set to false
                                "prompts", Map.of(
                                        "list", false,
                                        "get", false
                                )
                        )
                );
            case "notifications/initialized":
                // Respond to notifications/initialized with serverInfo as required by MCP client
                return Map.of(
                        "serverInfo", Map.of(
                                "name", "task-management",
                                "version", "0.1.0"
                        )
                );
            case "ping":
                return Map.of("pong", true, "timestamp", System.currentTimeMillis());
            case "listProjects":
                return projectService.getAllProjects();
            case "getProject":
                Long pid = asLong(params.get("id"));
                return projectService.getProjectById(pid).orElse(null);
            case "createProject":
                String name = (String) params.get("name");
                String description = (String) params.getOrDefault("description", null);
                Project p = Project.builder().name(name).description(description).build();
                return projectService.createProject(p);
            case "updateProject":
                Long upId = asLong(params.get("id"));
                String upName = (String) params.get("name");
                String upDesc = (String) params.get("description");
                Project updated = Project.builder().name(upName).description(upDesc).build();
                return projectService.updateProject(upId, updated);
            case "deleteProject":
                Long dpId = asLong(params.get("id"));
                projectService.deleteProject(dpId);
                return Map.of("deleted", dpId);
            case "listTasks":
                Long projectId = asLong(params.get("projectId"));
                return taskService.getTasksByProjectId(projectId);
            case "getTask":
                Long taskId = asLong(params.get("id"));
                return taskService.getTaskById(taskId).orElse(null);
            case "createTask":
                Long cProjectId = asLong(params.get("projectId"));
                String title = (String) params.get("title");
                String desc = (String) params.getOrDefault("description", null);
                String statusStr = (String) params.getOrDefault("status", "TODO");
                Task.Status status = Task.Status.valueOf(statusStr);
                Task t = Task.builder().title(title).description(desc).status(status).build();
                return taskService.createTask(cProjectId, t);
            case "updateTask":
                Long utId = asLong(params.get("id"));
                String utTitle = (String) params.get("title");
                String utDesc = (String) params.get("description");
                String utStatusStr = (String) params.get("status");
                Task.Status utStatus = utStatusStr != null ? Task.Status.valueOf(utStatusStr) : null;
                Task updateTask = Task.builder().title(utTitle).description(utDesc).status(utStatus).build();
                return taskService.updateTask(utId, updateTask);
            case "deleteTask":
                Long dtId = asLong(params.get("id"));
                taskService.deleteTask(dtId);
                return Map.of("deleted", dtId);
            case "searchTasksByStatus":
                String st = (String) params.get("status");
                Task.Status filterStatus = Task.Status.valueOf(st);
                // naive in-memory filter
                return projectService.getAllProjects().stream()
                        .filter(proj -> proj.getTasks() != null)
                        .flatMap(proj -> proj.getTasks().stream())
                        .filter(task -> task.getStatus() == filterStatus)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unknown method: " + method);
        }
    }

    private Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.valueOf(o.toString());
    }
}
