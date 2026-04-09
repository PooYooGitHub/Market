// 用户状态管理
import { defineStore } from 'pinia'
import { login, getCurrentUser } from '@/api/user'

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
    // 登录
    async login(loginForm) {
      try {
        const response = await login(loginForm)

        if (response.code === 200 && response.data?.token) {
          this.token = response.data.token
          this.userInfo = response.data.userInfo || {}

          // 保存到本地存储
          localStorage.setItem('token', this.token)
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))

          return { success: true, data: response.data }
        } else {
          throw new Error(response.message || '登录失败')
        }
      } catch (error) {
        console.error('登录失败：', error.message)
        throw error
      }
    },

    // 获取用户信息
    async fetchUserInfo() {
      try {
        const response = await getCurrentUser()

        if (response.code === 200) {
          this.userInfo = response.data
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
          return this.userInfo
        } else {
          throw new Error(response.message || '获取用户信息失败')
        }
      } catch (error) {
        console.error('获取用户信息失败：', error.message)
        // 如果获取失败，清除本地存储
        this.logout()
        throw error
      }
    },

    // 登出
    logout() {
      this.token = ''
      this.userInfo = null

      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')

      return true
    },

    // 更新用户信息
    updateUserInfo(userInfo) {
      this.userInfo = { ...this.userInfo, ...userInfo }
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },

    // 初始化用户状态（从localStorage恢复）
    initUserState() {
      const token = localStorage.getItem('token')
      const userInfo = localStorage.getItem('userInfo')

      if (token) {
        this.token = token
      }

      if (userInfo) {
        try {
          this.userInfo = JSON.parse(userInfo)
        } catch (error) {
          console.error('解析用户信息失败：', error)
          localStorage.removeItem('userInfo')
        }
      }
    }
  }
})