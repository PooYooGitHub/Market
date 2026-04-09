<template>
  <div class="messages-container">
    <!-- 会话列表 -->
    <div class="conversation-list">
      <div class="list-header">
        <h2>消息</h2>
        <div class="online-info">
          <span class="online-dot"></span>
          <span>{{ onlineCount }} 人在线</span>
        </div>
      </div>

      <div class="conversation-items">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          @click="selectConversation(conv)"
          :class="['conversation-item', { active: selectedConv?.id === conv.id }]"
        >
          <div class="avatar-wrapper">
            <img 
              :src="conv.otherAvatar || defaultAvatar" 
              class="avatar" 
              alt="用户头像"
              @error="handleAvatarError"
            />
            <span v-if="isUserOnline(conv.otherUserId)" class="online-badge"></span>
          </div>
          <div class="info">
            <div class="username">{{ conv.otherUsername }}</div>
            <div class="last-message">{{ conv.lastMessage }}</div>
          </div>
          <div class="meta">
            <div class="time">{{ formatTime(conv.lastMessageTime) }}</div>
            <div v-if="conv.unreadCount > 0" class="unread-badge">
              {{ conv.unreadCount }}
            </div>
          </div>
        </div>

        <div v-if="conversations.length === 0" class="empty-state">
          <p>暂无会话</p>
        </div>
      </div>
    </div>

    <!-- 聊天窗口 -->
    <div class="chat-window" v-if="selectedConv">
      <div class="chat-header">
        <div class="header-info">
          <span class="username">{{ selectedConv.otherUsername }}</span>
          <span :class="['online-status', { online: currentUserOnline }]">
            {{ currentUserOnline ? '在线' : '离线' }}
          </span>
        </div>
        <button @click="handleDeleteConversation" class="delete-btn">删除会话</button>
      </div>

      <div class="message-list" ref="messageList">
        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['message-item', { 'is-sender': msg.isSender }]"
        >
          <img
            :src="getMessageAvatar(msg)"
            class="avatar"
            alt="头像"
            @error="handleAvatarError"
          />
          <div class="message-content">
            <div class="message-text-wrapper">
              <div class="message-text">{{ msg.content }}</div>
              <!-- 发送状态图标（仅对发送者显示） -->
              <div v-if="msg.isSender" class="message-status">
                <!-- 发送中 -->
                <span v-if="msg.sendStatus === 'pending'" class="status-pending" title="发送中...">⏱</span>
                <!-- 发送成功 -->
                <span v-else-if="msg.sendStatus === 'success'" class="status-success" title="已送达">✓</span>
                <!-- 发送失败 -->
                <span v-else-if="msg.sendStatus === 'failed'" class="status-failed" :title="msg.errorMessage || '发送失败'">❗</span>
              </div>
            </div>
            <div class="message-time">{{ formatTime(msg.createTime) }}</div>
          </div>
        </div>

        <div v-if="messages.length === 0" class="empty-messages">
          开始聊天吧
        </div>
      </div>

      <div class="message-input">
        <textarea
          v-model="inputMessage"
          placeholder="输入消息... (按Enter发送，Shift+Enter换行)"
          @keydown.enter.exact.prevent="handleSendMessage"
          rows="3"
        ></textarea>
        <button @click="handleSendMessage" :disabled="!inputMessage.trim()">发送</button>
      </div>
    </div>

    <!-- 未选择会话时的提示 -->
    <div class="chat-placeholder" v-else>
      <div class="placeholder-content">
        <svg class="placeholder-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
        </svg>
        <p>选择一个会话开始聊天</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  getConversationList,
  getChatHistory,
  sendMessage,
  markAsRead,
  deleteConversation,
  getOnlineStatus,
  getOnlineCount
} from '@/api/message'
import websocketService from '@/utils/websocket'

const route = useRoute()
const userStore = useUserStore()

// 状态
const conversations = ref([])
const selectedConv = ref(null)
const messages = ref([])
const inputMessage = ref('')
const currentUserOnline = ref(false)
const onlineCount = ref(0)
const onlineUsers = ref(new Set())
const messageList = ref(null)
// 保存商品ID（从商品详情页跳转时带过来的）
const productId = ref(null)
// 使用 base64 编码的 SVG 默认头像，不依赖外部服务
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2Y1ZjVmNSIvPgogIDxjaXJjbGUgY3g9IjUwIiBjeT0iNDAiIHI9IjE1IiBmaWxsPSIjZGRkIi8+CiAgPHBhdGggZD0iTSAzMCA3MCBRIDMwIDU1IDUwIDU1IFEgNzAgNTUgNzAgNzAgTCA3MCA4MCBRIDcwIDkwIDUwIDkwIFEgMzAgOTAgMzAgODAgWiIgZmlsbD0iI2RkZCIvPgo8L3N2Zz4='

// WebSocket 连接
const connectWebSocket = () => {
  const userId = userStore.userInfo?.id
  if (!userId) {
    console.error('用户未登录，无法建立 WebSocket 连接')
    return
  }

  websocketService.connect(userId)
  websocketService.addMessageHandler(handleWebSocketMessage)
}

// 处理 WebSocket 消息
const handleWebSocketMessage = (message) => {
  console.log('收到 WebSocket 消息:', message)

  switch (message.type) {
    case 'connect':
      console.log('WebSocket 连接成功')
      break

    case 'message':
      // 实时消息
      handleNewMessage(message, false)
      break

    case 'offline_message':
      // 离线消息 - 标记为离线消息
      handleNewMessage(message, true)
      break

    case 'offline_push_complete':
      // 离线消息推送完成
      console.log(`📮 ${message.message}`)
      // 显示通知
      if (message.count > 0) {
        showOfflineNotification(message.count)
      }
      // 刷新会话列表
      loadConversations()
      break
  }
}

// 显示离线消息通知
const showOfflineNotification = (count) => {
  // 可以使用 Element Plus 的通知组件
  console.log(`您有 ${count} 条离线消息`)
  // TODO: 添加UI通知组件
}

// 处理新消息
const handleNewMessage = (message, isOffline = false) => {
  // 如果是离线消息，添加标记
  if (isOffline) {
    message.isOffline = true
  }
  
  // 为接收的消息添加成功状态
  if (!message.sendStatus) {
    message.sendStatus = 'success'
  }

  // 如果是当前会话的消息，添加到消息列表
  if (
    selectedConv.value &&
    (message.senderId === selectedConv.value.otherUserId ||
      message.receiverId === selectedConv.value.otherUserId)
  ) {
    // 防止重复消息：检查最近的消息中是否已有相同内容的消息
    const isDuplicate = messages.value.some(msg => {
      // 如果是同一个发送者在5秒内发送的相同内容，认为是重复消息
      const timeDiff = Math.abs(new Date(msg.createTime) - new Date(message.createTime))
      return msg.senderId === message.senderId && 
             msg.content === message.content && 
             timeDiff < 5000
    })
    
    if (!isDuplicate) {
      messages.value.push(message)
      scrollToBottom()

      // 如果是收到的消息，标记为已读
      if (!message.isSender && !isOffline) {
        markMessagesAsRead(message.senderId)
      }
    }
  }

  // 更新会话列表（除非是批量离线消息）
  if (!isOffline) {
    loadConversations()
  }
}

// 加载会话列表
const loadConversations = async () => {
  try {
    const res = await getConversationList()
    if (res.code === 200) {
      conversations.value = res.data || []
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

// 选择会话
const selectConversation = async (conv) => {
  selectedConv.value = conv
  await loadChatHistory(conv.otherUserId)
  await markMessagesAsRead(conv.otherUserId)
  await checkOnlineStatus(conv.otherUserId)
}

// 加载聊天记录
const loadChatHistory = async (otherUserId) => {
  try {
    const res = await getChatHistory(otherUserId, 1, 50)
    if (res.code === 200) {
      // 为历史消息添加成功状态
      messages.value = res.data.records.reverse().map(msg => ({
        ...msg,
        sendStatus: 'success' // 历史消息都是已成功的
      }))
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载聊天记录失败:', error)
  }
}

// 发送消息
const handleSendMessage = async () => {
  if (!inputMessage.value.trim() || !selectedConv.value) return

  const messageContent = inputMessage.value.trim()
  const receiverId = selectedConv.value.otherUserId
  
  // 立即在界面上显示发送的消息（乐观更新）
  const newMessage = {
    id: Date.now(), // 使用时间戳作为临时ID
    senderId: userStore.userInfo?.id,
    receiverId: receiverId,
    content: messageContent,
    messageType: 1,
    createTime: new Date().toISOString(),
    isSender: true,
    sendStatus: 'pending', // 发送状态：pending/success/failed
    errorMessage: ''
  }
  
  messages.value.push(newMessage)
  inputMessage.value = ''
  
  await nextTick()
  scrollToBottom()
  
  // 然后异步发送API请求
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

    if (res.code === 200) {
      // 发送成功，更新消息状态
      newMessage.sendStatus = 'success'
      // 更新会话列表
      loadConversations()
    } else {
      // 发送失败，更新消息状态
      newMessage.sendStatus = 'failed'
      newMessage.errorMessage = res.message || '发送失败'
      console.error('发送失败:', res.message)
    }
  } catch (error) {
    // 发送失败，更新消息状态
    newMessage.sendStatus = 'failed'
    newMessage.errorMessage = error.message || '网络错误，请重试'
    console.error('发送消息失败:', error)
  }
}

// 标记消息为已读
const markMessagesAsRead = async (otherUserId) => {
  try {
    await markAsRead(otherUserId)

    // 更新会话列表中的未读数
    const conv = conversations.value.find((c) => c.otherUserId === otherUserId)
    if (conv) {
      conv.unreadCount = 0
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 删除会话
const handleDeleteConversation = async () => {
  if (!selectedConv.value) return

  if (!confirm('确定要删除这个会话吗？')) return

  try {
    const res = await deleteConversation(selectedConv.value.id)
    if (res.code === 200) {
      // 从列表中移除
      conversations.value = conversations.value.filter((c) => c.id !== selectedConv.value.id)
      selectedConv.value = null
      messages.value = []
      alert('会话已删除')
    }
  } catch (error) {
    console.error('删除会话失败:', error)
    alert('删除会话失败，请重试')
  }
}

// 检查在线状态
const checkOnlineStatus = async (userId) => {
  try {
    const res = await getOnlineStatus(userId)
    if (res.code === 200) {
      currentUserOnline.value = res.data.isOnline
    }
  } catch (error) {
    console.error('获取在线状态失败:', error)
  }
}

// 获取在线用户数
const loadOnlineCount = async () => {
  try {
    const res = await getOnlineCount()
    if (res.code === 200) {
      onlineCount.value = res.data
    }
  } catch (error) {
    console.error('获取在线用户数失败:', error)
  }
}

// 判断用户是否在线
const isUserOnline = (userId) => {
  return onlineUsers.value.has(userId)
}

// 滚动到底部
const scrollToBottom = () => {
  setTimeout(() => {
    if (messageList.value) {
      messageList.value.scrollTop = messageList.value.scrollHeight
    }
  }, 100)
}

// 处理头像加载失败
const handleAvatarError = (e) => {
  // 防止无限循环
  if (e.target.src === defaultAvatar || e.target.src.startsWith('data:image/svg')) {
    e.target.onerror = null
    return
  }
  e.target.src = defaultAvatar
}

// 获取消息头像
const getMessageAvatar = (msg) => {
  if (msg.isSender) {
    return userStore.userInfo?.avatar || defaultAvatar
  } else {
    return selectedConv.value?.otherAvatar || defaultAvatar
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''

  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 小于1分钟
  if (diff < 60000) return '刚刚'

  // 小于1小时
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return `${minutes}分钟前`
  }

  // 小于1天
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }

  // 今年
  if (date.getFullYear() === now.getFullYear()) {
    const month = date.getMonth() + 1
    const day = date.getDate()
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    return `${month}-${day} ${hour}:${minute}`
  }

  // 往年
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${year}-${month}-${day}`
}

// 生命周期
onMounted(async () => {
  connectWebSocket()
  await loadConversations()
  loadOnlineCount()

  // 检查是否有URL参数传递的用户ID（从商品详情页跳转过来）
  const targetUserId = route.query.userId
  // 获取商品ID
  if (route.query.productId) {
    productId.value = parseInt(route.query.productId)
    console.log('从商品详情页跳转，商品ID:', productId.value)
  }

  if (targetUserId) {
    // 尝试在会话列表中查找该用户
    const existingConv = conversations.value.find(
      (conv) => conv.otherUserId === parseInt(targetUserId)
    )

    if (existingConv) {
      // 如果已有会话，直接选择
      await selectConversation(existingConv)
    } else {
      // 如果没有会话，创建临时会话对象
      const tempConv = {
        id: 0, // 临时ID
        otherUserId: parseInt(targetUserId),
        otherUsername: route.query.username || '用户',
        otherNickname: route.query.nickname || '',
        otherAvatar: route.query.avatar || defaultAvatar,
        lastMessage: '开始新会话',
        lastMessageTime: new Date().toISOString(),
        unreadCount: 0
      }

      selectedConv.value = tempConv
      messages.value = []
      await checkOnlineStatus(tempConv.otherUserId)
    }
  }

  // 定期刷新在线状态
  const onlineStatusInterval = setInterval(() => {
    if (selectedConv.value) {
      checkOnlineStatus(selectedConv.value.otherUserId)
    }
    loadOnlineCount()
  }, 30000) // 每30秒刷新一次

  // 保存定时器以便清理
  onUnmounted(() => {
    clearInterval(onlineStatusInterval)
    websocketService.removeMessageHandler(handleWebSocketMessage)
  })
})
</script>

<style scoped>
.messages-container {
  display: flex;
  height: calc(100vh - 60px);
  background: linear-gradient(135deg, #f5f7ff 0%, #e8f4fd 100%);
}

/* 会话列表样式 */
.conversation-list {
  width: 320px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(102, 126, 234, 0.1);
  box-shadow: 2px 0 20px rgba(102, 126, 234, 0.08);
  display: flex;
  flex-direction: column;
}

.list-header {
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.online-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.online-dot {
  width: 8px;
  height: 8px;
  background: #48bb78;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(72, 187, 120, 0.7);
  }
  70% {
    box-shadow: 0 0 0 4px rgba(72, 187, 120, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(72, 187, 120, 0);
  }
}

.conversation-items {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  padding: 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(102, 126, 234, 0.05);
  transition: all 0.3s ease;
  border-radius: 8px;
  margin: 0 8px 4px;
}

.conversation-item:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  transform: translateX(4px);
}

.conversation-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  border-left: 3px solid #667eea;
}

.avatar-wrapper {
  position: relative;
  margin-right: 12px;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.online-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  background-color: #67c23a;
  border: 2px solid white;
  border-radius: 50%;
}

.info {
  flex: 1;
  overflow: hidden;
  min-width: 0;
}

.username {
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 15px;
}

.last-message {
  color: #888;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  margin-left: 8px;
}

.time {
  color: #999;
  font-size: 12px;
}

.unread-badge {
  background-color: #f56c6c;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 12px;
  min-width: 18px;
  text-align: center;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
  color: #999;
}

/* 聊天窗口样式 */
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.chat-header {
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-info .username {
  font-size: 16px;
  font-weight: 600;
}

.online-status {
  font-size: 14px;
  color: #999;
}

.online-status.online {
  color: #67c23a;
}

.online-status::before {
  content: '●';
  margin-right: 4px;
}

.delete-btn {
  padding: 6px 16px;
  background-color: transparent;
  color: #f56c6c;
  border: 1px solid #f56c6c;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.delete-btn:hover {
  background-color: #f56c6c;
  color: white;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #fafafa;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  gap: 12px;
}

.message-item.is-sender {
  flex-direction: row-reverse;
}

.message-item .avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.message-content {
  max-width: 60%;
}

.message-text-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 6px;
}

.is-sender .message-text-wrapper {
  flex-direction: row-reverse;
}

.message-text {
  background: linear-gradient(135deg, #f7fafc 0%, #e2e8f0 100%);
  padding: 12px 16px;
  border-radius: 18px;
  word-wrap: break-word;
  font-size: 14px;
  line-height: 1.5;
  color: #2d3748;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.is-sender .message-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

/* 消息状态图标 */
.message-status {
  display: flex;
  align-items: center;
  font-size: 14px;
  flex-shrink: 0;
}

.status-pending {
  color: #909399;
  animation: rotate 1s linear infinite;
}

.status-success {
  color: #67c23a;
  font-weight: bold;
}

.status-failed {
  color: #f56c6c;
  cursor: help;
  font-weight: bold;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  padding: 0 4px;
}

.empty-messages {
  text-align: center;
  color: #999;
  padding: 40px 20px;
}

.message-input {
  border-top: 1px solid rgba(102, 126, 234, 0.1);
  padding: 16px 20px;
  display: flex;
  gap: 12px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
}

.message-input textarea {
  flex: 1;
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  padding: 12px 16px;
  resize: none;
  font-family: inherit;
  font-size: 14px;
  transition: all 0.3s ease;
  background: white;
  line-height: 1.4;
  min-height: 20px;
  max-height: 120px;
}

.message-input textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.message-input button {
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.message-input button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.message-input button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

/* 占位符样式 */
.chat-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: white;
}

.placeholder-content {
  text-align: center;
  color: #999;
}

.placeholder-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  opacity: 0.3;
}

.placeholder-content p {
  margin: 0;
  font-size: 16px;
}

/* 滚动条样式 */
.conversation-items::-webkit-scrollbar,
.message-list::-webkit-scrollbar {
  width: 6px;
}

.conversation-items::-webkit-scrollbar-thumb,
.message-list::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 3px;
}

.conversation-items::-webkit-scrollbar-thumb:hover,
.message-list::-webkit-scrollbar-thumb:hover {
  background-color: #ccc;
}
</style>
