/*
 * 管理员账号初始化脚本
 * 创建真实的管理员用户和角色数据
 */

-- 使用用户服务数据库
USE market_user;

-- 清除可能存在的测试数据（可选）
-- DELETE FROM t_user_role WHERE user_id IN (1, 2, 3, 4, 5);
-- DELETE FROM t_user WHERE id IN (1, 2, 3, 4, 5);
-- DELETE FROM t_role WHERE id IN (1, 2);

-- 1. 插入角色数据
INSERT INTO t_role (id, role_name, role_code, description, create_time) VALUES
(1, '管理员', 'ROLE_ADMIN', '系统管理员，拥有所有权限', NOW()),
(2, '普通用户', 'ROLE_USER', '普通用户，可以买卖商品', NOW())
ON DUPLICATE KEY UPDATE
role_name = VALUES(role_name),
description = VALUES(description);

-- 2. 插入管理员用户数据
-- 密码: 123456 (BCrypt加密后的值)
INSERT INTO t_user (id, username, password, nickname, avatar, phone, email, status, create_time, update_time) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '系统管理员', '/avatar/admin.jpg', '13800138000', 'admin@campus.edu.cn', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
password = VALUES(password),
nickname = VALUES(nickname),
status = VALUES(status),
update_time = NOW();

-- 3. 插入一些普通用户数据（用于测试）
INSERT INTO t_user (id, username, password, nickname, avatar, phone, email, status, create_time, update_time) VALUES
(2, 'zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '张三', '/avatar/zhangsan.jpg', '13912345601', 'zhangsan@campus.edu.cn', 1, NOW(), NOW()),
(3, 'lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '李四', '/avatar/lisi.jpg', '13912345602', 'lisi@campus.edu.cn', 1, NOW(), NOW()),
(4, 'wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lIzYbEvKfkGDTQjfS', '王五', '/avatar/wangwu.jpg', '13912345603', 'wangwu@campus.edu.cn', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
password = VALUES(password),
nickname = VALUES(nickname),
status = VALUES(status),
update_time = NOW();

-- 4. 分配角色给用户
INSERT INTO t_user_role (user_id, role_id) VALUES
(1, 1),  -- admin 是管理员
(2, 2),  -- zhangsan 是普通用户
(3, 2),  -- lisi 是普通用户
(4, 2)   -- wangwu 是普通用户
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 5. 验证数据
SELECT 'Users:' as info;
SELECT id, username, nickname, status, create_time FROM t_user;

SELECT 'Roles:' as info;
SELECT id, role_name, role_code, description FROM t_role;

SELECT 'User-Role Assignments:' as info;
SELECT ur.user_id, u.username, r.role_code, r.role_name
FROM t_user_role ur
LEFT JOIN t_user u ON ur.user_id = u.id
LEFT JOIN t_role r ON ur.role_id = r.id;

-- 显示创建完成的消息
SELECT '管理员账号初始化完成！' as message;
SELECT 'admin/123456 - 管理员账号已创建' as admin_info;