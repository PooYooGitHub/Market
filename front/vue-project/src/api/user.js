// 用户相关 API
import request from '@/utils/request'

/**
 * 用户注册
 * @param {Object} data - 注册信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.confirmPassword - 确认密码
 * @param {string} [data.nickname] - 昵称
 * @param {string} [data.phone] - 手机号
 * @param {string} [data.email] - 邮箱
 */
export function register(data) {
  return request({
    url: '/api/user/register',  // 根据对接文档修正为正确路径
    method: 'post',
    data
  })
}

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise} 返回包含 token 和用户信息的对象
 */
export function login(data) {
  return request({
    url: '/api/user/login',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise} 返回当前登录用户信息
 */
export function getCurrentUser() {
  return request({
    url: '/api/user/info',
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {Object} data - 要更新的用户信息
 * @param {string} [data.nickname] - 昵称
 * @param {string} [data.avatar] - 头像URL
 * @param {string} [data.email] - 邮箱
 */
export function updateUser(data) {
  return request({
    url: '/api/user/update',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 * @param {Object} data - 密码信息
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 * @param {string} data.confirmPassword - 确认新密码
 */
export function changePassword(data) {
  return request({
    url: '/api/user/change-password',
    method: 'put',
    data
  })
}
