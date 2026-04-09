/*
 * 简化的数据库表结构修复脚本
 */

USE `market_product`;

-- 添加t_category表缺失字段
ALTER TABLE `t_category` ADD COLUMN `status` tinyint(4) DEFAULT 1 COMMENT '状态 1:启用 0:禁用';
ALTER TABLE `t_category` ADD COLUMN `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `t_category` ADD COLUMN `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 添加t_product表软删除字段
ALTER TABLE `t_product` ADD COLUMN `deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0:未删除 1:已删除';