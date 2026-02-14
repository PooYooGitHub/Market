# Market 项目 - 轻量级启动指南

## 🎯 优化说明

为了在内存有限的环境下运行，项目已进行架构优化：

### 原架构
```
需要启动 10+ 个微服务：
- Gateway (网关)
- Auth (认证)
- User Service (用户)
- Product Service (商品)
- Trade Service (交易)
- Message Service (消息)
- Credit Service (信用)
- Arbitration Service (仲裁)
- File Service (文件)
- Admin Service (管理)
```

### 优化后架构
```
只需启动 3 个核心服务：
✅ Gateway (网关) - 8080
✅ Auth (认证) - 8081
✅ Service-Core (核心业务：整合user+product+trade) - 9001
```

**内存占用减少约 60%！**

---

## 🚀 快速启动

### 1. 启动基础设施

```powershell
# 启动 MySQL、Redis、Nacos
.\start-infrastructure.ps1

# 或使用 Docker Compose
docker-compose up -d
```

**重要**: Nacos端口已修改为 **8849**

### 2. 初始化数据库

```powershell
# 初始化 Nacos 配置数据库
.\init-nacos-db.ps1

# 初始化业务数据库（在MySQL中执行）
doc/SQL/init_schema.sql
doc/SQL/mysql-schema.sql
doc/SQL/sample_data.sql
```

### 3. 编译项目

```powershell
# 使用轻量级启动脚本
.\start-light.ps1

# 或手动编译
mvn clean package -DskipTests
```

### 4. 启动服务

#### 方式一：使用脚本（推荐）
```powershell
.\start-light.ps1
# 按照提示在不同终端启动各服务
```

#### 方式二：手动启动

**终端 1 - 启动网关**
```powershell
cd market-gateway\target
java -jar market-gateway-1.0-SNAPSHOT.jar
```

**终端 2 - 启动认证服务**
```powershell
cd market-auth\target
java -jar market-auth-1.0-SNAPSHOT.jar
```

**终端 3 - 启动核心业务服务**
```powershell
cd market-service\market-service-core\target
java -jar market-service-core-1.0-SNAPSHOT.jar
```

---

## 📝 服务信息

| 服务 | 端口 | 说明 | 必须启动 |
|-----|------|-----|---------|
| MySQL | 3306 | 数据库 | ✅ |
| Redis | 6379 | 缓存 | ✅ |
| Nacos | 8849 | 注册中心 | ✅ |
| Gateway | 8080 | API网关 | ✅ |
| Auth | 8081 | 认证服务 | ✅ |
| Service-Core | 9001 | 核心业务 | ✅ |

**访问地址：**
- API网关: http://localhost:8080
- Nacos控制台: http://localhost:8849/nacos (nacos/nacos)

---

## 🔧 配置说明

### Nacos 端口修改
所有配置文件中的Nacos端口已从 **8848** 修改为 **8849**：
- `docker-compose.yml` - 端口映射
- `bootstrap.yml` - 所有服务配置

### 核心服务说明
`market-service-core` 整合了以下功能：
- **用户管理**: 注册、登录、个人信息
- **商品管理**: 发布、浏览、搜索
- **交易管理**: 下单、支付、订单

通过组件扫描和依赖注入，复用原有代码，无需重复开发。

---

## 💡 进一步优化建议

如果内存仍然不足，可以：

1. **减少JVM内存**
```powershell
java -Xmx256m -Xms128m -jar xxx.jar
```

2. **使用H2数据库代替MySQL**（开发环境）

3. **禁用不必要的功能**
   - Sentinel 流量控制
   - Elasticsearch 搜索（使用数据库LIKE查询）
   - RocketMQ 消息队列（使用Spring Event）

---

## 🐛 常见问题

### 1. 端口冲突
- Nacos已改为8849，避免与其他应用冲突
- 如需修改，同步更新 `docker-compose.yml` 和所有 `bootstrap.yml`

### 2. 服务注册失败
- 检查Nacos是否启动: `http://localhost:8849/nacos`
- 检查namespace是否为 `dev`
- 查看服务日志

### 问题3: 数据库连接失败
- 检查MySQL是否启动
- 确认数据库名为 `market`
- 密码默认 `123456789`

### 4. 内存不足
- 降低JVM堆内存: `-Xmx256m`
- 只启动核心服务
- 关闭不必要的IDE和浏览器标签

---

## 📚 架构详情

### 核心服务启动类
```java
@SpringBootApplication(scanBasePackages = {
    "org.shyu.marketservicecore",
    "org.shyu.marketserviceuser",
    "org.shyu.marketserviceproduct",
    "org.shyu.marketservicetrade"
})
@MapperScan({
    "org.shyu.marketserviceuser.mapper",
    "org.shyu.marketserviceproduct.mapper",
    "org.shyu.marketservicetrade.mapper"
})
```

通过扫描多个包，整合三个服务的所有Controller、Service、Mapper。

### 网关路由配置
```yaml
# user/product/trade 都路由到 market-service-core
- id: market-service-user
  uri: lb://market-service-core
  predicates:
    - Path=/api/user/**
```

---

## 📞 技术支持

如有问题，请查看：
- `STARTUP_GUIDE.md` - 完整启动指南
- `README.md` - 项目详细说明
- `COMPLETION_REPORT.md` - 项目完成报告

祝开发顺利！🎉

