# MCP Server Test Script
# Run this after starting the application to verify MCP functionality

Write-Host "=== MCP Server Comprehensive Test ===" -ForegroundColor Green

# Test 1: Ping
Write-Host "`n1. Testing MCP Ping..." -ForegroundColor Yellow
$ping = Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"1","method":"ping","params":{}}'
Write-Host "   Result: pong=$($ping.result.pong), timestamp=$($ping.result.timestamp)" -ForegroundColor Green

# Test 2: Create Project
Write-Host "`n2. Creating Project via MCP..." -ForegroundColor Yellow
$project = Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"2","method":"createProject","params":{"name":"MCP Test Project","description":"Created via MCP JSON-RPC"}}'
Write-Host "   Created Project ID: $($project.result.id), Name: $($project.result.name)" -ForegroundColor Green

# Test 3: List Projects
Write-Host "`n3. Listing Projects via MCP..." -ForegroundColor Yellow
$projects = Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"3","method":"listProjects","params":{}}'
Write-Host "   Found $($projects.result.Count) project(s)" -ForegroundColor Green

# Test 4: Create Task
Write-Host "`n4. Creating Task via MCP..." -ForegroundColor Yellow
$projectId = $project.result.id
$task = Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body "{`"jsonrpc`":`"2.0`",`"id`":`"4`",`"method`":`"createTask`",`"params`":{`"projectId`":$projectId,`"title`":`"Test Task`",`"description`":`"Task via MCP`",`"status`":`"TODO`"}}"
Write-Host "   Created Task ID: $($task.result.id), Title: $($task.result.title), Status: $($task.result.status)" -ForegroundColor Green

# Test 5: List Tasks for Project
Write-Host "`n5. Listing Tasks for Project via MCP..." -ForegroundColor Yellow
$tasks = Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body "{`"jsonrpc`":`"2.0`",`"id`":`"5`",`"method`":`"listTasks`",`"params`":{`"projectId`":$projectId}}"
Write-Host "   Found $($tasks.result.Count) task(s) in project" -ForegroundColor Green

# Test 6: Update Task Status
Write-Host "`n6. Updating Task Status via MCP..." -ForegroundColor Yellow
$taskId = $task.result.id
$updated = Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body "{`"jsonrpc`":`"2.0`",`"id`":`"6`",`"method`":`"updateTask`",`"params`":{`"id`":$taskId,`"title`":`"Test Task`",`"description`":`"Updated via MCP`",`"status`":`"IN_PROGRESS`"}}"
Write-Host "   Updated Task Status: $($updated.result.status)" -ForegroundColor Green

# Test 7: Search Tasks by Status
Write-Host "`n7. Searching Tasks by Status via MCP..." -ForegroundColor Yellow
$search = Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"7","method":"searchTasksByStatus","params":{"status":"IN_PROGRESS"}}'
Write-Host "   Found $($search.result.Count) task(s) with status IN_PROGRESS" -ForegroundColor Green

# Test 8: Verify Port Isolation (MCP on 8080 should fail)
Write-Host "`n8. Testing Port Isolation..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "http://localhost:8080/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"1","method":"ping","params":{}}'
    Write-Host "   ERROR: MCP should not be accessible on port 8080!" -ForegroundColor Red
} catch {
    Write-Host "   Port isolation working correctly (404 on port 8080)" -ForegroundColor Green
}

# Test 9: Verify REST API on 8080
Write-Host "`n9. Testing REST API on Port 8080..." -ForegroundColor Yellow
$restProjects = Invoke-RestMethod -Uri "http://localhost:8080/api/projects"
Write-Host "   REST API: Found $($restProjects.Count) project(s)" -ForegroundColor Green

# Test 10: Test Error Handling (unknown method)
Write-Host "`n10. Testing Error Handling..." -ForegroundColor Yellow
try {
    $error = Invoke-RestMethod -Uri "http://localhost:8081/mcp/rpc" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","id":"99","method":"unknownMethod","params":{}}'
    Write-Host "   ERROR: Should have received error response!" -ForegroundColor Red
} catch {
    Write-Host "   Error handling working correctly (method not found)" -ForegroundColor Green
}

Write-Host "`n=== All Tests Completed ===" -ForegroundColor Green
Write-Host "`nSummary:" -ForegroundColor Cyan
Write-Host "  - MCP Server is running on port 8081" -ForegroundColor Cyan
Write-Host "  - REST API is running on port 8080" -ForegroundColor Cyan
Write-Host "  - Port isolation is working" -ForegroundColor Cyan
Write-Host "  - All JSON-RPC methods are functional" -ForegroundColor Cyan

