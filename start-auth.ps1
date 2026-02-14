# 启动market-auth服务
# 此脚本包含解决Nacos gRPC连接问题的JVM参数

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  启动 market-auth 认证服务" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Nacos是否运行
Write-Host "检查 Nacos 运行状态..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://127.0.0.1:8849/nacos/v1/console/server/state" -Method Get -UseBasicParsing -TimeoutSec 3
    Write-Host "✅ Nacos 运行正常" -ForegroundColor Green
} catch {
    Write-Host "❌ Nacos 未运行或无法访问！" -ForegroundColor Red
    Write-Host "请先启动 Nacos 服务器" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# 检查Redis是否运行
Write-Host "检查 Redis 运行状态..." -ForegroundColor Yellow
$redisRunning = Test-NetConnection -ComputerName 127.0.0.1 -Port 6379 -WarningAction SilentlyContinue
if ($redisRunning.TcpTestSucceeded) {
    Write-Host "✅ Redis 运行正常" -ForegroundColor Green
} else {
    Write-Host "⚠️  Redis 未运行！" -ForegroundColor Yellow
    Write-Host "服务可能无法完全启动" -ForegroundColor Yellow
}
Write-Host ""

# 设置Java环境
$JAVA_HOME = "D:\program\JDK"
$JAVA_EXE = "$JAVA_HOME\bin\java.exe"

if (!(Test-Path $JAVA_EXE)) {
    Write-Host "❌ 找不到Java: $JAVA_EXE" -ForegroundColor Red
    Write-Host "请检查JAVA_HOME配置" -ForegroundColor Yellow
    exit 1
}

# 切换到项目目录
Set-Location "$PSScriptRoot\market-auth"

# 检查JAR文件是否存在
$JAR_FILE = "target\market-auth-1.0-SNAPSHOT.jar"
if (!(Test-Path $JAR_FILE)) {
    Write-Host "❌ 找不到JAR文件: $JAR_FILE" -ForegroundColor Red
    Write-Host "请先运行 mvn clean install" -ForegroundColor Yellow
    exit 1
}

Write-Host "启动 market-auth 服务..." -ForegroundColor Yellow
Write-Host "端口: 8081" -ForegroundColor Gray
Write-Host ""

# JVM参数说明：
# -Xms256m -Xmx512m: 内存设置
# -Dnacos.remote.client.grpc.enable=false: 禁用gRPC，使用HTTP模式（解决连接问题）
# -Dspring.profiles.active=dev: 激活dev配置
$JVM_OPTS = @(
    "-Xms256m",
    "-Xmx512m",
    "-Dnacos.remote.client.grpc.enable=false",
    "-Dspring.profiles.active=dev",
    "-Dfile.encoding=UTF-8"
)

# 启动应用
& $JAVA_EXE $JVM_OPTS -jar $JAR_FILE

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  market-auth 服务已停止" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

