# Start MinIO with correct data persistence
# PowerShell Script

Write-Host "Starting MinIO..." -ForegroundColor Cyan
Write-Host ""

# Check if MinIO container already exists
$existingContainer = docker ps -a --filter "name=minio" --format "{{.Names}}"

if ($existingContainer -eq "minio") {
    Write-Host "MinIO container already exists. Starting..." -ForegroundColor Yellow
    docker start minio

    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] MinIO started" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Failed to start MinIO" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "Creating new MinIO container..." -ForegroundColor Yellow

    # Create data directory
    $currentDir = Get-Location
    $minioDataDir = Join-Path $currentDir "docker\minio\data"
    $minioConfigDir = Join-Path $currentDir "docker\minio\config"

    if (!(Test-Path $minioDataDir)) {
        New-Item -ItemType Directory -Path $minioDataDir -Force | Out-Null
    }
    if (!(Test-Path $minioConfigDir)) {
        New-Item -ItemType Directory -Path $minioConfigDir -Force | Out-Null
    }

    # Start container
    docker run -d `
      --name minio `
      --restart=always `
      -p 9900:9000 `
      -p 9090:9090 `
      -e "MINIO_ROOT_USER=minioadmin" `
      -e "MINIO_ROOT_PASSWORD=minioadmin123" `
      -v "${minioDataDir}:/data" `
      -v "${minioConfigDir}:/root/.minio" `
      minio/minio:RELEASE.2021-06-17T00-10-46Z server /data --console-address ":9090"

    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] MinIO created and started" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Failed to create MinIO" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "MinIO 访问信息:" -ForegroundColor Cyan
Write-Host "  API:      http://localhost:9900" -ForegroundColor White
Write-Host "  控制台:   http://localhost:9090" -ForegroundColor White
Write-Host "  User:     minioadmin" -ForegroundColor White
Write-Host "  Password: minioadmin123" -ForegroundColor White
Write-Host ""

