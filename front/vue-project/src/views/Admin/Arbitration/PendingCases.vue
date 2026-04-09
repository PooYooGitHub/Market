<template>
  <div class="pending-cases">
    <div class="header">
      <h2>待处理仲裁案件</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索案件编号、申请人"
          style="width: 300px"
          clearable
          @change="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value">{{ statistics.pendingCount }}</div>
              <div class="stats-label">待处理案件</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value urgent">{{ statistics.urgentCount }}</div>
              <div class="stats-label">紧急案件</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value">{{ statistics.todayCount }}</div>
              <div class="stats-label">今日新增</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选器 -->
    <div class="filters">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="filters.priority" placeholder="优先级" clearable @change="handleFilter">
            <el-option label="全部" value=""></el-option>
            <el-option label="紧急" value="high"></el-option>
            <el-option label="普通" value="normal"></el-option>
            <el-option label="低" value="low"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="filters.type" placeholder="争议类型" clearable @change="handleFilter">
            <el-option label="全部" value=""></el-option>
            <el-option label="商品质量" value="quality"></el-option>
            <el-option label="交易纠纷" value="trade"></el-option>
            <el-option label="售后服务" value="service"></el-option>
            <el-option label="其他" value="other"></el-option>
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
        <el-col :span="6">
          <el-button @click="resetFilters">重置筛选</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 案件列表 -->
    <el-card class="table-card">
      <el-table
        :data="caseList"
        v-loading="loading"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />

        <el-table-column prop="caseNumber" label="案件编号" width="150">
          <template #default="scope">
            <el-link @click="openCaseDetail(scope.row)" type="primary">
              {{ scope.row.caseNumber }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="争议标题" min-width="200" show-overflow-tooltip />

        <el-table-column prop="applicantName" label="申请人" width="120" />

        <el-table-column prop="respondentName" label="被申请人" width="120" />

        <el-table-column prop="disputeType" label="争议类型" width="120">
          <template #default="scope">
            <el-tag :type="getDisputeTypeColor(scope.row.disputeType)">
              {{ getDisputeTypeName(scope.row.disputeType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="getPriorityColor(scope.row.priority)">
              {{ getPriorityName(scope.row.priority) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="waitingDays" label="等待天数" width="100">
          <template #default="scope">
            <span :class="{ 'text-danger': scope.row.waitingDays > 3 }">
              {{ scope.row.waitingDays }}天
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="申请时间" width="180" />

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="acceptCase(scope.row)">
              受理
            </el-button>
            <el-button size="small" type="info" @click="openCaseDetail(scope.row)">
              详情
            </el-button>
            <el-button size="small" type="danger" @click="rejectCase(scope.row)">
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div class="batch-actions" v-if="selectedCases.length > 0">
        <span class="selection-info">已选择 {{ selectedCases.length }} 个案件</span>
        <el-button @click="batchAccept" :loading="batchLoading">批量受理</el-button>
        <el-button @click="batchReject" :loading="batchLoading">批量驳回</el-button>
      </div>

      <!-- 分页 -->
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { arbitrationApi } from '@/api/arbitration'

// 响应式数据
const loading = ref(false)
const batchLoading = ref(false)
const searchKeyword = ref('')
const caseList = ref([])
const selectedCases = ref([])

// 统计数据
const statistics = reactive({
  pendingCount: 0,
  urgentCount: 0,
  todayCount: 0
})

// 筛选条件
const filters = reactive({
  priority: '',
  type: '',
  dateRange: null
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 生命周期
onMounted(() => {
  loadPendingCases()
  loadStatistics()
})

// 加载待处理案件（真实数据）
const loadPendingCases = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.page,
      size: pagination.size,
      status: 0,
      keyword: searchKeyword.value || undefined,
      priority: filters.priority || undefined
    }

    const response = await arbitrationApi.getAdminArbitrationList(params)
    const page = response?.data || {}
    const records = page.records || []

    caseList.value = records.map(item => ({
      id: item.id,
      caseNumber: `ARB${String(item.id).padStart(6, '0')}`,
      title: item.description || item.reason || '',
      applicantName: item.applicantName || `用户${item.applicantId ?? ''}`,
      respondentName: item.respondentName || `用户${item.respondentId ?? ''}`,
      disputeType: mapReasonToDisputeType(item.reason),
      priority: calcPriority(item.createTime),
      waitingDays: calculateWaitingDays(item.createTime),
      rawCreateTime: item.createTime,
      createTime: formatDateTime(item.createTime),
      orderId: item.orderId,
      status: item.status
    }))

    // 本地争议类型筛选（后端无该参数）
    if (filters.type) {
      caseList.value = caseList.value.filter(item => item.disputeType === filters.type)
    }

    // 本地日期筛选
    if (filters.dateRange?.[0] && filters.dateRange?.[1]) {
      const start = new Date(filters.dateRange[0]).getTime()
      const end = new Date(filters.dateRange[1]).getTime() + 24 * 60 * 60 * 1000 - 1
      caseList.value = caseList.value.filter(item => {
        const t = new Date(item.rawCreateTime || item.createTime).getTime()
        return t >= start && t <= end
      })
    }

    pagination.total = page.total || 0
  } catch (error) {
    console.error('加载待处理案件失败：', error)
    caseList.value = []
    pagination.total = 0
    ElMessage.error(`加载待处理案件失败：${error?.message || '未知错误'}`)
  } finally {
    loading.value = false
  }
}

// 加载统计数据（真实数据）
const loadStatistics = async () => {
  try {
    const response = await arbitrationApi.getArbitrationStats()
    const stats = response?.data || {}
    statistics.pendingCount = stats.pendingCount || 0
    statistics.urgentCount = stats.urgentCount || 0
    statistics.todayCount = stats.todayNewCount || stats.todayCount || 0
  } catch (error) {
    console.error('加载统计数据失败：', error)
    statistics.pendingCount = 0
    statistics.urgentCount = 0
    statistics.todayCount = 0
    ElMessage.error(`加载统计数据失败：${error?.message || '未知错误'}`)
  }
}

const mapReasonToDisputeType = (reason) => {
  const map = {
    QUALITY_ISSUE: 'quality',
    SHIPPING_DELAY: 'trade',
    DESCRIPTION_MISMATCH: 'quality',
    NO_RESPONSE: 'service',
    OTHER: 'other'
  }
  return map[reason] || 'other'
}

const calcPriority = (createTime) => {
  const days = calculateWaitingDays(createTime)
  if (days >= 3) return 'high'
  if (days >= 1) return 'normal'
  return 'low'
}

// 工具函数
const calculateWaitingDays = (createTime) => {
  if (!createTime) return 0
  const now = new Date()
  const created = new Date(createTime)
  const diffTime = Math.abs(now - created)
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getDisputeTypeName = (type) => {
  const names = {
    quality: '商品质量',
    trade: '交易纠纷',
    service: '售后服务',
    other: '其他'
  }
  return names[type] || '其他'
}

const getDisputeTypeColor = (type) => {
  const colors = {
    quality: 'warning',
    trade: 'danger',
    service: 'info',
    other: ''
  }
  return colors[type] || ''
}

const getPriorityName = (priority) => {
  const names = {
    high: '紧急',
    normal: '普通',
    low: '低'
  }
  return names[priority] || '普通'
}

const getPriorityColor = (priority) => {
  const colors = {
    high: 'danger',
    normal: 'success',
    low: 'info'
  }
  return colors[priority] || 'success'
}

// 事件处理
const handleSearch = () => {
  pagination.page = 1
  loadPendingCases()
}

const handleFilter = () => {
  pagination.page = 1
  loadPendingCases()
}

const resetFilters = () => {
  Object.keys(filters).forEach(key => {
    filters[key] = key === 'dateRange' ? null : ''
  })
  searchKeyword.value = ''
  pagination.page = 1
  loadPendingCases()
}

const refreshData = () => {
  loadPendingCases()
  loadStatistics()
}

const handleSelectionChange = (selection) => {
  selectedCases.value = selection
}

const handleSizeChange = (val) => {
  pagination.size = val
  loadPendingCases()
}

const handleCurrentChange = (val) => {
  pagination.page = val
  loadPendingCases()
}

// 案件操作
const viewDetail = (caseItem) => {
  // 跳转到详情页面或显示详情弹窗
  console.log('查看案件详情：', caseItem)
  ElMessage.info(`查看案件详情：${caseItem.caseNumber}`)
}

const openCaseDetailLegacy = async (caseItem) => {
  await ElMessageBox.alert(
    `<div style="line-height:1.8;">
      <div><strong>案件编号：</strong>${caseItem.caseNumber || '-'}</div>
      <div><strong>争议标题：</strong>${caseItem.title || '-'}</div>
      <div><strong>申请人：</strong>${caseItem.applicantName || '-'}</div>
      <div><strong>被申请人：</strong>${caseItem.respondentName || '-'}</div>
      <div><strong>争议类型：</strong>${getDisputeTypeName(caseItem.disputeType)}</div>
      <div><strong>优先级：</strong>${getPriorityName(caseItem.priority)}</div>
      <div><strong>等待天数：</strong>${caseItem.waitingDays || 0} 天</div>
      <div><strong>申请时间：</strong>${caseItem.createTime || '-'}</div>
      <div><strong>关联订单：</strong>${caseItem.orderId || '-'}</div>
    </div>`,
    '案件详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

const openCaseDetail = async (caseItem) => {
  try {
    const [detailRes, logsRes] = await Promise.all([
      arbitrationApi.getArbitrationDetail(caseItem.id),
      arbitrationApi.getArbitrationLogs(caseItem.id).catch(() => ({ data: [] }))
    ])
    const detail = detailRes?.data || {}
    const logs = Array.isArray(logsRes?.data) ? logsRes.data : []
    const evidenceList = parseEvidence(detail.evidence)

    const evidenceHtml = evidenceList.length
      ? evidenceList.map((item, idx) => `<div>${idx + 1}. <a href="${escapeHtml(item)}" target="_blank" rel="noopener noreferrer">${escapeHtml(item)}</a></div>`).join('')
      : '<div>无</div>'

    const logsHtml = logs.length
      ? logs.map((log, idx) => `<div>${idx + 1}. ${escapeHtml(formatDateTime(log.createTime))} - ${escapeHtml(log.action || '操作')} ${log.remark ? `（${escapeHtml(log.remark)}）` : ''}</div>`).join('')
      : '<div>暂无处理记录</div>'

    await ElMessageBox.alert(
      `<div style="line-height:1.85;max-height:65vh;overflow:auto;padding-right:6px;">
        <div><strong>案件编号：</strong>${escapeHtml(caseItem.caseNumber || '-')}</div>
        <div><strong>订单号：</strong>${escapeHtml(String(detail.orderId || caseItem.orderId || '-'))}</div>
        <div><strong>申请人：</strong>${escapeHtml(caseItem.applicantName || '-')}（ID: ${escapeHtml(String(detail.applicantId || '-'))}）</div>
        <div><strong>被申请人：</strong>${escapeHtml(caseItem.respondentName || '-')}（ID: ${escapeHtml(String(detail.respondentId || '-'))}）</div>
        <div><strong>仲裁原因：</strong>${escapeHtml(getReasonName(detail.reason))}</div>
        <div><strong>详细描述：</strong>${escapeHtml(detail.description || '无')}</div>
        <div><strong>证据材料：</strong>${evidenceHtml}</div>
        <div><strong>状态：</strong>${escapeHtml(getStatusName(detail.status))}</div>
        <div><strong>当前优先级：</strong>${escapeHtml(getPriorityName(caseItem.priority))}</div>
        <div><strong>等待天数：</strong>${escapeHtml(String(caseItem.waitingDays || 0))} 天</div>
        <div><strong>申请时间：</strong>${escapeHtml(formatDateTime(detail.createTime))}</div>
        <div><strong>最后更新时间：</strong>${escapeHtml(formatDateTime(detail.updateTime))}</div>
        <div style="margin-top:8px;"><strong>处理记录：</strong>${logsHtml}</div>
      </div>`,
      '仲裁申请详情',
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '关闭'
      }
    )
  } catch (error) {
    console.error('加载案件详情失败:', error)
    ElMessage.error(error?.message || '加载案件详情失败')
  }
}

const getReasonName = (reason) => {
  const map = {
    QUALITY_ISSUE: '商品质量问题',
    SHIPPING_DELAY: '发货/物流问题',
    DESCRIPTION_MISMATCH: '商品描述不符',
    NO_RESPONSE: '对方失联/不处理',
    SERVICE_ISSUE: '售后服务问题',
    OTHER: '其他'
  }
  return map[reason] || reason || '未知'
}

const getStatusName = (status) => {
  const map = {
    0: '待处理',
    1: '处理中',
    2: '已完结',
    3: '已驳回'
  }
  return map[Number(status)] || '未知'
}

const parseEvidence = (evidence) => {
  if (!evidence) return []
  if (Array.isArray(evidence)) return evidence.map(item => String(item)).filter(Boolean)
  if (typeof evidence !== 'string') return [String(evidence)]
  try {
    const parsed = JSON.parse(evidence)
    if (Array.isArray(parsed)) return parsed.map(item => String(item)).filter(Boolean)
    if (typeof parsed === 'string') return parsed ? [parsed] : []
    return []
  } catch {
    return evidence ? [evidence] : []
  }
}

const escapeHtml = (value) => {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

const acceptCase = async (caseItem) => {
  try {
    await ElMessageBox.confirm('确定要受理此案件吗？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await arbitrationApi.acceptArbitration(caseItem.id)
    ElMessage.success('案件受理成功')
    refreshData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('受理案件失败：', error)
      ElMessage.error('受理失败')
    }
  }
}

const rejectCase = async (caseItem) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入驳回原因', '驳回案件', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValidator: (value) => {
        if (!value) return '请输入驳回原因'
        return true
      }
    })

    await arbitrationApi.rejectArbitration(caseItem.id, reason)
    ElMessage.success('案件驳回成功')
    refreshData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('驳回案件失败：', error)
      ElMessage.error('驳回失败')
    }
  }
}

const batchAccept = async () => {
  if (selectedCases.value.length === 0) {
    ElMessage.warning('请选择要受理的案件')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要批量受理 ${selectedCases.value.length} 个案件吗？`, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    batchLoading.value = true
    await Promise.all(selectedCases.value.map(item => arbitrationApi.acceptArbitration(item.id)))

    ElMessage.success(`成功受理 ${selectedCases.value.length} 个案件`)
    selectedCases.value = []
    refreshData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量受理失败：', error)
      ElMessage.error('批量受理失败')
    }
  } finally {
    batchLoading.value = false
  }
}

const batchReject = async () => {
  if (selectedCases.value.length === 0) {
    ElMessage.warning('请选择要驳回的案件')
    return
  }

  try {
    const { value: reason } = await ElMessageBox.prompt('请输入批量驳回原因', '批量驳回案件', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValidator: (value) => {
        if (!value) return '请输入驳回原因'
        return true
      }
    })

    batchLoading.value = true
    await Promise.all(selectedCases.value.map(item => arbitrationApi.rejectArbitration(item.id, reason)))

    ElMessage.success(`成功驳回 ${selectedCases.value.length} 个案件`)
    selectedCases.value = []
    refreshData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量驳回失败：', error)
      ElMessage.error('批量驳回失败')
    }
  } finally {
    batchLoading.value = false
  }
}
</script>

<style scoped>
.pending-cases {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 15px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stats-card {
  text-align: center;
}

.stats-item {
  padding: 20px;
}

.stats-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stats-value.urgent {
  color: #F56C6C;
}

.stats-label {
  font-size: 14px;
  color: #909399;
}

.filters {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 0;
  border-top: 1px solid #EBEEF5;
  margin-top: 15px;
}

.selection-info {
  color: #606266;
  font-size: 14px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.text-danger {
  color: #F56C6C;
  font-weight: bold;
}
</style>
