<template>
  <div class="page-container">
    <main class="main-content">
      <div class="payment-container">
        <h2 class="page-title">💳 支付记录</h2>

        <!-- 筛选器 -->
        <div class="filters">
          <div class="filter-group">
            <label>支付状态：</label>
            <select v-model="filters.status" @change="loadPayments">
              <option value="">全部状态</option>
              <option value="pending">支付中</option>
              <option value="success">支付成功</option>
              <option value="failed">支付失败</option>
              <option value="cancelled">已取消</option>
            </select>
          </div>
          <div class="filter-group">
            <label>支付方式：</label>
            <select v-model="filters.paymentMethod" @change="loadPayments">
              <option value="">全部方式</option>
              <option value="alipay">支付宝</option>
              <option value="wechat">微信支付</option>
              <option value="balance">余额支付</option>
              <option value="bank">银行卡支付</option>
            </select>
          </div>
          <div class="filter-group">
            <button class="btn btn-refresh" @click="loadPayments">
              🔄 刷新
            </button>
          </div>
        </div>

        <div v-if="loading" class="loading">加载中...</div>

        <div v-else-if="paymentList.length === 0" class="empty-payments">
          <div class="empty-icon">💳</div>
          <p>暂无支付记录</p>
          <button class="btn btn-primary" @click="router.push('/products')">去购物</button>
        </div>

        <div v-else class="payment-content">
          <!-- 统计信息 -->
          <div class="payment-stats">
            <div class="stat-card">
              <div class="stat-icon">💰</div>
              <div class="stat-info">
                <div class="stat-label">总支付金额</div>
                <div class="stat-value">¥{{ totalAmount }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">✅</div>
              <div class="stat-info">
                <div class="stat-label">成功支付</div>
                <div class="stat-value">{{ successCount }}笔</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">📊</div>
              <div class="stat-info">
                <div class="stat-label">本月支付</div>
                <div class="stat-value">{{ monthCount }}笔</div>
              </div>
            </div>
          </div>

          <!-- 支付记录列表 -->
          <div class="payment-list">
            <div
              v-for="payment in paymentList"
              :key="payment.id"
              class="payment-card"
              @click="showDetail(payment)"
            >
              <div class="payment-header">
                <div class="payment-method">
                  <span class="method-icon">{{ getMethodIcon(payment.paymentMethod) }}</span>
                  <span class="method-name">{{ getMethodName(payment.paymentMethod) }}</span>
                </div>
                <div :class="['payment-status', `status-${payment.status}`]">
                  {{ getStatusText(payment.status) }}
                </div>
              </div>

              <div class="payment-body">
                <div class="payment-info">
                  <div class="payment-order">
                    <img
                      :src="payment.productImage || defaultImage"
                      :alt="payment.productTitle"
                      class="order-img"
                      @error="e => e.target.src = defaultImage"
                    />
                    <div class="order-info">
                      <div class="order-title">{{ payment.productTitle }}</div>
                      <div class="order-no">订单号：{{ payment.orderNo }}</div>
                    </div>
                  </div>

                  <div class="payment-amount">
                    <div class="amount-label">支付金额</div>
                    <div class="amount-value">¥{{ Number(payment.amount).toFixed(2) }}</div>
                  </div>
                </div>

                <div class="payment-details">
                  <div class="detail-item">
                    <span class="detail-label">交易单号：</span>
                    <span class="detail-value">{{ payment.transactionId || '-' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">创建时间：</span>
                    <span class="detail-value">{{ formatTime(payment.createTime) }}</span>
                  </div>
                  <div v-if="payment.payTime" class="detail-item">
                    <span class="detail-label">支付时间：</span>
                    <span class="detail-value">{{ formatTime(payment.payTime) }}</span>
                  </div>
                  <div v-if="payment.failReason" class="detail-item">
                    <span class="detail-label">失败原因：</span>
                    <span class="detail-value error">{{ payment.failReason }}</span>
                  </div>
                </div>
              </div>

              <div class="payment-actions">
                <button
                  v-if="payment.status === 'failed' && payment.orderId"
                  class="btn btn-small btn-primary"
                  @click.stop="retryPayment(payment)"
                >
                  重新支付
                </button>
                <button
                  v-if="payment.status === 'success' && canRefund(payment)"
                  class="btn btn-small btn-secondary"
                  @click.stop="requestRefund(payment)"
                >
                  申请退款
                </button>
                <button
                  class="btn btn-small btn-default"
                  @click.stop="showDetail(payment)"
                >
                  查看详情
                </button>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="total > pageSize" class="pagination">
            <button
              class="page-btn"
              :disabled="pageNum === 1"
              @click="changePage(pageNum - 1)"
            >
              上一页
            </button>
            <span class="page-info">{{ pageNum }} / {{ totalPages }}</span>
            <button
              class="page-btn"
              :disabled="pageNum >= totalPages"
              @click="changePage(pageNum + 1)"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </main>

    <!-- 支付详情弹窗 -->
    <PaymentDetailModal
      v-if="showDetailModal"
      :visible="showDetailModal"
      :payment="selectedPayment"
      @close="showDetailModal = false"
      @retry="retryPayment"
      @refund="requestRefund"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPaymentHistory, requestRefund as apiRequestRefund } from '@/api/payment'
import PaymentDetailModal from '@/components/PaymentDetailModal.vue'

const router = useRouter()

// 状态
const loading = ref(true)
const paymentList = ref([])
const showDetailModal = ref(false)
const selectedPayment = ref(null)

// 分页
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选器
const filters = ref({
  status: '',
  paymentMethod: ''
})

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="80" height="80"%3E%3Crect fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999"%3E商品%3C/text%3E%3C/svg%3E'

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const successPayments = computed(() =>
  paymentList.value.filter(p => p.status === 'success')
)

const totalAmount = computed(() =>
  successPayments.value.reduce((sum, p) => sum + Number(p.amount), 0).toFixed(2)
)

const successCount = computed(() => successPayments.value.length)

const monthCount = computed(() => {
  const currentMonth = new Date().getMonth()
  const currentYear = new Date().getFullYear()

  return paymentList.value.filter(p => {
    const payTime = new Date(p.createTime)
    return payTime.getMonth() === currentMonth && payTime.getFullYear() === currentYear
  }).length
})

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

  // 24小时内可以申请退款（毕业设计简化逻辑）
  const payTime = new Date(payment.payTime)
  const now = new Date()
  const diffHours = (now - payTime) / (1000 * 60 * 60)

  return diffHours <= 24
}

// 加载支付记录
const loadPayments = async () => {
  loading.value = true
  try {
    const params = {
      ...filters.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }

    const res = await getPaymentHistory(params)
    paymentList.value = res.data.records || []
    total.value = res.data.total || 0

  } catch (error) {
    console.error('加载支付记录失败:', error)
    alert(error.message || '加载支付记录失败')
  } finally {
    loading.value = false
  }
}

// 显示详情
const showDetail = (payment) => {
  selectedPayment.value = payment
  showDetailModal.value = true
}

// 重新支付
const retryPayment = (payment) => {
  if (!payment.orderId) {
    alert('订单信息不完整，无法重新支付')
    return
  }

  router.push(`/payment/${payment.orderId}`)
}

// 申请退款
const requestRefund = async (payment) => {
  const reason = prompt('请输入退款理由：')
  if (!reason || !reason.trim()) {
    return
  }

  try {
    await apiRequestRefund({
      orderId: payment.orderId,
      reason: reason.trim()
    })

    alert('退款申请已提交，我们会在1-3个工作日内处理')
    loadPayments()

  } catch (error) {
    alert(error.message || '申请退款失败')
  }
}

// 切换页码
const changePage = (page) => {
  pageNum.value = page
  loadPayments()
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
    minute: '2-digit'
  })
}

onMounted(() => {
  loadPayments()
})
</script>

<style scoped>
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.payment-container {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 30px;
  text-align: center;
}

/* 筛选器 */
.filters {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.filter-group select {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
}

.btn-refresh {
  padding: 8px 16px;
  border: none;
  background: #667eea;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-refresh:hover {
  background: #5a6fd8;
}

.loading {
  text-align: center;
  padding: 60px;
  color: #999;
  font-size: 16px;
}

.empty-payments {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-payments p {
  color: #999;
  font-size: 18px;
  margin-bottom: 30px;
}

/* 统计信息 */
.payment-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.stat-icon {
  font-size: 32px;
  opacity: 0.8;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
}

/* 支付记录列表 */
.payment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.payment-card {
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
}

.payment-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.payment-method {
  display: flex;
  align-items: center;
  gap: 8px;
}

.method-icon {
  font-size: 18px;
}

.method-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
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

.payment-body {
  padding: 20px;
}

.payment-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.payment-order {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.order-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}

.order-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.order-no {
  font-size: 12px;
  color: #999;
}

.payment-amount {
  text-align: right;
}

.amount-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.amount-value {
  font-size: 20px;
  font-weight: bold;
  color: #ff4081;
}

.payment-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
}

.detail-label {
  color: #999;
}

.detail-value {
  color: #333;
  font-weight: 500;
}

.detail-value.error {
  color: #f44336;
}

.payment-actions {
  padding: 16px 20px;
  background: #fafafa;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-small {
  padding: 6px 12px;
  font-size: 12px;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5a6fd8;
}

.btn-secondary {
  background: #ff9800;
  color: white;
}

.btn-secondary:hover {
  background: #f57c00;
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

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
}

.page-btn {
  padding: 8px 20px;
  border: 1px solid #d9d9d9;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  border-color: #667eea;
  color: #667eea;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}
</style>