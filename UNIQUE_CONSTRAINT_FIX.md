# 用户注册唯一索引冲突问题修复

## 问题描述

用户注册时出现数据库唯一索引冲突错误：
```
SQLIntegrityConstraintViolationException: Duplicate entry '' for key 't_user.uk_phone'
```

## 问题原因

### 1. 数据库设计
`t_user`表的`phone`字段有唯一索引`uk_phone`：
```sql
UNIQUE KEY `uk_phone` (`phone`)
```

### 2. 数据插入问题
- 前端注册时，如果用户**不填写手机号**
- 后端接收到`phone`字段为**空字符串** `''`
- 直接插入数据库，`phone`值为 `''`
- 第二次注册不填手机号时，再次插入 `phone=''`
- **违反唯一索引约束** → 报错

### 3. 根本原因
- 空字符串 `''` ≠ `NULL`
- 数据库唯一索引对 `''` 视为有值
- 多个 `''` 违反唯一性
- 但多个 `NULL` 不违反（NULL表示"无值"）

---

## 解决方案

### 1. 后端代码修复 ✅

**文件**：`market-service-user/service/impl/UserServiceImpl.java`

**修改内容**：
```java
// 修改前（有问题）
user.setPhone(registerDTO.getPhone());
user.setEmail(registerDTO.getEmail());

// 修改后（正确）
// 将空字符串转换为null，避免违反唯一索引约束
user.setPhone(isNotEmpty(registerDTO.getPhone()) ? registerDTO.getPhone() : null);
user.setEmail(isNotEmpty(registerDTO.getEmail()) ? registerDTO.getEmail() : null);

// 添加辅助方法
private boolean isNotEmpty(String str) {
    return str != null && !str.trim().isEmpty();
}
```

**逻辑**：
- 如果字符串不为空 → 使用原值
- 如果字符串为空/null/空格 → 设置为`null`

### 2. 数据库数据清理 ⚠️

**SQL脚本**：`doc/SQL/fix_unique_constraint_conflict.sql`

**执行步骤**：

#### 步骤1: 检查问题数据
```sql
SELECT id, username, phone, email, create_time 
FROM t_user 
WHERE phone = '';
```

#### 步骤2: 修复数据
```sql
-- 将空字符串更新为NULL
UPDATE t_user 
SET phone = NULL 
WHERE phone = '';

-- 同样处理email
UPDATE t_user 
SET email = NULL 
WHERE email = '';
```

#### 步骤3: 验证结果
```sql
SELECT 
    COUNT(*) as total_users,
    SUM(CASE WHEN phone IS NULL THEN 1 ELSE 0 END) as null_phone_count,
    SUM(CASE WHEN phone = '' THEN 1 ELSE 0 END) as empty_phone_count
FROM t_user;
```

**预期结果**：
- `empty_phone_count` = 0（没有空字符串）
- `null_phone_count` > 0（空值已转为NULL）

---

## 测试验证

### 1. 重启User Service

**原因**：代码已修改，需要重新编译加载

**步骤**：
1. 停止当前运行的User Service
2. 重新运行 `MarketServiceUserApplication`

### 2. 执行SQL清理脚本

**在MySQL中执行**：
```bash
mysql -u root -p market_user < doc/SQL/fix_unique_constraint_conflict.sql
```

或者在MySQL客户端中手动执行SQL。

### 3. 测试注册功能

#### 测试1: 不填手机号注册（应该成功）
```bash
curl -X POST http://localhost:9000/api/user/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser1",
    "password": "Test123456",
    "confirmPassword": "Test123456",
    "nickname": "测试用户1"
  }'
```
**预期**：✅ 200 OK，注册成功

#### 测试2: 再次不填手机号注册（应该成功）
```bash
curl -X POST http://localhost:9000/api/user/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser2",
    "password": "Test123456",
    "confirmPassword": "Test123456",
    "nickname": "测试用户2"
  }'
```
**预期**：✅ 200 OK，注册成功（不再报唯一索引冲突）

#### 测试3: 填写相同手机号（应该失败）
```bash
curl -X POST http://localhost:9000/api/user/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser3",
    "password": "Test123456",
    "confirmPassword": "Test123456",
    "phone": "13800138000"
  }'

# 再次使用相同手机号
curl -X POST http://localhost:9000/api/user/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser4",
    "password": "Test123456",
    "confirmPassword": "Test123456",
    "phone": "13800138000"
  }'
```
**预期**：❌ 第二次应该失败，提示"手机号已被注册"

---

## 数据库字段约束说明

### 唯一索引与NULL的关系

| 场景 | phone值 | 能否插入 | 说明 |
|-----|---------|---------|------|
| 第1条记录 | `NULL` | ✅ 允许 | NULL表示"无值" |
| 第2条记录 | `NULL` | ✅ 允许 | 多个NULL不冲突 |
| 第3条记录 | `''` | ✅ 允许 | 第一个空字符串 |
| 第4条记录 | `''` | ❌ **冲突** | 第二个空字符串违反唯一性 |
| 第5条记录 | `'13800138000'` | ✅ 允许 | 有效手机号 |
| 第6条记录 | `'13800138000'` | ❌ **冲突** | 重复的手机号 |

**关键点**：
- ✅ **NULL可以重复**：多个用户可以都不填手机号（phone=NULL）
- ❌ **空字符串不能重复**：只能有一个phone=''的记录
- ❌ **相同值不能重复**：phone='13800138000'只能有一个

---

## 代码逻辑说明

### 修复前的逻辑
```java
// 前端没填写 → registerDTO.getPhone() = ""
user.setPhone(registerDTO.getPhone());  // 直接设置 ""
// 插入数据库 → phone = ''
```

### 修复后的逻辑
```java
// 前端没填写 → registerDTO.getPhone() = ""
user.setPhone(isNotEmpty("") ? "" : null);  // 判断后设置 null
// 插入数据库 → phone = NULL

// 前端填写了 → registerDTO.getPhone() = "13800138000"
user.setPhone(isNotEmpty("13800138000") ? "13800138000" : null);
// 插入数据库 → phone = '13800138000'
```

### isNotEmpty判断逻辑
```java
private boolean isNotEmpty(String str) {
    return str != null && !str.trim().isEmpty();
}

// 测试用例
isNotEmpty(null)        → false → 设置NULL
isNotEmpty("")          → false → 设置NULL
isNotEmpty("   ")       → false → 设置NULL
isNotEmpty("13800")     → true  → 使用原值
```

---

## 其他可能需要处理的字段

如果以下字段也有唯一索引，建议同样处理：

### email字段
```java
user.setEmail(isNotEmpty(registerDTO.getEmail()) ? registerDTO.getEmail() : null);
```

### 数据清理
```sql
UPDATE t_user SET email = NULL WHERE email = '';
```

---

## 最佳实践建议

### 1. 数据库设计
对于**可选的唯一字段**：
- ✅ 允许NULL：`phone VARCHAR(20) NULL UNIQUE`
- ✅ 默认值NULL：`DEFAULT NULL`
- ❌ 不要默认空字符串：~~`DEFAULT ''`~~

### 2. 后端验证
```java
// 统一的空值处理方法
private String toNullIfEmpty(String str) {
    return (str == null || str.trim().isEmpty()) ? null : str.trim();
}
```

### 3. 前端处理
```javascript
// 注册时，不填的字段不要发送空字符串
const registerData = {
  username: this.username,
  password: this.password,
  ...(this.phone && { phone: this.phone }),  // 只在有值时才添加
  ...(this.email && { email: this.email })
}
```

---

## 检查清单

修复后确认：

- [ ] User Service代码已修改并重启
- [ ] 数据库SQL脚本已执行
- [ ] 数据库中没有phone=''的记录
- [ ] 可以连续注册多个不填手机号的用户
- [ ] 填写相同手机号会被正确拦截
- [ ] 前端注册功能正常工作

---

## 相关文件

- 代码修复：`market-service-user/service/impl/UserServiceImpl.java`
- SQL脚本：`doc/SQL/fix_unique_constraint_conflict.sql`
- 数据库设计：`doc/SQL/market_user.sql`

---

**修复时间**：2026-02-15  
**问题状态**：✅ 代码已修复  
**下一步**：重启User Service + 执行SQL清理

