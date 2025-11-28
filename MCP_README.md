# MCP Server Usage Guide

This Spring Boot application now exposes TWO interfaces:

1. Normal REST API (Projects & Tasks) on port **8080**
2. MCP (Model Context Protocol) JSON-RPC & WebSocket interface on port **8081**

Both run from the same `TaskManagementApplication` (single JVM) using an additional Tomcat connector.

---
## 1. Starting Everything

Simply run the Spring Boot app; the MCP server starts automatically:

```bash
mvn spring-boot:run
```

You should see a log line similar to:
```
Tomcat initialized with ports 8080 (http), 8081 (http)
```

### Quick Test
Run the comprehensive test script to verify everything works:

**Windows (PowerShell):**
```powershell
.\test-mcp.ps1
```

**macOS/Linux:**
```bash
./test-mcp.sh
```

### Port Conflict Resolution

If you get port conflicts, free ports first:

**Windows (PowerShell):**
```powershell
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force
netstat -ano | findstr :8080
netstat -ano | findstr :8081
```

**macOS/Linux:**
```bash
pkill -9 java
lsof -ti:8080 | xargs kill -9
lsof -ti:8081 | xargs kill -9
# Or check ports:
lsof -i :8080
lsof -i :8081
```

(Ensure no LISTENING entries remain.)

---
## 2. REST API (Port 8080)

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/projects"
```

**macOS/Linux:**
```bash
curl http://localhost:8080/api/projects
```

---
## 3. MCP HTTP JSON-RPC (Port 8081)

Endpoint: `POST http://localhost:8081/mcp/rpc`

Request format:
```json
{
  "jsonrpc": "2.0",
  "id": "<client-id>",
  "method": "<methodName>",
  "params": {}
}
```

### Ping Example

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"1","method":"ping","params":{}}'
```

**macOS/Linux:**
```bash
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"1","method":"ping","params":{}}'
```

### Create Project

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"2","method":"createProject","params":{"name":"MCP Project","description":"Created via JSON-RPC"}}'
```

**macOS/Linux:**
```bash
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"2","method":"createProject","params":{"name":"MCP Project","description":"Created via JSON-RPC"}}'
```

### List Projects

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"3","method":"listProjects","params":{}}'
```

**macOS/Linux:**
```bash
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"3","method":"listProjects","params":{}}'
```

### Search Tasks by Status

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"4","method":"searchTasksByStatus","params":{"status":"TODO"}}'
```

**macOS/Linux:**
```bash
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"4","method":"searchTasksByStatus","params":{"status":"TODO"}}'
```

Error example (unknown method): returns JSON-RPC error with code -32601.

---
## 4. MCP WebSocket (Port 8081)

URL: `ws://localhost:8081/mcp/ws`

After connecting, send JSON-RPC messages as text frames. Example message:
```json
{"jsonrpc":"2.0","id":"ws-1","method":"ping","params":{}}
```

### Windows PowerShell Test Script:
```powershell
Add-Type -AssemblyName System.Net.WebSockets
$ws = New-Object System.Net.WebSockets.ClientWebSocket
$ws.ConnectAsync([Uri]"ws://localhost:8081/mcp/ws",[Threading.CancellationToken]::None).Wait()
$msg = '{"jsonrpc":"2.0","id":"ws-1","method":"ping","params":{}}'
$bytes = [Text.Encoding]::UTF8.GetBytes($msg)
$seg = [ArraySegment[byte]]::new($bytes)
$ws.SendAsync($seg,[System.Net.WebSockets.WebSocketMessageType]::Text,$true,[Threading.CancellationToken]::None).Wait()
$buffer = New-Object byte[] 2048
$recv = [ArraySegment[byte]]::new($buffer)
$ws.ReceiveAsync($recv,[Threading.CancellationToken]::None).Wait()
$response = ([Text.Encoding]::UTF8.GetString($buffer)).Trim([char]0)
Write-Host "Response: $response"
$ws.Dispose()
```

### macOS/Linux Test (using websocat):
First install websocat:
```bash
# macOS
brew install websocat

# Linux
cargo install websocat
# or download from https://github.com/vi/websocat/releases
```

Then test:
```bash
echo '{"jsonrpc":"2.0","id":"ws-1","method":"ping","params":{}}' | websocat ws://localhost:8081/mcp/ws
```

Or interactive mode:
```bash
websocat ws://localhost:8081/mcp/ws
# Then type: {"jsonrpc":"2.0","id":"ws-1","method":"ping","params":{}}
```

---
## 5. Method Catalogue

| Method | Params | Description |
|--------|--------|-------------|
| ping | (none) | Health check |
| listProjects | (none) | Returns all projects |
| getProject | id (Long) | Fetch single project |
| createProject | name (String), description? (String) | Create project |
| updateProject | id, name, description? | Update project |
| deleteProject | id | Delete project |
| listTasks | projectId | Tasks for project |
| getTask | id | Fetch single task |
| createTask | projectId, title, description?, status | Create task |
| updateTask | id, title, description?, status | Update task |
| deleteTask | id | Delete task |
| searchTasksByStatus | status (TODO|IN_PROGRESS|DONE) | Filter tasks by status |

---
## 6. Port Isolation Behavior
- Requests to `/mcp/**` on port **8080** return 404.
- Requests to normal REST paths on port **8081** return 404.

---
## 7. Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| Port 8080 in use | Another app running | Stop other java processes (`Get-Process java | Stop-Process`) |
| Port 8081 in use | Previous failed run | Same as above |
| JSON-RPC error -32601 | Unknown method | Check `method` spelling |
| 404 on MCP path | Using port 8080 | Use port 8081 |
| 404 on REST path via 8081 | Isolation filter working | Use port 8080 |
| WebSocket fails | Firewall or wrong port | Ensure `ws://localhost:8081/mcp/ws` |

---
## 8. Integration (mcp.json)

The provided `mcp.json` file points tools at the MCP transport:
```json
{
  "transport": {
    "websocket": {"url": "ws://localhost:8081/mcp/ws"},
    "http": {"url": "http://localhost:8081/mcp/rpc"}
  }
}
```

---
## 9. Future Improvements (Optional)
- Add request auth (shared secret header).
- Add param validation and structured error codes.
- Add batching support for JSON-RPC.
- Introduce DTOs to slim down entity serialization.
- Add metrics (e.g. Micrometer) per port.

---
## 10. Clean Shutdown

Press `Ctrl+C` in the terminal running `mvn spring-boot:run`. 

If ports remain bound:

**Windows (PowerShell):**
```powershell
Get-Process -Name java | Stop-Process -Force
```

**macOS/Linux:**
```bash
pkill -9 java
# Or to kill specific ports:
lsof -ti:8080 | xargs kill -9
lsof -ti:8081 | xargs kill -9
```

---
**Done.** The MCP server requires no separate start command—running the main Spring Boot application starts both ports automatically.
