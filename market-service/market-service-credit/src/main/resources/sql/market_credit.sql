-- =========================================================================================
-- 信用服务数据库表结构 (market_credit)
-- 创建信用分表和评价表
-- =========================================================================================

USE `market_credit`;

-- 信用分表
CREATE TABLE IF NOT EXISTS `t_credit_score` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `score` int(11) DEFAULT 100 COMMENT '信用分',
  `level` varchar(20) DEFAULT '一般' COMMENT '信用等级',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
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
  `comment` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_evaluator` (`order_id`, `evaluator_id`),
  KEY `idx_target_id` (`target_id`),
  KEY `idx_evaluator_id` (`evaluator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 插入一些初始信用等级数据说明
-- 信用分等级对应关系：
-- 0-59: 差
-- 60-79: 一般
-- 80-99: 良好
-- 100-119: 优秀
-- 120-150: 极好