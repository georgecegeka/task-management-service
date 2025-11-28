# How to Connect to Your MCP Server

## Configuration File

The `mcp.json` file has been updated with the correct configuration to connect to your MCP server.

## Key Configuration Details

- **Server URL**: `http://localhost:8080`
- **Transport**: SSE (Server-Sent Events)
- **Endpoint**: `/mcp/sse`
- **Protocol Version**: `2024-11-05`

## Using with Different Clients

### 1. Claude Desktop

**Location of config file:**
- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`

**Configuration to add:**

```json
{
  "mcpServers": {
    "task-management": {
      "url": "http://localhost:8080/mcp/sse",
      "type": "sse"
    }
  }
}
```

**Alternative using curl (for remote servers):**

```json
{
  "mcpServers": {
    "task-management": {
      "command": "curl",
      "args": [
        "-N",
        "-H", "Accept: text/event-stream",
        "http://localhost:8080/mcp/sse"
      ]
    }
  }
}
```

### 2. Using with MCP Inspector

Install MCP Inspector:
```bash
npm install -g @modelcontextprotocol/inspector
```

Test your server:
```bash
mcp-inspector http://localhost:8080/mcp/sse
```

### 3. Using with Node.js/TypeScript

```typescript
import { Client } from '@modelcontextprotocol/sdk/client/index.js';
import { SSEClientTransport } from '@modelcontextprotocol/sdk/client/sse.js';

// Create SSE transport
const transport = new SSEClientTransport(
  new URL('http://localhost:8080/mcp/sse')
);

// Create MCP client
const client = new Client(
  {
    name: 'my-client',
    version: '1.0.0',
  },
  {
    capabilities: {
      tools: {},
      prompts: {},
      resources: {}
    },
  }
);

// Connect
await client.connect(transport);

// List tools
const tools = await client.listTools();
console.log('Available tools:', tools);

// Call a tool
const result = await client.callTool({
  name: 'listProjects',
  arguments: {}
});
console.log('Projects:', result);

// Close connection
await client.close();
```

### 4. Using with Python

```python
from mcp import ClientSession, StdioServerParameters
from mcp.client.sse import sse_client
import asyncio

async def main():
    # Connect via SSE
    async with sse_client("http://localhost:8080/mcp/sse") as (read, write):
        async with ClientSession(read, write) as session:
            # Initialize
            await session.initialize()
            
            # List tools
            tools = await session.list_tools()
            print(f"Available tools: {tools}")
            
            # Call a tool
            result = await session.call_tool("listProjects", {})
            print(f"Projects: {result}")

if __name__ == "__main__":
    asyncio.run(main())
```

### 5. Direct HTTP/REST Access (without SSE)

If your client doesn't support SSE, you can use the HTTP endpoints:

**Initialize:**
```bash
curl -X POST http://localhost:8080/mcp/initialize \
  -H "Content-Type: application/json" \
  -d '{
    "protocolVersion": "2024-11-05",
    "clientInfo": {
      "name": "my-client",
      "version": "1.0.0"
    },
    "capabilities": {}
  }'
```

**List Tools:**
```bash
curl -X POST http://localhost:8080/mcp/tools/list \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Call a Tool:**
```bash
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "listProjects",
    "arguments": {}
  }'
```

### 6. Using with Web Browser (EventSource API)

```javascript
// Connect to SSE endpoint
const eventSource = new EventSource('http://localhost:8080/mcp/sse');

// Handle messages
eventSource.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log('Received:', data);
  
  if (data.method === 'endpoint') {
    // Server sent endpoint info
    const messageEndpoint = data.params.endpoint;
    console.log('Message endpoint:', messageEndpoint);
    
    // Send messages to the message endpoint
    sendMessage(messageEndpoint, {
      jsonrpc: '2.0',
      id: 1,
      method: 'tools/list',
      params: {}
    });
  }
};

// Handle specific event types
eventSource.addEventListener('ping', (event) => {
  console.log('Ping:', event.data);
});

// Handle errors
eventSource.onerror = (error) => {
  console.error('SSE Error:', error);
};

// Send message to server
async function sendMessage(endpoint, message) {
  const response = await fetch(`http://localhost:8080${endpoint}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(message)
  });
  
  const result = await response.json();
  console.log('Result:', result);
}
```

## Quick Connection Test

### Test 1: Check if server is running
```bash
curl http://localhost:8080/mcp/ping
```

Expected response:
```json
{
  "status": "ok",
  "timestamp": 1701187200000
}
```

### Test 2: Test SSE connection
```bash
curl -N -H "Accept: text/event-stream" http://localhost:8080/mcp/sse
```

Expected: Stream of SSE events with endpoint info and periodic pings

### Test 3: List available tools
```bash
curl -X POST http://localhost:8080/mcp/tools/list \
  -H "Content-Type: application/json" \
  -d '{}'
```

Expected: JSON with 11 tools (listProjects, getProject, createProject, etc.)

## Common Connection Issues

### Issue: "Connection refused"
**Solution:** Make sure your Spring Boot application is running:
```bash
mvn spring-boot:run
```

### Issue: "CORS error in browser"
**Solution:** CORS is already configured for localhost. If accessing from a different origin, update `CorsConfig.java`

### Issue: "404 Not Found"
**Solution:** Verify you're using the correct URL:
- Base: `http://localhost:8080`
- SSE: `http://localhost:8080/mcp/sse`
- HTTP: `http://localhost:8080/mcp/*`

### Issue: "SSE connection drops"
**Solution:** The server sends keepalive pings every 30 seconds. Check your client timeout settings.

## Available Capabilities

### Tools (11)
- Project management: listProjects, getProject, createProject, updateProject, deleteProject
- Task management: listTasks, getTask, createTask, updateTask, deleteTask, searchTasksByStatus

### Prompts (3)
- project_summary - Generate project summaries
- task_report - Generate task status reports
- project_planning - AI-assisted project planning

### Resources (4)
- task://projects - All projects
- task://tasks/todo - TODO tasks
- task://tasks/in_progress - In-progress tasks
- task://tasks/done - Completed tasks

## Example: Complete Workflow

```bash
# 1. Start the server
mvn spring-boot:run

# 2. Check health
curl http://localhost:8080/mcp/ping

# 3. Initialize connection
curl -X POST http://localhost:8080/mcp/initialize \
  -H "Content-Type: application/json" \
  -d '{"protocolVersion":"2024-11-05","clientInfo":{"name":"test","version":"1.0.0"}}'

# 4. Create a project
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name":"createProject","arguments":{"name":"My Project","description":"Test"}}'

# 5. Create a task
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name":"createTask","arguments":{"projectId":1,"title":"My Task","status":"TODO"}}'

# 6. List tasks
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name":"listTasks","arguments":{"projectId":1}}'
```

## Security Notes

**Current Configuration:**
- ✅ CORS enabled for localhost
- ✅ Multiple port support (3000, 4200, 8080)
- ⚠️ No authentication (development mode)

**For Production:**
- Add authentication (OAuth2/JWT)
- Restrict CORS to specific domains
- Use HTTPS
- Add rate limiting

## Support

For more information, see:
- `MCP_SERVER_README.md` - Full documentation
- `test-mcp-enhanced.http` - Test examples
- `MCP_QUICK_REFERENCE.md` - Quick reference

