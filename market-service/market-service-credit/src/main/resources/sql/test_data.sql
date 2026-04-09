-- 信用评价系统测试数据初始化脚本

USE market_credit;

-- 清空现有数据（如果需要重新初始化）
-- DELETE FROM t_evaluation;
-- DELETE FROM t_credit_score;

-- 初始化用户信用分数据（对应前端显示的用户）
INSERT INTO t_credit_score (user_id, score, level, update_time) VALUES
(1, 120, '优秀', NOW()),
(2, 85, '良好', NOW()),
(3, 65, '一般', NOW()),
(4, 45, '较差', NOW()),
(5, 105, '优秀', NOW());

-- 插入评价测试数据
INSERT INTO t_evaluation (order_id, evaluator_id, target_id, score, content, create_time) VALUES
-- 用户1收到的评价
(1001, 2, 1, 5, '卖家服务态度很好，商品质量excellent！', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1002, 3, 1, 5, '发货速度快，包装仔细，推荐！', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1003, 4, 1, 4, '商品不错，就是价格稍微贵了点', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(1004, 5, 1, 5, '非常满意的一次交易，下次还来！', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1005, 2, 1, 4, '整体满意，物流稍慢', DATE_SUB(NOW(), INTERVAL 15 DAY)),

-- 用户2收到的评价
(2001, 1, 2, 4, '买家付款及时，沟通顺畅', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2002, 3, 2, 5, '很好的买家，推荐！', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2003, 4, 2, 4, '交易愉快，值得信赖', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(2004, 5, 2, 3, '还行吧，一般般', DATE_SUB(NOW(), INTERVAL 12 DAY)),

-- 用户3收到的评价
(3001, 1, 3, 3, '商品描述基本属实，还可以', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3002, 2, 3, 4, '发货及时，商品质量不错', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3003, 4, 3, 2, '商品有点瑕疵，不太满意', DATE_SUB(NOW(), INTERVAL 14 DAY)),

-- 用户4收到的评价
(4001, 1, 4, 2, '商品与描述不符，沟通态度一般', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(4002, 2, 4, 3, '凑合吧，下次不会再买了', DATE_SUB(NOW(), INTERVAL 16 DAY)),

-- 用户5收到的评价
(5001, 1, 5, 5, '超级棒的卖家，商品物超所值！', DATE_SUB(NOW(), INTERVAL 11 DAY)),
(5002, 2, 5, 5, '包装精美，服务一流！', DATE_SUB(NOW(), INTERVAL 13 DAY)),
(5003, 3, 5, 4, '质量很好，价格合理', DATE_SUB(NOW(), INTERVAL 17 DAY));

-- 更新信用分，使其与评价数据匹配
-- 用户1：5条评价，平均4.6分，好评率100%，应该是优秀级别
UPDATE t_credit_score SET score = 120, level = '优秀' WHERE user_id = 1;

-- 用户2：4条评价，平均4.0分，好评率75%，应该是良好级别
UPDATE t_credit_score SET score = 85, level = '良好' WHERE user_id = 2;

-- 用户3：3条评价，平均3.0分，好评率33%，应该是一般级别
UPDATE t_credit_score SET score = 65, level = '一般' WHERE user_id = 3;

-- 用户4：2条评价，平均2.5分，好评率0%，应该是较差级别
UPDATE t_credit_score SET score = 45, level = '较差' WHERE user_id = 4;

-- 用户5：3条评价，平均4.7分，好评率100%，应该是优秀级别
UPDATE t_credit_score SET score = 105, level = '优秀' WHERE user_id = 5;