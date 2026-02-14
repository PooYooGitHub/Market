# 重启Nacos容器并添加gRPC端口映射
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  重新启动 Nacos 服务" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 停止并删除旧容器
Write-Host "停止并删除旧的Nacos容器..." -ForegroundColor Yellow
docker stop nacos 2>$null
docker rm nacos 2>$null
Write-Host "✅ 旧容器已清理" -ForegroundColor Green
Write-Host ""

# 启动新容器，添加gRPC端口映射
Write-Host "启动新的Nacos容器（包含gRPC端口映射）..." -ForegroundColor Yellow
docker run -d `
  --name nacos `
  -p 8849:8848 `
  -p 9849:9848 `
  -p 9850:9849 `
  -e MODE=standalone `
  -e PREFER_HOST_MODE=ip `
  -e TIME_ZONE=Asia/Shanghai `
  -e JVM_XMS=512m `
  -e JVM_XMX=512m `
  -e JVM_XMN=256m `
  -e NACOS_AUTH_ENABLE=true `
  nacos/nacos-server:v2.2.3

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Nacos容器启动成功" -ForegroundColor Green
    Write-Host ""
    Write-Host "等待Nacos完全启动（约30秒）..." -ForegroundColor Yellow

    $seconds = 30
    for ($i = 1; $i -le $seconds; $i++) {
        Write-Progress -Activity "等待Nacos启动" -Status "已等待 $i/$seconds 秒" -PercentComplete (($i / $seconds) * 100)
        Start-Sleep -Seconds 1
    }
    Write-Progress -Activity "等待Nacos启动" -Completed

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  🎉 Nacos 启动完成！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "端口映射：" -ForegroundColor Yellow
    Write-Host "  HTTP端口:   8849 -> 8848" -ForegroundColor White
    Write-Host "  gRPC端口:   9849 -> 9848 (客户端连接)" -ForegroundColor White
    Write-Host "  gRPC端口:   9850 -> 9849 (服务端连接)" -ForegroundColor White
    Write-Host ""
    Write-Host "访问地址：" -ForegroundColor Yellow
    Write-Host "  控制台: http://localhost:8849/nacos" -ForegroundColor White
    Write-Host "  用户名: nacos" -ForegroundColor Gray
    Write-Host "  密码:   nacos" -ForegroundColor Gray
    Write-Host ""
    Write-Host "查看日志: docker logs -f nacos" -ForegroundColor Yellow
    Write-Host "查看状态: docker ps | findstr nacos" -ForegroundColor Yellow
    Write-Host ""
} else {
    Write-Host "❌ Nacos容器启动失败" -ForegroundColor Red
    Write-Host "请查看错误信息" -ForegroundColor Yellow
    exit 1
}

