<template>
  <div class="arbitration-stats">
    <div class="header">
      <h2>仲裁数据统计</h2>
      <el-button :loading="loading" @click="loadStats">刷新数据</el-button>
    </div>

    <el-row :gutter="16" class="metric-row">
      <el-col :span="6">
        <el-card>
          <div class="metric-item">
            <div class="label">待处理</div>
            <div class="value pending">{{ stats.pendingCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="metric-item">
            <div class="label">处理中</div>
            <div class="value processing">{{ stats.processingCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="metric-item">
            <div class="label">已完结</div>
            <div class="value completed">{{ stats.completedCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="metric-item">
            <div class="label">驳回案件</div>
            <div class="value rejected">{{ stats.rejectedCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>案件状态分布</span>
          </template>
          <div class="status-chart">
            <div v-for="item in statusDistribution" :key="item.key" class="status-row">
              <div class="status-name">{{ item.label }}</div>
              <div class="status-bar-wrap">
                <div class="status-bar" :class="item.key" :style="{ width: item.percent + '%' }"></div>
              </div>
              <div class="status-value">{{ item.value }} ({{ item.percent }}%)</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>处理效率</span>
          </template>
          <div class="efficiency-panel">
            <el-progress
              type="dashboard"
              :percentage="completionRate"
              :stroke-width="14"
              status="success"
            >
              <template #default>
                <div class="rate-value">{{ completionRate }}%</div>
                <div class="rate-label">完结率</div>
              </template>
            </el-progress>

            <div class="efficiency-meta">
              <div class="meta-item">
                <span>今日新增</span>
                <strong>{{ stats.todayNewCount }}</strong>
              </div>
              <div class="meta-item">
                <span>平均处理天数</span>
                <strong>{{ stats.avgHandleDays }}</strong>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="trend-card">
      <template #header>
        <span>近7日完结趋势</span>
      </template>
      <div class="trend-chart">
        <div v-for="item in weeklyTrend" :key="item.date" class="trend-item">
          <div class="bar-wrap">
            <div class="bar" :style="{ height: item.height + 'px' }"></div>
          </div>
          <div class="count">{{ item.count }}</div>
          <div class="date">{{ item.date }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { arbitrationApi } from '@/api/arbitration'

const loading = ref(false)

const stats = ref({
  pendingCount: 0,
  processingCount: 0,
  completedCount: 0,
  rejectedCount: 0,
  todayNewCount: 0,
  avgHandleDays: 0
})

const weeklyTrend = ref([])

const completionRate = computed(() => {
  const total = stats.value.pendingCount + stats.value.processingCount + stats.value.completedCount + stats.value.rejectedCount
  if (!total) return 0
  return Math.round(((stats.value.completedCount + stats.value.rejectedCount) / total) * 100)
})

const statusDistribution = computed(() => {
  const total = stats.value.pendingCount + stats.value.processingCount + stats.value.completedCount + stats.value.rejectedCount
  const percent = (val) => {
    if (!total) return 0
    return Math.round((val / total) * 100)
  }

  return [
    { key: 'pending', label: '待处理', value: stats.value.pendingCount, percent: percent(stats.value.pendingCount) },
    { key: 'processing', label: '处理中', value: stats.value.processingCount, percent: percent(stats.value.processingCount) },
    { key: 'completed', label: '已完结', value: stats.value.completedCount, percent: percent(stats.value.completedCount) },
    { key: 'rejected', label: '已驳回', value: stats.value.rejectedCount, percent: percent(stats.value.rejectedCount) }
  ]
})

const loadStats = async () => {
  loading.value = true
  try {
    const [statsRes, casesRes] = await Promise.all([
      arbitrationApi.getArbitrationStats(),
      arbitrationApi.getAdminArbitrationList({ current: 1, size: 200 })
    ])

    const rawStats = statsRes?.data || {}
    stats.value = {
      pendingCount: Number(rawStats.pendingCount || 0),
      processingCount: Number(rawStats.processingCount || 0),
      completedCount: Number(rawStats.completedCount || 0),
      rejectedCount: Number(rawStats.rejectedCount || 0),
      todayNewCount: Number(rawStats.todayNewCount || 0),
      avgHandleDays: Number(rawStats.avgHandleDays || 0)
    }

    const records = casesRes?.data?.records || casesRes?.data?.list || []
    weeklyTrend.value = buildWeeklyTrend(records)
  } catch (error) {
    console.error('加载仲裁统计失败:', error)
    ElMessage.warning('统计接口异常，已展示模拟分析数据')
    setMockData()
  } finally {
    loading.value = false
  }
}

const buildWeeklyTrend = (records = []) => {
  const today = new Date()
  const dayMap = {}

  for (let i = 6; i >= 0; i -= 1) {
    const d = new Date(today)
    d.setDate(today.getDate() - i)
    const key = d.toISOString().slice(0, 10)
    dayMap[key] = 0
  }

  records.forEach(item => {
    const status = Number(item.status)
    if (status !== 2 && status !== 3) {
      return
    }

    const sourceTime = item.updateTime || item.finishTime || item.createTime
    if (!sourceTime) {
      return
    }

    const date = new Date(sourceTime)
    if (Number.isNaN(date.getTime())) {
      return
    }

    const key = date.toISOString().slice(0, 10)
    if (dayMap[key] !== undefined) {
      dayMap[key] += 1
    }
  })

  const maxCount = Math.max(...Object.values(dayMap), 1)
  return Object.entries(dayMap).map(([date, count]) => ({
    date: date.slice(5),
    count,
    height: Math.max(20, Math.round((count / maxCount) * 120))
  }))
}

const setMockData = () => {
  stats.value = {
    pendingCount: 12,
    processingCount: 8,
    completedCount: 25,
    rejectedCount: 6,
    todayNewCount: 4,
    avgHandleDays: 2.3
  }

  weeklyTrend.value = [
    { date: '04-01', count: 2, height: 40 },
    { date: '04-02', count: 3, height: 60 },
    { date: '04-03', count: 4, height: 80 },
    { date: '04-04', count: 5, height: 100 },
    { date: '04-05', count: 6, height: 120 },
    { date: '04-06', count: 4, height: 80 },
    { date: '04-07', count: 3, height: 60 }
  ]
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.arbitration-stats {
  padding: 20px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.metric-row {
  margin-bottom: 16px;
}

.metric-item {
  text-align: center;
}

.metric-item .label {
  color: #909399;
  margin-bottom: 8px;
}

.metric-item .value {
  font-size: 30px;
  font-weight: 700;
}

.metric-item .value.pending {
  color: #e6a23c;
}

.metric-item .value.processing {
  color: #409eff;
}

.metric-item .value.completed {
  color: #67c23a;
}

.metric-item .value.rejected {
  color: #f56c6c;
}

.status-chart {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-row {
  display: grid;
  grid-template-columns: 70px 1fr 100px;
  align-items: center;
  gap: 10px;
}

.status-name,
.status-value {
  font-size: 13px;
  color: #606266;
}

.status-bar-wrap {
  height: 10px;
  border-radius: 8px;
  background: #f0f2f5;
  overflow: hidden;
}

.status-bar {
  height: 100%;
  border-radius: 8px;
}

.status-bar.pending {
  background: #e6a23c;
}

.status-bar.processing {
  background: #409eff;
}

.status-bar.completed {
  background: #67c23a;
}

.status-bar.rejected {
  background: #f56c6c;
}

.efficiency-panel {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 30px;
  min-height: 230px;
}

.rate-value {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.rate-label {
  margin-top: 2px;
  color: #909399;
  font-size: 12px;
}

.efficiency-meta {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  width: 180px;
  color: #606266;
}

.meta-item strong {
  color: #303133;
}

.trend-card {
  margin-top: 16px;
}

.trend-chart {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 220px;
  padding: 20px 10px 10px;
}

.trend-item {
  width: 48px;
  text-align: center;
}

.bar-wrap {
  height: 130px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.bar {
  width: 24px;
  background: linear-gradient(180deg, #79bbff 0%, #409eff 100%);
  border-radius: 6px 6px 0 0;
}

.count {
  font-size: 12px;
  color: #303133;
  margin-top: 6px;
}

.date {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>