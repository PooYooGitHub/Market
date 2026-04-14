import request from '@/utils/request'

export const arbitrationApi = {
  createArbitration(data) {
    return request({
      url: '/api/arbitration/submit',
      method: 'post',
      data
    })
  },

  updateArbitration(id, data) {
    return request({
      url: `/api/arbitration/update/${id}`,
      method: 'put',
      data
    })
  },

  getUserArbitrationList(params) {
    return request({
      url: '/api/arbitration/my',
      method: 'get',
      params
    })
  },

  getArbitrationDetail(id) {
    return request({
      url: `/api/arbitration/detail/${id}`,
      method: 'get'
    })
  },

  getMyArbitrationByOrderId(orderId) {
    return request({
      url: `/api/arbitration/my/order/${orderId}`,
      method: 'get'
    })
  },

  cancelArbitration(id) {
    return request({
      url: `/api/arbitration/cancel/${id}`,
      method: 'post'
    })
  },

  getUserArbitrationStats() {
    return request({
      url: '/api/arbitration/stats/user',
      method: 'get'
    })
  },

  getArbitrationLogs(arbitrationId) {
    return request({
      url: `/api/arbitration/logs/${arbitrationId}`,
      method: 'get'
    })
  },

  getAdminArbitrationList(params) {
    return request({
      url: '/api/arbitration/admin/list',
      method: 'get',
      params
    })
  },

  getAdminArbitrationDetail(id) {
    return request({
      url: `/api/arbitration/admin/detail/${id}`,
      method: 'get'
    })
  },

  acceptArbitration(id) {
    return request({
      url: `/api/arbitration/admin/accept/${id}`,
      method: 'post'
    })
  },

  handleArbitration(data) {
    return request({
      url: '/api/arbitration/admin/handle',
      method: 'post',
      data
    })
  },

  rejectArbitration(id, reason) {
    return request({
      url: `/api/arbitration/admin/reject/${id}`,
      method: 'post',
      params: { reason }
    })
  },

  requestSupplement(data) {
    return request({
      url: '/api/arbitration/admin/supplement/request',
      method: 'post',
      data
    })
  },

  expireSupplement(requestId) {
    return request({
      url: `/api/arbitration/admin/supplement/expire/${requestId}`,
      method: 'post'
    })
  },

  submitSupplement(data) {
    return request({
      url: '/api/arbitration/supplement/submit',
      method: 'post',
      data
    })
  },

  getArbitrationStats() {
    return request({
      url: '/api/arbitration/admin/stats',
      method: 'get'
    })
  }
}
