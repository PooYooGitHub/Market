# IDEA Maven 刷新脚本
# 用于解决"无法解析符号"的问题

Write-Host "==> 正在刷新 Maven 项目..." -ForegroundColor Green

# 进入项目目录
$projectPath = "D:\program\Market"
Set-Location $projectPath

Write-Host "当前目录: $projectPath" -ForegroundColor Yellow

# 清理并重新编译
Write-Host "`n[1/3] 清理旧的编译文件..." -ForegroundColor Cyan
mvn clean -q

Write-Host "`n[2/3] 重新下载依赖..." -ForegroundColor Cyan
mvn dependency:resolve -q

Write-Host "`n[3/3] 编译项目..." -ForegroundColor Cyan
mvn compile -DskipTests

Write-Host "`n✅ Maven 刷新完成!" -ForegroundColor Green
Write-Host "`n📌 下一步操作（在 IDEA 中）：" -ForegroundColor Yellow
Write-Host "   1. 右键点击项目根目录或 market-service-message 模块" -ForegroundColor White
Write-Host "   2. 选择 'Maven' → 'Reload Project'" -ForegroundColor White
Write-Host "   3. 或点击 IDEA 右侧 Maven 工具栏的 🔄 刷新按钮" -ForegroundColor White
Write-Host "`n如果还是报错，请执行：" -ForegroundColor Yellow
Write-Host "   File → Invalidate Caches... → Invalidate and Restart" -ForegroundColor White

Write-Host "`n💡 提示：这些错误不影响实际运行，只是 IDEA 索引问题" -ForegroundColor Magenta

