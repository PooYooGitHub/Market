-- 创建支付记录表
CREATE TABLE `tb_payment` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `payment_no` VARCHAR(64) NOT NULL COMMENT '支付流水号（系统生成）',
  `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（支付人）',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `payment_method` TINYINT NOT NULL COMMENT '支付方式：1-支付宝，2-微信支付，3-余额支付，4-银行卡',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款',
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
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- 插入测试数据（可选）
INSERT INTO `tb_payment`
(`id`, `payment_no`, `order_id`, `order_no`, `user_id`, `amount`, `payment_method`, `status`, `description`, `third_party_no`, `paid_time`)
VALUES
(1, 'PAY20261204100001123456', 1, 'ORD20261204100001123456', 1, 100.00, 1, 1, '商品购买支付 - ORD20261204100001123456', 'ALI20261204100001123456', '2026-12-04 10:00:01'),
(2, 'PAY20261204100002123456', 2, 'ORD20261204100002123456', 2, 50.00, 2, 1, '商品购买支付 - ORD20261204100002123456', 'WX20261204100002123456', '2026-12-04 10:00:02');