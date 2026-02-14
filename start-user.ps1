# 快速启动用户服务
# 前提：Nacos 已启动

Write-Host "启动用户服务..." -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 检查 Nacos
Write-Host "检查 Nacos..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://127.0.0.1:8849/nacos" -TimeoutSec 2 -UseBasicParsing
    Write-Host "✓ Nacos 运行中" -ForegroundColor Green
} catch {
    Write-Host "✗ Nacos 未启动，请先运行: .\start-infrastructure.ps1" -ForegroundColor Red
    exit 1
}

# 启动用户服务
Write-Host ""
Write-Host "启动 market-service-user..." -ForegroundColor Cyan
Set-Location "$scriptDir\market-service\market-service-user"

Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run"

Write-Host ""
Write-Host "✓ 用户服务启动中..." -ForegroundColor Green
Write-Host "  服务地址: http://localhost:9001" -ForegroundColor Cyan
Write-Host "  健康检查: http://localhost:9001/actuator/health" -ForegroundColor Cyan
Write-Host "  Nacos 控制台: http://localhost:8849/nacos" -ForegroundColor Cyan

