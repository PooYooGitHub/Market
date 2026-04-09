<template>
  <div class="admin-layout">
    <!-- 顶部导航栏 -->
    <el-header class="admin-header">
      <div class="header-left">
        <div class="logo">
          <el-icon size="28" color="#409EFF">
            <Monitor />
          </el-icon>
          <h2>仲裁管理系统</h2>
        </div>
      </div>

      <div class="header-right">
        <el-space>
          <!-- 通知铃铛 -->
          <el-badge :value="notificationCount" :hidden="notificationCount === 0">
            <el-button circle @click="showNotifications">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>

          <!-- 用户信息 -->
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userInfo.avatar">
                <el-icon v-if="!userInfo.avatar"><UserFilled /></el-icon>
              </el-avatar>
              <span class="username">{{ userInfo.name || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人设置
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-space>
      </div>
    </el-header>

    <!-- 主体内容 -->
    <el-container class="admin-container">
      <!-- 侧边栏 -->
      <el-aside :width="collapsed ? '64px' : '240px'" class="admin-aside">
        <div class="aside-header">
          <el-button
            @click="toggleCollapse"
            circle
            size="small"
            type="primary"
            class="collapse-btn"
          >
            <el-icon>
              <component :is="collapsed ? 'Expand' : 'Fold'" />
            </el-icon>
          </el-button>
        </div>

        <el-scrollbar class="aside-scrollbar">
          <el-menu
            :default-active="$route.path"
            :collapse="collapsed"
            :unique-opened="true"
            router
            class="admin-menu"
          >
            <!-- 工作台 -->
            <el-menu-item index="/admin/dashboard">
              <el-icon><Odometer /></el-icon>
              <template #title>工作台</template>
            </el-menu-item>

            <!-- 仲裁管理 -->
            <el-sub-menu index="/admin/arbitration">
              <template #title>
                <el-icon><Document /></el-icon>
                <span>仲裁管理</span>
              </template>
              <el-menu-item index="/admin/cases">
                <el-icon><Files /></el-icon>
                <template #title>案件管理</template>
              </el-menu-item>
              <el-menu-item index="/admin/arbitration">
                <el-icon><List /></el-icon>
                <template #title>申请列表</template>
              </el-menu-item>
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
                <template #title>已完结</template>
              </el-menu-item>
            </el-sub-menu>

            <!-- 数据统计 -->
            <el-sub-menu index="/admin/statistics">
              <template #title>
                <el-icon><TrendCharts /></el-icon>
                <span>数据统计</span>
              </template>
              <el-menu-item index="/admin/statistics/overview">
                <el-icon><DataAnalysis /></el-icon>
                <template #title>数据概览</template>
              </el-menu-item>
              <el-menu-item index="/admin/statistics/reports">
                <el-icon><Document /></el-icon>
                <template #title>统计报表</template>
              </el-menu-item>
            </el-sub-menu>

            <!-- 用户管理 -->
            <el-menu-item index="/admin/users">
              <el-icon><UserFilled /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>

            <!-- 系统设置 -->
            <el-sub-menu index="/admin/settings">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>系统设置</span>
              </template>
              <el-menu-item index="/admin/settings/arbitration">
                <el-icon><Tools /></el-icon>
                <template #title>仲裁配置</template>
              </el-menu-item>
              <el-menu-item index="/admin/settings/system">
                <el-icon><Cpu /></el-icon>
                <template #title>系统配置</template>
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <!-- 主要内容区 -->
      <el-main class="admin-main">
        <div class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </el-main>
    </el-container>

    <!-- 通知抽屉 -->
    <el-drawer
      v-model="notificationDrawer"
      title="通知消息"
      :size="400"
      direction="rtl"
    >
      <div class="notification-list">
        <div v-for="item in notifications" :key="item.id" class="notification-item">
          <div class="notification-content">
            <div class="notification-title">{{ item.title }}</div>
            <div class="notification-desc">{{ item.content }}</div>
            <div class="notification-time">{{ formatTime(item.createTime) }}</div>
          </div>
          <el-button size="small" @click="markAsRead(item.id)">标记已读</el-button>
        </div>
        <el-empty v-if="notifications.length === 0" description="暂无通知" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Monitor, Bell, UserFilled, User, Lock, SwitchButton, ArrowDown,
  Expand, Fold, Odometer, Document, List, Clock, Loading, CircleCheck,
  TrendCharts, DataAnalysis, Setting, Tools, Cpu, Files
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format } from 'date-fns'

const router = useRouter()

// 响应式数据
const collapsed = ref(false)
const notificationDrawer = ref(false)
const notifications = ref([])

// 用户信息
const userInfo = ref({
  name: '系统管理员',
  role: 'admin',
  avatar: ''
})

// 计算属性
const notificationCount = computed(() => {
  return notifications.value.filter(item => !item.read).length
})

// 切换侧边栏折叠
const toggleCollapse = () => {
  collapsed.value = !collapsed.value
}

// 显示通知
const showNotifications = () => {
  notificationDrawer.value = true
  loadNotifications()
}

// 加载通知数据
const loadNotifications = () => {
  // 模拟通知数据
  notifications.value = [
    {
      id: 1,
      title: '新的仲裁申请',
      content: '用户张三提交了订单相关的仲裁申请，请及时处理',
      createTime: new Date(),
      read: false
    },
    {
      id: 2,
      title: '仲裁申请超时提醒',
      content: '有3个仲裁申请已超过24小时未处理，请关注',
      createTime: new Date(Date.now() - 3600000),
      read: false
    }
  ]
}

// 标记已读
const markAsRead = (id) => {
  const item = notifications.value.find(item => item.id === id)
  if (item) {
    item.read = true
  }
}

// 处理用户操作
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人设置功能待实现')
      break
    case 'password':
      ElMessage.info('修改密码功能待实现')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确认退出登录？', '退出确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 清理本地数据
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminInfo')

    ElMessage.success('已退出登录')
    router.push('/admin/login')

  } catch {
    // 用户取消
  }
}

// 格式化时间
const formatTime = (time) => {
  return format(new Date(time), 'MM-dd HH:mm')
}

// 页面初始化
onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 头部样式 */
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-left .logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left .logo h2 {
  margin: 0;
  color: #409EFF;
  font-size: 20px;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #606266;
}

/* 主容器样式 */
.admin-container {
  flex: 1;
  overflow: hidden;
}

/* 侧边栏样式 */
.admin-aside {
  background: #304156;
  transition: width 0.3s ease;
  overflow: hidden;
}

.aside-header {
  padding: 12px;
  text-align: center;
}

.collapse-btn {
  background: rgba(255, 255, 255, 0.1);
  border: none;
}

.aside-scrollbar {
  height: calc(100vh - 120px);
}

.admin-menu {
  border: none;
  background: transparent;
}

.admin-menu .el-menu-item,
.admin-menu .el-sub-menu__title {
  color: #bfcbd9 !important;
}

.admin-menu .el-menu-item:hover,
.admin-menu .el-sub-menu__title:hover {
  background: rgba(255, 255, 255, 0.1) !important;
  color: #fff !important;
}

.admin-menu .el-menu-item.is-active {
  background: #409EFF !important;
  color: #fff !important;
}

/* 主内容区样式 */
.admin-main {
  background: #f0f2f5;
  padding: 0;
  overflow-y: auto;
}

.main-content {
  padding: 20px;
  min-height: 100%;
}

/* 页面切换动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 通知样式 */
.notification-list {
  padding: 16px 0;
}

.notification-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
}

.notification-content {
  flex: 1;
  margin-right: 12px;
}

.notification-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.notification-desc {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 8px;
}

.notification-time {
  color: #909399;
  font-size: 12px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .admin-header {
    padding: 0 12px;
  }

  .header-left .logo h2 {
    display: none;
  }

  .main-content {
    padding: 12px;
  }

  .admin-aside {
    position: fixed;
    z-index: 1000;
    height: 100vh;
  }

  .admin-main {
    margin-left: 64px;
  }
}
</style>