# MCP Server Configuration

This application implements the Model Context Protocol (MCP) server capabilities, similar to the [dvaas](https://github.com/danvega/dvaas) implementation, but adapted for task management.

## Overview

The MCP server exposes task management operations as tools, prompts, and resources that can be used by AI assistants and other MCP clients.

## Endpoints

### HTTP/REST Endpoints

All MCP endpoints are available under `/mcp`:

- `POST /mcp/initialize` - Initialize MCP connection
- `POST /mcp/notifications/initialized` - Handle initialization notification
- `POST /mcp/tools/list` - List available tools
- `POST /mcp/tools/call` - Call a specific tool
- `POST /mcp/prompts/list` - List available prompts
- `POST /mcp/prompts/get` - Get a specific prompt
- `POST /mcp/resources/list` - List available resources
- `POST /mcp/resources/read` - Read a specific resource
- `GET /mcp/ping` - Health check
- `GET /mcp` - Server info

### SSE (Server-Sent Events) Endpoint

For streaming support:

- `GET /mcp/sse` - SSE connection for MCP messages
- `POST /mcp/sse/message` - Handle MCP messages over SSE

## Capabilities

### 1. Tools

The server exposes the following tools for task management:

#### Project Tools
- `listProjects` - List all projects
- `getProject` - Get project by ID
- `createProject` - Create a new project
- `updateProject` - Update a project
- `deleteProject` - Delete a project

#### Task Tools
- `listTasks` - List tasks for a project
- `getTask` - Get task by ID
- `createTask` - Create a new task
- `updateTask` - Update a task
- `deleteTask` - Delete a task
- `searchTasksByStatus` - Search tasks by status (TODO, IN_PROGRESS, DONE)

### 2. Prompts

Pre-configured prompts for common scenarios:

- `project_summary` - Generate a comprehensive project summary
  - Arguments: `projectId` (required)
  
- `task_report` - Generate a task report by status
  - Arguments: `status` (optional, default: TODO)
  
- `project_planning` - Help plan a new project with task suggestions
  - Arguments: `projectName` (required)

### 3. Resources

Static resources for quick data access:

- `task://projects` - All projects in the system
- `task://tasks/todo` - All TODO tasks
- `task://tasks/in_progress` - All in-progress tasks
- `task://tasks/done` - All completed tasks

## Usage Examples

### Using with Claude Desktop

Add to your Claude Desktop config (`~/Library/Application Support/Claude/claude_desktop_config.json` on macOS):

```json
{
  "mcpServers": {
    "task-management": {
      "command": "curl",
      "args": [
        "-X", "POST",
        "http://localhost:8080/mcp/sse"
      ],
      "env": {}
    }
  }
}
```

### Using with HTTP Client

#### Initialize Connection

```bash
curl -X POST http://localhost:8080/mcp/initialize \
  -H "Content-Type: application/json" \
  -d '{
    "protocolVersion": "2024-11-05",
    "clientInfo": {
      "name": "test-client",
      "version": "1.0.0"
    }
  }'
```

#### List All Tools

```bash
curl -X POST http://localhost:8080/mcp/tools/list \
  -H "Content-Type: application/json"
```

#### Call a Tool

```bash
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "listProjects",
    "arguments": {}
  }'
```

#### Create a Project

```bash
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "createProject",
    "arguments": {
      "name": "My New Project",
      "description": "A test project created via MCP"
    }
  }'
```

#### Create a Task

```bash
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "createTask",
    "arguments": {
      "projectId": 1,
      "title": "Implement feature X",
      "description": "Add new feature",
      "status": "TODO"
    }
  }'
```

#### Get a Prompt

```bash
curl -X POST http://localhost:8080/mcp/prompts/get \
  -H "Content-Type: application/json" \
  -d '{
    "name": "project_summary",
    "arguments": {
      "projectId": 1
    }
  }'
```

#### Read a Resource

```bash
curl -X POST http://localhost:8080/mcp/resources/read \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "task://projects"
  }'
```

### Using SSE Endpoint

```javascript
const eventSource = new EventSource('http://localhost:8080/mcp/sse');

eventSource.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log('Received:', data);
};

eventSource.onerror = (error) => {
  console.error('SSE Error:', error);
};
```

## Configuration

### CORS Settings

CORS is configured to allow requests from:
- `http://localhost:4200`
- `http://localhost:3000`
- `http://localhost:8080`
- `http://127.0.0.1:*`

Modify `CorsConfig.java` to add additional origins.

### Server Port

Default port is `8080`. Change in `application.properties`:

```properties
server.port=8080
```

## Protocol Version

This implementation follows MCP protocol version `2024-11-05`.

## Development

### Running the Server

```bash
mvn spring-boot:run
```

### Building

```bash
mvn clean install
```

### Testing MCP Endpoints

See the `test-mcp-tools.http` file for comprehensive test examples.

## Architecture

The MCP implementation consists of:

1. **McpController** - HTTP/REST endpoints for MCP protocol
2. **McpSseController** - SSE streaming endpoint
3. **McpServerConfiguration** - Tool registry and configuration
4. **McpToolsConfiguration** - Tool definitions using Spring Function beans
5. **CorsConfig** - CORS configuration for web clients

## Comparison with dvaas

This implementation follows the same architectural patterns as [dvaas](https://github.com/danvega/dvaas):

- Function-based tool definitions
- SSE support for streaming
- HTTP/REST fallback
- Prompts capability
- Resources capability
- Full MCP 2024-11-05 protocol compliance

Key differences:
- Adapted for task management domain (vs. video management)
- Additional task status filtering
- Project-task hierarchical operations
- Enhanced error handling

