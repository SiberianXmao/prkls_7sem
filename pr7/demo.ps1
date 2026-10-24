param(
    [string]$BaseUrl = "http://localhost:8087"
)

$ErrorActionPreference = "Stop"

Write-Host "Creating a delivery through reactive WebClient lookup..."
$createJson = @{ orderId = 1; courier = "Ivan Petrov" } | ConvertTo-Json -Compress
$createBody = [System.Text.Encoding]::UTF8.GetBytes($createJson)
$created = Invoke-RestMethod `
    -Method Post `
    -Uri "$BaseUrl/api/deliveries" `
    -ContentType "application/json; charset=utf-8" `
    -Body $createBody

$deliveryId = $created.id
Write-Host "Created delivery ID: $deliveryId (order data received through WebClient)"

$streamFile = Join-Path ([System.IO.Path]::GetTempPath()) "pr7-sse-$deliveryId.txt"
$errorFile = Join-Path ([System.IO.Path]::GetTempPath()) "pr7-sse-$deliveryId.err.txt"

Write-Host "Opening SSE stream..."
$streamProcess = Start-Process `
    -FilePath "curl.exe" `
    -ArgumentList @("-N", "--max-time", "10", "$BaseUrl/api/deliveries/$deliveryId/stream") `
    -RedirectStandardOutput $streamFile `
    -RedirectStandardError $errorFile `
    -WindowStyle Hidden `
    -PassThru

Start-Sleep -Seconds 2

foreach ($status in @("ACCEPTED", "IN_TRANSIT", "DELIVERED")) {
    $updateJson = @{ courier = "Ivan Petrov"; status = $status } | ConvertTo-Json -Compress
    $updateBody = [System.Text.Encoding]::UTF8.GetBytes($updateJson)
    $updated = Invoke-RestMethod `
        -Method Put `
        -Uri "$BaseUrl/api/deliveries/$deliveryId" `
        -ContentType "application/json; charset=utf-8" `
        -Body $updateBody
    Write-Host "Updated status: $($updated.status)"
    Start-Sleep -Seconds 1
}

Wait-Process -Id $streamProcess.Id -Timeout 15 -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "SSE events received by the HTTP client:"
Get-Content -LiteralPath $streamFile -Encoding UTF8

Write-Host ""
Write-Host "Final object from HTTP GET:"
curl.exe -sS "$BaseUrl/api/deliveries/$deliveryId"
Write-Host ""

Remove-Item -LiteralPath $streamFile -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $errorFile -ErrorAction SilentlyContinue
