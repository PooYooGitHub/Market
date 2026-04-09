// 信用评价相关 API
import request from '@/utils/request'

/**
 * 创建评价
 * @param {Object} data - 评价信息
 * @param {number} data.orderId - 订单ID
 * @param {number} data.targetId - 被评价人ID
 * @param {number} data.score - 评分 (1-5)
 * @param {string} data.content - 评价内容
 */
export function createEvaluation(data) {
  return request({
    url: '/api/evaluation',
    method: 'post',
    data
  })
}

/**
 * 获取用户信用信息
 * @param {number} userId - 用户ID (可选，不传则获取当前用户)
 */
export function getCreditInfo(userId) {
  const url = userId ? `/api/credit/${userId}` : '/api/credit/my'
  return request({
    url,
    method: 'get'
  })
}

/**
 * 获取收到的评价列表
 * @param {Object} params - 查询参数
 * @param {number} params.userId - 用户ID (可选)
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 页大小
 */
export function getReceivedEvaluations(params = {}) {
  return request({
    url: '/api/evaluation/received',
    method: 'get',
    params: {
      pageNum: 1,
      pageSize: 10,
      ...params
    }
  })
}

/**
 * 获取给出的评价列表
 * @param {Object} params - 查询参数
 * @param {number} params.userId - 用户ID (可选)
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 页大小
 */
export function getGivenEvaluations(params = {}) {
  return request({
    url: '/api/evaluation/given',
    method: 'get',
    params: {
      pageNum: 1,
      pageSize: 10,
      ...params
    }
  })
}

/**
 * 检查订单评价状态
 * @param {number} orderId - 订单ID
 */
export function checkEvaluationStatus(orderId) {
  return request({
    url: '/api/evaluation/status',
    method: 'get',
    params: { orderId }
  })
}

/**
 * 初始化用户信用分
 * @param {number} userId - 用户ID
 */
export function initUserCredit(userId) {
  return request({
    url: `/api/credit/init/${userId}`,
    method: 'post'
  })
}

/**
 * 重新计算用户信用分
 * @param {number} userId - 用户ID
 */
export function recalculateUserCredit(userId) {
  return request({
    url: `/api/credit/recalculate/${userId}`,
    method: 'post'
  })
}