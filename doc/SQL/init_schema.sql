/*
 * 校园跳蚤市场平台 - 数据库初始化脚本
 * 对应服务：User, Product, Trade, Message, Credit, Arbitration, File
 * 版本：v1.0.0
 * 日期：2026-01-07
 */

-- =========================================================================================
-- 1. 用户服务数据库 (market_user)
-- =========================================================================================
CREATE DATABASE IF NOT EXISTS `market_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `market_user`;

-- 用户表
CREATE TABLE IF NOT EXISTS `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码(加密)',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态 1:正常 0:禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `t_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `t_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';


-- =========================================================================================
-- 2. 商品服务数据库 (market_product)
-- =========================================================================================
CREATE DATABASE IF NOT EXISTS `market_product` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `market_product`;

-- 商品表
CREATE TABLE IF NOT EXISTS `t_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `seller_id` bigint(20) NOT NULL COMMENT '卖家ID',
  `title` varchar(100) NOT NULL COMMENT '商品标题',
  `description` text COMMENT '商品描述',
  `price` decimal(10,2) NOT NULL COMMENT '当前价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态 0:草稿 1:发布 2:已售出 3:下架',
  `view_count` int(11) DEFAULT 0 COMMENT '浏览量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品图片表
CREATE TABLE IF NOT EXISTS `t_product_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `image_url` varchar(255) NOT NULL COMMENT '图片地址',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- 商品分类表
CREATE TABLE IF NOT EXISTS `t_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
  `level` int(11) DEFAULT 1 COMMENT '层级',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';


-- =========================================================================================
-- 3. 交易服务数据库 (market_trade)
-- =========================================================================================
CREATE DATABASE IF NOT EXISTS `market_trade` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `market_trade`;

-- 购物车表
CREATE TABLE IF NOT EXISTS `t_cart` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `quantity` int(11) DEFAULT 1 COMMENT '数量',
  `selected` tinyint(1) DEFAULT 1 COMMENT '是否选中',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 订单表
CREATE TABLE IF NOT EXISTS `t_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单编号',
  `buyer_id` bigint(20) NOT NULL COMMENT '买家ID',
  `seller_id` bigint(20) NOT NULL COMMENT '卖家ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态 0:待支付 1:已支付 2:已发货 3:已收货/完成 4:已取消 5:售后中',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单日志表
CREATE TABLE IF NOT EXISTS `t_order_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `status` tinyint(4) NOT NULL COMMENT '变更后状态',
  `operator` varchar(50) DEFAULT NULL COMMENT '操作人',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单日志表';


-- =========================================================================================
-- 4. 消息服务数据库 (market_message)
-- =========================================================================================
CREATE DATABASE IF NOT EXISTS `market_message` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `market_message`;

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `t_chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者ID',
  `content` text NOT NULL COMMENT '消息内容',
  `type` tinyint(4) DEFAULT 0 COMMENT '消息类型 0:文本 1:图片 2:商品卡片',
  `read_status` tinyint(1) DEFAULT 0 COMMENT '状态 0:未读 1:已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender_receiver` (`sender_id`,`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 系统通知表
CREATE TABLE IF NOT EXISTS `t_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '接收用户ID',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `type` tinyint(4) DEFAULT 0 COMMENT '类型 0:系统通知 1:订单通知 2:仲裁通知',
  `read_status` tinyint(1) DEFAULT 0 COMMENT '状态 0:未读 1:已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '通知时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';


-- =========================================================================================
-- 5. 信用服务数据库 (market_credit)
-- =========================================================================================
CREATE DATABASE IF NOT EXISTS `market_credit` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `market_credit`;

-- 信用分表
CREATE TABLE IF NOT EXISTS `t_credit_score` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `score` int(11) DEFAULT 100 COMMENT '信用分',
  `level` varchar(20) DEFAULT '一般' COMMENT '信用等级',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用分表';

-- 评价表
CREATE TABLE IF NOT EXISTS `t_evaluation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `evaluator_id` bigint(20) NOT NULL COMMENT '评价人ID',
  `target_id` bigint(20) NOT NULL COMMENT '被评价人ID',
  `score` tinyint(4) DEFAULT 5 COMMENT '评分 1-5',
  `content` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  KEY `idx_target_id` (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';


-- =========================================================================================
-- 6. 仲裁服务数据库 (market_arbitration)
-- =========================================================================================
CREATE DATABASE IF NOT EXISTS `market_arbitration` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `market_arbitration`;

-- 仲裁申请表
CREATE TABLE IF NOT EXISTS `t_arbitration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint(20) NOT NULL COMMENT '关联订单ID',
  `applicant_id` bigint(20) NOT NULL COMMENT '申请人ID',
  `respondent_id` bigint(20) NOT NULL COMMENT '被申诉人ID',
  `reason` varchar(50) NOT NULL COMMENT '仲裁原因',
  `description` text COMMENT '详细描述',
  `evidence` json DEFAULT NULL COMMENT '证据图片(JSON数组)',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态 0:待处理 1:处理中 2:已完结 3:已驳回',
  `result` text COMMENT '裁决结果',
  `handler_id` bigint(20) DEFAULT NULL COMMENT '处理管理员ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仲裁申请表';

-- 仲裁记录表
CREATE TABLE IF NOT EXISTS `t_arbitration_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `arbitration_id` bigint(20) NOT NULL COMMENT '仲裁ID',
  `operator_id` bigint(20) NOT NULL COMMENT '操作人ID',
  `action` varchar(50) NOT NULL COMMENT '动作',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_arbitration_id` (`arbitration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仲裁记录表';


-- =========================================================================================
-- 7. 文件服务数据库 (market_file)
-- =========================================================================================
CREATE DATABASE IF NOT EXISTS `market_file` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `market_file`;

-- 文件信息表
CREATE TABLE IF NOT EXISTS `t_file_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `original_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径/URL',
  `file_size` bigint(20) DEFAULT 0 COMMENT '文件大小(字节)',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型/后缀',
  `uploader_id` bigint(20) DEFAULT NULL COMMENT '上传者ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

