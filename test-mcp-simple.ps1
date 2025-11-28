# Simple MCP Tools Test Script
# Tests all MCP endpoints

$ErrorActionPreference = "Continue"
$baseUrl = "http://localhost:8080/mcp"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    MCP Tools Testing Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Ping
Write-Host "[Test 1] Ping..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/ping" -Method GET
    Write-Host "SUCCESS: Status = $($response.status)" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 2: Initialize
Write-Host "[Test 2] Initialize..." -ForegroundColor Yellow
try {
    $body = @{
        protocolVersion = "2024-11-05"
        clientInfo = @{ name = "test-client"; version = "1.0.0" }
        capabilities = @{}
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/initialize" -Method POST -Body $body -ContentType "application/json"
    Write-Host "SUCCESS: Protocol = $($response.protocolVersion)" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 3: List Tools
Write-Host "[Test 3] List Tools..." -ForegroundColor Yellow
try {
    $body = @{} | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "$baseUrl/tools/list" -Method POST -Body $body -ContentType "application/json"
    Write-Host "SUCCESS: Found $($response.tools.Count) tools" -ForegroundColor Green
    foreach ($tool in $response.tools) {
        Write-Host "  - $($tool.name)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 4: Create Project
Write-Host "[Test 4] Create Project..." -ForegroundColor Yellow
try {
    $body = @{
        name = "createProject"
        arguments = @{
            name = "Test Project"
            description = "Created by test script"
        }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    $project = $response.content[0].text | ConvertFrom-Json
    $projectId = $project.id
    Write-Host "SUCCESS: Created project with ID = $projectId" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 5: List Projects
Write-Host "[Test 5] List Projects..." -ForegroundColor Yellow
try {
    $body = @{
        name = "listProjects"
        arguments = @{}
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    $projects = $response.content[0].text | ConvertFrom-Json
    Write-Host "SUCCESS: Found $($projects.Count) project(s)" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 6: Get Project
Write-Host "[Test 6] Get Project by ID..." -ForegroundColor Yellow
try {
    $body = @{
        name = "getProject"
        arguments = @{ id = $projectId }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    $project = $response.content[0].text | ConvertFrom-Json
    Write-Host "SUCCESS: Got project '$($project.name)'" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 7: Create Task (DONE)
Write-Host "[Test 7] Create Task (DONE)..." -ForegroundColor Yellow
try {
    $body = @{
        name = "createTask"
        arguments = @{
            projectId = $projectId
            title = "Setup MCP Server"
            description = "Configure endpoints"
            status = "DONE"
        }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    $task1 = $response.content[0].text | ConvertFrom-Json
    $task1Id = $task1.id
    Write-Host "SUCCESS: Created task with ID = $task1Id" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 8: Create Task (IN_PROGRESS)
Write-Host "[Test 8] Create Task (IN_PROGRESS)..." -ForegroundColor Yellow
try {
    $body = @{
        name = "createTask"
        arguments = @{
            projectId = $projectId
            title = "Implement Features"
            description = "Add new functionality"
            status = "IN_PROGRESS"
        }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    $task2 = $response.content[0].text | ConvertFrom-Json
    Write-Host "SUCCESS: Created task with ID = $($task2.id)" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 9: Create Task (TODO)
Write-Host "[Test 9] Create Task (TODO)..." -ForegroundColor Yellow
try {
    $body = @{
        name = "createTask"
        arguments = @{
            projectId = $projectId
            title = "Write Documentation"
            description = "Create user guide"
            status = "TODO"
        }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    $task3 = $response.content[0].text | ConvertFrom-Json
    $task3Id = $task3.id
    Write-Host "SUCCESS: Created task with ID = $task3Id" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 10: List Tasks
Write-Host "[Test 10] List Tasks for Project..." -ForegroundColor Yellow
try {
    $body = @{
        name = "listTasks"
        arguments = @{ projectId = $projectId }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    $tasks = $response.content[0].text | ConvertFrom-Json
    Write-Host "SUCCESS: Found $($tasks.Count) task(s)" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 11: Get Task
Write-Host "[Test 11] Get Task by ID..." -ForegroundColor Yellow
try {
    $body = @{
        name = "getTask"
        arguments = @{ id = $task1Id }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    $task = $response.content[0].text | ConvertFrom-Json
    Write-Host "SUCCESS: Got task '$($task.title)'" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 12: Update Task
Write-Host "[Test 12] Update Task..." -ForegroundColor Yellow
try {
    $body = @{
        name = "updateTask"
        arguments = @{
            id = $task1Id
            title = "Setup MCP Server - UPDATED"
            description = "Configure endpoints - Complete"
            status = "DONE"
        }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    Write-Host "SUCCESS: Task updated" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 13: Search Tasks by Status (TODO)
Write-Host "[Test 13] Search Tasks (TODO)..." -ForegroundColor Yellow
try {
    $body = @{
        name = "searchTasksByStatus"
        arguments = @{ status = "TODO" }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    $tasks = $response.content[0].text | ConvertFrom-Json
    Write-Host "SUCCESS: Found $($tasks.Count) TODO task(s)" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 14: Search Tasks by Status (IN_PROGRESS)
Write-Host "[Test 14] Search Tasks (IN_PROGRESS)..." -ForegroundColor Yellow
try {
    $body = @{
        name = "searchTasksByStatus"
        arguments = @{ status = "IN_PROGRESS" }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    $tasks = $response.content[0].text | ConvertFrom-Json
    Write-Host "SUCCESS: Found $($tasks.Count) IN_PROGRESS task(s)" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 15: Search Tasks by Status (DONE)
Write-Host "[Test 15] Search Tasks (DONE)..." -ForegroundColor Yellow
try {
    $body = @{
        name = "searchTasksByStatus"
        arguments = @{ status = "DONE" }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    $tasks = $response.content[0].text | ConvertFrom-Json
    Write-Host "SUCCESS: Found $($tasks.Count) DONE task(s)" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 16: Delete Task
Write-Host "[Test 16] Delete Task..." -ForegroundColor Yellow
try {
    $body = @{
        name = "deleteTask"
        arguments = @{ id = $task3Id }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    Write-Host "SUCCESS: Task deleted" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 17: Error - Get Non-existent Task
Write-Host "[Test 17] Error Test - Get Non-existent Task..." -ForegroundColor Yellow
try {
    $body = @{
        name = "getTask"
        arguments = @{ id = 99999 }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    if ($response.isError) {
        Write-Host "SUCCESS: Error handled correctly" -ForegroundColor Green
    } else {
        Write-Host "WARNING: Expected error but got success" -ForegroundColor Yellow
    }
} catch {
    Write-Host "SUCCESS: Error caught as expected" -ForegroundColor Green
}
Write-Host ""

# Test 18: Update Project
Write-Host "[Test 18] Update Project..." -ForegroundColor Yellow
try {
    $body = @{
        name = "updateProject"
        arguments = @{
            id = $projectId
            name = "Test Project - UPDATED"
            description = "Updated by test script"
        }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    Write-Host "SUCCESS: Project updated" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Test 19: Delete Project
Write-Host "[Test 19] Delete Project..." -ForegroundColor Yellow
try {
    $body = @{
        name = "deleteProject"
        arguments = @{ id = $projectId }
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/tools/call" -Method POST -Body $body -ContentType "application/json"
    Write-Host "SUCCESS: Project deleted" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $_" -ForegroundColor Red
}
Write-Host ""

# Final Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    All Tests Completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

