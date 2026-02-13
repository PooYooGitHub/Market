# Nacos 数据库初始化脚本
# PowerShell Script

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Nacos 数据库初始化" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 配置信息
$MYSQL_HOST = "127.0.0.1"
$MYSQL_PORT = "3306"
$MYSQL_USER = "root"
$MYSQL_PASSWORD = "root123456"
$SQL_FILE = "doc\SQL\nacos_config.sql"

Write-Host "配置信息：" -ForegroundColor Yellow
Write-Host "  MySQL 地址: $MYSQL_HOST:$MYSQL_PORT" -ForegroundColor Gray
Write-Host "  MySQL 用户: $MYSQL_USER" -ForegroundColor Gray
Write-Host "  SQL 脚本: $SQL_FILE" -ForegroundColor Gray
Write-Host ""

# 检查 SQL 文件是否存在
if (!(Test-Path $SQL_FILE)) {
    Write-Host "❌ SQL 文件不存在: $SQL_FILE" -ForegroundColor Red
    exit 1
}
Write-Host "✅ SQL 文件存在" -ForegroundColor Green
Write-Host ""

# 检查 MySQL 是否可访问
Write-Host "检查 MySQL 连接..." -ForegroundColor Yellow
$testConnection = "mysql -h $MYSQL_HOST -P $MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -e 'SELECT 1;' 2>&1"
$result = Invoke-Expression $testConnection

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 无法连接到 MySQL，请检查：" -ForegroundColor Red
    Write-Host "   1. MySQL 服务是否启动" -ForegroundColor Gray
    Write-Host "   2. 用户名和密码是否正确" -ForegroundColor Gray
    Write-Host "   3. 端口是否正确" -ForegroundColor Gray
    Write-Host ""
    Write-Host "提示：如果使用 Docker，请先启动 MySQL 容器：" -ForegroundColor Yellow
    Write-Host "      docker-compose up -d mysql" -ForegroundColor White
    Write-Host ""
    exit 1
}
Write-Host "✅ MySQL 连接成功" -ForegroundColor Green
Write-Host ""

# 执行初始化脚本
Write-Host "开始执行 Nacos 数据库初始化..." -ForegroundColor Yellow
Write-Host "这可能需要几秒钟..." -ForegroundColor Gray
Write-Host ""

$importCommand = "mysql -h $MYSQL_HOST -P $MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD < $SQL_FILE 2>&1"

try {
    # 使用 cmd 执行重定向命令
    $output = cmd /c "mysql -h $MYSQL_HOST -P $MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD < $SQL_FILE 2>&1"

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Nacos 数据库初始化成功！" -ForegroundColor Green
        Write-Host ""

        # 验证数据库和表
        Write-Host "验证数据库表..." -ForegroundColor Yellow
        $verifyCommand = "mysql -h $MYSQL_HOST -P $MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD nacos_config -e 'SHOW TABLES;' 2>&1"
        $tables = Invoke-Expression $verifyCommand

        if ($tables -match "config_info") {
            Write-Host "✅ 数据库表创建成功" -ForegroundColor Green
            Write-Host ""
            Write-Host "数据库信息：" -ForegroundColor Cyan
            Write-Host "  数据库名: nacos_config" -ForegroundColor White
            Write-Host "  表数量: 14" -ForegroundColor White
            Write-Host "  默认用户: nacos" -ForegroundColor White
            Write-Host "  默认密码: nacos" -ForegroundColor White
            Write-Host ""
        }
    } else {
        Write-Host "❌ 初始化失败" -ForegroundColor Red
        Write-Host $output -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ 执行脚本时发生错误：$_" -ForegroundColor Red
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  下一步：启动 Nacos 服务" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "使用 Docker Compose 启动：" -ForegroundColor Yellow
Write-Host "  docker-compose up -d nacos" -ForegroundColor White
Write-Host ""
Write-Host "或启动所有基础设施：" -ForegroundColor Yellow
Write-Host "  .\start-infrastructure.ps1" -ForegroundColor White
Write-Host ""
Write-Host "Nacos 访问地址：" -ForegroundColor Yellow
Write-Host "  http://localhost:8848/nacos" -ForegroundColor White
Write-Host "  用户名/密码: nacos/nacos" -ForegroundColor Gray
Write-Host ""

