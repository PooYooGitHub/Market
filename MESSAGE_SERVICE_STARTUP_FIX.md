# Message 服务启动问题解决方案

## 📋 问题描述

**错误信息**: `错误: 找不到或无法加载主类 org.shyu.marketservicemessage.MarketServiceMessageApplication`

## ✅ 已完成的检查

### 1. 主类文件检查
- ✅ 主类源文件存在: `MarketServiceMessageApplication.java`
- ✅ 包名正确: `org.shyu.marketservicemessage`
- ✅ 类名正确: `MarketServiceMessageApplication`

### 2. 编译检查
- ✅ Maven 编译成功
- ✅ class 文件已生成: `target\classes\org\shyu\marketservicemessage\MarketServiceMessageApplication.class`
- ✅ JAR 文件已生成: `target\market-service-message-1.0-SNAPSHOT.jar` (96MB)

### 3. 依赖检查
- ✅ 父 POM 配置正确
- ✅ Spring Boot Maven Plugin 已配置

## 🔍 可能的原因

### 原因 1: IDE 缓存问题
如果您是在 IDEA 中运行的，可能是 IDE 缓存导致的。

**解决方案**:
1. 在 IDEA 中点击 `File` -> `Invalidate Caches / Restart`
2. 选择 `Invalidate and Restart`
3. 等待 IDEA 重新索引完成

### 原因 2: 运行配置问题
IDEA 的运行配置可能有问题。

**解决方案**:
1. 删除现有的运行配置
2. 重新创建运行配置：
   - 右键点击 `MarketServiceMessageApplication.java`
   - 选择 `Run 'MarketServiceMessageApplication'`

### 原因 3: 模块依赖未正确加载
Maven 模块依赖可能未正确加载。

**解决方案**:
1. 在 IDEA 右侧打开 Maven 工具窗口
2. 点击 `Reload All Maven Projects` (刷新图标)
3. 等待依赖下载完成
4. 重新编译项目

## 🚀 推荐的启动方式

### 方式 1: 使用 PowerShell 脚本启动（推荐）

我已经为您创建了启动脚本：`start-message.ps1`

```powershell
# 在项目根目录执行
.\start-message.ps1
```

这个脚本会：
1. 检查 JAR 文件是否存在
2. 如果不存在，自动编译
3. 使用 `java -jar` 启动服务

### 方式 2: 使用 Maven 命令启动

```powershell
cd D:\program\Market\market-service\market-service-message
mvn clean spring-boot:run
```

### 方式 3: 使用 java -jar 启动

```powershell
cd D:\program\Market\market-service\market-service-message
mvn clean package -DskipTests
java -jar target\market-service-message-1.0-SNAPSHOT.jar
```

### 方式 4: 在 IDEA 中配置启动

1. **创建新的运行配置**:
   - 点击 `Run` -> `Edit Configurations`
   - 点击 `+` -> `Spring Boot`
   - 配置如下：
     - Name: `MessageService`
     - Main class: `org.shyu.marketservicemessage.MarketServiceMessageApplication`
     - Working directory: `D:\program\Market\market-service\market-service-message`
     - Use classpath of module: `market-service-message`

2. **点击 Run 启动**

## 📝 完整启动步骤

### 步骤 1: 确保基础设施运行

```powershell
# 1. Nacos (8849)
.\start-nacos.ps1

# 2. MySQL (3306)
# 确保 MySQL 服务正在运行

# 3. Redis (6379)
# 确保 Redis 服务正在运行

# 4. RocketMQ (9876)
# 确保 RocketMQ 正在运行
docker ps | Select-String "rocketmq"
```

### 步骤 2: 编译并打包

```powershell
cd D:\program\Market\market-service\market-service-message

# 清理并重新编译
mvn clean compile

# 打包（生成可执行 JAR）
mvn package -DskipTests
```

### 步骤 3: 启动服务

```powershell
# 方式 A: 使用脚本
cd D:\program\Market
.\start-message.ps1

# 方式 B: 直接启动
cd D:\program\Market\market-service\market-service-message
java -jar target\market-service-message-1.0-SNAPSHOT.jar

# 方式 C: 使用 Maven
mvn spring-boot:run
```

### 步骤 4: 验证启动

```powershell
# 检查端口是否监听
netstat -ano | Select-String ":8103"

# 检查 Nacos 注册
# 访问: http://localhost:8849/nacos
# 查看 market-service-message 是否注册成功
```

## 🔧 配置文件检查

### application.yml

确保配置正确：

```yaml
server:
  port: 8103

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/market_message
    username: root
    password: 123456789
  
  redis:
    host: localhost
    port: 6379
    database: 3

rocketmq:
  name-server: localhost:9876
  producer:
    group: market-message-producer
```

### bootstrap.yml

如果有 bootstrap.yml，确保 Nacos 配置正确。

## 🐛 常见错误及解决方案

### 错误 1: 端口被占用

**错误信息**: `Port 8103 was already in use`

**解决方案**:
```powershell
# 查找占用进程
netstat -ano | Select-String ":8103"

# 终止进程
taskkill /PID <进程ID> /F
```

### 错误 2: 数据库连接失败

**错误信息**: `Cannot connect to database`

**解决方案**:
1. 确保 MySQL 正在运行
2. 检查数据库是否存在：
```sql
CREATE DATABASE IF NOT EXISTS market_message CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
3. 检查数据库配置（用户名、密码）

### 错误 3: Nacos 连接失败

**错误信息**: `Unable to connect to Nacos`

**解决方案**:
1. 确保 Nacos 正在运行（端口 8849）
2. 检查 bootstrap.yml 中的 Nacos 地址配置

### 错误 4: RocketMQ 连接失败

**错误信息**: `Connect to 127.0.0.1:9876 failed`

**解决方案**:
```powershell
# 检查 RocketMQ 是否运行
docker ps | Select-String "rocketmq"

# 如果未运行，启动 RocketMQ
docker start rocketmq-namesrv
docker start rocketmq-broker
```

## 📊 服务端口分配

| 服务 | 端口 | 状态 |
|-----|------|------|
| Gateway | 9901 | ✅ 已更新 |
| User Service | 8101 | ✅ |
| Product Service | 8102 | ✅ |
| Message Service | 8103 | 🔄 待启动 |
| File Service | 8106 | ✅ |
| MinIO | 9900 | ✅ 已更新 |
| MinIO Console | 9090 | ✅ |
| Nacos | 8849 | ✅ |
| MySQL | 3306 | ✅ |
| Redis | 6379 | ✅ |
| RocketMQ | 9876 | ✅ |

## ✅ 验证清单

启动成功后，检查以下内容：

- [ ] Message 服务端口 8103 正在监听
- [ ] 服务已注册到 Nacos（访问 http://localhost:8849/nacos 查看）
- [ ] 可以通过 Gateway 访问 Message API（http://localhost:9901/api/message/）
- [ ] WebSocket 连接正常（ws://localhost:8103/ws/chat/{userId}）
- [ ] 日志中没有错误信息

## 📚 相关文档

- [前端对接文档-Message模块](./front/对接文档/前端对接文档-Message模块.md)
- [Gateway 端口更新说明](./GATEWAY_PORT_UPDATE.md)
- [MinIO 数据持久化修复](./MINIO_DATA_PERSISTENCE_FIX.md)

## 💡 其他建议

### 1. 使用统一启动脚本

创建一个 `start-all-services.ps1` 来统一管理所有服务的启动：

```powershell
# 启动所有服务
Write-Host "启动基础设施..." -ForegroundColor Cyan
.\start-infrastructure.ps1

Write-Host "启动 Gateway..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-File", ".\start-gateway.ps1"

Write-Host "启动 User Service..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-File", ".\start-user.ps1"

Write-Host "启动 Product Service..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-File", ".\start-product.ps1"

Write-Host "启动 Message Service..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-File", ".\start-message.ps1"

Write-Host "启动 File Service..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-File", ".\start-file.ps1"
```

### 2. 配置开发环境

在 IDEA 中配置 Compound 运行配置，一次启动所有服务。

### 3. 使用 Docker Compose

考虑将所有服务容器化，使用 Docker Compose 统一管理。

---

**如果以上方法都无法解决问题，请提供以下信息**：

1. 完整的错误堆栈信息
2. IDEA 的版本
3. JDK 版本（`java -version`）
4. Maven 版本（`mvn -version`）
5. 启动日志的最后 50 行

这样我可以提供更具针对性的解决方案。

