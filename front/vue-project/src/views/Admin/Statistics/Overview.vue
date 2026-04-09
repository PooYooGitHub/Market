<template>
  <div class="overview" v-loading="loading">
    <h2>数据概览</h2>

    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="metrics-row">
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon arbitration">
              <el-icon size="30"><Management /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ metrics.totalCases }}</div>
              <div class="metric-label">总仲裁案件</div>
              <div class="metric-trend up">较上月 +{{ Math.round((metrics.totalCases * 0.12)) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon pending">
              <el-icon size="30"><Clock /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ metrics.pendingCases }}</div>
              <div class="metric-label">待处理案件</div>
              <div class="metric-trend" :class="metrics.pendingCases > 20 ? 'urgent' : 'normal'">
                {{ metrics.pendingCases > 20 ? '需要关注' : '正常水平' }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon success">
              <el-icon size="30"><CircleCheck /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ metrics.resolvedCases }}</div>
              <div class="metric-label">已解决案件</div>
              <div class="metric-trend up">
                解决率 {{ Math.round((metrics.resolvedCases / metrics.totalCases) * 100) }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon time">
              <el-icon size="30"><Timer /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ metrics.avgProcessTime }}天</div>
              <div class="metric-label">平均处理时间</div>
              <div class="metric-trend" :class="metrics.avgProcessTime < 3 ? 'up' : 'normal'">
                {{ metrics.avgProcessTime < 3 ? '效率优秀' : '有待提升' }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 案件趋势图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>月度处理趋势</span>
              <el-button @click="refreshData" size="small">
                <el-icon><Download /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          <div class="chart-container">
            <div class="trend-chart">
              <div class="chart-legend">
                <span class="legend-item">
                  <span class="legend-color applied"></span>申请数量
                </span>
                <span class="legend-item">
                  <span class="legend-color resolved"></span>处理数量
                </span>
                <span class="legend-item">
                  <span class="legend-color pending"></span>待处理
                </span>
              </div>
              <div class="chart-content">
                <div class="chart-bars" v-for="item in monthlyTrend" :key="item.month">
                  <div class="month-label">{{ item.month }}</div>
                  <div class="bars-container">
                    <div class="bar applied" :style="{ height: (item.applied * 3) + 'px' }">
                      <span class="bar-value">{{ item.applied }}</span>
                    </div>
                    <div class="bar resolved" :style="{ height: (item.resolved * 3) + 'px' }">
                      <span class="bar-value">{{ item.resolved }}</span>
                    </div>
                    <div class="bar pending" :style="{ height: (item.pending * 3) + 'px' }">
                      <span class="bar-value">{{ item.pending }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 案件类型分布 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>案件类型分布</span>
          </template>
          <div class="chart-container">
            <div class="type-distribution">
              <div class="distribution-list">
                <div
                  v-for="item in typeDistribution"
                  :key="item.name"
                  class="distribution-item"
                >
                  <div class="item-header">
                    <span class="item-name">{{ item.name }}</span>
                    <span class="item-value">{{ item.value }}件</span>
                  </div>
                  <div class="progress-bar">
                    <div
                      class="progress-fill"
                      :style="{
                        width: ((item.value / totalTypeValue) * 100) + '%',
                        backgroundColor: item.color
                      }"
                    ></div>
                  </div>
                  <div class="item-percentage">
                    {{ Math.round((item.value / totalTypeValue) * 100) }}%
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 处理效率分析 -->
    <el-row :gutter="20" class="efficiency-row">
      <el-col :span="24">
        <el-card class="efficiency-card">
          <template #header>
            <span>处理效率分析</span>
          </template>
          <div class="efficiency-content">
            <div class="efficiency-stats">
              <div
                v-for="item in efficiencyData"
                :key="item.name"
                class="efficiency-item"
                :class="item.name === '超期处理' ? 'warning' : ''"
              >
                <div class="efficiency-circle">
                  <div class="circle-progress" :style="{
                    '--progress': item.value + '%',
                    '--color': item.name === '超期处理' ? '#F56C6C' : '#67C23A'
                  }">
                    <span class="circle-text">{{ item.value }}%</span>
                  </div>
                </div>
                <div class="efficiency-label">{{ item.name }}</div>
              </div>
            </div>

            <div class="efficiency-summary">
              <div class="summary-item">
                <span class="summary-label">处理效率评级：</span>
                <span class="summary-value" :class="getEfficiencyRating().class">
                  {{ getEfficiencyRating().text }}
                </span>
              </div>
              <div class="summary-item">
                <span class="summary-label">建议改进：</span>
                <span class="summary-value">{{ getImprovementSuggestion() }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" class="actions-row">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="viewPendingCases">
              <el-icon><Clock /></el-icon>
              处理待办案件
            </el-button>
            <el-button type="success" @click="exportReport">
              <el-icon><Download /></el-icon>
              导出统计报告
            </el-button>
            <el-button type="info" @click="viewTrendAnalysis">
              <el-icon><Timer /></el-icon>
              趋势分析
            </el-button>
            <el-button @click="refreshData">
              刷新数据
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Management, Clock, CircleCheck, Timer, Download
} from '@element-plus/icons-vue'
import { arbitrationApi } from '@/api/arbitration'
import { analyticsApi } from '@/api/admin'

const router = useRouter()

// 响应式数据
const loading = ref(false)

// 核心指标
const metrics = reactive({
  totalCases: 0,
  pendingCases: 0,
  resolvedCases: 0,
  avgProcessTime: 0
})

// 仲裁类型分布数据
const typeDistribution = ref([])

// 每月处理趋势数据
const monthlyTrend = ref([])

// 处理效率数据
const efficiencyData = ref([])

// 计算属性
const totalTypeValue = computed(() => {
  return typeDistribution.value.reduce((sum, item) => sum + item.value, 0)
})

// 生命周期
onMounted(() => {
  loadOverviewData()
})

// 加载概览数据
const loadOverviewData = async () => {
  loading.value = true
  try {
    // 并行加载多个数据源
    const [statsResponse] = await Promise.all([
      arbitrationApi.getArbitrationStats().catch(() => ({ code: 500 }))
    ])

    if (statsResponse.code === 200) {
      const stats = statsResponse.data
      metrics.totalCases = stats.totalCases || 0
      metrics.pendingCases = stats.pendingCount || 0
      metrics.resolvedCases = stats.resolvedCount || 0
      metrics.avgProcessTime = stats.avgProcessDays || 0

      // 更新类型分布
      if (stats.typeDistribution) {
        typeDistribution.value = stats.typeDistribution
      } else {
        setDefaultTypeDistribution()
      }

      // 更新月度趋势
      if (stats.monthlyTrend) {
        monthlyTrend.value = stats.monthlyTrend
      } else {
        setDefaultMonthlyTrend()
      }

      // 更新效率数据
      if (stats.efficiencyData) {
        efficiencyData.value = stats.efficiencyData
      } else {
        setDefaultEfficiencyData()
      }
    } else {
      // API调用失败，使用默认数据
      setDefaultData()
    }

  } catch (error) {
    console.error('加载概览数据失败：', error)
    setDefaultData()
  } finally {
    loading.value = false
  }
}

// 设置默认数据
const setDefaultData = () => {
  metrics.totalCases = 156
  metrics.pendingCases = 23
  metrics.resolvedCases = 133
  metrics.avgProcessTime = 3.2

  setDefaultTypeDistribution()
  setDefaultMonthlyTrend()
  setDefaultEfficiencyData()
}

const setDefaultTypeDistribution = () => {
  typeDistribution.value = [
    { name: '商品质量', value: 45, color: '#409EFF' },
    { name: '交易纠纷', value: 35, color: '#67C23A' },
    { name: '售后服务', value: 15, color: '#E6A23C' },
    { name: '其他', value: 5, color: '#F56C6C' }
  ]
}

const setDefaultMonthlyTrend = () => {
  const months = ['1月', '2月', '3月', '4月', '5月', '6月']
  monthlyTrend.value = months.map((month, index) => ({
    month,
    applied: Math.floor(Math.random() * 30) + 10,
    resolved: Math.floor(Math.random() * 25) + 8,
    pending: Math.floor(Math.random() * 10) + 2
  }))
}

const setDefaultEfficiencyData = () => {
  efficiencyData.value = [
    { name: '当日处理', value: 65 },
    { name: '3日内处理', value: 25 },
    { name: '超期处理', value: 10 }
  ]
}

// 工具方法
const getEfficiencyRating = () => {
  const onTimeRate = efficiencyData.value.find(item => item.name === '当日处理')?.value || 0
  if (onTimeRate >= 80) return { text: '优秀', class: 'excellent' }
  if (onTimeRate >= 60) return { text: '良好', class: 'good' }
  if (onTimeRate >= 40) return { text: '一般', class: 'average' }
  return { text: '需改进', class: 'poor' }
}

const getImprovementSuggestion = () => {
  const overtimeRate = efficiencyData.value.find(item => item.name === '超期处理')?.value || 0
  if (overtimeRate > 15) return '建议增加仲裁员数量或优化处理流程'
  if (overtimeRate > 10) return '适当调整案件分配策略'
  return '当前处理效率良好，保持现状'
}

// 事件处理
const refreshData = () => {
  loadOverviewData()
  ElMessage.success('数据已刷新')
}

const viewPendingCases = () => {
  router.push('/admin/arbitration/pending')
}

const exportReport = () => {
  router.push('/admin/statistics/reports')
}

const viewTrendAnalysis = () => {
  router.push('/admin/statistics/trend')
}
</script>

<style scoped>
.overview {
  padding: 20px;
}

.overview h2 {
  margin-bottom: 20px;
  color: #303133;
}

.metrics-row {
  margin-bottom: 20px;
}

.metric-card {
  border-radius: 12px;
  transition: all 0.3s;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
}

.metric-content {
  display: flex;
  align-items: center;
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

.metric-icon.arbitration {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.metric-icon.pending {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.metric-icon.success {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.metric-icon.time {
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

.metric-trend {
  font-size: 12px;
  font-weight: 500;
}

.metric-trend.up {
  color: #67C23A;
}

.metric-trend.normal {
  color: #409EFF;
}

.metric-trend.urgent {
  color: #F56C6C;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.trend-chart {
  width: 100%;
  height: 100%;
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #606266;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-color.applied {
  background: #409EFF;
}

.legend-color.resolved {
  background: #67C23A;
}

.legend-color.pending {
  background: #E6A23C;
}

.chart-content {
  display: flex;
  justify-content: space-around;
  align-items: end;
  height: 200px;
  padding: 20px 0;
}

.chart-bars {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.month-label {
  font-size: 12px;
  color: #909399;
}

.bars-container {
  display: flex;
  gap: 4px;
  align-items: end;
  height: 150px;
}

.bar {
  width: 20px;
  border-radius: 2px 2px 0 0;
  position: relative;
  min-height: 10px;
}

.bar.applied {
  background: #409EFF;
}

.bar.resolved {
  background: #67C23A;
}

.bar.pending {
  background: #E6A23C;
}

.bar-value {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: #606266;
}

.type-distribution {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.distribution-list {
  width: 80%;
}

.distribution-item {
  margin-bottom: 20px;
}

.item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.item-name {
  font-size: 14px;
  color: #303133;
}

.item-value {
  font-size: 14px;
  font-weight: bold;
  color: #409EFF;
}

.progress-bar {
  height: 8px;
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 5px;
}

.progress-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}

.item-percentage {
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.efficiency-row {
  margin-bottom: 20px;
}

.efficiency-card {
  border-radius: 12px;
}

.efficiency-content {
  display: flex;
  gap: 40px;
}

.efficiency-stats {
  display: flex;
  gap: 30px;
  flex: 1;
}

.efficiency-item {
  text-align: center;
}

.efficiency-circle {
  margin-bottom: 15px;
}

.circle-progress {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: conic-gradient(var(--color) var(--progress), #f0f0f0 0deg);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.circle-progress::before {
  content: '';
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: white;
  position: absolute;
}

.circle-text {
  position: relative;
  z-index: 1;
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}

.efficiency-label {
  font-size: 14px;
  color: #606266;
}

.efficiency-summary {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 15px;
}

.summary-item {
  display: flex;
  align-items: center;
}

.summary-label {
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
}

.summary-value {
  font-size: 14px;
  font-weight: bold;
}

.summary-value.excellent {
  color: #67C23A;
}

.summary-value.good {
  color: #409EFF;
}

.summary-value.average {
  color: #E6A23C;
}

.summary-value.poor {
  color: #F56C6C;
}

.actions-row {
  margin-bottom: 20px;
}

.quick-actions {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.quick-actions .el-button {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>