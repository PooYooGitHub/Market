# 🚨 Message服务数据库初始化 - 快速修复

## 问题
```
❌ Table 'market_message.t_conversation' doesn't exist
❌ Unknown column 'message_type' in 'field list'
```

## 原因
**Message服务的数据库还没有初始化！**

---

## ⚡ 快速修复（3步）

### 1️⃣ 打开数据库管理工具
- Navicat / DBeaver / phpMyAdmin / MySQL Workbench
- 连接到MySQL: `localhost:3306`, 用户: `root`, 密码: `123456`

### 2️⃣ 执行SQL脚本
**方式A - 打开文件**:
- 找到文件: `D:\program\Market\market-service\market-service-message\src\main\resources\sql\market_message.sql`
- 在数据库工具中打开并执行

**方式B - 复制粘贴**:
```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `market_message` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `market_message`;

-- 创建聊天消息表
DROP TABLE IF EXISTS `t_chat_message`;
CREATE TABLE `t_chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `sender_id` BIGINT NOT NULL,
    `receiver_id` BIGINT NOT NULL,
    `content` TEXT NOT NULL,
    `message_type` TINYINT NOT NULL DEFAULT 1,
    `is_read` TINYINT NOT NULL DEFAULT 0,
    `product_id` BIGINT DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
    KEY `idx_receiver_read` (`receiver_id`, `is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建会话表
DROP TABLE IF EXISTS `t_conversation`;
CREATE TABLE `t_conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user1_id` BIGINT NOT NULL,
    `user2_id` BIGINT NOT NULL,
    `last_message` VARCHAR(500) DEFAULT NULL,
    `last_message_time` DATETIME DEFAULT NULL,
    `user1_unread_count` INT NOT NULL DEFAULT 0,
    `user2_unread_count` INT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `status` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users` (`user1_id`, `user2_id`),
    KEY `idx_user1` (`user1_id`),
    KEY `idx_user2` (`user2_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 3️⃣ 重启Message服务
- 在IDEA中停止 `MarketServiceMessageApplication`
- 重新启动

---

## ✅ 验证
执行SQL后运行：
```sql
USE market_message;
SHOW TABLES;  -- 应该看到 t_chat_message 和 t_conversation
```

---

## 📄 详细文档
完整说明请查看: `MESSAGE_DATABASE_INIT_GUIDE.md`

