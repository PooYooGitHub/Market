# 💬 ProductId 参数使用说明 (Message 模块补充)

## 📌 概述

在消息模块的 `sendMessage` 接口中，`productId` 是一个**可选参数**，用于关联消息与商品。

## 🎯 使用场景

### 场景1: 从商品详情页咨询（推荐）✅

**业务流程**:
1. 用户浏览商品列表
2. 点击商品进入详情页
3. 在详情页点击"联系卖家"按钮
4. 系统跳转到消息页面，并传递 `productId`
5. 发送消息时自动关联该商品

**实现代码** (ProductDetail.vue):
```javascript
// 联系卖家按钮点击事件
const contactSeller = () => {
  if (!userStore.isLoggedIn) {
    alert('请先登录')
    router.push('/login')
    return
  }
  
  // 跳转到消息页面，传递商品信息
  router.push({
    path: '/messages',
    query: {
      userId: product.value.seller.id,
      username: product.value.seller.username,
      nickname: product.value.seller.nickname,
      avatar: product.value.seller.avatar,
      productId: product.value.id  // ⭐ 关键：传递商品ID
    }
  })
}
```

**URL 示例**:
```
http://localhost:5173/messages?userId=6&username=seller1&nickname=卖家&avatar=xxx.jpg&productId=1
```

### 场景2: 直接访问消息页面

**业务流程**:
1. 用户直接点击导航栏的"消息"
2. 选择已有会话进行聊天
3. 发送消息时不关联商品

**特点**:
- 没有 `productId` 参数
- 发送的消息 `product_id` 字段为 `NULL`
- 这是正常的，因为不是从商品详情页来的

## 🔧 前端实现

### Messages.vue 完整实现

```javascript
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { sendMessage } from '@/api/message'

const route = useRoute()

// 1️⃣ 声明状态变量
const productId = ref(null)

// 2️⃣ 在组件加载时获取 productId
onMounted(async () => {
  // 从 URL query 参数获取商品ID
  if (route.query.productId) {
    productId.value = parseInt(route.query.productId)
    console.log('从商品详情页跳转，商品ID:', productId.value)
  }
  
  // ... 其他初始化代码
})

// 3️⃣ 发送消息时包含 productId
const handleSendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  const messageContent = inputMessage.value.trim()
  const receiverId = selectedConv.value.otherUserId
  
  // 构建请求数据
  const requestData = {
    receiverId: receiverId,
    content: messageContent,
    messageType: 1
  }
  
  // ⭐ 如果有 productId，添加到请求中
  if (productId.value) {
    requestData.productId = productId.value
  }
  
  try {
    const res = await sendMessage(requestData)
    
    if (res.code === 200) {
      console.log('消息发送成功')
      // 清空输入框等后续处理...
    }
  } catch (error) {
    console.error('发送消息失败:', error)
  }
}
```

## 📊 数据流追踪

```
┌─────────────────┐
│ 商品详情页       │
│ ProductDetail   │
└────────┬────────┘
         │ router.push({ query: { productId: 1 } })
         ↓
┌─────────────────┐
│ 消息页面        │
│ Messages.vue    │
├─────────────────┤
│ productId = 1   │ ← route.query.productId
└────────┬────────┘
         │ sendMessage({ productId: 1 })
         ↓
┌─────────────────┐
│ 后端 API        │
│ /message/send   │
├─────────────────┤
│ productId: 1    │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│ 数据库          │
│ t_chat_message  │
├─────────────────┤
│ product_id: 1   │ ✅
└─────────────────┘
```

## 🗄️ 数据库结构

### t_chat_message 表

```sql
CREATE TABLE t_chat_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sender_id BIGINT NOT NULL COMMENT '发送者ID',
  receiver_id BIGINT NOT NULL COMMENT '接收者ID',
  content TEXT NOT NULL COMMENT '消息内容',
  message_type INT NOT NULL DEFAULT 1 COMMENT '消息类型:1-文字 2-图片',
  product_id BIGINT NULL COMMENT '关联商品ID',  -- ⭐ 可以为空
  is_read INT DEFAULT 0 COMMENT '是否已读:0-未读 1-已读',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_sender (sender_id),
  INDEX idx_receiver (receiver_id),
  INDEX idx_product (product_id)  -- 索引，便于查询
);
```

### 查询示例

**查询某个商品的所有咨询消息**:
```sql
SELECT 
  cm.*,
  u1.username as sender_username,
  u2.username as receiver_username
FROM t_chat_message cm
LEFT JOIN t_user u1 ON cm.sender_id = u1.id
LEFT JOIN t_user u2 ON cm.receiver_id = u2.id
WHERE cm.product_id = 1  -- 商品ID
ORDER BY cm.create_time DESC;
```

**统计商品咨询次数**:
```sql
SELECT 
  p.id,
  p.title,
  COUNT(DISTINCT cm.sender_id) as consult_user_count,
  COUNT(cm.id) as consult_message_count
FROM t_product p
LEFT JOIN t_chat_message cm ON p.id = cm.product_id
GROUP BY p.id, p.title
ORDER BY consult_message_count DESC;
```

## 🎨 UI 展示建议

### 在聊天界面显示关联商品

```vue
<template>
  <div class="chat-window">
    <!-- 如果有关联商品，显示商品卡片 -->
    <div v-if="currentProduct" class="product-card">
      <img :src="currentProduct.image" class="product-image" />
      <div class="product-info">
        <div class="product-title">{{ currentProduct.title }}</div>
        <div class="product-price">¥{{ currentProduct.price }}</div>
        <a :href="`/product/${currentProduct.id}`" class="view-link">查看详情</a>
      </div>
    </div>
    
    <!-- 消息列表 -->
    <div class="message-list">
      <!-- ... -->
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getProductDetail } from '@/api/product'

const currentProduct = ref(null)
const productId = ref(null)

// 监听 productId 变化，加载商品信息
watch(productId, async (newId) => {
  if (newId) {
    try {
      const res = await getProductDetail(newId)
      if (res.code === 200) {
        currentProduct.value = res.data
      }
    } catch (error) {
      console.error('加载商品信息失败:', error)
    }
  } else {
    currentProduct.value = null
  }
}, { immediate: true })
</script>
```

## ✅ 测试检查清单

### 正常流程测试
- [ ] 从商品详情页点击"联系卖家"
- [ ] URL 包含 `productId` 参数
- [ ] 浏览器控制台打印 "从商品详情页跳转，商品ID: X"
- [ ] 发送消息
- [ ] 数据库 `product_id` 字段有值

### 边界情况测试
- [ ] 直接访问消息页面（无 productId）
  - 发送消息成功
  - `product_id` 为 NULL
  
- [ ] 从商品页进入后，切换到其他会话
  - 仍然使用第一次的 productId
  - 建议改进：切换会话时清空 productId

- [ ] 刷新页面
  - 如果 URL 有 productId → 保持
  - 如果 URL 没有 productId → NULL

## 🔍 调试技巧

### 1. 查看浏览器控制台
```javascript
// 在 Messages.vue 中添加日志
console.log('当前 productId:', productId.value)
console.log('发送消息请求:', requestData)
```

### 2. 查看网络请求
```
F12 → Network → /api/message/send
Request Payload 中应该看到 productId 字段（如果有的话）
```

### 3. 查询数据库
```sql
-- 查看最新的消息记录
SELECT * FROM t_chat_message ORDER BY id DESC LIMIT 10;

-- 检查 product_id 是否有值
SELECT 
  id,
  content,
  product_id,
  CASE 
    WHEN product_id IS NULL THEN '❌ 没有关联商品'
    ELSE CONCAT('✅ 关联商品ID: ', product_id)
  END as status
FROM t_chat_message
ORDER BY id DESC
LIMIT 10;
```

## 🚀 未来优化方向

### 1. 会话级别的 productId 管理
```javascript
// 在会话对象中保存 productId
const conversations = ref([
  {
    id: 1,
    otherUserId: 2,
    productId: 10,  // 这个会话是关于商品10的
    // ...
  }
])

// 切换会话时更新 productId
watch(selectedConv, (newConv) => {
  if (newConv && newConv.productId) {
    productId.value = newConv.productId
  } else {
    productId.value = null
  }
})
```

### 2. 消息气泡中显示商品信息
```vue
<div v-for="msg in messages" :key="msg.id" class="message-item">
  <div class="message-content">{{ msg.content }}</div>
  
  <!-- 如果消息关联了商品，显示商品缩略信息 -->
  <div v-if="msg.productId" class="linked-product">
    <small>💬 关于: <a :href="`/product/${msg.productId}`">商品详情</a></small>
  </div>
</div>
```

### 3. 商品状态同步
- 如果商品已售出，在聊天界面显示"该商品已售出"
- 如果商品已删除，显示"该商品不存在"

---

## 📚 相关文档

- [完整修复说明](./PRODUCT_ID_FIX.md)
- [测试指南](./TEST_PRODUCT_ID.md)
- [Message 模块 API 文档](./front/对接文档/前端对接文档-Message模块.md)

---

**文档更新**: 2026-02-16  
**适用版本**: v1.0+  
**维护者**: 开发团队

