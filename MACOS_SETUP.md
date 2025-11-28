# MCP Server Setup for macOS Users

Hi Daniel! 👋

This guide is specifically for you as a macOS user. The MCP server is now fully cross-platform compatible.

## Quick Start (macOS)

### 1. Start the Application
```bash
mvn spring-boot:run
```

Wait for this log line:
```
Tomcat initialized with ports 8080 (http), 8081 (http)
```

### 2. Test Everything Works
```bash
chmod +x test-mcp.sh
./test-mcp.sh
```

You should see all tests passing with green checkmarks ✓

---

## Essential Commands for macOS

### Test MCP Ping
```bash
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"1","method":"ping","params":{}}'
```

Expected response:
```json
{"jsonrpc":"2.0","id":"1","result":{"pong":true,"timestamp":1234567890}}
```

### Create a Project
```bash
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"2","method":"createProject","params":{"name":"My Mac Project","description":"Testing from macOS"}}'
```

### List Projects
```bash
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"3","method":"listProjects","params":{}}'
```

### Create a Task
```bash
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"4","method":"createTask","params":{"projectId":1,"title":"Task from Mac","description":"Testing","status":"TODO"}}'
```

---

## WebSocket Testing (macOS)

### Install websocat
```bash
brew install websocat
```

### Test WebSocket
```bash
echo '{"jsonrpc":"2.0","id":"ws-1","method":"ping","params":{}}' | websocat ws://localhost:8081/mcp/ws
```

### Interactive WebSocket Session
```bash
websocat ws://localhost:8081/mcp/ws
```
Then type JSON-RPC commands interactively.

---

## Troubleshooting (macOS)

### Check if Ports are in Use
```bash
lsof -i :8080
lsof -i :8081
```

### Kill Java Processes
```bash
pkill -9 java
```

### Kill Specific Port
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9

# Kill process on port 8081
lsof -ti:8081 | xargs kill -9
```

### Maven Not Found?
```bash
# If using Homebrew
brew install maven

# Or add to PATH if already installed
export PATH="/usr/local/bin:$PATH"
```

---

## Port Information

- **Port 8080**: Normal REST API
  - Example: `curl http://localhost:8080/api/projects`
  
- **Port 8081**: MCP Server
  - HTTP JSON-RPC: `http://localhost:8081/mcp/rpc`
  - WebSocket: `ws://localhost:8081/mcp/ws`

### Port Isolation
- MCP endpoints don't work on port 8080 (you'll get 404)
- REST endpoints don't work on port 8081 (you'll get 404)
- This is intentional security/isolation

---

## Available Methods

All these work via JSON-RPC on port 8081:

- `ping` - Health check
- `listProjects` - Get all projects
- `getProject` - Get one project by ID
- `createProject` - Create new project
- `updateProject` - Update existing project
- `deleteProject` - Delete project
- `listTasks` - Get tasks for a project
- `getTask` - Get one task by ID
- `createTask` - Create new task
- `updateTask` - Update existing task
- `deleteTask` - Delete task
- `searchTasksByStatus` - Find tasks by status (TODO/IN_PROGRESS/DONE)

---

## Using with mcp.json

The `mcp.json` file is already configured:

```json
{
  "name": "task-management-mcp",
  "transport": {
    "websocket": {"url": "ws://localhost:8081/mcp/ws"},
    "http": {"url": "http://localhost:8081/mcp/rpc"}
  }
}
```

You can use this with any MCP-compatible client tool.

---

## Files Created for You

1. **`MCP_README.md`** - Complete documentation (Windows + macOS)
2. **`test-mcp.sh`** - Automated test script for macOS
3. **`MCP_QUICK_REFERENCE.md`** - Quick command reference
4. **`MACOS_SETUP.md`** - This file!

---

## Example Session

```bash
# 1. Start app
mvn spring-boot:run

# 2. In another terminal, test ping
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"1","method":"ping","params":{}}'

# 3. Create project
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"2","method":"createProject","params":{"name":"Test","description":"My project"}}'

# 4. List projects
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"3","method":"listProjects","params":{}}'

# 5. Create task (assuming project ID is 1)
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"4","method":"createTask","params":{"projectId":1,"title":"My Task","description":"Test","status":"TODO"}}'
```

---

## Common Issues

### "Connection refused" on port 8081
- App isn't running yet, or failed to start
- Check logs for "Tomcat initialized with ports 8080 (http), 8081 (http)"

### "Port already in use"
- Previous instance still running
- Run: `pkill -9 java` then restart

### JSON syntax errors in curl
- Make sure to use single quotes around the JSON
- Escape inner quotes if needed
- Or save JSON to file: `curl -X POST ... -d @request.json`

---

## Need Help?

Check these files:
- `MCP_README.md` - Full documentation
- `MCP_QUICK_REFERENCE.md` - Command cheatsheet
- `test-mcp.sh` - See working examples in the test script

Run the test script to see everything in action:
```bash
./test-mcp.sh
```

Happy coding! 🚀

