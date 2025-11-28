# MCP Quick Reference

## Start Server
```bash
mvn spring-boot:run
```

## Base URL
```
http://localhost:8080/mcp
```

## Quick Test Commands

### Health Check
```bash
curl http://localhost:8080/mcp/ping
```

### List Tools
```bash
curl -X POST http://localhost:8080/mcp/tools/list -H "Content-Type: application/json" -d '{}'
```

### Create Project
```bash
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name":"createProject","arguments":{"name":"Test Project","description":"Via MCP"}}'
```

### List Projects
```bash
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name":"listProjects","arguments":{}}'
```

### Create Task
```bash
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name":"createTask","arguments":{"projectId":1,"title":"New Task","description":"Test","status":"TODO"}}'
```

### Search Tasks by Status
```bash
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name":"searchTasksByStatus","arguments":{"status":"TODO"}}'
```

## SSE Connection
```bash
curl -N http://localhost:8080/mcp/sse
```

## All Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/mcp` | Server info |
| GET | `/mcp/ping` | Health check |
| POST | `/mcp/initialize` | Initialize |
| POST | `/mcp/tools/list` | List tools |
| POST | `/mcp/tools/call` | Call tool |
| POST | `/mcp/prompts/list` | List prompts |
| POST | `/mcp/prompts/get` | Get prompt |
| POST | `/mcp/resources/list` | List resources |
| POST | `/mcp/resources/read` | Read resource |
| GET | `/mcp/sse` | SSE stream |
| POST | `/mcp/sse/message` | SSE message |

## Tool Names

**Projects:** listProjects, getProject, createProject, updateProject, deleteProject

**Tasks:** listTasks, getTask, createTask, updateTask, deleteTask, searchTasksByStatus

## Prompt Names

- project_summary
- task_report
- project_planning

## Resource URIs

- task://projects
- task://tasks/todo
- task://tasks/in_progress
- task://tasks/done

## Status Values

- TODO
- IN_PROGRESS
- DONE

## Test File

Open `test-mcp-enhanced.http` in your IDE for 25+ ready-to-use test cases.

