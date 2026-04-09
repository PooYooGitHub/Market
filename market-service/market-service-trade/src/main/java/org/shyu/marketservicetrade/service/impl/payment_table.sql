-- 支付记录表
CREATE TABLE `tb_payment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `payment_no` VARCHAR(64) NOT NULL COMMENT '支付流水号（系统生成）',
    `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（支付人）',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `payment_method` INT NOT NULL DEFAULT '1' COMMENT '支付方式：1-支付宝，2-微信支付，3-余额支付，4-银行卡',
    `status` INT NOT NULL DEFAULT '0' COMMENT '支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '支付描述',
    `third_party_no` VARCHAR(128) DEFAULT NULL COMMENT '第三方支付流水号（模拟）',
    `paid_time` DATETIME DEFAULT NULL COMMENT '支付完成时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_payment_method` (`payment_method`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- 插入一些测试数据（可选）
INSERT INTO `tb_payment` (`payment_no`, `order_id`, `order_no`, `user_id`, `amount`, `payment_method`, `status`, `description`, `third_party_no`, `paid_time`) VALUES
('PAY202604041505001234', 1, 'ORD202604041500001', 1, 99.99, 1, 1, '商品购买支付 - ORD202604041500001', 'ALI202604041505001234', '2024-04-04 15:05:30'),
('PAY202604041510002345', 2, 'ORD202604041505002', 1, 299.99, 2, 1, '商品购买支付 - ORD202604041505002', 'WX202604041510002345', '2024-04-04 15:10:45'),
('PAY202604041515003456', 3, 'ORD202604041510003', 2, 149.50, 3, 1, '商品购买支付 - ORD202604041510003', 'BAL202604041515003456', '2024-04-04 15:15:20'),
('PAY202604041520004567', 4, 'ORD202604041515004', 2, 59.99, 1, 2, '商品购买支付 - ORD202604041515004', NULL, NULL);