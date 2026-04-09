-- ========================================
-- 校园跳蚤市场平台 - 用户数据插入
-- ========================================

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