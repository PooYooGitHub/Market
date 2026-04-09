<template>
  <div v-if="visible" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <!-- 成功状态 -->
      <div v-if="result.success" class="result-success">
        <div class="success-icon">✅</div>
        <h3 class="result-title">支付成功</h3>
        <div class="result-message">{{ result.message }}</div>

        <div class="payment-details">
          <div class="detail-row">
            <span class="detail-label">支付方式：</span>
            <span class="detail-value">{{ getPaymentMethodName(result.method) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">支付金额：</span>
            <span class="detail-value amount">¥{{ Number(result.amount).toFixed(2) }}</span>
          </div>
          <div v-if="result.transactionId" class="detail-row">
            <span class="detail-label">交易单号：</span>
            <span class="detail-value">{{ result.transactionId }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">支付时间：</span>
            <span class="detail-value">{{ formatTime(result.payTime || new Date()) }}</span>
          </div>
        </div>

        <div class="order-summary">
          <div class="order-title">订单信息</div>
          <div class="order-item">
            <img
              :src="orderInfo.productImage || defaultImage"
              :alt="orderInfo.productTitle"
              class="order-img"
            />
            <div class="order-info">
              <div class="order-product-title">{{ orderInfo.productTitle }}</div>
              <div class="order-no">订单号：{{ orderInfo.orderNo }}</div>
            </div>
          </div>
        </div>

        <div class="success-tips">
          <div class="tips-title">💡 温馨提示</div>
          <ul class="tips-list">
            <li>支付成功后，我们会立即通知卖家</li>
            <li>卖家确认发货后，您将收到发货通知</li>
            <li>收到商品后请及时确认收货</li>
          </ul>
        </div>

        <div class="modal-actions">
          <button class="btn btn-secondary" @click="$emit('close')">完成</button>
          <button class="btn btn-primary" @click="$emit('view-order')">查看订单</button>
        </div>
      </div>

      <!-- 失败状态 -->
      <div v-else class="result-failure">
        <div class="failure-icon">❌</div>
        <h3 class="result-title">支付失败</h3>
        <div class="result-message">{{ result.message }}</div>

        <div class="failure-tips">
          <div class="tips-title">💡 可能的原因</div>
          <ul class="tips-list">
            <li>余额不足</li>
            <li>网络连接异常</li>
            <li>支付密码错误</li>
            <li>银行卡状态异常</li>
          </ul>
        </div>

        <div class="modal-actions">
          <button class="btn btn-secondary" @click="$emit('close')">取消</button>
          <button class="btn btn-primary" @click="$emit('close')">重新支付</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  result: {
    type: Object,
    default: () => ({})
  },
  orderInfo: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['close', 'view-order'])

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="80" height="80"%3E%3Crect fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999"%3E商品%3C/text%3E%3C/svg%3E'

// 获取支付方式名称
const getPaymentMethodName = (method) => {
  const methodMap = {
    alipay: '支付宝',
    wechat: '微信支付',
    balance: '余额支付',
    bank: '银行卡支付'
  }
  return methodMap[method] || '未知支付方式'
}

// 格式化时间
const formatTime = (time) => {
  const date = new Date(time)
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
  padding: 40px;
  width: 520px;
  max-width: 95vw;
  max-height: 90vh;
  overflow-y: auto;
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

/* 成功状态样式 */
.result-success {
  text-align: center;
}

.success-icon {
  font-size: 64px;
  margin-bottom: 20px;
  animation: successPulse 0.6s ease-out;
}

@keyframes successPulse {
  0% { transform: scale(0.8); opacity: 0.8; }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}

.result-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 16px;
}

.result-message {
  font-size: 16px;
  color: #52c41a;
  margin-bottom: 30px;
}

.payment-details {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 30px;
  text-align: left;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-size: 14px;
  color: #666;
}

.detail-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.detail-value.amount {
  color: #ff4081;
  font-size: 18px;
  font-weight: bold;
}

.order-summary {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 30px;
  text-align: left;
}

.order-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
}

.order-item {
  display: flex;
  gap: 16px;
  align-items: center;
}

.order-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.order-info {
  flex: 1;
}

.order-product-title {
  font-size: 16px;
  color: #333;
  font-weight: 500;
  margin-bottom: 8px;
  line-height: 1.4;
}

.order-no {
  font-size: 14px;
  color: #999;
}

/* 失败状态样式 */
.result-failure {
  text-align: center;
}

.failure-icon {
  font-size: 64px;
  margin-bottom: 20px;
  animation: failureShake 0.6s ease-out;
}

@keyframes failureShake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}

.result-failure .result-title {
  color: #ff4d4f;
}

.result-failure .result-message {
  color: #ff4d4f;
}

/* 提示区域 */
.success-tips,
.failure-tips {
  background: #f0f7ff;
  border: 1px solid #d6e9ff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 30px;
  text-align: left;
}

.failure-tips {
  background: #fff2f0;
  border-color: #ffccc7;
}

.tips-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.tips-list li {
  margin-bottom: 4px;
}

/* 操作按钮 */
.modal-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn {
  padding: 12px 32px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 120px;
}

.btn-secondary {
  background: #f5f5f5;
  color: #666;
}

.btn-secondary:hover {
  background: #e0e0e0;
  color: #333;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}
</style>