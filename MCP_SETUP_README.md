# Task Management MCP Server

This application exposes task and project management endpoints as MCP (Model Context Protocol) tools, allowing you to interact with it through GitHub Copilot.

## Features

- **Project Management**: Create, read, update, and delete projects
- **Task Management**: Create, read, update, and delete tasks within projects
- **Task Search**: Search tasks by status across all projects
- **MCP Integration**: Full Model Context Protocol support for AI assistant integration

## Available MCP Tools

### Project Tools
- `listProjects` - List all projects
- `getProject` - Get a specific project by ID
- `createProject` - Create a new project
- `updateProject` - Update an existing project
- `deleteProject` - Delete a project

### Task Tools
- `listTasks` - List all tasks for a project
- `getTask` - Get a specific task by ID
- `createTask` - Create a new task in a project
- `updateTask` - Update an existing task
- `deleteTask` - Delete a task
- `searchTasksByStatus` - Search tasks by status (TODO, IN_PROGRESS, DONE)

## Quick Start

### 1. Build the Application

```powershell
mvn clean package -DskipTests
```

### 2. Start the MCP Server

```powershell
.\start-mcp-server.ps1
```

The server will start on `http://localhost:8080` with MCP endpoints at `/mcp`.

### 3. Configure GitHub Copilot

To use this MCP server with GitHub Copilot, you need to add it to your Copilot MCP configuration.

#### Option A: Using the provided configuration file

Copy the contents of `copilot-mcp.json` to your Copilot settings:

**Location:** `%APPDATA%\Code\User\globalStorage\github.copilot-chat\mcp.json`

Or manually create/edit the file with:

```json
{
  "mcpServers": {
    "task-management": {
      "command": "pwsh",
      "args": [
        "-File",
        "C:\\projects\\aiHackathon\\task-force\\service\\start-mcp-server.ps1"
      ],
      "description": "Task Management MCP Server - Manage projects and tasks",
      "env": {
        "SERVER_PORT": "8080"
      }
    }
  }
}
```

**Note:** Update the path in `args` to match your actual installation directory.

#### Option B: Using HTTP transport (Alternative)

If you prefer HTTP transport instead of stdio, you can configure it as:

```json
{
  "mcpServers": {
    "task-management": {
      "type": "http",
      "url": "http://localhost:8080/mcp",
      "description": "Task Management MCP Server - Manage projects and tasks"
    }
  }
}
```

Then start the server normally with:
```powershell
java -jar target/taskmanagement-0.0.1-SNAPSHOT.jar
```

### 4. Test the MCP Server

#### Health Check
```powershell
curl http://localhost:8080/mcp/ping
```

#### List Tools
```powershell
curl -X POST http://localhost:8080/mcp/tools/list -H "Content-Type: application/json"
```

#### Call a Tool (Example: List Projects)
```powershell
curl -X POST http://localhost:8080/mcp/tools/call `
  -H "Content-Type: application/json" `
  -d '{\"name\":\"listProjects\",\"arguments\":{}}'
```

### 5. Use with GitHub Copilot

Once configured, you can interact with the task management system through Copilot chat:

**Examples:**
- "Create a new project called 'AI Hackathon' with description 'Building MCP integrations'"
- "List all projects"
- "Create a task 'Setup development environment' in project 1"
- "Show me all tasks that are in progress"
- "Update task 3 status to DONE"

## MCP Endpoints

- `GET /mcp` - Server information
- `GET /mcp/ping` - Health check
- `POST /mcp/initialize` - Initialize MCP connection
- `POST /mcp/notifications/initialized` - Handle initialization notification
- `POST /mcp/tools/list` - List all available tools
- `POST /mcp/tools/call` - Call a specific tool

## API Endpoints (REST)

The application also exposes standard REST endpoints:

### Projects
- `GET /api/projects` - List all projects
- `GET /api/projects/{id}` - Get project by ID
- `POST /api/projects` - Create project
- `PUT /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project

### Tasks
- `GET /api/projects/{projectId}/tasks` - List tasks for project
- `GET /api/tasks/{id}` - Get task by ID
- `POST /api/projects/{projectId}/tasks` - Create task
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task

## Configuration

The application uses H2 in-memory database by default. Configuration can be modified in `src/main/resources/application.properties`.

Default settings:
- Server Port: 8080
- H2 Console: http://localhost:8080/h2-console
- Database: jdbc:h2:mem:taskmanagement

## Development

### Project Structure
```
src/main/java/com/example/taskmanagement/
├── controller/          # REST controllers
├── model/              # Domain models (Project, Task)
├── repository/         # JPA repositories
├── service/            # Business logic
└── mcp/                # MCP integration
    ├── McpController.java              # MCP HTTP endpoints
    ├── McpServerConfiguration.java     # MCP server setup
    └── McpToolsConfiguration.java      # Tool definitions
```

### Adding New Tools

1. Add a new `@Bean` method in `McpToolsConfiguration.java` with `@Description` annotation
2. Define request/response DTOs as records
3. The tool will be automatically discovered and exposed via MCP

Example:
```java
@Bean
@Description("Your tool description here")
public Function<YourRequest, YourResponse> yourTool() {
    return request -> {
        // Tool implementation
    };
}
```

## Troubleshooting

### MCP Server not connecting
- Ensure the path in `copilot-mcp.json` is correct
- Check that port 8080 is not in use
- Restart VS Code after updating MCP configuration

### Build fails
- Verify Java 17 or higher is installed
- Run `mvn clean` before building
- Check Maven is properly configured

### Tools not appearing in Copilot
- Restart the MCP server
- Reload VS Code window
- Check the MCP server logs for errors

## License

This project is created for educational and demonstration purposes.

