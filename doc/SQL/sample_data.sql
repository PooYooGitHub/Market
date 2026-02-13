-- ========================================
-- 校园跳蚤市场平台 - 样例数据脚本
-- 版本：v1.0.0
-- 创建日期：2026-01-07
-- ========================================

-- ========================================
-- 1. 用户服务数据库 (market_user)
-- ========================================

USE market_user;

-- 1.1 用户表数据 (密码统一为: 123456，BCrypt加密后)
INSERT INTO t_user (id, username, password, nickname, avatar, phone, email, status, create_time, update_time) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '超级管理员', '/avatar/admin.jpg', '13800138000', 'admin@campus.edu.cn', 1, '2025-12-01 10:00:00', '2025-12-01 10:00:00'),
(2, 'zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '张三', '/avatar/zhangsan.jpg', '13912345601', 'zhangsan@campus.edu.cn', 1, '2025-12-05 09:30:00', '2025-12-05 09:30:00'),
(3, 'lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '李四', '/avatar/lisi.jpg', '13912345602', 'lisi@campus.edu.cn', 1, '2025-12-06 14:20:00', '2025-12-06 14:20:00'),
(4, 'wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '王五', '/avatar/wangwu.jpg', '13912345603', 'wangwu@campus.edu.cn', 1, '2025-12-08 16:45:00', '2025-12-08 16:45:00'),
(5, 'zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '赵六', '/avatar/zhaoliu.jpg', '13912345604', 'zhaoliu@campus.edu.cn', 1, '2025-12-10 11:15:00', '2025-12-10 11:15:00');

-- 1.2 角色表数据
INSERT INTO t_role (id, role_name, role_code, description, create_time) VALUES
(1, '管理员', 'ROLE_ADMIN', '系统管理员，拥有所有权限', '2025-12-01 10:00:00'),
(2, '普通用户', 'ROLE_USER', '普通用户，可以买卖商品', '2025-12-01 10:00:00');

-- 1.3 用户角色关联表数据
INSERT INTO t_user_role (id, user_id, role_id) VALUES
(1, 1, 1),  -- admin是管理员
(2, 2, 2),  -- zhangsan是普通用户
(3, 3, 2),  -- lisi是普通用户
(4, 4, 2),  -- wangwu是普通用户
(5, 5, 2);  -- zhaoliu是普通用户

-- ========================================
-- 2. 商品服务数据库 (market_product)
-- ========================================

USE market_product;

-- 2.1 商品分类表数据
INSERT INTO t_category (id, parent_id, name, sort, icon, level) VALUES
(1, 0, '数码产品', 1, '/icon/digital.png', 1),
(2, 0, '图书教材', 2, '/icon/book.png', 1),
(3, 0, '生活用品', 3, '/icon/daily.png', 1),
(4, 0, '运动健身', 4, '/icon/sports.png', 1),
(5, 1, '手机', 1, '/icon/phone.png', 2),
(6, 1, '电脑', 2, '/icon/computer.png', 2),
(7, 2, '专业书籍', 1, '/icon/professional.png', 2),
(8, 2, '考试资料', 2, '/icon/exam.png', 2);

-- 2.2 商品表数据
INSERT INTO t_product (id, seller_id, title, description, price, original_price, category_id, status, view_count, create_time, update_time) VALUES
(1, 2, 'iPhone 13 128G 95成新', '用了半年，无磕碰，功能完好，配件齐全。因换新机出售。', 3800.00, 5999.00, 5, 1, 156, '2025-12-15 10:30:00', '2025-12-15 10:30:00'),
(2, 2, '数据结构与算法分析（第3版）', '计算机专业经典教材，9成新，无笔记勾画。', 35.00, 59.00, 7, 1, 89, '2025-12-16 14:20:00', '2025-12-16 14:20:00'),
(3, 3, 'MacBook Air M1 8G 256G', '2021款，8成新，使用正常，自带充电器。适合日常办公学习。', 5200.00, 7999.00, 6, 1, 234, '2025-12-17 09:15:00', '2025-12-17 09:15:00'),
(4, 3, '全新羽毛球拍套装', '李宁N90，全新未开封，去年双11买的一直没用。', 180.00, 299.00, 4, 1, 67, '2025-12-18 16:40:00', '2025-12-18 16:40:00'),
(5, 4, '护眼台灯', '飞利浦品牌，用了一年，功能正常。搬宿舍不方便带走。', 80.00, 199.00, 3, 1, 45, '2025-12-19 11:25:00', '2025-12-19 11:25:00'),
(6, 4, '高等数学考研资料', '考研真题+笔记，内容齐全，上岸必备！', 50.00, 0.00, 8, 1, 123, '2025-12-20 13:50:00', '2025-12-20 13:50:00'),
(7, 5, '小米手环6 NFC版', '95成新，功能完好，可刷校园卡。', 120.00, 229.00, 1, 1, 98, '2025-12-21 10:10:00', '2025-12-21 10:10:00'),
(8, 5, '宿舍小风扇', '美的品牌，7成新，夏天必备神器。', 25.00, 59.00, 3, 2, 56, '2025-12-22 15:30:00', '2025-12-28 09:20:00');

-- 2.3 商品图片表数据
INSERT INTO t_product_image (id, product_id, image_url, sort) VALUES
(1, 1, '/product/iphone13_1.jpg', 0),
(2, 1, '/product/iphone13_2.jpg', 1),
(3, 1, '/product/iphone13_3.jpg', 2),
(4, 2, '/product/book_datastructure.jpg', 0),
(5, 3, '/product/macbook_1.jpg', 0),
(6, 3, '/product/macbook_2.jpg', 1),
(7, 4, '/product/badminton.jpg', 0),
(8, 5, '/product/lamp.jpg', 0),
(9, 6, '/product/math_exam.jpg', 0),
(10, 7, '/product/miband.jpg', 0),
(11, 8, '/product/fan.jpg', 0);

-- ========================================
-- 3. 交易服务数据库 (market_trade)
-- ========================================

USE market_trade;

-- 3.1 购物车表数据
INSERT INTO t_cart (id, user_id, product_id, quantity, selected, create_time, update_time) VALUES
(1, 3, 2, 1, 1, '2025-12-25 10:20:00', '2025-12-25 10:20:00'),
(2, 5, 5, 1, 1, '2025-12-26 14:35:00', '2025-12-26 14:35:00');

-- 3.2 订单表数据
INSERT INTO t_order (id, order_no, buyer_id, seller_id, product_id, total_amount, status, create_time, pay_time, update_time) VALUES
(1, 'ORD20251228001', 3, 2, 1, 3800.00, 3, '2025-12-28 10:00:00', '2025-12-28 10:05:00', '2025-12-30 16:20:00'),
(2, 'ORD20251229001', 4, 3, 4, 180.00, 1, '2025-12-29 11:20:00', '2025-12-29 11:25:00', '2025-12-29 11:25:00'),
(3, 'ORD20251230001', 5, 4, 6, 50.00, 3, '2025-12-30 09:15:00', '2025-12-30 09:18:00', '2026-01-02 14:30:00'),
(4, 'ORD20260102001', 2, 5, 8, 25.00, 2, '2026-01-02 15:40:00', '2026-01-02 15:42:00', '2026-01-03 10:00:00');

-- 3.3 订单日志表数据
INSERT INTO t_order_log (id, order_id, status, operator, remark, create_time) VALUES
(1, 1, 0, 'lisi', '创建订单', '2025-12-28 10:00:00'),
(2, 1, 1, 'lisi', '支付成功', '2025-12-28 10:05:00'),
(3, 1, 2, 'zhangsan', '卖家已发货', '2025-12-29 14:30:00'),
(4, 1, 3, 'lisi', '买家确认收货', '2025-12-30 16:20:00'),
(5, 2, 0, 'wangwu', '创建订单', '2025-12-29 11:20:00'),
(6, 2, 1, 'wangwu', '支付成功', '2025-12-29 11:25:00'),
(7, 3, 0, 'zhaoliu', '创建订单', '2025-12-30 09:15:00'),
(8, 3, 1, 'zhaoliu', '支付成功', '2025-12-30 09:18:00'),
(9, 3, 2, 'wangwu', '卖家已发货', '2025-12-31 10:00:00'),
(10, 3, 3, 'zhaoliu', '买家确认收货', '2026-01-02 14:30:00');

-- ========================================
-- 4. 消息服务数据库 (market_message)
-- ========================================

USE market_message;

-- 4.1 聊天消息表数据
INSERT INTO t_chat_message (id, sender_id, receiver_id, content, type, read_status, create_time) VALUES
(1, 3, 2, '你好，iPhone 13还在吗？', 0, 1, '2025-12-27 15:20:00'),
(2, 2, 3, '在的，功能完好，你可以来看看实物。', 0, 1, '2025-12-27 15:22:00'),
(3, 3, 2, '好的，能优惠一点吗？', 0, 1, '2025-12-27 15:25:00'),
(4, 2, 3, '最低3800，真心出售。', 0, 1, '2025-12-27 15:27:00'),
(5, 4, 3, '羽毛球拍还有吗？', 0, 1, '2025-12-29 10:30:00'),
(6, 3, 4, '有的，全新的，可以面交。', 0, 1, '2025-12-29 10:35:00');

-- 4.2 系统通知表数据
INSERT INTO t_notification (id, user_id, title, content, type, read_status, create_time) VALUES
(1, 2, '订单已完成', '您的订单ORD20251228001已确认收货，交易完成。', 1, 1, '2025-12-30 16:20:00'),
(2, 3, '订单支付成功', '您已成功支付订单ORD20251228001，请等待卖家发货。', 1, 1, '2025-12-28 10:05:00'),
(3, 3, '商品已发货', '您购买的商品已发货，请注意查收。', 1, 1, '2025-12-29 14:30:00'),
(4, 5, '欢迎加入', '欢迎来到校园跳蚤市场平台！', 0, 1, '2025-12-10 11:15:00');

-- ========================================
-- 5. 信用服务数据库 (market_credit)
-- ========================================

USE market_credit;

-- 5.1 信用分表数据
INSERT INTO t_credit_score (id, user_id, score, level, update_time) VALUES
(1, 1, 100, '优秀', '2025-12-01 10:00:00'),
(2, 2, 105, '优秀', '2025-12-30 16:30:00'),
(3, 3, 102, '优秀', '2025-12-30 16:30:00'),
(4, 4, 100, '优秀', '2025-12-10 11:15:00'),
(5, 5, 103, '优秀', '2026-01-02 14:40:00');

-- 5.2 评价表数据
INSERT INTO t_evaluation (id, order_id, evaluator_id, target_id, score, content, create_time) VALUES
(1, 1, 3, 2, 5, '卖家很好，手机成色不错，值得购买！', '2025-12-30 16:25:00'),
(2, 1, 2, 3, 5, '买家爽快，交易顺利！', '2025-12-30 16:30:00'),
(3, 3, 5, 4, 5, '资料很全，对考研很有帮助，感谢！', '2026-01-02 14:35:00'),
(4, 3, 4, 5, 5, '好买家，沟通愉快。', '2026-01-02 14:40:00');

-- ========================================
-- 6. 仲裁服务数据库 (market_arbitration)
-- ========================================

USE market_arbitration;

-- 6.1 仲裁申请表数据（示例：暂无仲裁记录，可根据需要添加）
-- INSERT INTO t_arbitration (id, order_id, applicant_id, respondent_id, reason, description, evidence, status, result, handler_id, create_time) VALUES
-- (1, 2, 4, 3, '商品质量问题', '收到的羽毛球拍与描述不符，怀疑不是全新。', '["evidence1.jpg","evidence2.jpg"]', 0, NULL, NULL, '2026-01-05 10:00:00');

-- 6.2 仲裁记录表数据
-- INSERT INTO t_arbitration_log (id, arbitration_id, operator_id, action, remark, create_time) VALUES
-- (1, 1, 4, '提交仲裁', '申请仲裁', '2026-01-05 10:00:00');

-- ========================================
-- 7. 文件服务数据库 (market_file)
-- ========================================

USE market_file;

-- 7.1 文件信息表数据
INSERT INTO t_file_info (id, file_name, original_name, file_path, file_size, file_type, uploader_id, create_time) VALUES
(1, 'uuid-001-avatar', 'admin.jpg', '/upload/avatar/admin.jpg', 45678, 'jpg', 1, '2025-12-01 10:00:00'),
(2, 'uuid-002-avatar', 'zhangsan.jpg', '/upload/avatar/zhangsan.jpg', 52341, 'jpg', 2, '2025-12-05 09:30:00'),
(3, 'uuid-003-product', 'iphone13_1.jpg', '/upload/product/iphone13_1.jpg', 234567, 'jpg', 2, '2025-12-15 10:30:00'),
(4, 'uuid-004-product', 'iphone13_2.jpg', '/upload/product/iphone13_2.jpg', 198765, 'jpg', 2, '2025-12-15 10:30:00'),
(5, 'uuid-005-product', 'macbook_1.jpg', '/upload/product/macbook_1.jpg', 312456, 'jpg', 3, '2025-12-17 09:15:00'),
(6, 'uuid-006-product', 'badminton.jpg', '/upload/product/badminton.jpg', 145678, 'jpg', 3, '2025-12-18 16:40:00');

-- ========================================
-- 数据导入完成
-- ========================================

