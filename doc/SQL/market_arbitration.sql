/*
 Navicat Premium Dump SQL

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80408 (8.4.8)
 Source Host           : localhost:3306
 Source Schema         : market_arbitration

 Target Server Type    : MySQL
 Target Server Version : 80408 (8.4.8)
 File Encoding         : 65001

 Date: 14/04/2026 10:24:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_arbitration
-- ----------------------------
DROP TABLE IF EXISTS `t_arbitration`;
CREATE TABLE `t_arbitration`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `respondent_id` bigint NOT NULL COMMENT '被申诉人ID',
  `reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仲裁原因',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '详细描述',
  `evidence` json NULL COMMENT '证据图片(JSON数组)',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态 0:待处理 1:处理中 2:已完结 3:已驳回',
  `result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '裁决结果',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理管理员ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仲裁申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_arbitration_copy1
-- ----------------------------
DROP TABLE IF EXISTS `t_arbitration_copy1`;
CREATE TABLE `t_arbitration_copy1`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `respondent_id` bigint NOT NULL COMMENT '被申诉人ID',
  `reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仲裁原因',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '详细描述',
  `evidence` json NULL COMMENT '证据图片(JSON数组)',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态 0:待处理 1:处理中 2:已完结 3:已驳回',
  `result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '裁决结果',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理管理员ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仲裁申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_arbitration_evidence_submission
-- ----------------------------
DROP TABLE IF EXISTS `t_arbitration_evidence_submission`;
CREATE TABLE `t_arbitration_evidence_submission`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `arbitration_id` bigint NOT NULL COMMENT '仲裁ID',
  `supplement_request_id` bigint NULL DEFAULT NULL COMMENT '关联补证请求ID，可空',
  `submitter_id` bigint NOT NULL COMMENT '提交人ID',
  `submitter_role` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提交人角色 BUYER/SELLER/SYSTEM',
  `claim_text` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主张',
  `fact_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '事实描述',
  `evidence_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '证据URL数组(JSON)',
  `note` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `is_late` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否逾期提交',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_submission_arbitration`(`arbitration_id` ASC) USING BTREE,
  INDEX `idx_submission_request`(`supplement_request_id` ASC) USING BTREE,
  INDEX `idx_submission_role`(`submitter_role` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仲裁证据提交表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_arbitration_log
-- ----------------------------
DROP TABLE IF EXISTS `t_arbitration_log`;
CREATE TABLE `t_arbitration_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `arbitration_id` bigint NOT NULL COMMENT '仲裁ID',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arbitration_id`(`arbitration_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仲裁记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_arbitration_supplement_request
-- ----------------------------
DROP TABLE IF EXISTS `t_arbitration_supplement_request`;
CREATE TABLE `t_arbitration_supplement_request`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `arbitration_id` bigint NOT NULL COMMENT '仲裁ID',
  `requested_by` bigint NOT NULL COMMENT '发起补证的管理员ID',
  `target_party` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '补证对象 BUYER/SELLER/BOTH',
  `required_items` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '补证要求',
  `remark` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '补证备注',
  `due_time` datetime NOT NULL COMMENT '补证截止时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待补证 1已满足 2已超时 3已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_supplement_arbitration`(`arbitration_id` ASC) USING BTREE,
  INDEX `idx_supplement_status_due`(`status` ASC, `due_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仲裁补证请求表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
