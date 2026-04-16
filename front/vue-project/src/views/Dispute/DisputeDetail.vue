<template>
  <div class="dispute-detail-page" v-loading="loading">
    <el-card v-if="detail" shadow="hover">
      <template #header>
        <div class="header-row">
          <h3>争议详情 #{{ detail.id }}</h3>
          <div>
            <el-tag :type="statusType(detail.status)">{{ detail.statusLabel || detail.status }}</el-tag>
            <el-button style="margin-left: 8px" @click="$router.go(-1)">返回</el-button>
          </div>
        </div>
      </template>

      <el-descriptions border :column="2">
        <el-descriptions-item label="订单ID">{{ detail.orderId }}</el-descriptions-item>
        <el-descriptions-item label="商品ID">{{ detail.productId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="争议原因">{{ detail.reason }}</el-descriptions-item>
        <el-descriptions-item label="诉求类型">{{ detail.requestType }}</el-descriptions-item>
        <el-descriptions-item label="期望金额">¥{{ formatAmount(detail.expectedAmount) }}</el-descriptions-item>
        <el-descriptions-item label="倒计时">{{ countdownText }}</el-descriptions-item>
        <el-descriptions-item label="事实说明" :span="2">{{ detail.factDescription }}</el-descriptions-item>
        <el-descriptions-item label="诉求说明" :span="2">{{ detail.requestDescription }}</el-descriptions-item>
      </el-descriptions>

      <el-card v-if="detail.sellerProposal" class="sub-card" shadow="never">
        <template #header><span>卖家协商方案</span></template>
        <el-descriptions border :column="2">
          <el-descriptions-item label="方案类型">{{ detail.sellerProposal.proposalType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="方案金额">¥{{ formatAmount(detail.sellerProposal.proposalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="运费承担">{{ detail.sellerProposal.freightBearer || '-' }}</el-descriptions-item>
          <el-descriptions-item label="方案说明" :span="2">{{ detail.sellerProposal.proposalDescription || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="sub-card" shadow="never">
        <template #header><span>平台裁决与执行进度</span></template>
        <el-descriptions border :column="1">
          <el-descriptions-item label="裁决类型">
            {{ decisionTypeLabel(detail.finalDecisionType) }}
          </el-descriptions-item>
          <el-descriptions-item label="执行状态">
            {{ detail.executionStatusLabel || detail.finalExecutionStatus || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="结果说明">
            {{ detail.finalResultDescription || detail.executionRemark || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="下一步提示">
            {{ detail.nextActionHint || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="sub-card" shadow="never">
        <template #header><span>协商记录摘要</span></template>
        <div class="summary">{{ detail.negotiationSummary || '-' }}</div>
      </el-card>

      <el-card class="sub-card" shadow="never">
        <template #header><span>聊天摘要</span></template>
        <div class="chat-list">
          <div v-for="(item, idx) in detail.chatSummary || []" :key="item.id || idx" class="chat-item">
            <div class="chat-meta">
              <span class="speaker">{{ item.speaker || '-' }}</span>
              <span class="time">{{ formatTime(item.time) }}</span>
            </div>
            <div class="chat-content">{{ item.content || '-' }}</div>
          </div>
        </div>
      </el-card>

      <el-card class="sub-card" shadow="never">
        <template #header><span>协商时间线</span></template>
        <el-timeline>
          <el-timeline-item
            v-for="log in detail.negotiationLogs"
            :key="log.id"
            :timestamp="formatTime(log.createTime)"
          >
            <div><strong>{{ log.actionLabel || log.actionType }}</strong></div>
            <div>{{ log.content }}</div>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <div class="action-row">
        <el-button v-if="detail.canBuyerConfirm" type="success" @click="confirmProposal(true)">接受方案</el-button>
        <el-button v-if="detail.canBuyerConfirm" type="danger" @click="confirmProposal(false)">拒绝并升级仲裁</el-button>
        <el-button v-if="detail.canEscalate" type="danger" plain @click="escalate">升级仲裁</el-button>
        <el-tag v-if="detail.escalatedArbitrationId" type="danger">已升级仲裁：{{ detail.escalatedArbitrationId }}</el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { arbitrationApi } from '@/api/arbitration'

const route = useRoute()
const loading = ref(false)
const detail = ref(null)

const statusType = (status) => {
  if (status === 'NEGOTIATION_SUCCESS') return 'success'
  if (status === 'NEGOTIATION_FAILED' || status === 'SELLER_TIMEOUT') return 'warning'
  if (status === 'ESCALATED_TO_ARBITRATION') return 'danger'
  if (status === 'ARBITRATION_DECIDED' || status === 'ARBITRATION_EXECUTING') return 'warning'
  if (status === 'ARBITRATION_EXECUTED' || status === 'CLOSED') return 'success'
  return 'info'
}

const decisionTypeLabel = (value) => {
  const map = {
    SUPPORT_FULL_REFUND: '支持全额退款',
    SUPPORT_PARTIAL_REFUND: '支持部分退款',
    SUPPORT_RETURN_AND_REFUND: '支持退货退款',
    SUPPORT_REPLACE: '支持换货/补发',
    REJECT_BUYER_REQUEST: '驳回买家诉求',
    REQUIRE_SUPPLEMENT: '要求补充材料',
    CLOSE_WITH_NEGOTIATION_RESULT: '按协商结果结案',
    OTHER: '其他'
  }
  return map[value] || '-'
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return String(time)
  return date.toLocaleString('zh-CN')
}

const formatAmount = (amount) => {
  const value = Number(amount || 0)
  return Number.isNaN(value) ? '0.00' : value.toFixed(2)
}

const countdownText = computed(() => {
  const seconds = Number(detail.value?.countdownSeconds || 0)
  if (seconds <= 0) return '-'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  return `${h}小时${m}分钟`
})

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await arbitrationApi.getDisputeDetail(route.params.id)
    detail.value = res?.data || null
  } catch (error) {
    ElMessage.error(error?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const confirmProposal = async (accept) => {
  try {
    const tip = accept ? '确认接受卖家方案？' : '确认拒绝方案并升级仲裁？'
    await ElMessageBox.confirm(tip, '买家确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await arbitrationApi.buyerConfirmProposal({
      disputeId: detail.value.id,
      acceptProposal: accept,
      remark: accept ? '买家接受方案' : '买家拒绝方案并升级仲裁'
    })
    ElMessage.success('操作成功')
    await loadDetail()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error?.message || '操作失败')
  }
}

const escalate = async () => {
  try {
    await ElMessageBox.confirm('确认升级仲裁吗？', '升级仲裁', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await arbitrationApi.escalateDispute({
      disputeId: detail.value.id,
      escalateReason: '买家主动升级仲裁'
    })
    ElMessage.success(`升级成功，仲裁ID：${res?.data || '-'}`)
    await loadDetail()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error?.message || '升级失败')
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.dispute-detail-page {
  padding: 20px 0;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sub-card {
  margin-top: 16px;
}

.summary {
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chat-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 10px;
  background: #fafafa;
}

.chat-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.chat-content {
  margin-top: 6px;
  color: #303133;
  line-height: 1.6;
}

.action-row {
  margin-top: 18px;
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
</style>

