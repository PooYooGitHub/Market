<template>
  <div v-if="visible" class="modal-overlay">
    <div class="modal">
      <div class="payment-header">
        <div class="payment-method-info">
          <div class="method-icon">
            {{ getMethodIcon(paymentMethod) }}
          </div>
          <div class="method-details">
            <div class="method-name">{{ getMethodName(paymentMethod) }}</div>
            <div class="method-desc">安全支付环境</div>
          </div>
        </div>
        <button class="close-btn" @click="handleCancel">×</button>
      </div>

      <div class="payment-body">
        <!-- 支付金额 -->
        <div class="amount-section">
          <div class="amount-label">支付金额</div>
          <div class="amount-value">¥{{ Number(amount).toFixed(2) }}</div>
        </div>

        <!-- 支付方式特有UI -->
        <div class="payment-interface">
          <!-- 支付宝界面 -->
          <div v-if="paymentMethod === 'alipay'" class="alipay-interface">
            <div class="qr-section">
              <div class="qr-placeholder">
                <div class="qr-icon">📱</div>
                <div class="qr-text">请使用支付宝扫码支付</div>
              </div>
              <div class="qr-tips">
                <p>✓ 打开支付宝APP</p>
                <p>✓ 点击扫码功能</p>
                <p>✓ 扫描上方二维码完成支付</p>
              </div>
            </div>
          </div>

          <!-- 微信界面 -->
          <div v-if="paymentMethod === 'wechat'" class="wechat-interface">
            <div class="qr-section">
              <div class="qr-placeholder">
                <div class="qr-icon">📱</div>
                <div class="qr-text">请使用微信扫码支付</div>
              </div>
              <div class="qr-tips">
                <p>✓ 打开微信APP</p>
                <p>✓ 点击扫码功能</p>
                <p>✓ 扫描上方二维码完成支付</p>
              </div>
            </div>
          </div>

          <!-- 银行卡界面 -->
          <div v-if="paymentMethod === 'bank'" class="bank-interface">
            <div class="card-form">
              <div class="form-group">
                <label>银行卡号</label>
                <input
                  v-model="cardNumber"
                  type="text"
                  placeholder="请输入16位银行卡号"
                  maxlength="19"
                  @input="formatCardNumber"
                />
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>有效期</label>
                  <input
                    v-model="expiry"
                    type="text"
                    placeholder="MM/YY"
                    maxlength="5"
                    @input="formatExpiry"
                  />
                </div>
                <div class="form-group">
                  <label>CVV</label>
                  <input
                    v-model="cvv"
                    type="password"
                    placeholder="CVV"
                    maxlength="3"
                  />
                </div>
              </div>
              <div class="form-group">
                <label>持卡人姓名</label>
                <input v-model="cardHolder" type="text" placeholder="请输入持卡人姓名" />
              </div>
            </div>
          </div>
        </div>

        <!-- 支付状态 -->
        <div v-if="processing" class="payment-status">
          <div class="status-icon spinning">⏳</div>
          <div class="status-text">{{ statusText }}</div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progress + '%' }"></div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="payment-actions">
          <button
            v-if="!processing"
            class="btn btn-cancel"
            @click="handleCancel"
          >
            取消支付
          </button>
          <button
            v-if="!processing"
            class="btn btn-pay"
            :disabled="!canPay"
            @click="handlePay"
          >
            确认支付
          </button>
        </div>
      </div>

      <!-- 底部安全提示 -->
      <div class="security-info">
        <div class="security-item">
          <span class="security-icon">🔒</span>
          <span class="security-text">256位SSL安全加密</span>
        </div>
        <div class="security-item">
          <span class="security-icon">🛡️</span>
          <span class="security-text">PCI DSS认证</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  paymentMethod: {
    type: String,
    required: true
  },
  amount: {
    type: [String, Number],
    required: true
  }
})

const emit = defineEmits(['success', 'failed', 'cancel'])

// 状态
const processing = ref(false)
const progress = ref(0)
const statusText = ref('')
const progressTimer = ref(null)
const simulateTimer = ref(null)

// 银行卡表单数据
const cardNumber = ref('')
const expiry = ref('')
const cvv = ref('')
const cardHolder = ref('')

// 获取支付方式图标
const getMethodIcon = (method) => {
  const iconMap = {
    alipay: '💙',
    wechat: '💚',
    bank: '🏦'
  }
  return iconMap[method] || '💳'
}

// 获取支付方式名称
const getMethodName = (method) => {
  const nameMap = {
    alipay: '支付宝支付',
    wechat: '微信支付',
    bank: '银行卡支付'
  }
  return nameMap[method] || '第三方支付'
}

// 是否可以支付
const canPay = computed(() => {
  if (props.paymentMethod === 'bank') {
    return cardNumber.value.length >= 16 &&
           expiry.value.length === 5 &&
           cvv.value.length === 3 &&
           cardHolder.value.trim().length > 0
  }
  return true
})

// 格式化银行卡号
const formatCardNumber = (event) => {
  let value = event.target.value.replace(/\D/g, '')
  value = value.replace(/(\d{4})(?=\d)/g, '$1 ')
  cardNumber.value = value
}

// 格式化有效期
const formatExpiry = (event) => {
  let value = event.target.value.replace(/\D/g, '')
  if (value.length >= 2) {
    value = value.substring(0, 2) + '/' + value.substring(2, 4)
  }
  expiry.value = value
}

// 开始支付处理
const handlePay = () => {
  processing.value = true
  progress.value = 0

  const statusMessages = [
    '正在连接支付服务...',
    '正在验证支付信息...',
    '正在处理支付请求...',
    '支付处理中...',
    '正在确认支付结果...'
  ]

  let messageIndex = 0
  let progressStep = 0

  // 更新状态文本
  const updateStatus = () => {
    if (messageIndex < statusMessages.length) {
      statusText.value = statusMessages[messageIndex]
      messageIndex++
    }
  }

  // 进度条动画
  const updateProgress = () => {
    progress.value = Math.min(progressStep * 20, 100)
    progressStep++

    if (progressStep <= 5) {
      updateStatus()
    }
  }

  // 启动进度更新
  progressTimer.value = setInterval(updateProgress, 800)

  // 模拟支付结果 (80%成功率，用于演示)
  simulateTimer.value = setTimeout(() => {
    clearInterval(progressTimer.value)

    const isSuccess = Math.random() > 0.2 // 80%成功率

    if (isSuccess) {
      const result = {
        transactionId: generateTransactionId(),
        payTime: new Date().toISOString(),
        method: props.paymentMethod
      }
      emit('success', result)
    } else {
      const errors = [
        '支付密码错误',
        '银行卡余额不足',
        '网络连接超时',
        '银行系统维护中'
      ]
      const randomError = errors[Math.floor(Math.random() * errors.length)]
      emit('failed', randomError)
    }
  }, 4500)
}

// 生成模拟交易单号
const generateTransactionId = () => {
  const timestamp = Date.now().toString()
  const random = Math.random().toString(36).substring(2, 8).toUpperCase()
  return `TXN${timestamp}${random}`
}

// 取消支付
const handleCancel = () => {
  if (processing.value) {
    clearInterval(progressTimer.value)
    clearTimeout(simulateTimer.value)
  }
  emit('cancel')
}

// 清理定时器
onUnmounted(() => {
  if (progressTimer.value) {
    clearInterval(progressTimer.value)
  }
  if (simulateTimer.value) {
    clearTimeout(simulateTimer.value)
  }
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
  backdrop-filter: blur(4px);
}

.modal {
  background: white;
  border-radius: 12px;
  width: 480px;
  max-width: 95vw;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.payment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e8e8e8;
}

.payment-method-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.method-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #f0f2f5;
}

.method-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.method-desc {
  font-size: 12px;
  color: #999;
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

.payment-body {
  padding: 24px;
}

.amount-section {
  text-align: center;
  margin-bottom: 30px;
}

.amount-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 8px;
}

.amount-value {
  font-size: 32px;
  font-weight: bold;
  color: #ff4081;
}

/* 支付界面 */
.payment-interface {
  margin-bottom: 30px;
}

.qr-section {
  text-align: center;
}

.qr-placeholder {
  width: 200px;
  height: 200px;
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  margin: 0 auto 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.qr-icon {
  font-size: 48px;
}

.qr-text {
  font-size: 14px;
  color: #666;
}

.qr-tips {
  text-align: left;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.qr-tips p {
  margin: 0 0 8px;
  font-size: 14px;
  color: #666;
}

.qr-tips p:last-child {
  margin-bottom: 0;
}

/* 银行卡表单 */
.card-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-group {
  flex: 1;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 6px;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

/* 支付状态 */
.payment-status {
  text-align: center;
  margin-bottom: 30px;
}

.status-icon {
  font-size: 32px;
  margin-bottom: 16px;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.status-text {
  font-size: 16px;
  color: #666;
  margin-bottom: 20px;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 3px;
  transition: width 0.3s ease;
}

/* 操作按钮 */
.payment-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn {
  padding: 12px 32px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 120px;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-pay {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-pay:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-pay:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* 安全信息 */
.security-info {
  display: flex;
  justify-content: center;
  gap: 32px;
  padding: 16px 24px;
  background: #f8f9fa;
  border-top: 1px solid #e8e8e8;
}

.security-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #999;
}

.security-icon {
  font-size: 14px;
}
</style>