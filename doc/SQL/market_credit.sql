SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `t_credit_log`;
DROP TABLE IF EXISTS `t_evaluation`;
DROP TABLE IF EXISTS `t_credit_score`;

CREATE TABLE `t_credit_score` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `score` int(11) NOT NULL DEFAULT 550,
  `level` varchar(32) NOT NULL DEFAULT '成长中',
  `badge_code` varchar(32) NOT NULL DEFAULT 'ROOKIE',
  `badge_name` varchar(32) NOT NULL DEFAULT '新手认证',
  `badge_color` varchar(16) NOT NULL DEFAULT '#C0C4CC',
  `badge_desc` varchar(255) NOT NULL DEFAULT '交易次数较少，建议先小额交易',
  `high_trust` tinyint(1) NOT NULL DEFAULT 0,
  `valid_trade_count` int(11) NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_evaluation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `evaluator_id` bigint(20) NOT NULL,
  `target_id` bigint(20) NOT NULL,
  `score` tinyint(4) NOT NULL,
  `content` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_evaluator` (`order_id`, `evaluator_id`),
  KEY `idx_target_id` (`target_id`),
  KEY `idx_evaluator_id` (`evaluator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_credit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `change_type` varchar(32) NOT NULL,
  `raw_score_change` int(11) NOT NULL DEFAULT 0,
  `score_change` int(11) NOT NULL DEFAULT 0,
  `before_score` int(11) NOT NULL,
  `after_score` int(11) NOT NULL,
  `related_id` bigint(20) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `event_key` varchar(128) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_key` (`event_key`),
  KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
