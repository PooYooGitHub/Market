import request from '@/utils/request'

/**
 * 仲裁相关 API
 */
export const arbitrationApi = {
  /**
   * 创建仲裁申请
   */
  createArbitration(data) {
    return request({
      url: '/api/arbitration/submit',
      method: 'post',
      data
    })
  },

  /**
   * 修改仲裁申请
   */
  updateArbitration(id, data) {
    return request({
      url: `/api/arbitration/update/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 查询当前用户仲裁列表
   */
  getUserArbitrationList(params) {
    return request({
      url: '/api/arbitration/my',
      method: 'get',
      params
    })
  },

  /**
   * 查询仲裁详情
   */
  getArbitrationDetail(id) {
    return request({
      url: `/api/arbitration/detail/${id}`,
      method: 'get'
    })
  },

  /**
   * 根据订单ID查询当前用户仲裁申请
   */
  getMyArbitrationByOrderId(orderId) {
    return request({
      url: `/api/arbitration/my/order/${orderId}`,
      method: 'get'
    })
  },

  /**
   * 取消仲裁申请
   */
  cancelArbitration(id) {
    return request({
      url: `/api/arbitration/cancel/${id}`,
      method: 'post'
    })
  },

  /**
   * 查询用户仲裁统计
   */
  getUserArbitrationStats() {
    return request({
      url: '/api/arbitration/stats/user',
      method: 'get'
    })
  },

  /**
   * 查询仲裁日志
   */
  getArbitrationLogs(arbitrationId) {
    return request({
      url: `/api/arbitration/logs/${arbitrationId}`,
      method: 'get'
    })
  },

  /**
   * 管理端仲裁列表
   */
  getAdminArbitrationList(params) {
    return request({
      url: '/api/arbitration/admin/list',
      method: 'get',
      params
    })
  },

  /**
   * 受理仲裁
   */
  acceptArbitration(id) {
    return request({
      url: `/api/arbitration/admin/accept/${id}`,
      method: 'post'
    })
  },

  /**
   * 处理仲裁
   */
  handleArbitration(data) {
    return request({
      url: '/api/arbitration/admin/handle',
      method: 'post',
      data
    })
  },

  /**
   * 驳回仲裁
   */
  rejectArbitration(id, reason) {
    return request({
      url: `/api/arbitration/admin/reject/${id}`,
      method: 'post',
      params: { reason }
    })
  },

  /**
   * 管理端仲裁统计
   */
  getArbitrationStats() {
    return request({
      url: '/api/arbitration/admin/stats',
      method: 'get'
    })
  }
}
