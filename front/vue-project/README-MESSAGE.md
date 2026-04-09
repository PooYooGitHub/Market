# 消息模块开发文档

## 📋 功能概述

消息模块实现了完整的即时通讯功能，支持用户之间的实时消息收发。基于 WebSocket 实现消息推送，提供流畅的聊天体验。

## 🏗️ 架构设计

### 文件结构

```
vue-project/
├── src/
│   ├── api/
│   │   └── message.js          # 消息相关 API 接口
│   ├── utils/
│   │   └── websocket.js        # WebSocket 连接管理服务
│   ├── views/
│   │   └── Messages.vue        # 消息页面组件
│   └── App.vue                 # 全局 WebSocket 连接管理
```

### 技术栈

- **Vue 3**: 使用 Composition API 开发
- **WebSocket**: 实时消息推送
- **Axios**: HTTP 请求处理
- **Pinia**: 用户状态管理

## 🚀 核心功能

### 1. WebSocket 连接管理 (`websocket.js`)

- ✅ 自动连接和断线重连
- ✅ 心跳保活机制
- ✅ 消息监听器管理
- ✅ 连接状态监控

**使用示例**：

```javascript
import websocketService from '@/utils/websocket'

// 建立连接
websocketService.connect(userId)

// 添加消息监听器
const handleMessage = (message) => {
  console.log('收到消息:', message)
}
websocketService.addMessageHandler(handleMessage)

// 发送消息（如需要）
websocketService.send({ type: 'ping' })

// 关闭连接
websocketService.close()
```

### 2. 消息 API (`message.js`)

提供完整的消息相关接口：

- **sendMessage**: 发送消息
- **getConversationList**: 获取会话列表
- **getChatHistory**: 获取聊天记录
- **markAsRead**: 标记消息为已读
- **getUnreadCount**: 获取未读消息总数
- **deleteConversation**: 删除会话
- **getOnlineStatus**: 获取用户在线状态
- **getOnlineCount**: 获取在线用户数

### 3. 消息页面 (`Messages.vue`)

完整的聊天界面，包含：

- **会话列表**: 显示所有会话，包含未读消息数
- **聊天窗口**: 实时消息收发
- **在线状态**: 显示对方是否在线
- **自动已读**: 打开会话时自动标记为已读
- **消息推送**: 通过 WebSocket 实时接收新消息

## 🎯 使用说明

### 访问消息页面

1. 用户需要先登录
2. 点击导航栏中的"消息"链接
3. 进入消息页面查看会话列表

### 发送消息

1. 在会话列表中选择联系人
2. 在输入框中输入消息内容
3. 按 Enter 键或点击"发送"按钮

### 快捷键

- `Enter`: 发送消息
- `Shift + Enter`: 换行

## 🔧 配置说明

### WebSocket 连接地址

默认地址：`ws://localhost:8104/ws/chat/{userId}`

如需修改，请编辑 `src/utils/websocket.js` 文件：

```javascript
this.ws = new WebSocket(`ws://your-server:port/ws/chat/${userId}`)
```

### API 基础地址

默认地址：`http://localhost:9000/api/message`

如需修改，请编辑 `src/utils/request.js` 中的 `baseURL`。

## 📝 注意事项

### 1. WebSocket 连接时机

- 用户登录后自动建立 WebSocket 连接（在 `App.vue` 中处理）
- 用户登出时自动关闭 WebSocket 连接
- 页面刷新时会自动重连

### 2. 消息发送流程

1. 前端调用 `sendMessage` API
2. 后端通过 RocketMQ 异步处理消息
3. 后端通过 WebSocket 推送消息给发送者和接收者
4. 前端收到推送后更新界面

### 3. 未读消息处理

- 打开聊天窗口时自动标记为已读
- 会话列表显示每个会话的未读数
- 可在导航栏显示总未读数（需额外集成）

### 4. 在线状态刷新

- 打开聊天窗口时查询对方在线状态
- 每 30 秒自动刷新在线状态
- 在线用户数每 30 秒刷新一次

### 5. 性能优化

- 聊天记录分页加载（默认 50 条）
- WebSocket 消息自动去重
- 断线重连采用指数退避策略

## 🐛 故障排查

### WebSocket 无法连接

1. 检查后端消息服务是否启动（端口 8104）
2. 检查网关是否正确转发 WebSocket 请求
3. 查看浏览器控制台错误信息

### 消息无法发送

1. 检查用户是否已登录
2. 检查 token 是否有效
3. 查看网络请求响应状态

### 消息无法实时显示

1. 检查 WebSocket 连接状态
2. 查看浏览器控制台是否收到 WebSocket 消息
3. 检查消息处理器是否正确注册

## 🔄 后续优化方向

1. **消息类型扩展**: 支持图片、文件等多媒体消息
2. **消息撤回**: 支持消息撤回功能
3. **消息搜索**: 支持消息内容搜索
4. **消息通知**: 浏览器通知、声音提醒
5. **表情包**: 支持表情符号和贴图
6. **消息转发**: 支持消息转发给其他用户
7. **群聊功能**: 支持多人群聊
8. **未读消息角标**: 在导航栏显示未读消息数

## 📞 技术支持

如有问题，请参考：

- 前端对接文档：`对接文档/前端对接文档-Message模块.md`
- 后端 API 文档：Message 服务接口文档

---

**更新日期**: 2026-02-15
**版本**: 1.0.0
