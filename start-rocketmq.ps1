# RocketMQ 启动脚本
# 说明：RocketMQ 是可选组件，不启动不影响消息功能

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "     RocketMQ 启动脚本（可选）" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 Docker 是否运行
$dockerStatus = docker info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker 未运行，请先启动 Docker Desktop" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Docker 正在运行" -ForegroundColor Green
Write-Host ""

# 检查是否已经运行
$existing = docker ps -a --filter "name=rmq" --format "{{.Names}}"
if ($existing) {
    Write-Host "⚠️  检测到已存在的 RocketMQ 容器" -ForegroundColor Yellow
    Write-Host "容器列表: $existing" -ForegroundColor Yellow
    Write-Host ""
    $choice = Read-Host "是否删除并重新创建? (y/n)"
    if ($choice -eq "y") {
        Write-Host "正在停止并删除旧容器..." -ForegroundColor Yellow
        docker-compose -f docker-compose-rocketmq.yml down -v
        Write-Host "✅ 旧容器已删除" -ForegroundColor Green
    } else {
        Write-Host "正在启动现有容器..." -ForegroundColor Yellow
        docker-compose -f docker-compose-rocketmq.yml start
        exit 0
    }
}

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "正在启动 RocketMQ 服务..." -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# 启动服务
docker-compose -f docker-compose-rocketmq.yml up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "===========================================" -ForegroundColor Green
    Write-Host "     ✅ RocketMQ 启动成功！" -ForegroundColor Green
    Write-Host "===========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 服务信息:" -ForegroundColor Cyan
    Write-Host "  - NameServer: localhost:9876"
    Write-Host "  - Broker: localhost:10911"
    Write-Host "  - 控制台: http://localhost:8180"
    Write-Host ""
    Write-Host "🔍 检查服务状态:" -ForegroundColor Cyan
    docker-compose -f docker-compose-rocketmq.yml ps
    Write-Host ""
    Write-Host "📝 查看日志:" -ForegroundColor Cyan
    Write-Host "  docker logs rmqnamesrv"
    Write-Host "  docker logs rmqbroker"
    Write-Host "  docker logs rmqconsole"
    Write-Host ""
    Write-Host "⏹️  停止服务:" -ForegroundColor Cyan
    Write-Host "  docker-compose -f docker-compose-rocketmq.yml stop"
    Write-Host ""

    Write-Host "⏳ 等待服务完全启动（约10秒）..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10

    Write-Host "✅ RocketMQ 已就绪！" -ForegroundColor Green
    Write-Host ""
    Write-Host "💡 提示: 现在可以重启消息服务来使用 RocketMQ" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "❌ RocketMQ 启动失败" -ForegroundColor Red
    Write-Host "请查看错误信息并重试" -ForegroundColor Red
    exit 1
}

