# Market 项目 - 轻量级启动脚本
# 只启动核心服务，减少内存占用

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Market 项目 - 轻量级启动" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "优化说明：" -ForegroundColor Yellow
Write-Host "  当前启动以下核心服务：" -ForegroundColor Gray
Write-Host "    1. market-gateway (网关)" -ForegroundColor Green
Write-Host "    2. market-auth (认证)" -ForegroundColor Green
Write-Host "    3. market-service-user (用户服务)" -ForegroundColor Green
Write-Host ""

# 检查基础设施
Write-Host "步骤 1: 检查基础设施服务..." -ForegroundColor Cyan
Write-Host "  请确保以下服务已启动：" -ForegroundColor Gray
Write-Host "    - MySQL (3306)" -ForegroundColor Gray
Write-Host "    - Redis (6379)" -ForegroundColor Gray
Write-Host "    - Nacos (8849)" -ForegroundColor Gray
Write-Host ""

$continue = Read-Host "基础设施是否已启动? (y/n)"
if ($continue -ne 'y') {
    Write-Host "请先启动基础设施: .\start-infrastructure.ps1" -ForegroundColor Yellow
    exit
}

# 编译项目
Write-Host ""
Write-Host "步骤 2: 编译项目..." -ForegroundColor Cyan
mvn clean package -DskipTests -pl market-gateway,market-auth,market-service/market-service-user -am

if ($LASTEXITCODE -ne 0) {
    Write-Host "编译失败！" -ForegroundColor Red
    exit 1
}

Write-Host "编译成功！" -ForegroundColor Green
Write-Host ""

# 提示启动服务
Write-Host "步骤 3: 启动服务" -ForegroundColor Cyan
Write-Host "请按以下顺序在不同终端窗口启动服务：" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. 启动网关服务：" -ForegroundColor Cyan
Write-Host "   cd market-gateway\target" -ForegroundColor White
Write-Host "   java -jar market-gateway-1.0-SNAPSHOT.jar" -ForegroundColor White
Write-Host ""
Write-Host "2. 启动认证服务：" -ForegroundColor Cyan
Write-Host "   cd market-auth\target" -ForegroundColor White
Write-Host "   java -jar market-auth-1.0-SNAPSHOT.jar" -ForegroundColor White
Write-Host ""
Write-Host "3. 启动用户服务：" -ForegroundColor Cyan
Write-Host "   cd market-service\market-service-user\target" -ForegroundColor White
Write-Host "   java -jar market-service-user-1.0-SNAPSHOT.jar" -ForegroundColor White
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "服务访问地址：" -ForegroundColor Cyan
Write-Host "  网关: http://localhost:9000" -ForegroundColor Green
Write-Host "  认证: http://localhost:8888" -ForegroundColor Green
Write-Host "  用户服务: http://localhost:9001" -ForegroundColor Green
Write-Host "  Nacos: http://localhost:8849/nacos" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan

