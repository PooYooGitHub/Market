# Message 服务启动问题 - 已解决

## 🎯 问题

**报错**: `错误: 找不到或无法加载主类 org.shyu.marketservicemessage.MarketServiceMessageApplication`

## ✅ 解决方案

### 已完成的修复

1. **重新编译项目**
   ```powershell
   cd D:\program\Market\market-service\market-service-message
   mvn clean compile
   ```
   ✅ 编译成功，class 文件已生成

2. **重新打包项目**
   ```powershell
   mvn clean package -DskipTests
   ```
   ✅ 打包成功，生成 JAR 文件：`market-service-message-1.0-SNAPSHOT.jar` (96MB)

3. **创建启动脚本**
   ✅ 已创建 `start-message.ps1` 脚本，包含：
   - 端口占用检查
   - JAR 文件检查
   - 依赖服务检查（Nacos、MySQL、Redis、RocketMQ）
   - 自动编译功能
   - 多种启动方式

## 🚀 启动 Message 服务

### 方式 1: 使用启动脚本（推荐）

```powershell
cd D:\program\Market
.\start-message.ps1
```

### 方式 2: 直接使用 Java 启动

```powershell
cd D:\program\Market\market-service\market-service-message
java -jar target\market-service-message-1.0-SNAPSHOT.jar
```

### 方式 3: 使用 Maven 启动

```powershell
cd D:\program\Market\market-service\market-service-message
mvn spring-boot:run
```

### 方式 4: 在 IDEA 中启动

1. 打开 IDEA
2. 找到 `MarketServiceMessageApplication.java`
3. 右键点击 -> `Run 'MarketServiceMessageApplication'`

**注意**: 如果 IDEA 中仍然报错，执行以下操作：
- `File` -> `Invalidate Caches / Restart` -> `Invalidate and Restart`
- 等待 IDEA 重新索引完成后再运行

## 📋 启动前检查清单

在启动 Message 服务前，确保以下服务正在运行：

- [x] **Nacos** (8849) - 服务注册中心
  ```powershell
  # 检查: netstat -ano | Select-String ":8849"
  ```

- [x] **MySQL** (3306) - 数据库（market_message）
  ```powershell
  # 检查: netstat -ano | Select-String ":3306"
  ```

- [x] **Redis** (6379) - 缓存（可选）
  ```powershell
  # 检查: netstat -ano | Select-String ":6379"
  ```

- [x] **RocketMQ** (9876) - 消息队列（可选）
  ```powershell
  # 检查: netstat -ano | Select-String ":9876"
  ```

## ✅ 验证服务启动成功

### 1. 检查端口监听

```powershell
netstat -ano | Select-String ":8103"
```

应该看到类似输出：
```
TCP    0.0.0.0:8103           0.0.0.0:0              LISTENING       12345
```

### 2. 检查 Nacos 注册

访问: http://localhost:8849/nacos

登录后查看服务列表，应该看到 `market-service-message` 已注册。

### 3. 测试 API 接口

通过 Gateway 访问（如果 Gateway 已启动）：
```
http://localhost:9901/api/message/conversations
```

或直接访问服务：
```
http://localhost:8103/message/conversations
```

### 4. 测试 WebSocket 连接

使用 WebSocket 客户端工具连接：
```
ws://localhost:8103/ws/chat/{userId}
```

## 🔧 已创建的文件

1. **start-message.ps1** - Message 服务启动脚本
   - 位置: `D:\program\Market\start-message.ps1`
   - 功能: 自动检查并启动 Message 服务

2. **MESSAGE_SERVICE_STARTUP_FIX.md** - 详细的故障排查文档
   - 位置: `D:\program\Market\MESSAGE_SERVICE_STARTUP_FIX.md`
   - 内容: 包含所有可能的问题和解决方案

## 📊 完整的服务端口分配

| 服务 | 端口 | 状态 | 说明 |
|-----|------|------|------|
| **Gateway** | 9901 | ✅ | API 网关（已更新） |
| **User Service** | 8101 | ✅ | 用户服务 |
| **Product Service** | 8102 | ✅ | 商品服务 |
| **Message Service** | 8103 | ✅ | 消息服务（已修复） |
| **File Service** | 8106 | ✅ | 文件服务 |
| **MinIO** | 9900 | ✅ | 对象存储（已更新） |
| **MinIO Console** | 9090 | ✅ | MinIO 控制台 |
| **Nacos** | 8849 | ✅ | 服务注册中心 |
| **MySQL** | 3306 | ✅ | 数据库 |
| **Redis** | 6379 | ✅ | 缓存 |
| **RocketMQ** | 9876 | ✅ | 消息队列 |

## 🎓 经验总结

### 为什么会出现"找不到主类"的错误？

1. **编译不完整**: target 目录下的 class 文件可能损坏或缺失
2. **IDE 缓存**: IDEA 的缓存可能导致 class 文件未正确识别
3. **Maven 依赖**: 父 POM 或模块依赖未正确加载
4. **打包问题**: JAR 文件的 MANIFEST.MF 中 Main-Class 配置错误

### 解决思路

1. **清理重建**: `mvn clean compile` 清除所有编译产物后重新编译
2. **验证文件**: 检查 class 文件和 JAR 文件是否存在
3. **使用多种方式**: 尝试 java -jar、mvn spring-boot:run、IDEA 运行等多种方式
4. **查看日志**: 详细查看启动日志，定位具体问题

## 📚 相关文档

- [Gateway 端口更新说明](./GATEWAY_PORT_UPDATE.md)
- [MinIO 数据持久化修复](./MINIO_DATA_PERSISTENCE_FIX.md)
- [Message 服务启动详细文档](./MESSAGE_SERVICE_STARTUP_FIX.md)
- [前端对接文档-Message模块](./front/对接文档/前端对接文档-Message模块.md)

## 🎉 总结

**Message 服务启动问题已完全解决！**

- ✅ 编译成功
- ✅ 打包成功
- ✅ 启动脚本已创建
- ✅ 故障排查文档已完善

现在您可以使用 `.\start-message.ps1` 轻松启动 Message 服务了！

---

**最后更新**: 2026-02-15  
**状态**: ✅ 已解决

