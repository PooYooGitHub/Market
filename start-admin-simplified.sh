#!/bin/bash

# 简化后的管理后台启动脚本

echo "=== 启动简化后的仲裁管理系统 ==="

# 检查是否在前端目录
if [ ! -f "package.json" ]; then
    echo "请在前端项目目录中运行此脚本"
    echo "正在切换到前端目录..."
    cd front/vue-project
    if [ ! -f "package.json" ]; then
        echo "错误：找不到前端项目目录"
        exit 1
    fi
fi

echo "1. 检查依赖..."
if [ ! -d "node_modules" ]; then
    echo "正在安装依赖..."
    npm install
fi

echo "2. 启动开发服务器..."
echo "管理后台访问地址："
echo "  - 管理员登录: http://localhost:3000/admin/login"
echo "  - 工作台: http://localhost:3000/admin/dashboard"
echo "  - 待处理仲裁: http://localhost:3000/admin/arbitration/pending"
echo "  - 数据概览: http://localhost:3000/admin/statistics/overview"
echo ""
echo "功能说明："
echo "  ✅ 仲裁管理 - 专业的争议处理工具"
echo "  ✅ 数据统计 - 完整的分析报表系统"
echo "  ❌ 用户管理 - 已移除"
echo "  ❌ 商品管理 - 已移除"
echo "  ❌ 系统管理 - 已移除"
echo ""

npm run dev