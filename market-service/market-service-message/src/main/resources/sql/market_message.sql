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
-- 假设用户ID为6和7
INSERT INTO `t_chat_message` (`sender_id`, `receiver_id`, `content`, `message_type`, `is_read`, `product_id`, `create_time`)
VALUES
(6, 7, '你好，我对你发布的iPhone13感兴趣，还在吗？', 1, 1, 1, '2026-02-15 10:00:00'),
(7, 6, '你好！在的，手机成色很好，有意向可以面交看货', 1, 1, 1, '2026-02-15 10:01:00'),
(6, 7, '好的，能便宜点吗？', 1, 1, 1, '2026-02-15 10:02:00'),
(7, 6, '已经是最低价了，可以加我微信详聊', 1, 0, 1, '2026-02-15 10:03:00');

INSERT INTO `t_conversation` (`user1_id`, `user2_id`, `last_message`, `last_message_time`, `user1_unread_count`, `user2_unread_count`)
VALUES
(6, 7, '已经是最低价了，可以加我微信详聊', '2026-02-15 10:03:00', 1, 0);

