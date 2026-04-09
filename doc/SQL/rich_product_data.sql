-- ========================================
-- 校园跳蚤市场平台 - 丰富产品数据脚本
-- 版本：v1.0.1
-- 创建日期：2026-01-07
-- 描述：添加更多真实的校园二手商品数据
-- ========================================

-- 首先创建更多用户数据（作为卖家）
USE market_user;

-- 添加更多用户（ID从6开始）
INSERT INTO t_user (id, username, password, nickname, avatar, phone, email, status, create_time, update_time) VALUES
(6, 'xiaoming', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '小明', '/avatar/xiaoming.jpg', '13912345605', 'xiaoming@campus.edu.cn', 1, '2025-12-11 08:20:00', '2025-12-11 08:20:00'),
(7, 'xiaohong', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '小红', '/avatar/xiaohong.jpg', '13912345606', 'xiaohong@campus.edu.cn', 1, '2025-12-12 10:45:00', '2025-12-12 10:45:00'),
(8, 'dawei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '大伟', '/avatar/dawei.jpg', '13912345607', 'dawei@campus.edu.cn', 1, '2025-12-13 15:30:00', '2025-12-13 15:30:00'),
(9, 'xiaoyu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '小雨', '/avatar/xiaoyu.jpg', '13912345608', 'xiaoyu@campus.edu.cn', 1, '2025-12-14 09:15:00', '2025-12-14 09:15:00'),
(10, 'student01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '学生A', '/avatar/student01.jpg', '13912345609', 'student01@campus.edu.cn', 1, '2025-12-15 11:20:00', '2025-12-15 11:20:00'),
(11, 'student02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '学生B', '/avatar/student02.jpg', '13912345610', 'student02@campus.edu.cn', 1, '2025-12-16 14:10:00', '2025-12-16 14:10:00'),
(12, 'graduate01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '研一同学', '/avatar/graduate01.jpg', '13912345611', 'graduate01@campus.edu.cn', 1, '2025-12-17 16:40:00', '2025-12-17 16:40:00'),
(13, 'senior01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '大四学长', '/avatar/senior01.jpg', '13912345612', 'senior01@campus.edu.cn', 1, '2025-12-18 12:25:00', '2025-12-18 12:25:00');

-- 为新用户分配普通用户角色
INSERT INTO t_user_role (user_id, role_id) VALUES
(6, 2), (7, 2), (8, 2), (9, 2), (10, 2), (11, 2), (12, 2), (13, 2);

-- 切换到商品数据库
USE market_product;

-- 添加更多商品分类
INSERT INTO t_category (id, parent_id, name, sort, icon, level) VALUES
(9, 0, '服装鞋包', 5, '/icon/clothing.png', 1),
(10, 0, '美妆护肤', 6, '/icon/beauty.png', 1),
(11, 0, '食品零食', 7, '/icon/food.png', 1),
(12, 1, '平板电脑', 3, '/icon/tablet.png', 2),
(13, 1, '耳机音响', 4, '/icon/audio.png', 2),
(14, 2, '小说文学', 3, '/icon/novel.png', 2),
(15, 3, '宿舍装饰', 1, '/icon/decoration.png', 2),
(16, 4, '健身器材', 1, '/icon/fitness.png', 2);

-- 添加丰富的商品数据
INSERT INTO t_product (seller_id, title, description, price, original_price, category_id, status, view_count, create_time, update_time) VALUES

-- 数码产品
(6, 'iPad Air 4 64G WiFi版 玫瑰金', '2021年购买，9成新，主要用来看剧和做笔记，屏幕无划痕，包装盒配件齐全。赠送钢化膜和保护套。', 2800.00, 4799.00, 12, 1, 189, '2026-01-01 10:30:00', '2026-01-01 10:30:00'),
(7, 'AirPods Pro 2代 降噪耳机', '用了8个月，左右耳机和充电盒功能正常，降噪效果很好，适合上课和自习时佩戴。', 1200.00, 1899.00, 13, 1, 145, '2026-01-02 14:20:00', '2026-01-02 14:20:00'),
(8, '小米笔记本Pro 14 锐龙版', 'AMD R5 5600H，16G+512G，2.5K屏，轻薄便携，适合编程和办公，使用一年半。', 3200.00, 4999.00, 6, 1, 267, '2026-01-03 09:15:00', '2026-01-03 09:15:00'),
(9, 'Apple Watch SE 40mm GPS版', '黑色铝金属表壳，运动型表带，健康监测功能齐全，电池续航良好。', 1400.00, 2199.00, 1, 1, 98, '2026-01-04 11:45:00', '2026-01-04 11:45:00'),
(10, '华为MateBook D14 i5版本', 'Intel i5-1135G7，8G+512G SSD，14英寸全面屏，轻薄设计，学习办公两不误。', 2600.00, 4199.00, 6, 1, 156, '2026-01-05 16:30:00', '2026-01-05 16:30:00'),

-- 图书教材
(6, 'Java从入门到精通（第6版）', '软件工程专业教材，内容详细，适合初学者，书本九成新，无涂改。', 45.00, 79.00, 7, 1, 134, '2026-01-01 15:20:00', '2026-01-01 15:20:00'),
(7, '线性代数及其应用 第5版', '数学系必修教材，David C. Lay著，中英文对照，习题丰富。', 60.00, 99.00, 7, 1, 87, '2026-01-02 08:45:00', '2026-01-02 08:45:00'),
(11, '考研英语真题详解 2019-2023', '张剑黄皮书，包含近5年真题及详细解析，备考必备！', 35.00, 68.00, 8, 1, 203, '2026-01-03 19:30:00', '2026-01-03 19:30:00'),
(12, '机器学习实战：Python版', 'Peter Harrington著，代码实例丰富，AI专业必读，九成新。', 55.00, 89.00, 7, 1, 167, '2026-01-04 13:15:00', '2026-01-04 13:15:00'),
(13, '哈利波特全集（中文版）', '7本全套，人民文学出版社正版，收藏版本，成色很好。', 180.00, 280.00, 14, 1, 256, '2026-01-05 10:20:00', '2026-01-05 10:20:00'),

-- 生活用品
(8, 'MUJI无印良品 桌面收纳盒', '简约设计，白色塑料材质，5格分类，适合收纳文具和小物件。', 25.00, 49.00, 15, 1, 76, '2026-01-01 12:10:00', '2026-01-01 12:10:00'),
(9, '小熊电热水壶 1.7L 快速煮水', '304不锈钢内胆，自动断电，宿舍必备，使用半年，功能完好。', 45.00, 89.00, 3, 1, 89, '2026-01-02 17:25:00', '2026-01-02 17:25:00'),
(10, '网易严选乳胶枕 颈椎保护', '天然乳胶枕头，护颈设计，改善睡眠质量，用了3个月。', 120.00, 199.00, 3, 1, 145, '2026-01-03 20:40:00', '2026-01-03 20:40:00'),
(11, '宜家FINTORP厨房挂钩套装', '白色金属材质，可挂厨具和毛巾，节省空间，几乎全新。', 15.00, 29.00, 3, 1, 67, '2026-01-04 09:30:00', '2026-01-04 09:30:00'),
(6, 'LED护眼台灯 三档调光', '可调节亮度和角度，USB充电，无频闪，保护视力，学习必备。', 80.00, 159.00, 3, 1, 134, '2026-01-05 14:50:00', '2026-01-05 14:50:00'),

-- 运动健身
(7, 'Keep瑜伽垫 10mm加厚防滑', '紫色TPE材质，环保无味，防滑效果好，适合瑜伽和健身训练。', 60.00, 99.00, 16, 1, 98, '2026-01-01 18:20:00', '2026-01-01 18:20:00'),
(8, '李宁篮球鞋 驭帅13代 42码', '黑红配色，实战篮球鞋，耐磨防滑，穿了不到10次，九成新。', 320.00, 499.00, 4, 1, 156, '2026-01-02 11:15:00', '2026-01-02 11:15:00'),
(12, '哑铃套装 可调节重量 20kg', '铁质哑铃片+连接杆，可根据需要调节重量，宿舍健身神器。', 150.00, 299.00, 16, 1, 234, '2026-01-03 15:45:00', '2026-01-03 15:45:00'),
(13, '速干运动T恤 Nike DRI-FIT', 'L码，黑色，透气排汗，适合跑步和健身，洗过几次，很新。', 80.00, 199.00, 4, 1, 76, '2026-01-04 16:20:00', '2026-01-04 16:20:00'),

-- 服装鞋包
(9, 'Adidas三叶草卫衣 经典款', 'M码，灰色连帽卫衣，纯棉材质，版型正，几乎没怎么穿。', 180.00, 399.00, 9, 1, 187, '2026-01-01 13:30:00', '2026-01-01 13:30:00'),
(10, 'JanSport双肩包 经典款 红色', '25L容量，结实耐用，适合上课背书，拉链和肩带都完好。', 120.00, 299.00, 9, 1, 123, '2026-01-02 12:40:00', '2026-01-02 12:40:00'),
(11, 'Converse帆布鞋 All Star 白色', '37码，经典高帮款式，鞋底磨损不大，鞋面稍有泛黄属正常。', 180.00, 399.00, 9, 1, 167, '2026-01-03 14:10:00', '2026-01-03 14:10:00'),
(6, 'Uniqlo优衣库 羽绒服 轻型', 'S码，黑色，轻薄保暖，可折叠收纳，冬天必备，八成新。', 250.00, 399.00, 9, 1, 298, '2026-01-04 17:55:00', '2026-01-04 17:55:00'),

-- 美妆护肤
(7, '兰蔻小黑瓶精华液 30ml', '使用了1/3，正品保证，改善肌肤暗沉，提亮肤色，效果很好。', 280.00, 520.00, 10, 1, 145, '2026-01-01 19:25:00', '2026-01-01 19:25:00'),
(9, 'SK-II护肤面膜套装', '包含神仙水面膜5片+前男友面膜3片，专柜正品，保质期到2026年。', 350.00, 680.00, 10, 1, 234, '2026-01-02 20:15:00', '2026-01-02 20:15:00'),
(11, '完美日记眼影盘 探险家十二色', '几乎全新，只试色过几个颜色，粉质细腻，显色度高。', 45.00, 89.00, 10, 1, 87, '2026-01-03 16:30:00', '2026-01-03 16:30:00'),

-- 食品零食（已售出的商品示例）
(8, '良品铺子坚果大礼包', '混合坚果礼盒装，营养健康，适合学习时补充能量，保质期充足。', 68.00, 128.00, 11, 2, 156, '2026-01-01 11:20:00', '2026-01-02 09:30:00'),
(10, '三只松鼠每日坚果 30包装', '小包装方便携带，营养均衡，还剩25包，因减肥出售。', 120.00, 199.00, 11, 2, 203, '2026-01-02 13:45:00', '2026-01-03 10:15:00'),

-- 下架商品示例
(12, 'Surface Pro 7 i5版本', '二合一笔记本，办公学习利器，Type Cover键盘，因升级设备下架。', 3800.00, 6988.00, 12, 3, 345, '2026-01-01 14:20:00', '2026-01-04 11:20:00'),

-- 最近发布的热门商品
(13, '戴森V8 Fluffy无线吸尘器', '轻量化设计，强劲吸力，适合宿舍清洁，电池续航30分钟，九成新。', 1200.00, 2490.00, 3, 1, 423, '2026-01-06 09:30:00', '2026-01-06 09:30:00'),
(6, 'Switch游戏主机 续航增强版', '红蓝手柄版本，包含底座和Pro手柄，已破解可玩各种游戏。', 1800.00, 2599.00, 1, 1, 567, '2026-01-06 15:20:00', '2026-01-06 15:20:00'),
(7, 'Kindle Paperwhite 4 32G', '护眼阅读器，300ppi高清显示，背光可调，内置几百本电子书。', 600.00, 998.00, 1, 1, 234, '2026-01-06 18:45:00', '2026-01-06 18:45:00'),
(8, '小米空气净化器Pro H', '除甲醛除PM2.5，HEPA滤网，低噪运行，宿舍空气净化专家。', 800.00, 1599.00, 3, 1, 178, '2026-01-06 20:10:00', '2026-01-06 20:10:00');

-- 为新商品添加图片
INSERT INTO t_product_image (product_id, image_url, sort) VALUES
-- iPad Air 4 图片
((SELECT id FROM t_product WHERE title = 'iPad Air 4 64G WiFi版 玫瑰金'), '/product/ipad_air4_1.jpg', 0),
((SELECT id FROM t_product WHERE title = 'iPad Air 4 64G WiFi版 玫瑰金'), '/product/ipad_air4_2.jpg', 1),

-- AirPods Pro 图片
((SELECT id FROM t_product WHERE title = 'AirPods Pro 2代 降噪耳机'), '/product/airpods_pro2.jpg', 0),

-- 小米笔记本 图片
((SELECT id FROM t_product WHERE title = '小米笔记本Pro 14 锐龙版'), '/product/xiaomi_laptop_1.jpg', 0),
((SELECT id FROM t_product WHERE title = '小米笔记本Pro 14 锐龙版'), '/product/xiaomi_laptop_2.jpg', 1),

-- Apple Watch 图片
((SELECT id FROM t_product WHERE title = 'Apple Watch SE 40mm GPS版'), '/product/apple_watch_se.jpg', 0),

-- Java书籍 图片
((SELECT id FROM t_product WHERE title = 'Java从入门到精通（第6版）'), '/product/java_book.jpg', 0),

-- 哈利波特全集 图片
((SELECT id FROM t_product WHERE title = '哈利波特全集（中文版）'), '/product/harry_potter_set.jpg', 0),

-- 戴森吸尘器 图片
((SELECT id FROM t_product WHERE title = '戴森V8 Fluffy无线吸尘器'), '/product/dyson_v8_1.jpg', 0),
((SELECT id FROM t_product WHERE title = '戴森V8 Fluffy无线吸尘器'), '/product/dyson_v8_2.jpg', 1),

-- Switch游戏机 图片
((SELECT id FROM t_product WHERE title = 'Switch游戏主机 续航增强版'), '/product/nintendo_switch_1.jpg', 0),
((SELECT id FROM t_product WHERE title = 'Switch游戏主机 续航增强版'), '/product/nintendo_switch_2.jpg', 1),

-- Kindle 图片
((SELECT id FROM t_product WHERE title = 'Kindle Paperwhite 4 32G'), '/product/kindle_paperwhite.jpg', 0),

-- 运动鞋图片
((SELECT id FROM t_product WHERE title = '李宁篮球鞋 驭帅13代 42码'), '/product/lining_basketball.jpg', 0),

-- 卫衣图片
((SELECT id FROM t_product WHERE title = 'Adidas三叶草卫衣 经典款'), '/product/adidas_hoodie.jpg', 0),

-- 双肩包图片
((SELECT id FROM t_product WHERE title = 'JanSport双肩包 经典款 红色'), '/product/jansport_backpack.jpg', 0);

-- 切换到信用数据库，为新用户添加信用记录
USE market_credit;

INSERT INTO t_credit_score (user_id, score, level, update_time) VALUES
(6, 98, '良好', '2025-12-11 08:20:00'),
(7, 102, '优秀', '2025-12-12 10:45:00'),
(8, 100, '优秀', '2025-12-13 15:30:00'),
(9, 105, '优秀', '2025-12-14 09:15:00'),
(10, 95, '良好', '2025-12-15 11:20:00'),
(11, 103, '优秀', '2025-12-16 14:10:00'),
(12, 108, '优秀', '2025-12-17 16:40:00'),
(13, 110, '优秀', '2025-12-18 12:25:00');

-- 为文件服务添加新的图片文件记录
USE market_file;

INSERT INTO t_file_info (file_name, original_name, file_path, file_size, file_type, uploader_id, create_time) VALUES
('uuid-007-product', 'ipad_air4_1.jpg', '/upload/product/ipad_air4_1.jpg', 245678, 'jpg', 6, '2026-01-01 10:30:00'),
('uuid-008-product', 'ipad_air4_2.jpg', '/upload/product/ipad_air4_2.jpg', 298765, 'jpg', 6, '2026-01-01 10:30:00'),
('uuid-009-product', 'airpods_pro2.jpg', '/upload/product/airpods_pro2.jpg', 156789, 'jpg', 7, '2026-01-02 14:20:00'),
('uuid-010-product', 'xiaomi_laptop_1.jpg', '/upload/product/xiaomi_laptop_1.jpg', 387654, 'jpg', 8, '2026-01-03 09:15:00'),
('uuid-011-product', 'dyson_v8_1.jpg', '/upload/product/dyson_v8_1.jpg', 234567, 'jpg', 13, '2026-01-06 09:30:00'),
('uuid-012-product', 'nintendo_switch_1.jpg', '/upload/product/nintendo_switch_1.jpg', 345678, 'jpg', 6, '2026-01-06 15:20:00'),
('uuid-013-avatar', 'xiaoming.jpg', '/upload/avatar/xiaoming.jpg', 45678, 'jpg', 6, '2025-12-11 08:20:00'),
('uuid-014-avatar', 'xiaohong.jpg', '/upload/avatar/xiaohong.jpg', 52341, 'jpg', 7, '2025-12-12 10:45:00');

-- ========================================
-- 数据导入完成
-- 现在数据库中有：
-- - 13个用户（包括管理员）
-- - 40+个商品（涵盖各个分类）
-- - 不同状态的商品（在售、已售出、已下架）
-- - 真实的价格和描述
-- - 完整的关联关系
-- ========================================
