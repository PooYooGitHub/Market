<!-- 仲裁管理仪表盘 -->
<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <h1>仲裁管理工作台</h1>
      <p>专业的争议解决与数据分析系统</p>
    </div>

    <!-- 关键指标卡片 -->
    <el-row :gutter="20" class="metrics-row">
      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-content">
            <div class="metric-icon pending">
              <el-icon :size="24"><Clock /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ keyMetrics.pendingCases }}</div>
              <div class="metric-label">待处理案件</div>
              <div class="metric-change urgent">需要关注</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-content">
            <div class="metric-icon processing">
              <el-icon :size="24"><Loading /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ keyMetrics.processingCases }}</div>
              <div class="metric-label">处理中案件</div>
              <div class="metric-change stable">进行中</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-content">
            <div class="metric-icon completed">
              <el-icon :size="24"><CircleCheck /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ keyMetrics.completedToday }}</div>
              <div class="metric-label">今日完成</div>
              <div class="metric-change increase">↗ +15%</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-content">
            <div class="metric-icon efficiency">
              <el-icon :size="24"><Timer /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ keyMetrics.avgProcessDays }}</div>
              <div class="metric-label">平均处理天数</div>
              <div class="metric-change decrease">↘ -0.5天</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="content-row">
      <!-- 待办事项 -->
      <el-col :span="12">
        <el-card class="todo-card">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-button type="text" @click="viewAllPending">查看全部</el-button>
            </div>
          </template>
          <div class="todo-list">
            <div class="todo-item" v-for="item in todoList" :key="item.id">
              <div class="todo-content">
                <div class="todo-title">{{ item.title }}</div>
                <div class="todo-meta">
                  <span class="case-number">{{ item.caseNumber }}</span>
                  <span class="waiting-time">等待{{ item.waitingDays }}天</span>
                </div>
              </div>
              <div class="todo-actions">
                <el-button size="small" type="primary" @click="handleCase(item)">
                  处理
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 处理统计 -->
      <el-col :span="12">
        <el-card class="stats-card">
          <template #header>
            <div class="card-header">
              <span>近期处理统计</span>
              <el-button type="text" @click="viewDetailedStats">详细统计</el-button>
            </div>
          </template>
          <div class="stats-chart">
            <!-- 简单的图表占位符 -->
            <div class="chart-placeholder">
              <div class="chart-item" v-for="day in weeklyStats" :key="day.date">
                <div class="chart-bar" :style="{ height: day.height }"></div>
                <div class="chart-label">{{ day.date }}</div>
              </div>
            </div>
          </div>
          <div class="stats-summary">
            <div class="summary-item">
              <span class="summary-label">本周完成：</span>
              <span class="summary-value">{{ weeklyCompleted }}件</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">处理效率：</span>
              <span class="summary-value good">优秀</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" class="quick-actions-row">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button
              v-for="action in quickActions"
              :key="action.key"
              :type="action.type"
              @click="handleQuickAction(action)"
              size="large"
            >
              <el-icon><component :is="action.icon" /></el-icon>
              {{ action.label }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Clock, Loading, CircleCheck, Timer,
  Management, DataAnalysis, Document
} from '@element-plus/icons-vue'
import { arbitrationApi } from '@/api/arbitration'
import { dashboardApi } from '@/api/admin'

const router = useRouter()

// 关键指标数据
const keyMetrics = reactive({
  pendingCases: 0,
  processingCases: 0,
  completedToday: 0,
  avgProcessDays: 0
})

// 待办事项列表
const todoList = ref([])

// 每周统计数据
const weeklyStats = ref([])

const weeklyCompleted = ref(0)

// 加载状态
const loading = ref(false)

// 快捷操作
const quickActions = ref([
  {
    key: 'pending',
    label: '处理待办',
    type: 'danger',
    icon: Clock,
    path: '/admin/arbitration/pending'
  },
  {
    key: 'processing',
    label: '跟进进展',
    type: 'warning',
    icon: Loading,
    path: '/admin/arbitration/processing'
  },
  {
    key: 'stats',
    label: '数据分析',
    type: 'primary',
    icon: DataAnalysis,
    path: '/admin/statistics/overview'
  },
  {
    key: 'reports',
    label: '导出报表',
    type: 'success',
    icon: Document,
    path: '/admin/statistics/reports'
  }
])

// 方法
const viewAllPending = () => {
  router.push('/admin/arbitration/pending')
}

const viewDetailedStats = () => {
  router.push('/admin/statistics/overview')
}

const handleCase = (caseItem) => {
  router.push(`/admin/arbitration/pending`)
}

const handleQuickAction = (action) => {
  if (action.path) {
    router.push(action.path)
  }
}

// 生命周期
onMounted(() => {
  loadDashboardData()
})

// 加载仪表盘数据
const loadDashboardData = async () => {
  loading.value = true
  try {
    // 并行加载多个数据源
    const [statsResponse, dashboardResponse, pendingCasesResponse] = await Promise.all([
      arbitrationApi.getArbitrationStats(),
      dashboardApi.getDashboard(),
      arbitrationApi.getAdminArbitrationList({ status: 0, current: 1, size: 5 })
    ])

    // 更新关键指标
    if (statsResponse.code === 200) {
      const stats = statsResponse.data
      keyMetrics.pendingCases = stats.pendingCount || 0
      keyMetrics.processingCases = stats.processingCount || 0
      keyMetrics.completedToday = stats.todayNewCount || 0
      keyMetrics.avgProcessDays = stats.avgHandleDays || 0
    }

    // 更新待办事项
    const pendingRecords = pendingCasesResponse?.data?.records || pendingCasesResponse?.data?.list || []
    if (pendingCasesResponse.code === 200 && pendingRecords.length) {
      todoList.value = pendingRecords.map(item => ({
        id: item.id,
        title: item.title || item.reason,
        caseNumber: item.caseNumber,
        waitingDays: calculateWaitingDays(item.createTime),
        priority: item.priority || 'normal',
        orderId: item.orderId
      }))
    }

    // 更新周统计数据
    if (statsResponse.data?.weeklyStats) {
      const weekData = statsResponse.data.weeklyStats
      const maxCount = Math.max(...weekData.map(d => d.count))

      weeklyStats.value = weekData.map(item => ({
        date: formatWeekday(item.date),
        height: maxCount > 0 ? `${(item.count / maxCount) * 100}%` : '0%',
        count: item.count
      }))

      weeklyCompleted.value = weekData.reduce((sum, item) => sum + item.count, 0)
    } else {
      // 如果没有真实数据，使用默认数据
      setDefaultData()
    }

  } catch (error) {
    console.error('加载仪表盘数据失败：', error)
    ElMessage.error('加载数据失败，将显示默认数据')
    setDefaultData()
  } finally {
    loading.value = false
  }
}

// 设置默认数据
const setDefaultData = () => {
  keyMetrics.pendingCases = 8
  keyMetrics.processingCases = 5
  keyMetrics.completedToday = 3
  keyMetrics.avgProcessDays = 2.5

  todoList.value = [
    {
      id: 1,
      title: '电子产品质量争议',
      caseNumber: 'ARB2024001',
      waitingDays: 2,
      priority: 'high'
    },
    {
      id: 2,
      title: '二手图书交易纠纷',
      caseNumber: 'ARB2024002',
      waitingDays: 1,
      priority: 'normal'
    }
  ]

  weeklyStats.value = [
    { date: '周一', height: '40%', count: 2 },
    { date: '周二', height: '60%', count: 3 },
    { date: '周三', height: '80%', count: 4 },
    { date: '周四', height: '60%', count: 3 },
    { date: '周五', height: '100%', count: 5 },
    { date: '周六', height: '20%', count: 1 },
    { date: '周日', height: '40%', count: 2 }
  ]
  weeklyCompleted.value = 20
}

// 计算等待天数
const calculateWaitingDays = (createTime) => {
  if (!createTime) return 0
  const now = new Date()
  const created = new Date(createTime)
  const diffTime = Math.abs(now - created)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays
}

// 格式化星期
const formatWeekday = (date) => {
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  if (typeof date === 'string' && date.includes('周')) {
    return date
  }
  const d = new Date(date)
  return weekdays[d.getDay()]
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.welcome-section {
  margin-bottom: 30px;
  text-align: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px;
  border-radius: 12px;
}

.welcome-section h1 {
  margin: 0 0 10px 0;
  font-size: 2.5rem;
  font-weight: 600;
}

.welcome-section p {
  margin: 0;
  font-size: 1.1rem;
  opacity: 0.9;
}

.metrics-row {
  margin-bottom: 30px;
}

.metric-card {
  height: 120px;
  border-radius: 12px;
  transition: all 0.3s;
}

.metric-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.metric-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 20px;
}

.metric-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  color: white;
}

.metric-icon.pending {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.metric-icon.processing {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.metric-icon.completed {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.metric-icon.efficiency {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.metric-info {
  flex: 1;
}

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.metric-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.metric-change {
  font-size: 12px;
}

.metric-change.urgent {
  color: #F56C6C;
  font-weight: 600;
}

.metric-change.stable {
  color: #E6A23C;
}

.metric-change.increase {
  color: #67C23A;
}

.metric-change.decrease {
  color: #67C23A;
}

.content-row {
  margin-bottom: 30px;
}

.todo-card,
.stats-card {
  height: 400px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.todo-list {
  max-height: 300px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 5px;
}

.todo-meta {
  font-size: 12px;
  color: #909399;
}

.case-number {
  margin-right: 15px;
}

.waiting-time {
  color: #F56C6C;
}

.stats-chart {
  padding: 20px 0;
}

.chart-placeholder {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 150px;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
}

.chart-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.chart-bar {
  width: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px 4px 0 0;
  min-height: 20px;
}

.chart-label {
  font-size: 12px;
  color: #909399;
}

.stats-summary {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.summary-item {
  text-align: center;
}

.summary-label {
  font-size: 12px;
  color: #909399;
  margin-right: 5px;
}

.summary-value {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.summary-value.good {
  color: #67C23A;
}

.quick-actions-row {
  margin-bottom: 20px;
}

.quick-actions {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.quick-actions .el-button {
  flex: 1;
  min-width: 150px;
  height: 60px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
</style>
