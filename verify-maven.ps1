# Maven 配置验证脚本
# PowerShell Script

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Maven 项目配置验证" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 Maven 是否安装
Write-Host "1. 检查 Maven 安装..." -ForegroundColor Yellow
$mavenVersion = mvn -v 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Maven 已安装" -ForegroundColor Green
    Write-Host $mavenVersion -ForegroundColor Gray
} else {
    Write-Host "❌ Maven 未安装或未配置环境变量！" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 检查 JDK 版本
Write-Host "2. 检查 JDK 版本..." -ForegroundColor Yellow
$javaVersion = java -version 2>&1
if ($javaVersion -match "version") {
    Write-Host "✅ JDK 已安装" -ForegroundColor Green
    Write-Host $javaVersion -ForegroundColor Gray
} else {
    Write-Host "❌ JDK 未安装或未配置环境变量！" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 验证 pom.xml 文件
Write-Host "3. 验证 pom.xml 文件..." -ForegroundColor Yellow
if (Test-Path "pom.xml") {
    Write-Host "✅ pom.xml 文件存在" -ForegroundColor Green
} else {
    Write-Host "❌ pom.xml 文件不存在！" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 验证 Maven 项目结构
Write-Host "4. 验证 Maven 项目..." -ForegroundColor Yellow
mvn validate
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Maven 项目验证通过" -ForegroundColor Green
} else {
    Write-Host "❌ Maven 项目验证失败！" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 下载依赖（不执行构建）
Write-Host "5. 下载依赖（可能需要较长时间）..." -ForegroundColor Yellow
mvn dependency:resolve
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 依赖下载完成" -ForegroundColor Green
} else {
    Write-Host "⚠️  部分依赖下载失败（可能是因为子模块尚未创建）" -ForegroundColor Yellow
}
Write-Host ""

# 显示依赖树
Write-Host "6. 显示依赖管理信息..." -ForegroundColor Yellow
mvn dependency:tree -DoutputFile=dependency-tree.txt -Dverbose=true
if (Test-Path "dependency-tree.txt") {
    Write-Host "✅ 依赖树已生成：dependency-tree.txt" -ForegroundColor Green
}
Write-Host ""

# 检查插件
Write-Host "7. 检查 Maven 插件..." -ForegroundColor Yellow
mvn help:effective-pom -Doutput=effective-pom.xml
if (Test-Path "effective-pom.xml") {
    Write-Host "✅ 有效 POM 已生成：effective-pom.xml" -ForegroundColor Green
}
Write-Host ""

# 总结
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  验证完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "项目信息：" -ForegroundColor Yellow
Write-Host "  GroupId:    org.shyu" -ForegroundColor White
Write-Host "  ArtifactId: Market" -ForegroundColor White
Write-Host "  Version:    1.0-SNAPSHOT" -ForegroundColor White
Write-Host "  Packaging:  pom" -ForegroundColor White
Write-Host ""
Write-Host "下一步：" -ForegroundColor Yellow
Write-Host "  1. 创建子模块" -ForegroundColor White
Write-Host "  2. 配置 Nacos" -ForegroundColor White
Write-Host "  3. 开始开发" -ForegroundColor White
Write-Host ""
Write-Host "查看依赖树: Get-Content dependency-tree.txt" -ForegroundColor Gray
Write-Host "查看有效POM: Get-Content effective-pom.xml" -ForegroundColor Gray
Write-Host ""

