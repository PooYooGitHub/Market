/*
 * 完整的数据库表结构修复脚本
 * 修复所有已知的字段缺失问题
 */

USE `market_product`;

-- 修复 t_product 表的所有缺失字段
ALTER TABLE `t_product`
ADD COLUMN IF NOT EXISTS `deleted` tinyint(4) DEFAULT 0 COMMENT '是否删除 0:未删除 1:已删除';

-- 添加 audit_remark 字段（如果不存在）
SET @col_count = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'market_product' AND table_name = 't_product' AND column_name = 'audit_remark');
SET @sql = IF(@col_count = 0, 'ALTER TABLE `t_product` ADD COLUMN `audit_remark` varchar(255) DEFAULT NULL COMMENT ''审核备注'';', 'SELECT ''Column audit_remark already exists'' as info;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 修复 t_category 表的所有缺失字段
SET @col_count = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'market_product' AND table_name = 't_category' AND column_name = 'status');
SET @sql = IF(@col_count = 0, 'ALTER TABLE `t_category` ADD COLUMN `status` tinyint(4) DEFAULT 1 COMMENT ''状态 1:启用 0:禁用'';', 'SELECT ''Column status already exists'' as info;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_count = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'market_product' AND table_name = 't_category' AND column_name = 'create_time');
SET @sql = IF(@col_count = 0, 'ALTER TABLE `t_category` ADD COLUMN `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'';', 'SELECT ''Column create_time already exists'' as info;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_count = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'market_product' AND table_name = 't_category' AND column_name = 'update_time');
SET @sql = IF(@col_count = 0, 'ALTER TABLE `t_category` ADD COLUMN `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'';', 'SELECT ''Column update_time already exists'' as info;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 显示修复后的表结构
SELECT 'Product table structure after fix:' as info;
DESCRIBE `t_product`;

SELECT 'Category table structure after fix:' as info;
DESCRIBE `t_category`;

SELECT 'All database structure fixes completed!' as status;