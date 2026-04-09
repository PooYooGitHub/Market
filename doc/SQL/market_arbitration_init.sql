-- =====================================================
-- 仲裁服务数据库初始化脚本
-- 数据库：market_arbitration
-- 功能：创建仲裁相关的表结构和初始数据
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `market_arbitration`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `market_arbitration`;

-- =====================================================
-- 1. 仲裁申请表
-- =====================================================
DROP TABLE IF EXISTS `t_arbitration`;
CREATE TABLE `t_arbitration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint(20) NOT NULL COMMENT '关联订单ID',
  `applicant_id` bigint(20) NOT NULL COMMENT '申请人ID',
  `respondent_id` bigint(20) NOT NULL COMMENT '被申诉人ID',
  `reason` varchar(50) NOT NULL COMMENT '仲裁原因',
  `description` text COMMENT '详细描述',
  `evidence` json DEFAULT NULL COMMENT '证据材料JSON',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态 0:待处理 1:处理中 2:已完结 3:已驳回',
  `result` text COMMENT '裁决结果',
  `handler_id` bigint(20) DEFAULT NULL COMMENT '处理管理员ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_applicant` (`order_id`, `applicant_id`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_respondent_id` (`respondent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_handler_id` (`handler_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_reason` (`reason`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仲裁申请表';

-- =====================================================
-- 2. 仲裁处理日志表
-- =====================================================
DROP TABLE IF EXISTS `t_arbitration_log`;
CREATE TABLE `t_arbitration_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `arbitration_id` bigint(20) NOT NULL COMMENT '仲裁ID',
  `operator_id` bigint(20) NOT NULL COMMENT '操作人ID',
  `action` varchar(50) NOT NULL COMMENT '操作动作',
  `remark` varchar(255) DEFAULT NULL COMMENT '操作备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_arbitration_id` (`arbitration_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仲裁处理日志表';

-- =====================================================
-- 3. 插入测试数据
-- =====================================================

-- 插入仲裁申请测试数据
INSERT INTO `t_arbitration` (`order_id`, `applicant_id`, `respondent_id`, `reason`, `description`, `evidence`, `status`, `result`, `handler_id`) VALUES
(1, 1, 2, 'QUALITY_ISSUE', '收到的商品与描述不符，存在明显质量问题，要求退货退款。', '["http://localhost:9900/evidence/img1.jpg", "http://localhost:9900/evidence/img2.jpg"]', 0, NULL, NULL),
(2, 3, 2, 'SHIPPING_DELAY', '卖家承诺3天内发货，但已超过一周仍未发货，严重影响使用。', '["http://localhost:9900/evidence/chat1.jpg"]', 1, NULL, 1),
(3, 4, 1, 'DESCRIPTION_MISMATCH', '商品实际规格与页面描述完全不符，属于虚假宣传。', '["http://localhost:9900/evidence/compare1.jpg", "http://localhost:9900/evidence/compare2.jpg"]', 2, '经核实，商品确实存在描述不符的问题。裁决：支持买家退货，卖家承担邮费，并扣除信用分10分作为警告。', 1),
(4, 5, 2, 'NO_RESPONSE', '多次联系卖家咨询商品问题，卖家完全不回复消息。', '["http://localhost:9900/evidence/chat2.jpg"]', 3, '经调查，卖家确实存在沟通不及时的问题，但商品本身无质量问题，且买家未在合理时间内退货。驳回仲裁申请。', 1);

-- 插入仲裁日志测试数据
INSERT INTO `t_arbitration_log` (`arbitration_id`, `operator_id`, `action`, `remark`) VALUES
(1, 1, 'SUBMIT', '用户提交仲裁申请'),
(2, 3, 'SUBMIT', '用户提交仲裁申请'),
(2, 1, 'ACCEPT', '管理员受理仲裁申请'),
(2, 1, 'PROCESSING', '开始处理仲裁申请'),
(3, 4, 'SUBMIT', '用户提交仲裁申请'),
(3, 1, 'ACCEPT', '管理员受理仲裁申请'),
(3, 1, 'RESOLVE', '仲裁完结：支持买家'),
(4, 5, 'SUBMIT', '用户提交仲裁申请'),
(4, 1, 'ACCEPT', '管理员受理仲裁申请'),
(4, 1, 'REJECT', '驳回仲裁申请');

-- =====================================================
-- 4. 创建视图和存储过程（可选）
-- =====================================================

-- 仲裁统计视图
CREATE OR REPLACE VIEW `v_arbitration_stats` AS
SELECT
    DATE(create_time) as date,
    COUNT(*) as total_count,
    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as pending_count,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as processing_count,
    SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as completed_count,
    SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as rejected_count
FROM t_arbitration
GROUP BY DATE(create_time)
ORDER BY date DESC;

-- 用户仲裁统计视图
CREATE OR REPLACE VIEW `v_user_arbitration_stats` AS
SELECT
    applicant_id as user_id,
    COUNT(*) as total_applications,
    SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as successful_count,
    SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as rejected_count,
    ROUND(
        SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2
    ) as success_rate
FROM t_arbitration
GROUP BY applicant_id;

-- =====================================================
-- 5. 创建索引优化
-- =====================================================

-- 为查询优化创建复合索引
CREATE INDEX `idx_status_create_time` ON `t_arbitration` (`status`, `create_time`);
CREATE INDEX `idx_applicant_status` ON `t_arbitration` (`applicant_id`, `status`);
CREATE INDEX `idx_respondent_status` ON `t_arbitration` (`respondent_id`, `status`);
CREATE INDEX `idx_handler_status` ON `t_arbitration` (`handler_id`, `status`);

-- 为日志表创建复合索引
CREATE INDEX `idx_arbitration_action_time` ON `t_arbitration_log` (`arbitration_id`, `action`, `create_time`);

-- =====================================================
-- 6. 数据完整性检查
-- =====================================================

-- 检查表是否创建成功
SELECT
    TABLE_NAME,
    TABLE_COMMENT,
    TABLE_ROWS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'market_arbitration'
ORDER BY TABLE_NAME;

-- 检查测试数据是否插入成功
SELECT '仲裁申请表数据' as table_name, COUNT(*) as count FROM t_arbitration
UNION ALL
SELECT '仲裁日志表数据', COUNT(*) FROM t_arbitration_log;

-- =====================================================
-- 结束脚本
-- =====================================================
SELECT '仲裁服务数据库初始化完成！' as result;