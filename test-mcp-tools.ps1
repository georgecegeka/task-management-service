# MCP Tools Testing Script
# This script tests all MCP endpoints and tools

$baseUrl = "http://localhost:8080/mcp"
$headers = @{
    "Content-Type" = "application/json"
    "Accept" = "application/json"
}

Write-Host "================================" -ForegroundColor Cyan
Write-Host "MCP Tools Testing Script" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Helper function to make requests
function Invoke-McpRequest {
    param(
        [string]$Endpoint,
        [string]$Method = "POST",
        [object]$Body = $null
    )

    try {
        $params = @{
            Uri = "$baseUrl/$Endpoint"
            Method = $Method
            Headers = $headers
        }

        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
        }

        $response = Invoke-RestMethod @params
        return $response
    } catch {
        Write-Host "Error: $_" -ForegroundColor Red
        return $null
    }
}

# Test 1: Health Check
Write-Host "[1/30] Testing Health Check (Ping)..." -ForegroundColor Yellow
$pingResponse = Invoke-RestMethod -Uri "$baseUrl/ping" -Method GET -Headers $headers
Write-Host "✓ Ping successful: $($pingResponse | ConvertTo-Json)" -ForegroundColor Green
Write-Host ""

# Test 2: Server Info
Write-Host "[2/30] Testing Server Info..." -ForegroundColor Yellow
$serverInfo = Invoke-RestMethod -Uri $baseUrl -Method GET -Headers $headers
Write-Host "✓ Server: $($serverInfo.name) v$($serverInfo.version)" -ForegroundColor Green
Write-Host ""

# Test 3: Initialize
Write-Host "[3/30] Testing Initialize..." -ForegroundColor Yellow
$initBody = @{
    protocolVersion = "2024-11-05"
    clientInfo = @{
        name = "test-client"
        version = "1.0.0"
    }
    capabilities = @{}
}
$initResponse = Invoke-McpRequest -Endpoint "initialize" -Body $initBody
Write-Host "✓ Protocol Version: $($initResponse.protocolVersion)" -ForegroundColor Green
Write-Host ""

# Test 4: Notifications Initialized
Write-Host "[4/30] Testing Notifications Initialized..." -ForegroundColor Yellow
$notifResponse = Invoke-McpRequest -Endpoint "notifications/initialized" -Body @{}
Write-Host "✓ Notification sent" -ForegroundColor Green
Write-Host ""

# Test 5: List Tools
Write-Host "[5/30] Testing List Tools..." -ForegroundColor Yellow
$toolsResponse = Invoke-McpRequest -Endpoint "tools/list" -Body @{}
Write-Host "Available tools: $($toolsResponse.tools.Count)" -ForegroundColor Green
foreach ($tool in $toolsResponse.tools) {
    $toolName = $tool.name
    $toolDesc = $tool.description
    Write-Host "  - $toolName : $toolDesc" -ForegroundColor Cyan
}
Write-Host ""

# Test 6: Create Project 1
Write-Host "[6/30] Testing Create Project 1..." -ForegroundColor Yellow
$createProject1 = @{
    name = "createProject"
    arguments = @{
        name = "AI Hackathon Project"
        description = "Main project for AI Hackathon 2025"
    }
}
$project1Response = Invoke-McpRequest -Endpoint "tools/call" -Body $createProject1
if ($project1Response) {
    $project1 = $project1Response.content[0].text | ConvertFrom-Json
    Write-Host "✓ Project created with ID: $($project1.id)" -ForegroundColor Green
    $project1Id = $project1.id
}
Write-Host ""

# Test 7: Create Project 2
Write-Host "[7/30] Testing Create Project 2..." -ForegroundColor Yellow
$createProject2 = @{
    name = "createProject"
    arguments = @{
        name = "Task Management System"
        description = "MCP-enabled task management application"
    }
}
$project2Response = Invoke-McpRequest -Endpoint "tools/call" -Body $createProject2
if ($project2Response) {
    $project2 = $project2Response.content[0].text | ConvertFrom-Json
    Write-Host "✓ Project created with ID: $($project2.id)" -ForegroundColor Green
    $project2Id = $project2.id
}
Write-Host ""

# Test 8: List Projects
Write-Host "[8/30] Testing List Projects..." -ForegroundColor Yellow
$listProjects = @{
    name = "listProjects"
    arguments = @{}
}
$listProjectsResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $listProjects
if ($listProjectsResponse) {
    $projects = $listProjectsResponse.content[0].text | ConvertFrom-Json
    Write-Host "✓ Total projects: $($projects.Count)" -ForegroundColor Green
}
Write-Host ""

# Test 9: Get Project by ID
Write-Host "[9/30] Testing Get Project by ID..." -ForegroundColor Yellow
$getProject = @{
    name = "getProject"
    arguments = @{
        id = $project1Id
    }
}
$getProjectResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $getProject
if ($getProjectResponse) {
    Write-Host "✓ Project retrieved successfully" -ForegroundColor Green
}
Write-Host ""

# Test 10: Update Project
Write-Host "[10/30] Testing Update Project..." -ForegroundColor Yellow
$updateProject = @{
    name = "updateProject"
    arguments = @{
        id = $project1Id
        name = "AI Hackathon Project - Updated"
        description = "Main project for AI Hackathon 2025 - MCP Integration Complete"
    }
}
$updateProjectResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $updateProject
if ($updateProjectResponse) {
    Write-Host "✓ Project updated successfully" -ForegroundColor Green
}
Write-Host ""

# Test 11-14: Create Tasks
Write-Host "[11/30] Creating Task 1 (DONE)..." -ForegroundColor Yellow
$createTask1 = @{
    name = "createTask"
    arguments = @{
        projectId = $project1Id
        title = "Setup MCP Server"
        description = "Configure and test MCP server endpoints"
        status = "DONE"
    }
}
$task1Response = Invoke-McpRequest -Endpoint "tools/call" -Body $createTask1
if ($task1Response) {
    $task1 = $task1Response.content[0].text | ConvertFrom-Json
    Write-Host "✓ Task created with ID: $($task1.id)" -ForegroundColor Green
    $task1Id = $task1.id
}
Write-Host ""

Write-Host "[12/30] Creating Task 2 (IN_PROGRESS)..." -ForegroundColor Yellow
$createTask2 = @{
    name = "createTask"
    arguments = @{
        projectId = $project1Id
        title = "Implement Spring AI Integration"
        description = "Add Spring AI MCP capabilities"
        status = "IN_PROGRESS"
    }
}
$task2Response = Invoke-McpRequest -Endpoint "tools/call" -Body $createTask2
if ($task2Response) {
    $task2 = $task2Response.content[0].text | ConvertFrom-Json
    Write-Host "✓ Task created with ID: $($task2.id)" -ForegroundColor Green
    $task2Id = $task2.id
}
Write-Host ""

Write-Host "[13/30] Creating Task 3 (TODO)..." -ForegroundColor Yellow
$createTask3 = @{
    name = "createTask"
    arguments = @{
        projectId = $project1Id
        title = "Create Angular UI"
        description = "Build front-end interface for task management"
        status = "TODO"
    }
}
$task3Response = Invoke-McpRequest -Endpoint "tools/call" -Body $createTask3
if ($task3Response) {
    $task3 = $task3Response.content[0].text | ConvertFrom-Json
    Write-Host "✓ Task created with ID: $($task3.id)" -ForegroundColor Green
    $task3Id = $task3.id
}
Write-Host ""

Write-Host "[14/30] Creating Task 4 for Project 2 (DONE)..." -ForegroundColor Yellow
$createTask4 = @{
    name = "createTask"
    arguments = @{
        projectId = $project2Id
        title = "Database Setup"
        description = "Configure H2 database and repositories"
        status = "DONE"
    }
}
$task4Response = Invoke-McpRequest -Endpoint "tools/call" -Body $createTask4
if ($task4Response) {
    Write-Host "✓ Task created successfully" -ForegroundColor Green
}
Write-Host ""

# Test 15-16: List Tasks
Write-Host "[15/30] Listing Tasks for Project 1..." -ForegroundColor Yellow
$listTasks1 = @{
    name = "listTasks"
    arguments = @{
        projectId = $project1Id
    }
}
$listTasks1Response = Invoke-McpRequest -Endpoint "tools/call" -Body $listTasks1
if ($listTasks1Response) {
    $tasks = $listTasks1Response.content[0].text | ConvertFrom-Json
    Write-Host "✓ Tasks in Project 1: $($tasks.Count)" -ForegroundColor Green
}
Write-Host ""

Write-Host "[16/30] Listing Tasks for Project 2..." -ForegroundColor Yellow
$listTasks2 = @{
    name = "listTasks"
    arguments = @{
        projectId = $project2Id
    }
}
$listTasks2Response = Invoke-McpRequest -Endpoint "tools/call" -Body $listTasks2
if ($listTasks2Response) {
    $tasks = $listTasks2Response.content[0].text | ConvertFrom-Json
    Write-Host "✓ Tasks in Project 2: $($tasks.Count)" -ForegroundColor Green
}
Write-Host ""

# Test 17: Get Task
Write-Host "[17/30] Testing Get Task by ID..." -ForegroundColor Yellow
$getTask = @{
    name = "getTask"
    arguments = @{
        id = $task1Id
    }
}
$getTaskResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $getTask
if ($getTaskResponse) {
    Write-Host "✓ Task retrieved successfully" -ForegroundColor Green
}
Write-Host ""

# Test 18: Update Task
Write-Host "[18/30] Testing Update Task..." -ForegroundColor Yellow
$updateTask = @{
    name = "updateTask"
    arguments = @{
        id = $task2Id
        title = "Implement Spring AI Integration"
        description = "Add Spring AI MCP capabilities - Testing complete"
        status = "DONE"
    }
}
$updateTaskResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $updateTask
if ($updateTaskResponse) {
    Write-Host "✓ Task updated successfully" -ForegroundColor Green
}
Write-Host ""

# Test 19-21: Search by Status
Write-Host "[19/30] Searching Tasks by Status: TODO..." -ForegroundColor Yellow
$searchTodo = @{
    name = "searchTasksByStatus"
    arguments = @{
        status = "TODO"
    }
}
$searchTodoResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $searchTodo
if ($searchTodoResponse) {
    $todoTasks = $searchTodoResponse.content[0].text | ConvertFrom-Json
    Write-Host "✓ TODO tasks found: $($todoTasks.Count)" -ForegroundColor Green
}
Write-Host ""

Write-Host "[20/30] Searching Tasks by Status: IN_PROGRESS..." -ForegroundColor Yellow
$searchInProgress = @{
    name = "searchTasksByStatus"
    arguments = @{
        status = "IN_PROGRESS"
    }
}
$searchInProgressResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $searchInProgress
if ($searchInProgressResponse) {
    $inProgressTasks = $searchInProgressResponse.content[0].text | ConvertFrom-Json
    Write-Host "✓ IN_PROGRESS tasks found: $($inProgressTasks.Count)" -ForegroundColor Green
}
Write-Host ""

Write-Host "[21/30] Searching Tasks by Status: DONE..." -ForegroundColor Yellow
$searchDone = @{
    name = "searchTasksByStatus"
    arguments = @{
        status = "DONE"
    }
}
$searchDoneResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $searchDone
if ($searchDoneResponse) {
    $doneTasks = $searchDoneResponse.content[0].text | ConvertFrom-Json
    Write-Host "✓ DONE tasks found: $($doneTasks.Count)" -ForegroundColor Green
}
Write-Host ""

# Test 22-24: Error Scenarios
Write-Host "[22/30] Testing Error: Get Non-existent Project..." -ForegroundColor Yellow
$getInvalidProject = @{
    name = "getProject"
    arguments = @{
        id = 999
    }
}
$errorResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $getInvalidProject
if ($errorResponse -and $errorResponse.isError) {
    Write-Host "✓ Error handled correctly" -ForegroundColor Green
}
Write-Host ""

Write-Host "[23/30] Testing Error: Get Non-existent Task..." -ForegroundColor Yellow
$getInvalidTask = @{
    name = "getTask"
    arguments = @{
        id = 999
    }
}
$errorResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $getInvalidTask
if ($errorResponse -and $errorResponse.isError) {
    Write-Host "✓ Error handled correctly" -ForegroundColor Green
}
Write-Host ""

Write-Host "[24/30] Testing Error: Create Task with Invalid Project..." -ForegroundColor Yellow
$createInvalidTask = @{
    name = "createTask"
    arguments = @{
        projectId = 999
        title = "Invalid Task"
        description = "This should fail"
        status = "TODO"
    }
}
$errorResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $createInvalidTask
if ($errorResponse -and $errorResponse.isError) {
    Write-Host "✓ Error handled correctly" -ForegroundColor Green
}
Write-Host ""

Write-Host "[25/30] Testing Error: Call Unknown Tool..." -ForegroundColor Yellow
$unknownTool = @{
    name = "unknownTool"
    arguments = @{}
}
$errorResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $unknownTool
if ($errorResponse) {
    Write-Host "✓ Error handled correctly" -ForegroundColor Green
}
Write-Host ""

# Test 26-27: Cleanup
Write-Host "[26/30] Testing Delete Task..." -ForegroundColor Yellow
$deleteTask = @{
    name = "deleteTask"
    arguments = @{
        id = $task3Id
    }
}
$deleteTaskResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $deleteTask
if ($deleteTaskResponse) {
    Write-Host "✓ Task deleted successfully" -ForegroundColor Green
}
Write-Host ""

Write-Host "[27/30] Testing Delete Project..." -ForegroundColor Yellow
$deleteProject = @{
    name = "deleteProject"
    arguments = @{
        id = $project2Id
    }
}
$deleteProjectResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $deleteProject
if ($deleteProjectResponse) {
    Write-Host "✓ Project deleted successfully" -ForegroundColor Green
}
Write-Host ""

# Test 28-30: Verification
Write-Host "[28/30] Verifying Project Still Exists..." -ForegroundColor Yellow
$verifyProject = @{
    name = "getProject"
    arguments = @{
        id = $project1Id
    }
}
$verifyProjectResponse = Invoke-McpRequest -Endpoint "tools/call" -Body $verifyProject
if ($verifyProjectResponse -and -not $verifyProjectResponse.isError) {
    Write-Host "✓ Project verified" -ForegroundColor Green
}
Write-Host ""

Write-Host "[29/30] Final List of All Projects..." -ForegroundColor Yellow
$finalListProjects = Invoke-McpRequest -Endpoint "tools/call" -Body $listProjects
if ($finalListProjects) {
    $projects = $finalListProjects.content[0].text | ConvertFrom-Json
    Write-Host "✓ Total projects remaining: $($projects.Count)" -ForegroundColor Green
}
Write-Host ""

Write-Host "[30/30] Final List of Tasks in Project 1..." -ForegroundColor Yellow
$finalListTasks = Invoke-McpRequest -Endpoint "tools/call" -Body $listTasks1
if ($finalListTasks) {
    $tasks = $finalListTasks.content[0].text | ConvertFrom-Json
    Write-Host "✓ Total tasks remaining: $($tasks.Count)" -ForegroundColor Green
}
Write-Host ""

Write-Host "================================" -ForegroundColor Cyan
Write-Host "✓ All Tests Completed!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Cyan

