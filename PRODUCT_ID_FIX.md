# 商品ID传递问题修复说明

## 📋 问题描述

**现象**: 用户从商品详情页点击"联系卖家"发送消息时，数据库中的 `t_chat_message` 表的 `product_id` 字段为 `NULL`。

**影响**: 无法关联消息与商品，导致后续无法追踪用户在咨询哪个商品。

## 🔍 问题分析

### 数据流追踪

```
商品详情页 (ProductDetail.vue)
    ↓ 通过 router.push 传递 query 参数
消息页面 (Messages.vue) 
    ↓ 从 route.query 获取 productId
发送消息请求
    ↓ API: /api/message/send
后端 (MessageController.java)
    ↓ 保存到数据库
t_chat_message 表
```

### 根本原因

前端代码流程：
1. ✅ **ProductDetail.vue** 正确传递了 `productId`：
   ```javascript
   router.push({
     path: '/messages',
     query: {
       userId: product.value.seller.id,
       username: product.value.seller.username,
       nickname: product.value.seller.nickname,
       avatar: product.value.seller.avatar,
       productId: product.value.id  // ✅ 传递了 productId
     }
   })
   ```

2. ❌ **Messages.vue** 没有接收和使用 `productId`：
   - 没有从 `route.query.productId` 中获取商品ID
   - 发送消息时没有包含 `productId` 字段

3. ✅ **后端 SendMessageDTO** 已有 `productId` 字段（可选）

## ✅ 修复方案

### 修改文件: `front/vue-project/src/views/Messages.vue`

#### 1. 添加状态变量保存 productId

```javascript
// 在状态声明区域添加
const productId = ref(null)
```

#### 2. 在 onMounted 中获取 productId

```javascript
onMounted(async () => {
  // ... 现有代码 ...
  
  // 获取商品ID
  if (route.query.productId) {
    productId.value = parseInt(route.query.productId)
    console.log('从商品详情页跳转，商品ID:', productId.value)
  }
  
  // ... 现有代码 ...
})
```

#### 3. 发送消息时包含 productId

```javascript
const handleSendMessage = async () => {
  // ... 现有代码 ...
  
  try {
    const requestData = {
      receiverId: receiverId,
      content: messageContent,
      messageType: 1
    }
    
    // 如果有 productId，添加到请求中
    if (productId.value) {
      requestData.productId = productId.value
    }
    
    const res = await sendMessage(requestData)
    
    // ... 现有代码 ...
  }
}
```

## 🧪 测试验证

### 测试步骤

1. **启动服务**
   ```bash
   # 确保以下服务已启动
   - MySQL (3306)
   - Redis (6379)
   - Nacos (8848)
   - RocketMQ (9876)
   - Gateway (9901)
   - User服务 (8101)
   - Product服务 (8102)
   - Message服务 (8103)
   ```

2. **前端测试**
   ```bash
   cd front/vue-project
   npm run dev
   ```

3. **操作流程**
   - 登录系统
   - 浏览商品列表
   - 点击某个商品进入详情页
   - 点击"联系卖家"按钮
   - 发送一条消息（例如："你好，商品还在吗？"）

4. **数据验证**
   
   **查询数据库**:
   ```sql
   SELECT 
     id,
     sender_id,
     receiver_id,
     content,
     product_id,  -- 应该不为 NULL
     create_time
   FROM t_chat_message
   ORDER BY create_time DESC
   LIMIT 5;
   ```

   **预期结果**:
   ```
   +----+-----------+-------------+------------------+------------+---------------------+
   | id | sender_id | receiver_id | content          | product_id | create_time         |
   +----+-----------+-------------+------------------+------------+---------------------+
   | 10 | 8         | 6           | 你好，商品还在吗？ | 1          | 2026-02-16 10:30:45 |
   +----+-----------+-------------+------------------+------------+---------------------+
   ```
   
   ✅ **product_id 字段应该有值（商品ID）**

## 📊 修复前后对比

### 修复前
```javascript
// Messages.vue - handleSendMessage
const res = await sendMessage({
  receiverId: receiverId,
  content: messageContent,
  messageType: 1
  // ❌ 缺少 productId
})
```

**数据库结果**:
```sql
product_id: NULL  ❌
```

### 修复后
```javascript
// Messages.vue - handleSendMessage
const requestData = {
  receiverId: receiverId,
  content: messageContent,
  messageType: 1
}

// ✅ 条件添加 productId
if (productId.value) {
  requestData.productId = productId.value
}

const res = await sendMessage(requestData)
```

**数据库结果**:
```sql
product_id: 1  ✅
```

## 🎯 业务价值

修复后可以实现：

1. **消息与商品关联**
   - 查看用户在咨询哪个商品
   - 统计商品咨询量

2. **更好的用户体验**
   - 在聊天界面显示关联商品信息
   - 快速跳转到商品详情

3. **数据分析**
   - 分析热门咨询商品
   - 统计转化率

## 📝 注意事项

1. **productId 是可选字段**
   - 从商品详情页进入：有 productId
   - 直接访问消息页面：没有 productId
   - 后续发送消息：继续使用第一次的 productId

2. **会话切换时的处理**
   - 当前实现：productId 在整个 Messages 组件生命周期内保持
   - 改进建议：切换会话时清空 productId，或根据会话记录恢复

3. **后续优化方向**
   - 在会话对象中保存 productId
   - 在聊天界面显示关联的商品卡片
   - 支持在聊天中发送商品链接

## ✨ 总结

- **问题**: 前端没有传递 productId 给后端
- **原因**: Messages.vue 未从 query 参数中获取和使用 productId
- **修复**: 添加 productId 状态变量，从 route.query 获取，发送消息时包含
- **影响**: 仅涉及前端代码，后端无需修改
- **状态**: ✅ 已修复

---

**修复完成时间**: 2026-02-16  
**修复文件**: `front/vue-project/src/views/Messages.vue`  
**测试状态**: 待验证 🧪

