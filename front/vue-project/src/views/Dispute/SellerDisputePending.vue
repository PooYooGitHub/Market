<template>
  <div class="seller-dispute-page">
    <el-card shadow="hover">
      <template #header>
        <div class="header-row">
          <h3>卖家待响应争议</h3>
          <el-button @click="loadList">刷新</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="id" label="争议ID" width="100" />
        <el-table-column prop="orderId" label="订单ID" width="120" />
        <el-table-column prop="reason" label="争议原因" width="160" />
        <el-table-column prop="statusLabel" label="状态" width="160" />
        <el-table-column label="剩余时间" width="160">
          <template #default="{ row }">{{ formatCountdown(row.expireTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="goDetail(row.id)">响应协商</el-button>
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

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10 })

const formatCountdown = (expireTime) => {
  if (!expireTime) return '-'
  const diff = new Date(expireTime).getTime() - Date.now()
  if (diff <= 0) return '已超时'
  const h = Math.floor(diff / (1000 * 60 * 60))
  const m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  return `${h}小时${m}分`
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await arbitrationApi.getSellerPendingDisputes(query)
    list.value = res?.data?.records || []
    total.value = Number(res?.data?.total || 0)
  } catch (error) {
    ElMessage.error(error?.message || '加载失败')
  } finally {
    loading.value = false
  }
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

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>

