# Restart MinIO with data persistence
# PowerShell Script

Write-Host "MinIO Data Persistence Fix Script" -ForegroundColor Cyan
Write-Host ""

# Stop and remove old container
Write-Host "Step 1: Stopping old MinIO container..." -ForegroundColor Yellow
docker stop minio 2>&1 | Out-Null
docker rm minio 2>&1 | Out-Null
Write-Host "Done" -ForegroundColor Green
Write-Host ""

# Create data directories
Write-Host "Step 2: Creating data directories..." -ForegroundColor Yellow
$currentDir = Get-Location
$minioDataDir = Join-Path $currentDir "docker\minio\data"
$minioConfigDir = Join-Path $currentDir "docker\minio\config"

if (!(Test-Path $minioDataDir)) {
    New-Item -ItemType Directory -Path $minioDataDir -Force | Out-Null
}
if (!(Test-Path $minioConfigDir)) {
    New-Item -ItemType Directory -Path $minioConfigDir -Force | Out-Null
}
Write-Host "Data dir: $minioDataDir" -ForegroundColor Gray
Write-Host "Done" -ForegroundColor Green
Write-Host ""

# Start new container
Write-Host "Step 3: Starting new MinIO container..." -ForegroundColor Yellow
docker run -d `
  --name minio `
  --restart=always `
  -p 9001:9000 `
  -p 9090:9090 `
  -e "MINIO_ROOT_USER=minioadmin" `
  -e "MINIO_ROOT_PASSWORD=minioadmin123" `
  -v "${minioDataDir}:/data" `
  -v "${minioConfigDir}:/root/.minio" `
  minio/minio:RELEASE.2021-06-17T00-10-46Z server /data --console-address ":9090"

if ($LASTEXITCODE -eq 0) {
    Write-Host "Done" -ForegroundColor Green
    Write-Host ""
    Start-Sleep -Seconds 5

    Write-Host "MinIO Fix Complete!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Access Information:" -ForegroundColor Yellow
    Write-Host "  API:      http://localhost:9001" -ForegroundColor White
    Write-Host "  Console:  http://localhost:9090" -ForegroundColor White
    Write-Host "  User:     minioadmin" -ForegroundColor White
    Write-Host "  Password: minioadmin123" -ForegroundColor White
    Write-Host ""
    Write-Host "Data saved to: $minioDataDir" -ForegroundColor Gray
} else {
    Write-Host "Failed!" -ForegroundColor Red
    Write-Host "Check logs: docker logs minio" -ForegroundColor Yellow
    exit 1
}



