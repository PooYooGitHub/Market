# 快速构建脚本
# Quick build script for Market project

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Market Project - Quick Build Script  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Maven
Write-Host "[1/4] Checking Maven..." -ForegroundColor Yellow
$mvnVersion = mvn -version 2>&1 | Select-String "Apache Maven"
if ($mvnVersion) {
    Write-Host "✓ Maven is installed: $mvnVersion" -ForegroundColor Green
} else {
    Write-Host "✗ Maven is not installed or not in PATH" -ForegroundColor Red
    exit 1
}

# 检查Java
Write-Host ""
Write-Host "[2/4] Checking Java..." -ForegroundColor Yellow
$javaVersion = java -version 2>&1 | Select-String "version"
if ($javaVersion) {
    Write-Host "✓ Java is installed: $javaVersion" -ForegroundColor Green
} else {
    Write-Host "✗ Java is not installed or not in PATH" -ForegroundColor Red
    exit 1
}

# 清理项目
Write-Host ""
Write-Host "[3/4] Cleaning project..." -ForegroundColor Yellow
mvn clean | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Project cleaned successfully" -ForegroundColor Green
} else {
    Write-Host "✗ Failed to clean project" -ForegroundColor Red
    exit 1
}

# 编译项目
Write-Host ""
Write-Host "[4/4] Compiling project..." -ForegroundColor Yellow
Write-Host "This may take a few minutes..." -ForegroundColor Gray

$output = mvn compile -DskipTests 2>&1
$success = $output | Select-String "BUILD SUCCESS"

if ($success) {
    Write-Host "✓ Project compiled successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Build Summary" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    # 显示编译摘要
    $summary = $output | Select-String "Reactor Summary" -Context 0,25
    if ($summary) {
        $summary.Context.PostContext | ForEach-Object {
            if ($_ -match "SUCCESS") {
                Write-Host $_ -ForegroundColor Green
            } elseif ($_ -match "FAILURE") {
                Write-Host $_ -ForegroundColor Red
            } else {
                Write-Host $_
            }
        }
    }

    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "1. Start infrastructure: .\start-infrastructure.ps1" -ForegroundColor White
    Write-Host "2. Initialize database: .\init-nacos-db.ps1" -ForegroundColor White
    Write-Host "3. Package project: mvn clean package -DskipTests" -ForegroundColor White
    Write-Host "4. Run services individually from their target folders" -ForegroundColor White
    Write-Host ""
    Write-Host "For more details, see: PROJECT_IMPROVEMENT_SUMMARY.md" -ForegroundColor Gray

} else {
    Write-Host "✗ Build failed" -ForegroundColor Red
    Write-Host ""
    Write-Host "Error details:" -ForegroundColor Yellow
    $errors = $output | Select-String "ERROR"
    $errors | Select-Object -Last 10 | ForEach-Object {
        Write-Host $_ -ForegroundColor Red
    }
    exit 1
}

Write-Host ""

