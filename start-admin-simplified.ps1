# 简化后的管理后台启动脚本

echo "=== 启动简化后的仲裁管理系统 ==="

# 检查是否在正确的目录
if (!(Test-Path "package.json")) {
    Write-Host "请在前端项目目录中运行此脚本"
    Write-Host "正在切换到前端目录..."
    Set-Location "front\vue-project"
    if (!(Test-Path "package.json")) {
        Write-Host "错误：找不到前端项目目录" -ForegroundColor Red
        exit 1
    }
}

Write-Host "1. 检查依赖..." -ForegroundColor Green
if (!(Test-Path "node_modules")) {
    Write-Host "正在安装依赖..."
    npm install
}

Write-Host "2. 启动开发服务器..." -ForegroundColor Green
Write-Host "管理后台访问地址：" -ForegroundColor Yellow
Write-Host "  - 管理员登录: http://localhost:5173/admin/login"
Write-Host "  - 工作台: http://localhost:5173/admin/dashboard"
Write-Host "  - 待处理仲裁: http://localhost:5173/admin/arbitration/pending"
Write-Host "  - 数据概览: http://localhost:5173/admin/statistics/overview"
Write-Host ""
Write-Host "用户端访问地址：" -ForegroundColor Yellow
Write-Host "  - 跳蚤市场主页: http://localhost:5173/"
Write-Host "  - 商品列表: http://localhost:5173/products"
Write-Host "  - 用户登录: http://localhost:5173/login"
Write-Host ""
Write-Host "功能说明：" -ForegroundColor Cyan
Write-Host "  ✅ 仲裁管理 - 专业的争议处理工具" -ForegroundColor Green
Write-Host "  ✅ 数据统计 - 完整的分析报表系统" -ForegroundColor Green
Write-Host "  ❌ 用户管理 - 已移除" -ForegroundColor Red
Write-Host "  ❌ 商品管理 - 已移除" -ForegroundColor Red
Write-Host "  ❌ 系统管理 - 已移除" -ForegroundColor Red
Write-Host ""

npm run dev