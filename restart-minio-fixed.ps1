端口# MinIO 数据持久化修复脚本
# PowerShell Script

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MinIO 数据持久化修复工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 停止并删除旧容器
Write-Host "[1/5] 清理旧容器..." -ForegroundColor Yellow
docker stop minio 2>$null
docker rm minio 2>$null
Write-Host "  ✓ 旧容器已清理" -ForegroundColor Green
Write-Host ""

# 创建数据目录（使用绝对路径）
Write-Host "[2/5] 创建数据目录..." -ForegroundColor Yellow
$minioDataDir = "D:\program\Market\minio\data"
$minioConfigDir = "D:\program\Market\minio\config"

if (!(Test-Path $minioDataDir)) {
    New-Item -ItemType Directory -Path $minioDataDir -Force | Out-Null
    Write-Host "  ✓ 创建数据目录: $minioDataDir" -ForegroundColor Green
} else {
    Write-Host "  ✓ 数据目录已存在: $minioDataDir" -ForegroundColor Green
}

if (!(Test-Path $minioConfigDir)) {
    New-Item -ItemType Directory -Path $minioConfigDir -Force | Out-Null
    Write-Host "  ✓ 创建配置目录: $minioConfigDir" -ForegroundColor Green
} else {
    Write-Host "  ✓ 配置目录已存在: $minioConfigDir" -ForegroundColor Green
}
Write-Host ""

# 检查端口占用
Write-Host "[3/5] 检查端口占用..." -ForegroundColor Yellow
$port9900 = netstat -ano | Select-String ":9900" | Select-String "LISTENING"
$port9090 = netstat -ano | Select-String ":9090" | Select-String "LISTENING"

if ($port9900) {
    Write-Host "  ⚠ 警告: 端口 9900 已被占用" -ForegroundColor Red
    Write-Host "  请手动停止占用该端口的程序" -ForegroundColor Red
    exit 1
}
if ($port9090) {
    Write-Host "  ⚠ 警告: 端口 9090 已被占用" -ForegroundColor Red
    Write-Host "  请手动停止占用该端口的程序" -ForegroundColor Red
    exit 1
}
Write-Host "  ✓ 端口检查通过" -ForegroundColor Green
Write-Host ""

# 启动容器
Write-Host "[4/5] 启动 MinIO 容器..." -ForegroundColor Yellow
docker run -d `
  --name minio `
  --restart=unless-stopped `
  -p 9900:9000 `
  -p 9090:9090 `
  -e "MINIO_ROOT_USER=minioadmin" `
  -e "MINIO_ROOT_PASSWORD=minioadmin123" `
  -v "${minioDataDir}:/data" `
  -v "${minioConfigDir}:/root/.minio" `
  minio/minio:RELEASE.2021-06-17T00-10-46Z server /data --console-address ":9090"

if ($LASTEXITCODE -ne 0) {
    Write-Host "  ✗ MinIO 启动失败" -ForegroundColor Red
    exit 1
}

Write-Host "  ✓ MinIO 容器已启动" -ForegroundColor Green
Write-Host ""

# 等待 MinIO 启动
Write-Host "[5/5] 等待 MinIO 服务就绪..." -ForegroundColor Yellow
Start-Sleep -Seconds 8

# 检查容器状态
$containerStatus = docker ps --filter "name=minio" --format "{{.Status}}"
if ($containerStatus -match "Up") {
    Write-Host "  ✓ MinIO 服务运行正常" -ForegroundColor Green
} else {
    Write-Host "  ✗ MinIO 服务异常" -ForegroundColor Red
    Write-Host ""
    Write-Host "查看容器日志:" -ForegroundColor Yellow
    docker logs minio --tail 20
    exit 1
}
Write-Host ""

# 显示访问信息
Write-Host "========================================" -ForegroundColor Green
Write-Host "  MinIO 启动成功！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "访问信息:" -ForegroundColor Cyan
Write-Host "  API端点:  http://localhost:9900" -ForegroundColor White
Write-Host "  控制台:   http://localhost:9090" -ForegroundColor White
Write-Host "  用户名:   minioadmin" -ForegroundColor White
Write-Host "  密码:     minioadmin123" -ForegroundColor White
Write-Host ""
Write-Host "数据持久化:" -ForegroundColor Cyan
Write-Host "  数据目录: $minioDataDir" -ForegroundColor White
Write-Host "  配置目录: $minioConfigDir" -ForegroundColor White
Write-Host ""
Write-Host "容器信息:" -ForegroundColor Cyan
docker ps --filter "name=minio" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
Write-Host ""

# 提示：创建桶
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  接下来需要创建存储桶" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "方法 1: 访问控制台 http://localhost:9090" -ForegroundColor White
Write-Host "  1. 登录 MinIO 控制台" -ForegroundColor White
Write-Host "  2. 点击 'Buckets' -> 'Create Bucket'" -ForegroundColor White
Write-Host "  3. 创建以下桶:" -ForegroundColor White
Write-Host "     - market-avatar" -ForegroundColor Cyan
Write-Host "     - market-product" -ForegroundColor Cyan
Write-Host "  4. 设置桶为 Public 访问" -ForegroundColor White
Write-Host ""
Write-Host "方法 2: 使用 mc 命令行工具" -ForegroundColor White
Write-Host "  docker exec -it minio sh" -ForegroundColor Cyan
Write-Host "  mc alias set local http://localhost:9000 minioadmin minioadmin123" -ForegroundColor Cyan
Write-Host "  mc mb local/market-avatar" -ForegroundColor Cyan
Write-Host "  mc mb local/market-product" -ForegroundColor Cyan
Write-Host "  mc policy set public local/market-avatar" -ForegroundColor Cyan
Write-Host "  mc policy set public local/market-product" -ForegroundColor Cyan
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  脚本执行完成" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

