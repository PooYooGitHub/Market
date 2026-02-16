# 🧪 ProductId 传递功能测试指南

## 快速测试 (5分钟)

### 1️⃣ 准备工作

确保以下服务已启动：
```bash
# 查看服务状态
docker ps  # MySQL, Redis, Nacos, RocketMQ 应该都在运行

# 查看 Java 服务
jps  # 应该看到 Gateway, User, Product, Message 服务
```

### 2️⃣ 启动前端

```bash
cd D:\program\Market\front\vue-project
npm run dev
```

访问: http://localhost:5173

### 3️⃣ 测试步骤

#### Step 1: 登录
- 使用已有账号登录（例如：用户名 `test1`，密码 `123456`）

#### Step 2: 浏览商品
- 点击导航栏的"商品列表"
- 选择任意一个商品点击进入详情

#### Step 3: 联系卖家
- 在商品详情页点击 **"联系卖家"** 按钮
- 页面会跳转到消息页面

#### Step 4: 发送消息
- 在输入框输入消息，例如："你好，这个商品还在吗？"
- 点击"发送"按钮

#### Step 5: 验证数据

**方法1: 查看浏览器控制台**
- 按 F12 打开开发者工具
- 查看 Console 标签，应该看到：
  ```
  从商品详情页跳转，商品ID: 1
  ```

**方法2: 查看网络请求**
- F12 → Network 标签
- 找到 `/api/message/send` 请求
- 查看 Request Payload：
  ```json
  {
    "receiverId": 6,
    "content": "你好，这个商品还在吗？",
    "messageType": 1,
    "productId": 1  // ✅ 应该有这个字段
  }
  ```

**方法3: 查询数据库**
```sql
-- 打开 Navicat 或 MySQL Workbench
-- 连接到 market_message 数据库

SELECT 
  id,
  sender_id,
  receiver_id,
  content,
  product_id,  -- 重点关注这个字段
  create_time
FROM t_chat_message
ORDER BY create_time DESC
LIMIT 5;
```

**预期结果**:
```
+----+-----------+-------------+---------------------------+------------+---------------------+
| id | sender_id | receiver_id | content                   | product_id | create_time         |
+----+-----------+-------------+---------------------------+------------+---------------------+
| 15 | 8         | 6           | 你好，这个商品还在吗？      | 1          | 2026-02-16 11:00:00 |
+----+-----------+-------------+---------------------------+------------+---------------------+
```

✅ **product_id 不为 NULL，显示商品ID**

### 4️⃣ 成功标志

- [ ] 浏览器控制台显示商品ID日志
- [ ] 网络请求包含 productId 字段
- [ ] 数据库 product_id 字段有值
- [ ] 消息发送成功

---

## 🐛 故障排查

### 问题1: product_id 仍然是 NULL

**原因**: 前端代码未更新

**解决**:
```bash
# 1. 停止前端服务 (Ctrl+C)
# 2. 清除缓存
cd D:\program\Market\front\vue-project
rm -rf node_modules/.vite

# 3. 重启
npm run dev
```

### 问题2: 控制台没有看到"商品ID"日志

**检查**:
- 确保是从**商品详情页**点击"联系卖家"进入，而不是直接访问消息页面
- URL 应该是: `http://localhost:5173/messages?userId=6&username=...&productId=1`

### 问题3: 网络请求中没有 productId

**检查浏览器缓存**:
```
F12 → Application → Clear storage → Clear site data
然后刷新页面重试
```

---

## 📋 测试检查表

### 正常流程
- [ ] 从商品列表进入商品详情
- [ ] 商品详情页显示"联系卖家"按钮
- [ ] 点击按钮跳转到消息页面
- [ ] URL 包含 userId 和 productId 参数
- [ ] 控制台显示"从商品详情页跳转，商品ID: X"
- [ ] 发送消息成功
- [ ] 数据库 product_id 字段有值

### 边界情况
- [ ] 直接访问消息页面（没有 productId）
  - 发送消息成功
  - product_id 为 NULL（符合预期）
  
- [ ] 切换会话后发送消息
  - product_id 保持第一次的值
  
- [ ] 刷新消息页面
  - 如果 URL 有 productId，保持
  - 如果 URL 没有 productId，为 NULL

---

## 🎉 测试成功示例

### 浏览器控制台输出
```
从商品详情页跳转，商品ID: 1
WebSocket 连接成功
收到 WebSocket 消息: {type: "connect", ...}
```

### 网络请求 Payload
```json
{
  "receiverId": 6,
  "content": "你好，这个商品还在吗？",
  "messageType": 1,
  "productId": 1
}
```

### 数据库记录
```sql
mysql> SELECT id, sender_id, receiver_id, content, product_id FROM t_chat_message ORDER BY id DESC LIMIT 1;
+----+-----------+-------------+---------------------------+------------+
| id | sender_id | receiver_id | content                   | product_id |
+----+-----------+-------------+---------------------------+------------+
| 15 | 8         | 6           | 你好，这个商品还在吗？      | 1          |
+----+-----------+-------------+---------------------------+------------+
1 row in set (0.00 sec)
```

✅ **所有测试通过！**

---

**测试编写**: 2026-02-16  
**预计测试时长**: 5分钟  
**难度**: ⭐ (简单)

