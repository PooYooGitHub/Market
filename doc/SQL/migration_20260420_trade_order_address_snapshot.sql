USE `market_trade`;

ALTER TABLE `t_order`
    ADD COLUMN `address_id` bigint NULL COMMENT '收货地址ID' AFTER `product_id`,
    ADD COLUMN `receiver_name` varchar(50) NULL COMMENT '收货人姓名(快照)' AFTER `address_id`,
    ADD COLUMN `receiver_phone` varchar(20) NULL COMMENT '收货人手机号(快照)' AFTER `receiver_name`,
    ADD COLUMN `receiver_province` varchar(50) NULL COMMENT '收货省份(快照)' AFTER `receiver_phone`,
    ADD COLUMN `receiver_city` varchar(50) NULL COMMENT '收货城市(快照)' AFTER `receiver_province`,
    ADD COLUMN `receiver_district` varchar(50) NULL COMMENT '收货区县(快照)' AFTER `receiver_city`,
    ADD COLUMN `receiver_detail_address` varchar(255) NULL COMMENT '收货详细地址(快照)' AFTER `receiver_district`,
    ADD COLUMN `receiver_postal_code` varchar(20) NULL COMMENT '收货邮编(快照)' AFTER `receiver_detail_address`;

ALTER TABLE `t_order`
    ADD INDEX `idx_address_id` (`address_id`);