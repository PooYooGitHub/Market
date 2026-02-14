# ⚠️ 代码与数据库表结构对齐修改报告

## 📋 修改概览

根据 `doc/SQL/init_schema.sql` 中的表结构定义，对代码进行了以下修正：

---

## 🔧 修改内容

### 1. **数据库连接配置修正** ✅

**文件**: `market-service-core/src/main/resources/bootstrap.yml`

**修改前**:
```yaml
url: jdbc:mysql://localhost:3306/market?...
```

**修改后**:
```yaml
url: jdbc:mysql://localhost:3306/market_user?...
namespace: dev  # 添加 Nacos namespace
```

**原因**: SQL 定义的数据库名为 `market_user`，不是 `market`

---

### 2. **Status 字段语义修正** ⚠️ **重要**

#### 表结构定义
```sql
`status` tinyint(4) DEFAULT 1 COMMENT '状态 1:正常 0:禁用'
```

#### 代码修改前的理解（错误）
- 0 = 已删除（软删除）
- 1 = 正常
- 2 = 禁用

#### 代码修改后（与表结构一致）
- **1 = 正常**
- **0 = 禁用**
- **无软删除标记**（删除=物理删除）

---

### 3. **具体代码修改**

#### 3.1 登录验证逻辑

**文件**: `UserServiceImpl.java`

```java
// 修改前
if (user.getStatus() == 0) {
    throw new BusinessException("账号已被删除");
}
if (user.getStatus() == 2) {
    throw new BusinessException("账号已被禁用");
}

// 修改后
if (user.getStatus() == 0) {
    throw new BusinessException("账号已被禁用");
}
```

---

#### 3.2 Feign 接口查询逻辑

**文件**: `UserFeignController.java`

```java
// 修改前：排除 status=0
wrapper.ne(UserEntity::getStatus, 0);

// 修改后：只返回 status=1 的正常用户
wrapper.eq(UserEntity::getStatus, 1);
```

**影响的方法**:
- `getUserById()` - 过滤禁用用户
- `getUserByUsername()` - 只返回正常用户
- `getUserByPhone()` - 只返回正常用户

---

#### 3.3 管理员操作修正

**文件**: `UserAdminController.java`

| 操作 | 修改前 | 修改后 |
|------|--------|--------|
| **禁用用户** | `status = 2` | `status = 0` |
| **删除用户** | `status = 0`（软删除） | 物理删除 `removeById()` |
| **批量删除** | 批量更新 `status = 0` | 物理删除 `removeByIds()` |

```java
// 禁用用户
user.setStatus(0);  // 改为 0

// 删除用户
userService.removeById(id);  // 改为物理删除
```

---

#### 3.4 统计数据修正

**文件**: `UserServiceImpl.java`

```java
// 修改前
totalCount = 总数（排除 status=0）
activeCount = status=1 的数量
disabledCount = status=2 的数量
deletedCount = status=0 的数量

// 修改后
totalCount = 总数（所有用户）
activeCount = status=1 的数量（正常）
disabledCount = status=0 的数量（禁用）
deletedCount = 0（物理删除无法统计）
```

---

### 4. **MyBatis Plus 配置修正**

**文件**: `bootstrap.yml`

```yaml
# 修改前
global-config:
  db-config:
    logic-delete-field: deleted  # ❌ 表中不存在此字段
    logic-delete-value: 1
    logic-not-delete-value: 0

# 修改后
global-config:
  db-config:
    id-type: auto
    # 注释：表结构中 status 字段定义为 1:正常 0:禁用
    # 不是逻辑删除字段，软删除需在业务层实现
```

---

## 📊 影响范围分析

### ✅ 正常工作的功能
- 用户注册（默认 status=1）
- 用户登录（status=1 可登录，status=0 被拦截）
- 获取用户信息
- 修改用户信息
- 修改密码

### ⚠️ 行为变化的功能

| 功能 | 修改前 | 修改后 |
|------|--------|--------|
| **禁用用户** | status=2 | status=0 |
| **删除用户** | status=0（软删除） | 物理删除 |
| **查询已删除用户** | 可查询（status=0） | 无法查询（已删除） |
| **恢复删除用户** | 可以（修改status） | 不可以（数据已删除） |

---

## 🎯 关键变化总结

### 原架构设计（错误理解）
```
状态流转: 正常(1) → 禁用(2) → 删除(0)
删除方式: 软删除
```

### 表结构实际定义（正确理解）
```
状态定义: 正常(1) ⟷ 禁用(0)
删除方式: 物理删除（从数据库移除记录）
```

---

## ⚠️ 注意事项

### 1. **数据不可恢复性**
- **修改前**: 删除用户只是标记（status=0），可以恢复
- **修改后**: 删除用户会从数据库物理删除，**无法恢复**

### 2. **建议方案**
如果业务需要"软删除"功能（已删除用户可恢复），有两个选择：

**方案A**: 修改表结构（推荐）
```sql
ALTER TABLE t_user 
ADD COLUMN `deleted` tinyint(1) DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除';
```

**方案B**: 使用 status 扩展语义（当前方案）
```
0 = 禁用
1 = 正常
2 = 已删除（软删除标记）
```
需要修改 SQL 表注释并更新代码。

---

## 📝 测试建议

### 测试用例

#### 测试1: 禁用用户
```bash
PUT /api/user/admin/{id}/disable
预期: status 改为 0
验证: 用户无法登录
```

#### 测试2: 删除用户
```bash
DELETE /api/user/admin/{id}
预期: 数据库记录被删除
验证: 无法通过任何方式查询到该用户
```

#### 测试3: Feign 接口过滤
```bash
GET /api/user/{id}  # 禁用用户(status=0)
预期: 返回 null
```

#### 测试4: 统计数据
```bash
GET /api/user/admin/statistics
预期: 
- totalCount: 所有用户
- activeCount: status=1 的数量
- disabledCount: status=0 的数量
- deletedCount: 0
```

---

## 📂 修改文件清单

### 配置文件
- ✅ `market-service-core/src/main/resources/bootstrap.yml`

### 业务代码
- ✅ `UserServiceImpl.java` - 登录验证、统计逻辑
- ✅ `UserFeignController.java` - 查询过滤逻辑
- ✅ `UserAdminController.java` - 禁用/删除操作
- ✅ `UserStatisticsVO.java` - 注释更新

---

## ✅ 编译状态

```bash
mvn clean compile -Dmaven.test.skip=true
```

**预期结果**: ✅ 编译成功

---

## 🚀 下一步操作

1. **启动服务测试**
```bash
.\start-infrastructure.ps1  # 启动 Nacos
.\start-light.ps1           # 启动核心服务
```

2. **初始化数据库**
```bash
mysql -u root -p < doc/SQL/init_schema.sql
```

3. **测试关键功能**
- 用户注册
- 用户登录
- 禁用用户（验证 status=0）
- 删除用户（验证物理删除）

---

**修改时间**: 2026-02-14  
**修改人**: Development Team  
**状态**: ✅ 代码已与表结构对齐  
**风险等级**: ⚠️ 中等（删除方式从软删除变为物理删除）

