# 🚀 RocketMQ离线推送完整实现指南

## ✅ 已完成的功能

### 1. RocketMQ消费者监听器 ✅
**文件**: `ChatMessageListener.java`

**功能**:
- 监听 `chat-message-topic` 主题
- 检查接收者是否在线
- 在线用户 → WebSocket实时推送
- 离线用户 → 存储到Redis离线消息队列

### 2. 离线消息服务 ✅
**文件**: `OfflineMessageService.java` / `OfflineMessageServiceImpl.java`

**功能**:
- 获取离线消息列表
- 清除离线消息
- 推送离线消息给上线用户

### 3. WebSocket自动推送 ✅
**文件**: `ChatWebSocketServer.java`

**功能**:
- 用户连接时自动检测离线消息
- 延迟1秒后推送所有离线消息
- 推送完成后发送通知

### 4. Controller接口 ✅
**新增接口**:
- `GET /message/offline` - 获取离线消息
- `DELETE /message/offline` - 清除离线消息

---

## 🔧 RocketMQ配置修复步骤

### 步骤1: 检查RocketMQ容器状态

```powershell
# 查看RocketMQ相关容器
docker ps -a | Select-String rocketmq

# 应该看到两个容器：
# - rocketmq-namesrv (NameServer)
# - rocketmq-broker (Broker)
```

### 步骤2: 启动RocketMQ容器

如果容器已停止，执行：

```powershell
# 启动NameServer
docker start rocketmq-namesrv

# 启动Broker
docker start rocketmq-broker

# 验证状态
docker ps | Select-String rocketmq
```

### 步骤3: 检查端口映射

```powershell
# 查看NameServer端口
docker port rocketmq-namesrv

# 应该显示: 9876/tcp -> 0.0.0.0:9876

# 查看Broker端口
docker port rocketmq-broker

# 应该显示:
# 10909/tcp -> 0.0.0.0:10909
# 10911/tcp -> 0.0.0.0:10911
# 10912/tcp -> 0.0.0.0:10912
```

### 步骤4: 配置文件修改（如果需要）

**文件**: `market-service-message/src/main/resources/application.yml`

**当前配置**:
```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: market-message-producer
    send-message-timeout: 3000
    retry-times-when-send-failed: 2
```

**如果localhost不工作，尝试修改为**:
```yaml
rocketmq:
  name-server: 127.0.0.1:9876  # 或 Docker宿主机IP
  producer:
    group: market-message-producer
    send-message-timeout: 5000  # 增加超时时间
    retry-times-when-send-failed: 3  # 增加重试次数
```

---

## 📊 完整消息流程

### 场景1: 用户在线接收消息

```
用户A发送消息给用户B (B在线)
    ↓
1. MessageService保存到MySQL ✅
    ↓
2. 发送到RocketMQ队列 ✅
    ↓
3. ChatMessageListener消费消息
    ↓
4. 检测到用户B在线 ✅
    ↓
5. 通过WebSocket实时推送 ✅
    ↓
6. 用户B立即收到消息 🎉
```

### 场景2: 用户离线接收消息

```
用户A发送消息给用户B (B离线)
    ↓
1. MessageService保存到MySQL ✅
    ↓
2. 发送到RocketMQ队列 ✅
    ↓
3. ChatMessageListener消费消息
    ↓
4. 检测到用户B离线 ⚠️
    ↓
5. 消息ID存入Redis离线队列 ✅
   Key: offline:message:user:{userId}
   Value: [messageId1, messageId2, ...]
    ↓
用户B重新上线
    ↓
6. WebSocket连接建立
    ↓
7. 自动触发离线消息推送 ✅
    ↓
8. 从Redis读取离线消息ID列表
    ↓
9. 从MySQL查询消息详情
    ↓
10. 通过WebSocket逐条推送 📮
    ↓
11. 推送完成后清除Redis记录 ✅
    ↓
12. 发送通知: "您有X条离线消息" 🔔
```

---

## 🧪 测试步骤

### 测试1: RocketMQ连接测试

1. 确保RocketMQ容器运行中
2. 启动Message服务
3. 查看启动日志，应该看到：

```
✅ RocketMQTemplate初始化成功
✅ ChatMessageListener注册成功
✅ Tomcat started on port(s): 8103
```

### 测试2: 在线消息推送

1. 打开两个浏览器窗口
2. 分别登录用户A (ID=8) 和用户B (ID=6)
3. A给B发送消息
4. 观察后端日志：

```
✅ 消息发送成功: 8 -> 6
✅ RocketMQ消息推送成功
✅ RocketMQ收到消息: messageId=xxx, sender=8, receiver=6
✅ 检测到用户6在线
✅ 消息实时推送成功: messageId=xxx, receiver=6
```

5. B应该立即收到消息 ✅

### 测试3: 离线消息推送

1. 只登录用户A (ID=8)
2. A给用户B (ID=6) 发送3条消息
3. 观察后端日志：

```
✅ 消息发送成功: 8 -> 6
✅ RocketMQ消息推送成功
✅ RocketMQ收到消息: messageId=xxx
⚠️ 检测到用户6离线
✅ 离线消息已存储: messageId=xxx, receiver=6
```

4. 登录用户B
5. B连接WebSocket后，观察日志：

```
✅ 用户6建立WebSocket连接
✅ 用户6有3条离线消息
✅ 已推送3条离线消息给用户6
✅ 已清除用户6的离线消息
```

6. B应该看到弹出通知: "您有3条离线消息" 🔔

---

## 🔍 日志检查

### 正常日志示例

```
# 消息发送
2026-02-16 10:00:00 INFO  MessageServiceImpl - 消息发送成功: 8 -> 6
2026-02-16 10:00:00 INFO  MessageServiceImpl - RocketMQ消息推送成功

# RocketMQ消费
2026-02-16 10:00:01 INFO  ChatMessageListener - RocketMQ收到消息: messageId=1, sender=8, receiver=6

# 在线推送
2026-02-16 10:00:01 INFO  ChatMessageListener - 消息实时推送成功: messageId=1, receiver=6

# 离线存储
2026-02-16 10:00:01 INFO  ChatMessageListener - 离线消息已存储: messageId=2, receiver=7

# 用户上线
2026-02-16 10:05:00 INFO  ChatWebSocketServer - 用户7建立WebSocket连接
2026-02-16 10:05:01 INFO  OfflineMessageServiceImpl - 用户7有5条离线消息
2026-02-16 10:05:02 INFO  OfflineMessageServiceImpl - 已推送5条离线消息给用户7
```

### 异常日志处理

#### 1. RocketMQ连接超时

```
❌ ERROR: sendDefaultImpl call timeout
```

**解决方案**:
- 检查RocketMQ容器是否运行
- 验证端口映射: `docker port rocketmq-namesrv`
- 修改配置使用127.0.0.1或宿主机IP

#### 2. Redis连接失败

```
❌ ERROR: Cannot get Jedis connection
```

**解决方案**:
- 检查Redis服务状态
- 验证application.yml中的Redis配置

---

## 🎯 接口文档更新

### 新增接口

#### 1. 获取离线消息

**接口**: `GET /api/message/offline`

**请求头**:
```
X-User-Id: 8
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "senderId": 6,
      "senderUsername": "testuser",
      "senderAvatar": "http://...",
      "content": "你好",
      "messageType": 1,
      "productId": null,
      "createTime": "2026-02-16T10:00:00"
    }
  ]
}
```

#### 2. 清除离线消息

**接口**: `DELETE /api/message/offline`

**请求头**:
```
X-User-Id: 8
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功"
}
```

---

## 🚀 启动顺序

1. ✅ 启动Redis
2. ✅ 启动MySQL
3. ✅ 启动RocketMQ (NameServer + Broker)
4. ✅ 启动Nacos
5. ✅ 启动Gateway
6. ✅ 启动User服务
7. ✅ 启动Message服务

---

## 📝 前端对接

### WebSocket消息类型

#### 1. 连接成功
```javascript
{
  type: "connect",
  message: "连接成功"
}
```

#### 2. 实时消息
```javascript
{
  type: "message",
  messageId: 1,
  senderId: 6,
  senderUsername: "testuser",
  senderAvatar: "http://...",
  content: "你好",
  messageType: 1,
  productId: null,
  createTime: "2026-02-16T10:00:00"
}
```

#### 3. 离线消息
```javascript
{
  type: "offline_message",  // ← 注意类型
  messageId: 1,
  senderId: 6,
  senderUsername: "testuser",
  senderAvatar: "http://...",
  content: "你好",
  messageType: 1,
  productId: null,
  createTime: "2026-02-16T10:00:00"
}
```

#### 4. 离线推送完成通知
```javascript
{
  type: "offline_push_complete",
  count: 5,
  message: "您有 5 条离线消息"
}
```

### 前端处理示例

```javascript
// WebSocket连接
const ws = new WebSocket(`ws://localhost:8103/ws/chat/${userId}`);

ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  
  switch(data.type) {
    case 'connect':
      console.log('连接成功');
      break;
      
    case 'message':
      // 实时消息 - 显示在聊天窗口
      addMessageToChat(data);
      break;
      
    case 'offline_message':
      // 离线消息 - 显示在聊天窗口，可加特殊标记
      addMessageToChat(data, { isOffline: true });
      break;
      
    case 'offline_push_complete':
      // 显示通知
      showNotification(data.message);
      break;
  }
};
```

---

## ✅ 验证清单

- [ ] RocketMQ容器运行中
- [ ] NameServer端口9876可访问
- [ ] Broker端口10911可访问
- [ ] Message服务启动成功
- [ ] 发送消息时看到"RocketMQ消息推送成功"日志
- [ ] ChatMessageListener收到消息
- [ ] 在线用户能立即收到消息
- [ ] 离线消息存储到Redis
- [ ] 用户上线时收到离线消息推送
- [ ] 前端显示"您有X条离线消息"通知

---

## 🎉 完成标志

当你看到以下现象，说明RocketMQ离线推送功能完全正常：

1. ✅ 用户A给离线的用户B发消息
2. ✅ 后端日志显示"离线消息已存储"
3. ✅ Redis中能看到离线消息记录
4. ✅ 用户B上线后立即收到推送
5. ✅ 前端显示通知："您有X条离线消息"
6. ✅ Redis中的离线消息记录被清除

**恭喜！RocketMQ离线推送功能完整实现！** 🎊

