package com.example.taskmanagement.mcp;

import com.example.taskmanagement.model.Project;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.service.ProjectService;
import com.example.taskmanagement.service.TaskService;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MCP Tools Configuration using Spring AI
 * Exposes all project and task management operations as MCP tools
 */
@Configuration
@RequiredArgsConstructor
public class McpToolsConfiguration {

    private final ProjectService projectService;
    private final TaskService taskService;

    // ==================== PROJECT TOOLS ====================

    @Bean
    @Description("List all projects in the system")
    public Function<ListProjectsRequest, List<Project>> listProjects() {
        return request -> projectService.getAllProjects();
    }

    @Bean
    @Description("Get details of a specific project by its ID")
    public Function<GetProjectRequest, Project> getProject() {
        return request -> projectService.getProjectById(request.id())
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + request.id()));
    }

    @Bean
    @Description("Create a new project with name and description")
    public Function<CreateProjectRequest, Project> createProject() {
        return request -> {
            Project project = Project.builder()
                    .name(request.name())
                    .description(request.description())
                    .build();
            return projectService.createProject(project);
        };
    }

    @Bean
    @Description("Update an existing project's name and/or description")
    public Function<UpdateProjectRequest, Project> updateProject() {
        return request -> {
            Project project = Project.builder()
                    .name(request.name())
                    .description(request.description())
                    .build();
            return projectService.updateProject(request.id(), project);
        };
    }

    @Bean
    @Description("Delete a project by its ID")
    public Function<DeleteProjectRequest, Map<String, Object>> deleteProject() {
        return request -> {
            projectService.deleteProject(request.id());
            return Map.of(
                    "success", true,
                    "message", "Project deleted successfully",
                    "id", request.id()
            );
        };
    }

    // ==================== TASK TOOLS ====================

    @Bean
    @Description("List all tasks for a specific project")
    public Function<ListTasksRequest, List<Task>> listTasks() {
        return request -> taskService.getTasksByProjectId(request.projectId());
    }

    @Bean
    @Description("Get details of a specific task by its ID")
    public Function<GetTaskRequest, Task> getTask() {
        return request -> taskService.getTaskById(request.id())
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + request.id()));
    }

    @Bean
    @Description("Create a new task in a project with title, description, and status")
    public Function<CreateTaskRequest, Task> createTask() {
        return request -> {
            Task.Status status = request.status() != null
                    ? Task.Status.valueOf(request.status().toUpperCase())
                    : Task.Status.TODO;
            Task task = Task.builder()
                    .title(request.title())
                    .description(request.description())
                    .status(status)
                    .build();
            return taskService.createTask(request.projectId(), task);
        };
    }

    @Bean
    @Description("Update an existing task's title, description, and/or status")
    public Function<UpdateTaskRequest, Task> updateTask() {
        return request -> {
            Task task = Task.builder()
                    .title(request.title())
                    .description(request.description())
                    .status(request.status() != null ? Task.Status.valueOf(request.status().toUpperCase()) : null)
                    .build();
            return taskService.updateTask(request.id(), task);
        };
    }

    @Bean
    @Description("Delete a task by its ID")
    public Function<DeleteTaskRequest, Map<String, Object>> deleteTask() {
        return request -> {
            taskService.deleteTask(request.id());
            return Map.of(
                    "success", true,
                    "message", "Task deleted successfully",
                    "id", request.id()
            );
        };
    }

    @Bean
    @Description("Search for tasks across all projects by their status (TODO, IN_PROGRESS, or DONE)")
    public Function<SearchTasksByStatusRequest, List<Task>> searchTasksByStatus() {
        return request -> {
            Task.Status filterStatus = Task.Status.valueOf(request.status().toUpperCase());
            return projectService.getAllProjects().stream()
                    .filter(proj -> proj.getTasks() != null)
                    .flatMap(proj -> proj.getTasks().stream())
                    .filter(task -> task.getStatus() == filterStatus)
                    .collect(Collectors.toList());
        };
    }

    // ==================== REQUEST DTOs ====================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Request to list all projects")
    public record ListProjectsRequest() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Request to get a specific project by ID")
    public record GetProjectRequest(
            @JsonProperty(required = true)
            @JsonPropertyDescription("The unique identifier of the project")
            Long id
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Request to create a new project")
    public record CreateProjectRequest(
            @JsonProperty(required = true)
            @JsonPropertyDescription("Name of the project")
            String name,

            @JsonProperty(required = false)
            @JsonPropertyDescription("Description of the project")
            String description
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Request to update an existing project")
    public record UpdateProjectRequest(
            @JsonProperty(required = true)
            @JsonPropertyDescription("The unique identifier of the project to update")
            Long id,

            @JsonProperty(required = true)
            @JsonPropertyDescription("New name for the project")
            String name,

            @JsonProperty(required = false)
            @JsonPropertyDescription("New description for the project")
            String description
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Request to delete a project")
    public record DeleteProjectRequest(
            @JsonProperty(required = true)
            @JsonPropertyDescription("The unique identifier of the project to delete")
            Long id
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Request to list all tasks for a project")
    public record ListTasksRequest(
            @JsonProperty(required = true)
            @JsonPropertyDescription("The project ID to list tasks from")
            Long projectId
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Request to get a specific task by ID")
    public record GetTaskRequest(
            @JsonProperty(required = true)
            @JsonPropertyDescription("The unique identifier of the task")
            Long id
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Request to create a new task")
    public record CreateTaskRequest(
            @JsonProperty(required = true)
            @JsonPropertyDescription("The project ID to create the task in")
            Long projectId,

            @JsonProperty(required = true)
            @JsonPropertyDescription("Title of the task")
            String title,

            @JsonProperty(required = false)
            @JsonPropertyDescription("Description of the task")
            String description,

            @JsonProperty(required = false)
            @JsonPropertyDescription("Status of the task: TODO, IN_PROGRESS, or DONE (default: TODO)")
            String status
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Request to update an existing task")
    public record UpdateTaskRequest(
            @JsonProperty(required = true)
            @JsonPropertyDescription("The unique identifier of the task to update")
            Long id,

            @JsonProperty(required = true)
            @JsonPropertyDescription("New title for the task")
            String title,

            @JsonProperty(required = false)
            @JsonPropertyDescription("New description for the task")
            String description,

            @JsonProperty(required = false)
            @JsonPropertyDescription("New status for the task: TODO, IN_PROGRESS, or DONE")
            String status
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Request to delete a task")
    public record DeleteTaskRequest(
            @JsonProperty(required = true)
            @JsonPropertyDescription("The unique identifier of the task to delete")
            Long id
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Request to search tasks by status")
    public record SearchTasksByStatusRequest(
            @JsonProperty(required = true)
            @JsonPropertyDescription("Status to filter by: TODO, IN_PROGRESS, or DONE")
            String status
    ) {}
}

