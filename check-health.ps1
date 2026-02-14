# Market 项目健康检查脚本

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Market 项目健康检查" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$allChecks = @()

# 检查 1: Maven 安装
Write-Host "1. 检查 Maven..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven" | Select-Object -First 1
    if ($mavenVersion) {
        Write-Host "   ✅ Maven 已安装: $mavenVersion" -ForegroundColor Green
        $allChecks += $true
    }
} catch {
    Write-Host "   ❌ Maven 未安装或未配置PATH" -ForegroundColor Red
    $allChecks += $false
}

# 检查 2: Java 版本
Write-Host "2. 检查 Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
    if ($javaVersion) {
        Write-Host "   ✅ Java 已安装: $javaVersion" -ForegroundColor Green
        $allChecks += $true
    }
} catch {
    Write-Host "   ❌ Java 未安装或未配置PATH" -ForegroundColor Red
    $allChecks += $false
}

# 检查 3: Docker 运行状态
Write-Host "3. 检查 Docker..." -ForegroundColor Yellow
try {
    $dockerStatus = docker ps 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✅ Docker 正在运行" -ForegroundColor Green
        $allChecks += $true
    }
} catch {
    Write-Host "   ⚠️  Docker 未运行或未安装（可选）" -ForegroundColor Yellow
    $allChecks += $null
}

# 检查 4: 项目结构
Write-Host "4. 检查项目结构..." -ForegroundColor Yellow
$requiredDirs = @(
    "market-gateway",
    "market-auth",
    "market-service\market-service-core",
    "market-common",
    "docker-compose.yml"
)

$structureOk = $true
foreach ($dir in $requiredDirs) {
    if (Test-Path $dir) {
        Write-Host "   ✅ $dir" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $dir 不存在" -ForegroundColor Red
        $structureOk = $false
    }
}
$allChecks += $structureOk

# 检查 5: 配置文件
Write-Host "5. 检查配置文件..." -ForegroundColor Yellow
$configFiles = @(
    "market-gateway\src\main\resources\bootstrap.yml",
    "market-auth\src\main\resources\bootstrap.yml",
    "market-service\market-service-core\src\main\resources\bootstrap.yml"
)

$configOk = $true
foreach ($file in $configFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        if ($content -match "8849") {
            Write-Host "   ✅ $file (Nacos端口8849)" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️  $file (未检测到8849端口)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "   ❌ $file 不存在" -ForegroundColor Red
        $configOk = $false
    }
}
$allChecks += $configOk

# 检查 6: 编译状态
Write-Host "6. 检查编译状态..." -ForegroundColor Yellow
$targetDirs = @(
    "market-gateway\target",
    "market-auth\target",
    "market-service\market-service-core\target"
)

$compiled = 0
foreach ($dir in $targetDirs) {
    if (Test-Path "$dir\classes") {
        $compiled++
    }
}

if ($compiled -eq $targetDirs.Count) {
    Write-Host "   ✅ 所有核心服务已编译 ($compiled/$($targetDirs.Count))" -ForegroundColor Green
    $allChecks += $true
} elseif ($compiled -gt 0) {
    Write-Host "   ⚠️  部分服务已编译 ($compiled/$($targetDirs.Count))" -ForegroundColor Yellow
    $allChecks += $null
} else {
    Write-Host "   ❌ 尚未编译，请运行: mvn clean compile" -ForegroundColor Red
    $allChecks += $false
}

# 检查 7: 端口占用
Write-Host "7. 检查端口占用..." -ForegroundColor Yellow
$ports = @(3306, 6379, 8849, 8080, 8081, 9001)
$portsInUse = @()

foreach ($port in $ports) {
    $connection = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
    if ($connection.TcpTestSucceeded) {
        $portsInUse += $port
    }
}

if ($portsInUse.Count -gt 0) {
    Write-Host "   ⚠️  以下端口已被占用: $($portsInUse -join ', ')" -ForegroundColor Yellow
    Write-Host "      如果是基础设施端口(3306,6379,8849)被占用，这是正常的" -ForegroundColor Gray
} else {
    Write-Host "   ✅ 关键端口未被占用" -ForegroundColor Green
}

# 总结
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "检查总结" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$passed = ($allChecks | Where-Object { $_ -eq $true }).Count
$failed = ($allChecks | Where-Object { $_ -eq $false }).Count
$warnings = ($allChecks | Where-Object { $_ -eq $null }).Count

Write-Host "✅ 通过: $passed" -ForegroundColor Green
Write-Host "❌ 失败: $failed" -ForegroundColor Red
Write-Host "⚠️  警告: $warnings" -ForegroundColor Yellow
Write-Host ""

if ($failed -eq 0) {
    Write-Host "SUCCESS: Project is healthy! Ready to start services." -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Yellow
    Write-Host "  1. Start infrastructure: .\start-infrastructure.ps1" -ForegroundColor White
    Write-Host "  2. Initialize database: .\init-nacos-db.ps1" -ForegroundColor White
    Write-Host "  3. Start services: .\start-light.ps1" -ForegroundColor White
    Write-Host ""
    Write-Host "Documentation:" -ForegroundColor Yellow
    Write-Host "  - LIGHT_MODE_GUIDE.md" -ForegroundColor White
    Write-Host "  - OPTIMIZATION_REPORT.md" -ForegroundColor White
} else {
    Write-Host "WARNING: Issues found. Please fix according to the hints above." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan

