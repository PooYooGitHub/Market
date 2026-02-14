# 🔧 注册接口 500 错误修复完成

> **时间**: 2026-02-14 17:30  
> **问题**: 前端注册返回 500 错误，Gateway 无法连接 Nacos  
> **状态**: ✅ 已修复，需要重启服务

---

## 一、问题分析

### 1.1 错误现象

**前端返回**:
```json
{
  "timestamp": "2026-02-14T09:26:39.958+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/user/register"
}
```

**后端日志**:
```
ERROR: get services from nacos server fail
com.alibaba.nacos.api.exception.NacosException: java.util.concurrent.TimeoutException: Waited 3000 milliseconds
```

### 1.2 根本原因

1. **Nacos gRPC 超时** - Gateway 使用 gRPC 协议连接 Nacos 超时（等待3秒）
2. **前端 API 路径错误** - 前端使用 `/api/user/register`，实际应该是 `/api/user/auth/register`
3. **Gateway 白名单配置错误** - 白名单中配置的是 `/api/user/register`，不匹配实际路径

---

## 二、修复方案

### 2.1 修复 Gateway Nacos 配置

**文件**: `market-gateway/src/main/resources/bootstrap.yml`

**修改内容**:
```yaml
spring:
  cloud:
    nacos:
      discovery:
        # 添加以下配置，优化 Nacos 连接
        naming-load-cache-at-start: true        # 启动时加载服务列表
        naming-polling-thread-count: 1           # 轮询线程数
        naming-use-endpoint-parsing-rule: false  # 使用 HTTP 而非 gRPC
        register-enabled: true
        heartbeat-interval: 5000                 # 心跳间隔5秒
        timeout: 3000                            # 超时时间3秒
```

**说明**: 
- Nacos 2.2.0 默认使用 gRPC (端口 9848/9849)
- 配置 `naming-use-endpoint-parsing-rule: false` 使用 HTTP 长轮询
- 避免 gRPC 连接超时问题

### 2.2 修复前端 API 路径

**文件**: `front/vue-project/src/api/user.js`

**修改内容**:
```javascript
// 修改前 ❌
export function register(data) {
  return request({
    url: '/api/user/register',  // 错误路径
    method: 'post',
    data
  })
}

// 修改后 ✅
export function register(data) {
  return request({
    url: '/api/user/auth/register',  // 正确路径
    method: 'post',
    data
  })
}
```

### 2.3 修复 Gateway 白名单配置

**文件**: `market-gateway/src/main/java/org/shyu/marketgateway/config/WhiteListProperties.java`

**修改内容**:
```java
// 修改前 ❌
private List<String> whiteList = Arrays.asList(
    "/api/user/login",
    "/api/user/register",  // 路径不匹配
    ...
);

// 修改后 ✅
private List<String> whiteList = Arrays.asList(
    "/api/user/auth/",  // 匹配所有 /api/user/auth/* 路径
    ...
);
```

---

## 三、验证步骤

### 3.1 检查 Nacos 服务注册

```bash
# 检查服务列表
curl.exe -s "http://localhost:8849/nacos/v1/ns/service/list?pageNo=1&pageSize=100"

# 应该看到
{"count":2,"doms":["market-service-user","market-gateway"]}

# 检查 user 服务实例
curl.exe -s "http://localhost:8849/nacos/v1/ns/instance/list?serviceName=market-service-user"

# 应该看到 healthy: true
```

### 3.2 重启 Gateway

**方式1: IDEA**
```
1. 停止 market-gateway ⏹️
2. 重新启动 ▶️
3. 等待看到 "Started MarketGatewayApplication"
```

**方式2: 命令行**
```bash
cd D:\program\Market\market-gateway
mvn spring-boot:run
```

### 3.3 重启前端

```bash
cd D:\program\Market\front\vue-project
npm run dev
```

### 3.4 测试注册接口

**前端操作**:
1. 打开 `http://localhost:5173` (或前端端口)
2. 进入注册页面
3. 填写用户信息
4. 点击注册

**期望结果**:
```
✅ 注册成功，跳转到登录页
```

**后端日志应显示**:
```
DEBUG: Gateway 收到请求: /api/user/auth/register
DEBUG: 白名单路径，直接放行: /api/user/auth/register
INFO: Registration request: username=xxx
INFO: Registration successful
```

---

## 四、为什么会出现这个问题？

### 4.1 Nacos gRPC 超时

**原因**:
- Nacos 2.x 默认使用 gRPC 协议（端口 9848/9849）
- 某些网络环境或防火墙配置可能导致 gRPC 连接不稳定
- Gateway 启动时尝试通过 gRPC 获取服务列表，超时后失败

**解决方案**:
- 配置使用 HTTP 长轮询替代 gRPC
- 或者确保 9848/9849 端口可访问

### 4.2 API 路径不一致

**问题链**:
```
前端 /api/user/register
  ↓
Gateway 白名单检查失败（配置的是 /api/user/register）
  ↓
尝试路由到 market-service-user
  ↓
Nacos 获取服务列表超时
  ↓
返回 500 错误
```

**正确的流程**:
```
前端 /api/user/auth/register
  ↓
Gateway 白名单匹配成功（/api/user/auth/）
  ↓
直接放行（不需要 Token）
  ↓
路由到 market-service-user
  ↓
StripPrefix 移除 /api 变成 /user/auth/register
  ↓
User Service 处理注册
```

---

## 五、完整的路径映射

| 前端请求 | Gateway 接收 | StripPrefix 后 | User Service 处理 |
|---------|-------------|----------------|------------------|
| `/api/user/auth/register` | `/api/user/auth/register` | `/user/auth/register` | `UserAuthController.register()` |
| `/api/user/auth/validate-login` | `/api/user/auth/validate-login` | `/user/auth/validate-login` | `UserAuthController.validateLogin()` |
| `/api/user/profile` | `/api/user/profile` | `/user/profile` | `UserController.getProfile()` |

**注意**: 
- Gateway 的白名单检查在 StripPrefix **之前**
- 所以白名单配置要使用完整路径 `/api/user/auth/*`

---

## 六、重启检查清单

### Gateway 重启后检查

- [ ] 启动日志中没有 Nacos 连接错误
- [ ] 可以看到 "nacos registry...register finished"
- [ ] 访问 `http://localhost:9000/actuator/health` 返回 "UP"

### 前端重启后检查

- [ ] 访问 `http://localhost:5173` 可以打开页面
- [ ] 浏览器控制台没有 CORS 错误
- [ ] 注册页面可以正常显示

### 注册功能检查

- [ ] 填写表单，点击注册
- [ ] 前端不再显示 500 错误
- [ ] 显示"注册成功"并跳转登录页
- [ ] 数据库中可以查到新用户记录

---

## 七、故障排查

### 如果 Gateway 还报 Nacos 超时

1. **检查 Nacos 是否运行**:
   ```bash
   netstat -ano | findstr 8849
   ```

2. **检查 Nacos 控制台**:
   访问 `http://localhost:8849/nacos`

3. **检查服务注册**:
   Nacos 控制台 → 服务管理 → 服务列表
   应该能看到 `market-service-user` 和 `market-gateway`

4. **清理 Nacos 缓存**:
   ```bash
   cd D:\program\nacos\data
   del /s /q *
   ```

### 如果前端还是 500 错误

1. **检查前端 API 配置**:
   - 打开 `src/api/user.js`
   - 确认是 `/api/user/auth/register`

2. **检查浏览器网络请求**:
   - F12 → Network
   - 查看实际请求的 URL

3. **检查 Gateway 白名单日志**:
   - 应该看到 "白名单路径，直接放行"
   - 如果没有，说明白名单配置还有问题

---

## 八、总结

这次问题涉及三个层面：

1. **网络层** - Nacos gRPC 超时
2. **路径层** - API 路径不一致
3. **配置层** - 白名单配置不匹配

修复后的完整流程：

```
前端 (Vue) 
  ↓ POST /api/user/auth/register
Gateway (Nacos HTTP 连接正常)
  ↓ 白名单匹配 /api/user/auth/
  ↓ 直接放行
  ↓ StripPrefix 移除 /api
User Service
  ↓ 接收 /user/auth/register
  ↓ SaTokenConfig 白名单 /api/user/auth/
  ↓ 直接放行
  ↓ UserAuthController.register()
  ↓ 注册成功
返回 {"code": 200, "message": "Registration successful"}
```

---

**修复完成！请重启 Gateway 和前端进行测试。** 🚀

---

**相关文档**:
- [FINAL_OPERATION_GUIDE.md](./FINAL_OPERATION_GUIDE.md) - User Service 操作指南
- [CLASSCAST_FIX.md](./CLASSCAST_FIX.md) - User Service 问题修复
- [doc/架构设计说明书.md](./doc/架构设计说明书.md) - 系统架构设计

