# 用户服务快速启动指南

## 🚀 快速开始

### 前置条件
- ✅ JDK 8+
- ✅ MySQL 8.0+
- ✅ Redis 5.0+
- ✅ Maven 3.6+
- ✅ Nacos 2.x

### 一键启动步骤

#### 1️⃣ 初始化数据库 (首次运行)
```powershell
# 在项目根目录执行
mysql -u root -p < doc\SQL\init_schema.sql
```

#### 2️⃣ 启动依赖服务
```powershell
# 启动Nacos (如果未启动)
.\start-infrastructure.ps1

# 或者手动启动Nacos
cd nacos\bin
startup.cmd -m standalone

# 启动Redis (如果未启动)
redis-server
```

#### 3️⃣ 启动用户服务
```powershell
# 方式1: 使用启动脚本 (推荐)
.\start-user.ps1

# 方式2: 使用Maven
cd market-service\market-service-user
mvn spring-boot:run
```

#### 4️⃣ 验证服务
打开浏览器访问:
- **API文档**: http://localhost:8101/doc.html
- **健康检查**: http://localhost:8101/actuator/health (如已配置)

---

## 🧪 测试接口

### 1. 注册用户
```bash
curl -X POST http://localhost:8101/api/user/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"testuser\",\"password\":\"123456\",\"confirmPassword\":\"123456\",\"nickname\":\"测试用户\"}"
```

### 2. 登录获取Token
```bash
curl -X POST http://localhost:8101/api/user/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"testuser\",\"password\":\"123456\"}"
```

### 3. 获取当前用户信息 (需要token)
```bash
curl -X GET http://localhost:8101/api/user/current ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## ⚙️ 配置说明

### 数据库配置
文件: `market-service/market-service-user/src/main/resources/bootstrap.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/market_user
    username: root
    password: root
```

### Redis配置
```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
```

### Nacos配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8849
```

---

## 🐛 常见问题

### Q1: 编译失败 - 找不到依赖
**解决方案**:
```powershell
# 先编译依赖模块
cd D:\program\Market
mvn clean install -pl market-common,market-api/market-api-user -am '-Dmaven.test.skip=true'
```

### Q2: 启动失败 - 无法连接数据库
**检查清单**:
1. MySQL是否启动？
2. 数据库market_user是否存在？
3. 用户名密码是否正确？
4. 防火墙是否阻止连接？

### Q3: 登录后访问接口返回401
**解决方案**:
1. 确认请求头携带token: `Authorization: Bearer {token}`
2. 检查token是否过期
3. 确认Redis连接正常

### Q4: Nacos连接失败
**解决方案**:
```powershell
# 检查Nacos是否启动
curl http://127.0.0.1:8849/nacos

# 重启Nacos
cd nacos\bin
shutdown.cmd
startup.cmd -m standalone
```

---

## 📊 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 用户服务 | 8101 | market-service-user |
| Nacos | 8849 | 服务注册中心 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |

---

## 📝 API接口列表

### 无需认证
- `POST /api/user/auth/register` - 用户注册
- `POST /api/user/auth/login` - 用户登录

### 需要认证 (需要token)
- `POST /api/user/auth/logout` - 用户登出
- `GET /api/user/current` - 获取当前用户信息
- `PUT /api/user/update` - 更新用户信息
- `GET /api/user/check` - 检查登录状态

---

## 📚 更多文档

- **详细功能文档**: [USER_SERVICE_README.md](./USER_SERVICE_README.md)
- **开发完成报告**: [USER_DEVELOPMENT_COMPLETE.md](./USER_DEVELOPMENT_COMPLETE.md)
- **数据库设计**: [doc/SQL/数据库设计.md](./doc/SQL/数据库设计.md)

---

## 🎯 下一步

1. ✅ 用户服务已完成
2. ⏭️ 开发商品服务 (market-service-product)
3. ⏭️ 开发交易服务 (market-service-trade)
4. ⏭️ 开发网关服务 (market-gateway)
5. ⏭️ 前后端联调测试

---

**提示**: 首次启动可能需要下载依赖，请耐心等待。如遇问题请查看控制台输出日志。

