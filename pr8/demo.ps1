param(
    [string]$GatewayUrl = "http://localhost:8090",
    [string]$KeycloakUrl = "http://localhost:8092"
)

$ErrorActionPreference = "Stop"

Write-Host "Obtaining OAuth2 token from Keycloak..."
$tokenResponse = Invoke-RestMethod `
    -Method Post `
    -Uri "$KeycloakUrl/realms/pr8/protocol/openid-connect/token" `
    -ContentType "application/x-www-form-urlencoded" `
    -Body @{ grant_type = "password"; client_id = "pr8-client"; username = "student"; password = "student123" }

$headers = @{
    Authorization = "Bearer $($tokenResponse.access_token)"
    "X-Request-ID" = "demo-$([Guid]::NewGuid())"
}

Write-Host "Reading products through Traefik and API Gateway..."
$products = Invoke-RestMethod -Method Get -Uri "$GatewayUrl/api/products" -Headers $headers
$products | Format-Table id, name, price, availableQuantity

$clientRequestId = "demo-$([Guid]::NewGuid())"
$orderJson = @{
    clientRequestId = $clientRequestId
    productId = 1
    quantity = 2
    customerName = "Anna Smirnova"
    deliveryAddress = "Moscow, Tverskaya street, 10"
} | ConvertTo-Json -Compress
$orderBody = [System.Text.Encoding]::UTF8.GetBytes($orderJson)

Write-Host "Creating order: Gateway -> Order Service -> Feign -> Supplier Service..."
$order = Invoke-RestMethod `
    -Method Post `
    -Uri "$GatewayUrl/api/orders" `
    -Headers $headers `
    -ContentType "application/json; charset=utf-8" `
    -Body $orderBody
Write-Host "Order ID: $($order.id), status: $($order.status), served by: $($order.servedBy)"

Write-Host "Repeating the same clientRequestId to prove HTTP idempotency..."
$repeated = Invoke-RestMethod `
    -Method Post `
    -Uri "$GatewayUrl/api/orders" `
    -Headers $headers `
    -ContentType "application/json; charset=utf-8" `
    -Body $orderBody
Write-Host "Repeated request returned order ID: $($repeated.id)"

Write-Host "Waiting for RabbitMQ consumer to create delivery..."
$delivery = $null
for ($attempt = 1; $attempt -le 20 -and $null -eq $delivery; $attempt++) {
    Start-Sleep -Milliseconds 500
    $deliveries = Invoke-RestMethod -Method Get -Uri "$GatewayUrl/api/deliveries" -Headers $headers
    $delivery = $deliveries.GetEnumerator() |
        Where-Object { $_.orderId -eq $order.id } |
        Select-Object -First 1
}
if ($null -eq $delivery) {
    throw "Delivery was not created within 10 seconds"
}
Write-Host "Delivery ID: $($delivery.id), status: $($delivery.status)"

$streamFile = Join-Path ([System.IO.Path]::GetTempPath()) "pr8-sse-$($order.id).txt"
$errorFile = Join-Path ([System.IO.Path]::GetTempPath()) "pr8-sse-$($order.id).err.txt"
$authorizationArgument = '"Authorization: Bearer {0}"' -f $tokenResponse.access_token
$acceptArgument = '"Accept: text/event-stream"'
$streamUrlArgument = '"{0}/api/orders/{1}/events"' -f $GatewayUrl, $order.id
$streamProcess = Start-Process `
    -FilePath "curl.exe" `
    -ArgumentList @("-sS", "-N", "--max-time", "9", "-H", $authorizationArgument, "-H", $acceptArgument, $streamUrlArgument) `
    -RedirectStandardOutput $streamFile `
    -RedirectStandardError $errorFile `
    -WindowStyle Hidden `
    -PassThru

Start-Sleep -Seconds 2
foreach ($status in @("IN_TRANSIT", "DELIVERED")) {
    $statusJson = @{ status = $status } | ConvertTo-Json -Compress
    $statusBody = [System.Text.Encoding]::UTF8.GetBytes($statusJson)
    $updated = Invoke-RestMethod `
        -Method Put `
        -Uri "$GatewayUrl/api/deliveries/$($delivery.id)/status" `
        -Headers $headers `
        -ContentType "application/json; charset=utf-8" `
        -Body $statusBody
    Write-Host "Delivery status: $($updated.status)"
    Start-Sleep -Seconds 1
}

Wait-Process -Id $streamProcess.Id -Timeout 12 -ErrorAction SilentlyContinue
Write-Host ""
Write-Host "SSE timeline (persisted history plus live updates):"
$streamOutput = Get-Content -LiteralPath $streamFile -Raw -Encoding UTF8
if ([string]::IsNullOrWhiteSpace($streamOutput)) {
    $streamError = Get-Content -LiteralPath $errorFile -Raw -Encoding UTF8 -ErrorAction SilentlyContinue
    throw "SSE stream returned no events. curl output: $streamError"
}
Write-Host $streamOutput

Write-Host "Final order:"
curl.exe -sS -H "Authorization: Bearer $($tokenResponse.access_token)" "$GatewayUrl/api/orders/$($order.id)"
Write-Host ""
Write-Host "Final delivery:"
curl.exe -sS -H "Authorization: Bearer $($tokenResponse.access_token)" "$GatewayUrl/api/deliveries/$($delivery.id)"
Write-Host ""

Remove-Item -LiteralPath $streamFile -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $errorFile -ErrorAction SilentlyContinue
