<template>
  <div class="arbitration-case-detail">
    <div class="page-header">
      <div class="title-group">
        <h2>仲裁详情</h2>
        <el-tag v-if="arbitration.id" :type="getStatusType(arbitration.status)">
          {{ getStatusName(arbitration.status) }}
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <el-button :loading="loading" @click="loadDetail">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <el-card class="action-card">
      <template #header>
        <div class="card-title">管理员操作</div>
      </template>
      <div class="action-buttons">
        <el-button
          v-if="canAccept"
          type="primary"
          :loading="actionLoading"
          @click="acceptCase"
        >
          受理案件
        </el-button>
        <el-button
          v-if="canReject"
          type="danger"
          :loading="actionLoading"
          @click="rejectCase"
        >
          驳回案件
        </el-button>
        <el-button
          v-if="canRequestSupplement"
          type="warning"
          :loading="actionLoading"
          @click="requestSupplement"
        >
          要求补证
        </el-button>
        <el-button
          v-if="canResolve"
          type="success"
          :loading="actionLoading"
          @click="resolveCase"
        >
          完结裁决
        </el-button>
      </div>
    </el-card>

    <el-alert
      v-if="errorMsg"
      type="error"
      :closable="false"
      :title="errorMsg"
      class="mb16"
    />

    <div v-loading="loading">
      <el-card class="section-card">
        <template #header>
          <div class="card-title">案件基本信息</div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="案件编号">
            {{ caseNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="订单ID">
            {{ arbitration.orderId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="申请人">
            {{ applicantDisplay }}
          </el-descriptions-item>
          <el-descriptions-item label="被申请人">
            {{ respondentDisplay }}
          </el-descriptions-item>
          <el-descriptions-item label="仲裁原因">
            {{ getReasonName(arbitration.reason) }}
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            {{ getStatusName(arbitration.status) }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ formatDateTime(arbitration.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(arbitration.updateTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ arbitration.description || '-' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="arbitration.result" label="裁决结果" :span="2">
            {{ arbitration.result }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="section-card">
        <template #header>
          <div class="card-title">订单信息</div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">
            {{ orderSnapshot.orderNo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            {{ orderSnapshot.status || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="买家ID">
            {{ orderSnapshot.buyerId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="卖家ID">
            {{ orderSnapshot.sellerId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">
            {{ orderSnapshot.amount || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="快照时间">
            {{ formatDateTime(orderSnapshot.snapshotAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="订单创建时间">
            {{ formatDateTime(orderSnapshot.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="订单更新时间">
            {{ formatDateTime(orderSnapshot.updateTime) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="orderSnapshot.note" label="备注" :span="2">
            {{ orderSnapshot.note }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="section-card">
        <template #header>
          <div class="card-title">商品信息</div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品ID">
            {{ productSnapshot.productId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="卖家ID">
            {{ productSnapshot.sellerId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="商品标题" :span="2">
            {{ productSnapshot.title || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="商品价格">
            {{ productSnapshot.price || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="商品状态">
            {{ productSnapshot.status || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="商品描述" :span="2">
            {{ productSnapshot.description || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="快照时间" :span="2">
            {{ formatDateTime(productSnapshot.snapshotAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="商品图片" :span="2">
            <div v-if="productImages.length" class="images-wrap">
              <el-image
                v-for="(img, idx) in productImages"
                :key="`${img}-${idx}`"
                :src="img"
                fit="cover"
                :preview-src-list="productImages"
                preview-teleported
                class="product-image"
              />
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="section-card">
        <template #header>
          <div class="card-title">聊天信息（上下文）</div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="范围">
            {{ chatContext.scope || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="数据源">
            {{ chatContext.source || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="窗口开始">
            {{ formatDateTime(chatContext.windowStart) }}
          </el-descriptions-item>
          <el-descriptions-item label="窗口结束">
            {{ formatDateTime(chatContext.windowEnd) }}
          </el-descriptions-item>
          <el-descriptions-item label="说明" :span="2">
            {{ chatContext.note || '暂无说明' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="section-card">
        <template #header>
          <div class="card-title">买卖双方对照视图</div>
        </template>
        <el-row :gutter="16" class="compare-row">
          <el-col
            v-for="panel in partyComparePanels"
            :key="panel.role"
            :xs="24"
            :md="12"
          >
            <el-card class="compare-card" shadow="never">
              <template #header>
                <div class="compare-header">
                  <span class="compare-title">{{ panel.title }}</span>
                  <el-tag size="small" type="info">{{ panel.submissions.length }} 条提交</el-tag>
                </div>
              </template>
              <el-descriptions :column="1" border class="compare-meta">
                <el-descriptions-item label="用户ID">
                  {{ panel.partyId }}
                </el-descriptions-item>
                <el-descriptions-item label="案件角色">
                  {{ panel.caseRole }}
                </el-descriptions-item>
              </el-descriptions>

              <div v-if="panel.submissions.length === 0" class="compare-empty">
                <el-empty description="暂无提交" :image-size="64" />
              </div>
              <div v-else class="compare-list">
                <el-card
                  v-for="sub in panel.submissions"
                  :key="sub._compareKey"
                  class="compare-item"
                  shadow="never"
                >
                  <div class="compare-item-header">
                    <span>{{ formatDateTime(sub.createTime) }}</span>
                    <el-tag size="small" :type="sub.late ? 'danger' : 'success'">
                      {{ sub.late ? '逾期提交' : '按时提交' }}
                    </el-tag>
                  </div>
                  <div class="compare-item-field">
                    <span class="field-label">主张：</span>{{ sub.claim || '-' }}
                  </div>
                  <div class="compare-item-field">
                    <span class="field-label">事实：</span>{{ sub.facts || '-' }}
                  </div>
                  <div class="compare-item-field">
                    <span class="field-label">备注：</span>{{ sub.note || '-' }}
                  </div>
                  <div class="compare-item-field">
                    <span class="field-label">证据：</span>
                    <div v-if="sub._evidenceUrls.length" class="compare-links">
                      <a
                        v-for="(url, idx) in sub._evidenceUrls"
                        :key="`${sub._compareKey}-${idx}`"
                        :href="url"
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        证据 {{ idx + 1 }}
                      </a>
                    </div>
                    <span v-else>-</span>
                  </div>
                </el-card>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-card>

      <el-card class="section-card">
        <template #header>
          <div class="card-title">证据信息（按角色分组）</div>
        </template>
        <div v-if="evidenceBundles.length === 0">
          <el-empty description="暂无证据提交" />
        </div>
        <div v-else>
          <el-card
            v-for="bundle in evidenceBundles"
            :key="bundle.role"
            class="bundle-card"
            shadow="never"
          >
            <template #header>
              <div class="bundle-title">{{ getRoleName(bundle.role) }}</div>
            </template>
            <div v-if="!bundle.submissions || bundle.submissions.length === 0">
              <el-empty description="暂无该角色证据" :image-size="64" />
            </div>
            <div v-else>
              <div v-for="sub in bundle.submissions" :key="sub.id" class="submission-item">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="提交时间">
                    {{ formatDateTime(sub.createTime) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="提交人ID">
                    {{ sub.submitterId || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="补证请求ID">
                    {{ sub.supplementRequestId || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="是否逾期">
                    {{ sub.late ? '是' : '否' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="主张" :span="2">
                    {{ sub.claim || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="事实" :span="2">
                    {{ sub.facts || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="备注" :span="2">
                    {{ sub.note || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="证据链接" :span="2">
                    <div v-if="parseStringArray(sub.evidenceUrls).length">
                      <div
                        v-for="(url, idx) in parseStringArray(sub.evidenceUrls)"
                        :key="`${sub.id}-${idx}`"
                      >
                        <a :href="url" target="_blank" rel="noopener noreferrer">{{ url }}</a>
                      </div>
                    </div>
                    <span v-else>-</span>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </div>
          </el-card>
        </div>
      </el-card>

      <el-card class="section-card">
        <template #header>
          <div class="card-title">补证请求</div>
        </template>
        <div v-if="supplementRequests.length === 0">
          <el-empty description="暂无补证请求" />
        </div>
        <el-table v-else :data="supplementRequests" stripe>
          <el-table-column prop="id" label="请求ID" width="100" />
          <el-table-column label="目标方" width="120">
            <template #default="scope">
              {{ getTargetPartyName(scope.row.targetParty) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getSupplementStatusType(scope.row.status)">
                {{ getSupplementStatusName(scope.row.status, scope.row.statusDesc) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="requiredItems" label="要求项" min-width="220" show-overflow-tooltip />
          <el-table-column prop="remark" label="说明" min-width="160" show-overflow-tooltip />
          <el-table-column label="截止时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.dueTime) }}
            </template>
          </el-table-column>
          <el-table-column label="发起时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button
                v-if="Number(scope.row.status) === 0"
                size="small"
                type="warning"
                :loading="actionLoading"
                @click="expireSupplement(scope.row)"
              >
                超时结转
              </el-button>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card class="section-card">
        <template #header>
          <div class="card-title">处理日志</div>
        </template>
        <div v-if="logs.length === 0">
          <el-empty description="暂无处理日志" />
        </div>
        <el-timeline v-else>
          <el-timeline-item
            v-for="log in logs"
            :key="log.id"
            :timestamp="formatDateTime(log.createTime)"
            placement="top"
          >
            <div><strong>{{ log.action || '操作' }}</strong></div>
            <div>操作人ID：{{ log.operatorId || '-' }}</div>
            <div v-if="log.remark">{{ log.remark }}</div>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import { arbitrationApi } from '@/api/arbitration'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const actionLoading = ref(false)
const detail = ref(null)
const errorMsg = ref('')

const arbitrationId = computed(() => Number(route.params.id))

const arbitration = computed(() => detail.value?.arbitration || {})
const systemContext = computed(() => detail.value?.systemContext || {})
const evidenceBundles = computed(() => detail.value?.evidenceBundles || [])
const supplementRequests = computed(() => detail.value?.supplementRequests || [])
const logs = computed(() => {
  const list = detail.value?.logs || []
  return [...list].sort((a, b) => new Date(b.createTime || 0).getTime() - new Date(a.createTime || 0).getTime())
})

const orderSnapshot = computed(() => systemContext.value.orderSnapshot || {})
const productSnapshot = computed(() => systemContext.value.productSnapshot || {})
const chatContext = computed(() => systemContext.value.chatContext || {})

const productImages = computed(() => parseStringArray(productSnapshot.value.imageUrls))
const normalizeId = value => (value === null || value === undefined || value === '' ? '' : String(value))

const getBundleSubmissionsByRole = (role) => {
  const bundle = evidenceBundles.value.find(item => String(item?.role || '').toUpperCase() === role)
  const submissions = Array.isArray(bundle?.submissions) ? bundle.submissions : []
  return submissions
    .map((submission, index) => ({
      ...submission,
      _compareKey: submission.id || `${role}-${submission.createTime || 'time'}-${index}`,
      _evidenceUrls: parseStringArray(submission.evidenceUrls)
    }))
    .sort((a, b) => new Date(b.createTime || 0).getTime() - new Date(a.createTime || 0).getTime())
}

const getCaseRoleByParty = (role) => {
  const partyId = normalizeId(role === 'BUYER' ? orderSnapshot.value.buyerId : orderSnapshot.value.sellerId)
  if (!partyId) return '未识别'
  const applicantId = normalizeId(arbitration.value.applicantId)
  const respondentId = normalizeId(arbitration.value.respondentId)
  const labels = []
  if (partyId === applicantId) labels.push('申请人')
  if (partyId === respondentId) labels.push('被申请人')
  return labels.length ? labels.join(' / ') : '非当事方'
}

const partyComparePanels = computed(() => {
  const buildPanel = (role, title) => {
    const submissions = getBundleSubmissionsByRole(role)
    const partyId = role === 'BUYER' ? orderSnapshot.value.buyerId : orderSnapshot.value.sellerId
    return {
      role,
      title,
      partyId: partyId || '-',
      caseRole: getCaseRoleByParty(role),
      submissions
    }
  }
  return [
    buildPanel('BUYER', '买方主张与证据'),
    buildPanel('SELLER', '卖方主张与证据')
  ]
})

const applicantDisplay = computed(() => route.query.applicantName || `用户${arbitration.value.applicantId || '-'}`)
const respondentDisplay = computed(() => route.query.respondentName || `用户${arbitration.value.respondentId || '-'}`)

const caseNumber = computed(() => {
  if (route.query.caseNumber) return route.query.caseNumber
  if (arbitration.value.id) return `ARB${String(arbitration.value.id).padStart(6, '0')}`
  return '-'
})

const from = computed(() => String(route.query.from || ''))
const backPath = computed(() => {
  if (from.value === 'processing') return '/admin/arbitration/processing'
  if (from.value === 'completed') return '/admin/arbitration/completed'
  return '/admin/arbitration/pending'
})

const currentStatus = computed(() => Number(arbitration.value.status))
const canAccept = computed(() => currentStatus.value === 0)
const canResolve = computed(() => [1, 4].includes(currentStatus.value))
const canReject = computed(() => [0, 1, 4].includes(currentStatus.value))
const canRequestSupplement = computed(() => [0, 1, 4].includes(currentStatus.value))

const loadDetail = async () => {
  if (!arbitrationId.value) {
    errorMsg.value = '仲裁ID无效'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const response = await arbitrationApi.getAdminArbitrationDetail(arbitrationId.value)
    detail.value = response?.data || {}
  } catch (error) {
    console.error('加载仲裁详情失败:', error)
    errorMsg.value = error?.message || '加载仲裁详情失败'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push(backPath.value)
}

const acceptCase = async () => {
  try {
    await ElMessageBox.confirm('确定受理该仲裁案件吗？', '确认受理', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    actionLoading.value = true
    await arbitrationApi.acceptArbitration(arbitrationId.value)
    ElMessage.success('案件受理成功')
    await loadDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('受理失败:', error)
      ElMessage.error(error?.message || '受理失败')
    }
  } finally {
    actionLoading.value = false
  }
}

const rejectCase = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回案件', {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputValidator: (val) => {
        if (!val || !String(val).trim()) return '请填写驳回原因'
        return true
      }
    })
    actionLoading.value = true
    await arbitrationApi.rejectArbitration(arbitrationId.value, String(value).trim())
    ElMessage.success('案件已驳回')
    await loadDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('驳回失败:', error)
      ElMessage.error(error?.message || '驳回失败')
    }
  } finally {
    actionLoading.value = false
  }
}

const requestSupplement = async () => {
  try {
    const { value } = await ElMessageBox.prompt(
      '请输入补证要求（默认通知买卖双方，48小时内补充）',
      '要求补证',
      {
        confirmButtonText: '发送补证要求',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '例如：请补充聊天记录、交易凭证、商品实拍图',
        inputValidator: (val) => {
          if (!val || !String(val).trim()) return '请填写补证要求'
          return true
        }
      }
    )
    actionLoading.value = true
    await arbitrationApi.requestSupplement({
      arbitrationId: arbitrationId.value,
      targetParty: 'BOTH',
      requiredItems: String(value).trim(),
      remark: '管理员要求补充材料',
      dueHours: 48
    })
    ElMessage.success('补证请求已发起')
    await loadDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发起补证失败:', error)
      ElMessage.error(error?.message || '发起补证失败')
    }
  } finally {
    actionLoading.value = false
  }
}

const resolveCase = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入裁决结果说明', '完结裁决', {
      confirmButtonText: '提交完结',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputValidator: (val) => {
        if (!val || !String(val).trim()) return '请填写裁决说明'
        return true
      }
    })

    await resolveCaseInternal(String(value).trim(), false)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完结失败:', error)
      ElMessage.error(error?.message || '完结失败')
    }
  }
}

const resolveCaseInternal = async (result, force) => {
  actionLoading.value = true
  try {
    await arbitrationApi.handleArbitration({
      id: arbitrationId.value,
      result,
      force
    })
    ElMessage.success(force ? '案件已强制完结' : '案件已完结')
    await loadDetail()
  } catch (error) {
    const msg = String(error?.message || '')
    if (!force && (msg.includes('force=true') || msg.includes('待补证请求'))) {
      await ElMessageBox.confirm(
        '当前仍有待补证请求，是否强制完结并自动结转待补证请求？',
        '强制完结确认',
        {
          confirmButtonText: '强制完结',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      await resolveCaseInternal(result, true)
      return
    }
    throw error
  } finally {
    actionLoading.value = false
  }
}

const expireSupplement = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认将补证请求 #${row.id} 标记为超时并结转？`,
      '超时结转确认',
      {
        confirmButtonText: '确认结转',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    actionLoading.value = true
    await arbitrationApi.expireSupplement(row.id)
    ElMessage.success('补证请求已结转')
    await loadDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('补证请求结转失败:', error)
      ElMessage.error(error?.message || '补证请求结转失败')
    }
  } finally {
    actionLoading.value = false
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  if (Number.isNaN(date.getTime())) return String(dateTime)
  return date.toLocaleString('zh-CN')
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
  return map[reason] || reason || '-'
}

const getStatusName = (status) => {
  const map = {
    0: '待处理',
    1: '处理中',
    2: '已完结',
    3: '已驳回',
    4: '待补证'
  }
  return map[Number(status)] || '未知'
}

const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'danger',
    4: 'warning'
  }
  return map[Number(status)] || 'info'
}

const getRoleName = (role) => {
  const map = {
    BUYER: '买方证据',
    SELLER: '卖方证据',
    SYSTEM: '系统证据'
  }
  return map[String(role || '').toUpperCase()] || '其他证据'
}

const getTargetPartyName = (target) => {
  const map = {
    BUYER: '买方',
    SELLER: '卖方',
    BOTH: '买卖双方'
  }
  return map[String(target || '').toUpperCase()] || '未知'
}

const getSupplementStatusName = (status, statusDesc) => {
  if (statusDesc) return statusDesc
  const map = {
    0: '待补证',
    1: '已满足',
    2: '已超时',
    3: '已取消'
  }
  return map[Number(status)] || '未知'
}

const getSupplementStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return map[Number(status)] || 'info'
}

const parseStringArray = (value) => {
  if (!value) return []
  if (Array.isArray(value)) return value.map(item => String(item)).filter(Boolean)
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value)
      if (Array.isArray(parsed)) return parsed.map(item => String(item)).filter(Boolean)
    } catch (_) {
      if (value.includes(',')) return value.split(',').map(item => item.trim()).filter(Boolean)
      return [value]
    }
    return []
  }
  return [String(value)]
}

watch(() => route.params.id, loadDetail)
onMounted(loadDetail)
</script>

<style scoped>
.arbitration-case-detail {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-group h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.action-card,
.section-card {
  margin-bottom: 16px;
}

.card-title {
  font-weight: 600;
  color: #303133;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.bundle-card {
  margin-bottom: 12px;
}

.compare-row {
  row-gap: 16px;
}

.compare-card {
  height: 100%;
}

.compare-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.compare-title {
  font-weight: 600;
}

.compare-meta {
  margin-bottom: 12px;
}

.compare-empty {
  padding: 8px 0;
}

.compare-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.compare-item {
  border: 1px solid #ebeef5;
}

.compare-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #606266;
  font-size: 13px;
}

.compare-item-field {
  line-height: 1.7;
  color: #303133;
  margin-bottom: 4px;
  word-break: break-word;
}

.compare-item-field:last-child {
  margin-bottom: 0;
}

.field-label {
  color: #909399;
}

.compare-links {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 2px;
}

.compare-links a {
  color: #409eff;
  text-decoration: none;
}

.compare-links a:hover {
  text-decoration: underline;
}

.bundle-title {
  font-weight: 600;
}

.submission-item {
  margin-bottom: 12px;
}

.submission-item:last-child {
  margin-bottom: 0;
}

.images-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.mb16 {
  margin-bottom: 16px;
}
</style>
