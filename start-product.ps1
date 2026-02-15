# 启动商品服务脚本
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "  启动商品服务 (Product Service)" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# 检查基础设施
Write-Host "[1/4] 检查基础设施..." -ForegroundColor Yellow

# 检查MySQL
Write-Host "  - 检查 MySQL..." -NoNewline
$mysqlRunning = Test-NetConnection -ComputerName localhost -Port 3306 -WarningAction SilentlyContinue
if ($mysqlRunning.TcpTestSucceeded) {
    Write-Host " ✓" -ForegroundColor Green
} else {
    Write-Host " ✗ MySQL未运行" -ForegroundColor Red
    exit 1
}

# 检查Redis
Write-Host "  - 检查 Redis..." -NoNewline
$redisRunning = Test-NetConnection -ComputerName localhost -Port 6379 -WarningAction SilentlyContinue
if ($redisRunning.TcpTestSucceeded) {
    Write-Host " ✓" -ForegroundColor Green
} else {
    Write-Host " ✗ Redis未运行" -ForegroundColor Red
    exit 1
}

# 检查Nacos
Write-Host "  - 检查 Nacos..." -NoNewline
$nacosRunning = Test-NetConnection -ComputerName localhost -Port 8849 -WarningAction SilentlyContinue
if ($nacosRunning.TcpTestSucceeded) {
    Write-Host " ✓" -ForegroundColor Green
} else {
    Write-Host " ✗ Nacos未运行" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 初始化数据库
Write-Host "[2/4] 初始化数据库..." -ForegroundColor Yellow
$initSql = "D:\program\Market\doc\SQL\init_product_data.sql"
if (Test-Path $initSql) {
    Write-Host "  - 执行初始化脚本..." -NoNewline
    try {
        mysql -u root -p123456789 < $initSql 2>$null
        Write-Host " ✓" -ForegroundColor Green
    } catch {
        Write-Host " ⚠ (可能已初始化)" -ForegroundColor Yellow
    }
} else {
    Write-Host "  - 未找到初始化脚本，跳过" -ForegroundColor Yellow
}

Write-Host ""

# 编译项目
Write-Host "[3/4] 编译项目..." -ForegroundColor Yellow
Set-Location "D:\program\Market\market-service\market-service-product"
mvn clean compile -DskipTests -q
if ($LASTEXITCODE -eq 0) {
    Write-Host "  - 编译成功 ✓" -ForegroundColor Green
} else {
    Write-Host "  - 编译失败 ✗" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 启动服务
Write-Host "[4/4] 启动服务..." -ForegroundColor Yellow
Write-Host "  - 端口: 8102" -ForegroundColor Cyan
Write-Host "  - 服务名: market-service-product" -ForegroundColor Cyan
Write-Host ""
Write-Host "正在启动..." -ForegroundColor Yellow
Write-Host ""

mvn spring-boot:run

