-- 创建交易服务数据库
CREATE DATABASE IF NOT EXISTS `market_trade` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `market_trade`;

-- 订单表
CREATE TABLE IF NOT EXISTS `t_order` (
    `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no`     VARCHAR(32)    NOT NULL COMMENT '订单编号',
    `buyer_id`     BIGINT         NOT NULL COMMENT '买家ID',
    `seller_id`    BIGINT         NOT NULL COMMENT '卖家ID',
    `product_id`   BIGINT         NOT NULL COMMENT '商品ID',
    `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    `status`       TINYINT        NOT NULL DEFAULT 0 COMMENT '订单状态:0待支付 1已支付 2已发货 3已收货/完成 4已取消 5售后中',
    `create_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间(下单时间)',
    `pay_time`     DATETIME                DEFAULT NULL COMMENT '支付时间',
    `update_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单表';

-- 购物车表
CREATE TABLE IF NOT EXISTS `t_cart` (
    `id`          BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT     NOT NULL COMMENT '用户ID',
    `product_id`  BIGINT     NOT NULL COMMENT '商品ID',
    `quantity`    INT        NOT NULL DEFAULT 1 COMMENT '购买数量',
    `selected`    TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否选中:1是 0否',
    `create_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='购物车表';

-- 订单日志表
CREATE TABLE IF NOT EXISTS `t_order_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id`    BIGINT       NOT NULL COMMENT '订单ID',
    `status`      TINYINT      NOT NULL COMMENT '变更后状态',
    `operator`    VARCHAR(64)           DEFAULT NULL COMMENT '操作人',
    `remark`      VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单日志表';

