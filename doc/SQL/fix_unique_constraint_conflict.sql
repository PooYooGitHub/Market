-- ================================================================
-- 修复用户表唯一索引冲突问题
-- 问题：多个用户的phone字段为空字符串''，违反uk_phone唯一索引
-- 解决：将空字符串更新为NULL
-- ================================================================

USE market_user;

-- 1. 查看当前有多少条phone为空字符串的记录
SELECT COUNT(*) as empty_phone_count
FROM t_user
WHERE phone = '';

-- 2. 显示这些记录（可选，用于确认）
SELECT id, username, phone, email, create_time
FROM t_user
WHERE phone = ''
ORDER BY id;

-- 3. 将空字符串更新为NULL
UPDATE t_user
SET phone = NULL
WHERE phone = '';

-- 4. 同样处理email字段（如果也有唯一索引）
UPDATE t_user
SET email = NULL
WHERE email = '';

-- 5. 验证修复结果
SELECT
    COUNT(*) as total_users,
    SUM(CASE WHEN phone IS NULL THEN 1 ELSE 0 END) as null_phone_count,
    SUM(CASE WHEN phone = '' THEN 1 ELSE 0 END) as empty_phone_count,
    SUM(CASE WHEN phone IS NOT NULL AND phone != '' THEN 1 ELSE 0 END) as valid_phone_count
FROM t_user;

-- 6. 确认唯一索引状态
SHOW INDEX FROM t_user WHERE Key_name = 'uk_phone';

-- ================================================================
-- 说明：
-- 1. 这个脚本会将所有phone=''的记录更新为NULL
-- 2. NULL值不会违反唯一索引约束
-- 3. 后端代码已经修复，新注册的用户不会再出现这个问题
-- ================================================================

