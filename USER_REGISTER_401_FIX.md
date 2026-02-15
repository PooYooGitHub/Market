# 用户注册401错误修复

## 问题描述

前端注册时出现401错误：
```
POST http://localhost:9000/api/user/auth/register 401 (Unauthorized)
```

## 问题原因

### 前端API路径
```javascript
// front/vue-project/src/api/user.js
url: '/api/user/auth/register'
```

### Gateway白名单缺失
Gateway的白名单配置**没有包含** `/api/user/auth/*` 路径：

**原配置**：
```yaml
white-list:
  - /api/user/login
  - /api/user/register
```

**前端实际请求**：
- `/api/user/auth/register` ❌ 不在白名单
- `/api/user/auth/login` ❌ 不在白名单

**结果**：Gateway拦截器验证Token失败 → 返回401

---

## 解决方案

### 修改Gateway白名单

**文件**：`market-gateway/resources/bootstrap.yml`

**修改内容**：
```yaml
gateway:
  auth:
    white-list:
      # 用户认证接口（新架构）
      - /api/user/auth/login
      - /api/user/auth/register
      # 用户接口（旧版兼容）
      - /api/user/login
      - /api/user/register
      # 商品相关公开接口
      - /api/product/list
      - /api/product/detail/**
      - /api/product/category/**
      # Feign内部调用
      - /feign/**
```

---

## 路径说明

### 为什么有两套路径？

#### 新架构（推荐）
```
前端: /api/user/auth/register
       ↓ Gateway (StripPrefix=1, 去掉 /api)
User Service: /user/auth/register
       ↓ Controller
@RequestMapping("/user/auth") + @PostMapping("/register")
```

#### 旧版（兼容）
```
前端: /api/user/register
       ↓ Gateway (StripPrefix=1, 去掉 /api)
User Service: /user/register
       ↓ Controller
@RequestMapping("/user") + @PostMapping("/register")
```

### User Service有两个Controller

1. **UserAuthController** (`/user/auth/*`)
   - 用于Auth Service调用
   - 新架构设计
   - 路径：`/user/auth/register`, `/user/auth/login`, `/user/auth/validate-login`

2. **UserController** (`/user/*`)
   - 直接用户操作
   - 旧版兼容
   - 路径：`/user/register`, `/user/login`, `/user/info`

---

## User Service白名单验证

**文件**：`market-service-user/config/SaTokenConfig.java`

User Service的白名单**已经包含**了auth路径：

```java
private boolean isWhiteListPath(String path) {
    // 认证相关接口（Gateway StripPrefix 后的路径）
    if (path.startsWith("/user/auth/")) {  // ✅ 已包含
        return true;
    }
    
    // 旧版注册登录接口（兼容）
    if (path.equals("/user/register") || path.equals("/user/login")) {
        return true;
    }
    // ...
}
```

**所以User Service这边不需要修改！**

---

## 重启服务

### 需要重启：Gateway

**原因**：只修改了Gateway的白名单配置

**Gateway进程**：
- PID: 24092
- 端口: 9000

**重启方法**：

**方式1：命令行**
```powershell
Stop-Process -Id 24092 -Force
```
然后在IDE中重新运行 `MarketGatewayApplication`

**方式2：IDE重启**
- 停止当前运行的Gateway
- 重新运行 `MarketGatewayApplication`

### 不需要重启

- ✅ User Service - 白名单已经正确配置
- ✅ Product Service - 不涉及
- ✅ File Service - 不涉及

---

## 测试验证

重启Gateway后测试：

### 1. 用户注册（修复的接口）
```bash
curl -X POST http://localhost:9000/api/user/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123456",
    "confirmPassword": "Test123456",
    "nickname": "测试用户",
    "phone": "13800138000",
    "email": "test@example.com"
  }'
```
**预期**：✅ 200 OK，注册成功

### 2. 用户登录
```bash
curl -X POST http://localhost:9000/api/user/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123456"
  }'
```
**预期**：✅ 200 OK，返回Token

### 3. 前端测试
1. 访问前端注册页面
2. 填写注册信息
3. 提交注册
4. **预期**：注册成功，自动跳转到登录页

---

## 路由流程图

### 注册流程

```
前端
  ↓ POST /api/user/auth/register
Gateway (9000)
  ↓ 检查白名单: /api/user/auth/register ✅
  ↓ StripPrefix=1: 去掉 /api
  ↓ 转发: /user/auth/register
User Service (8101)
  ↓ SaToken检查: /user/auth/* ✅
  ↓ Controller: @RequestMapping("/user/auth") + @PostMapping("/register")
  ↓ UserAuthController.register()
  ↓ UserService.register()
  ↓ 保存用户到数据库
  ↓ 返回: Result.success("注册成功", userId)
Gateway
  ↓ 转发响应
前端
  ↓ 显示注册成功
```

---

## 白名单完整配置

### Gateway白名单
```yaml
gateway:
  auth:
    white-list:
      # 用户认证（新架构）
      - /api/user/auth/login
      - /api/user/auth/register
      # 用户接口（旧版）
      - /api/user/login
      - /api/user/register
      # 商品公开接口
      - /api/product/list
      - /api/product/detail/**
      - /api/product/category/**
      # Feign调用
      - /feign/**
```

### User Service白名单
```java
// /user/auth/* - 认证相关
// /user/register, /user/login - 旧版兼容
// /user/{id}, /user/username, /user/phone - Feign调用
// /health - 健康检查
// /doc.html, /swagger-resources - 文档
```

### Product Service白名单
```java
// /product/list - 商品列表
// /product/detail/** - 商品详情
// /category/** - 分类接口
// /feign/** - Feign调用
```

---

## 前端API对照表

| 前端请求 | Gateway转发 | Service处理 | Controller |
|---------|------------|------------|-----------|
| `/api/user/auth/register` | `/user/auth/register` | UserAuthController | `@RequestMapping("/user/auth")` |
| `/api/user/auth/login` | `/user/auth/login` | UserAuthController | `@RequestMapping("/user/auth")` |
| `/api/user/register` | `/user/register` | UserController | `@RequestMapping("/user")` |
| `/api/user/login` | `/user/login` | UserController | `@RequestMapping("/user")` |

---

## 注意事项

### 1. 两套接口的区别

- **UserAuthController** (`/user/auth/*`)：设计给Auth Service使用
- **UserController** (`/user/*`)：直接用户操作

目前两个都可以使用，但推荐使用 `/auth/*` 路径。

### 2. 保持白名单一致

确保Gateway和各Service的白名单配置保持一致：
- Gateway: 带 `/api` 前缀
- Service: 不带 `/api` 前缀（因为StripPrefix已去掉）

### 3. 前端统一使用新路径

建议前端统一使用 `/api/user/auth/*` 路径：
- 更清晰的语义
- 符合新架构设计
- 未来扩展性更好

---

## 检查清单

重启Gateway后确认：

- [ ] Gateway启动成功（端口9000）
- [ ] 前端可以正常注册（无401错误）
- [ ] 前端可以正常登录
- [ ] Gateway日志显示auth路径在白名单中
- [ ] User Service正常接收注册请求

---

**修复时间**：2026-02-15  
**修复状态**：✅ 已修复Gateway配置  
**需要重启**：Gateway (PID: 24092, Port: 9000)

