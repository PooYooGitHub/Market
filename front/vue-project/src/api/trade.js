// 交易相关 API
import request from '@/utils/request'

/**
 * 通过订单号获取订单详情
 * @param {string} orderNo - 订单号
 */
export function getOrderByOrderNo(orderNo) {
  return request({ url: `/feign/trade/order/no/${orderNo}`, method: 'get' })
}

// ==================== 购物车 ====================

/**
 * 添加商品到购物车
 * @param {Object} data
 * @param {number} data.productId - 商品ID
 * @param {number} data.quantity  - 数量
 */
export function addToCart(data) {
  return request({ url: '/api/trade/cart/add', method: 'post', data })
}

/** 获取购物车列表 */
export function getCartList() {
  return request({ url: '/api/trade/cart/list', method: 'get' })
}

/**
 * 修改购物车商品数量
 * @param {number} cartId
 * @param {number} quantity
 */
export function updateCartQuantity(cartId, quantity) {
  return request({ url: `/api/trade/cart/quantity/${cartId}`, method: 'put', params: { quantity } })
}

/**
 * 删除购物车商品
 * @param {number} cartId
 */
export function removeFromCart(cartId) {
  return request({ url: `/api/trade/cart/${cartId}`, method: 'delete' })
}

/**
 * 批量删除购物车商品
 * @param {Array<number>} cartIds
 */
export function removeMultipleFromCart(cartIds) {
  return request({ url: '/api/trade/cart/batch', method: 'delete', data: cartIds })
}

/**
 * 更新购物车商品选中状态
 * @param {number} cartId
 * @param {boolean} selected
 */
export function updateCartSelected(cartId, selected) {
  return request({ url: `/api/trade/cart/selected/${cartId}`, method: 'put', params: { selected } })
}

/**
 * 全选/取消全选
 * @param {boolean} selected
 */
export function selectAllCart(selected) {
  return request({ url: '/api/trade/cart/selectAll', method: 'put', params: { selected } })
}

/** 清空购物车 */
export function clearCart() {
  return request({ url: '/api/trade/cart/clear', method: 'delete' })
}

/** 获取购物车选中数量 */
export function getCartSelectedCount() {
  return request({ url: '/api/trade/cart/selectedCount', method: 'get' })
}

// ==================== 订单 ====================

/**
 * 创建订单
 * @param {Object} data
 * @param {number} data.productId - 商品ID
 * @param {number} [data.addressId] - 收货地址ID（可选，不传则使用默认地址）
 */
export function createOrder(data) {
  return request({ url: '/api/trade/order/create', method: 'post', data })
}

/**
 * 取消订单
 * @param {number} orderId
 */
export function cancelOrder(orderId) {
  return request({ url: `/api/trade/order/cancel/${orderId}`, method: 'post' })
}

/**
 * 支付订单
 * @param {number} orderId
 */
export function payOrder(orderId) {
  return request({ url: `/api/trade/order/pay/${orderId}`, method: 'post' })
}

/**
 * 确认发货（卖家）
 * @param {number} orderId
 */
export function shipOrder(orderId) {
  return request({ url: `/api/trade/order/ship/${orderId}`, method: 'post' })
}

/**
 * 确认收货（买家）
 * @param {number} orderId
 */
export function receiveOrder(orderId) {
  return request({ url: `/api/trade/order/receive/${orderId}`, method: 'post' })
}

/**
 * 获取订单详情
 * @param {number} orderId
 */
export function getOrderDetail(orderId) {
  return request({ url: `/api/trade/order/${orderId}`, method: 'get' })
}

/**
 * 我的买入订单列表（买家视角）
 * @param {Object} params
 * @param {number} [params.status]   - 订单状态
 * @param {string} [params.orderNo]  - 订单号
 * @param {number} [params.pageNum]
 * @param {number} [params.pageSize]
 */
export function getMyOrders(params) {
  return request({ url: '/api/trade/order/my', method: 'get', params })
}

/**
 * 我的卖出订单列表（卖家视角）
 * @param {Object} params
 */
export function getMySalesOrders(params) {
  return request({ url: '/api/trade/order/sales', method: 'get', params })
}

