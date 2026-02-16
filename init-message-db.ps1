# 初始化Message服务数据库
# 执行market_message.sql创建数据库和表

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "初始化Message服务数据库" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# MySQL连接配置
$mysqlHost = "localhost"
$mysqlPort = "3306"
$mysqlUser = "root"
$mysqlPassword = "123456"
$sqlFile = ".\market-service\market-service-message\src\main\resources\sql\market_message.sql"

# 检查SQL文件是否存在
if (-not (Test-Path $sqlFile)) {
    Write-Host "❌ SQL文件不存在: $sqlFile" -ForegroundColor Red
    Write-Host "请确保在项目根目录下执行此脚本" -ForegroundColor Yellow
    exit 1
}

Write-Host "📄 SQL文件: $sqlFile" -ForegroundColor Green
Write-Host ""

# 检查MySQL是否可连接
Write-Host "🔍 检查MySQL连接..." -ForegroundColor Yellow
$testConnection = "SELECT 1"
try {
    $testResult = mysql -h$mysqlHost -P$mysqlPort -u$mysqlUser -p$mysqlPassword -e $testConnection 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "连接失败"
    }
    Write-Host "✅ MySQL连接成功" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ 无法连接到MySQL服务器" -ForegroundColor Red
    Write-Host "请检查:" -ForegroundColor Yellow
    Write-Host "  1. MySQL服务是否启动" -ForegroundColor Yellow
    Write-Host "  2. 连接配置是否正确 (host=$mysqlHost, port=$mysqlPort, user=$mysqlUser)" -ForegroundColor Yellow
    Write-Host "  3. 密码是否正确" -ForegroundColor Yellow
    exit 1
}

# 执行SQL脚本
Write-Host "🚀 开始执行SQL脚本..." -ForegroundColor Yellow
Write-Host ""

try {
    # 执行SQL文件
    $result = mysql -h$mysqlHost -P$mysqlPort -u$mysqlUser -p$mysqlPassword < $sqlFile 2>&1

    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ SQL执行失败" -ForegroundColor Red
        Write-Host $result -ForegroundColor Red
        exit 1
    }

    Write-Host "✅ SQL脚本执行成功!" -ForegroundColor Green
    Write-Host ""

    # 验证表是否创建成功
    Write-Host "🔍 验证表结构..." -ForegroundColor Yellow
    $checkTables = "USE market_message; SHOW TABLES;"
    $tables = mysql -h$mysqlHost -P$mysqlPort -u$mysqlUser -p$mysqlPassword -e $checkTables 2>&1

    if ($tables -match "t_chat_message" -and $tables -match "t_conversation") {
        Write-Host "✅ 表创建成功:" -ForegroundColor Green
        Write-Host "  - t_chat_message (聊天消息表)" -ForegroundColor Green
        Write-Host "  - t_conversation (会话表)" -ForegroundColor Green
        Write-Host ""

        # 显示表结构
        Write-Host "📊 t_chat_message 表结构:" -ForegroundColor Cyan
        $chatMessageStructure = mysql -h$mysqlHost -P$mysqlPort -u$mysqlUser -p$mysqlPassword -e "USE market_message; DESC t_chat_message;" 2>&1
        Write-Host $chatMessageStructure
        Write-Host ""

        Write-Host "📊 t_conversation 表结构:" -ForegroundColor Cyan
        $conversationStructure = mysql -h$mysqlHost -P$mysqlPort -u$mysqlUser -p$mysqlPassword -e "USE market_message; DESC t_conversation;" 2>&1
        Write-Host $conversationStructure
        Write-Host ""

        # 检查测试数据
        Write-Host "🔍 检查测试数据..." -ForegroundColor Yellow
        $messageCount = mysql -h$mysqlHost -P$mysqlPort -u$mysqlUser -p$mysqlPassword -e "USE market_message; SELECT COUNT(*) as count FROM t_chat_message;" -s -N 2>&1
        $conversationCount = mysql -h$mysqlHost -P$mysqlPort -u$mysqlUser -p$mysqlPassword -e "USE market_message; SELECT COUNT(*) as count FROM t_conversation;" -s -N 2>&1

        Write-Host "  - t_chat_message: $messageCount 条记录" -ForegroundColor Green
        Write-Host "  - t_conversation: $conversationCount 条记录" -ForegroundColor Green
        Write-Host ""
    } else {
        Write-Host "⚠️  表验证失败，请检查SQL执行结果" -ForegroundColor Yellow
    }

} catch {
    Write-Host "❌ 执行过程中出现错误:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ Message服务数据库初始化完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📝 下一步:" -ForegroundColor Yellow
Write-Host "  1. 重启Message服务 (如果正在运行)" -ForegroundColor Yellow
Write-Host "  2. 访问消息页面测试功能" -ForegroundColor Yellow
Write-Host ""

