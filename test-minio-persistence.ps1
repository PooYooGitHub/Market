# MinIO Data Persistence Test Script
# PowerShell Script

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MinIO Data Persistence Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if MinIO container is running
Write-Host "Step 1: Checking MinIO container status..." -ForegroundColor Yellow
$minioContainer = docker ps --filter "name=minio" --format "{{.Names}}"

if ($minioContainer -eq "minio") {
    Write-Host "[OK] MinIO container is running" -ForegroundColor Green
} else {
    Write-Host "[ERROR] MinIO container is not running" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please run: .\restart-minio.ps1" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# Check data directory
Write-Host "Step 2: Checking data directory..." -ForegroundColor Yellow
$dataDir = "docker\minio\data"
if (Test-Path $dataDir) {
    Write-Host "[OK] Data directory exists: $dataDir" -ForegroundColor Green

    # List bucket directories
    $buckets = Get-ChildItem $dataDir -Directory -ErrorAction SilentlyContinue
    if ($buckets.Count -gt 0) {
        Write-Host "  Found $($buckets.Count) bucket(s):" -ForegroundColor Gray
        foreach ($bucket in $buckets) {
            $fileCount = (Get-ChildItem -Path $bucket.FullName -File -Recurse -ErrorAction SilentlyContinue | Measure-Object).Count
            Write-Host "    - $($bucket.Name): $fileCount file(s)" -ForegroundColor Gray
        }
    } else {
        Write-Host "  No buckets created yet" -ForegroundColor Gray
    }
} else {
    Write-Host "[ERROR] Data directory not found: $dataDir" -ForegroundColor Red
}
Write-Host ""

# Test MinIO API
Write-Host "Step 3: Testing MinIO API..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9900/minio/health/live" -Method GET -TimeoutSec 5 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "[OK] MinIO API is responding" -ForegroundColor Green
    } else {
        Write-Host "[WARNING] MinIO API returned status: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[ERROR] Cannot connect to MinIO API" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
}
Write-Host ""

# Test MinIO Console
Write-Host "Step 4: Testing MinIO Console..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9090" -Method GET -TimeoutSec 5 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "[OK] MinIO Console is accessible" -ForegroundColor Green
    } else {
        Write-Host "[WARNING] MinIO Console returned status: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[ERROR] Cannot connect to MinIO Console" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
}
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Visit MinIO Console: http://localhost:9090" -ForegroundColor White
Write-Host "  2. Login with: minioadmin / minioadmin123" -ForegroundColor White
Write-Host "  3. Create bucket: market-avatar" -ForegroundColor White
Write-Host "  4. Upload test image" -ForegroundColor White
Write-Host "  5. Restart container: docker restart minio" -ForegroundColor White
Write-Host "  6. Verify image still exists" -ForegroundColor White
Write-Host ""
Write-Host "Data Persistence Test:" -ForegroundColor Yellow
Write-Host "  - Check: $dataDir" -ForegroundColor Gray
Write-Host "  - After upload, files should appear in this directory" -ForegroundColor Gray
Write-Host "  - After restart, files should still be there" -ForegroundColor Gray
Write-Host ""

