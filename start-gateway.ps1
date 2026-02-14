# Gateway启动脚本
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  启动 Market Gateway 服务" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Nacos
Write-Host "检查Nacos状态..." -ForegroundColor Yellow
$nacosPort = netstat -ano | Select-String ":8849.*LISTENING"
if ($nacosPort) {
    Write-Host "✅ Nacos运行正常" -ForegroundColor Green
} else {
    Write-Host "❌ Nacos未运行，请先启动Nacos" -ForegroundColor Red
    Write-Host "运行: .\restart-nacos.ps1" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# 切换到项目目录
Set-Location D:\program\Market\market-gateway

# 启动应用
Write-Host "启动Gateway服务..." -ForegroundColor Yellow
Write-Host "端口: 9000" -ForegroundColor Gray
Write-Host ""

mvn spring-boot:run

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ 启动失败" -ForegroundColor Red
    exit 1
}

