$body = @{
    jsonrpc = "2.0"
    id = 1
    method = "initialize"
    params = @{}
} | ConvertTo-Json

Write-Host "Request body:"
Write-Host $body

$response = Invoke-WebRequest -Uri "http://localhost:8081/mcp/rpc" -Method POST -ContentType "application/json" -Body $body
Write-Host "`nResponse:"
$response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

