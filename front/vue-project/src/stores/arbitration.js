import { defineStore } from 'pinia'
import { ElNotification } from 'element-plus'
import { arbitrationApi } from '@/api/arbitration'

let notificationSeed = 1

const toNumber = (value, fallback = 0) => {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

export const useArbitrationStore = defineStore('arbitration', {
  state: () => ({
    // 管理员端统计
    stats: {
      pendingCount: 0,
      processingCount: 0,
      completedCount: 0,
      rejectedCount: 0,
      todayNewCount: 0,
      avgHandleDays: 0
    },
    
    // 用户端状态
    userArbitrations: [],
    userStats: {
      totalCount: 0,
      pendingCount: 0,
      processingCount: 0,
      completedCount: 0,
      rejectedCount: 0,
      successRate: 0
    },
    userInitialized: false,
    userPollTimer: null,
    userPolling: false,
    
    loading: false,
    polling: false,
    pollIntervalMs: 30000,
    pollTimer: null,
    initialized: false,
    notifications: []
  }),

  getters: {
    unreadCount: (state) => state.notifications.filter(item => !item.read).length,
    latestNotifications: (state) => state.notifications.slice(0, 20),
    
    // 用户端 getters
    hasActiveArbitration: (state) => {
      return state.userArbitrations.some(item => 
        item.status === 0 || item.status === 1 || item.status === 4
      )
    },
    userPendingCount: (state) => state.userStats.pendingCount,
    userProcessingCount: (state) => state.userStats.processingCount
  },

  actions: {
    normalizeStats(rawStats = {}) {
      return {
        pendingCount: toNumber(rawStats.pendingCount),
        processingCount: toNumber(rawStats.processingCount),
        completedCount: toNumber(rawStats.completedCount),
        rejectedCount: toNumber(rawStats.rejectedCount),
        todayNewCount: toNumber(rawStats.todayNewCount),
        avgHandleDays: toNumber(rawStats.avgHandleDays)
      }
    },

    async fetchStats() {
      const response = await arbitrationApi.getArbitrationStats()
      return this.normalizeStats(response?.data || {})
    },

    pushNotification(notification) {
      this.notifications.unshift({
        id: notificationSeed++,
        title: notification.title,
        content: notification.content,
        time: new Date(),
        read: false
      })

      if (this.notifications.length > 50) {
        this.notifications = this.notifications.slice(0, 50)
      }
    },

    async refreshStats(options = {}) {
      const { silent = false, notifyDiff = true } = options

      if (!silent) {
        this.loading = true
      }

      try {
        const previous = { ...this.stats }
        const nextStats = await this.fetchStats()
        this.stats = nextStats

        if (this.initialized && notifyDiff) {
          const pendingDiff = nextStats.pendingCount - previous.pendingCount
          if (pendingDiff > 0) {
            const message = `新增 ${pendingDiff} 个待处理仲裁案件，请及时处理。`
            this.pushNotification({
              title: '仲裁待办更新',
              content: message
            })
            ElNotification({
              title: '仲裁通知',
              message,
              type: 'warning',
              duration: 3500
            })
          }
        }

        this.initialized = true
        return this.stats
      } catch (error) {
        console.error('刷新仲裁统计失败:', error)
        return this.stats
      } finally {
        this.loading = false
      }
    },

    startPolling(options = {}) {
      const { immediate = true } = options
      if (this.polling) {
        return
      }

      this.polling = true

      if (immediate) {
        this.refreshStats({ silent: true, notifyDiff: false })
      }

      this.pollTimer = setInterval(() => {
        this.refreshStats({ silent: true, notifyDiff: true })
      }, this.pollIntervalMs)
    },

    stopPolling() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer)
        this.pollTimer = null
      }
      this.polling = false
    },

    markAsRead(notificationId) {
      const target = this.notifications.find(item => item.id === notificationId)
      if (target) {
        target.read = true
      }
    },

    markAllAsRead() {
      this.notifications.forEach(item => {
        item.read = true
      })
    },

    // ============ 用户端方法 ============

    /**
     * 加载用户仲裁列表
     */
    async loadUserArbitrations() {
      try {
        const response = await arbitrationApi.getUserArbitrationList({
          current: 1,
          size: 100
        })
        
        const data = response?.data || {}
        this.userArbitrations = data.records || data.list || []
        this.updateUserStatsFromList()
        
        return this.userArbitrations
      } catch (error) {
        console.error('加载用户仲裁列表失败:', error)
        // 如果是认证错误，停止轮询
        if (error.message && error.message.includes('登录')) {
          console.warn('认证失败，停止用户仲裁轮询')
          this.stopUserPolling()
        }
        return []
      }
    },

    /**
     * 从列表数据更新用户统计
     */
    updateUserStatsFromList() {
      const list = this.userArbitrations
      this.userStats.totalCount = list.length
      this.userStats.pendingCount = list.filter(item => item.status === 0).length
      this.userStats.processingCount = list.filter(item => item.status === 1 || item.status === 4).length
      this.userStats.completedCount = list.filter(item => item.status === 2).length
      this.userStats.rejectedCount = list.filter(item => item.status === 3).length
      
      const resolved = this.userStats.completedCount + this.userStats.rejectedCount
      this.userStats.successRate = resolved > 0 
        ? Math.round((this.userStats.completedCount / resolved) * 100) 
        : 0
    },

    /**
     * 检查用户仲裁状态变化并通知
     */
    async checkUserStatusChanges() {
      const previousList = [...this.userArbitrations]
      await this.loadUserArbitrations()
      
      for (const current of this.userArbitrations) {
        const previous = previousList.find(item => item.id === current.id)
        
        if (previous && previous.status !== current.status) {
          this.notifyUserStatusChange(current, previous.status)
        }
      }
    },

    /**
     * 通知用户状态变化
     */
    notifyUserStatusChange(arbitration, previousStatus) {
      const statusNames = {
        0: '待处理',
        1: '处理中',
        4: '待补证',
        2: '已完结',
        3: '已驳回'
      }
      
      let title = '仲裁状态更新'
      let message = ''
      let type = 'info'
      
      const caseNumber = arbitration.caseNumber || `#${arbitration.id}`
      
      if (arbitration.status === 1) {
        title = '仲裁已受理'
        message = `您的仲裁申请 ${caseNumber} 已被受理，正在处理中`
        type = 'success'
      } else if (arbitration.status === 4) {
        title = '仲裁待补证'
        message = `您的仲裁申请 ${caseNumber} 正在等待补充证据，请尽快在时限内提交`
        type = 'warning'
      } else if (arbitration.status === 2) {
        title = '仲裁已完结'
        message = `您的仲裁申请 ${caseNumber} 已处理完成，请查看裁决结果`
        type = 'success'
      } else if (arbitration.status === 3) {
        title = '仲裁已驳回'
        message = `您的仲裁申请 ${caseNumber} 已被驳回，请查看驳回原因`
        type = 'warning'
      } else {
        const prevName = statusNames[previousStatus] || '未知'
        const currName = statusNames[arbitration.status] || '未知'
        message = `仲裁 ${caseNumber} 状态从"${prevName}"变更为"${currName}"`
      }
      
      this.pushNotification({ title, content: message })
      
      ElNotification({
        title,
        message,
        type,
        duration: 5000,
        position: 'top-right'
      })
    },

    /**
     * 开始用户端轮询
     */
    startUserPolling(options = {}) {
      const { immediate = true } = options
      if (this.userPolling) return
      
      this.userPolling = true
      
      if (immediate) {
        this.loadUserArbitrations()
      }
      
      this.userPollTimer = setInterval(() => {
        this.checkUserStatusChanges()
      }, this.pollIntervalMs)
      
      console.log('用户仲裁状态轮询已启动')
    },

    /**
     * 停止用户端轮询
     */
    stopUserPolling() {
      if (this.userPollTimer) {
        clearInterval(this.userPollTimer)
        this.userPollTimer = null
      }
      this.userPolling = false
      console.log('用户仲裁状态轮询已停止')
    },

    /**
     * 重置所有状态
     */
    reset() {
      this.stopPolling()
      this.stopUserPolling()
      
      this.stats = {
        pendingCount: 0,
        processingCount: 0,
        completedCount: 0,
        rejectedCount: 0,
        todayNewCount: 0,
        avgHandleDays: 0
      }
      
      this.userArbitrations = []
      this.userStats = {
        totalCount: 0,
        pendingCount: 0,
        processingCount: 0,
        completedCount: 0,
        rejectedCount: 0,
        successRate: 0
      }
      
      this.notifications = []
      this.initialized = false
      this.userInitialized = false
      this.loading = false
    }
  }
})
