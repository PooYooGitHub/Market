<template>
  <div v-if="visible" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <div class="modal-header">
        <h3 class="modal-title">支付详情</h3>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>

      <div class="modal-body">
        <div class="payment-overview">
          <div class="payment-method">
            <span class="method-icon">{{ getMethodIcon(payment.paymentMethod) }}</span>
            <div class="method-info">
              <div class="method-name">{{ getMethodName(payment.paymentMethod) }}</div>
              <div :class="['payment-status', `status-${payment.status}`]">
                {{ getStatusText(payment.status) }}
              </div>
            </div>
          </div>

          <div class="payment-amount">
            <div class="amount-label">支付金额</div>
            <div class="amount-value">¥{{ Number(payment.amount).toFixed(2) }}</div>
          </div>
        </div>

        <div class="detail-sections">
          <!-- 订单信息 -->
          <div class="detail-section">
            <h4 class="section-title">📦 订单信息</h4>
            <div class="order-card">
              <img
                :src="payment.productImage || defaultImage"
                :alt="payment.productTitle"
                class="product-img"
              />
              <div class="product-info">
                <div class="product-title">{{ payment.productTitle }}</div>
                <div class="product-price">¥{{ Number(payment.amount).toFixed(2) }}</div>
                <div class="order-no">订单号：{{ payment.orderNo }}</div>
              </div>
            </div>
          </div>

          <!-- 支付信息 -->
          <div class="detail-section">
            <h4 class="section-title">💳 支付信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">交易单号：</span>
                <span class="info-value">{{ payment.transactionId || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">支付方式：</span>
                <span class="info-value">{{ getMethodName(payment.paymentMethod) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">创建时间：</span>
                <span class="info-value">{{ formatTime(payment.createTime) }}</span>
              </div>
              <div v-if="payment.payTime" class="info-item">
                <span class="info-label">支付时间：</span>
                <span class="info-value">{{ formatTime(payment.payTime) }}</span>
              </div>
              <div v-if="payment.finishTime" class="info-item">
                <span class="info-label">完成时间：</span>
                <span class="info-value">{{ formatTime(payment.finishTime) }}</span>
              </div>
              <div v-if="payment.failReason" class="info-item">
                <span class="info-label">失败原因：</span>
                <span class="info-value error">{{ payment.failReason }}</span>
              </div>
            </div>
          </div>

          <!-- 金额明细 -->
          <div class="detail-section">
            <h4 class="section-title">💰 金额明细</h4>
            <div class="amount-breakdown">
              <div class="amount-item">
                <span class="amount-label">商品金额：</span>
                <span class="amount-value">¥{{ Number(payment.amount).toFixed(2) }}</span>
              </div>
              <div class="amount-item">
                <span class="amount-label">运费：</span>
                <span class="amount-value">¥0.00</span>
              </div>
              <div class="amount-item">
                <span class="amount-label">优惠：</span>
                <span class="amount-value">-¥0.00</span>
              </div>
              <div class="amount-item total">
                <span class="amount-label">实付金额：</span>
                <span class="amount-value">¥{{ Number(payment.amount).toFixed(2) }}</span>
              </div>
            </div>
          </div>

          <!-- 支付流程 -->
          <div class="detail-section">
            <h4 class="section-title">🔄 支付流程</h4>
            <div class="payment-timeline">
              <div class="timeline-item completed">
                <div class="timeline-dot"></div>
                <div class="timeline-content">
                  <div class="timeline-title">创建支付</div>
                  <div class="timeline-time">{{ formatTime(payment.createTime) }}</div>
                </div>
              </div>

              <div :class="['timeline-item', { completed: payment.status === 'success' || payment.status === 'failed' }]">
                <div class="timeline-dot"></div>
                <div class="timeline-content">
                  <div class="timeline-title">
                    {{ payment.status === 'success' ? '支付成功' : payment.status === 'failed' ? '支付失败' : '处理中' }}
                  </div>
                  <div class="timeline-time">
                    {{ payment.payTime ? formatTime(payment.payTime) : '-' }}
                  </div>
                </div>
              </div>

              <div v-if="payment.status === 'success'" class="timeline-item completed">
                <div class="timeline-dot"></div>
                <div class="timeline-content">
                  <div class="timeline-title">支付完成</div>
                  <div class="timeline-time">{{ formatTime(payment.finishTime || payment.payTime) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button
          v-if="payment.status === 'failed' && payment.orderId"
          class="btn btn-primary"
          @click="$emit('retry', payment)"
        >
          重新支付
        </button>
        <button
          v-if="payment.status === 'success' && canRefund(payment)"
          class="btn btn-secondary"
          @click="$emit('refund', payment)"
        >
          申请退款
        </button>
        <button class="btn btn-default" @click="$emit('close')">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  payment: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['close', 'retry', 'refund'])

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="80" height="80"%3E%3Crect fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999"%3E商品%3C/text%3E%3C/svg%3E'

// 获取支付方式图标
const getMethodIcon = (method) => {
  const iconMap = {
    alipay: '💙',
    wechat: '💚',
    balance: '🪙',
    bank: '🏦'
  }
  return iconMap[method] || '💳'
}

// 获取支付方式名称
const getMethodName = (method) => {
  const nameMap = {
    alipay: '支付宝',
    wechat: '微信支付',
    balance: '余额支付',
    bank: '银行卡支付'
  }
  return nameMap[method] || '未知方式'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '支付中',
    success: '支付成功',
    failed: '支付失败',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知状态'
}

// 判断是否可以退款
const canRefund = (payment) => {
  if (payment.status !== 'success') return false

  // 24小时内可以申请退款
  const payTime = new Date(payment.payTime)
  const now = new Date()
  const diffHours = (now - payTime) / (1000 * 60 * 60)

  return diffHours <= 24
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal {
  background: white;
  border-radius: 16px;
  width: 600px;
  max-width: 95vw;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e8e8e8;
}

.modal-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #f0f0f0;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.close-btn:hover {
  background: #e0e0e0;
  color: #666;
}

.modal-body {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

.payment-overview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 24px;
}

.payment-method {
  display: flex;
  align-items: center;
  gap: 12px;
}

.method-icon {
  font-size: 24px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: white;
}

.method-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.payment-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status-pending {
  background: #fff3e0;
  color: #ff9800;
}

.status-success {
  background: #e8f5e9;
  color: #4caf50;
}

.status-failed {
  background: #ffebee;
  color: #f44336;
}

.status-cancelled {
  background: #f5f5f5;
  color: #999;
}

.payment-amount {
  text-align: right;
}

.amount-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 4px;
}

.amount-value {
  font-size: 24px;
  font-weight: bold;
  color: #ff4081;
}

.detail-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0;
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.order-card {
  display: flex;
  gap: 16px;
  padding: 20px;
}

.product-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
}

.product-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
}

.product-price {
  font-size: 18px;
  font-weight: bold;
  color: #ff4081;
  margin-bottom: 8px;
}

.order-no {
  font-size: 14px;
  color: #999;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 14px;
  color: #666;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.info-value.error {
  color: #f44336;
}

.amount-breakdown {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.amount-item.total {
  padding-top: 12px;
  border-top: 1px solid #e8e8e8;
  font-size: 16px;
  font-weight: bold;
}

.amount-item .amount-label {
  color: #666;
}

.amount-item .amount-value {
  color: #333;
  font-weight: 500;
}

.amount-item.total .amount-value {
  color: #ff4081;
  font-size: 18px;
}

.payment-timeline {
  padding: 20px;
}

.timeline-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 12px 0;
  position: relative;
}

.timeline-item:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 11px;
  top: 32px;
  width: 2px;
  height: calc(100% - 8px);
  background: #e8e8e8;
}

.timeline-item.completed::after {
  background: #4caf50;
}

.timeline-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e8e8e8;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.timeline-item.completed .timeline-dot {
  background: #4caf50;
}

.timeline-item.completed .timeline-dot::before {
  content: '✓';
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.timeline-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.timeline-time {
  font-size: 12px;
  color: #999;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e8e8e8;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5a6fd8;
  transform: translateY(-1px);
}

.btn-secondary {
  background: #ff9800;
  color: white;
}

.btn-secondary:hover {
  background: #f57c00;
  transform: translateY(-1px);
}

.btn-default {
  background: white;
  color: #666;
  border: 1px solid #d9d9d9;
}

.btn-default:hover {
  border-color: #999;
  color: #333;
}
</style>