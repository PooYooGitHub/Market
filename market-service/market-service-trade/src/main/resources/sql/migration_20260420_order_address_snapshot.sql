-- 订单收货地址快照迁移脚本
-- 执行库：market_trade

ALTER TABLE `t_order`
    ADD COLUMN IF NOT EXISTS `address_id` BIGINT DEFAULT NULL COMMENT '收货地址ID' AFTER `product_id`,
    ADD COLUMN IF NOT EXISTS `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '收货人姓名(快照)' AFTER `address_id`,
    ADD COLUMN IF NOT EXISTS `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收货人手机号(快照)' AFTER `receiver_name`,
    ADD COLUMN IF NOT EXISTS `receiver_province` VARCHAR(50) DEFAULT NULL COMMENT '收货省份(快照)' AFTER `receiver_phone`,
    ADD COLUMN IF NOT EXISTS `receiver_city` VARCHAR(50) DEFAULT NULL COMMENT '收货城市(快照)' AFTER `receiver_province`,
    ADD COLUMN IF NOT EXISTS `receiver_district` VARCHAR(50) DEFAULT NULL COMMENT '收货区县(快照)' AFTER `receiver_city`,
    ADD COLUMN IF NOT EXISTS `receiver_detail_address` VARCHAR(255) DEFAULT NULL COMMENT '收货详细地址(快照)' AFTER `receiver_district`,
    ADD COLUMN IF NOT EXISTS `receiver_postal_code` VARCHAR(20) DEFAULT NULL COMMENT '收货邮编(快照)' AFTER `receiver_detail_address`;
