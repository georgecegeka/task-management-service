# HTTP Request Examples

This folder contains HTTP request examples for testing the Task Management API endpoints.

## Files Overview

### 1. `project-requests.http`
Contains all Project-related API endpoint examples:
- **GET** `/api/projects` - Get all projects
- **GET** `/api/projects/{id}` - Get project by ID
- **POST** `/api/projects` - Create a new project
- **PUT** `/api/projects/{id}` - Update an existing project
- **DELETE** `/api/projects/{id}` - Delete a project

### 2. `task-requests.http`
Contains all Task-related API endpoint examples:
- **GET** `/api/projects/{projectId}/tasks` - Get all tasks for a project
- **GET** `/api/tasks/{id}` - Get task by ID
- **POST** `/api/projects/{projectId}/tasks` - Create a new task
- **PUT** `/api/tasks/{id}` - Update an existing task
- **DELETE** `/api/tasks/{id}` - Delete a task

### 3. `complete-workflow.http`
A comprehensive end-to-end workflow example that demonstrates:
1. Creating multiple projects
2. Creating tasks with different statuses (TODO, IN_PROGRESS, DONE)
3. Retrieving projects and tasks
4. Updating task statuses
5. Updating project details
6. Cleanup operations

## How to Use

### Using IntelliJ IDEA / JetBrains IDEs
1. Make sure your Spring Boot application is running (`mvn spring-boot:run`)
2. Open any `.http` file in IntelliJ IDEA
3. Click the green "Run" arrow (▶️) next to any request
4. View the response in the "Run" tool window

### Using VS Code with REST Client Extension
1. Install the "REST Client" extension by Huachao Mao
2. Open any `.http` file
3. Click "Send Request" above each request
4. View the response in a new editor tab

### Using curl (Command Line)
You can also convert these to curl commands. Example:
```bash
# Get all projects
curl -X GET http://localhost:8080/api/projects \
  -H "Accept: application/json"

# Create a project
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Project","description":"A test project"}'
```

## Data Models

### Project
```json
{
  "id": 1,
  "name": "Project Name",
  "description": "Project Description"
}
```

### Task
```json
{
  "id": 1,
  "title": "Task Title",
  "description": "Task Description",
  "status": "TODO|IN_PROGRESS|DONE"
}
```

## Getting Started

### Quick Start - Complete Workflow
1. Start your Spring Boot application
2. Open `complete-workflow.http`
3. Execute the requests in order from STEP 1 to STEP 11
4. This will create projects, tasks, and demonstrate all CRUD operations

### Individual Testing
- Use `project-requests.http` to test only project endpoints
- Use `task-requests.http` to test only task endpoints

## Notes

- The application uses an **H2 in-memory database**, so all data is lost when the application restarts
- IDs in the examples (like `/api/projects/1`) assume sequential creation
- Adjust IDs based on your actual data
- Task statuses must be one of: `TODO`, `IN_PROGRESS`, or `DONE`
- Deleting a project will cascade delete all associated tasks

## Troubleshooting

### Connection Refused
- Ensure the Spring Boot application is running
- Verify the application is running on port 8080
- Check `application.properties` for any custom port configuration

### 404 Not Found
- Verify the resource ID exists in the database
- Check if you created the resources in the correct order

### 500 Internal Server Error
- Check the application logs for detailed error messages
- Ensure all required fields are provided in the request body
- Verify the JSON format is correct

## Application Configuration

The application runs on: `http://localhost:8080`

H2 Console (if enabled): `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:taskdb`
- Username: `sa`
- Password: (empty)

