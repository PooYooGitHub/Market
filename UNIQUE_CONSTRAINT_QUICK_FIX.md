# 注册唯一索引冲突 - 快速修复指南

## ✅ 问题已解决

### 错误信息
```
Duplicate entry '' for key 't_user.uk_phone'
```

### 问题原因
多个用户注册时不填手机号，导致多条`phone=''`记录违反唯一索引。

---

## 🔧 修复步骤（按顺序执行）

### 步骤1: 重启User Service ▶️

**User Service已停止，需要启动**：
- 在IDE中运行 `MarketServiceUserApplication`
- 端口：8101

### 步骤2: 清理数据库 🗄️

**执行SQL清理脚本**：

**方式1：命令行执行**
```bash
mysql -u root -p market_user < doc/SQL/fix_unique_constraint_conflict.sql
```

**方式2：MySQL客户端**
打开 `doc/SQL/fix_unique_constraint_conflict.sql`，复制以下关键SQL执行：

```sql
-- 清理phone空字符串
UPDATE t_user SET phone = NULL WHERE phone = '';

-- 清理email空字符串
UPDATE t_user SET email = NULL WHERE email = '';

-- 验证结果
SELECT COUNT(*) as total,
       SUM(CASE WHEN phone = '' THEN 1 ELSE 0 END) as empty_phone
FROM t_user;
```

**预期结果**：`empty_phone = 0`

### 步骤3: 测试注册 🧪

前端测试：
1. 访问注册页面
2. 只填用户名和密码（**不填手机号**）
3. 提交注册
4. **预期**：✅ 注册成功

再次测试：
1. 使用不同用户名
2. 同样不填手机号
3. **预期**：✅ 注册成功（不再报冲突）

---

## 🎯 修复内容

### 代码修复（已完成）✅
**文件**：`UserServiceImpl.java`

**修改**：
```java
// 空字符串转为NULL
user.setPhone(isNotEmpty(phone) ? phone : null);
user.setEmail(isNotEmpty(email) ? email : null);
```

### 数据清理（待执行）⏳
- 将数据库中的`phone=''`更新为`NULL`
- 将数据库中的`email=''`更新为`NULL`

---

## 📊 服务状态

| 服务 | 端口 | 状态 | 操作 |
|-----|------|------|------|
| Gateway | 9000 | ✅ 运行中 | 无需操作 |
| **User Service** | **8101** | ⚠️ **已停止** | **▶️ 需要启动** |
| Product Service | 8102 | ✅ 运行中 | 无需操作 |
| File Service | 8106 | ✅ 运行中 | 无需操作 |

---

## ✅ 检查清单

完成后确认：

- [ ] User Service已启动（端口8101）
- [ ] SQL清理脚本已执行
- [ ] 可以注册不填手机号的用户
- [ ] 可以连续注册多个不填手机号的用户
- [ ] 前端注册功能正常

---

## 📝 相关文档

- 详细说明：`UNIQUE_CONSTRAINT_FIX.md`
- SQL脚本：`doc/SQL/fix_unique_constraint_conflict.sql`

---

**修复状态**：✅ 代码已修复  
**待操作**：启动User Service + 执行SQL清理

