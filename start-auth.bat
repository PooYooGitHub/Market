@echo off
chcp 65001 >nul
echo ========================================
echo   启动 market-auth 认证服务
echo ========================================
echo.

echo 检查 Nacos 运行状态...
curl -s http://127.0.0.1:8849/nacos/v1/console/server/state >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Nacos 未运行或无法访问！
    echo 请先启动 Nacos 服务器
    pause
    exit /b 1
)
echo [OK] Nacos 运行正常
echo.

echo 启动 market-auth 服务...
echo 端口: 8081
echo.

cd /d %~dp0market-auth

set JAVA_HOME=D:\program\JDK
set JAR_FILE=target\market-auth-1.0-SNAPSHOT.jar

if not exist "%JAR_FILE%" (
    echo [ERROR] 找不到JAR文件: %JAR_FILE%
    echo 请先运行 mvn clean install
    pause
    exit /b 1
)

rem JVM参数：禁用gRPC，使用HTTP模式连接Nacos
set JVM_OPTS=-Xms256m -Xmx512m -Dnacos.remote.client.grpc.enable=false -Dspring.profiles.active=dev -Dfile.encoding=UTF-8

"%JAVA_HOME%\bin\java.exe" %JVM_OPTS% -jar %JAR_FILE%

echo.
echo ========================================
echo   market-auth 服务已停止
echo ========================================
pause

