<template>
  <div class="arbitration-apply-page">
    <el-card class="apply-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h3><el-icon><Document /></el-icon> {{ isEditMode ? '修改仲裁' : '申请仲裁' }}</h3>
          <el-button @click="$router.go(-1)" type="info" plain size="small">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="arbitration-form"
        @submit.prevent="submitForm"
      >
        <!-- 订单信息 -->
        <el-card class="section-card" shadow="never">
          <template #header>
            <h4><el-icon><Document /></el-icon> 订单信息</h4>
          </template>

          <el-form-item label="订单编号" prop="orderId" required>
            <el-input
              v-model="form.orderNo"
              placeholder="请输入订单编号"
              @blur="loadOrderInfo"
              clearable
            >
              <template #append>
                <el-button
                  @click="loadOrderInfo"
                  :loading="orderLoading"
                  type="primary"
                >
                  验证
                </el-button>
              </template>
            </el-input>
          </el-form-item>

          <!-- 订单详情展示 -->
          <div v-if="orderInfo" class="order-info-display">
            <el-row :gutter="20">
              <el-col :span="4">
                <el-image
                  :src="orderInfo.productImage"
                  :preview-src-list="[orderInfo.productImage]"
                  class="product-image"
                  fit="cover"
                >
                  <template #error>
                    <div class="image-slot">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
              </el-col>
              <el-col :span="20">
                <div class="order-details">
                  <h4>{{ orderInfo.productTitle }}</h4>
                  <p><strong>订单金额：</strong>¥{{ orderInfo.totalAmount }}</p>
                  <p><strong>交易对象：</strong>{{ orderInfo.tradeUser.nickname }}</p>
                  <p><strong>订单时间：</strong>{{ formatDate(orderInfo.createTime) }}</p>
                  <el-tag :type="getOrderStatusType(orderInfo.status)">
                    {{ getOrderStatusText(orderInfo.status) }}
                  </el-tag>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>

        <!-- 仲裁信息 -->
        <el-card class="section-card" shadow="never">
          <template #header>
            <h4><el-icon><Warning /></el-icon> 仲裁信息</h4>
          </template>

          <el-form-item label="仲裁原因" prop="reason" required>
            <el-select v-model="form.reason" placeholder="请选择仲裁原因" style="width: 100%">
              <el-option label="商品质量问题" value="QUALITY_ISSUE" />
              <el-option label="发货延迟" value="SHIPPING_DELAY" />
              <el-option label="商品描述不符" value="DESCRIPTION_MISMATCH" />
              <el-option label="卖家无响应" value="NO_RESPONSE" />
              <el-option label="其他问题" value="OTHER" />
            </el-select>
          </el-form-item>

          <el-form-item label="问题描述" prop="description" required>
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="6"
              placeholder="请详细描述遇到的问题，以便我们更好地处理您的申请"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="证据材料" prop="evidence">
            <div class="evidence-upload">
              <el-upload
                v-model:file-list="fileList"
                :http-request="handleEvidenceUpload"
                list-type="picture-card"
                :on-preview="handlePictureCardPreview"
                :on-remove="handleRemove"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeUpload"
                :limit="5"
                accept="image/*"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <el-dialog v-model="previewVisible" title="预览图片">
                <img w-full :src="previewUrl" alt="Preview" style="width: 100%" />
              </el-dialog>
              <div class="upload-tip">
                <el-text type="info" size="small">
                  支持上传聊天记录截图、商品照片等证据材料，最多5张
                </el-text>
              </div>
            </div>
          </el-form-item>
        </el-card>

        <!-- 重要提示 -->
        <el-alert
          title="重要提示"
          type="warning"
          :closable="false"
          show-icon
          class="important-notice"
        >
          <p>1. 请确保提供的信息真实有效，虚假申请将承担相应法律责任</p>
          <p>2. 仲裁申请一旦提交无法修改，请仔细核实信息后提交</p>
          <p>3. 我们将在3个工作日内处理您的申请，请耐心等待</p>
          <p>4. 如有疑问，可联系客服：400-888-8888</p>
        </el-alert>

        <!-- 提交按钮 -->
        <el-form-item class="submit-section">
          <el-button type="primary" @click="submitForm" :loading="submitting" size="large">
            <el-icon><Check /></el-icon>
            提交仲裁申请
          </el-button>
          <el-button @click="resetForm" size="large">
            <el-icon><Refresh /></el-icon>
            重置表单
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, ArrowLeft, DocumentCopy, Warning, Plus, Check, Refresh, Picture } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { arbitrationApi } from '@/api/arbitration'
import * as tradeApi from '@/api/trade'
import { uploadArbitrationEvidence } from '@/api/file'
import { format } from 'date-fns'

const route = useRoute()
const router = useRouter()

// 表单引用
const formRef = ref()

// 响应式数据
const orderLoading = ref(false)
const submitting = ref(false)
const orderInfo = ref(null)
const fileList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')
const editArbitrationId = ref(route.query.arbitrationId ? Number(route.query.arbitrationId) : null)
const isEditMode = computed(() => !!editArbitrationId.value)

// 表单数据
const form = reactive({
  orderNo: '',
  orderId: null,
  respondentId: null,
  reason: '',
  description: '',
  evidence: []
})

// 表单验证规则
const rules = {
  orderNo: [
    { required: true, message: '请输入订单编号', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请选择仲裁原因', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 10, message: '问题描述至少10个字符', trigger: 'blur' }
  ]
}

// 加载订单信息
const loadOrderInfo = async () => {
  if (!form.orderNo.trim()) {
    ElMessage.warning('请输入订单编号')
    return
  }

  orderLoading.value = true
  try {
    // 通过订单号查询订单信息
    console.log('🔍 查询订单号:', form.orderNo)
    
    const orderResponse = await tradeApi.getOrderByOrderNo(form.orderNo)
    console.log('📦 订单API响应:', orderResponse)
    
    if (orderResponse.code === 200 && orderResponse.data) {
      const order = orderResponse.data
      
      // 默认图片URL（如果后端没有返回图片）
      const defaultImage = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
      
      // 构建订单信息显示
      orderInfo.value = {
        id: order.id,
        orderNo: order.orderNo,
        productTitle: order.productName || order.productTitle || '商品',
        productImage: order.productImage || order.productPicture || order.productImg || defaultImage,
        totalAmount: order.totalAmount || order.amount || 0,
        status: order.status,
        createTime: order.createTime ? new Date(order.createTime) : null,
        tradeUser: {
          id: order.sellerId || order.sellerUserId || 0,
          nickname: order.sellerName || order.sellerNickname || '卖家',
          avatar: order.sellerAvatar || ''
        }
      }

      form.orderId = orderInfo.value.id
      form.respondentId = orderInfo.value.tradeUser.id

      ElMessage.success('订单信息加载成功')
    } else {
      throw new Error(orderResponse.message || '订单不存在')
    }

  } catch (error) {
    console.error('订单查询失败:', error)
    
    // 如果是404错误，提供更友好的提示
    let errorMessage = '订单信息加载失败'
    if (error.response?.status === 404 || error.message?.includes('Not Found')) {
      errorMessage = '订单不存在，请检查订单号是否正确，或前往"我的订单"查看可用订单'
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
    orderInfo.value = null
    form.orderId = null
    form.respondentId = null
  } finally {
    orderLoading.value = false
  }
}

// 获取订单状态类型
const getOrderStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'primary',
    2: 'info',
    3: 'success',
    4: 'danger',
    5: 'warning'
  }
  return types[status] || 'info'
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const texts = {
    0: '待支付',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消',
    5: '售后中'
  }
  return texts[status] || '未知'
}

// 格式化日期
const formatDate = (date) => {
  return format(new Date(date), 'yyyy-MM-dd HH:mm')
}

// 图片预览
const handlePictureCardPreview = (file) => {
  previewUrl.value = file.url || file.response?.data?.url || ''
  previewVisible.value = true
}

// 移除图片
const handleRemove = (file) => {
  const fileUrl = file.url || file.response?.data?.url
  const index = form.evidence.findIndex(url => url === fileUrl)
  if (index > -1) {
    form.evidence.splice(index, 1)
  }
}

// 上传成功
const handleEvidenceUpload = async (options) => {
  try {
    const res = await uploadArbitrationEvidence(options.file)

    if (res.code === 200 && res.data) {
      options.onSuccess?.({
        code: 200,
        data: {
          url: res.data
        }
      }, options.file)
      return
    }

    throw new Error(res.message || '图片上传失败')
  } catch (error) {
    options.onError?.(error)
  }
}

const handleUploadSuccess = (response, file) => {
  const url = response?.data?.url
  if (response?.code === 200 && url) {
    file.url = url
    form.evidence.push(url)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

// 上传前验证
const handleUploadError = (error) => {
  ElMessage.error(error?.message || '图片上传失败')
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

// 提交表单
const loadArbitrationForEdit = async () => {
  if (!editArbitrationId.value) return

  const res = await arbitrationApi.getArbitrationDetail(editArbitrationId.value)
  const data = res?.data
  if (!data) {
    throw new Error('未找到仲裁申请')
  }

  form.orderId = data.orderId
  form.respondentId = data.respondentId
  form.reason = data.reason || ''
  form.description = data.description || ''

  let evidenceList = []
  if (typeof data.evidence === 'string' && data.evidence.trim()) {
    try {
      evidenceList = JSON.parse(data.evidence)
    } catch {
      evidenceList = []
    }
  } else if (Array.isArray(data.evidence)) {
    evidenceList = data.evidence
  }

  form.evidence = evidenceList
  fileList.value = evidenceList.map((url, index) => ({
    name: `evidence-${index + 1}`,
    url
  }))
}

const submitForm = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    if (!orderInfo.value) {
      ElMessage.warning('请先验证订单信息')
      return
    }

    await ElMessageBox.confirm(
      '确认提交仲裁申请？提交后无法修改。',
      '确认提交',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true

    const submitData = {
      orderId: form.orderId,
      respondentId: form.respondentId,
      reason: form.reason,
      description: form.description,
      evidence: JSON.stringify(form.evidence || [])
    }

    if (isEditMode.value) {
      await arbitrationApi.updateArbitration(editArbitrationId.value, submitData)
      router.push('/arbitration/list')
      ElMessage.success('仲裁申请修改成功')
    } else {
      await arbitrationApi.createArbitration(submitData)
      router.push('/arbitration/list')
      ElMessage.success('仲裁申请提交成功')
    }


    // 跳转到仲裁列表页面
    router.push('/arbitration/list')

  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败：' + (error.message || '未知错误'))
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  orderInfo.value = null
  fileList.value = []
  form.evidence = []
}

// 页面初始化
onMounted(async () => {
  // 如果路由参数中有订单号，自动填入
  try {
    if (isEditMode.value) {
      await loadArbitrationForEdit()
    }

    if (route.query.orderNo) {
      form.orderNo = route.query.orderNo
      await loadOrderInfo()
    }
  } catch (error) {
    ElMessage.error(error?.message || '加载仲裁信息失败')
  }
})
</script>

<style scoped>
.arbitration-apply-page {
  max-width: 800px;
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

.section-card {
  margin-bottom: 20px;
}

.section-card :deep(.el-card__header) {
  padding: 12px 20px;
  background: #fafbfc;
}

.section-card h4 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.order-info-display {
  margin-top: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
}

.order-details h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
}

.order-details p {
  margin: 6px 0;
  color: #606266;
  font-size: 14px;
}

.evidence-upload {
  width: 100%;
}

.upload-tip {
  margin-top: 8px;
}

.important-notice {
  margin: 20px 0;
}

.important-notice p {
  margin: 4px 0;
  font-size: 14px;
}

.submit-section {
  margin-top: 30px;
  text-align: center;
}

.submit-section .el-button {
  margin: 0 10px;
  padding: 12px 30px;
}

@media (max-width: 768px) {
  .arbitration-apply-page {
    padding: 10px;
  }

  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .order-info-display .el-row {
    flex-direction: column;
  }

  .submit-section .el-button {
    display: block;
    width: 100%;
    margin: 5px 0;
  }
}
</style>
