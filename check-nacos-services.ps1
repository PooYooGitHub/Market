# Nacos服务注册验证脚本
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Nacos服务注册验证" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查Nacos是否运行
Write-Host "1. 检查Nacos运行状态..." -ForegroundColor Yellow
$nacosRunning = docker ps --filter "name=nacos" --format "{{.Status}}" 2>$null
if ($nacosRunning -like "*Up*") {
    Write-Host "   ✅ Nacos容器运行中: $nacosRunning" -ForegroundColor Green
} else {
    Write-Host "   ❌ Nacos容器未运行" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 2. 检查端口
Write-Host "2. 检查端口状态..." -ForegroundColor Yellow
$port8849 = netstat -ano | Select-String ":8849.*LISTENING"
$port9849 = netstat -ano | Select-String ":9849.*LISTENING"

if ($port8849) {
    Write-Host "   ✅ HTTP端口 8849 正在监听" -ForegroundColor Green
} else {
    Write-Host "   ❌ HTTP端口 8849 未监听" -ForegroundColor Red
}

if ($port9849) {
    Write-Host "   ✅ gRPC端口 9849 正在监听" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  gRPC端口 9849 未监听" -ForegroundColor Yellow
}
Write-Host ""

# 3. 查询命名空间列表
Write-Host "3. 查询Nacos命名空间..." -ForegroundColor Yellow
try {
    $namespaces = Invoke-RestMethod -Uri "http://localhost:8849/nacos/v1/console/namespaces" -Method Get
    if ($namespaces.data) {
        Write-Host "   命名空间列表:" -ForegroundColor White
        foreach ($ns in $namespaces.data) {
            $nsId = $ns.namespace
            $nsName = $ns.namespaceShowName
            if ([string]::IsNullOrEmpty($nsId)) {
                $nsId = "public (默认)"
            }
            Write-Host "   - $nsId : $nsName" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "   ⚠️  无法查询命名空间: $($_.Exception.Message)" -ForegroundColor Yellow
}
Write-Host ""

# 4. 查询public命名空间的服务列表
Write-Host "4. 查询服务列表（public命名空间）..." -ForegroundColor Yellow
try {
    # 不指定namespaceId或使用空字符串表示public命名空间
    $services = Invoke-RestMethod -Uri "http://localhost:8849/nacos/v1/ns/service/list?pageNo=1&pageSize=100" -Method Get

    if ($services.count -gt 0) {
        Write-Host "   找到 $($services.count) 个服务:" -ForegroundColor Green
        foreach ($service in $services.doms) {
            Write-Host "   ✅ $service" -ForegroundColor Green
        }
    } else {
        Write-Host "   ⚠️  未找到任何服务" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ❌ 查询失败: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 5. 专门查询market-auth服务
Write-Host "5. 查询market-auth服务实例..." -ForegroundColor Yellow
try {
    $instances = Invoke-RestMethod -Uri "http://localhost:8849/nacos/v1/ns/instance/list?serviceName=market-auth" -Method Get

    if ($instances.hosts -and $instances.hosts.Count -gt 0) {
        Write-Host "   ✅ 找到market-auth服务实例:" -ForegroundColor Green
        foreach ($host in $instances.hosts) {
            $ip = $host.ip
            $port = $host.port
            $healthy = $host.healthy
            $status = if ($healthy) { "健康" } else { "不健康" }
            $hostInfo = "$ip`:$port - $status"
            Write-Host "   - $hostInfo" -ForegroundColor $(if ($healthy) { "Green" } else { "Red" })
        }
    } else {
        Write-Host "   ❌ 未找到market-auth服务" -ForegroundColor Red
        Write-Host ""
        Write-Host "可能的原因:" -ForegroundColor Yellow
        Write-Host "1. 应用未启动或已停止" -ForegroundColor Gray
        Write-Host "2. 应用使用了不同的命名空间" -ForegroundColor Gray
        Write-Host "3. 服务注册失败" -ForegroundColor Gray
    }
} catch {
    Write-Host "   ❌ 查询失败: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 6. 检查应用是否在运行
Write-Host "6. 检查market-auth应用状态..." -ForegroundColor Yellow
$app8080 = netstat -ano | Select-String ":8080.*LISTENING"
if ($app8080) {
    Write-Host "   ✅ 应用在8080端口运行中" -ForegroundColor Green
    Write-Host "   测试健康检查: curl http://localhost:8080/actuator/health" -ForegroundColor Gray
} else {
    Write-Host "   ❌ 应用未在8080端口运行" -ForegroundColor Red
    Write-Host "   请先启动应用" -ForegroundColor Yellow
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "验证完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "提示:" -ForegroundColor Yellow
Write-Host "- 访问Nacos控制台: http://localhost:8849/nacos" -ForegroundColor White
Write-Host "- 用户名/密码: nacos/nacos" -ForegroundColor Gray
Write-Host "- 在服务管理-服务列表中查看注册的服务" -ForegroundColor Gray
Write-Host ""

