$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:8080"
$containers = @(docker compose ps -q delivery-service)

if ($containers.Count -lt 2) {
    throw "At least two delivery-service containers must be running."
}

$target = $containers[0]
$targetName = (docker inspect --format "{{.Name}}" $target).TrimStart("/")
Write-Host "Stopping $targetName..." -ForegroundColor Yellow
docker stop $target | Out-Null

try {
    # Traefik checks backends every five seconds; wait for one complete cycle.
    Start-Sleep -Seconds 8
    Write-Host "Requests served while one replica is stopped:" -ForegroundColor Cyan
    1..10 | ForEach-Object {
        $instance = Invoke-RestMethod -Uri "$baseUrl/api/deliveries/instance" -TimeoutSec 10
        [PSCustomObject]@{ Request = $_; ServedBy = $instance.servedBy }
    } | Format-Table -AutoSize
}
finally {
    Write-Host "Starting $targetName again..." -ForegroundColor Green
    docker start $target | Out-Null
}
