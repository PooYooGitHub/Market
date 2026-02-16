# Message服务测试启动脚本
Write-Host "=== 开始启动Message服务 ===" -ForegroundColor Green

# 切换到message服务目录
Set-Location "D:\program\Market\market-service\market-service-message"

Write-Host "清理并编译..." -ForegroundColor Yellow
mvn clean compile

if ($LASTEXITCODE -ne 0) {
    Write-Host "编译失败!" -ForegroundColor Red
    exit 1
}

Write-Host "`n启动服务..." -ForegroundColor Yellow
Write-Host "服务将在 http://localhost:8103 运行" -ForegroundColor Cyan
Write-Host "Swagger文档: http://localhost:8103/swagger-ui/" -ForegroundColor Cyan
Write-Host "`n按 Ctrl+C 停止服务`n" -ForegroundColor Gray

mvn spring-boot:run

