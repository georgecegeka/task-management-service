# MCP Server - Issue Resolution Summary

## Problem
The `curl` command syntax in MCP_README.md only worked on macOS/Linux, not on Windows PowerShell.

## Root Cause
PowerShell's `curl` is an alias for `Invoke-WebRequest`, which has different syntax than the Linux `curl` command. The `-d` and `-H` flags don't work the same way.

## Solution
Updated all documentation to include both Windows PowerShell and macOS/Linux bash syntax:

### Windows (PowerShell):
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"1","method":"ping","params":{}}'
```

### macOS/Linux (bash/zsh):
```bash
curl -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"1","method":"ping","params":{}}'
```

## Verified Functionality
All MCP features are working correctly:

✅ **Port 8080**: Normal REST API (`/api/projects`, `/api/tasks`)  
✅ **Port 8081**: MCP JSON-RPC (`/mcp/rpc`) and WebSocket (`/mcp/ws`)  
✅ **Port Isolation**: MCP endpoints blocked on 8080, REST endpoints blocked on 8081  
✅ **JSON-RPC Methods**: All 12 methods working (ping, listProjects, createProject, etc.)  
✅ **Error Handling**: Unknown methods return proper JSON-RPC error codes  

## Testing
Run the comprehensive test script:

**Windows (PowerShell):**
```powershell
.\test-mcp.ps1
```

**macOS/Linux (bash):**
```bash
chmod +x test-mcp.sh
./test-mcp.sh
```

This tests all MCP functionality including:
- Ping
- Create/list/update/delete projects
- Create/list/update/delete tasks
- Search tasks by status
- Port isolation
- Error handling

## Files Updated
1. `MCP_README.md` - Added both Windows PowerShell and macOS/Linux bash examples for all commands
2. `test-mcp.ps1` - Windows PowerShell comprehensive test script
3. `test-mcp.sh` - macOS/Linux bash comprehensive test script

## How It Works
When you run `TaskManagementApplication`:
1. Spring Boot starts Tomcat on port 8080 (default)
2. `McpAdditionalConnectorConfig` adds a second connector on 8081
3. `McpPortIsolationFilter` ensures MCP and REST endpoints are isolated
4. Both ports run in the same JVM process - no separate startup needed

## Next Steps
The MCP server is fully functional and ready to use. You can now:
- Use it from any MCP client tool via `mcp.json`
- Test via HTTP JSON-RPC on port 8081
- Connect via WebSocket at `ws://localhost:8081/mcp/ws`
- Access normal REST API on port 8080 as before

