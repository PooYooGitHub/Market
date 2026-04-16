<template>
  <div class="case-workbench" v-loading="loading">
    <div class="workbench-topbar">
      <div>
        <h2 class="page-title">管理员仲裁详情工作台</h2>
        <p class="page-subtitle">聚焦争议焦点、裁决结果与执行进度，完成闭环处理</p>
      </div>
      <div class="toolbar-actions">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <el-button :loading="loading" @click="loadCaseDetail">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <el-alert
      v-if="loadError"
      type="error"
      :closable="false"
      class="module-gap"
      :title="loadError"
    />

    <el-empty
      v-if="!loading && !caseDetail && !loadError"
      description="未找到案件数据"
      class="module-gap"
    />

    <CaseSummaryCard v-if="caseDetail" :case-data="caseDetail">
      <template #actions>
        <el-button
          v-for="action in quickActions"
          :key="action.key"
          :type="action.type"
          :plain="action.plain"
          @click="handleQuickAction(action.key)"
        >
          {{ action.label }}
        </el-button>
      </template>
    </CaseSummaryCard>

    <el-row v-if="caseDetail" :gutter="16" class="main-layout">
      <el-col :xs="24" :lg="17" class="left-col">
        <el-card class="module-card dispute-focus-card" shadow="never">
          <template #header>
            <div class="module-header">
              <span class="module-title">争议焦点</span>
              <span class="module-desc">办案摘要</span>
            </div>
          </template>

          <div class="focus-grid">
            <div class="focus-item buyer">
              <div class="focus-label">买家事实主张</div>
              <div class="focus-content">{{ caseDetail.buyerClaim || '-' }}</div>
            </div>
            <div class="focus-item seller">
              <div class="focus-label">卖家主张</div>
              <div class="focus-content">{{ caseDetail.sellerClaim || '-' }}</div>
            </div>
            <div class="focus-item platform">
              <div class="focus-label">平台关注点</div>
              <div class="focus-content">{{ caseDetail.platformFocus || '-' }}</div>
            </div>
            <div class="focus-item request">
              <div class="focus-label">仲裁请求（诉求）</div>
              <div class="focus-content">{{ caseDetail.arbitrationRequest || '未填写明确诉求' }}</div>
            </div>
          </div>
        </el-card>

        <el-card class="module-card module-gap" shadow="never">
          <template #header>
            <div class="module-header">
              <span class="module-title">协商记录摘要</span>
              <span class="module-desc">来源争议ID：{{ caseDetail.sourceDisputeId || '-' }}</span>
            </div>
          </template>
          <div class="negotiation-summary">
            {{ caseDetail.negotiationSummary || '-' }}
          </div>
        </el-card>

        <EvidenceComparePanel
          class="module-gap"
          :applicant-evidence="caseDetail.applicantEvidence"
          :respondent-evidence="caseDetail.respondentEvidence"
        />

        <el-card class="module-card module-gap" shadow="never">
          <template #header>
            <div class="module-header">
              <span class="module-title">平台辅助信息</span>
              <span class="module-desc">辅助判断，不抢主视觉</span>
            </div>
          </template>

          <el-tabs v-model="activeAssistTab" class="assist-tabs">
            <el-tab-pane label="聊天摘要" name="chat">
              <div class="chat-summary-list">
                <div
                  v-for="item in caseDetail.chatSummary"
                  :key="item.id"
                  class="chat-item"
                >
                  <div class="chat-meta">
                    <span class="speaker">{{ item.speaker }}</span>
                    <span class="time">{{ item.time }}</span>
                  </div>
                  <div class="chat-content">{{ item.content }}</div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="商品快照" name="product">
              <div class="snapshot-product">
                <el-image
                  :src="caseDetail.productImage"
                  :preview-src-list="caseDetail.productImage ? [caseDetail.productImage] : []"
                  fit="cover"
                  class="product-image"
                  preview-teleported
                />
                <div class="snapshot-info-grid">
                  <div class="snapshot-item">
                    <span class="k">商品名称</span>
                    <span class="v">{{ caseDetail.productName || '-' }}</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="k">商品标价</span>
                    <span class="v">¥{{ formatAmount(caseDetail.productPrice) }}</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="k">当前订单金额</span>
                    <span class="v">¥{{ formatAmount(caseDetail.orderAmount) }}</span>
                  </div>
                  <div class="snapshot-item full">
                    <span class="k">商品描述</span>
                    <span class="v">{{ caseDetail.productSnapshot.description || '-' }}</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="订单快照" name="order">
              <div class="snapshot-info-grid order-grid">
                <div class="snapshot-item">
                  <span class="k">订单号</span>
                  <span class="v">{{ caseDetail.orderSnapshot.orderNo || '-' }}</span>
                </div>
                <div class="snapshot-item">
                  <span class="k">订单状态</span>
                  <span class="v">{{ caseDetail.orderSnapshot.status || '-' }}</span>
                </div>
                <div class="snapshot-item">
                  <span class="k">下单时间</span>
                  <span class="v">{{ caseDetail.orderSnapshot.createTime || '-' }}</span>
                </div>
                <div class="snapshot-item">
                  <span class="k">成交金额</span>
                  <span class="v">¥{{ formatAmount(caseDetail.orderSnapshot.amount) }}</span>
                </div>
                <div class="snapshot-item full">
                  <span class="k">订单备注</span>
                  <span class="v">{{ caseDetail.orderSnapshot.note || '-' }}</span>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="系统证据" name="system">
              <div class="system-evidence-list">
                <div
                  v-for="item in caseDetail.systemEvidence"
                  :key="item.id"
                  class="system-evidence-item"
                >
                  <div class="system-main">
                    <div class="system-top">
                      <span class="system-title">{{ item.title }}</span>
                      <el-tag size="small" :type="getEvidenceTagType(item.type)">
                        {{ getEvidenceTypeLabel(item.type) }}
                      </el-tag>
                    </div>
                    <div class="system-desc">{{ item.description }}</div>
                    <div class="system-time">{{ item.createTime }}</div>
                  </div>
                  <el-image
                    v-if="item.type === 'image'"
                    :src="item.thumbnail || item.url"
                    :preview-src-list="item.url || item.thumbnail ? [item.url || item.thumbnail] : []"
                    fit="cover"
                    class="system-thumb"
                    preview-teleported
                  />
                  <div v-else class="system-icon-box">
                    <el-icon><DataAnalysis /></el-icon>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>

        <el-card class="module-card module-gap" shadow="never">
          <template #header>
            <div class="module-header">
              <span class="module-title">处理记录时间线</span>
              <span class="module-desc">裁决阶段 + 执行阶段</span>
            </div>
          </template>

          <el-timeline>
            <el-timeline-item
              v-for="item in caseDetail.timeline"
              :key="item.id"
              :timestamp="item.time"
              :color="item.color"
            >
              <div class="timeline-title">{{ item.title }}</div>
              <div class="timeline-desc">{{ item.description }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="7" class="right-col">
        <div class="sticky-wrap">
          <el-card class="module-card decision-panel" shadow="never">
            <template #header>
              <div class="module-header">
                <span class="module-title">处理面板</span>
                <el-tag :type="statusInfo.type">{{ statusInfo.label }}</el-tag>
              </div>
            </template>

            <div class="panel-section">
              <div class="panel-label">风险提示</div>
              <div class="risk-list">
                <div v-for="(tip, idx) in riskTips" :key="idx" class="risk-item">
                  {{ tip }}
                </div>
              </div>
            </div>

            <div class="panel-section recommend-section">
              <div class="panel-label">推荐动作</div>
              <div class="recommend-text">{{ recommendedAction }}</div>
            </div>

            <div class="panel-section">
              <div class="panel-label">裁决类型</div>
              <el-select
                v-model="decisionType"
                :disabled="isReadonly"
                placeholder="请选择裁决类型"
                style="width: 100%"
              >
                <el-option
                  v-for="item in decisionTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </div>

            <div class="panel-section">
              <div class="panel-label">处理备注</div>
              <el-input
                v-model="decisionRemark"
                type="textarea"
                :rows="4"
                resize="none"
                :disabled="isReadonly"
                placeholder="请输入裁决依据与处理说明"
              />
            </div>

            <div class="panel-section execution-box">
              <div class="panel-label">执行信息</div>
              <div class="execution-line">
                <span>执行动作</span>
                <strong>{{ caseDetail.executionTypeLabel || '-' }}</strong>
              </div>
              <div class="execution-line">
                <span>执行状态</span>
                <strong>{{ caseDetail.executionStatusLabel || '-' }}</strong>
              </div>
              <div class="execution-line">
                <span>执行备注</span>
                <strong>{{ caseDetail.executionRemark || '-' }}</strong>
              </div>
            </div>

            <div v-if="isReadonly" class="result-panel">
              <div class="result-title">处理结果</div>
              <div class="result-status">{{ statusInfo.label }}</div>
              <div class="result-remark">
                {{ caseDetail.decisionRemark || caseDetail.rejectReason || '-' }}
              </div>
            </div>

            <div v-else class="action-buttons">
              <el-button
                v-if="canAccept"
                type="primary"
                class="full-btn"
                @click="handleAccept"
              >
                受理
              </el-button>
              <el-button
                v-if="canComplete"
                type="success"
                class="full-btn"
                @click="handleComplete"
              >
                提交裁决
              </el-button>
              <el-button
                v-if="canReject"
                type="danger"
                plain
                class="full-btn"
                @click="handleReject"
              >
                驳回
              </el-button>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, DataAnalysis, Refresh } from '@element-plus/icons-vue'
import { arbitrationApi } from '@/api/arbitration'
import CaseSummaryCard from './components/CaseSummaryCard.vue'
import EvidenceComparePanel from './components/EvidenceComparePanel.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const loadError = ref('')
const caseDetail = ref(null)
const activeAssistTab = ref('chat')
const decisionRemark = ref('')
const decisionType = ref('SUPPORT_FULL_REFUND')

const decisionTypeOptions = [
  { value: 'SUPPORT_FULL_REFUND', label: '支持全额退款' },
  { value: 'SUPPORT_PARTIAL_REFUND', label: '支持部分退款' },
  { value: 'SUPPORT_RETURN_AND_REFUND', label: '支持退货退款' },
  { value: 'SUPPORT_REPLACE', label: '支持换货/补发' },
  { value: 'REJECT_BUYER_REQUEST', label: '驳回买家诉求' },
  { value: 'REQUIRE_SUPPLEMENT', label: '要求补充材料' },
  { value: 'CLOSE_WITH_NEGOTIATION_RESULT', label: '按协商结果结案' },
  { value: 'OTHER', label: '其他' }
]

const statusMap = {
  pending: { label: '待处理', type: 'warning' },
  processing: { label: '处理中', type: 'primary' },
  decided: { label: '已裁决', type: 'warning' },
  completed: { label: '已执行完成', type: 'success' },
  rejected: { label: '已驳回', type: 'danger' }
}

const statusInfo = computed(() => {
  const status = caseDetail.value?.status || 'pending'
  return statusMap[status] || statusMap.pending
})

const canAccept = computed(() => Boolean(caseDetail.value?.canAccept))
const canComplete = computed(() => Boolean(caseDetail.value?.canComplete))
const canReject = computed(() => Boolean(caseDetail.value?.canReject))
const isReadonly = computed(() => Boolean(caseDetail.value?.readonly))

const quickActions = computed(() => {
  if (!caseDetail.value) return []
  if (canAccept.value) {
    return [
      { key: 'accept', label: '立即受理', type: 'primary', plain: false },
      { key: 'reject', label: '直接驳回', type: 'danger', plain: true }
    ]
  }
  if (canComplete.value) {
    return [
      { key: 'complete', label: '提交裁决', type: 'success', plain: false },
      { key: 'reject', label: '驳回申请', type: 'danger', plain: true }
    ]
  }
  return [{ key: 'readonly', label: '查看结果', type: 'info', plain: true }]
})

const riskTips = computed(() => {
  if (!caseDetail.value) return []
  const tips = []
  const diffCount = Math.abs(
    (caseDetail.value.applicantEvidence || []).length - (caseDetail.value.respondentEvidence || []).length
  )

  if (canAccept.value) tips.push('案件尚未受理，存在处理时效风险')
  if (canComplete.value) tips.push('处理中案件，建议本轮给出明确裁决类型')
  if (diffCount >= 2) tips.push('双方证据量差异较大，建议重点核验系统记录')
  if (Number(caseDetail.value.orderAmount || 0) >= 500) tips.push('争议金额较高，建议补充完整裁决依据')
  if (!tips.length) tips.push('当前案件已进入执行阶段，请重点关注执行状态')
  return tips.slice(0, 3)
})

const recommendedAction = computed(() => {
  if (!caseDetail.value) return '-'
  if (canAccept.value) return '建议先受理，再核对聊天摘要与系统证据是否一致。'
  if (canComplete.value) return '请先选择裁决类型，再提交裁决并观察执行任务状态。'
  if (caseDetail.value.status === 'decided') return '案件已裁决，正在执行中或待人工处理。'
  return '案件已结束，可复核处理结果并保留备注用于后续追溯。'
})

const loadCaseDetail = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const id = route.params.id
    if (!id) {
      caseDetail.value = null
      return
    }

    const response = await arbitrationApi.getAdminArbitrationDetail(id)
    const detail = response?.data
    if (!detail) {
      caseDetail.value = null
      return
    }

    caseDetail.value = normalizeCaseDetail(detail)
    decisionRemark.value = caseDetail.value.decisionRemark || caseDetail.value.rejectReason || ''
    decisionType.value = caseDetail.value.decisionType || 'SUPPORT_FULL_REFUND'
    activeAssistTab.value = 'chat'
  } catch (error) {
    caseDetail.value = null
    loadError.value = error?.message || '案件数据加载失败'
    ElMessage.error(loadError.value)
  } finally {
    loading.value = false
  }
}

const normalizeCaseDetail = (detail) => {
  const statusCode = Number(detail.status ?? 0)
  const status = statusCodeToKey(statusCode)
  const orderSnapshot = {
    ...(detail.orderSnapshot || {}),
    createTime: formatDateTime(detail.orderSnapshot?.createTime),
    updateTime: formatDateTime(detail.orderSnapshot?.updateTime)
  }

  return {
    ...detail,
    id: detail.id,
    status,
    caseNumber: detail.caseNumber || `ARB${String(detail.id || '').padStart(6, '0')}`,
    applicant: detail.applicantName || (detail.applicantId ? `用户${detail.applicantId}` : '-'),
    respondent: detail.respondentName || (detail.respondentId ? `用户${detail.respondentId}` : '-'),
    handler: detail.handlerName || '待分配',
    createTime: formatDateTime(detail.createTime),
    buyerClaim: detail.buyerClaim || '-',
    sellerClaim: detail.sellerClaim || '-',
    platformFocus: detail.platformFocus || '-',
    arbitrationRequest: detail.arbitrationRequest || '未填写明确诉求',
    sourceDisputeId: detail.sourceDisputeId || null,
    negotiationSummary: detail.negotiationSummary || '-',
    applicantEvidence: normalizeEvidence(detail.applicantEvidence),
    respondentEvidence: normalizeEvidence(detail.respondentEvidence),
    systemEvidence: normalizeEvidence(detail.systemEvidence),
    chatSummary: normalizeChatSummary(detail.chatSummary),
    orderSnapshot,
    productSnapshot: {
      ...(detail.productSnapshot || {}),
      description: detail.productSnapshot?.description || '-'
    },
    timeline: normalizeTimeline(detail.timeline),
    canAccept: Boolean(detail.canAccept),
    canComplete: Boolean(detail.canComplete),
    canReject: Boolean(detail.canReject),
    readonly: Boolean(detail.readonly),
    decisionType: detail.decisionType || '',
    decisionTypeLabel: detail.decisionTypeLabel || '',
    decisionRemark: detail.decisionRemark || '',
    executionType: detail.executionType || '',
    executionTypeLabel: detail.executionTypeLabel || '',
    executionStatus: detail.executionStatus || '',
    executionStatusLabel: detail.executionStatusLabel || '',
    executionRemark: detail.executionRemark || '',
    rejectReason: detail.rejectReason || ''
  }
}

const normalizeEvidence = (list) => {
  if (!Array.isArray(list)) return []
  return list.map((item, index) => ({
    id: item.id || `${index + 1}`,
    type: item.type || 'text',
    title: item.title || '证据材料',
    description: item.description || '',
    url: item.url || '',
    thumbnail: item.thumbnail || item.url || '',
    createTime: formatDateTime(item.createTime)
  }))
}

const normalizeChatSummary = (list) => {
  if (!Array.isArray(list) || !list.length) {
    return [
      {
        id: 'empty',
        speaker: '系统',
        time: '',
        content: '暂无可用聊天摘要'
      }
    ]
  }
  return list.map((item, index) => ({
    id: item.id || `${index + 1}`,
    speaker: item.speaker || '平台',
    time: formatDateTime(item.time),
    content: item.content || '暂无可用聊天摘要'
  }))
}

const normalizeTimeline = (list) => {
  if (!Array.isArray(list)) return []
  return list.map((item, index) => ({
    id: item.id || `${index + 1}`,
    title: item.title || '处理记录',
    description: item.description || '',
    time: formatDateTime(item.time),
    color: item.color || '#909399'
  }))
}

const statusCodeToKey = (status) => {
  if (status === 1) return 'processing'
  if (status === 2) return 'completed'
  if (status === 3) return 'rejected'
  if (status === 5) return 'decided'
  return 'pending'
}

const handleQuickAction = (actionKey) => {
  if (actionKey === 'accept') return handleAccept()
  if (actionKey === 'complete') return handleComplete()
  if (actionKey === 'reject') return handleReject()
  ElMessage.info('案件已进入只读状态')
}

const handleAccept = async () => {
  if (!caseDetail.value || !canAccept.value || submitting.value) return
  try {
    await ElMessageBox.confirm('确认受理该案件？', '受理确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    submitting.value = true
    await arbitrationApi.acceptAdminArbitration(caseDetail.value.id)
    ElMessage.success('案件已受理')
    await loadCaseDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '受理失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleReject = async () => {
  if (!caseDetail.value || !canReject.value || submitting.value) return
  const remark = String(decisionRemark.value || '').trim()
  if (!remark) {
    ElMessage.warning('请先填写驳回原因')
    return
  }
  try {
    await ElMessageBox.confirm('确认驳回该案件？', '驳回确认', {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      type: 'warning'
    })
    submitting.value = true
    await arbitrationApi.rejectAdminArbitration({
      arbitrationId: caseDetail.value.id,
      rejectReason: remark
    })
    ElMessage.success('案件已驳回')
    await loadCaseDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '驳回失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleComplete = async () => {
  if (!caseDetail.value || !canComplete.value || submitting.value) return
  const remark = String(decisionRemark.value || '').trim()
  if (!remark) {
    ElMessage.warning('请先填写处理备注')
    return
  }
  if (!decisionType.value) {
    ElMessage.warning('请先选择裁决类型')
    return
  }
  try {
    await ElMessageBox.confirm('确认提交裁决？提交后将进入执行阶段。', '裁决确认', {
      confirmButtonText: '确认提交',
      cancelButtonText: '取消',
      type: 'warning'
    })
    submitting.value = true
    await arbitrationApi.completeAdminArbitration({
      arbitrationId: caseDetail.value.id,
      decisionType: decisionType.value,
      decisionRemark: remark
    })
    ElMessage.success('裁决已提交，执行任务已创建')
    await loadCaseDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '提交裁决失败')
    }
  } finally {
    submitting.value = false
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  if (Number.isNaN(date.getTime())) return String(dateTime)
  return date.toLocaleString('zh-CN')
}

const formatAmount = (amount) => {
  const value = Number(amount || 0)
  return Number.isNaN(value) ? '0.00' : value.toFixed(2)
}

const evidenceTypeLabelMap = {
  image: '图片',
  chat: '聊天截图',
  text: '文字说明',
  system: '系统记录'
}

const evidenceTypeTagMap = {
  image: 'success',
  chat: 'info',
  text: 'warning',
  system: 'primary'
}

const getEvidenceTypeLabel = (type) => evidenceTypeLabelMap[type] || '其他'
const getEvidenceTagType = (type) => evidenceTypeTagMap[type] || 'info'

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadCaseDetail()
})

watch(
  () => route.params.id,
  () => {
    loadCaseDetail()
  }
)
</script>

<style scoped>
.case-workbench {
  --line-color: #e5eaf3;
  --text-main: #111827;
  --text-sub: #6b7280;

  padding: 18px;
  background: #f3f6fb;
  min-height: calc(100vh - 56px);
}

.workbench-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 14px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  color: var(--text-main);
}

.page-subtitle {
  margin: 6px 0 0;
  color: var(--text-sub);
  font-size: 13px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.main-layout {
  align-items: flex-start;
}

.module-card {
  border-radius: 12px;
  border: 1px solid #dce4f2;
}

.module-gap {
  margin-top: 16px;
}

.module-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.module-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
}

.module-desc {
  font-size: 12px;
  color: #8a94a8;
}

.focus-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.focus-item {
  border-radius: 10px;
  border: 1px solid var(--line-color);
  padding: 12px;
  background: #fff;
}

.focus-item.buyer {
  border-color: #dce8ff;
  background: #f8fbff;
}

.focus-item.seller {
  border-color: #ffe1e1;
  background: #fff9f9;
}

.focus-item.platform {
  border-color: #dcefe6;
  background: #f8fdf9;
}

.focus-item.request {
  border-color: #fff0c7;
  background: #fffdf7;
}

.focus-label {
  font-size: 12px;
  color: #6b7280;
}

.focus-content {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.65;
  color: #1f2937;
}

.negotiation-summary {
  white-space: pre-wrap;
  line-height: 1.7;
  color: #1f2937;
  background: #f9fbff;
  border: 1px solid #e4ebf8;
  border-radius: 10px;
  padding: 12px;
}

.assist-tabs :deep(.el-tabs__header) {
  margin-bottom: 10px;
}

.chat-summary-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chat-item {
  border: 1px solid var(--line-color);
  border-radius: 8px;
  padding: 10px;
  background: #fbfcff;
}

.chat-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.chat-meta .speaker {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
}

.chat-meta .time {
  font-size: 12px;
  color: #8a94a8;
}

.chat-content {
  font-size: 13px;
  line-height: 1.6;
  color: #374151;
}

.snapshot-product {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.product-image {
  width: 190px;
  height: 132px;
  border-radius: 8px;
  border: 1px solid #dce4f2;
  flex-shrink: 0;
}

.snapshot-info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  width: 100%;
}

.order-grid {
  margin-top: 4px;
}

.snapshot-item {
  border: 1px solid var(--line-color);
  border-radius: 8px;
  padding: 10px;
  background: #fff;
}

.snapshot-item.full {
  grid-column: 1 / -1;
}

.snapshot-item .k {
  display: block;
  font-size: 12px;
  color: #6b7280;
}

.snapshot-item .v {
  display: block;
  margin-top: 5px;
  font-size: 13px;
  color: #1f2937;
  line-height: 1.55;
}

.system-evidence-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.system-evidence-item {
  border: 1px solid var(--line-color);
  border-radius: 8px;
  padding: 10px;
  display: flex;
  gap: 10px;
  background: #fff;
}

.system-main {
  flex: 1;
  min-width: 0;
}

.system-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.system-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.system-desc {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.5;
  color: #374151;
}

.system-time {
  margin-top: 6px;
  font-size: 12px;
  color: #8a94a8;
}

.system-thumb,
.system-icon-box {
  width: 70px;
  height: 70px;
  border-radius: 6px;
  border: 1px solid #dbe4f3;
}

.system-icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f4f7fd;
  color: #627491;
  font-size: 22px;
}

.timeline-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.timeline-desc {
  margin-top: 4px;
  font-size: 13px;
  line-height: 1.5;
  color: #4b5563;
}

.sticky-wrap {
  position: sticky;
  top: 16px;
}

.decision-panel {
  background: #fbfdff;
}

.panel-section {
  margin-bottom: 14px;
}

.panel-label {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 8px;
}

.risk-list {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.risk-item {
  font-size: 12px;
  line-height: 1.5;
  color: #7c2d12;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 8px;
  padding: 7px 9px;
}

.recommend-section {
  padding: 10px;
  border: 1px solid #dce6f8;
  border-radius: 8px;
  background: #f7fbff;
}

.recommend-text {
  font-size: 13px;
  line-height: 1.6;
  color: #1f2937;
}

.execution-box {
  padding: 10px;
  border: 1px solid #dce4f2;
  border-radius: 8px;
  background: #fff;
}

.execution-line {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  line-height: 1.6;
  color: #4b5563;
}

.execution-line + .execution-line {
  margin-top: 6px;
}

.result-panel {
  margin-top: 12px;
  padding: 10px;
  border: 1px solid #d5e8dc;
  background: #f8fdf9;
  border-radius: 8px;
}

.result-title {
  font-size: 12px;
  color: #6b7280;
}

.result-status {
  margin-top: 4px;
  font-size: 14px;
  font-weight: 700;
  color: #166534;
}

.result-remark {
  margin-top: 6px;
  font-size: 13px;
  color: #1f2937;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 14px;
}

.full-btn {
  margin-left: 0 !important;
  width: 100%;
}

@media (max-width: 1200px) {
  .focus-grid {
    grid-template-columns: 1fr;
  }

  .snapshot-product {
    flex-direction: column;
  }

  .product-image {
    width: 100%;
    height: 180px;
  }
}

@media (max-width: 992px) {
  .sticky-wrap {
    position: static;
    margin-top: 16px;
  }
}

@media (max-width: 768px) {
  .case-workbench {
    padding: 12px;
  }

  .workbench-topbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .toolbar-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .snapshot-info-grid {
    grid-template-columns: 1fr;
  }
}
</style>

