# Market 项目 - 快速参考卡片

## 🚀 3步启动

```powershell
# 1. 启动基础设施 (MySQL, Redis, Nacos)
.\start-infrastructure.ps1

# 2. 初始化数据库
.\init-nacos-db.ps1

# 3. 启动核心服务 (3个终端窗口)
# 终端1: java -jar market-gateway\target\market-gateway-1.0-SNAPSHOT.jar
# 终端2: java -jar market-auth\target\market-auth-1.0-SNAPSHOT.jar
# 终端3: java -jar market-service\market-service-core\target\market-service-core-1.0-SNAPSHOT.jar
```

---

## 📊 服务端口速查

| 服务 | 端口 | 访问地址 |
|-----|------|---------|
| **Gateway** | 8080 | http://localhost:8080 |
| **Auth** | 8081 | http://localhost:8081 |
| **Core** | 9001 | http://localhost:9001 |
| **Nacos** | **8849** | http://localhost:8849/nacos |
| **MySQL** | 3306 | localhost:3306 |
| **Redis** | 6379 | localhost:6379 |

⚠️ **注意**: Nacos端口已从8848改为**8849**

---

## 🔧 常用命令

### 编译项目
```powershell
# 编译所有核心模块
mvn clean compile -DskipTests

# 打包
mvn clean package -DskipTests
```

### 健康检查
```powershell
# 运行健康检查脚本
.\check-health.ps1

# 检查Nacos连接
curl http://localhost:8849/nacos
```

### Docker管理
```powershell
# 启动所有基础设施
docker-compose up -d

# 查看运行状态
docker-compose ps

# 停止所有服务
docker-compose down

# 查看日志
docker-compose logs -f nacos
```

---

## 📁 项目结构

```
Market/
├── market-gateway/              # API网关 (8080)
├── market-auth/                 # 认证服务 (8081)
├── market-service/
│   └── market-service-core/    # 核心业务 (9001) ⭐ 新增
│       ├── User               # 用户管理
│       ├── Product            # 商品管理
│       └── Trade              # 交易管理
├── market-common/              # 公共模块
└── market-api/                 # API接口定义
```

---

## 📚 文档导航

| 文档 | 用途 |
|-----|------|
| **LIGHT_MODE_GUIDE.md** | 轻量级启动完整指南 ⭐ |
| **OPTIMIZATION_REPORT.md** | 优化详细报告 |
| **COMPLETION_SUMMARY.md** | 优化完成总结 |
| **STARTUP_GUIDE.md** | 标准启动指南 |
| **README.md** | 项目介绍 |

---

## ⚡ 优化效果

- ✅ 服务数量: 10+ → **3个** (减少70%)
- ✅ 内存占用: 4-6GB → **1.5-2GB** (减少60%)
- ✅ 启动时间: 5-8分钟 → **2-3分钟** (减少60%)

---

## 🐛 常见问题

### Q1: 编译失败？
```powershell
mvn clean install -DskipTests -U
```

### Q2: Nacos连接不上？
- 检查端口是否为 **8849** (不是8848)
- 检查Docker容器是否运行: `docker ps`

### Q3: 服务注册失败？
- 确认namespace为 `dev`
- 检查bootstrap.yml中的server-addr

### Q4: 内存不足？
```powershell
# 减少JVM内存
java -Xmx256m -Xms128m -jar xxx.jar
```

---

## 🎯 核心配置

### Nacos配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8849  # ⚠️ 注意端口
        namespace: dev
```

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/market
    username: root
    password: 123456789
```

---

## 🔐 默认账号

| 系统 | 用户名 | 密码 |
|-----|--------|------|
| Nacos | nacos | nacos |
| MySQL | root | 123456789 |
| Redis | - | redis123456 |

---

## 💻 开发建议

### 推荐IDE设置
- **IDEA**: 增加堆内存 `-Xmx2048m`
- **Maven**: 使用本地仓库加速
- **Hot Reload**: 使用Spring DevTools

### 性能优化
```yaml
spring:
  main:
    lazy-initialization: true  # 懒加载
```

```powershell
# JVM优化参数
java -XX:+UseG1GC -Xmx512m -Xms256m -jar xxx.jar
```

---

## 📞 快速帮助

1. **检查项目健康**: `.\check-health.ps1`
2. **查看启动指南**: 打开 `LIGHT_MODE_GUIDE.md`
3. **查看优化报告**: 打开 `OPTIMIZATION_REPORT.md`

---

**打印此页面作为快速参考！** 📄

---
*最后更新: 2026-02-13*

