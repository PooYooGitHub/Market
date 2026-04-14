<template>
  <div class="dispute-apply-page">
    <el-card shadow="hover">
      <template #header>
        <div class="header-row">
          <h3>发起争议协商</h3>
          <el-button @click="$router.go(-1)" plain>返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="请输入订单号并校验">
            <template #append>
              <el-button :loading="orderLoading" @click="loadOrderInfo">校验</el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-alert
          v-if="orderInfo"
          type="success"
          :closable="false"
          class="order-alert"
          :title="`订单已匹配：${orderInfo.orderNo}，卖家ID：${orderInfo.sellerId}`"
        />

        <el-form-item label="争议原因" prop="reason">
          <el-select v-model="form.reason" placeholder="请选择争议原因" style="width: 100%">
            <el-option label="商品质量问题" value="QUALITY_ISSUE" />
            <el-option label="商品描述不符" value="DESCRIPTION_MISMATCH" />
            <el-option label="物流或发货问题" value="SHIPPING_DELAY" />
            <el-option label="售后服务问题" value="SERVICE_ISSUE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="事实说明" prop="factDescription">
          <el-input
            v-model="form.factDescription"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            placeholder="描述发生了什么问题（事实）"
          />
        </el-form-item>

        <el-form-item label="诉求类型" prop="requestType">
          <el-select v-model="form.requestType" placeholder="请选择诉求类型" style="width: 100%">
            <el-option label="全额退款" value="FULL_REFUND" />
            <el-option label="部分退款" value="PARTIAL_REFUND" />
            <el-option label="退货退款" value="RETURN_AND_REFUND" />
            <el-option label="补发/换货" value="REPLACE" />
          </el-select>
        </el-form-item>

        <el-form-item label="诉求说明" prop="requestDescription">
          <el-input
            v-model="form.requestDescription"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            placeholder="描述你希望平台如何处理（诉求）"
          />
        </el-form-item>

        <el-form-item label="期望金额" prop="expectedAmount">
          <el-input-number v-model="form.expectedAmount" :precision="2" :min="0" :max="999999" />
        </el-form-item>

        <el-form-item label="证据上传">
          <el-upload
            v-model:file-list="fileList"
            list-type="picture-card"
            :http-request="uploadEvidence"
            :on-preview="previewFile"
            :on-remove="removeEvidence"
            :limit="8"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">提交争议申请</el-button>
          <el-button @click="$router.push('/dispute/my')">我的争议</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="previewVisible" title="证据预览" width="520px">
      <img :src="previewUrl" alt="preview" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import * as tradeApi from '@/api/trade'
import { arbitrationApi } from '@/api/arbitration'
import { uploadArbitrationEvidence } from '@/api/file'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const orderLoading = ref(false)
const submitting = ref(false)
const orderInfo = ref(null)
const fileList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

const form = reactive({
  orderNo: '',
  orderId: null,
  reason: '',
  factDescription: '',
  requestType: '',
  requestDescription: '',
  expectedAmount: 0,
  evidenceList: []
})

const rules = {
  orderNo: [{ required: true, message: '请输入订单号', trigger: 'blur' }],
  reason: [{ required: true, message: '请选择争议原因', trigger: 'change' }],
  factDescription: [{ required: true, message: '请输入事实说明', trigger: 'blur' }],
  requestType: [{ required: true, message: '请选择诉求类型', trigger: 'change' }],
  requestDescription: [{ required: true, message: '请输入诉求说明', trigger: 'blur' }]
}

const loadOrderInfo = async () => {
  if (!form.orderNo) {
    ElMessage.warning('请先输入订单号')
    return
  }
  orderLoading.value = true
  try {
    const res = await tradeApi.getOrderByOrderNo(form.orderNo.trim())
    if (!res?.data?.id) {
      throw new Error(res?.message || '订单不存在')
    }
    orderInfo.value = res.data
    form.orderId = res.data.id
    ElMessage.success('订单校验成功')
  } catch (error) {
    orderInfo.value = null
    form.orderId = null
    ElMessage.error(error?.message || '订单校验失败')
  } finally {
    orderLoading.value = false
  }
}

const uploadEvidence = async (options) => {
  try {
    const res = await uploadArbitrationEvidence(options.file)
    if (res?.code === 200 && res?.data) {
      const url = String(res.data)
      const item = {
        evidenceType: 'IMAGE',
        title: options.file?.name || '证据图片',
        description: '',
        fileUrl: url,
        thumbnailUrl: url
      }
      form.evidenceList.push(item)
      options.onSuccess?.({ code: 200, data: { url } })
      ElMessage.success('证据上传成功')
      return
    }
    throw new Error(res?.message || '上传失败')
  } catch (error) {
    options.onError?.(error)
    ElMessage.error(error?.message || '上传失败')
  }
}

const removeEvidence = (file) => {
  const url = file.url || file.response?.data?.url
  form.evidenceList = form.evidenceList.filter(item => item.fileUrl !== url)
}

const previewFile = (file) => {
  previewUrl.value = file.url || file.response?.data?.url || ''
  previewVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (!form.orderId) {
      ElMessage.warning('请先校验订单号')
      return
    }
    submitting.value = true
    const payload = {
      orderId: form.orderId,
      reason: form.reason,
      factDescription: form.factDescription,
      requestType: form.requestType,
      requestDescription: form.requestDescription,
      expectedAmount: form.expectedAmount,
      evidenceList: form.evidenceList
    }
    await arbitrationApi.createDispute(payload)
    ElMessage.success('争议申请已提交，等待卖家协商响应')
    router.push('/dispute/my')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (route.query.orderNo) {
    form.orderNo = String(route.query.orderNo)
    loadOrderInfo()
  }
})
</script>

<style scoped>
.dispute-apply-page {
  max-width: 920px;
  margin: 0 auto;
  padding: 20px 0;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-alert {
  margin-bottom: 16px;
}
</style>

