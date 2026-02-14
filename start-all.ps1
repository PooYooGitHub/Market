# Market项目启动脚本
# 按顺序启动所有必要的服务

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Market 项目启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Nacos是否运行
Write-Host "[1/3] 检查 Nacos 状态..." -ForegroundColor Yellow
$nacosRunning = $false
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8849/nacos" -TimeoutSec 3 -UseBasicParsing -ErrorAction SilentlyContinue
    $nacosRunning = $true
    Write-Host "✓ Nacos 已运行" -ForegroundColor Green
} catch {
    Write-Host "✗ Nacos 未运行" -ForegroundColor Red
    Write-Host "  请先启动 Nacos: docker start nacos" -ForegroundColor Yellow
    exit 1
}

# 检查MySQL是否运行
Write-Host "[2/3] 检查 MySQL 状态..." -ForegroundColor Yellow
try {
    $mysqlRunning = Test-NetConnection -ComputerName localhost -Port 3306 -WarningAction SilentlyContinue -InformationLevel Quiet
    if ($mysqlRunning) {
        Write-Host "✓ MySQL 已运行" -ForegroundColor Green
    } else {
        Write-Host "✗ MySQL 未运行" -ForegroundColor Red
        Write-Host "  请先启动 MySQL 服务" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "✗ 无法检查 MySQL 状态" -ForegroundColor Red
    exit 1
}

# 检查Redis是否运行
Write-Host "[3/3] 检查 Redis 状态..." -ForegroundColor Yellow
try {
    $redisRunning = Test-NetConnection -ComputerName localhost -Port 6379 -WarningAction SilentlyContinue -InformationLevel Quiet
    if ($redisRunning) {
        Write-Host "✓ Redis 已运行" -ForegroundColor Green
    } else {
        Write-Host "✗ Redis 未运行" -ForegroundColor Red
        Write-Host "  请先启动 Redis 服务" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "✗ 无法检查 Redis 状态" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "所有基础服务已就绪，开始启动应用服务..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 编译项目（如果需要）
$compile = Read-Host "是否需要重新编译项目？(y/n)"
if ($compile -eq "y" -or $compile -eq "Y") {
    Write-Host "正在编译项目..." -ForegroundColor Yellow
    mvn '-Dmaven.test.skip=true' clean compile -pl market-service/market-service-core -am
    if ($LASTEXITCODE -ne 0) {
        Write-Host "编译失败！" -ForegroundColor Red
        exit 1
    }
    Write-Host "✓ 编译成功" -ForegroundColor Green
    Write-Host ""
}

# 启动认证服务
Write-Host "正在启动 market-auth (端口 8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\market-auth'; Write-Host 'Market Auth Service' -ForegroundColor Cyan; mvn spring-boot:run"

Write-Host "等待 5 秒让认证服务启动..." -ForegroundColor Gray
Start-Sleep -Seconds 5

# 启动核心业务服务
Write-Host "正在启动 market-service-core (端口 9001)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\market-service\market-service-core'; Write-Host 'Market Service Core' -ForegroundColor Cyan; mvn spring-boot:run"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "启动完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "服务列表：" -ForegroundColor White
Write-Host "  - Nacos:              http://localhost:8849/nacos" -ForegroundColor White
Write-Host "  - Market Auth:        http://localhost:8080" -ForegroundColor White
Write-Host "  - Market Service:     http://localhost:9001" -ForegroundColor White
Write-Host "  - API文档 (Knife4j):  http://localhost:9001/doc.html" -ForegroundColor White
Write-Host ""
Write-Host "提示：两个服务窗口将在新窗口中打开" -ForegroundColor Yellow
Write-Host "按任意键退出此脚本..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

