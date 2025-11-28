# MCP Server Quick Reference Card

## Start Application
```bash
mvn spring-boot:run
```

## Ports
- **8080**: Normal REST API (`/api/projects`, `/api/tasks`)
- **8081**: MCP JSON-RPC (`/mcp/rpc`) and WebSocket (`/mcp/ws`)

---

## Quick Commands

### Ping MCP Server

**Windows:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"1","method":"ping","params":{}}'
```

**macOS/Linux:**
```bash
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"1","method":"ping","params":{}}'
```

---

### Create Project

**Windows:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"2","method":"createProject","params":{"name":"My Project","description":"Test project"}}'
```

**macOS/Linux:**
```bash
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"2","method":"createProject","params":{"name":"My Project","description":"Test project"}}'
```

---

### List Projects

**Windows:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"3","method":"listProjects","params":{}}'
```

**macOS/Linux:**
```bash
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"3","method":"listProjects","params":{}}'
```

---

### Create Task

**Windows:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"4","method":"createTask","params":{"projectId":1,"title":"My Task","description":"Test task","status":"TODO"}}'
```

**macOS/Linux:**
```bash
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"4","method":"createTask","params":{"projectId":1,"title":"My Task","description":"Test task","status":"TODO"}}'
```

---

## Available Methods

| Method | Parameters | Description |
|--------|-----------|-------------|
| `ping` | - | Health check |
| `listProjects` | - | List all projects |
| `getProject` | `id` | Get single project |
| `createProject` | `name`, `description?` | Create project |
| `updateProject` | `id`, `name`, `description?` | Update project |
| `deleteProject` | `id` | Delete project |
| `listTasks` | `projectId` | List tasks in project |
| `getTask` | `id` | Get single task |
| `createTask` | `projectId`, `title`, `description?`, `status` | Create task |
| `updateTask` | `id`, `title`, `description?`, `status` | Update task |
| `deleteTask` | `id` | Delete task |
| `searchTasksByStatus` | `status` | Search tasks by status |

**Task Statuses:** `TODO`, `IN_PROGRESS`, `DONE`

---

## Run Tests

**Windows:**
```powershell
.\test-mcp.ps1
```

**macOS/Linux:**
```bash
chmod +x test-mcp.sh
./test-mcp.sh
```

---

## Troubleshooting

### Check Ports

**Windows:**
```powershell
netstat -ano | findstr LISTENING | findstr :8080
netstat -ano | findstr LISTENING | findstr :8081
```

**macOS/Linux:**
```bash
lsof -i :8080
lsof -i :8081
```

### Kill Java Processes

**Windows:**
```powershell
Get-Process -Name java | Stop-Process -Force
```

**macOS/Linux:**
```bash
pkill -9 java
```

### Kill Specific Ports

**macOS/Linux:**
```bash
lsof -ti:8080 | xargs kill -9
lsof -ti:8081 | xargs kill -9
```

---

## WebSocket

**URL:** `ws://localhost:8081/mcp/ws`

**Message Format:**
```json
{"jsonrpc":"2.0","id":"<id>","method":"<method>","params":{...}}
```

**Test WebSocket (macOS/Linux with websocat):**
```bash
brew install websocat
echo '{"jsonrpc":"2.0","id":"ws-1","method":"ping","params":{}}' | websocat ws://localhost:8081/mcp/ws
```

---

## Normal REST API (Port 8080)

**Windows:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/projects"
```

**macOS/Linux:**
```bash
curl http://localhost:8080/api/projects
```

---

For full documentation, see `MCP_README.md`

