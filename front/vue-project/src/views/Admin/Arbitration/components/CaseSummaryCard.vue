<template>
  <el-card class="case-summary-card" shadow="never">
    <div class="summary-head">
      <div class="head-main">
        <div class="case-no">{{ caseData.caseNumber }}</div>
        <div class="head-tags">
          <el-tag :type="statusInfo.type" effect="dark">{{ statusInfo.label }}</el-tag>
          <el-tag type="warning" effect="plain">{{ caseData.reasonLabel }}</el-tag>
          <el-tag type="success" effect="plain">¥{{ formatAmount(caseData.orderAmount) }}</el-tag>
        </div>
      </div>
      <div class="head-amount">
        <div class="amount-label">订单金额</div>
        <div class="amount-value">¥{{ formatAmount(caseData.orderAmount) }}</div>
      </div>
    </div>

    <div class="summary-grid">
      <div class="summary-item">
        <span class="label">商品名称</span>
        <span class="value strong">{{ caseData.productName }}</span>
      </div>
      <div class="summary-item">
        <span class="label">订单号</span>
        <span class="value">{{ caseData.orderNo }}</span>
      </div>
      <div class="summary-item">
        <span class="label">申请人</span>
        <span class="value">{{ caseData.applicant }}</span>
      </div>
      <div class="summary-item">
        <span class="label">被申请人</span>
        <span class="value">{{ caseData.respondent }}</span>
      </div>
      <div class="summary-item">
        <span class="label">发起时间</span>
        <span class="value">{{ caseData.createTime }}</span>
      </div>
      <div class="summary-item">
        <span class="label">当前处理人</span>
        <span class="value">{{ caseData.handler || '-' }}</span>
      </div>
      <div class="summary-item">
        <span class="label">来源争议ID</span>
        <span class="value">{{ caseData.sourceDisputeId || '-' }}</span>
      </div>
    </div>

    <div class="summary-actions">
      <slot name="actions" />
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  caseData: {
    type: Object,
    required: true
  }
})

const statusMap = {
  pending: { label: '待处理', type: 'warning' },
  processing: { label: '处理中', type: 'primary' },
  completed: { label: '已完结', type: 'success' },
  rejected: { label: '已驳回', type: 'danger' }
}

const statusInfo = computed(() => statusMap[props.caseData.status] || statusMap.pending)

const formatAmount = (amount) => {
  const value = Number(amount || 0)
  return value.toFixed(2)
}
</script>

<style scoped>
.case-summary-card {
  border: 1px solid #dbe6f5;
  border-radius: 14px;
  background: linear-gradient(135deg, #f8fbff 0%, #eef5ff 55%, #f6f9ff 100%);
  margin-bottom: 16px;
}

.summary-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.head-main {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.case-no {
  font-size: 20px;
  font-weight: 700;
  color: #1f2a37;
  letter-spacing: 0.3px;
}

.head-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.head-amount {
  min-width: 164px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #dbe7ff;
}

.amount-label {
  font-size: 12px;
  color: #6b7280;
}

.amount-value {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 700;
  color: #0f766e;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px 12px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #ffffff;
  border: 1px solid #e5ebf6;
}

.summary-item .label {
  font-size: 12px;
  color: #6b7280;
}

.summary-item .value {
  font-size: 14px;
  color: #111827;
  font-weight: 500;
}

.summary-item .value.strong {
  font-size: 15px;
  font-weight: 600;
}

.summary-actions {
  margin-top: 14px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .summary-head {
    flex-direction: column;
  }

  .head-amount {
    width: 100%;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
