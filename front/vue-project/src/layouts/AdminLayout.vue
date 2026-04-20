<!-- 管理系统主布局 -->
<template>
  <div class="admin-layout">
    <!-- 头部导航 -->
    <el-header class="admin-header">
      <div class="header-left">
        <el-icon @click="toggleSidebar" class="menu-toggle">
          <Expand v-if="isCollapse" />
          <Fold v-else />
        </el-icon>
        <h2 class="system-title">仲裁管理系统</h2>
      </div>

      <div class="header-right">
        <!-- 消息通知 -->
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
          <el-icon class="notification-icon" @click="showNotifications">
            <Bell />
          </el-icon>
        </el-badge>

        <!-- 用户信息 -->
        <el-dropdown @command="handleCommand">
          <div class="user-info">
            <el-avatar :src="userInfo.avatar" :size="32">
              <el-icon><User /></el-icon>
            </el-avatar>
            <span class="username">{{ userInfo.nickname }}</span>
            <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="settings">系统设置</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="sidebarWidth" class="admin-sidebar">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          router
          background-color="#2c3e50"
          text-color="#ecf0f1"
          active-text-color="#3498db"
        >
          <!-- 仲裁管理 -->
          <el-sub-menu index="arbitration">
            <template #title>
              <el-icon><Management /></el-icon>
              <span>仲裁管理</span>
            </template>
            <el-menu-item index="/admin/arbitration/pending">
              <el-icon><Clock /></el-icon>
              <template #title>待处理</template>
            </el-menu-item>
            <el-menu-item index="/admin/arbitration/processing">
              <el-icon><Loading /></el-icon>
              <template #title>处理中</template>
            </el-menu-item>
            <el-menu-item index="/admin/arbitration/completed">
              <el-icon><CircleCheck /></el-icon>
              <template #title>已完成</template>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="admin-main">
        <!-- 面包屑导航 -->
        <el-breadcrumb class="breadcrumb" separator-icon="ArrowRight">
          <el-breadcrumb-item :to="{ path: '/admin/arbitration/pending' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="{ path: item.path }">
            {{ item.name }}
          </el-breadcrumb-item>
        </el-breadcrumb>

        <!-- 路由视图 -->
        <div class="content-container">
          <router-view />
        </div>
      </el-main>
    </el-container>

    <!-- 通知抽屉 -->
    <el-drawer
      v-model="notificationDrawer"
      title="系统通知"
      size="400px"
      direction="rtl"
    >
      <div class="notification-list">
        <div v-for="notification in notifications" :key="notification.id" class="notification-item">
          <div class="notification-header">
            <span class="notification-title">{{ notification.title }}</span>
            <span class="notification-time">{{ formatTime(notification.time) }}</span>
          </div>
          <div class="notification-content">{{ notification.content }}</div>
          <div class="notification-actions">
            <el-button size="small" @click="markAsRead(notification.id)">标记已读</el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import {
  Bell, ArrowDown, Expand, Fold,
  Clock, Loading, CircleCheck,
  User, Management
} from '@element-plus/icons-vue'
import { useArbitrationStore } from '@/stores/arbitration'

const router = useRouter()
const route = useRoute()
const arbitrationStore = useArbitrationStore()
const { unreadCount, latestNotifications } = storeToRefs(arbitrationStore)

// 响应式数据
const isCollapse = ref(false)
const notificationDrawer = ref(false)
const userInfo = ref({ nickname: '管理员', avatar: '' })
const notifications = computed(() => latestNotifications.value)

// 计算属性
const sidebarWidth = computed(() => isCollapse.value ? '64px' : '200px')
const activeMenu = computed(() => route.path)
const breadcrumbs = computed(() => {
  const paths = route.path.split('/').filter(Boolean)
  const breadcrumbItems = []

  const routeNames = {
    'admin': '管理系统',
    'users': '用户管理',
    'products': '商品管理',
    'arbitration': '仲裁管理',
    'pending': '待处理',
    'processing': '处理中',
    'completed': '已完成'
  }

  for (let i = 1; i < paths.length; i++) {
    const path = '/' + paths.slice(0, i + 1).join('/')
    const name = routeNames[paths[i]] || paths[i]
    breadcrumbItems.push({ path, name })
  }

  return breadcrumbItems
})

// 方法
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

const showNotifications = () => {
  notificationDrawer.value = true
}

const handleCommand = (command) => {
  switch (command) {
    case 'logout':
      // 正确的退出登录逻辑
      localStorage.removeItem('adminToken')
      localStorage.removeItem('adminInfo')
      ElMessage.success('已退出登录')
      router.push('/admin/login').catch(err => {
        console.error('退出登录跳转失败:', err)
        window.location.replace('/admin/login')
      })
      break
  }
}

const hasPermission = (permission) => {
  // TODO: 实现权限检查
  return true
}

const markAsRead = (notificationId) => {
  arbitrationStore.markAsRead(notificationId)
}

const formatTime = (time) => {
  if (!time) return '-'
  return formatDistanceToNow(time, {
    addSuffix: true,
    locale: zhCN
  })
}

onMounted(() => {
  arbitrationStore.startPolling({ immediate: true })
})

onBeforeUnmount(() => {
  arbitrationStore.stopPolling()
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  background-color: #f5f5f5;
}

.admin-header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.menu-toggle {
  font-size: 18px;
  cursor: pointer;
  color: #606266;
  transition: color 0.3s;
}

.menu-toggle:hover {
  color: #3498db;
}

.system-title {
  color: #2c3e50;
  margin: 0;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
}

.admin-sidebar {
  background-color: #2c3e50;
  transition: width 0.3s;
}

.admin-main {
  padding: 20px;
  background-color: #f5f5f5;
}

.breadcrumb {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 4px;
}

.content-container {
  background: #fff;
  border-radius: 4px;
  min-height: calc(100vh - 160px);
}
</style>
