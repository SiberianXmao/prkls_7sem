param(
    [string]$GatewayUrl = "http://localhost:8090",
    [string]$KeycloakUrl = "http://localhost:8092"
)

$ErrorActionPreference = "Stop"
$token = (Invoke-RestMethod `
    -Method Post `
    -Uri "$KeycloakUrl/realms/pr8/protocol/openid-connect/token" `
    -ContentType "application/x-www-form-urlencoded" `
    -Body @{ grant_type = "password"; client_id = "pr8-client"; username = "student"; password = "student123" }).access_token
$headers = @{ Authorization = "Bearer $token"; "X-Request-ID" = "failure-demo" }

Write-Host "Stopping Supplier Service..."
docker compose stop supplier-service
try {
    $json = @{
        clientRequestId = "failure-$([Guid]::NewGuid())"
        productId = 1
        quantity = 1
        customerName = "Failure Demo"
        deliveryAddress = "Test address"
    } | ConvertTo-Json -Compress
    $body = [System.Text.Encoding]::UTF8.GetBytes($json)
    $started = Get-Date
    try {
        Invoke-RestMethod -Method Post -Uri "$GatewayUrl/api/orders" -Headers $headers `
            -ContentType "application/json; charset=utf-8" -Body $body | Out-Null
        Write-Host "Unexpected success"
    } catch {
        $elapsed = ((Get-Date) - $started).TotalSeconds
        $statusCode = [int]$_.Exception.Response.StatusCode
        $details = $_.ErrorDetails.Message
        if ([string]::IsNullOrWhiteSpace($details)) {
            $details = $_.Exception.Message
        }
        Write-Host "Fast controlled failure after $([Math]::Round($elapsed, 2)) s (HTTP $statusCode):"
        Write-Host $details
    }
} finally {
    Write-Host "Starting Supplier Service again..."
    docker compose up -d --wait supplier-service
}

$products = Invoke-RestMethod -Method Get -Uri "$GatewayUrl/api/products" -Headers $headers
Write-Host "Supplier Service recovered; products available: $($products.Count)"
