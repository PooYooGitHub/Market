-- =========================================
-- 争议协商 + 仲裁执行闭环（MySQL 8+）
-- =========================================

CREATE TABLE IF NOT EXISTS `t_dispute_request` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint DEFAULT NULL,
  `buyer_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  `reason` varchar(64) NOT NULL,
  `fact_description` varchar(2000) NOT NULL,
  `request_type` varchar(64) NOT NULL,
  `request_description` varchar(2000) NOT NULL,
  `expected_amount` decimal(12,2) DEFAULT NULL,
  `status` varchar(64) NOT NULL,
  `current_round` int NOT NULL DEFAULT 1,
  `seller_response_type` varchar(64) DEFAULT NULL,
  `seller_response_description` varchar(2000) DEFAULT NULL,
  `seller_response_proposal_type` varchar(64) DEFAULT NULL,
  `seller_response_amount` decimal(12,2) DEFAULT NULL,
  `seller_response_freight_bearer` varchar(32) DEFAULT NULL,
  `escalated_arbitration_id` bigint DEFAULT NULL,
  `final_decision_type` varchar(64) DEFAULT NULL,
  `final_execution_status` varchar(64) DEFAULT NULL,
  `final_result_description` varchar(2000) DEFAULT NULL,
  `expire_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dispute_order` (`order_id`),
  KEY `idx_dispute_buyer` (`buyer_id`),
  KEY `idx_dispute_seller` (`seller_id`),
  KEY `idx_dispute_status` (`status`),
  KEY `idx_dispute_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `t_dispute_evidence` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `biz_type` varchar(32) NOT NULL COMMENT 'DISPUTE / ARBITRATION',
  `biz_id` bigint NOT NULL,
  `uploader_id` bigint NOT NULL,
  `uploader_role` varchar(32) NOT NULL COMMENT 'BUYER / SELLER / SYSTEM / ADMIN',
  `evidence_type` varchar(32) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `file_url` varchar(1024) NOT NULL,
  `thumbnail_url` varchar(1024) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dispute_evidence_biz` (`biz_type`,`biz_id`),
  KEY `idx_dispute_evidence_uploader` (`uploader_id`,`uploader_role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `t_dispute_negotiation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dispute_id` bigint NOT NULL,
  `round_no` int NOT NULL DEFAULT 1,
  `actor_id` bigint NOT NULL,
  `actor_role` varchar(32) NOT NULL,
  `action_type` varchar(64) NOT NULL,
  `content` varchar(4000) DEFAULT NULL,
  `amount` decimal(12,2) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dispute_log_dispute` (`dispute_id`),
  KEY `idx_dispute_log_action` (`action_type`),
  KEY `idx_dispute_log_create` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `t_arbitration_execution_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `arbitration_id` bigint NOT NULL,
  `dispute_id` bigint DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `execution_type` varchar(64) NOT NULL,
  `execution_status` varchar(64) NOT NULL,
  `payload` text,
  `result_message` varchar(2000) DEFAULT NULL,
  `retry_count` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `finish_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_execution_task_arb` (`arbitration_id`),
  KEY `idx_execution_task_dispute` (`dispute_id`),
  KEY `idx_execution_task_status` (`execution_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================
-- 增量变更（幂等）
-- =========================================

ALTER TABLE `t_arbitration`
    ADD COLUMN `
` varchar(64) DEFAULT NULL COMMENT '裁决类型',
    ADD COLUMN `execution_type` varchar(64) DEFAULT NULL COMMENT '执行类型',
    ADD COLUMN `execution_status` varchar(64) DEFAULT NULL COMMENT '执行状态',
    ADD COLUMN `execution_remark` varchar(2000) DEFAULT NULL COMMENT '执行备注',
    ADD COLUMN `execution_payload` text COMMENT '执行载荷(JSON)',
    ADD COLUMN `decide_time` datetime DEFAULT NULL COMMENT '裁决时间',
    ADD COLUMN `execute_time` datetime DEFAULT NULL COMMENT '执行完成时间';
