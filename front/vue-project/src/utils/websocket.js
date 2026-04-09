// WebSocket 连接管理
class WebSocketService {
  constructor() {
    this.ws = null
    this.userId = null
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.messageHandlers = []
    this.isManualClose = false
  }

  /**
   * 建立 WebSocket 连接
   * @param {number} userId - 用户ID
   */
  connect(userId) {
    if (!userId) {
      console.error('WebSocket 连接失败: 缺少用户ID')
      return
    }

    this.userId = userId
    this.isManualClose = false

    try {
      // 修改为正确的Message服务端口8103
      this.ws = new WebSocket(`ws://localhost:8103/ws/chat/${userId}`)

      this.ws.onopen = this.onOpen.bind(this)
      this.ws.onmessage = this.onMessage.bind(this)
      this.ws.onclose = this.onClose.bind(this)
      this.ws.onerror = this.onError.bind(this)
    } catch (error) {
      console.error('WebSocket 连接失败:', error)
      this.reconnect()
    }
  }

  /**
   * 连接打开
   */
  onOpen(event) {
    console.log('WebSocket 连接成功')
    this.reconnectAttempts = 0
    this.startHeartbeat()
  }

  /**
   * 接收消息
   */
  onMessage(event) {
    try {
      const message = JSON.parse(event.data)
      console.log('收到消息:', message)

      // 处理离线推送完成通知
      if (message.type === 'offline_push_complete' && message.count > 0) {
        console.log(`📮 收到离线消息通知: ${message.message}`)
        // 可以触发浏览器通知
        if ('Notification' in window && Notification.permission === 'granted') {
          new Notification('离线消息', {
            body: message.message,
            icon: '/vite.svg'
          })
        }
      }

      // 通知所有监听器
      this.messageHandlers.forEach(handler => {
        try {
          handler(message)
        } catch (error) {
          console.error('消息处理器执行失败:', error)
        }
      })
    } catch (error) {
      console.error('消息解析失败:', error)
    }
  }

  /**
   * 连接关闭
   */
  onClose(event) {
    console.log('WebSocket 连接关闭')
    this.stopHeartbeat()

    // 非主动关闭时自动重连
    if (!this.isManualClose) {
      this.reconnect()
    }
  }

  /**
   * 连接错误
   */
  onError(error) {
    console.error('WebSocket 错误:', error)
  }

  /**
   * 重连
   */
  reconnect() {
    if (this.isManualClose) {
      return
    }

    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('WebSocket 重连失败，已达最大重连次数')
      return
    }

    this.reconnectAttempts++
    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000)
    
    console.log(`WebSocket 将在 ${delay}ms 后重连 (第${this.reconnectAttempts}次)`)
    
    this.reconnectTimer = setTimeout(() => {
      this.connect(this.userId)
    }, delay)
  }

  /**
   * 开启心跳
   */
  startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'ping' }))
      }
    }, 30000) // 每30秒发送一次心跳
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 发送消息
   * @param {Object} message - 消息对象
   */
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    } else {
      console.error('WebSocket 未连接，无法发送消息')
    }
  }

  /**
   * 添加消息监听器
   * @param {Function} handler - 消息处理函数
   */
  addMessageHandler(handler) {
    if (typeof handler === 'function') {
      this.messageHandlers.push(handler)
    }
  }

  /**
   * 移除消息监听器
   * @param {Function} handler - 消息处理函数
   */
  removeMessageHandler(handler) {
    const index = this.messageHandlers.indexOf(handler)
    if (index > -1) {
      this.messageHandlers.splice(index, 1)
    }
  }

  /**
   * 关闭连接
   */
  close() {
    this.isManualClose = true
    this.stopHeartbeat()

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.ws) {
      this.ws.close()
      this.ws = null
    }

    this.messageHandlers = []
    console.log('WebSocket 连接已关闭')
  }

  /**
   * 获取连接状态
   */
  getReadyState() {
    return this.ws ? this.ws.readyState : WebSocket.CLOSED
  }

  /**
   * 是否已连接
   */
  isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN
  }
}

// 导出单例
export default new WebSocketService()
