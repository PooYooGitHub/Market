# 校园跳蚤市场平台 - 基础设施启动脚本
# PowerShell Script

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  校园跳蚤市场平台 - 基础设施部署" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Docker是否运行
Write-Host "检查 Docker 运行状态..." -ForegroundColor Yellow
$dockerRunning = docker info 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker 未运行，请先启动 Docker Desktop！" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Docker 运行正常" -ForegroundColor Green
Write-Host ""

# 创建必要的目录
Write-Host "创建数据目录..." -ForegroundColor Yellow
$directories = @(
    "docker\mysql\data",
    "docker\mysql\conf",
    "docker\mysql\logs",
    "docker\redis\data",
    "docker\redis\conf",
    "docker\nacos\data",
    "docker\nacos\logs",
    "docker\elasticsearch\data",
    "docker\elasticsearch\plugins",
    "docker\minio\data",
    "docker\rocketmq\namesrv\logs",
    "docker\rocketmq\namesrv\store",
    "docker\rocketmq\broker\logs",
    "docker\rocketmq\broker\store",
    "docker\rocketmq\broker\conf",
    "docker\seata\config"
)

foreach ($dir in $directories) {
    if (!(Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-Host "  创建目录: $dir" -ForegroundColor Gray
    }
}
Write-Host "✅ 目录创建完成" -ForegroundColor Green
Write-Host ""

# 停止并删除旧容器
Write-Host "清理旧容器..." -ForegroundColor Yellow
docker-compose down 2>&1 | Out-Null
Write-Host "✅ 旧容器清理完成" -ForegroundColor Green
Write-Host ""

# 启动所有服务
Write-Host "启动基础设施服务..." -ForegroundColor Yellow
Write-Host ""
docker-compose up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  🎉 基础设施启动成功！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "服务访问地址：" -ForegroundColor Yellow
    Write-Host "  📦 Nacos:              http://localhost:8848/nacos" -ForegroundColor White
    Write-Host "     用户名/密码:        nacos/nacos" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  🗄️  MySQL:              localhost:3306" -ForegroundColor White
    Write-Host "     用户名/密码:        root/root123456" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  🔴 Redis:              localhost:6379" -ForegroundColor White
    Write-Host "     密码:              redis123456" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  🔍 Elasticsearch:      http://localhost:9200" -ForegroundColor White
    Write-Host "  📊 Kibana:             http://localhost:5601" -ForegroundColor White
    Write-Host ""
    Write-Host "  📁 MinIO:              http://localhost:9000" -ForegroundColor White
    Write-Host "     控制台:            http://localhost:9001" -ForegroundColor White
    Write-Host "     用户名/密码:        minioadmin/minioadmin123" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  📨 RocketMQ Dashboard: http://localhost:8180" -ForegroundColor White
    Write-Host ""
    Write-Host "  🛡️  Sentinel:           http://localhost:8858" -ForegroundColor White
    Write-Host "     用户名/密码:        sentinel/sentinel" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  🔄 Seata:              localhost:8091" -ForegroundColor White
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "查看运行状态: docker-compose ps" -ForegroundColor Yellow
    Write-Host "查看日志:     docker-compose logs -f [服务名]" -ForegroundColor Yellow
    Write-Host "停止服务:     docker-compose stop" -ForegroundColor Yellow
    Write-Host "删除服务:     docker-compose down" -ForegroundColor Yellow
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "❌ 启动失败，请检查日志！" -ForegroundColor Red
    Write-Host "查看日志命令: docker-compose logs" -ForegroundColor Yellow
    exit 1
}

