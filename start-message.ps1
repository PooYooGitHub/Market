# 启动消息服务
Write-Host "正在启动消息服务..." -ForegroundColor Green

# 编译项目
cd D:\program\Market\market-service\market-service-message
mvn clean compile

if ($LASTEXITCODE -eq 0) {
    Write-Host "编译成功,启动服务..." -ForegroundColor Green
    mvn spring-boot:run
} else {
    Write-Host "编译失败!" -ForegroundColor Red
}

