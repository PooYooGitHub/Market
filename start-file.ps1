# File 服务启动脚本
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "启动 File 服务 (端口: 8106)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 切换到项目目录
Set-Location "D:\program\Market\market-service\market-service-file"

# 检查 MinIO 是否运行
Write-Host ""
Write-Host "检查 MinIO 状态..." -ForegroundColor Yellow
$minioRunning = docker ps | Select-String "minio"
if ($minioRunning) {
    Write-Host "✓ MinIO 正在运行" -ForegroundColor Green
} else {
    Write-Host "✗ MinIO 未运行，请先启动 MinIO" -ForegroundColor Red
    Write-Host "  启动命令: docker start minio" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "正在启动 File 服务..." -ForegroundColor Yellow
Write-Host ""

# 启动服务
mvn spring-boot:run

