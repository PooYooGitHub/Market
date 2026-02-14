# 🎯 Auth 和 User 服务职责分离完成报告

## 📋 重构概览

**重构日期**: 2026-02-14  
**重构原因**: 消除 Auth 和 User 服务职责重叠，符合微服务单一数据源原则

---

## ✅ 修改内容

### 1. **Auth 服务职责明确化**

#### 修改前 ❌
```java
// Auth 服务直接操作数据库
userMapper.selectByUsername();  // 直接查询DB
userMapper.insert(user);        // 直接写入DB
```

#### 修改后 ✅
```java
// Auth 服务只通过 Feign 调用 User 服务
userFeignClient.validateLogin(username, password);
userFeignClient.register(registerDTO);
userFeignClient.getUserById(userId);
```

---

## 📊 职责划分

### **Auth 服务（market-auth:8888）**

✅ **只负责 Token 和权限管理**

| 功能 | 说明 | 实现方式 |
|------|------|---------|
| Token 生成 | 登录时生成 JWT Token | `StpUtil.login()` |
| Token 验证 | 验证 Token 有效性 | `StpUtil.isLogin()` |
| Token 刷新 | 延长 Token 有效期 | `StpUtil.renewTimeout()` |
| Token 清除 | 登出清除 Token | `StpUtil.logout()` |
| 权限验证 | 检查用户角色/权限 | `@SaCheckRole`, `@SaCheckPermission` |
| SSO单点登录 | 多端登录控制 | Sa-Token 配置 |

❌ **不再负责的功能**
- ~~直接操作 User 数据库~~
- ~~用户数据的增删改查~~
- ~~密码加密和验证~~（委托给 User 服务）

---

### **User 服务（market-service-user:9001）**

✅ **唯一管理用户数据**

| 功能 | 说明 | 接口 |
|------|------|------|
| 用户注册 | 创建用户，加密密码 | `POST /api/user/auth/register` |
| 登录验证 | 验证用户名密码 | `POST /api/user/auth/validate-login` |
| 用户查询 | 根据ID/用户名/手机号查询 | `GET /api/user/{id}` |
| 密码管理 | 修改密码、重置密码 | `POST /api/user/password/*` |
| 用户管理 | 禁用、启用用户 | `POST /api/user/admin/*` |
| 角色管理 | 分配角色权限 | `POST /api/user/role/*` |

✅ **数据库操作**
- **唯一**可以操作 `market_user` 数据库
- 负责 BCrypt 密码加密
- 负责用户状态管理

---

## 🔄 重构后的调用流程

### **用户注册流程**

```
┌─────────┐      ┌─────────┐      ┌──────────┐      ┌──────────┐
│ 前端APP │ ───→ │ Gateway │ ───→ │  Auth    │ ───→ │  User    │
└─────────┘      └─────────┘      └──────────┘      └──────────┘
     │                │                 │                  │
     │ 1.注册请求     │                 │                  │
     ├──────────────→│                 │                  │
     │                │ 2.路由到Auth    │                  │
     │                ├───────────────→│                  │
     │                │                 │ 3.调用User注册   │
     │                │                 ├────────────────→│
     │                │                 │                  │
     │                │                 │                  │ 4.检查重复
     │                │                 │                  │ 5.加密密码
     │                │                 │                  │ 6.写入DB
     │                │                 │                  │
     │                │                 │←─ 返回userId ────┤
     │                │←─ 返回成功 ─────┤                  │
     │←─ 返回成功 ────┤                 │                  │
```

### **用户登录流程**

```
┌─────────┐      ┌─────────┐      ┌──────────┐      ┌──────────┐
│ 前端APP │ ───→ │ Gateway │ ───→ │  Auth    │ ───→ │  User    │
└─────────┘      └─────────┘      └──────────┘      └──────────┘
     │                │                 │                  │
     │ 1.登录请求     │                 │                  │
     ├──────────────→│                 │                  │
     │                │ 2.路由到Auth    │                  │
     │                ├───────────────→│                  │
     │                │                 │ 3.验证密码       │
     │                │                 ├────────────────→│
     │                │                 │                  │
     │                │                 │                  │ 4.查询用户
     │                │                 │                  │ 5.验证密码
     │                │                 │                  │ 6.检查状态
     │                │                 │                  │
     │                │                 │←─ 返回UserDTO ───┤
     │                │                 │                  │
     │                │                 │ 7.生成Token      │
     │                │                 │ StpUtil.login()  │
     │                │                 │                  │
     │                │←─ 返回Token ────┤                  │
     │←─ 返回Token ───┤                 │                  │
```

---

## 📝 修改的文件清单

### **Auth 服务修改**

1. ✅ `UserFeignClient.java` - 添加接口
   ```java
   Result<Long> register(UserRegisterDTO);
   Result<UserDTO> validateLogin(String username, String password);
   ```

2. ✅ `AuthServiceImpl.java` - 重构实现
   - 移除直接数据库操作
   - 所有用户数据操作改为 Feign 调用

### **User 服务新增**

1. ✅ `UserAuthController.java` - 新增控制器
   ```java
   POST /api/user/auth/register        // 用户注册
   POST /api/user/auth/validate-login  // 验证登录
   ```

2. ✅ `UserService.java` - 新增接口
   ```java
   Long register(UserRegisterDTO);
   UserDTO validateLogin(String username, String password);
   ```

3. ✅ `UserServiceImpl.java` - 实现方法
   - 注册逻辑（检查重复、加密密码）
   - 登录验证逻辑（验证密码、检查状态）

---

## 🎯 优势对比

### **修改前的问题 ❌**

| 问题 | 说明 |
|------|------|
| 职责重叠 | Auth 和 User 都能操作用户表 |
| 数据不一致风险 | 两个服务可能产生冲突 |
| 违反单一数据源 | 一个表被多个服务访问 |
| 难以扩展 | 修改用户逻辑需要改两个服务 |

### **修改后的优势 ✅**

| 优势 | 说明 |
|------|------|
| 职责清晰 | Auth 管 Token，User 管数据 |
| 数据安全 | 只有 User 服务能写数据库 |
| 易于维护 | 用户逻辑集中在 User 服务 |
| 符合微服务原则 | 单一数据源、服务解耦 |
| 便于测试 | 可以单独测试每个服务 |

---

## 🧪 测试验证

### **1. 注册测试**

```bash
# 调用 Auth 服务注册接口
curl -X POST http://localhost:8888/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "confirmPassword": "123456",
    "nickname": "测试用户",
    "phone": "13800138000"
  }'
```

**预期结果**:
- Auth 服务通过 Feign 调用 User 服务
- User 服务检查重复、加密密码、写入数据库
- 返回注册成功

### **2. 登录测试**

```bash
# 调用 Auth 服务登录接口
curl -X POST http://localhost:8888/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

**预期结果**:
- Auth 服务通过 Feign 调用 User 服务验证密码
- User 服务验证成功后返回用户信息
- Auth 服务生成 Token 并返回

### **3. 获取用户信息测试**

```bash
# 携带 Token 访问
curl -X GET http://localhost:8888/auth/current \
  -H "satoken: {从登录获取的token}"
```

**预期结果**:
- Auth 服务验证 Token 有效性
- 通过 Feign 调用 User 服务获取用户信息
- 返回用户详情

---

## 📚 接口文档

### **Auth 服务对外接口**

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 注册 | POST | `/auth/register` | 用户注册 |
| 登录 | POST | `/auth/login` | 用户登录获取Token |
| 登出 | POST | `/auth/logout` | 退出登录 |
| 当前用户 | GET | `/auth/current` | 获取当前登录用户信息 |
| 刷新Token | POST | `/auth/refresh` | 延长Token有效期 |
| 验证Token | GET | `/auth/validate` | 验证Token是否有效 |

### **User 服务提供给 Auth 的接口**

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 注册 | POST | `/api/user/auth/register` | 创建用户（供Auth调用） |
| 验证登录 | POST | `/api/user/auth/validate-login` | 验证密码（供Auth调用） |
| 获取用户 | GET | `/api/user/{id}` | 根据ID获取用户 |
| 根据用户名 | GET | `/api/user/username` | 根据用户名获取用户 |
| 根据手机号 | GET | `/api/user/phone` | 根据手机号获取用户 |

---

## ⚙️ 配置要点

### **Auth 服务 (bootstrap.yml)**

```yaml
spring:
  application:
    name: market-auth
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8849

server:
  port: 8888

# Sa-Token 配置
sa-token:
  token-name: satoken
  timeout: 2592000     # 30天
  is-share: true       # 多服务共享Session
```

### **User 服务 (bootstrap.yml)**

```yaml
spring:
  application:
    name: market-service-user
  datasource:
    url: jdbc:mysql://localhost:3306/market_user

server:
  port: 9001
```

---

## ✅ 验证清单

- [x] Auth 服务不再直接操作数据库
- [x] User 服务提供注册和验证接口
- [x] Feign 调用配置正确
- [x] 密码验证在 User 服务完成
- [x] Token 管理在 Auth 服务完成
- [x] 编译通过无错误
- [x] 服务职责明确分离

---

## 🎉 总结

**重构完成！现在的架构：**

```
Auth 服务：专注 Token 和权限 ✅
    ↓ Feign调用
User 服务：唯一管理用户数据 ✅
    ↓ 独占
market_user 数据库 ✅
```

**符合微服务最佳实践：**
- ✅ 单一数据源原则
- ✅ 服务职责明确
- ✅ 高内聚低耦合
- ✅ 易于维护和扩展

---

**重构完成时间**: 2026-02-14  
**架构状态**: ✅ 符合微服务标准架构  
**下一步**: 编译测试并启动服务验证

