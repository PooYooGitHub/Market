// 支付相关 API
import request from '@/utils/request'

// ==================== 支付接口 ====================

/**
 * 发起支付
 * @param {Object} data
 * @param {number} data.orderId - 订单ID
 * @param {string} data.paymentMethod - 支付方式：alipay/wechat/balance/bank
 * @param {number} [data.amount] - 支付金额（用于验证）
 */
export function createPayment(data) {
  return request({
    url: '/api/payment/create',
    method: 'post',
    data
  })
}

/**
 * 查询支付状态
 * @param {string} paymentId - 支付ID
 */
export function getPaymentStatus(paymentId) {
  return request({
    url: `/api/payment/status/${paymentId}`,
    method: 'get'
  })
}

/**
 * 模拟支付结果（仅用于毕业设计演示）
 * @param {string} paymentId - 支付ID
 * @param {string} result - 支付结果：success/failed/cancel
 */
export function simulatePaymentResult(paymentId, result = 'success') {
  return request({
    url: `/api/payment/simulate/${paymentId}`,
    method: 'post',
    data: { result }
  })
}

/**
 * 获取支付记录列表
 * @param {Object} params
 * @param {string} [params.status] - 支付状态：pending/success/failed/cancelled
 * @param {string} [params.paymentMethod] - 支付方式
 * @param {number} [params.pageNum]
 * @param {number} [params.pageSize]
 */
export function getPaymentHistory(params) {
  return request({
    url: '/api/payment/history',
    method: 'get',
    params
  })
}

/**
 * 获取用户余额
 */
export function getUserBalance() {
  return request({
    url: '/api/payment/balance',
    method: 'get'
  })
}

/**
 * 余额支付
 * @param {Object} data
 * @param {number} data.orderId - 订单ID
 * @param {number} data.amount - 支付金额
 * @param {string} [data.password] - 支付密码（模拟）
 */
export function payWithBalance(data) {
  return request({
    url: '/api/payment/balance/pay',
    method: 'post',
    data
  })
}

/**
 * 获取支付方式配置
 */
export function getPaymentMethods() {
  return request({
    url: '/api/payment/methods',
    method: 'get'
  })
}

/**
 * 申请退款
 * @param {Object} data
 * @param {number} data.orderId - 订单ID
 * @param {string} data.reason - 退款理由
 */
export function requestRefund(data) {
  return request({
    url: '/api/payment/refund',
    method: 'post',
    data
  })
}