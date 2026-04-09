<template>
  <div class="arbitration-list-page">
    <el-card class="list-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h3><el-icon><Document /></el-icon> 我的仲裁</h3>
          <div class="header-actions">
            <el-button type="primary" @click="$router.push('/arbitration/apply')">
              <el-icon><Plus /></el-icon>
              申请仲裁
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选器 -->
      <div class="filter-bar">
        <el-radio-group v-model="activeTab" @change="loadArbitrationList">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="0">待处理</el-radio-button>
          <el-radio-button label="1">处理中</el-radio-button>
          <el-radio-button label="2">已完结</el-radio-button>
          <el-radio-button label="3">已驳回</el-radio-button>
        </el-radio-group>

        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索订单号、商品名称"
            clearable
            @clear="loadArbitrationList"
            @keyup.enter="loadArbitrationList"
            style="width: 250px"
          >
            <template #append>
              <el-button @click="loadArbitrationList" icon="Search" />
            </template>
          </el-input>
        </div>
      </div>

      <!-- 仲裁列表 -->
      <div class="arbitration-list" v-loading="loading">
        <div v-if="arbitrationList.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无仲裁申请记录">
            <el-button type="primary" @click="$router.push('/arbitration/apply')">
              立即申请仲裁
            </el-button>
          </el-empty>
        </div>

        <div v-else>
          <div
            v-for="arbitration in arbitrationList"
            :key="arbitration.id"
            class="arbitration-item"
            @click="goToDetail(arbitration.id)"
          >
            <!-- 仲裁头部信息 -->
            <div class="arbitration-header">
              <div class="arbitration-info">
                <span class="arbitration-id">仲裁编号: AR{{ String(arbitration.id).padStart(6, '0') }}</span>
                <el-tag :type="getStatusType(arbitration.status)" size="small">
                  {{ getStatusText(arbitration.status) }}
                </el-tag>
              </div>
              <div class="arbitration-time">{{ formatDate(arbitration.createTime) }}</div>
            </div>

            <!-- 仲裁内容 -->
            <div class="arbitration-content">
              <div class="order-section">
                <div class="order-info">
                  <h4>订单号: {{ arbitration.orderNo || ('ORDER' + arbitration.orderId) }}</h4>
                  <div class="arbitration-reason">
                    <el-tag type="warning" size="small" effect="plain">
                      {{ getReasonText(arbitration.reason) }}
                    </el-tag>
                  </div>
                </div>
              </div>

              <div class="description-section">
                <p class="description-text">{{ arbitration.description }}</p>
              </div>

              <!-- 对方信息 -->
              <div class="respondent-info">
                <el-avatar :src="arbitration.respondentAvatar" size="small" />
                <span>被申诉人: {{ arbitration.respondentName || '用户' + arbitration.respondentId }}</span>
              </div>

              <!-- 处理结果 -->
              <div v-if="arbitration.result && arbitration.status >= 2" class="result-section">
                <div class="result-title">
                  <el-icon><DocumentChecked /></el-icon>
                  处理结果
                </div>
                <div class="result-content">
                  {{ arbitration.result }}
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="arbitration-actions">
              <el-button
                type="primary"
                size="small"
                @click.stop="goToDetail(arbitration.id)"
              >
                查看详情
              </el-button>

              <el-button
                v-if="arbitration.status === 0"
                type="danger"
                size="small"
                @click.stop="cancelArbitration(arbitration)"
              >
                取消申请
              </el-button>

              <el-button
                v-if="arbitration.status === 2"
                type="success"
                size="small"
                @click.stop="rateHandler(arbitration)"
              >
                评价处理
              </el-button>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="pagination.current"
              v-model:page-size="pagination.size"
              :page-sizes="[10, 20, 50]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadArbitrationList"
              @current-change="loadArbitrationList"
            />
          </div>
        </div>
      </div>
    </el-card>

    <!-- 统计信息卡片 -->
    <el-card v-if="false" class="stats-card" shadow="hover">
      <template #header>
        <h4><el-icon><TrendCharts /></el-icon> 仲裁统计</h4>
      </template>

      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalCount || 0 }}</div>
            <div class="stat-label">总申请数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value success">{{ stats.successfulCount || 0 }}</div>
            <div class="stat-label">成功申请</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value danger">{{ stats.rejectedCount || 0 }}</div>
            <div class="stat-label">被驳回</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value info">{{ (stats.successRate || 0).toFixed(1) }}%</div>
            <div class="stat-label">成功率</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Plus, DocumentChecked, TrendCharts, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { arbitrationApi } from '@/api/arbitration'
import { format } from 'date-fns'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const activeTab = ref('all')
const searchKeyword = ref('')
const arbitrationList = ref([])
const stats = ref({})

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 状态值转换辅助函数
const getStatusValue = (tabKey) => {
  const statusMap = {
    'pending': 0,    // 待处理
    'processing': 1, // 处理中
    'completed': 2,  // 已完结
    'rejected': 3    // 已驳回
  }
  return statusMap[tabKey]
}

// 加载仲裁列表
const loadArbitrationList = async () => {
  loading.value = true

  try {
    // 调用真实API获取用户仲裁列表
    const params = {
      current: pagination.current,
      size: pagination.size,
      status: activeTab.value === 'all' ? undefined : getStatusValue(activeTab.value),
      keyword: searchKeyword.value
    }

    const response = await arbitrationApi.getUserArbitrationList(params)

    if (response.data) {
      arbitrationList.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      arbitrationList.value = []
      pagination.total = 0
    }

  } catch (error) {
    console.error('加载仲裁列表失败:', error)
    ElMessage.error('加载仲裁列表失败：' + (error.message || '未知错误'))

    // 如果API调用失败，使用模拟数据作为fallback
    console.warn('API调用失败，使用模拟数据作为备用方案')
    const mockData = {
      data: {
        records: [
          {
            id: 1,
            orderId: 100001,
            orderNo: 'ORDER20240001',
            respondentId: 2,
            respondentName: '张小明',
            respondentAvatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100',
            reason: 'QUALITY_ISSUE',
            description: '收到的商品与描述不符，存在明显质量问题，要求退货退款。商品包装完好但内容物有严重损坏，无法正常使用。',
            evidence: ['http://example.com/img1.jpg'],
            status: 0,
            result: null,
            createTime: new Date('2024-04-02 10:30:00'),
            updateTime: new Date('2024-04-02 10:30:00')
          },
          {
            id: 2,
            orderId: 100002,
            orderNo: 'ORDER20240002',
            respondentId: 3,
            respondentName: '李小红',
            respondentAvatar: 'https://images.unsplash.com/photo-1494790108755-2616b612d5db?w=100',
            reason: 'SHIPPING_DELAY',
            description: '卖家承诺3天内发货，但已超过一周仍未发货，严重影响使用。多次联系客服无果。',
            evidence: ['http://example.com/chat1.jpg'],
            status: 1,
            result: null,
            createTime: new Date('2024-04-04 15:20:00'),
            updateTime: new Date('2024-04-05 09:15:00')
          },
          {
            id: 3,
            orderId: 100003,
            orderNo: 'ORDER20240003',
            respondentId: 4,
            respondentName: '王小华',
            respondentAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100',
            reason: 'DESCRIPTION_MISMATCH',
            description: '商品实际规格与页面描述完全不符，属于虚假宣传。页面显示为全新商品，实际收到二手产品。',
            evidence: ['http://example.com/img2.jpg', 'http://example.com/img3.jpg'],
            status: 2,
            result: '经核实，商品确实存在描述不符的问题。裁决：支持买家退货，卖家承担邮费，并扣除信用分10分作为警告。',
            createTime: new Date('2024-03-25 09:15:00'),
            updateTime: new Date('2024-03-28 16:30:00')
          },
          {
            id: 4,
            orderId: 100004,
            orderNo: 'ORDER20240004',
            respondentId: 5,
            respondentName: '赵小丽',
            respondentAvatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100',
            reason: 'NO_RESPONSE',
            description: '多次联系卖家咨询商品问题，卖家完全不回复消息，售后服务极差。',
            evidence: ['http://example.com/chat2.jpg'],
            status: 3,
            result: '经调查，卖家确实存在沟通不及时的问题，但商品本身无质量问题，且买家未在合理时间内申请退货。驳回仲裁申请。',
            createTime: new Date('2024-03-20 14:45:00'),
            updateTime: new Date('2024-03-22 11:20:00')
          }
        ],
        total: 4,
        current: pagination.current,
        size: pagination.size
      }
    }

    // 根据状态筛选模拟数据（仅作为fallback使用）
    let filteredData = mockData.data.records
    if (activeTab.value !== 'all') {
      const statusValue = getStatusValue(activeTab.value)
      if (statusValue !== undefined) {
        filteredData = filteredData.filter(item => item.status === statusValue)
      }
    }

    // 根据关键字搜索
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      filteredData = filteredData.filter(item =>
        item.orderNo.toLowerCase().includes(keyword) ||
        item.description.toLowerCase().includes(keyword) ||
        item.respondentName.toLowerCase().includes(keyword)
      )
    }

    arbitrationList.value = filteredData
    pagination.total = filteredData.length
  }

  loading.value = false
}

// 加载统计数据
const loadStats = async () => {
  try {
    // 暂时使用模拟数据
    stats.value = {
      totalCount: 4,
      successfulCount: 1,
      rejectedCount: 1,
      successRate: 25.0
    }

    // 后续启用真实API
    /*
    const result = await arbitrationApi.getUserArbitrationStats()
    stats.value = result.data || stats.value
    */
  } catch (error) {
    console.error('加载统计数据失败：', error)
    // 使用默认数据
    stats.value = {
      totalCount: 0,
      successfulCount: 0,
      rejectedCount: 0,
      successRate: 0.0
    }
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    0: 'warning',  // 待处理
    1: 'primary',  // 处理中
    2: 'success',  // 已完结
    3: 'danger'    // 已驳回
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
    'QUALITY_ISSUE': '商品质量问题',
    'SHIPPING_DELAY': '发货延迟',
    'DESCRIPTION_MISMATCH': '商品描述不符',
    'NO_RESPONSE': '卖家无响应',
    'OTHER': '其他问题'
  }
  return texts[reason] || '其他问题'
}

// 格式化日期
const formatDate = (date) => {
  return format(new Date(date), 'yyyy-MM-dd HH:mm')
}

// 跳转详情页
const goToDetail = (id) => {
  router.push(`/arbitration/detail/${id}`)
}

// 取消仲裁申请
const cancelArbitration = async (arbitration) => {
  try {
    await ElMessageBox.confirm(
      `确认取消仲裁申请 "AR${String(arbitration.id).padStart(6, '0')}" ？`,
      '确认取消',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '保留',
        type: 'warning'
      }
    )

    // 调用真实API取消仲裁申请
    try {
      await arbitrationApi.cancelArbitration(arbitration.id)
      ElMessage.success('仲裁申请已取消')
      loadArbitrationList() // 重新加载列表
    } catch (apiError) {
      // API调用失败时显示提示信息
      ElMessage.warning('取消仲裁功能暂未开放，请联系客服处理')
      console.warn('取消仲裁API调用失败:', apiError)
    }

  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error.message || '未知错误'))
    }
  }
}

// 评价处理
const rateHandler = (arbitration) => {
  ElMessage.info('评价功能待实现')
}

// 页面初始化
onMounted(() => {
  loadArbitrationList()
  loadStats()
})
</script>

<style scoped>
.arbitration-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.arbitration-list {
  min-height: 400px;
}

.arbitration-item {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  margin-bottom: 16px;
  background: white;
  transition: all 0.3s ease;
  cursor: pointer;
}

.arbitration-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.arbitration-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f5f7fa;
  background: #fafbfc;
  border-radius: 12px 12px 0 0;
}

.arbitration-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.arbitration-id {
  font-weight: 500;
  color: #303133;
}

.arbitration-time {
  font-size: 14px;
  color: #999;
}

.arbitration-content {
  padding: 20px;
}

.order-section {
  margin-bottom: 16px;
}

.order-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.arbitration-reason {
  margin-top: 8px;
}

.description-section {
  margin-bottom: 16px;
}

.description-text {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.respondent-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #666;
}

.result-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}

.result-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.result-content {
  color: #606266;
  line-height: 1.6;
}

.arbitration-actions {
  padding: 16px 20px;
  border-top: 1px solid #f5f7fa;
  text-align: right;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: center;
}

.stats-card {
  margin-top: 20px;
}

.stats-card h4 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.stats-row {
  margin-top: 16px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.danger {
  color: #f56c6c;
}

.stat-value.info {
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.empty-state {
  padding: 80px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .arbitration-list-page {
    padding: 10px;
  }

  .filter-bar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .arbitration-header {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }

  .arbitration-actions {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .stats-row .el-col {
    margin-bottom: 20px;
  }
}
</style>
