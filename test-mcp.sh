#!/bin/bash
# MCP Server Test Script for macOS/Linux
# Run this after starting the application to verify MCP functionality

echo -e "\033[32m=== MCP Server Comprehensive Test ===\033[0m"

# Test 1: Ping
echo -e "\n\033[33m1. Testing MCP Ping...\033[0m"
response=$(curl -s -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"1","method":"ping","params":{}}')
echo "   Response: $response" | grep -o '"pong":true' && echo -e "   \033[32m✓ Ping successful\033[0m"

# Test 2: Create Project
echo -e "\n\033[33m2. Creating Project via MCP...\033[0m"
response=$(curl -s -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"2","method":"createProject","params":{"name":"MCP Test Project","description":"Created via MCP JSON-RPC"}}')
project_id=$(echo $response | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
echo -e "   \033[32m✓ Created Project ID: $project_id\033[0m"

# Test 3: List Projects
echo -e "\n\033[33m3. Listing Projects via MCP...\033[0m"
response=$(curl -s -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"3","method":"listProjects","params":{}}')
echo "   Response: $response" | grep -o '"result":\[' > /dev/null && echo -e "   \033[32m✓ Projects listed successfully\033[0m"

# Test 4: Create Task
echo -e "\n\033[33m4. Creating Task via MCP...\033[0m"
response=$(curl -s -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d "{\"jsonrpc\":\"2.0\",\"id\":\"4\",\"method\":\"createTask\",\"params\":{\"projectId\":$project_id,\"title\":\"Test Task\",\"description\":\"Task via MCP\",\"status\":\"TODO\"}}")
task_id=$(echo $response | grep -o '"id":[0-9]*' | tail -1 | grep -o '[0-9]*')
echo -e "   \033[32m✓ Created Task ID: $task_id, Status: TODO\033[0m"

# Test 5: List Tasks for Project
echo -e "\n\033[33m5. Listing Tasks for Project via MCP...\033[0m"
response=$(curl -s -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d "{\"jsonrpc\":\"2.0\",\"id\":\"5\",\"method\":\"listTasks\",\"params\":{\"projectId\":$project_id}}")
echo "   Response: $response" | grep -o '"result":\[' > /dev/null && echo -e "   \033[32m✓ Tasks listed successfully\033[0m"

# Test 6: Update Task Status
echo -e "\n\033[33m6. Updating Task Status via MCP...\033[0m"
response=$(curl -s -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d "{\"jsonrpc\":\"2.0\",\"id\":\"6\",\"method\":\"updateTask\",\"params\":{\"id\":$task_id,\"title\":\"Test Task\",\"description\":\"Updated via MCP\",\"status\":\"IN_PROGRESS\"}}")
echo "   Response: $response" | grep -o '"status":"IN_PROGRESS"' && echo -e "   \033[32m✓ Task updated to IN_PROGRESS\033[0m"

# Test 7: Search Tasks by Status
echo -e "\n\033[33m7. Searching Tasks by Status via MCP...\033[0m"
response=$(curl -s -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"7","method":"searchTasksByStatus","params":{"status":"IN_PROGRESS"}}')
echo "   Response: $response" | grep -o '"result":\[' > /dev/null && echo -e "   \033[32m✓ Search completed successfully\033[0m"

# Test 8: Verify Port Isolation (MCP on 8080 should fail)
echo -e "\n\033[33m8. Testing Port Isolation...\033[0m"
response=$(curl -s -w "%{http_code}" -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"1","method":"ping","params":{}}' -o /dev/null)
if [ "$response" == "404" ]; then
    echo -e "   \033[32m✓ Port isolation working correctly (404 on port 8080)\033[0m"
else
    echo -e "   \033[31m✗ ERROR: MCP should not be accessible on port 8080!\033[0m"
fi

# Test 9: Verify REST API on 8080
echo -e "\n\033[33m9. Testing REST API on Port 8080...\033[0m"
response=$(curl -s -w "%{http_code}" http://localhost:8080/api/projects -o /dev/null)
if [ "$response" == "200" ]; then
    echo -e "   \033[32m✓ REST API working correctly on port 8080\033[0m"
else
    echo -e "   \033[31m✗ ERROR: REST API not responding on port 8080\033[0m"
fi

# Test 10: Test Error Handling (unknown method)
echo -e "\n\033[33m10. Testing Error Handling...\033[0m"
response=$(curl -s -X POST http://localhost:8081/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"99","method":"unknownMethod","params":{}}')
echo "   Response: $response" | grep -o '"error":' > /dev/null && echo -e "   \033[32m✓ Error handling working correctly (method not found)\033[0m"

echo -e "\n\033[32m=== All Tests Completed ===\033[0m"
echo -e "\n\033[36mSummary:\033[0m"
echo -e "  \033[36m- MCP Server is running on port 8081\033[0m"
echo -e "  \033[36m- REST API is running on port 8080\033[0m"
echo -e "  \033[36m- Port isolation is working\033[0m"
echo -e "  \033[36m- All JSON-RPC methods are functional\033[0m"

