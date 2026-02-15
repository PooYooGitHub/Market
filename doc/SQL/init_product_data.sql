-- 商品服务初始化数据
-- 使用数据库
USE `market_product`;

-- 清空表（如果需要重新初始化）
-- TRUNCATE TABLE `t_category`;

-- 插入商品分类数据
INSERT INTO `t_category` (`id`, `parent_id`, `name`, `sort`, `icon`, `level`) VALUES
(1, 0, '数码产品', 1, 'phone', 1),
(2, 0, '图书教材', 2, 'book', 1),
(3, 0, '生活用品', 3, 'home', 1),
(4, 0, '服装鞋包', 4, 'clothes', 1),
(5, 0, '运动健身', 5, 'sport', 1),
(6, 0, '其他', 99, 'more', 1);

-- 插入二级分类（可选）
INSERT INTO `t_category` (`parent_id`, `name`, `sort`, `icon`, `level`) VALUES
(1, '手机', 1, 'mobile', 2),
(1, '电脑', 2, 'laptop', 2),
(1, '平板', 3, 'tablet', 2),
(1, '耳机音响', 4, 'headphone', 2),
(2, '教材教辅', 1, 'textbook', 2),
(2, '小说文学', 2, 'novel', 2),
(2, '考研资料', 3, 'exam', 2),
(3, '家居日用', 1, 'daily', 2),
(3, '美妆护肤', 2, 'cosmetic', 2),
(4, '上衣', 1, 'top', 2),
(4, '裤装', 2, 'pants', 2),
(4, '鞋类', 3, 'shoes', 2),
(5, '球类', 1, 'ball', 2),
(5, '健身器材', 2, 'fitness', 2);

-- 插入测试商品数据（可选）
INSERT INTO `t_product` (`seller_id`, `title`, `description`, `price`, `original_price`, `category_id`, `status`, `view_count`) VALUES
(1, '二手iPhone 13 Pro 128G', '9成新，无磕碰，原装配件齐全。因换新机出售，性能完好。', 4999.00, 6999.00, 1, 1, 128),
(1, '大学英语四级真题', '最新版四级真题集，基本全新，只做过一套题。', 15.00, 35.00, 2, 1, 45),
(2, 'MacBook Air M1 256G', '2021款，9.5成新，无维修记录。办公学习完全够用。', 5500.00, 7999.00, 1, 1, 256),
(2, '高等数学教材（同济版）', '7成新，有笔记但不影响阅读。', 20.00, 45.00, 2, 1, 67),
(3, '全新耐克运动鞋', '40码，全新未穿，买错尺码了。', 299.00, 599.00, 4, 1, 89);

-- 插入商品图片数据（需要先有商品）
INSERT INTO `t_product_image` (`product_id`, `image_url`, `sort`) VALUES
(1, 'https://example.com/images/iphone13-1.jpg', 0),
(1, 'https://example.com/images/iphone13-2.jpg', 1),
(2, 'https://example.com/images/book-cet4.jpg', 0),
(3, 'https://example.com/images/macbook-1.jpg', 0),
(3, 'https://example.com/images/macbook-2.jpg', 1),
(4, 'https://example.com/images/math-book.jpg', 0),
(5, 'https://example.com/images/nike-shoes-1.jpg', 0),
(5, 'https://example.com/images/nike-shoes-2.jpg', 1);

COMMIT;

