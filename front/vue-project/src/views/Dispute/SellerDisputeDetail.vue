<template>
  <div class="seller-dispute-detail-page" v-loading="loading">
    <el-card shadow="hover" v-if="detail">
      <template #header>
        <div class="header-row">
          <h3>卖家协商响应 #{{ detail.id }}</h3>
          <el-button @click="$router.go(-1)">返回</el-button>
        </div>
      </template>

      <el-descriptions border :column="2">
        <el-descriptions-item label="订单ID">{{ detail.orderId }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.statusLabel }}</el-descriptions-item>
        <el-descriptions-item label="争议原因">{{ detail.reason }}</el-descriptions-item>
        <el-descriptions-item label="诉求类型">{{ detail.requestType }}</el-descriptions-item>
        <el-descriptions-item label="期望金额">¥{{ formatAmount(detail.expectedAmount) }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ formatTime(detail.expireTime) }}</el-descriptions-item>
        <el-descriptions-item label="事实说明" :span="2">{{ detail.factDescription }}</el-descriptions-item>
        <el-descriptions-item label="诉求说明" :span="2">{{ detail.requestDescription }}</el-descriptions-item>
      </el-descriptions>

      <el-card class="result-card" shadow="never">
        <template #header><span>平台裁决与执行进度</span></template>
        <el-descriptions border :column="1">
          <el-descriptions-item label="裁决类型">{{ decisionTypeLabel(detail.finalDecisionType) }}</el-descriptions-item>
          <el-descriptions-item label="执行状态">{{ detail.finalExecutionStatus || detail.executionStatusLabel || '-' }}</el-descriptions-item>
          <el-descriptions-item label="结果说明">{{ detail.finalResultDescription || detail.executionRemark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下一步提示">{{ detail.nextActionHint || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-form
        v-if="detail.canSellerRespond"
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="response-form"
      >
        <el-form-item label="响应类型" prop="responseType">
          <el-radio-group v-model="form.responseType">
            <el-radio value="AGREE">同意买家诉求</el-radio>
            <el-radio value="REJECT">拒绝买家诉求</el-radio>
            <el-radio value="PROPOSE">提出替代方案</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="响应说明" prop="responseDescription" v-if="form.responseType !== 'PROPOSE'">
          <el-input v-model="form.responseDescription" type="textarea" :rows="3" />
        </el-form-item>

        <template v-if="form.responseType === 'PROPOSE'">
          <el-form-item label="方案类型" prop="proposalType">
            <el-select v-model="form.proposalType" style="width: 100%">
              <el-option label="部分退款" value="PARTIAL_REFUND" />
              <el-option label="全额退款" value="FULL_REFUND" />
              <el-option label="退货退款" value="RETURN_AND_REFUND" />
              <el-option label="补发/换货" value="REPLACE" />
            </el-select>
          </el-form-item>
          <el-form-item label="方案金额" prop="proposalAmount">
            <el-input-number v-model="form.proposalAmount" :precision="2" :min="0" :max="999999" />
          </el-form-item>
          <el-form-item label="运费承担" prop="freightBearer">
            <el-select v-model="form.freightBearer" style="width: 100%">
              <el-option label="买家承担" value="BUYER" />
              <el-option label="卖家承担" value="SELLER" />
              <el-option label="平台承担" value="PLATFORM" />
            </el-select>
          </el-form-item>
          <el-form-item label="方案说明" prop="proposalDescription">
            <el-input v-model="form.proposalDescription" type="textarea" :rows="3" />
          </el-form-item>
        </template>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitResponse">提交响应</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { arbitrationApi } from '@/api/arbitration'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const detail = ref(null)
const formRef = ref()

const form = reactive({
  responseType: 'AGREE',
  responseDescription: '',
  proposalType: '',
  proposalAmount: 0,
  proposalDescription: '',
  freightBearer: ''
})

const rules = {
  responseType: [{ required: true, message: '请选择响应类型', trigger: 'change' }],
  proposalType: [{
    validator: (_, value, callback) => {
      if (form.responseType === 'PROPOSE' && !value) callback(new Error('请选择方案类型'))
      else callback()
    }, trigger: 'change'
  }],
  proposalDescription: [{
    validator: (_, value, callback) => {
      if (form.responseType === 'PROPOSE' && !value) callback(new Error('请填写方案说明'))
      else callback()
    }, trigger: 'blur'
  }]
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

const submitResponse = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    const payload = {
      disputeId: Number(route.params.id),
      responseType: form.responseType,
      responseDescription: form.responseDescription,
      proposalType: form.proposalType,
      proposalAmount: form.proposalAmount,
      proposalDescription: form.proposalDescription,
      freightBearer: form.freightBearer
    }
    await arbitrationApi.sellerRespondDispute(payload)
    ElMessage.success('响应提交成功')
    router.push('/dispute/seller/pending')
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.seller-dispute-detail-page {
  padding: 20px 0;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-card {
  margin-top: 16px;
}

.response-form {
  margin-top: 20px;
}
</style>

