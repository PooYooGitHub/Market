<template>
  <div class="completed-cases">
    <div class="header">
      <h2>已完成仲裁案件</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索案件编号、申请人、裁决关键词"
          style="width: 320px"
          clearable
          @change="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button :loading="loading" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value success">{{ statistics.completedCount }}</div>
              <div class="stats-label">已完结</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value danger">{{ statistics.rejectedCount }}</div>
              <div class="stats-label">已驳回</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value">{{ statistics.todayNewCount }}</div>
              <div class="stats-label">今日新增</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="filters">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="filters.status" placeholder="案件状态" @change="handleFilter">
            <el-option label="仅已完结" :value="2" />
            <el-option label="仅已驳回" :value="3" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleFilter"
          />
        </el-col>
        <el-col :span="12" class="filter-actions">
          <el-button @click="resetFilters">重置筛选</el-button>
        </el-col>
      </el-row>
    </div>

    <el-card class="table-card">
      <el-table :data="caseList" v-loading="loading" stripe>
        <el-table-column prop="caseNumber" label="案件编号" width="160" />

        <el-table-column prop="title" label="争议标题" min-width="220" show-overflow-tooltip />

        <el-table-column prop="applicantName" label="申请人" width="120" />

        <el-table-column prop="respondentName" label="被申请人" width="120" />

        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusColor(scope.row.status)">{{ getStatusName(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="result" label="裁决结果" min-width="280" show-overflow-tooltip />

        <el-table-column prop="finishedTime" label="结案时间" width="180" />

        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewResult(scope.row)">查看裁决</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { arbitrationApi } from '@/api/arbitration'

const loading = ref(false)
const searchKeyword = ref('')
const caseList = ref([])

const statistics = reactive({
  completedCount: 0,
  rejectedCount: 0,
  todayNewCount: 0
})

const filters = reactive({
  status: 2,
  dateRange: null
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

onMounted(() => {
  loadCompletedCases()
  loadStatistics()
})

const loadCompletedCases = async () => {
  loading.value = true
  try {
    const response = await arbitrationApi.getAdminArbitrationList({
      current: pagination.page,
      size: pagination.size,
      status: filters.status,
      keyword: searchKeyword.value || undefined
    })

    const pageData = response?.data || {}
    const records = pageData.records || []

    caseList.value = records
      .map(item => normalizeCase(item))
      .filter(item => matchClientFilter(item))

    pagination.total = Number(pageData.total || caseList.value.length || 0)
  } catch (error) {
    console.error('加载已完成案件失败:', error)
    caseList.value = []
    pagination.total = 0
    ElMessage.error(error?.message || '加载已完成案件失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await arbitrationApi.getArbitrationStats()
    const stats = response?.data || {}
    statistics.completedCount = Number(stats.completedCount || 0)
    statistics.rejectedCount = Number(stats.rejectedCount || 0)
    statistics.todayNewCount = Number(stats.todayNewCount || 0)
  } catch (error) {
    console.error('加载统计失败:', error)
    statistics.completedCount = 0
    statistics.rejectedCount = 0
    statistics.todayNewCount = 0
  }
}

const normalizeCase = (item) => {
  return {
    id: item.id,
    caseNumber: item.caseNumber || `ARB${item.id || ''}`,
    title: item.title || item.reason || '未命名仲裁案件',
    applicantName: item.applicantName || item.applicant?.nickname || item.applicant?.username || `用户${item.applicantId || '-'}`,
    respondentName: item.respondentName || item.respondent?.nickname || item.respondent?.username || `用户${item.respondentId || '-'}`,
    status: Number(item.status),
    result: item.result || '无裁决说明',
    finishedTime: formatDateTime(item.updateTime || item.createTime),
    orderId: item.orderId
  }
}

const matchClientFilter = (item) => {
  if (filters.dateRange && filters.dateRange.length === 2) {
    const start = new Date(filters.dateRange[0]).getTime()
    const end = new Date(filters.dateRange[1]).getTime()
    const ts = new Date(item.finishedTime).getTime()
    if (!Number.isNaN(ts) && (ts < start || ts > end)) {
      return false
    }
  }

  if (!searchKeyword.value) {
    return true
  }

  const keyword = searchKeyword.value.toLowerCase()
  return [item.caseNumber, item.title, item.applicantName, item.respondentName, item.result, item.orderId]
    .filter(Boolean)
    .some(value => String(value).toLowerCase().includes(keyword))
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString('zh-CN')
}

const getStatusName = (status) => {
  if (status === 2) return '已完结'
  if (status === 3) return '已驳回'
  return '未知'
}

const getStatusColor = (status) => {
  if (status === 2) return 'success'
  if (status === 3) return 'danger'
  return 'info'
}

const handleSearch = () => {
  pagination.page = 1
  loadCompletedCases().then(loadStatistics)
}

const handleFilter = () => {
  pagination.page = 1
  loadCompletedCases().then(loadStatistics)
}

const resetFilters = () => {
  filters.status = 2
  filters.dateRange = null
  searchKeyword.value = ''
  pagination.page = 1
  refreshData()
}

const refreshData = () => {
  loadCompletedCases().then(loadStatistics)
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadCompletedCases().then(loadStatistics)
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadCompletedCases().then(loadStatistics)
}

const viewResult = async (caseItem) => {
  await ElMessageBox.alert(
    `<div style="line-height:1.7;"><strong>案件编号：</strong>${caseItem.caseNumber}<br/><strong>裁决结果：</strong>${caseItem.result}</div>`,
    '裁决详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '我知道了'
    }
  )
}
</script>

<style scoped>
.completed-cases {
  padding: 20px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stats-card {
  text-align: center;
}

.stats-item {
  padding: 18px;
}

.stats-value {
  font-size: 30px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 6px;
}

.stats-value.success {
  color: #67c23a;
}

.stats-value.danger {
  color: #f56c6c;
}

.stats-label {
  color: #909399;
  font-size: 14px;
}

.filters {
  margin-bottom: 20px;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
