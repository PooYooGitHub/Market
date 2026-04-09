<template>
  <div class="evaluation-form">
    <el-dialog
      v-model="visible"
      title="评价用户"
      width="600px"
      :close-on-click-modal="false"
      @close="handleClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="订单信息" v-if="orderInfo">
          <div class="order-info">
            <div class="order-header">
              <span class="order-no">订单号: {{ orderInfo.orderNo }}</span>
              <el-tag type="success" size="small">{{ orderInfo.statusText }}</el-tag>
            </div>
            <div class="order-content">
              <div class="product-info">
                <el-image
                  :src="orderInfo.productImage"
                  :preview-src-list="[orderInfo.productImage]"
                  class="product-image"
                  fit="cover"
                />
                <div class="product-details">
                  <div class="product-title">{{ orderInfo.productTitle }}</div>
                  <div class="product-price">¥{{ orderInfo.totalAmount }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="评价对象">
          <div class="target-user">
            <el-avatar :src="targetUser.avatar" size="default" />
            <span class="target-name">{{ targetUser.nickname || targetUser.username }}</span>
            <el-tag size="small" type="info">
              {{ form.evaluatorId === orderInfo?.buyerId ? '卖家' : '买家' }}
            </el-tag>
          </div>
        </el-form-item>

        <el-form-item label="评分" prop="score">
          <div class="score-section">
            <el-rate
              v-model="form.score"
              :max="5"
              show-text
              :texts="['极差', '较差', '一般', '满意', '非常满意']"
              :colors="['#F56C6C', '#E6A23C', '#E6A23C', '#67C23A', '#67C23A']"
              size="large"
            />
            <div class="score-desc">{{ getScoreDescription(form.score) }}</div>
          </div>
        </el-form-item>

        <el-form-item label="评价内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请描述您对本次交易的感受..."
            maxlength="500"
            show-word-limit
            :autosize="{ minRows: 4, maxRows: 8 }"
          />
          <div class="content-tips">
            <el-text size="small" type="info">
              <el-icon><InfoFilled /></el-icon>
              请客观公正地评价，您的评价将影响对方的信用分
            </el-text>
          </div>
        </el-form-item>

        <!-- 信用分影响提示 -->
        <el-form-item>
          <el-alert
            :title="getCreditImpactText(form.score)"
            :type="getCreditImpactType(form.score)"
            show-icon
            :closable="false"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleClose">取消</el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="submitting"
            :disabled="!form.score"
          >
            提交评价
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import { createEvaluation } from '@/api/credit'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  orderInfo: {
    type: Object,
    default: null
  },
  targetUser: {
    type: Object,
    default: () => ({})
  },
  evaluatorId: {
    type: Number,
    required: true
  }
})

// Emits
const emit = defineEmits(['update:visible', 'success'])

// 响应式数据
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  orderId: null,
  targetId: null,
  score: 5,
  content: ''
})

const rules = {
  score: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ]
}

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 监听props变化，初始化表单
const initForm = () => {
  console.log('初始化评价表单，orderInfo:', props.orderInfo, 'targetUser:', props.targetUser)

  // 重置表单数据
  form.orderId = null
  form.targetId = null
  form.score = 5
  form.content = ''

  if (props.orderInfo && props.targetUser) {
    form.orderId = props.orderInfo.id
    form.targetId = props.targetUser.id

    console.log('表单初始化完成:', {
      orderId: form.orderId,
      targetId: form.targetId,
      score: form.score,
      orderInfo: props.orderInfo,
      targetUser: props.targetUser
    })

    // 验证必要的数据
    if (!form.orderId) {
      console.error('订单ID为空:', props.orderInfo)
    }
    if (!form.targetId) {
      console.error('被评价用户ID为空:', props.targetUser)
    }
  } else {
    console.error('表单初始化失败，缺少必要数据:', {
      hasOrderInfo: !!props.orderInfo,
      hasTargetUser: !!props.targetUser,
      orderInfo: props.orderInfo,
      targetUser: props.targetUser
    })
  }
}

// 获取评分描述
const getScoreDescription = (score) => {
  const descriptions = {
    1: '本次交易体验很差，存在严重问题',
    2: '本次交易体验较差，有一些问题',
    3: '本次交易体验一般，勉强满意',
    4: '本次交易体验不错，比较满意',
    5: '本次交易体验很好，非常满意'
  }
  return descriptions[score] || '请选择评分'
}

// 获取信用分影响文本
const getCreditImpactText = (score) => {
  const impacts = {
    1: '差评会对对方信用记录产生负面影响',
    2: '中评会对对方信用记录产生轻微负面影响',
    3: '中性评价不会影响对方信用记录',
    4: '好评会提升对方信用记录',
    5: '优秀评价会显著提升对方信用记录'
  }
  return impacts[score] || ''
}

// 获取信用分影响类型
const getCreditImpactType = (score) => {
  if (score <= 2) return 'error'
  if (score === 3) return 'info'
  return 'success'
}

// 提交评价
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    // 提交前的数据验证和调试
    console.log('准备提交评价，表单数据:', {
      orderId: form.orderId,
      targetId: form.targetId,
      score: form.score,
      content: form.content
    })

    // 验证必要数据
    if (!form.orderId) {
      ElMessage.error('订单ID不能为空')
      return
    }
    if (!form.targetId) {
      ElMessage.error('被评价用户ID不能为空')
      return
    }
    if (!form.score) {
      ElMessage.error('评分不能为空')
      return
    }

    submitting.value = true

    const response = await createEvaluation({
      orderId: form.orderId,
      targetId: form.targetId,
      score: form.score,
      content: form.content
    })

    console.log('评价提交响应:', response)

    if (response.success) {
      ElMessage.success('评价提交成功')
      emit('success', response.data)
      handleClose()
    } else {
      ElMessage.error(response.message || '评价提交失败')
    }
  } catch (error) {
    if (error.fields) {
      // 表单验证失败
      return
    }
    console.error('提交评价失败:', error)
    ElMessage.error('评价提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  formRef.value?.resetFields()
  form.content = ''
  emit('update:visible', false)
}

// 监听对话框打开，初始化表单
const handleOpen = () => {
  initForm()
}

// 监听 visible 变化，在对话框打开时初始化表单
watch(visible, (newVisible) => {
  if (newVisible) {
    // 对话框打开时，稍微延迟一下再初始化，确保props已经传递完毕
    nextTick(() => {
      initForm()
    })
  }
})

// 暴露方法给父组件
defineExpose({
  handleOpen
})
</script>

<style scoped>
.order-info {
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.order-no {
  font-weight: 500;
  color: #303133;
}

.order-content {
  padding: 0;
}

.product-info {
  display: flex;
  gap: 15px;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  flex-shrink: 0;
}

.product-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.product-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  font-size: 18px;
  font-weight: bold;
  color: #e6a23c;
}

.target-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
}

.target-name {
  font-weight: 500;
  color: #303133;
}

.score-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.score-desc {
  font-size: 14px;
  color: #666;
  margin-left: 5px;
}

.content-tips {
  margin-top: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-rate__text) {
  font-weight: 500;
  margin-left: 10px;
}

:deep(.el-alert__content) {
  font-size: 14px;
}
</style>