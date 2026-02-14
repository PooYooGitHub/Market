# Gateway 测试脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Gateway 模块测试脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 配置
$GATEWAY_URL = "http://localhost:9000"

# 测试 1: 健康检查
Write-Host "测试 1: 健康检查" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$GATEWAY_URL/health" -Method Get
    Write-Host "✅ 健康检查通过" -ForegroundColor Green
    Write-Host "   状态: $($response.status)"
    Write-Host "   Redis: $($response.redis)"
} catch {
    Write-Host "❌ 健康检查失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试 2: 访问白名单（商品列表）
Write-Host "测试 2: 访问白名单接口（无需Token）" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$GATEWAY_URL/api/product/list" -Method Get -ErrorAction Stop
    Write-Host "✅ 白名单接口访问成功" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode.Value__ -eq 404) {
        Write-Host "⚠️  Product 服务未启动（这是正常的）" -ForegroundColor Yellow
    } else {
        Write-Host "❌ 访问失败: $_" -ForegroundColor Red
    }
}
Write-Host ""

# 测试 3: 无 Token 访问受保护接口
Write-Host "测试 3: 无 Token 访问受保护接口" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$GATEWAY_URL/api/user/info" -Method Get -ErrorAction Stop
    Write-Host "❌ 应该返回 401，但返回了成功" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.Value__ -eq 401) {
        Write-Host "✅ 正确返回 401 未授权" -ForegroundColor Green
    } else {
        Write-Host "⚠️  返回了其他错误: $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor Yellow
    }
}
Write-Host ""

# 测试 4: 使用无效 Token
Write-Host "测试 4: 使用无效 Token 访问" -ForegroundColor Yellow
try {
    $headers = @{
        "satoken" = "invalid-token-12345"
    }
    $response = Invoke-RestMethod -Uri "$GATEWAY_URL/api/user/info" -Method Get -Headers $headers -ErrorAction Stop
    Write-Host "❌ 应该返回 401，但返回了成功" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.Value__ -eq 401) {
        Write-Host "✅ 正确返回 401 Token无效" -ForegroundColor Green
    } else {
        Write-Host "⚠️  返回了其他错误: $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor Yellow
    }
}
Write-Host ""

# 测试 5: 登录测试（需要 User 服务）
Write-Host "测试 5: 用户登录" -ForegroundColor Yellow
try {
    $loginData = @{
        username = "test"
        password = "123456"
    } | ConvertTo-Json

    $headers = @{
        "Content-Type" = "application/json"
    }

    $response = Invoke-RestMethod -Uri "$GATEWAY_URL/api/user/login" -Method Post -Body $loginData -Headers $headers -ErrorAction Stop

    if ($response.code -eq 200) {
        Write-Host "✅ 登录成功" -ForegroundColor Green
        Write-Host "   Token: $($response.data.token.Substring(0, [Math]::Min(20, $response.data.token.Length)))..."
        Write-Host "   UserID: $($response.data.userId)"

        # 测试 6: 使用有效 Token 访问
        Write-Host ""
        Write-Host "测试 6: 使用有效 Token 访问" -ForegroundColor Yellow
        $tokenHeaders = @{
            "satoken" = $response.data.token
        }

        try {
            $userInfoResponse = Invoke-RestMethod -Uri "$GATEWAY_URL/api/user/info" -Method Get -Headers $tokenHeaders -ErrorAction Stop
            Write-Host "✅ Token 验证通过，成功获取用户信息" -ForegroundColor Green
        } catch {
            Write-Host "❌ Token 验证失败: $_" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ 登录失败: $($response.message)" -ForegroundColor Red
    }
} catch {
    if ($_.Exception.Response.StatusCode.Value__ -eq 404) {
        Write-Host "⚠️  User 服务未启动" -ForegroundColor Yellow
        Write-Host "   请先启动 User 服务: cd market-service\market-service-user; mvn spring-boot:run"
    } else {
        Write-Host "❌ 登录测试失败: $_" -ForegroundColor Red
    }
}
Write-Host ""

# 总结
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "说明：" -ForegroundColor Yellow
Write-Host "  ✅ = 测试通过"
Write-Host "  ❌ = 测试失败"
Write-Host "  ⚠️  = 需要启动其他服务"
Write-Host ""
Write-Host "下一步："
Write-Host "  1. 如果 User 服务未启动，请启动它"
Write-Host "  2. 重新运行此测试脚本"
Write-Host "  3. 查看完整文档: GATEWAY_COMPLETE.md"
Write-Host ""

