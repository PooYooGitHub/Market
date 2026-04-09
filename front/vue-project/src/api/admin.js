// 管理员API服务
import request from '@/utils/request'

/**
 * 管理员仪表盘API
 */
export const dashboardApi = {
  // 获取仪表盘数据
  getDashboard() {
    return request.get('/analytics/admin/dashboard')
  },

  // 获取实时数据
  getRealtimeData() {
    return request.get('/system/admin/realtime-data')
  }
}

/**
 * 用户管理API
 */
export const userAdminApi = {
  // 获取用户列表
  getUserList(params) {
    return request.get('/user/admin/list', { params })
  },

  // 获取用户详情
  getUserDetail(id) {
    return request.get(`/user/admin/${id}`)
  },

  // 启用用户
  enableUser(id) {
    return request.put(`/user/admin/${id}/enable`)
  },

  // 禁用用户
  disableUser(id) {
    return request.put(`/user/admin/${id}/disable`)
  },

  // 删除用户
  deleteUser(id) {
    return request.delete(`/user/admin/${id}`)
  },

  // 批量删除用户
  batchDeleteUsers(ids) {
    return request.delete('/user/admin/batch', { data: ids })
  },

  // 重置密码
  resetPassword(id, newPassword) {
    return request.put(`/user/admin/${id}/reset-password`, null, {
      params: { newPassword }
    })
  },

  // 获取用户统计
  getUserStatistics() {
    return request.get('/user/admin/statistics')
  }
}

/**
 * 商品管理API
 */
export const productAdminApi = {
  // 获取商品列表
  getProductList(params) {
    return request.get('/product/admin/list', { params })
  },

  // 获取商品详情
  getProductDetail(id) {
    return request.get(`/product/admin/${id}`)
  },

  // 审核商品
  auditProduct(id, status, auditRemark) {
    return request.post(`/product/admin/${id}/audit`, null, {
      params: { status, auditRemark }
    })
  },

  // 批量审核商品
  batchAuditProducts(ids, status, auditRemark) {
    return request.post('/product/admin/batch-audit', ids, {
      params: { status, auditRemark }
    })
  },

  // 下架商品
  offlineProduct(id, reason) {
    return request.put(`/product/admin/${id}/offline`, null, {
      params: { reason }
    })
  },

  // 批量下架商品
  batchOfflineProducts(ids, reason) {
    return request.put('/product/admin/batch-offline', ids, {
      params: { reason }
    })
  },

  // 删除商品
  deleteProduct(id) {
    return request.delete(`/product/admin/${id}`)
  },

  // 批量删除商品
  batchDeleteProducts(ids) {
    return request.delete('/product/admin/batch', { data: ids })
  },

  // 获取商品统计
  getProductStatistics(startDate, endDate) {
    return request.get('/product/admin/statistics', {
      params: { startDate, endDate }
    })
  },

  // 获取分类统计
  getCategoryStatistics() {
    return request.get('/product/admin/category-statistics')
  },

  // 获取热门商品
  getHotProducts(limit) {
    return request.get('/product/admin/hot-products', {
      params: { limit }
    })
  },

  // 获取分类列表
  getCategories() {
    return request.get('/product/admin/categories')
  }
}

/**
 * 订单管理API
 */
export const orderAdminApi = {
  // 获取订单列表
  getOrderList(params) {
    return request.get('/order/admin/list', { params })
  },

  // 获取订单详情
  getOrderDetail(id) {
    return request.get(`/order/admin/${id}`)
  },

  // 根据订单号查询
  getOrderByNo(orderNo) {
    return request.get(`/order/admin/by-no/${orderNo}`)
  },

  // 取消订单
  cancelOrder(id, reason) {
    return request.put(`/order/admin/${id}/cancel`, null, {
      params: { reason }
    })
  },

  // 批量取消订单
  batchCancelOrders(ids, reason) {
    return request.put('/order/admin/batch-cancel', ids, {
      params: { reason }
    })
  },

  // 强制完成订单
  forceCompleteOrder(id, reason) {
    return request.put(`/order/admin/${id}/force-complete`, null, {
      params: { reason }
    })
  },

  // 处理退款
  processRefund(id, refundRequest) {
    return request.post(`/order/admin/${id}/refund`, refundRequest)
  },

  // 获取订单统计
  getOrderStatistics(startDate, endDate) {
    return request.get('/order/admin/statistics', {
      params: { startDate, endDate }
    })
  },

  // 获取交易分析
  getTransactionAnalysis(startDate, endDate, groupBy) {
    return request.get('/order/admin/analysis', {
      params: { startDate, endDate, groupBy }
    })
  },

  // 导出订单数据
  exportOrders(params) {
    return request.post('/order/admin/export', params)
  }
}

/**
 * 数据分析API
 */
export const analyticsApi = {
  // 获取用户分析报表
  getUserAnalysisReport(params) {
    return request.get('/analytics/admin/reports/user-analysis', { params })
  },

  // 获取商品分析报表
  getProductAnalysisReport(params) {
    return request.get('/analytics/admin/reports/product-analysis', { params })
  },

  // 获取交易分析报表
  getTradeAnalysisReport(params) {
    return request.get('/analytics/admin/reports/trade-analysis', { params })
  },

  // 获取财务分析报表
  getFinancialAnalysisReport(params) {
    return request.get('/analytics/admin/reports/financial-analysis', { params })
  },

  // 获取实时数据看板
  getRealtimeDashboard() {
    return request.get('/analytics/admin/realtime/dashboard')
  },

  // 导出报表
  exportReport(reportType, params, format) {
    return request.post(`/analytics/admin/export/${reportType}`, params, {
      params: { format }
    })
  }
}

/**
 * 内容审核API
 */
export const moderationApi = {
  // 获取审核任务列表
  getModerationTasks(params) {
    return request.get('/moderation/admin/tasks', { params })
  },

  // 获取审核任务详情
  getModerationTaskDetail(taskId) {
    return request.get(`/moderation/admin/task/${taskId}`)
  },

  // 处理审核任务
  processModerationTask(taskId, actionRequest) {
    return request.post(`/moderation/admin/task/${taskId}/action`, actionRequest)
  },

  // 批量处理审核任务
  batchProcessModerationTasks(taskIds, action, reason) {
    return request.post('/moderation/admin/tasks/batch-action', taskIds, {
      params: { action, reason }
    })
  },

  // 获取敏感词列表
  getSensitiveWords(pageNum, pageSize, keyword, category) {
    return request.get('/moderation/admin/sensitive-words', {
      params: { pageNum, pageSize, keyword, category }
    })
  },

  // 添加敏感词
  addSensitiveWord(word, category, description, level) {
    return request.post('/moderation/admin/sensitive-word', null, {
      params: { word, category, description, level }
    })
  },

  // 删除敏感词
  deleteSensitiveWord(wordId) {
    return request.delete(`/moderation/admin/sensitive-word/${wordId}`)
  },

  // 获取审核统计
  getModerationStatistics(startDate, endDate) {
    return request.get('/moderation/admin/statistics', {
      params: { startDate, endDate }
    })
  }
}

/**
 * 权限管理API
 */
export const permissionApi = {
  // 获取角色列表
  getRoles(pageNum, pageSize, keyword) {
    return request.get('/permission/admin/roles', {
      params: { pageNum, pageSize, keyword }
    })
  },

  // 获取所有角色
  getAllRoles() {
    return request.get('/permission/admin/roles/all')
  },

  // 创建角色
  createRole(roleData) {
    return request.post('/permission/admin/role', roleData)
  },

  // 更新角色
  updateRole(roleId, roleData) {
    return request.put(`/permission/admin/role/${roleId}`, roleData)
  },

  // 删除角色
  deleteRole(roleId) {
    return request.delete(`/permission/admin/role/${roleId}`)
  },

  // 获取权限树
  getPermissionTree() {
    return request.get('/permission/admin/permissions/tree')
  },

  // 获取权限列表
  getPermissions(pageNum, pageSize, keyword, type) {
    return request.get('/permission/admin/permissions', {
      params: { pageNum, pageSize, keyword, type }
    })
  },

  // 创建权限
  createPermission(permissionData) {
    return request.post('/permission/admin/permission', permissionData)
  },

  // 更新权限
  updatePermission(permissionId, permissionData) {
    return request.put(`/permission/admin/permission/${permissionId}`, permissionData)
  },

  // 获取用户角色
  getUserRoles(userId) {
    return request.get(`/permission/admin/user/${userId}/roles`)
  },

  // 分配用户角色
  assignUserRoles(userId, roleIds) {
    return request.post(`/permission/admin/user/${userId}/roles`, roleIds)
  }
}

/**
 * 系统管理API
 */
export const systemApi = {
  // 获取平台总览
  getPlatformOverview() {
    return request.get('/system/admin/overview')
  },

  // 系统健康检查
  systemHealthCheck() {
    return request.get('/system/admin/health-check')
  },

  // 获取服务状态
  getServiceStatus() {
    return request.get('/system/admin/service-status')
  },

  // 获取系统配置
  getSystemConfig() {
    return request.get('/system/admin/config')
  },

  // 更新系统配置
  updateSystemConfig(configData) {
    return request.put('/system/admin/config', configData)
  },

  // 清理缓存
  clearCache(cacheType) {
    return request.post('/system/admin/clear-cache', null, {
      params: { cacheType }
    })
  },

  // 系统备份
  systemBackup(backupType) {
    return request.post('/system/admin/backup', null, {
      params: { backupType }
    })
  },

  // 发送系统通知
  sendSystemNotification(notificationData) {
    return request.post('/system/admin/notification', notificationData)
  },

  // 获取系统日志
  getSystemLogs(pageNum, pageSize, level, startDate, endDate) {
    return request.get('/system/admin/logs', {
      params: { pageNum, pageSize, level, startDate, endDate }
    })
  },

  // 获取系统性能
  getSystemPerformance() {
    return request.get('/system/admin/performance')
  }
}

// 导出所有API
export default {
  dashboardApi,
  userAdminApi,
  productAdminApi,
  orderAdminApi,
  analyticsApi,
  moderationApi,
  permissionApi,
  systemApi
}