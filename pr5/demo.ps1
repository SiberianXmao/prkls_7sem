$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:8080"

Write-Host "Creating an order through Traefik..." -ForegroundColor Cyan
$order = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/orders" `
    -ContentType "application/json" `
    -Body (@{
        customerName = "Ivan Petrov"
        deliveryAddress = "Moscow, Pushkina street, 10"
        productName = "Laptop"
        quantity = 1
        totalAmount = 89990.00
    } | ConvertTo-Json)
$order | Format-List

Write-Host "Creating a delivery through an inter-service request..." -ForegroundColor Cyan
$delivery = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/deliveries" `
    -ContentType "application/json" `
    -Body (@{
        orderId = $order.id
        courierName = "Alexey Smirnov"
    } | ConvertTo-Json)
$delivery | Format-List

Write-Host "Ten requests demonstrating load balancing..." -ForegroundColor Cyan
1..10 | ForEach-Object {
    $instance = Invoke-RestMethod -Uri "$baseUrl/api/deliveries/instance"
    [PSCustomObject]@{ Request = $_; ServedBy = $instance.servedBy }
} | Format-Table -AutoSize
