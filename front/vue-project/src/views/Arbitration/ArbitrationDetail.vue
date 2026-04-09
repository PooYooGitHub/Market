<template>
  <div class="arbitration-detail-page">
    <div v-loading="loading" class="detail-container">
      <!-- 顶部导航 -->
      <el-card class="nav-card" shadow="never">
        <div class="nav-header">
          <el-button @click="$router.go(-1)" type="info" plain>
            <el-icon><ArrowLeft /></el-icon>
            返回列表
          </el-button>

          <div class="detail-title">
            <h2><el-icon><Document /></el-icon> 仲裁详情</h2>
            <el-tag :type="getStatusType(detail.status)" size="large">
              {{ getStatusText(detail.status) }}
            </el-tag>
          </div>
        </div>
      </el-card>

      <div class="detail-content">
        <!-- 基本信息卡片 -->
        <el-card class="info-card" shadow="hover">
          <template #header>
            <h3><el-icon><Document /></el-icon> 基本信息</h3>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="仲裁编号">
              <el-text type="primary" size="large" tag="strong">
                AR{{ String(detail.id).padStart(6, '0') }}
              </el-text>
            </el-descriptions-item>
            <el-descriptions-item label="申请时间">
              {{ formatDate(detail.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="订单编号">
              <el-link @click="goToOrder(detail.orderId)" type="primary">
                ORDER{{ String(detail.orderId).padStart(8, '0') }}
              </el-link>
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatDate(detail.updateTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="仲裁原因">
              <el-tag type="warning" effect="plain">
                {{ getReasonText(detail.reason) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="getStatusType(detail.status)">
                {{ getStatusText(detail.status) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 涉及人员信息 -->
        <el-card class="participants-card" shadow="hover">
          <template #header>
            <h3><el-icon><User /></el-icon> 涉及人员</h3>
          </template>

          <el-row :gutter="20">
            <el-col :span="12">
              <div class="participant-info">
                <div class="participant-role">申请人</div>
                <div class="participant-details">
                  <el-avatar :src="detail.applicantAvatar" size="large" />
                  <div class="participant-text">
                    <div class="participant-name">{{ detail.applicantName || '用户' + detail.applicantId }}</div>
                    <div class="participant-id">用户ID: {{ detail.applicantId }}</div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="participant-info">
                <div class="participant-role">被申诉人</div>
                <div class="participant-details">
                  <el-avatar :src="detail.respondentAvatar" size="large" />
                  <div class="participant-text">
                    <div class="participant-name">{{ detail.respondentName || '用户' + detail.respondentId }}</div>
                    <div class="participant-id">用户ID: {{ detail.respondentId }}</div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 问题描述卡片 -->
        <el-card class="description-card" shadow="hover">
          <template #header>
            <h3><el-icon><EditPen /></el-icon> 问题描述</h3>
          </template>

          <div class="description-content">
            <p>{{ detail.description }}</p>
          </div>
        </el-card>

        <!-- 证据材料卡片 -->
        <el-card v-if="evidenceList.length > 0" class="evidence-card" shadow="hover">
          <template #header>
            <h3><el-icon><Picture /></el-icon> 证据材料</h3>
          </template>

          <div class="evidence-gallery">
            <el-image
              v-for="(image, index) in evidenceList"
              :key="index"
              :src="image"
              :preview-src-list="evidenceList"
              :initial-index="index"
              class="evidence-image"
              fit="cover"
              lazy
            />
          </div>
        </el-card>

        <!-- 处理结果卡片 -->
        <el-card v-if="detail.result" class="result-card" shadow="hover">
          <template #header>
            <div class="result-header">
              <h3><el-icon><DocumentChecked /></el-icon> 处理结果</h3>
              <div v-if="detail.handlerName" class="handler-info">
                <el-text type="info">处理人: {{ detail.handlerName }}</el-text>
              </div>
            </div>
          </template>

          <div class="result-content">
            <el-alert
              :title="detail.status === 2 ? '仲裁完结' : '申请驳回'"
              :type="detail.status === 2 ? 'success' : 'error'"
              :closable="false"
              show-icon
            >
              <div class="result-text">{{ detail.result }}</div>
            </el-alert>
          </div>
        </el-card>

        <!-- 处理时间线 -->
        <el-card v-if="timelineData.length > 0" class="timeline-card" shadow="hover">
          <template #header>
            <h3><el-icon><Clock /></el-icon> 处理记录</h3>
          </template>

          <el-timeline>
            <el-timeline-item
              v-for="item in timelineData"
              :key="item.id"
              :timestamp="formatDate(item.createTime)"
              :type="getTimelineType(item.action)"
              :icon="getTimelineIcon(item.action)"
            >
              <div class="timeline-content">
                <h4>{{ getActionText(item.action) }}</h4>
                <p v-if="item.remark">{{ item.remark }}</p>
                <el-text type="info">操作人: {{ item.operatorName || '用户' + item.operatorId }}</el-text>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <!-- 操作按钮 -->
        <div v-if="canOperate" class="action-buttons">
          <el-button
            v-if="detail.status === 0"
            type="danger"
            @click="cancelArbitration"
            :loading="operating"
          >
            <el-icon><Close /></el-icon>
            取消申请
          </el-button>

          <el-button
            v-if="detail.status === 2"
            type="success"
            @click="rateHandler"
          >
            <el-icon><Star /></el-icon>
            评价处理
          </el-button>

          <el-button
            type="primary"
            @click="contactSupport"
          >
            <el-icon><ChatDotRound /></el-icon>
            联系客服
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, Document, DocumentCopy, User, EditPen, Picture,
  DocumentChecked, Clock, Close, Star, ChatDotRound
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { arbitrationApi } from '@/api/arbitration'
import { format } from 'date-fns'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(true)
const operating = ref(false)
const detail = ref({})
const timelineData = ref([])

// 计算属性
const evidenceList = computed(() => {
  if (!detail.value.evidence) return []
  try {
    return typeof detail.value.evidence === 'string'
      ? JSON.parse(detail.value.evidence)
      : detail.value.evidence
  } catch {
    return []
  }
})

const canOperate = computed(() => {
  return detail.value.applicantId === getCurrentUserId()
})

// 获取当前用户ID (实际应该从store或token中获取)
const getCurrentUserId = () => {
  return 1 // 模拟用户ID
}

// 加载仲裁详情
const loadArbitrationDetail = async () => {
  const arbitrationId = route.params.id

  try {
    loading.value = true

    const result = await arbitrationApi.getArbitrationDetail(arbitrationId)
    detail.value = result.data

    // 模拟数据 (实际开发时删除)
    if (!result.data) {
      detail.value = {
        id: parseInt(arbitrationId),
        orderId: 100001,
        applicantId: 1,
        applicantName: '张三',
        applicantAvatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100',
        respondentId: 2,
        respondentName: '李四',
        respondentAvatar: 'https://images.unsplash.com/photo-1494790108755-2616b612d5db?w=100',
        reason: 'QUALITY_ISSUE',
        description: '收到的商品与描述不符，存在明显质量问题。商品外观有多处划痕，功能异常，完全无法正常使用。我要求退货退款，但卖家拒绝处理。',
        evidence: JSON.stringify([
          'https://images.unsplash.com/photo-1551963831-b3b1ca40c98e?w=400',
          'https://images.unsplash.com/photo-1551782450-a2132b4ba21d?w=400',
          'https://images.unsplash.com/photo-1522770179533-24471fcdba45?w=400'
        ]),
        status: 1,
        result: null,
        handlerId: 10,
        handlerName: '客服小王',
        createTime: new Date('2024-03-28 10:30:00'),
        updateTime: new Date('2024-03-29 14:20:00')
      }
    }

    // 加载时间线数据
    await loadTimelineData(arbitrationId)

  } catch (error) {
    ElMessage.error('加载失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 加载时间线数据
const loadTimelineData = async (arbitrationId) => {
  try {
    // 调用真实API获取仲裁操作日志
    const result = await arbitrationApi.getArbitrationLogs(arbitrationId)

    if (result.data && result.data.length > 0) {
      timelineData.value = result.data.map(log => ({
        id: log.id,
        arbitrationId: log.arbitrationId,
        operatorId: log.operatorId,
        operatorName: log.operatorName || '系统',
        action: log.action,
        remark: log.remark,
        createTime: new Date(log.createTime)
      }))
    } else {
      // API返回空数据时使用默认时间线
      timelineData.value = [{
        id: 1,
        arbitrationId,
        operatorId: detail.value.applicantId,
        operatorName: detail.value.applicantName || '用户',
        action: 'SUBMIT',
        remark: '用户提交仲裁申请',
        createTime: detail.value.createTime
      }]
    }

  } catch (error) {
    console.warn('加载时间线数据失败，使用模拟数据:', error)
    // API调用失败时使用模拟时间线数据
    timelineData.value = [
      {
        id: 1,
        arbitrationId,
        operatorId: 1,
        operatorName: '张三',
        action: 'SUBMIT',
        remark: '用户提交仲裁申请',
        createTime: new Date('2024-03-28 10:30:00')
      },
      {
        id: 2,
        arbitrationId,
        operatorId: 10,
        operatorName: '客服小王',
        action: 'ACCEPT',
        remark: '管理员受理仲裁申请，开始核实相关情况',
        createTime: new Date('2024-03-29 09:15:00')
      },
      {
        id: 3,
        arbitrationId,
        operatorId: 10,
        operatorName: '客服小王',
        action: 'PROCESSING',
        remark: '正在联系双方当事人，收集相关证据',
        createTime: new Date('2024-03-29 14:20:00')
      }
    ]
  }
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
    'QUALITY_ISSUE': '商品质量问题',
    'SHIPPING_DELAY': '发货延迟',
    'DESCRIPTION_MISMATCH': '商品描述不符',
    'NO_RESPONSE': '卖家无响应',
    'OTHER': '其他问题'
  }
  return texts[reason] || '其他问题'
}

// 获取时间线类型
const getTimelineType = (action) => {
  const types = {
    'SUBMIT': 'primary',
    'ACCEPT': 'success',
    'PROCESSING': 'warning',
    'RESOLVE': 'success',
    'REJECT': 'danger',
    'CANCEL': 'info'
  }
  return types[action] || 'primary'
}

// 获取时间线图标
const getTimelineIcon = (action) => {
  // 这里返回图标组件名称字符串，实际使用时需要映射到具体组件
  const icons = {
    'SUBMIT': 'EditPen',
    'ACCEPT': 'Select',
    'PROCESSING': 'Loading',
    'RESOLVE': 'CircleCheck',
    'REJECT': 'CircleClose',
    'CANCEL': 'Close'
  }
  return icons[action] || 'InfoFilled'
}

// 获取操作文本
const getActionText = (action) => {
  const texts = {
    'SUBMIT': '提交申请',
    'ACCEPT': '受理申请',
    'PROCESSING': '处理中',
    'RESOLVE': '仲裁完结',
    'REJECT': '驳回申请',
    'CANCEL': '取消申请'
  }
  return texts[action] || '未知操作'
}

// 格式化日期
const formatDate = (date) => {
  return format(new Date(date), 'yyyy-MM-dd HH:mm:ss')
}

// 跳转订单详情
const goToOrder = (orderId) => {
  router.push(`/order/${orderId}`)
}

// 取消仲裁申请
const cancelArbitration = async () => {
  try {
    await ElMessageBox.confirm(
      '确认取消此仲裁申请？取消后无法恢复。',
      '确认取消',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '保留',
        type: 'warning'
      }
    )

    operating.value = true

    await arbitrationApi.cancelArbitration(detail.value.id)

    ElMessage.success('仲裁申请已取消')
    router.push('/arbitration/list')

  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败：' + (error.message || '未知错误'))
    }
  } finally {
    operating.value = false
  }
}

// 评价处理
const rateHandler = async () => {
  try {
    // 这里可以弹出评价对话框让用户评分和评价
    const { value } = await ElMessageBox.prompt(
      '请对本次仲裁处理结果进行评价（1-5分）',
      '评价处理结果',
      {
        confirmButtonText: '提交评价',
        cancelButtonText: '取消',
        inputPattern: /^[1-5]$/,
        inputErrorMessage: '请输入1-5之间的整数'
      }
    )

    const rating = parseInt(value)

    // 可以进一步实现评价API调用
    // await arbitrationApi.rateArbitration(detail.value.id, { rating, comment: '' })

    ElMessage.success(`感谢您的评价！您给出了 ${rating} 分`)

  } catch (error) {
    if (error !== 'cancel') {
      console.warn('评价功能暂未完全实现:', error)
    }
  }
}

// 联系客服
const contactSupport = () => {
  // 可以集成客服系统或跳转到客服页面
  ElMessageBox.alert(
    '如需帮助，请联系客服：\n电话：400-123-4567\n邮箱：support@campus-market.com\n在线客服：工作时间 9:00-18:00',
    '联系客服',
    {
      confirmButtonText: '知道了',
      type: 'info'
    }
  )
}

// 页面初始化
onMounted(() => {
  loadArbitrationDetail()
})
</script>

<style scoped>
.arbitration-detail-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.nav-card {
  margin-bottom: 20px;
}

.nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.detail-title h2 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card,
.participants-card,
.description-card,
.evidence-card,
.result-card,
.timeline-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.info-card h3,
.participants-card h3,
.description-card h3,
.evidence-card h3,
.result-card h3,
.timeline-card h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.participants-card .participant-info {
  text-align: center;
  padding: 20px;
}

.participant-role {
  font-size: 14px;
  color: #909399;
  margin-bottom: 16px;
  font-weight: 500;
}

.participant-details {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.participant-text {
  text-align: left;
}

.participant-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.participant-id {
  font-size: 12px;
  color: #909399;
}

.description-content {
  padding: 16px 0;
}

.description-content p {
  margin: 0;
  color: #606266;
  line-height: 1.8;
  font-size: 15px;
}

.evidence-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  padding: 16px 0;
}

.evidence-image {
  width: 100%;
  height: 150px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.3s ease;
}

.evidence-image:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.handler-info {
  font-size: 14px;
}

.result-content {
  padding: 16px 0;
}

.result-text {
  margin-top: 8px;
  line-height: 1.6;
}

.timeline-content h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.timeline-content p {
  margin: 0 0 8px 0;
  color: #606266;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 30px 0;
  border-top: 1px solid #ebeef5;
}

.action-buttons .el-button {
  padding: 12px 24px;
}

@media (max-width: 768px) {
  .arbitration-detail-page {
    padding: 10px;
  }

  .nav-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .detail-title {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .participant-details {
    flex-direction: column;
    gap: 12px;
  }

  .participant-text {
    text-align: center;
  }

  .evidence-gallery {
    grid-template-columns: repeat(2, 1fr);
  }

  .action-buttons {
    flex-direction: column;
    align-items: stretch;
  }

  .action-buttons .el-button {
    width: 100%;
    margin: 4px 0;
  }
}
</style>