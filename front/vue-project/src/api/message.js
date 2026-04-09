// 消息相关 API
import request from '@/utils/request'

/**
 * 发送消息
 * @param {Object} data - 消息信息
 * @param {number} data.receiverId - 接收者用户ID
 * @param {string} data.content - 消息内容
 * @param {number} data.messageType - 消息类型: 1-文字 2-图片
 * @param {number} [data.productId] - 关联商品ID
 */
export function sendMessage(data) {
  return request({
    url: '/api/message/send',
    method: 'post',
    data
  })
}

/**
 * 获取会话列表
 * @returns {Promise} 返回会话列表
 */
export function getConversationList() {
  return request({
    url: '/api/message/conversation/list',
    method: 'get'
  })
}

/**
 * 获取聊天记录
 * @param {number} otherUserId - 对方用户ID
 * @param {number} pageNum - 页码
 * @param {number} pageSize - 每页数量
 * @returns {Promise} 返回聊天记录分页数据
 */
export function getChatHistory(otherUserId, pageNum = 1, pageSize = 20) {
  return request({
    url: `/api/message/history/${otherUserId}`,
    method: 'get',
    params: {
      pageNum,
      pageSize
    }
  })
}

/**
 * 标记消息为已读
 * @param {number} otherUserId - 对方用户ID
 * @returns {Promise}
 */
export function markAsRead(otherUserId) {
  return request({
    url: `/api/message/read/${otherUserId}`,
    method: 'put'
  })
}

/**
 * 获取未读消息总数
 * @returns {Promise} 返回未读消息总数
 */
export function getUnreadCount() {
  return request({
    url: '/api/message/unread/count',
    method: 'get'
  })
}

/**
 * 删除会话
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 */
export function deleteConversation(conversationId) {
  return request({
    url: `/api/message/conversation/${conversationId}`,
    method: 'delete'
  })
}

/**
 * 获取用户在线状态
 * @param {number} userId - 用户ID
 * @returns {Promise} 返回在线状态
 */
export function getOnlineStatus(userId) {
  return request({
    url: `/api/message/online/${userId}`,
    method: 'get'
  })
}

/**
 * 获取在线用户数
 * @returns {Promise} 返回在线用户总数
 */
export function getOnlineCount() {
  return request({
    url: '/api/message/online/count',
    method: 'get'
  })
}
