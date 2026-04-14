-- 仲裁补证能力升级脚本
-- 执行时间: 2026-04-13

USE market_arbitration;

-- 1) 仲裁补证请求表
CREATE TABLE IF NOT EXISTS `t_arbitration_supplement_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `arbitration_id` bigint(20) NOT NULL COMMENT '仲裁ID',
  `requested_by` bigint(20) NOT NULL COMMENT '发起补证的管理员ID',
  `target_party` varchar(16) NOT NULL COMMENT '补证对象 BUYER/SELLER/BOTH',
  `required_items` varchar(1000) NOT NULL COMMENT '补证要求',
  `remark` varchar(1000) DEFAULT NULL COMMENT '补证备注',
  `due_time` datetime NOT NULL COMMENT '补证截止时间',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0待补证 1已满足 2已超时 3已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_supplement_arbitration` (`arbitration_id`),
  KEY `idx_supplement_status_due` (`status`, `due_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仲裁补证请求表';

-- 2) 仲裁证据提交表（含初始申请和补证）
CREATE TABLE IF NOT EXISTS `t_arbitration_evidence_submission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `arbitration_id` bigint(20) NOT NULL COMMENT '仲裁ID',
  `supplement_request_id` bigint(20) DEFAULT NULL COMMENT '关联补证请求ID，可空',
  `submitter_id` bigint(20) NOT NULL COMMENT '提交人ID',
  `submitter_role` varchar(16) NOT NULL COMMENT '提交人角色 BUYER/SELLER/SYSTEM',
  `claim_text` varchar(1000) DEFAULT NULL COMMENT '主张',
  `fact_text` text COMMENT '事实描述',
  `evidence_urls` text COMMENT '证据URL数组(JSON)',
  `note` varchar(1000) DEFAULT NULL COMMENT '备注',
  `is_late` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否逾期提交',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_submission_arbitration` (`arbitration_id`),
  KEY `idx_submission_request` (`supplement_request_id`),
  KEY `idx_submission_role` (`submitter_role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仲裁证据提交表';

-- 3) 可选: 为仲裁状态补充注释（应用层新增 4=待补证）
-- ALTER TABLE `t_arbitration` MODIFY COLUMN `status` tinyint(4) DEFAULT 0 COMMENT '状态 0待处理 1处理中 2已完结 3已驳回 4待补证';
