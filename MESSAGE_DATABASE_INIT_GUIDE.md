# 🔧 Message服务数据库初始化 - 紧急修复

## 问题描述
Message服务启动报错：
- ❌ **Table 'market_message.t_conversation' doesn't exist** - 会话表不存在
- ❌ **Unknown column 'message_type' in 'field list'** - 聊天消息表结构不完整

## 原因
Message服务的数据库 `market_message` 尚未初始化，需要执行SQL脚本创建表结构。

---

## 🚀 快速修复步骤

### 方式1: 使用Navicat/DBeaver等数据库工具（推荐）

1. **打开数据库管理工具** (Navicat / DBeaver / phpMyAdmin 等)

2. **连接到MySQL服务器**
   - Host: `localhost`
   - Port: `3306`
   - Username: `root`
   - Password: `123456`

3. **执行SQL脚本**
   - 打开文件: `market-service/market-service-message/src/main/resources/sql/market_message.sql`
   - 或直接复制下面的SQL内容
   - 执行整个SQL脚本

4. **验证**
   - 查看是否创建了数据库 `market_message`
   - 查看是否创建了表 `t_chat_message` 和 `t_conversation`

### 方式2: 使用MySQL命令行

```powershell
# 进入项目目录
cd D:\program\Market

# 使用MySQL命令行执行（需要修改为你的MySQL安装路径）
# 示例路径，请根据实际安装位置修改：
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -uroot -p123456 < .\market-service\market-service-message\src\main\resources\sql\market_message.sql
```

### 方式3: 在Docker中执行（如果MySQL在Docker中）

```powershell
# 查找MySQL容器名称
docker ps | Select-String mysql

# 执行SQL（替换container_name为实际容器名）
docker exec -i container_name mysql -uroot -p123456 < .\market-service\market-service-message\src\main\resources\sql\market_message.sql
```

---

## 📄 SQL脚本内容

**文件位置**: `market-service/market-service-message/src/main/resources/sql/market_message.sql`

```sql
-- 创建消息服务数据库
CREATE DATABASE IF NOT EXISTS `market_message` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `market_message`;

-- 聊天消息表
DROP TABLE IF EXISTS `t_chat_message`;
CREATE TABLE `t_chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者用户ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者用户ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `message_type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型: 1-文字 2-图片 3-系统通知',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
    `product_id` BIGINT DEFAULT NULL COMMENT '关联商品ID(可选)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
    KEY `idx_receiver_read` (`receiver_id`, `is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 会话表
DROP TABLE IF EXISTS `t_conversation`;
CREATE TABLE `t_conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user1_id` BIGINT NOT NULL COMMENT '用户1 ID',
    `user2_id` BIGINT NOT NULL COMMENT '用户2 ID',
    `last_message` VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息内容',
    `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
    `user1_unread_count` INT NOT NULL DEFAULT 0 COMMENT '用户1未读消息数',
    `user2_unread_count` INT NOT NULL DEFAULT 0 COMMENT '用户2未读消息数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users` (`user1_id`, `user2_id`),
    KEY `idx_user1` (`user1_id`),
    KEY `idx_user2` (`user2_id`),
    KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- 插入测试数据
INSERT INTO `t_chat_message` (`sender_id`, `receiver_id`, `content`, `message_type`, `is_read`, `product_id`, `create_time`)
VALUES
(6, 7, '你好，我对你发布的iPhone13感兴趣，还在吗？', 1, 1, 1, '2026-02-15 10:00:00'),
(7, 6, '你好！在的，手机成色很好，有意向可以面交看货', 1, 1, 1, '2026-02-15 10:01:00'),
(6, 7, '好的，能便宜点吗？', 1, 1, 1, '2026-02-15 10:02:00'),
(7, 6, '已经是最低价了，可以加我微信详聊', 1, 0, 1, '2026-02-15 10:03:00');

INSERT INTO `t_conversation` (`user1_id`, `user2_id`, `last_message`, `last_message_time`, `user1_unread_count`, `user2_unread_count`)
VALUES
(6, 7, '已经是最低价了，可以加我微信详聊', '2026-02-15 10:03:00', 1, 0);
```

---

## ✅ 验证数据库初始化成功

执行SQL后，在数据库工具中验证：

### 1. 检查数据库
```sql
SHOW DATABASES LIKE 'market_message';
```
应该看到 `market_message` 数据库

### 2. 检查表
```sql
USE market_message;
SHOW TABLES;
```
应该看到：
- `t_chat_message`
- `t_conversation`

### 3. 检查表结构
```sql
-- 聊天消息表结构
DESC t_chat_message;

-- 会话表结构
DESC t_conversation;
```

确保 `t_chat_message` 表包含以下字段：
- ✅ `id`
- ✅ `sender_id`
- ✅ `receiver_id`
- ✅ `content`
- ✅ **`message_type`** ← 这个很重要！
- ✅ `is_read`
- ✅ `product_id`
- ✅ `create_time`
- ✅ `status`

### 4. 检查测试数据
```sql
SELECT COUNT(*) FROM t_chat_message;    -- 应该返回 4
SELECT COUNT(*) FROM t_conversation;     -- 应该返回 1
```

---

## 🔄 重启Message服务

数据库初始化完成后，**重启Message服务**：

1. **停止** Message服务 (`MarketServiceMessageApplication`)
2. **重新启动**
3. 查看启动日志，应该不再有表不存在的错误

---

## 🧪 测试消息功能

1. 打开浏览器 → http://localhost:5173
2. 登录账号
3. 进入商品详情页
4. 点击"联系卖家"
5. **应该能正常跳转到消息页面并加载会话列表** ✅

---

## 📊 数据库架构说明

### market_message 数据库

包含2张核心表：

#### 1. t_chat_message - 聊天消息表
存储所有的聊天消息记录

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 消息ID（主键） |
| sender_id | BIGINT | 发送者用户ID |
| receiver_id | BIGINT | 接收者用户ID |
| content | TEXT | 消息内容 |
| **message_type** | TINYINT | 消息类型 (1-文字 2-图片) |
| is_read | TINYINT | 是否已读 (0-未读 1-已读) |
| product_id | BIGINT | 关联商品ID（可选） |
| create_time | DATETIME | 创建时间 |
| status | TINYINT | 删除标记 (0-未删除) |

#### 2. t_conversation - 会话表
存储用户之间的会话信息（一对一聊天）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 会话ID（主键） |
| user1_id | BIGINT | 用户1 ID |
| user2_id | BIGINT | 用户2 ID |
| last_message | VARCHAR(500) | 最后一条消息 |
| last_message_time | DATETIME | 最后消息时间 |
| user1_unread_count | INT | 用户1未读数 |
| user2_unread_count | INT | 用户2未读数 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| status | TINYINT | 删除标记 |

---

## ⚠️ 注意事项

1. **唯一约束**: `t_conversation` 表对 `(user1_id, user2_id)` 有唯一约束，确保两个用户之间只有一个会话
2. **测试数据**: SQL脚本包含用户6和7之间的测试会话数据
3. **字符编码**: 数据库使用 `utf8mb4` 编码，支持emoji表情
4. **索引优化**: 已对常用查询字段添加索引

---

## 🎯 常见问题

### Q: 执行SQL后仍然报错？
**A**: 确保Message服务已重启，旧的连接池可能还在使用旧的配置

### Q: 测试数据中的用户ID 6和7是什么？
**A**: 这是示例数据，用于测试。你可以删除或修改为实际用户ID

### Q: 如何清空测试数据？
**A**: 
```sql
USE market_message;
DELETE FROM t_chat_message;
DELETE FROM t_conversation;
```

---

**执行完SQL后，请重启Message服务！** 🔄

