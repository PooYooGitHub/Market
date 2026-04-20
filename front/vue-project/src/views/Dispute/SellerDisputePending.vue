<template>
  <div class="seller-dispute-page">
    <el-card shadow="hover">
      <template #header>
        <div class="header-row">
          <h3>卖家争议中心</h3>
          <el-button @click="loadList">刷新</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="待处理" name="pending" />
        <el-tab-pane label="全部争议" name="all" />
      </el-tabs>

      <div v-if="activeTab === 'all'" class="filter-row">
        <el-select v-model="statusFilter" placeholder="按状态筛选" clearable style="width: 240px" @change="loadList">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="id" label="争议ID" width="100" />
        <el-table-column prop="orderId" label="订单ID" width="120" />
        <el-table-column label="争议原因" width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ getDisputeReasonLabel(row.reason) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="180">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.statusLabel || getDisputeStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="截止时间" width="180">
          <template #default="{ row }">{{ formatCountdown(row.expireTime) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="goDetail(row.id)">
              {{ canRespond(row.status) ? '响应协商' : '查看详情' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { arbitrationApi } from '@/api/arbitration'
import { getDisputeReasonLabel, getDisputeStatusLabel } from '@/utils/disputeEnums'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const activeTab = ref('pending')
const statusFilter = ref('')
const query = reactive({ current: 1, size: 10 })

const statusOptions = [
  { label: '待卖家响应', value: 'WAIT_SELLER_RESPONSE' },
  { label: '待买家确认', value: 'WAIT_BUYER_CONFIRM' },
  { label: '协商成功', value: 'NEGOTIATION_SUCCESS' },
  { label: '协商失败', value: 'NEGOTIATION_FAILED' },
  { label: '卖家超时', value: 'SELLER_TIMEOUT' },
  { label: '已升级仲裁', value: 'ESCALATED_TO_ARBITRATION' },
  { label: '平台已裁决', value: 'ARBITRATION_DECIDED' },
  { label: '裁决执行中', value: 'ARBITRATION_EXECUTING' },
  { label: '裁决执行完成', value: 'ARBITRATION_EXECUTED' },
  { label: '已关闭', value: 'CLOSED' }
]

const canRespond = (status) => ['WAIT_SELLER_RESPONSE', 'SELLER_TIMEOUT'].includes(status)

const statusTagType = (status) => {
  if (['NEGOTIATION_SUCCESS', 'ARBITRATION_EXECUTED', 'CLOSED'].includes(status)) return 'success'
  if (['NEGOTIATION_FAILED', 'ESCALATED_TO_ARBITRATION'].includes(status)) return 'warning'
  if (['WAIT_SELLER_RESPONSE', 'SELLER_TIMEOUT', 'WAIT_BUYER_CONFIRM', 'ARBITRATION_DECIDED', 'ARBITRATION_EXECUTING'].includes(status)) return 'info'
  return 'info'
}

const formatCountdown = (expireTime) => {
  if (!expireTime) return '-'
  const diff = new Date(expireTime).getTime() - Date.now()
  if (diff <= 0) return '已超时'
  const h = Math.floor(diff / (1000 * 60 * 60))
  const m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  return `${h}小时${m}分钟`
}

const buildParams = () => {
  const params = { current: query.current, size: query.size }
  if (activeTab.value === 'all' && statusFilter.value) {
    params.status = statusFilter.value
  }
  return params
}

const loadList = async () => {
  loading.value = true
  try {
    const params = buildParams()
    const res = activeTab.value === 'pending'
      ? await arbitrationApi.getSellerPendingDisputes(params)
      : await arbitrationApi.getSellerAllDisputes(params)
    list.value = res?.data?.records || []
    total.value = Number(res?.data?.total || 0)
  } catch (error) {
    ElMessage.error(error?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const onTabChange = () => {
  query.current = 1
  loadList()
}

const goDetail = (id) => {
  router.push(`/dispute/seller/detail/${id}`)
}

onMounted(loadList)
</script>

<style scoped>
.seller-dispute-page {
  padding: 20px 0;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-row {
  margin-bottom: 12px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
