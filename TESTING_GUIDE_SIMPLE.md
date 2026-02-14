# UserFeign接口测试指南（简化版）

## ✅ 已完成的工作

1. **创建了 Feign 接口** (`UserFeignClient.java`)
   - 根据ID获取用户
   - 根据用户名获取用户
   - 根据手机号获取用户

2. **实现了服务端接口** (`UserFeignController.java`)
   - 提供内部服务调用的端点
   - **使用status=0作为删除标记**，查询时自动过滤

3. **编写了测试** 
   - ✅ 单元测试（Mock测试）- 8个测试已通过
   - ⚠️ 集成测试（需要服务运行）

---

## 🚀 快速测试方法

### 方法1：运行 Mock 单元测试（推荐，最简单）

```powershell
# 进入项目目录
cd D:\program\Market

# 运行单元测试
mvn test -pl market-api/market-api-user -Dtest=UserFeignClientTest
```

**优点**：
- ⚡ 快速，不需要启动任何服务
- ✅ 已通过所有8个测试
- 🎯 验证接口逻辑正确

---

### 方法2：通过API手动测试（需要启动服务）

#### Step 1: 启动服务
```powershell
# 启动 Nacos
.\start-infrastructure.ps1

# 启动核心服务
.\start-light.ps1
```

#### Step 2: 准备测试数据

在MySQL中执行：
```sql
USE market;

-- 插入测试用户（status=1 表示正常）
INSERT INTO t_user (username, password, nickname, phone, email, status, create_time, update_time) 
VALUES 
('testUser', '$2a$10$xxxxx', '测试用户', '13800138000', 'test@example.com', 1, NOW(), NOW()),
('demoUser', '$2a$10$xxxxx', '演示用户', '13900139000', 'demo@example.com', 1, NOW(), NOW());

-- 插入已删除用户（status=0 表示已删除）
INSERT INTO t_user (username, password, nickname, phone, email, status, create_time, update_time) 
VALUES 
('deletedUser', '$2a$10$xxxxx', '已删除用户', '15800000000', 'deleted@example.com', 0, NOW(), NOW());
```

#### Step 3: 使用 curl 测试

```powershell
# 测试1: 获取正常用户（应该返回用户信息）
curl http://localhost:9000/api/user/1

# 测试2: 通过用户名获取
curl "http://localhost:9000/api/user/username?username=testUser"

# 测试3: 通过手机号获取
curl "http://localhost:9000/api/user/phone?phone=13800138000"

# 测试4: 获取已删除用户（status=0，应该返回null）
curl http://localhost:9000/api/user/999
```

---

## 📋 关键点说明

### Status 字段含义（按您的要求）
- **status = 0**: 已删除（软删除标记）
- **status = 1**: 正常用户
- **status ≠ 0**: 其他状态（禁用等）

### UserFeignController 的查询逻辑
```java
// 所有查询都会过滤 status=0 的记录
wrapper.ne(UserEntity::getStatus, 0); // 排除已删除的用户
```

---

## 📂 相关文件位置

### 接口定义
```
market-api/market-api-user/src/main/java/org/shyu/marketapiuser/feign/UserFeignClient.java
```

### 服务实现
```
market-service/market-service-user/src/main/java/org/shyu/marketserviceuser/controller/UserFeignController.java
```

### 单元测试
```
market-api/market-api-user/src/test/java/org/shyu/marketapiuser/feign/UserFeignClientTest.java
```

### 详细测试指南
```
USER_FEIGN_TEST_GUIDE.md
```

---

## ❓ 常见问题

**Q: IDE显示"无法解析符号'Test'"？**  
A: 在IntelliJ IDEA中，右键项目 → Maven → Reload Project

**Q: 单元测试能否验证完整功能？**  
A: 单元测试使用Mock方式，可以验证接口逻辑，但不能测试真实的数据库交互。要完整测试需要启动服务并使用手动API测试。

**Q: status=0的用户为什么查不到？**  
A: 这是按您的要求设计的。`UserFeignController`在所有查询中都会自动过滤`status=0`的记录，保证删除的用户不会被返回。

---

## 🎯 推荐测试流程

1. **首先运行单元测试** - 验证逻辑正确性
   ```powershell
   mvn test -pl market-api/market-api-user -Dtest=UserFeignClientTest
   ```

2. **然后手动API测试** - 验证实际运行效果（可选）
   - 启动服务
   - 准备测试数据
   - 使用curl或Postman测试

---

**祝测试顺利！** 🎉

