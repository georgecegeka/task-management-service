#!/usr/bin/env pwsh

# Task Management MCP Server Startup Script
# This script starts the task management application in MCP mode

$ErrorActionPreference = "Stop"

Write-Host "Starting Task Management MCP Server..." -ForegroundColor Green

# Check if JAR exists
$jarPath = "target/taskmanagement-0.0.1-SNAPSHOT.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "JAR file not found. Building project..." -ForegroundColor Yellow
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Build failed!" -ForegroundColor Red
        exit 1
    }
}

# Start the application
Write-Host "Launching MCP server on http://localhost:8080/mcp" -ForegroundColor Cyan
java -jar $jarPath --server.port=8080

Write-Host "MCP Server stopped." -ForegroundColor Yellow

