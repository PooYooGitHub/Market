<template>
  <div class="processing-cases">
    <div class="header">
      <h2>处理中仲裁案件</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索案件编号、申请人、订单号"
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
              <div class="stats-value">{{ statistics.processingCount }}</div>
              <div class="stats-label">处理中</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value warning">{{ statistics.urgentCount }}</div>
              <div class="stats-label">高优先级</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value danger">{{ statistics.overdueCount }}</div>
              <div class="stats-label">超 3 天未完结</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="filters">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="filters.priority" placeholder="优先级" clearable @change="handleFilter">
            <el-option label="全部" value="" />
            <el-option label="高" value="high" />
            <el-option label="普通" value="normal" />
            <el-option label="低" value="low" />
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
        <el-table-column prop="caseNumber" label="案件编号" width="160">
          <template #default="scope">
            <el-link type="primary" @click="goCaseDetail(scope.row)">{{ scope.row.caseNumber }}</el-link>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="争议标题" min-width="200" show-overflow-tooltip />

        <el-table-column prop="applicantName" label="申请人" width="120" />

        <el-table-column prop="respondentName" label="被申请人" width="120" />

        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="getPriorityColor(scope.row.priority)">{{ getPriorityName(scope.row.priority) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="waitingDays" label="已处理天数" width="110">
          <template #default="scope">
            <span :class="{ 'text-danger': scope.row.waitingDays > 3 }">{{ scope.row.waitingDays }} 天</span>
          </template>
        </el-table-column>

        <el-table-column prop="acceptedTime" label="受理时间" width="180" />

        <el-table-column label="操作" width="340" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="resolveCase(scope.row)">完结裁决</el-button>
            <el-button size="small" type="danger" @click="rejectCase(scope.row)">驳回</el-button>
            <el-button size="small" type="warning" @click="requestSupplement(scope.row)">要求补证</el-button>
            <el-button size="small" @click="goCaseDetail(scope.row)">详情</el-button>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { arbitrationApi } from '@/api/arbitration'

const STATUS_PROCESSING = 1
const STATUS_WAIT_SUPPLEMENT = 4

const loading = ref(false)
const searchKeyword = ref('')
const caseList = ref([])
const router = useRouter()

const statistics = reactive({
  processingCount: 0,
  urgentCount: 0,
  overdueCount: 0
})

const filters = reactive({
  priority: '',
  dateRange: null
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

onMounted(() => {
  loadProcessingCases()
  loadStatistics()
})

const loadProcessingCases = async () => {
  loading.value = true
  try {
    const response = await arbitrationApi.getAdminArbitrationList({
      current: pagination.page,
      size: pagination.size,
      status: STATUS_PROCESSING,
      priority: filters.priority || undefined,
      keyword: searchKeyword.value || undefined
    })

    const pageData = response?.data || {}
    const records = pageData.records || []

    caseList.value = records
      .map(item => normalizeCase(item))
      .filter(item => [STATUS_PROCESSING, STATUS_WAIT_SUPPLEMENT].includes(Number(item.status)))
      .filter(item => matchClientFilter(item))

    pagination.total = Number(pageData.total || caseList.value.length || 0)
  } catch (error) {
    console.error('加载处理中案件失败:', error)
    caseList.value = []
    pagination.total = 0
    ElMessage.error(error?.message || '加载处理中案件失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await arbitrationApi.getArbitrationStats()
    const stats = response?.data || {}
    statistics.processingCount = Number(stats.processingCount || 0)
    const urgent = caseList.value.filter(item => item.priority === 'high').length
    const overdue = caseList.value.filter(item => item.waitingDays > 3).length
    statistics.urgentCount = urgent
    statistics.overdueCount = overdue
  } catch (error) {
    console.error('加载统计失败:', error)
    statistics.processingCount = 0
    statistics.urgentCount = 0
    statistics.overdueCount = 0
  }
}

const normalizeCase = (item) => {
  const createdAt = item.createTime || item.createdAt
  const acceptedAt = item.updateTime || item.acceptTime || createdAt
  return {
    id: item.id,
    caseNumber: item.caseNumber || `ARB${item.id || ''}`,
    title: item.title || item.reason || '未命名仲裁案件',
    applicantName: item.applicantName || item.applicant?.nickname || item.applicant?.username || `用户${item.applicantId || '-'}`,
    respondentName: item.respondentName || item.respondent?.nickname || item.respondent?.username || `用户${item.respondentId || '-'}`,
    priority: item.priority || 'normal',
    waitingDays: calculateWaitingDays(createdAt),
    acceptedTime: formatDateTime(acceptedAt),
    status: Number(item.status),
    result: item.result || '',
    orderId: item.orderId
  }
}

const matchClientFilter = (item) => {
  if (filters.dateRange && filters.dateRange.length === 2) {
    const start = new Date(filters.dateRange[0]).getTime()
    const end = new Date(filters.dateRange[1]).getTime()
    const ts = new Date(item.acceptedTime).getTime()
    if (!Number.isNaN(ts) && (ts < start || ts > end)) {
      return false
    }
  }

  if (!searchKeyword.value) {
    return true
  }

  const keyword = searchKeyword.value.toLowerCase()
  return [item.caseNumber, item.title, item.applicantName, item.respondentName, item.orderId]
    .filter(Boolean)
    .some(value => String(value).toLowerCase().includes(keyword))
}

const calculateWaitingDays = (createTime) => {
  if (!createTime) return 0
  const now = Date.now()
  const created = new Date(createTime).getTime()
  if (Number.isNaN(created)) return 0
  return Math.max(1, Math.ceil((now - created) / (1000 * 60 * 60 * 24)))
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString('zh-CN')
}

const getPriorityName = (priority) => {
  const map = {
    high: '高',
    normal: '普通',
    low: '低'
  }
  return map[priority] || '普通'
}

const getPriorityColor = (priority) => {
  const map = {
    high: 'danger',
    normal: 'warning',
    low: 'info'
  }
  return map[priority] || 'warning'
}

const handleSearch = () => {
  pagination.page = 1
  loadProcessingCases().then(loadStatistics)
}

const handleFilter = () => {
  pagination.page = 1
  loadProcessingCases().then(loadStatistics)
}

const resetFilters = () => {
  filters.priority = ''
  filters.dateRange = null
  searchKeyword.value = ''
  pagination.page = 1
  refreshData()
}

const refreshData = () => {
  loadProcessingCases().then(loadStatistics)
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadProcessingCases().then(loadStatistics)
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadProcessingCases().then(loadStatistics)
}

const goCaseDetail = (caseItem) => {
  router.push({
    name: 'AdminArbitrationDetail',
    params: { id: caseItem.id },
    query: {
      from: 'processing',
      caseNumber: caseItem.caseNumber,
      applicantName: caseItem.applicantName,
      respondentName: caseItem.respondentName
    }
  })
}

const requestSupplement = async (caseItem) => {
  try {
    const { value: requiredItems } = await ElMessageBox.prompt(
      '请输入需补充的证据要求（默认通知买卖双方，48小时内补充）',
      `要求补证 ${caseItem.caseNumber}`,
      {
        confirmButtonText: '发送补证要求',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '例如：请补充完整聊天记录截图、交易凭证和商品实拍图',
        inputValidator: (value) => {
          if (!value || !String(value).trim()) return '请填写补证要求'
          return true
        }
      }
    )

    await arbitrationApi.requestSupplement({
      arbitrationId: caseItem.id,
      targetParty: 'BOTH',
      requiredItems: String(requiredItems).trim(),
      remark: '管理员要求补充材料',
      dueHours: 48
    })
    ElMessage.success('补证请求已发起（对象：买卖双方，时限48小时）')
    refreshData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发起补证失败:', error)
      ElMessage.error(error?.message || '发起补证失败')
    }
  }
}

const resolveCase = async (caseItem) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入裁决结果说明（将写入仲裁结果）', `完结案件 ${caseItem.caseNumber}`, {
      confirmButtonText: '提交完结',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '例如：支持申请方退款，卖家承担运费。',
      inputValidator: (val) => {
        if (!val || !String(val).trim()) return '请填写裁决说明'
        return true
      }
    })

    await arbitrationApi.handleArbitration({
      id: caseItem.id,
      result: String(value).trim()
    })

    ElMessage.success('案件已完结')
    refreshData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完结案件失败:', error)
      ElMessage.error(error?.message || '完结失败')
    }
  }
}

const rejectCase = async (caseItem) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', `驳回案件 ${caseItem.caseNumber}`, {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputValidator: (val) => {
        if (!val || !String(val).trim()) return '请填写驳回原因'
        return true
      }
    })

    await arbitrationApi.rejectArbitration(caseItem.id, String(value).trim())
    ElMessage.success('案件已驳回')
    refreshData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('驳回失败:', error)
      ElMessage.error(error?.message || '驳回失败')
    }
  }
}
</script>

<style scoped>
.processing-cases {
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

.stats-value.warning {
  color: #e6a23c;
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

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}
</style>
