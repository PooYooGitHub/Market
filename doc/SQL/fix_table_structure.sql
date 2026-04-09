/*
 * 数据库表结构修复脚本
 * 修复后端代码与数据库表结构不匹配的问题
 * 执行前请先备份数据库！
 */

-- 修复商品服务数据库
USE `market_product`;

-- 1. 修复 t_category 表 - 添加缺失字段（忽略已存在字段的错误）
SET @sql = '';
SELECT COUNT(*) INTO @col_exists FROM information_schema.columns WHERE table_schema = 'market_product' AND table_name = 't_category' AND column_name = 'status';
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `t_category` ADD COLUMN `status` tinyint(4) DEFAULT 1 COMMENT ''状态 1:启用 0:禁用'';', 'SELECT ''Column status already exists'' as info;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @col_exists FROM information_schema.columns WHERE table_schema = 'market_product' AND table_name = 't_category' AND column_name = 'create_time';
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `t_category` ADD COLUMN `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'';', 'SELECT ''Column create_time already exists'' as info;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @col_exists FROM information_schema.columns WHERE table_schema = 'market_product' AND table_name = 't_category' AND column_name = 'update_time';
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `t_category` ADD COLUMN `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'';', 'SELECT ''Column update_time already exists'' as info;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 修复 t_product 表 - 添加软删除字段
SELECT COUNT(*) INTO @col_exists FROM information_schema.columns WHERE table_schema = 'market_product' AND table_name = 't_product' AND column_name = 'deleted';
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `t_product` ADD COLUMN `deleted` tinyint(4) DEFAULT 0 COMMENT ''是否删除 0:未删除 1:已删除'';', 'SELECT ''Column deleted already exists'' as info;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证表结构
SELECT 'Category table structure:' as info;
DESCRIBE `t_category`;

SELECT 'Product table structure:' as info;
DESCRIBE `t_product`;

SELECT 'Fix completed successfully!' as status;