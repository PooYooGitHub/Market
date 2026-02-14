// 用户状态管理
import { defineStore } from 'pinia'
import { login, getCurrentUser } from '@/api/user'
import router from '@/router'

export const useUserStore = defineStore('user', {
  // 状态
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),

  // 计算属性
  getters: {
    // 是否已登录
    isLoggedIn: (state) => !!state.token,
    
    // 用户名
    username: (state) => state.userInfo?.username || '',
    
    // 昵称
    nickname: (state) => state.userInfo?.nickname || '',
    
    // 头像
    avatar: (state) => state.userInfo?.avatar || '',
    
    // 用户ID
    userId: (state) => state.userInfo?.id || null
  },

  // 方法
  actions: {
    /**
     * 用户登录
     * @param {Object} loginData - 登录信息
     * @param {string} loginData.username - 用户名
     * @param {string} loginData.password - 密码
     */
    async login(loginData) {
      try {
        const res = await login(loginData)
        
        if (res.code === 200) {
          // 保存 token
          this.token = res.data.token
          localStorage.setItem('token', res.data.token)
          
          // 保存用户信息
          this.userInfo = res.data.userInfo
          localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo))
          
          return { success: true, message: '登录成功' }
        } else {
          return { success: false, message: res.message }
        }
      } catch (error) {
        console.error('登录失败:', error)
        return { success: false, message: '登录失败，请稍后重试' }
      }
    },

    /**
     * 用户登出
     */
    logout() {
      // 清除状态
      this.token = ''
      this.userInfo = null
      
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      
      // 跳转到登录页
      router.push('/login')
    },

    /**
     * 获取用户信息
     */
    async getUserInfo() {
      try {
        const res = await getCurrentUser()
        
        if (res.code === 200) {
          this.userInfo = res.data
          localStorage.setItem('userInfo', JSON.stringify(res.data))
          return { success: true, data: res.data }
        } else {
          return { success: false, message: res.message }
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        return { success: false, message: '获取用户信息失败' }
      }
    },

    /**
     * 更新本地用户信息
     * @param {Object} newInfo - 新的用户信息
     */
    updateUserInfo(newInfo) {
      this.userInfo = { ...this.userInfo, ...newInfo }
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    }
  }
})
