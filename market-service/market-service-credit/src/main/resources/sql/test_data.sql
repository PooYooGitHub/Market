USE `market_credit`;

DELETE FROM `t_credit_log`;
DELETE FROM `t_evaluation`;
DELETE FROM `t_credit_score`;

INSERT INTO `t_credit_score`
(`user_id`, `score`, `level`, `badge_code`, `badge_name`, `badge_color`, `badge_desc`, `high_trust`, `valid_trade_count`)
VALUES
(1, 905, '优秀', 'DIAMOND', '钻石信誉', '#67C23A', '长期稳定高信用用户', 1, 35),
(2, 840, '良好', 'GOLD', '金牌信誉', '#E6A23C', '高可信交易伙伴', 0, 18),
(3, 710, '稳定', 'BRONZE', '铜牌信誉', '#B88230', '信用稳定，持续成长中', 0, 9),
(4, 580, '成长中', 'ROOKIE', '新手认证', '#C0C4CC', '交易次数较少，建议先小额交易', 0, 2);

INSERT INTO `t_evaluation` (`order_id`, `evaluator_id`, `target_id`, `score`, `content`, `create_time`) VALUES
(1001, 2, 1, 5, '交易流程顺畅，响应及时', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1002, 3, 1, 4, '体验不错，值得推荐', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1003, 1, 2, 5, '付款及时，沟通高效', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1004, 4, 2, 3, '中规中矩', DATE_SUB(NOW(), INTERVAL 3 DAY));
