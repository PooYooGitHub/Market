# 🚨 紧急：Message服务数据库未初始化

## 现在需要做什么

### 1. 打开数据库工具
Navicat / DBeaver / MySQL Workbench / phpMyAdmin

连接: `localhost:3306`, 用户: `root`, 密码: `123456`

### 2. 执行SQL
**打开这个文件**:
```
D:\program\Market\market-service\market-service-message\src\main\resources\sql\market_message.sql
```

**或复制执行**:
```sql
CREATE DATABASE IF NOT EXISTS `market_message` DEFAULT CHARACTER SET utf8mb4;
USE `market_message`;

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
    KEY `idx_sender_receiver` (`sender_id`, `receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
    UNIQUE KEY `uk_users` (`user1_id`, `user2_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 3. 重启Message服务
在IDEA中重启 `MarketServiceMessageApplication`

### 4. 测试
点击商品详情页的"联系卖家" → 应该正常跳转到消息页面

---

**详细说明**: 查看 `MESSAGE_FIX_SUMMARY.md`

