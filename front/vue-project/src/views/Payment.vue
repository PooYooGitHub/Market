<template>
  <div class="page-container">
    <main class="main-content">
      <div class="payment-container">
        <h2 class="page-title">💳 订单支付</h2>

        <div v-if="loading" class="loading">加载中...</div>

        <div v-else class="payment-content">
          <!-- 订单信息 -->
          <div class="order-section">
            <h3 class="section-title">📦 订单信息</h3>
            <div class="order-card">
              <div class="order-header">
                <span class="order-no">订单号：{{ orderInfo.orderNo }}</span>
                <span class="order-status">待支付</span>
              </div>

              <div class="order-product">
                <img
                  :src="orderInfo.productImage || defaultImage"
                  :alt="orderInfo.productTitle"
                  class="product-img"
                  @error="e => e.target.src = defaultImage"
                />
                <div class="product-info">
                  <div class="product-title">{{ orderInfo.productTitle }}</div>
                  <div class="product-desc">{{ orderInfo.productDescription || '无描述' }}</div>
                  <div class="seller-info">
                    <span class="seller-label">卖家：</span>
                    <img
                      :src="orderInfo.sellerAvatar || defaultAvatar"
                      :alt="orderInfo.sellerNickname"
                      class="seller-avatar"
                      @error="e => e.target.src = defaultAvatar"
                    />
                    <span class="seller-name">{{ orderInfo.sellerNickname }}</span>
                  </div>
                </div>
                <div class="product-price">
                  <div class="price-label">商品价格</div>
                  <div class="price-amount">¥{{ Number(orderInfo.totalAmount).toFixed(2) }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 支付方式选择 -->
          <div class="payment-section">
            <h3 class="section-title">💰 选择支付方式</h3>

            <div class="payment-methods">
              <!-- 支付宝 -->
              <div
                :class="['payment-method', { active: selectedMethod === 'alipay' }]"
                @click="selectPaymentMethod('alipay')"
              >
                <div class="method-icon alipay-icon">💙</div>
                <div class="method-info">
                  <div class="method-name">支付宝</div>
                  <div class="method-desc">安全快捷，信任之选</div>
                </div>
                <div class="method-radio">
                  <input type="radio" :checked="selectedMethod === 'alipay'" />
                </div>
              </div>

              <!-- 微信支付 -->
              <div
                :class="['payment-method', { active: selectedMethod === 'wechat' }]"
                @click="selectPaymentMethod('wechat')"
              >
                <div class="method-icon wechat-icon">💚</div>
                <div class="method-info">
                  <div class="method-name">微信支付</div>
                  <div class="method-desc">微信安全支付</div>
                </div>
                <div class="method-radio">
                  <input type="radio" :checked="selectedMethod === 'wechat'" />
                </div>
              </div>

              <!-- 余额支付 -->
              <div
                :class="['payment-method', { active: selectedMethod === 'balance' }]"
                @click="selectPaymentMethod('balance')"
              >
                <div class="method-icon balance-icon">🪙</div>
                <div class="method-info">
                  <div class="method-name">余额支付</div>
                  <div class="method-desc">
                    余额：¥{{ Number(userBalance).toFixed(2) }}
                    <span v-if="Number(userBalance) < Number(orderInfo.totalAmount)" class="balance-insufficient">
                      (余额不足)
                    </span>
                  </div>
                </div>
                <div class="method-radio">
                  <input
                    type="radio"
                    :checked="selectedMethod === 'balance'"
                    :disabled="Number(userBalance) < Number(orderInfo.totalAmount)"
                  />
                </div>
              </div>

              <!-- 银行卡支付 -->
              <div
                :class="['payment-method', { active: selectedMethod === 'bank' }]"
                @click="selectPaymentMethod('bank')"
              >
                <div class="method-icon bank-icon">🏦</div>
                <div class="method-info">
                  <div class="method-name">银行卡支付</div>
                  <div class="method-desc">支持各大银行储蓄卡及信用卡</div>
                </div>
                <div class="method-radio">
                  <input type="radio" :checked="selectedMethod === 'bank'" />
                </div>
              </div>
            </div>

            <!-- 余额支付密码输入 -->
            <div v-if="selectedMethod === 'balance' && Number(userBalance) >= Number(orderInfo.totalAmount)" class="balance-password">
              <div class="password-label">请输入支付密码（演示用，任意6位数字）</div>
              <input
                v-model="paymentPassword"
                type="password"
                class="password-input"
                placeholder="请输入6位支付密码"
                maxlength="6"
              />
            </div>
          </div>

          <!-- 支付信息确认 -->
          <div class="confirm-section">
            <div class="amount-info">
              <div class="amount-item">
                <span class="amount-label">商品金额：</span>
                <span class="amount-value">¥{{ Number(orderInfo.totalAmount).toFixed(2) }}</span>
              </div>
              <div class="amount-item">
                <span class="amount-label">运费：</span>
                <span class="amount-value">¥0.00</span>
              </div>
              <div class="amount-item total">
                <span class="amount-label">应付金额：</span>
                <span class="amount-value">¥{{ Number(orderInfo.totalAmount).toFixed(2) }}</span>
              </div>
            </div>

            <div class="payment-actions">
              <button
                class="btn btn-cancel"
                @click="router.back()"
              >
                取消支付
              </button>
              <button
                class="btn btn-pay"
                :disabled="!selectedMethod || paying"
                @click="handlePay"
              >
                {{ paying ? '支付中...' : `立即支付 ¥${Number(orderInfo.totalAmount).toFixed(2)}` }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 支付结果弹窗 -->
    <PaymentResultModal
      v-if="showResultModal"
      :visible="showResultModal"
      :result="paymentResult"
      :order-info="orderInfo"
      @close="handleResultClose"
      @view-order="goToOrder"
    />

    <!-- 支付中弹窗（模拟第三方支付页面） -->
    <PaymentProcessModal
      v-if="showProcessModal"
      :visible="showProcessModal"
      :payment-method="selectedMethod"
      :amount="orderInfo.totalAmount"
      @success="handlePaymentSuccess"
      @failed="handlePaymentFailed"
      @cancel="handlePaymentCancel"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getOrderDetail } from '@/api/trade'
import { createPayment, getUserBalance, payWithBalance, simulatePaymentResult } from '@/api/payment'
import PaymentResultModal from '@/components/PaymentResultModal.vue'
import PaymentProcessModal from '@/components/PaymentProcessModal.vue'

const router = useRouter()
const route = useRoute()

const orderId = computed(() => route.params.id)

// 状态
const loading = ref(true)
const paying = ref(false)
const orderInfo = ref({})
const userBalance = ref(0)
const selectedMethod = ref('')
const paymentPassword = ref('')
const showResultModal = ref(false)
const showProcessModal = ref(false)
const paymentResult = ref(null)

// 默认图片
const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="120" height="120"%3E%3Crect fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999"%3E商品图片%3C/text%3E%3C/svg%3E'
const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="32" height="32"%3E%3Ccircle cx="16" cy="16" r="16" fill="%23ddd"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-size="10"%3E头像%3C/text%3E%3C/svg%3E'

// 加载订单信息
const loadOrderInfo = async () => {
  try {
    loading.value = true
    const [orderRes, balanceRes] = await Promise.all([
      getOrderDetail(orderId.value),
      getUserBalance()
    ])

    orderInfo.value = orderRes.data || {}
    userBalance.value = balanceRes.data?.balance || 0

    // 检查订单状态
    if (orderInfo.value.status !== 0) {
      alert('该订单不是待支付状态')
      router.back()
      return
    }

  } catch (error) {
    console.error('加载订单信息失败:', error)
    alert(error.message || '加载订单信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 选择支付方式
const selectPaymentMethod = (method) => {
  // 检查余额支付是否可用
  if (method === 'balance' && Number(userBalance.value) < Number(orderInfo.value.totalAmount)) {
    alert('余额不足，请选择其他支付方式')
    return
  }

  selectedMethod.value = method
  paymentPassword.value = ''
}

// 处理支付
const handlePay = async () => {
  if (!selectedMethod.value) {
    alert('请选择支付方式')
    return
  }

  // 余额支付验证
  if (selectedMethod.value === 'balance') {
    if (!paymentPassword.value || paymentPassword.value.length !== 6) {
      alert('请输入6位支付密码')
      return
    }

    if (!/^\d{6}$/.test(paymentPassword.value)) {
      alert('支付密码必须是6位数字')
      return
    }
  }

  paying.value = true

  try {
    if (selectedMethod.value === 'balance') {
      // 余额支付直接调用接口
      await payWithBalance({
        orderId: orderId.value,
        amount: orderInfo.value.totalAmount,
        password: paymentPassword.value
      })

      // 余额支付成功
      paymentResult.value = {
        success: true,
        method: 'balance',
        amount: orderInfo.value.totalAmount,
        message: '余额支付成功！'
      }
      showResultModal.value = true

    } else {
      // 第三方支付，显示模拟支付界面
      const paymentRes = await createPayment({
        orderId: orderId.value,
        paymentMethod: selectedMethod.value,
        amount: orderInfo.value.totalAmount
      })

      // 显示支付处理弹窗
      showProcessModal.value = true
    }

  } catch (error) {
    console.error('支付失败:', error)
    alert(error.message || '支付失败')
  } finally {
    paying.value = false
  }
}

// 支付成功处理
const handlePaymentSuccess = (result) => {
  showProcessModal.value = false
  paymentResult.value = {
    success: true,
    method: selectedMethod.value,
    amount: orderInfo.value.totalAmount,
    message: '支付成功！',
    ...result
  }
  showResultModal.value = true
}

// 支付失败处理
const handlePaymentFailed = (error) => {
  showProcessModal.value = false
  paymentResult.value = {
    success: false,
    method: selectedMethod.value,
    message: error || '支付失败，请重试'
  }
  showResultModal.value = true
}

// 支付取消处理
const handlePaymentCancel = () => {
  showProcessModal.value = false
  paying.value = false
}

// 结果弹窗关闭处理
const handleResultClose = () => {
  showResultModal.value = false
  if (paymentResult.value?.success) {
    router.replace('/orders')
  }
}

// 查看订单
const goToOrder = () => {
  router.replace(`/order/${orderId.value}`)
}

onMounted(() => {
  if (!orderId.value) {
    alert('订单ID不能为空')
    router.back()
    return
  }

  loadOrderInfo()
})
</script>

<style scoped>
.main-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
}

.payment-container {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  text-align: center;
  padding: 30px 0 10px;
  margin: 0;
}

.loading {
  text-align: center;
  padding: 80px 20px;
  color: #999;
  font-size: 16px;
}

.payment-content {
  padding: 0 30px 30px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 30px 0 20px;
  display: flex;
  align-items: center;
}

/* 订单信息区域 */
.order-section .section-title {
  margin-top: 0;
}

.order-card {
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.order-no {
  font-size: 14px;
  color: #666;
}

.order-status {
  background: #fff3e0;
  color: #ff9800;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.order-product {
  display: flex;
  padding: 20px;
  gap: 20px;
}

.product-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  line-height: 1.4;
}

.product-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: auto;
}

.seller-label {
  font-size: 14px;
  color: #999;
}

.seller-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.seller-name {
  font-size: 14px;
  color: #333;
}

.product-price {
  text-align: right;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.price-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 8px;
}

.price-amount {
  font-size: 24px;
  font-weight: bold;
  color: #ff4081;
}

/* 支付方式区域 */
.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-method {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.payment-method:hover {
  border-color: #d0d0d0;
}

.payment-method.active {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.method-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.alipay-icon {
  background: #e6f7ff;
}

.wechat-icon {
  background: #f6ffed;
}

.balance-icon {
  background: #fff7e6;
}

.bank-icon {
  background: #f0f2f5;
}

.method-info {
  flex: 1;
}

.method-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.method-desc {
  font-size: 14px;
  color: #666;
}

.balance-insufficient {
  color: #ff4d4f;
  font-weight: bold;
}

.method-radio input {
  width: 20px;
  height: 20px;
  accent-color: #667eea;
}

.balance-password {
  margin-top: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.password-label {
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;
}

.password-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 16px;
  letter-spacing: 4px;
  text-align: center;
}

.password-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

/* 确认支付区域 */
.confirm-section {
  background: #f8f9fa;
  padding: 30px;
  margin: 30px -30px -30px;
  border-top: 1px solid #e8e8e8;
}

.amount-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 30px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
}

.amount-item.total {
  padding-top: 12px;
  border-top: 1px solid #e8e8e8;
  font-size: 18px;
  font-weight: bold;
}

.amount-label {
  color: #666;
}

.amount-value {
  color: #333;
  font-weight: bold;
}

.amount-item.total .amount-value {
  color: #ff4081;
  font-size: 24px;
}

.payment-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.btn {
  padding: 16px 40px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 140px;
}

.btn-cancel {
  background: white;
  color: #666;
  border: 1px solid #d9d9d9;
}

.btn-cancel:hover {
  border-color: #999;
  color: #333;
}

.btn-pay {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-pay:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.btn-pay:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}
</style>