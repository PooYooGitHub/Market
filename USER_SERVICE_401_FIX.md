# 🎯 注册功能 401 错误修复完成

> **时间**: 2026-02-14 17:45  
> **问题**: 从 500 错误变成 401 未授权错误  
> **原因**: User Service 白名单路径配置错误  
> **状态**: ✅ 已修复

---

## 一、问题分析

### 1.1 错误变化

```
之前: 500 Internal Server Error
现在: 401 Unauthorized
```

**这说明**:
- ✅ Gateway 已经正常工作（白名单放行成功）
- ✅ 请求已经到达 User Service
- ❌ User Service 的 Sa-Token 拦截器拒绝了请求

### 1.2 请求流程分析

```
前端发送
  ↓ POST /api/user/register
Gateway 接收
  ↓ 路径: /api/user/register
  ↓ 白名单检查: ✅ 匹配 /api/user/register
  ↓ 放行
  ↓ StripPrefix=1 移除 /api
  ↓ 转发路径: /user/register
User Service 接收
  ↓ 路径: /user/register
  ↓ Sa-Token 白名单检查: ❌ 不匹配 /api/user/auth/
  ↓ 需要登录验证
  ↓ 返回 401 未授权
```

### 1.3 根本原因

**User Service 的白名单配置错误**:
```java
❌ 错误配置
if (path.startsWith("/api/user/auth/")) {
    return true;
}

// 实际收到的路径是 /user/register（Gateway 已经移除了 /api）
// 所以不匹配白名单
```

---

## 二、修复方案

### 2.1 修改 User Service 白名单

**文件**: `market-service-user/src/main/java/org/shyu/marketserviceuser/config/SaTokenConfig.java`

**修改内容**:
```java
// 修改前 ❌
private boolean isWhiteListPath(String path) {
    if (path.startsWith("/api/user/auth/")) {  // 错误：实际路径不包含 /api
        return true;
    }
    // ...
}

// 修改后 ✅
private boolean isWhiteListPath(String path) {
    // Gateway StripPrefix 后的路径
    if (path.startsWith("/user/auth/")) {  // 正确：匹配 Gateway 转发后的路径
        return true;
    }
    
    // 兼容旧版路径
    if (path.equals("/user/register") || path.equals("/user/login")) {
        return true;
    }
    
    // Feign 调用接口
    if (path.matches("/user/\\d+")) {  // 从 /api/user/\d+ 改为 /user/\d+
        return true;
    }
    // ...
}
```

### 2.2 关键改动

| 场景 | 错误配置 | 正确配置 |
|------|---------|---------|
| 认证接口 | `/api/user/auth/` | `/user/auth/` |
| 旧版注册 | - | `/user/register` |
| 旧版登录 | - | `/user/login` |
| Feign调用 | `/api/user/\d+` | `/user/\d+` |

---

## 三、完整的路径映射

### 3.1 请求链路

```
前端 Vue.js
  ↓ POST http://localhost:9000/api/user/register
  ↓
Gateway (端口 9000)
  ↓ 接收: /api/user/register
  ↓ 白名单检查: ✅ 匹配 /api/user/register
  ↓ StripPrefix=1: 移除 /api
  ↓ 转发: http://28.0.0.1:8101/user/register
  ↓
User Service (端口 8101)
  ↓ 接收: /user/register
  ↓ Sa-Token 白名单: ✅ 匹配 /user/register
  ↓ 放行（不需要登录）
  ↓ UserController.register()
  ↓ 注册成功
  ↓ 返回: {"code": 200, ...}
```

### 3.2 路径对应表

| 前端请求 | Gateway 接收 | Gateway 白名单 | StripPrefix 后 | User Service 接收 | User Service 白名单 | Controller 方法 |
|---------|-------------|---------------|----------------|------------------|-------------------|----------------|
| `/api/user/register` | `/api/user/register` | ✅ 匹配 | `/user/register` | `/user/register` | ✅ 匹配 | `UserController.register()` |
| `/api/user/auth/register` | `/api/user/auth/register` | ✅ 匹配 | `/user/auth/register` | `/user/auth/register` | ✅ 匹配 | `UserAuthController.register()` |
| `/api/user/profile` | `/api/user/profile` | ❌ 需要Token | `/user/profile` | `/user/profile` | ❌ 需要登录 | `UserController.getProfile()` |

---

## 四、立即执行

### 4.1 重启 User Service

**IDEA 操作**:
```
1. 停止 MarketServiceUserApplication ⏹️
2. 等待完全停止
3. 重新运行 ▶️
4. 等待看到 "Started MarketServiceUserApplication"
```

**命令行操作**:
```bash
cd D:\program\Market\market-service\market-service-user
mvn spring-boot:run
```

### 4.2 测试注册

**方式1: 前端测试**
1. 打开 `http://localhost:5173`
2. 进入注册页面
3. 填写信息并提交

**方式2: curl 测试**
```bash
curl -X POST http://localhost:9000/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser999",
    "password": "123456",
    "confirmPassword": "123456",
    "nickname": "测试用户",
    "phone": "13900139999"
  }'
```

### 4.3 预期结果

**后端日志**:
```
Gateway:
DEBUG: Gateway 收到请求: /api/user/register
DEBUG: 白名单路径，直接放行: /api/user/register

User Service:
INFO: Sa-Token interceptor checking path: /user/register
INFO: Path /user/register is in whitelist, skip authentication
INFO: 处理注册请求...
```

**前端显示**:
```
✅ 注册成功，请登录
✅ 自动跳转到登录页
```

**返回数据**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": 1
}
```

---

## 五、为什么会出现这个问题？

### 5.1 StripPrefix 的作用

Gateway 配置：
```yaml
routes:
  - id: market-service-user
    uri: lb://market-service-user
    predicates:
      - Path=/api/user/**
    filters:
      - StripPrefix=1  # ← 关键配置
```

**StripPrefix=1 的含义**:
- 移除路径中的**第一段**（以 `/` 分隔）
- `/api/user/register` → `/user/register`
- `/api/user/auth/login` → `/user/auth/login`

**为什么需要 StripPrefix**？
- Gateway 需要 `/api` 前缀来区分不同服务（用户、商品、交易）
- 但 User Service 的 Controller 不包含 `/api` 前缀
- 所以 Gateway 转发时需要移除 `/api`

### 5.2 白名单配置的时机

```
Gateway 白名单检查
  ↓ 在 StripPrefix 之前
  ↓ 检查的是 /api/user/register
  
User Service 白名单检查
  ↓ 在 StripPrefix 之后
  ↓ 检查的是 /user/register  ← 注意没有 /api
```

**所以两处白名单配置不同**:
- Gateway: `/api/user/register`（包含 /api）
- User Service: `/user/register`（不包含 /api）

---

## 六、验证检查清单

### User Service 启动检查

- [ ] 启动无错误
- [ ] 看到 "Started MarketServiceUserApplication"
- [ ] 看到 "nacos registry...register finished"

### 注册功能检查

- [ ] 前端可以访问注册页面
- [ ] 填写表单可以提交
- [ ] **不再显示 401 错误** ✅
- [ ] **不再显示 500 错误** ✅
- [ ] 显示"注册成功"提示
- [ ] 自动跳转到登录页

### 日志检查

- [ ] Gateway: "白名单路径，直接放行"
- [ ] User Service: "Path /user/register is in whitelist"
- [ ] User Service: 没有 "NotLoginException"
- [ ] User Service: 没有 "未能读取到有效Token"

### 数据库检查

```sql
USE market_user;
SELECT * FROM user ORDER BY id DESC LIMIT 1;
-- 应该能看到刚注册的用户
```

---

## 七、故障排查

### 如果还是 401 错误

**检查 User Service 日志**:
```
如果看到:
INFO: Sa-Token interceptor checking path: /user/register
INFO: Path /user/register is in whitelist, skip authentication

说明白名单生效了 ✅

如果没有看到第二行，说明白名单配置有问题 ❌
```

**解决方案**:
1. 确认 `SaTokenConfig.java` 已保存
2. 确认 User Service 已重启
3. 清理编译：`mvn clean compile`

### 如果日志显示路径不对

**如果日志显示**:
```
INFO: Sa-Token interceptor checking path: /api/user/register
```

**说明**:
- User Service 接收到的路径还包含 `/api`
- Gateway 的 StripPrefix 没有生效
- 需要检查 Gateway 配置

---

## 八、总结

这次问题的核心在于理解 **Gateway StripPrefix 对路径的影响**：

```
完整流程：

前端: /api/user/register
  ↓
Gateway 白名单: /api/user/register ✅
  ↓
StripPrefix=1
  ↓
User Service 白名单: /user/register ✅
  ↓
Controller: /user + /register ✅
```

**关键点**:
1. Gateway 的白名单检查在 StripPrefix **之前**
2. User Service 的白名单检查在 StripPrefix **之后**
3. 两处白名单配置要匹配各自接收到的实际路径

---

**修复完成！重启 User Service 后应该可以正常注册了。** 🎉

---

**相关文档**:
- [GATEWAY_NACOS_FIX.md](./GATEWAY_NACOS_FIX.md) - Gateway 和 Nacos 配置修复
- [FINAL_OPERATION_GUIDE.md](./FINAL_OPERATION_GUIDE.md) - User Service 操作指南
- [CLASSCAST_FIX.md](./CLASSCAST_FIX.md) - ClassCastException 修复

