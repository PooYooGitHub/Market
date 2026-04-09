-- =====================================================
-- 信用服务数据库初始化脚本
-- 数据库：market_credit
-- 功能：创建信用评价相关的表结构和初始数据
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `market_credit`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `market_credit`;

-- =====================================================
-- 1. 信用分表
-- =====================================================
DROP TABLE IF EXISTS `t_credit_score`;
CREATE TABLE `t_credit_score` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `score` int(11) DEFAULT 100 COMMENT '信用分',
  `level` varchar(20) DEFAULT '一般' COMMENT '信用等级',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_score` (`score`),
  KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信用分表';

-- =====================================================
-- 2. 评价表
-- =====================================================
DROP TABLE IF EXISTS `t_evaluation`;
CREATE TABLE `t_evaluation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `evaluator_id` bigint(20) NOT NULL COMMENT '评价人ID',
  `target_id` bigint(20) NOT NULL COMMENT '被评价人ID',
  `score` tinyint(4) DEFAULT 5 COMMENT '评分 1-5分',
  `comment` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_evaluator` (`order_id`, `evaluator_id`),
  KEY `idx_target_id` (`target_id`),
  KEY `idx_evaluator_id` (`evaluator_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_score` (`score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户评价表';

-- =====================================================
-- 3. 信用变更记录表（新增）
-- =====================================================
DROP TABLE IF EXISTS `t_credit_log`;
CREATE TABLE `t_credit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `change_type` varchar(50) NOT NULL COMMENT '变更类型：EVALUATION(评价)，PENALTY(惩罚)，REWARD(奖励)',
  `score_change` int(11) NOT NULL COMMENT '分数变化，正数为增加，负数为减少',
  `before_score` int(11) NOT NULL COMMENT '变更前分数',
  `after_score` int(11) NOT NULL COMMENT '变更后分数',
  `related_id` bigint(20) DEFAULT NULL COMMENT '关联ID（如评价ID、订单ID等）',
  `reason` varchar(200) DEFAULT NULL COMMENT '变更原因描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_change_type` (`change_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用分变更日志表';

-- =====================================================
-- 4. 插入测试数据
-- =====================================================

-- 插入信用分测试数据
INSERT INTO `t_credit_score` (`user_id`, `score`, `level`) VALUES
(1, 100, '一般'),
(2, 120, '优秀'),
(3, 85, '良好'),
(4, 65, '一般'),
(5, 45, '较差');

-- 插入评价测试数据
INSERT INTO `t_evaluation` (`order_id`, `evaluator_id`, `target_id`, `score`, `comment`) VALUES
(1, 1, 2, 5, '非常好的卖家，商品描述准确，发货及时！'),
(2, 2, 1, 4, '买家很好沟通，付款及时'),
(3, 3, 2, 5, '商品质量很好，卖家服务态度佳'),
(4, 2, 3, 3, '商品一般，但卖家态度还行'),
(5, 4, 2, 5, '五星好评！下次还会找这个卖家'),
(6, 2, 4, 2, '买家比较挑剔，但最终交易完成了');

-- 插入信用变更记录测试数据
INSERT INTO `t_credit_log` (`user_id`, `change_type`, `score_change`, `before_score`, `after_score`, `related_id`, `reason`) VALUES
(2, 'EVALUATION', 10, 110, 120, 1, '收到5星好评'),
(1, 'EVALUATION', 5, 95, 100, 2, '收到4星好评'),
(2, 'EVALUATION', 10, 110, 120, 3, '收到5星好评'),
(3, 'EVALUATION', -5, 90, 85, 4, '收到3星评价'),
(2, 'EVALUATION', 10, 110, 120, 5, '收到5星好评'),
(4, 'EVALUATION', -15, 60, 45, 6, '收到2星差评');

-- =====================================================
-- 5. 创建视图和存储过程（可选）
-- =====================================================

-- 用户信用统计视图
CREATE OR REPLACE VIEW `v_user_credit_stats` AS
SELECT
    cs.user_id,
    cs.score,
    cs.level,
    COALESCE(COUNT(e.id), 0) as total_evaluations,
    COALESCE(AVG(e.score), 0) as avg_score,
    COALESCE(SUM(CASE WHEN e.score >= 4 THEN 1 ELSE 0 END), 0) as good_count,
    COALESCE(ROUND(SUM(CASE WHEN e.score >= 4 THEN 1 ELSE 0 END) * 100.0 / COUNT(e.id), 2), 0) as good_rate,
    cs.update_time
FROM t_credit_score cs
LEFT JOIN t_evaluation e ON cs.user_id = e.target_id
GROUP BY cs.user_id, cs.score, cs.level, cs.update_time;

-- =====================================================
-- 6. 创建索引优化
-- =====================================================

-- 为评价表创建复合索引，优化常用查询
CREATE INDEX `idx_target_score_time` ON `t_evaluation` (`target_id`, `score`, `create_time`);
CREATE INDEX `idx_evaluator_time` ON `t_evaluation` (`evaluator_id`, `create_time`);

-- 为信用分表创建索引
CREATE INDEX `idx_score_level` ON `t_credit_score` (`score`, `level`);

-- 为信用日志表创建复合索引
CREATE INDEX `idx_user_type_time` ON `t_credit_log` (`user_id`, `change_type`, `create_time`);

-- =====================================================
-- 7. 数据完整性检查
-- =====================================================

-- 检查表是否创建成功
SELECT
    TABLE_NAME,
    TABLE_COMMENT,
    TABLE_ROWS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'market_credit'
ORDER BY TABLE_NAME;

-- 检查测试数据是否插入成功
SELECT '信用分表数据' as table_name, COUNT(*) as count FROM t_credit_score
UNION ALL
SELECT '评价表数据', COUNT(*) FROM t_evaluation
UNION ALL
SELECT '信用日志数据', COUNT(*) FROM t_credit_log;

-- =====================================================
-- 结束脚本
-- =====================================================
SELECT '信用服务数据库初始化完成！' as result;