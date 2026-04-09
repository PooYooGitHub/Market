<template>
  <div class="admin-arbitration-page">
    <!-- 统计面板 -->
    <div class="stats-panel">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <el-statistic title="待处理" :value="stats.pendingCount" suffix="件">
              <template #prefix>
                <el-icon color="#e6a23c"><Clock /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <el-statistic title="处理中" :value="stats.processingCount" suffix="件">
              <template #prefix>
                <el-icon color="#409eff"><Loading /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <el-statistic title="已完结" :value="stats.completedCount" suffix="件">
              <template #prefix>
                <el-icon color="#67c23a"><CircleCheck /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <el-statistic title="已驳回" :value="stats.rejectedCount" suffix="件">
              <template #prefix>
                <el-icon color="#f56c6c"><CircleClose /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 主要内容卡片 -->
    <el-card class="main-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h3><el-icon><Document /></el-icon> 仲裁管理</h3>
          <div class="header-actions">
            <el-button @click="exportData" type="info" plain>
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
            <el-button @click="showStatsDialog" type="primary" plain>
              <el-icon><TrendCharts /></el-icon>
              统计分析
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选器 -->
      <div class="filter-section">
        <el-form :model="filterForm" inline>
          <el-form-item label="状态">
            <el-select v-model="filterForm.status" placeholder="全部状态" clearable>
              <el-option label="全部" value="" />
              <el-option label="待处理" value="0" />
              <el-option label="处理中" value="1" />
              <el-option label="已完结" value="2" />
              <el-option label="已驳回" value="3" />
            </el-select>
          </el-form-item>

          <el-form-item label="处理人">
            <el-select v-model="filterForm.handlerId" placeholder="全部处理人" clearable>
              <el-option label="全部" value="" />
              <el-option label="客服小王" value="10" />
              <el-option label="客服小李" value="11" />
              <el-option label="主管张三" value="12" />
            </el-select>
          </el-form-item>

          <el-form-item label="申请时间">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="searchArbitration">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 仲裁列表 -->
      <div class="arbitration-table">
        <el-table
          v-loading="loading"
          :data="arbitrationList"
          stripe
          @selection-change="handleSelectionChange"
          @row-click="viewDetail"
          style="width: 100%"
        >
          <el-table-column type="selection" width="55" />

          <el-table-column prop="id" label="仲裁编号" width="120">
            <template #default="{ row }">
              AR{{ String(row.id).padStart(6, '0') }}
            </template>
          </el-table-column>

          <el-table-column prop="orderId" label="订单编号" width="140">
            <template #default="{ row }">
              <el-link @click.stop="viewOrder(row.orderId)" type="primary">
                ORDER{{ String(row.orderId).padStart(8, '0') }}
              </el-link>
            </template>
          </el-table-column>

          <el-table-column prop="reason" label="仲裁原因" width="130">
            <template #default="{ row }">
              <el-tag type="warning" effect="plain" size="small">
                {{ getReasonText(row.reason) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="applicantName" label="申请人" width="120">
            <template #default="{ row }">
              <div class="user-info">
                <el-avatar :src="row.applicantAvatar" size="small" />
                <span>{{ row.applicantName || '用户' + row.applicantId }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="respondentName" label="被申诉人" width="120">
            <template #default="{ row }">
              <div class="user-info">
                <el-avatar :src="row.respondentAvatar" size="small" />
                <span>{{ row.respondentName || '用户' + row.respondentId }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="handlerName" label="处理人" width="100">
            <template #default="{ row }">
              {{ row.handlerName || '-' }}
            </template>
          </el-table-column>

          <el-table-column prop="createTime" label="申请时间" width="150">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  type="primary"
                  size="small"
                  @click.stop="viewDetail(row)"
                >
                  详情
                </el-button>

                <el-button
                  v-if="row.status === 0"
                  type="success"
                  size="small"
                  @click.stop="acceptArbitration(row)"
                >
                  受理
                </el-button>

                <el-dropdown
                  v-if="row.status === 1"
                  @command="(command) => handleCommand(command, row)"
                  trigger="click"
                >
                  <el-button type="warning" size="small">
                    处理 <el-icon><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="resolve">完结</el-dropdown-item>
                      <el-dropdown-item command="reject">驳回</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 批量操作 -->
        <div v-if="selectedItems.length > 0" class="batch-actions">
          <el-alert
            :title="`已选择 ${selectedItems.length} 项`"
            type="info"
            :closable="false"
          >
            <template #default>
              <el-button type="primary" size="small" @click="batchAccept">批量受理</el-button>
              <el-button type="success" size="small" @click="batchResolve">批量完结</el-button>
              <el-button type="danger" size="small" @click="batchReject">批量驳回</el-button>
            </template>
          </el-alert>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadArbitrationList"
            @current-change="loadArbitrationList"
          />
        </div>
      </div>
    </el-card>

    <!-- 处理对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="handleFormRef"
        :model="handleForm"
        :rules="handleRules"
        label-width="80px"
      >
        <el-form-item label="处理结果" prop="result">
          <el-select v-model="handleForm.result" placeholder="请选择处理结果">
            <el-option label="支持申请人" value="2" />
            <el-option label="驳回申请" value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="处理说明" prop="remark">
          <el-input
            v-model="handleForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理说明"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle" :loading="submitting">
          确认处理
        </el-button>
      </template>
    </el-dialog>

    <!-- 统计分析对话框 -->
    <el-dialog
      v-model="statsDialogVisible"
      title="统计分析"
      width="800px"
    >
      <div class="stats-content">
        <el-date-picker
          v-model="statsDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="loadStatsData"
          style="margin-bottom: 20px"
        />

        <div class="stats-charts">
          <!-- 这里可以集成图表组件，如ECharts -->
          <div class="chart-placeholder">
            <el-text type="info">统计图表功能待实现</el-text>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Document, Clock, Loading, CircleCheck, CircleClose, Download,
  TrendCharts, Search, ArrowDown
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { arbitrationApi } from '@/api/arbitration'
import { format } from 'date-fns'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const statsDialogVisible = ref(false)
const dialogTitle = ref('')
const currentRow = ref(null)
const arbitrationList = ref([])
const selectedItems = ref([])
const stats = ref({})
const statsDateRange = ref([])

// 表单数据
const filterForm = reactive({
  status: '',
  handlerId: '',
  dateRange: []
})

const handleForm = reactive({
  arbitrationId: null,
  result: '',
  remark: ''
})

// 分页数据
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 表单验证规则
const handleRules = {
  result: [
    { required: true, message: '请选择处理结果', trigger: 'change' }
  ],
  remark: [
    { required: true, message: '请输入处理说明', trigger: 'blur' },
    { min: 10, message: '处理说明至少10个字符', trigger: 'blur' }
  ]
}

const handleFormRef = ref()

// 加载仲裁列表
const loadArbitrationList = async () => {
  loading.value = true

  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      status: filterForm.status || undefined,
      handlerId: filterForm.handlerId || undefined,
      startDate: filterForm.dateRange?.[0],
      endDate: filterForm.dateRange?.[1]
    }

    const result = await arbitrationApi.getAdminArbitrationList(params)

    arbitrationList.value = result.data.records || []
    pagination.total = result.data.total || 0

    // 模拟数据 (实际开发时删除)
    if (!result.data || result.data.records.length === 0) {
      arbitrationList.value = [
        {
          id: 1,
          orderId: 100001,
          applicantId: 1,
          applicantName: '张三',
          applicantAvatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100',
          respondentId: 2,
          respondentName: '李四',
          respondentAvatar: 'https://images.unsplash.com/photo-1494790108755-2616b612d5db?w=100',
          reason: 'QUALITY_ISSUE',
          description: '商品质量问题',
          status: 0,
          handlerId: null,
          handlerName: null,
          createTime: new Date('2024-03-28 10:30:00')
        },
        {
          id: 2,
          orderId: 100002,
          applicantId: 3,
          applicantName: '王五',
          applicantAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100',
          respondentId: 4,
          respondentName: '赵六',
          respondentAvatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100',
          reason: 'SHIPPING_DELAY',
          description: '发货延迟',
          status: 1,
          handlerId: 10,
          handlerName: '客服小王',
          createTime: new Date('2024-03-29 14:20:00')
        }
      ]
      pagination.total = 2
    }

  } catch (error) {
    ElMessage.error('加载失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    // 实际应该调用统计API
    // const result = await arbitrationApi.getAdminStats()

    // 模拟统计数据
    stats.value = {
      pendingCount: 15,
      processingCount: 8,
      completedCount: 42,
      rejectedCount: 6
    }
  } catch (error) {
    console.error('加载统计数据失败：', error)
  }
}

// 搜索仲裁
const searchArbitration = () => {
  pagination.current = 1
  loadArbitrationList()
}

// 重置筛选
const resetFilter = () => {
  Object.keys(filterForm).forEach(key => {
    if (Array.isArray(filterForm[key])) {
      filterForm[key] = []
    } else {
      filterForm[key] = ''
    }
  })
  searchArbitration()
}

// 查看详情
const viewDetail = (row) => {
  router.push(`/arbitration/detail/${row.id}`)
}

// 查看订单
const viewOrder = (orderId) => {
  router.push(`/order/${orderId}`)
}

// 受理仲裁
const acceptArbitration = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认受理仲裁申请 "AR${String(row.id).padStart(6, '0')}" ？`,
      '确认受理',
      {
        confirmButtonText: '确认受理',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await arbitrationApi.acceptArbitration(row.id, getCurrentAdminId())

    ElMessage.success('仲裁申请已受理')
    loadArbitrationList()

  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('受理失败：' + (error.message || '未知错误'))
    }
  }
}

// 处理命令
const handleCommand = (command, row) => {
  currentRow.value = row
  handleForm.arbitrationId = row.id

  if (command === 'resolve') {
    dialogTitle.value = '完结仲裁'
    handleForm.result = '2'
  } else if (command === 'reject') {
    dialogTitle.value = '驳回仲裁'
    handleForm.result = '3'
  }

  dialogVisible.value = true
}

// 提交处理
const submitHandle = async () => {
  if (!handleFormRef.value) return

  try {
    const valid = await handleFormRef.value.validate()
    if (!valid) return

    submitting.value = true

    const data = {
      arbitrationId: handleForm.arbitrationId,
      handlerId: getCurrentAdminId(),
      result: parseInt(handleForm.result),
      remark: handleForm.remark
    }

    await arbitrationApi.handleArbitration(data)

    ElMessage.success('处理完成')
    dialogVisible.value = false
    resetHandleForm()
    loadArbitrationList()

  } catch (error) {
    ElMessage.error('处理失败：' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

// 重置处理表单
const resetHandleForm = () => {
  Object.keys(handleForm).forEach(key => {
    if (typeof handleForm[key] === 'number') {
      handleForm[key] = null
    } else {
      handleForm[key] = ''
    }
  })
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

// 批量受理
const batchAccept = async () => {
  const pendingItems = selectedItems.value.filter(item => item.status === 0)

  if (pendingItems.length === 0) {
    ElMessage.warning('没有可受理的申请')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认批量受理 ${pendingItems.length} 个仲裁申请？`,
      '批量受理',
      { type: 'warning' }
    )

    // 并发处理
    const promises = pendingItems.map(item =>
      arbitrationApi.acceptArbitration(item.id, getCurrentAdminId())
    )

    await Promise.all(promises)

    ElMessage.success('批量受理成功')
    loadArbitrationList()

  } catch (error) {
    ElMessage.error('批量受理失败')
  }
}

// 批量完结
const batchResolve = () => {
  ElMessage.info('批量完结功能待实现')
}

// 批量驳回
const batchReject = () => {
  ElMessage.info('批量驳回功能待实现')
}

// 导出数据
const exportData = () => {
  ElMessage.info('数据导出功能待实现')
}

// 显示统计对话框
const showStatsDialog = () => {
  statsDialogVisible.value = true
}

// 加载统计数据
const loadStatsData = () => {
  ElMessage.info('统计数据加载功能待实现')
}

// 获取当前管理员ID
const getCurrentAdminId = () => {
  return 10 // 模拟管理员ID
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    0: '待处理',
    1: '处理中',
    2: '已完结',
    3: '已驳回'
  }
  return texts[status] || '未知'
}

// 获取原因文本
const getReasonText = (reason) => {
  const texts = {
    'QUALITY_ISSUE': '质量问题',
    'SHIPPING_DELAY': '发货延迟',
    'DESCRIPTION_MISMATCH': '描述不符',
    'NO_RESPONSE': '无响应',
    'OTHER': '其他'
  }
  return texts[reason] || '其他'
}

// 格式化日期
const formatDate = (date) => {
  return format(new Date(date), 'MM-dd HH:mm')
}

// 页面初始化
onMounted(() => {
  loadArbitrationList()
  loadStats()
})
</script>

<style scoped>
.admin-arbitration-page {
  padding: 20px;
}

.stats-panel {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-section {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.arbitration-table {
  min-height: 400px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.batch-actions {
  margin: 20px 0;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: center;
}

.stats-content {
  padding: 20px 0;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .admin-arbitration-page {
    padding: 10px;
  }

  .stats-panel .el-col {
    margin-bottom: 12px;
  }

  .card-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .filter-section .el-form {
    flex-direction: column;
  }

  .filter-section .el-form-item {
    margin-right: 0;
    margin-bottom: 16px;
  }
}
</style>