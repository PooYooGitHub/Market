# MySQL 连接问题解决方案

## 问题诊断
错误信息：`Access denied for user 'root'@'172.17.0.1' (using password: YES)`

**原因分析**：
1. MySQL 运行在 Docker 容器中
2. `172.17.0.1` 是 Docker 网络地址
3. root 用户没有从 Docker 网络访问的权限

---

## 解决方案

### 方案 1：修改数据库配置密码（已执行）

已将 `bootstrap.yml` 中的密码改为 `000000`

如果您的 MySQL 密码不是 `000000`，请手动修改：
```yaml
# D:\program\Market\market-service\market-service-user\src\main\resources\bootstrap.yml
spring:
  datasource:
    username: root
    password: "您的实际密码"
```

---

### 方案 2：为 Docker MySQL 授权（推荐）

#### Step 1: 进入 MySQL 容器

```bash
docker exec -it <mysql容器名> mysql -uroot -p
```

或者使用 MySQL 客户端：
```bash
mysql -h 127.0.0.1 -P 3306 -uroot -p
```

#### Step 2: 授权 root 用户从任何主机访问

```sql
-- 授权 root 用户从任何主机访问
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '000000' WITH GRANT OPTION;

-- 刷新权限
FLUSH PRIVILEGES;

-- 查看用户权限
SELECT Host, User FROM mysql.user WHERE User='root';
```

**预期结果**：
```
+-------------+------+
| Host        | User |
+-------------+------+
| %           | root |
| localhost   | root |
+-------------+------+
```

---

### 方案 3：创建专用数据库用户（最佳实践）

```sql
-- 创建新用户
CREATE USER 'market'@'%' IDENTIFIED BY 'market123';

-- 授权所有 market_* 数据库
GRANT ALL PRIVILEGES ON market_user.* TO 'market'@'%';
GRANT ALL PRIVILEGES ON market_product.* TO 'market'@'%';
GRANT ALL PRIVILEGES ON market_trade.* TO 'market'@'%';
GRANT ALL PRIVILEGES ON market_message.* TO 'market'@'%';
GRANT ALL PRIVILEGES ON market_credit.* TO 'market'@'%';
GRANT ALL PRIVILEGES ON market_arbitration.* TO 'market'@'%';
GRANT ALL PRIVILEGES ON market_file.* TO 'market'@'%';

-- 刷新权限
FLUSH PRIVILEGES;
```

然后修改配置：
```yaml
spring:
  datasource:
    username: market
    password: market123
```

---

### 方案 4：初始化数据库

确保数据库已创建：

```bash
# PowerShell
mysql -h 127.0.0.1 -P 3306 -uroot -p < D:\program\Market\doc\SQL\init_schema.sql
```

或者手动执行：
```sql
CREATE DATABASE IF NOT EXISTS `market_user` DEFAULT CHARACTER SET utf8mb4;
```

---

## 快速验证

### 1. 测试 MySQL 连接

```bash
# PowerShell
mysql -h 127.0.0.1 -P 3306 -uroot -p
```

输入密码后，如果能登录，说明密码正确。

### 2. 查看数据库列表

```sql
SHOW DATABASES;
```

应该看到 `market_user` 等数据库。

### 3. 重新启动服务

修改配置后，重新运行：
```
Run -> Debug 'MarketServiceUserApplication'
```

---

## 常见错误对照表

| 错误信息 | 原因 | 解决方案 |
|---------|------|---------|
| `Access denied for user 'root'@'172.17.0.1'` | Docker 网络权限 | 执行方案2授权 |
| `Access denied for user 'root'@'localhost'` | 密码错误 | 修改配置文件密码 |
| `Unknown database 'market_user'` | 数据库未创建 | 执行 init_schema.sql |
| `Communications link failure` | MySQL 未启动 | 启动 MySQL 服务 |

---

## 推荐操作顺序

1. ✅ **修改密码**（已完成）
2. ⚠️ **授权 Docker 访问**（执行方案2）
3. ✅ **初始化数据库**（执行 init_schema.sql）
4. ✅ **重新启动服务**

---

**当前状态**：密码已改为 `000000`，如果仍然失败，请执行方案2授权。

