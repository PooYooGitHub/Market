<template>
  <div class="dispute-list-page">
    <el-card shadow="hover">
      <template #header>
        <div class="header-row">
          <h3>我的争议协商</h3>
          <el-button type="primary" @click="$router.push('/dispute/apply')">发起争议</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="id" label="争议ID" width="100" />
        <el-table-column prop="orderId" label="订单ID" width="120" />
        <el-table-column prop="reason" label="原因" width="160" />
        <el-table-column prop="requestType" label="诉求类型" width="140" />
        <el-table-column prop="expectedAmount" label="期望金额" width="120" />
        <el-table-column label="状态" width="170">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ row.statusLabel || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="倒计时" width="170">
          <template #default="{ row }">
            {{ formatCountdown(row.expireTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="goDetail(row.id)">详情</el-button>
            <el-button v-if="row.canEscalate" size="small" type="danger" @click="escalate(row)">升级仲裁</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { arbitrationApi } from '@/api/arbitration'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10 })

const statusType = (status) => {
  if (status === 'NEGOTIATION_SUCCESS') return 'success'
  if (status === 'NEGOTIATION_FAILED' || status === 'SELLER_TIMEOUT') return 'warning'
  if (status === 'ESCALATED_TO_ARBITRATION') return 'danger'
  return 'info'
}

const formatCountdown = (expireTime) => {
  if (!expireTime) return '-'
  const diff = new Date(expireTime).getTime() - Date.now()
  if (Number.isNaN(diff) || diff <= 0) return '已超时'
  const h = Math.floor(diff / (1000 * 60 * 60))
  const m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  return `${h}小时${m}分`
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await arbitrationApi.getMyDisputeList(query)
    list.value = res?.data?.records || []
    total.value = Number(res?.data?.total || 0)
  } catch (error) {
    ElMessage.error(error?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => {
  router.push(`/dispute/detail/${id}`)
}

const escalate = async (row) => {
  try {
    await ElMessageBox.confirm(`确认将争议 #${row.id} 升级到管理员仲裁吗？`, '升级仲裁', {
      confirmButtonText: '确认升级',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await arbitrationApi.escalateDispute({
      disputeId: row.id,
      escalateReason: '买家主动升级仲裁'
    })
    ElMessage.success(`升级成功，仲裁ID：${res?.data || '-'}`)
    loadList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '升级失败')
    }
  }
}

onMounted(loadList)
</script>

<style scoped>
.dispute-list-page {
  padding: 20px 0;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>

