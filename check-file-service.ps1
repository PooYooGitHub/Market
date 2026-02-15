# File 服务快速测试脚本
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试 File 服务启动" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 检查 MinIO 是否运行
Write-Host ""
Write-Host "1. 检查 MinIO 状态..." -ForegroundColor Yellow
$minioRunning = docker ps --filter "name=minio" --format "{{.Names}}" | Select-String "minio"
if ($minioRunning) {
    Write-Host "   ✓ MinIO 正在运行" -ForegroundColor Green
} else {
    Write-Host "   ✗ MinIO 未运行" -ForegroundColor Red
    Write-Host "   启动命令: docker start minio" -ForegroundColor Yellow
    exit 1
}

# 检查 Nacos 是否运行
Write-Host ""
Write-Host "2. 检查 Nacos 状态..." -ForegroundColor Yellow
$nacosRunning = docker ps --filter "name=nacos" --format "{{.Names}}" | Select-String "nacos"
if ($nacosRunning) {
    Write-Host "   ✓ Nacos 正在运行" -ForegroundColor Green
} else {
    Write-Host "   ✗ Nacos 未运行" -ForegroundColor Red
    Write-Host "   启动命令: docker start nacos" -ForegroundColor Yellow
    exit 1
}

# 检查端口 8106 是否被占用
Write-Host ""
Write-Host "3. 检查端口 8106..." -ForegroundColor Yellow
$portInUse = netstat -ano | Select-String ":8106" | Select-String "LISTENING"
if ($portInUse) {
    Write-Host "   ✗ 端口 8106 已被占用" -ForegroundColor Red
    Write-Host "   请先关闭占用该端口的程序" -ForegroundColor Yellow
    exit 1
} else {
    Write-Host "   ✓ 端口 8106 可用" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "所有检查通过！准备启动服务..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "启动方式：" -ForegroundColor Yellow
Write-Host "1. Maven 启动: cd market-service\market-service-file && mvn spring-boot:run" -ForegroundColor White
Write-Host "2. IDEA 启动: 打开 MarketServiceFileApplication.java 右键运行" -ForegroundColor White
Write-Host ""
Write-Host "启动成功后应该看到：" -ForegroundColor Yellow
Write-Host "- Tomcat started on port(s): 8106 (http)" -ForegroundColor White
Write-Host "- Nacos registry, DEFAULT_GROUP market-service-file register finished" -ForegroundColor White
Write-Host ""

