# User 模块开发完成文档

## 📋 目录
- [功能概览](#功能概览)
- [API 接口清单](#api-接口清单)
- [数据库设计](#数据库设计)
- [测试指南](#测试指南)

---

## 功能概览

### 已完成功能

#### 1. 用户认证模块 (`UserAuthController`)
- ✅ 用户注册
- ✅ 用户登录
- ✅ 用户登出

#### 2. 用户信息模块 (`UserController`)
- ✅ 获取当前用户信息
- ✅ 更新用户信息
- ✅ 修改密码
- ✅ 检查登录状态

#### 3. 用户管理模块 (`UserAdminController`)
- ✅ 分页查询用户列表（支持关键词搜索、状态筛选、时间范围）
- ✅ 查询用户详情
- ✅ 启用/禁用用户
- ✅ 删除用户（软删除，status=0）
- ✅ 重置用户密码
- ✅ 批量删除用户
- ✅ 用户数据统计

#### 4. Feign 接口模块 (`UserFeignController`)
- ✅ 根据ID获取用户（内部调用）
- ✅ 根据用户名获取用户（内部调用）
- ✅ 根据手机号获取用户（内部调用）

---

## API 接口清单

### 用户认证接口

#### 1. 用户注册
```http
POST /api/user/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456",
  "confirmPassword": "123456",
  "nickname": "测试用户",
  "phone": "13800138000",
  "email": "test@example.com"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "phone": "13800138000",
    "email": "test@example.com",
    "status": 1,
    "createTime": "2026-02-14T12:00:00"
  }
}
```

#### 2. 用户登录
```http
POST /api/user/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400,
    "userInfo": {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户"
    }
  }
}
```

#### 3. 用户登出
```http
POST /api/user/auth/logout
Authorization: Bearer {token}
```

---

### 用户信息接口

#### 4. 获取当前用户信息
```http
GET /api/user/current
Authorization: Bearer {token}
```

#### 5. 更新用户信息
```http
PUT /api/user/update
Authorization: Bearer {token}
Content-Type: application/json

{
  "nickname": "新昵称",
  "avatar": "https://example.com/avatar.jpg",
  "email": "newemail@example.com"
}
```

#### 6. 修改密码
```http
PUT /api/user/change-password
Authorization: Bearer {token}
Content-Type: application/json

{
  "oldPassword": "123456",
  "newPassword": "654321",
  "confirmPassword": "654321"
}
```

#### 7. 检查登录状态
```http
GET /api/user/check
Authorization: Bearer {token}
```

---

### 用户管理接口（管理员）

#### 8. 分页查询用户列表
```http
GET /api/user/admin/list?current=1&size=10&keyword=test&status=1
Authorization: Bearer {adminToken}
```

**查询参数：**
- `current`: 当前页码（默认1）
- `size`: 每页大小（默认10）
- `keyword`: 关键词（用户名、昵称、手机号）
- `status`: 状态筛选（0-已删除，1-正常，2-禁用）
- `startTime`: 开始时间
- `endTime`: 结束时间

**响应示例：**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "username": "testuser",
        "nickname": "测试用户",
        "phone": "13800138000",
        "status": 1,
        "createTime": "2026-02-14T12:00:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

#### 9. 查询用户详情
```http
GET /api/user/admin/{id}
Authorization: Bearer {adminToken}
```

#### 10. 启用用户
```http
PUT /api/user/admin/{id}/enable
Authorization: Bearer {adminToken}
```

#### 11. 禁用用户
```http
PUT /api/user/admin/{id}/disable
Authorization: Bearer {adminToken}
```

#### 12. 删除用户（软删除）
```http
DELETE /api/user/admin/{id}
Authorization: Bearer {adminToken}
```

#### 13. 重置用户密码
```http
PUT /api/user/admin/{id}/reset-password?newPassword=123456
Authorization: Bearer {adminToken}
```

#### 14. 批量删除用户
```http
DELETE /api/user/admin/batch
Authorization: Bearer {adminToken}
Content-Type: application/json

[1, 2, 3]
```

#### 15. 用户数据统计
```http
GET /api/user/admin/statistics
Authorization: Bearer {adminToken}
```

**响应示例：**
```json
{
  "code": 200,
  "data": {
    "totalCount": 1000,
    "activeCount": 950,
    "disabledCount": 30,
    "deletedCount": 20,
    "todayRegister": 15
  }
}
```

---

### Feign 内部调用接口

#### 16. 根据ID获取用户
```http
GET /api/user/{id}
```

#### 17. 根据用户名获取用户
```http
GET /api/user/username?username=testuser
```

#### 18. 根据手机号获取用户
```http
GET /api/user/phone?phone=13800138000
```

---

## 数据库设计

### t_user 表结构

```sql
CREATE TABLE `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` int(1) NOT NULL DEFAULT '1' COMMENT '状态：0-已删除，1-正常，2-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### Status 字段说明
- **0**: 已删除（软删除）- 查询时自动过滤
- **1**: 正常状态
- **2**: 禁用状态

---

## 测试指南

### 1. 快速测试流程

#### Step 1: 启动服务
```powershell
# 启动 Nacos
.\start-infrastructure.ps1

# 启动核心服务
.\start-light.ps1
```

#### Step 2: 注册测试用户
```bash
curl -X POST http://localhost:9001/api/user/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "confirmPassword": "123456",
    "nickname": "测试用户",
    "phone": "13800138000"
  }'
```

#### Step 3: 登录获取 Token
```bash
curl -X POST http://localhost:9001/api/user/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

#### Step 4: 使用 Token 访问接口
```bash
curl http://localhost:9001/api/user/current \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 2. Postman 测试集合

#### 环境变量设置
```json
{
  "baseUrl": "http://localhost:9001",
  "token": ""
}
```

#### 测试场景

**场景1：用户注册登录流程**
1. 注册新用户
2. 登录获取token
3. 查看个人信息
4. 更新个人信息
5. 修改密码

**场景2：管理员管理用户**
1. 查询用户列表
2. 查看用户详情
3. 禁用用户
4. 启用用户
5. 删除用户
6. 查看统计数据

**场景3：软删除验证**
1. 删除用户（status设为0）
2. 尝试通过Feign接口查询（应返回null）
3. 尝试登录（应失败）
4. 管理员可以在列表中看到（筛选status=0）

---

### 3. 单元测试

运行用户服务测试：
```powershell
cd D:\program\Market
mvn test -pl market-service/market-service-user
```

---

## 技术栈

- **Spring Boot 2.7.18**
- **Spring Cloud 2021.0.8**
- **MyBatis Plus 3.5.x** - ORM框架
- **Sa-Token 1.37.0** - 认证授权
- **BCrypt** - 密码加密
- **Hutool 5.8.16** - 工具库
- **Swagger 2.9.2** - API文档

---

## 安全特性

### 1. 密码安全
- ✅ 使用 BCrypt 加密存储
- ✅ 密码不在响应中返回
- ✅ 修改密码后自动踢下线

### 2. 状态管理
- ✅ 已删除用户（status=0）无法登录
- ✅ 禁用用户（status=2）无法登录
- ✅ 状态变更后自动踢下线

### 3. 权限控制
- ✅ 用户只能操作自己的信息
- ✅ 管理员接口需要特殊权限（待实现）
- ✅ Feign接口只供内部调用

---

## 下一步计划

### 待开发功能
- [ ] 角色权限管理（RBAC）
- [ ] 用户头像上传
- [ ] 手机验证码登录
- [ ] 第三方登录（微信、QQ）
- [ ] 登录日志记录
- [ ] 操作审计日志
- [ ] 用户画像分析

### 优化计划
- [ ] 添加 Redis 缓存
- [ ] 接口限流
- [ ] 敏感操作二次验证
- [ ] 完善单元测试覆盖率
- [ ] API文档自动生成

---

## 常见问题

### Q1: 如何区分普通用户和管理员？
A: 当前版本未实现角色系统，所有用户权限相同。下一步将实现RBAC权限模型。

### Q2: 软删除的用户如何恢复？
A: 管理员可以通过修改 status 字段从 0 改为 1 来恢复用户。

### Q3: Feign接口如何防止外部调用？
A: 建议通过网关配置，只允许内网访问这些接口，或者添加内部鉴权token。

---

**文档版本**: 1.0  
**最后更新**: 2026-02-14  
**维护者**: Development Team

