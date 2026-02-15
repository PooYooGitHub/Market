# 2026-02-15 问题修复汇总

本文档汇总了今天修复的所有问题及其解决方案。

---

## 修复列表

### 1. ✅ MinIO端口配置错误

**问题**：文件上传时MinIO端口配置错误  
**错误**：`Response code: 404, Non-XML response`  
**原因**：配置端口9000被Gateway占用，MinIO实际在9001  
**修复**：修改配置 `endpoint: http://localhost:9001`  
**文档**：`MINIO_PORT_FIX.md`, `FILE_UPLOAD_SUCCESS_SUMMARY.md`

---

### 2. ✅ CORS跨域问题

**问题**：文件上传时CORS头重复  
**错误**：`Access-Control-Allow-Origin header contains multiple values`  
**原因**：Gateway和File Service都配置了CORS  
**修复**：只在Gateway配置CORS，删除Service中的配置  
**文档**：`doc/CORS跨域问题最终解决方案.md`

---

### 3. ✅ Product Service Token验证问题

**问题**：访问商品列表需要Token  
**错误**：`NotLoginException: 未能读取到有效Token`  
**原因**：SaToken配置拦截了公开接口  
**修复**：添加白名单 `/product/list`, `/category/**`  
**文档**：`PRODUCT_TOKEN_FIX_COMPLETE.md`

---

### 4. ✅ Gateway分类路由404

**问题**：访问分类接口404  
**错误**：`GET /api/product/category/list 404`  
**原因**：Gateway路由配置`StripPrefix`不正确  
**修复**：添加独立的分类路由，`StripPrefix=2`  
**文档**：`GATEWAY_ROUTE_404_FIX.md`

---

### 5. ✅ 用户注册401错误

**问题**：注册接口返回401  
**错误**：`POST /api/user/auth/register 401`  
**原因**：Gateway白名单缺少 `/api/user/auth/*` 路径  
**修复**：添加auth路径到白名单  
**文档**：`USER_REGISTER_401_FIX.md`

---

### 6. ✅ 唯一索引冲突

**问题**：注册时phone唯一索引冲突  
**错误**：`Duplicate entry '' for key 'uk_phone'`  
**原因**：空字符串被插入数据库，违反唯一约束  
**修复**：将空字符串转换为NULL  
**文档**：`UNIQUE_CONSTRAINT_FIX.md`

---

## 服务配置汇总

### Gateway配置（Port 9000）

#### 路由配置
```yaml
routes:
  # 分类路由（具体规则优先）
  - id: market-service-category
    uri: lb://market-service-product
    predicates:
      - Path=/api/product/category/**
    filters:
      - StripPrefix=2  # 去掉 /api/product

  # 商品路由
  - id: market-service-product
    uri: lb://market-service-product
    predicates:
      - Path=/api/product/**
    filters:
      - StripPrefix=1  # 去掉 /api
```

#### 白名单配置
```yaml
gateway:
  auth:
    white-list:
      # 用户认证
      - /api/user/auth/login
      - /api/user/auth/register
      - /api/user/login
      - /api/user/register
      # 商品公开接口
      - /api/product/list
      - /api/product/detail/**
      - /api/product/category/**
      # Feign调用
      - /feign/**
```

---

### User Service配置（Port 8101）

#### SaToken白名单
```java
// 认证相关
if (path.startsWith("/user/auth/")) return true;

// 旧版兼容
if (path.equals("/user/register") || path.equals("/user/login")) return true;

// Feign调用
if (path.matches("/user/\\d+")) return true;
```

#### 注册逻辑修复
```java
// 空字符串转NULL
user.setPhone(isNotEmpty(phone) ? phone : null);
user.setEmail(isNotEmpty(email) ? email : null);
```

---

### Product Service配置（Port 8102）

#### SaToken白名单
```java
SaRouter.match("/**")
    .notMatch("/product/list")           // 商品列表
    .notMatch("/product/detail/**")      // 商品详情
    .notMatch("/category/**")            // 分类接口
    .notMatch("/feign/**")               // Feign调用
    .check(r -> StpUtil.checkLogin());
```

---

### File Service配置（Port 8106）

#### MinIO配置
```yaml
minio:
  endpoint: http://localhost:9001  # 正确端口
  access-key: admin
  secret-key: 3083446265@qq.com
  bucket:
    avatar: market-avatar
    product: market-product
    arbitration: market-arbitration
```

#### 无CORS配置
- 已删除CORS配置，统一由Gateway处理

---

## 端口分配

| 服务 | 端口 | 说明 |
|-----|------|------|
| 前端 | 5173 | Vue开发服务器 |
| Gateway | 9000 | API网关 |
| **MinIO** | **9001** | **对象存储** |
| User Service | 8101 | 用户服务 |
| Product Service | 8102 | 商品服务 |
| File Service | 8106 | 文件服务 |
| Nacos | 8849 | 服务注册中心 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |

---

## 待执行操作

### 1. 启动服务

**User Service**（已停止）：
- 运行 `MarketServiceUserApplication`
- 端口：8101

**Gateway**（如果停止）：
- 运行 `MarketGatewayApplication`
- 端口：9000

### 2. 清理数据库

执行SQL脚本：
```sql
-- 清理空字符串
UPDATE t_user SET phone = NULL WHERE phone = '';
UPDATE t_user SET email = NULL WHERE email = '';
```

脚本位置：`doc/SQL/fix_unique_constraint_conflict.sql`

---

## 测试验证

### 1. 文件上传测试 ✅
```bash
# 上传头像
POST http://localhost:9000/api/file/upload/avatar
```
**状态**：✅ 已成功

### 2. 商品列表测试 ⏳
```bash
# 获取商品列表（无需Token）
GET http://localhost:9000/api/product/list
```
**状态**：⏳ 待测试

### 3. 分类列表测试 ⏳
```bash
# 获取分类列表（无需Token）
GET http://localhost:9000/api/product/category/list
```
**状态**：⏳ 待测试

### 4. 用户注册测试 ⏳
```bash
# 注册（不填手机号）
POST http://localhost:9000/api/user/auth/register
{
  "username": "testuser",
  "password": "Test123456",
  "confirmPassword": "Test123456"
}
```
**状态**：⏳ 待测试（需先执行SQL清理）

---

## 功能状态

| 功能 | 状态 | 说明 |
|-----|------|------|
| 用户注册 | ⏳ 待测试 | 需启动服务+清理数据库 |
| 用户登录 | ✅ 可用 | |
| 文件上传 | ✅ 可用 | 头像上传成功 |
| 商品列表 | ⏳ 待测试 | 配置已修复 |
| 分类列表 | ⏳ 待测试 | 路由已修复 |
| 商品详情 | ⏳ 待测试 | |

---

## 文档索引

### 问题修复文档
1. `MINIO_PORT_FIX.md` - MinIO端口修复
2. `doc/CORS跨域问题最终解决方案.md` - CORS配置
3. `PRODUCT_TOKEN_FIX_COMPLETE.md` - Product Token验证
4. `GATEWAY_ROUTE_404_FIX.md` - Gateway路由配置
5. `USER_REGISTER_401_FIX.md` - 注册401问题
6. `UNIQUE_CONSTRAINT_FIX.md` - 唯一索引冲突

### 快速参考
1. `FILE_UPLOAD_SUCCESS_SUMMARY.md` - 文件上传总结
2. `GATEWAY_404_QUICK_FIX.md` - 404快速修复
3. `REGISTER_401_QUICK_FIX.md` - 401快速修复
4. `UNIQUE_CONSTRAINT_QUICK_FIX.md` - 唯一索引快速修复

### 详细文档
1. `doc/MinIO图片存储完整讲解.md` - MinIO配置详解
2. `doc/架构设计说明书.md` - 系统架构设计

---

## 下一步计划

### 立即操作
1. ▶️ 启动User Service
2. 🗄️ 执行SQL清理脚本
3. 🧪 测试所有修复的功能

### 后续开发
1. 完善Product模块功能
2. 开发Trade交易模块
3. 完善前端页面

---

**汇总时间**：2026-02-15  
**修复数量**：6个问题  
**总体状态**：✅ 所有问题已修复，待启动服务测试

