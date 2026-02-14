# UserFeignClient 接口测试指南

## 📋 目录
1. [单元测试（Mock测试）](#单元测试)
2. [集成测试（真实调用）](#集成测试)
3. [手动API测试](#手动api测试)
4. [测试数据准备](#测试数据准备)

---

## 1. 单元测试（Mock测试）

### 测试文件位置
```
market-api/market-api-user/src/test/java/org/shyu/marketapiuser/feign/UserFeignClientTest.java
```

### 运行单元测试

**方式1：使用 Maven 命令**
```powershell
# 在项目根目录运行
cd D:\program\Market
mvn test -pl market-api/market-api-user
```

**方式2：在 IntelliJ IDEA 中**
1. 打开 `UserFeignClientTest.java`
2. 右键点击类名或测试方法
3. 选择 "Run 'UserFeignClientTest'"

### 测试覆盖内容
- ✅ 根据ID获取正常用户 (status=1)
- ✅ 根据ID获取已删除用户 (status=0) - 返回null
- ✅ 根据ID获取不存在用户 - 返回null
- ✅ 根据用户名获取用户
- ✅ 根据手机号获取用户
- ✅ 多次调用验证

### 优点
- ⚡ 快速，不需要启动服务
- 🔒 隔离性好，不依赖外部服务
- 🎯 可以精确控制测试场景

---

## 2. 集成测试（真实调用）

### 前置条件

#### Step 1: 启动基础设施
```powershell
# 启动 Nacos
.\start-infrastructure.ps1
```

#### Step 2: 准备测试数据
```sql
-- 在 MySQL 中执行
USE market;

-- 插入测试用户（正常状态）
INSERT INTO t_user (id, username, password, nickname, phone, email, status, create_time, update_time) 
VALUES 
(1, 'testUser', '$2a$10$xxxxx', '测试用户', '13800138000', 'test@example.com', 1, NOW(), NOW()),
(2, 'demoUser', '$2a$10$xxxxx', '演示用户', '13900139000', 'demo@example.com', 1, NOW(), NOW());

-- 插入测试用户（已删除）
INSERT INTO t_user (id, username, password, nickname, phone, email, status, create_time, update_time) 
VALUES 
(999, 'deletedUser', '$2a$10$xxxxx', '已删除用户', '15800000000', 'deleted@example.com', 0, NOW(), NOW());
```

#### Step 3: 启动核心服务
```powershell
# 启动 market-service-core（包含用户服务）
.\start-light.ps1
```

#### Step 4: 验证服务注册
打开浏览器访问：http://localhost:8848/nacos
- 用户名：nacos
- 密码：nacos
- 检查 `market-service-core` 服务是否已注册

### 运行集成测试

1. 修改 `UserFeignClientIntegrationTest.java`，取消注释：
```java
@SpringBootTest
@EnableFeignClients(basePackages = "org.shyu.marketapiuser.feign")
class UserFeignClientIntegrationTest {
```

2. 运行测试：
```powershell
mvn test -pl market-api/market-api-user -Dtest=UserFeignClientIntegrationTest
```

---

## 3. 手动API测试

### 使用 cURL 测试

#### 测试1: 根据ID获取用户
```powershell
curl http://localhost:9000/api/user/1
```

**预期响应**:
```json
{
  "id": 1,
  "username": "testUser",
  "nickname": "测试用户",
  "phone": "13800138000",
  "email": "test@example.com",
  "status": 1,
  "createTime": "2026-02-14T10:00:00",
  "updateTime": "2026-02-14T10:00:00"
}
```

#### 测试2: 根据用户名获取用户
```powershell
curl "http://localhost:9000/api/user/username?username=testUser"
```

#### 测试3: 根据手机号获取用户
```powershell
curl "http://localhost:9000/api/user/phone?phone=13800138000"
```

#### 测试4: 获取已删除用户（应返回null或404）
```powershell
curl http://localhost:9000/api/user/999
```

### 使用 Postman 测试

#### 创建测试集合
1. 打开 Postman
2. 创建新的 Collection: "Market User API"

#### 测试用例1: 获取用户（通过ID）
- **Method**: GET
- **URL**: `http://localhost:9000/api/user/1`
- **Tests**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("User ID matches", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.eql(1);
});

pm.test("User is not deleted", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.status).to.not.eql(0);
});
```

#### 测试用例2: 获取用户（通过用户名）
- **Method**: GET
- **URL**: `http://localhost:9000/api/user/username?username=testUser`
- **Tests**:
```javascript
pm.test("Username matches", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.username).to.eql("testUser");
});
```

#### 测试用例3: 获取用户（通过手机号）
- **Method**: GET
- **URL**: `http://localhost:9000/api/user/phone?phone=13800138000`
- **Tests**:
```javascript
pm.test("Phone matches", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.phone).to.eql("13800138000");
});
```

---

## 4. 测试数据准备

### 快速初始化脚本

创建 `init-test-data.sql`:
```sql
-- 清理旧数据
DELETE FROM t_user WHERE id IN (1, 2, 999);

-- 插入测试数据
INSERT INTO t_user (id, username, password, nickname, phone, email, status, create_time, update_time) VALUES
(1, 'testUser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户', '13800138000', 'test@example.com', 1, NOW(), NOW()),
(2, 'demoUser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '演示用户', '13900139000', 'demo@example.com', 1, NOW(), NOW()),
(999, 'deletedUser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '已删除用户', '15800000000', 'deleted@example.com', 0, NOW(), NOW());

-- 验证数据
SELECT id, username, nickname, phone, status FROM t_user WHERE id IN (1, 2, 999);
```

执行脚本：
```powershell
mysql -u root -p market < init-test-data.sql
```

### Status 字段说明
- `status = 0`: 用户已删除（软删除标记）
- `status = 1`: 正常用户
- `status = 2`: 冻结/禁用用户（如需要）

---

## 5. 测试检查清单

### ✅ 功能测试
- [ ] 能够根据ID获取正常用户
- [ ] 能够根据用户名获取用户
- [ ] 能够根据手机号获取用户
- [ ] 已删除用户（status=0）不会被返回
- [ ] 不存在的用户返回 null
- [ ] 返回的数据不包含密码等敏感信息

### ✅ 性能测试
- [ ] 单次查询响应时间 < 200ms
- [ ] 并发10个请求能正常处理
- [ ] 内存占用稳定

### ✅ 异常测试
- [ ] 传入null参数的处理
- [ ] 传入非法ID（如负数）的处理
- [ ] 服务不可用时的降级处理

---

## 6. 常见问题排查

### 问题1: 服务无法连接
**症状**: `java.net.ConnectException: Connection refused`

**解决**:
```powershell
# 检查服务是否启动
netstat -ano | findstr :9000

# 检查 Nacos 注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=market-service-core
```

### 问题2: 返回 null
**可能原因**:
1. 数据库中没有对应数据
2. 用户 status=0（已删除）
3. 查询条件不匹配

**检查**:
```sql
-- 检查数据库
SELECT * FROM t_user WHERE id = 1;
SELECT * FROM t_user WHERE username = 'testUser';
```

### 问题3: Feign 调用超时
**解决**: 调整超时配置
```yaml
# application.yml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
```

---

## 7. 最佳实践

### 测试顺序
1. ✅ **先运行单元测试** - 快速验证逻辑
2. ✅ **再运行集成测试** - 验证服务间调用
3. ✅ **最后手动测试** - 验证实际场景

### 持续集成
```powershell
# 添加到 CI 流程
mvn clean test
```

### 测试数据隔离
- 使用固定的测试ID范围（如 1-1000）
- 测试前清理，测试后恢复
- 不要在生产环境运行测试

---

## 8. 快速测试命令总结

```powershell
# 1. 运行单元测试
mvn test -pl market-api/market-api-user -Dtest=UserFeignClientTest

# 2. 启动服务
.\start-infrastructure.ps1
.\start-light.ps1

# 3. 快速测试
curl http://localhost:9000/api/user/1
curl "http://localhost:9000/api/user/username?username=testUser"
curl "http://localhost:9000/api/user/phone?phone=13800138000"

# 4. 查看日志
Get-Content .\logs\market-service-core.log -Tail 50
```

---

## 📞 需要帮助？
如果测试过程中遇到问题，请检查：
1. 📝 日志文件: `logs/market-service-core.log`
2. 🔍 Nacos 控制台: http://localhost:8848/nacos
3. 💾 数据库数据是否正确

---

**文档版本**: 1.0  
**最后更新**: 2026-02-14

