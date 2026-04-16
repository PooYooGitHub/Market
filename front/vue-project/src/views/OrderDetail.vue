<template>
  <div class="page-container">
    <div class="order-detail-container">
      <button class="back-btn" @click="router.back()">← 返回</button>

      <div v-if="loading" class="loading">加载中...</div>

      <div v-else-if="!order" class="empty">
        <p>订单不存在或无权限访问</p>
        <button class="btn btn-primary" @click="router.push('/orders')">返回订单列表</button>
      </div>

      <div v-else class="order-detail">
        <section class="status-section">
          <div :class="['status-icon', `status-${order.status}`]">{{ getStatusIcon(order.status) }}</div>
          <div class="status-info">
            <div :class="['status-text', `status-${order.status}`]">{{ order.statusDesc }}</div>
            <div class="status-tip">{{ getStatusTip(order.status) }}</div>
          </div>
        </section>

        <section class="card-section">
          <h3 class="section-title">订单信息</h3>
          <div class="info-grid">
            <div class="info-item"><span class="k">订单号</span><span class="v">{{ order.orderNo }}</span></div>
            <div class="info-item"><span class="k">下单时间</span><span class="v">{{ formatTime(order.createTime) }}</span></div>
            <div class="info-item" v-if="order.payTime"><span class="k">支付时间</span><span class="v">{{ formatTime(order.payTime) }}</span></div>
            <div class="info-item"><span class="k">订单状态</span><span class="v">{{ order.statusDesc }}</span></div>
          </div>
        </section>

        <section class="card-section">
          <h3 class="section-title">商品信息</h3>
          <div class="product-card" @click="router.push(`/product/${order.productId}`)">
            <img
              :src="order.productImage || defaultImage"
              :alt="order.productTitle"
              class="product-img"
              @error="e => (e.target.src = defaultImage)"
            />
            <div class="product-info">
              <div class="product-title">{{ order.productTitle }}</div>
              <div class="product-price">¥{{ Number(order.productPrice || 0).toFixed(2) }}</div>
            </div>
          </div>
        </section>

        <section class="card-section">
          <h3 class="section-title">交易双方</h3>
          <div class="parties-grid">
            <div class="party-card" @click="viewUserProfile(order.buyerId)">
              <div class="party-label">买家</div>
              <div class="party-main">
                <img :src="order.buyerAvatar || defaultAvatar" class="party-avatar" @error="e => (e.target.src = defaultAvatar)" />
                <span>{{ order.buyerNickname || `用户${order.buyerId}` }}</span>
              </div>
            </div>
            <div class="party-card" @click="viewUserProfile(order.sellerId)">
              <div class="party-label">卖家</div>
              <div class="party-main">
                <img :src="order.sellerAvatar || defaultAvatar" class="party-avatar" @error="e => (e.target.src = defaultAvatar)" />
                <span>{{ order.sellerNickname || `用户${order.sellerId}` }}</span>
              </div>
            </div>
          </div>
        </section>

        <section class="card-section">
          <h3 class="section-title">金额信息</h3>
          <div class="amount-list">
            <div class="amount-item"><span>商品价格</span><strong>¥{{ Number(order.productPrice || 0).toFixed(2) }}</strong></div>
            <div class="amount-item total"><span>订单总额</span><strong>¥{{ Number(order.totalAmount || 0).toFixed(2) }}</strong></div>
          </div>
        </section>

        <section class="actions-section">
          <button v-if="order.status === 0 && isBuyer" class="btn btn-primary" @click="handlePay">去支付</button>
          <button v-if="order.status === 0" class="btn btn-default" @click="handleCancel">取消订单</button>
          <button v-if="order.status === 1 && isSeller" class="btn btn-primary" @click="handleShip">确认发货</button>
          <button v-if="order.status === 2 && isBuyer" class="btn btn-primary" @click="handleReceive">确认收货</button>

          <button v-if="canShowDisputeAction" class="btn btn-warning" @click="goDisputeFlow">
            ⚖️ {{ disputeActionText }}
          </button>

          <button v-if="order.status >= 1 && order.status <= 2" class="btn btn-info" @click="contactOther">
            联系{{ isBuyer ? '卖家' : '买家' }}
          </button>

          <button v-if="order.status === 3 && !hasEvaluated" class="btn btn-success" @click="openEvaluationDialog">
            评价{{ isBuyer ? '卖家' : '买家' }}
          </button>
          <div v-if="order.status === 3 && hasEvaluated" class="evaluation-status">已评价</div>
        </section>
      </div>
    </div>

    <EvaluationForm
      v-model:visible="showEvaluationDialog"
      :order-info="order"
      :target-user="targetUser"
      :evaluator-id="userStore.userInfo?.id"
      @success="handleEvaluationSuccess"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, cancelOrder, payOrder, shipOrder, receiveOrder } from '@/api/trade'
import { checkEvaluationStatus } from '@/api/credit'
import { arbitrationApi } from '@/api/arbitration'
import { useUserStore } from '@/stores/user'
import EvaluationForm from '@/components/EvaluationForm.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const order = ref(null)
const loading = ref(false)
const showEvaluationDialog = ref(false)
const hasEvaluated = ref(false)
const targetUser = ref({})
const currentDispute = ref(null)

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100"%3E%3Crect fill="%23ddd"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999"%3E暂无图片%3C/text%3E%3C/svg%3E'
const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="40" height="40"%3E%3Ccircle cx="20" cy="20" r="20" fill="%23ddd"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-size="12"%3E头像%3C/text%3E%3C/svg%3E'

const isBuyer = computed(() => order.value && userStore.userInfo && order.value.buyerId === userStore.userInfo.id)
const isSeller = computed(() => order.value && userStore.userInfo && order.value.sellerId === userStore.userInfo.id)

const canShowDisputeAction = computed(() => {
  if (!order.value) return false
  if (!isBuyer.value) return false
  return order.value.status === 1 || order.value.status === 2 || order.value.status === 5
})

const disputeActionText = computed(() => {
  if (!currentDispute.value) return '发起争议协商'
  if (currentDispute.value.status === 'ESCALATED_TO_ARBITRATION') return '查看争议与仲裁'
  return '查看争议协商'
})

const getStatusIcon = (status) => {
  const icons = { 0: '⏳', 1: '📦', 2: '🚚', 3: '✅', 4: '❌', 5: '🔧' }
  return icons[status] || '📄'
}

const getStatusTip = (status) => {
  const tips = {
    0: '请尽快完成支付',
    1: '等待卖家发货',
    2: '商品运输中，请注意查收',
    3: '交易已完成',
    4: '订单已取消',
    5: '售后处理中'
  }
  return tips[status] || ''
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  if (Number.isNaN(date.getTime())) return String(timeStr)
  return date.toLocaleString('zh-CN')
}

const loadOrderDispute = async (orderId) => {
  currentDispute.value = null
  try {
    const res = await arbitrationApi.getMyDisputeByOrderId(orderId)
    currentDispute.value = res?.data || null
  } catch (error) {
    console.warn('加载订单争议状态失败:', error)
  }
}

const loadOrderDetail = async () => {
  loading.value = true
  try {
    const orderId = Number(route.params.id)
    const res = await getOrderDetail(orderId)
    order.value = res.data
    await loadOrderDispute(orderId)

    if (order.value?.status === 3) {
      await checkOrderEvaluationStatus()
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    alert(error.message || '加载订单详情失败')
    order.value = null
  } finally {
    loading.value = false
  }
}

const checkOrderEvaluationStatus = async () => {
  try {
    const res = await checkEvaluationStatus(order.value.id)
    hasEvaluated.value = Boolean(res?.data?.hasEvaluated)
  } catch (error) {
    console.error('检查评价状态失败:', error)
    hasEvaluated.value = false
  }
}

const handlePay = async () => {
  if (!confirm('确认支付该订单？')) return
  try {
    await payOrder(order.value.id)
    alert('支付成功')
    await loadOrderDetail()
  } catch (error) {
    alert(error.message || '支付失败')
  }
}

const handleCancel = async () => {
  if (!confirm('确认取消该订单？')) return
  try {
    await cancelOrder(order.value.id)
    alert('订单已取消')
    await loadOrderDetail()
  } catch (error) {
    alert(error.message || '取消订单失败')
  }
}

const handleShip = async () => {
  if (!confirm('确认已发货？')) return
  try {
    await shipOrder(order.value.id)
    alert('发货成功')
    await loadOrderDetail()
  } catch (error) {
    alert(error.message || '发货失败')
  }
}

const handleReceive = async () => {
  if (!confirm('确认收货？')) return
  try {
    await receiveOrder(order.value.id)
    alert('确认收货成功')
    await loadOrderDetail()
  } catch (error) {
    alert(error.message || '确认收货失败')
  }
}

const goDisputeFlow = async () => {
  if (!order.value) return

  const actionText = disputeActionText.value || '发起争议协商'
  if (!confirm(`确认要${actionText}吗？\n\n请先准备好事实说明与诉求说明。`)) return

  try {
    const orderId = Number(order.value.id)
    const orderNo = order.value.orderNo || `ORDER${String(orderId).padStart(8, '0')}`
    await loadOrderDispute(orderId)

    if (currentDispute.value?.id) {
      router.push(`/dispute/detail/${currentDispute.value.id}`)
      return
    }

    router.push({
      path: '/dispute/apply',
      query: { orderNo }
    })
  } catch (error) {
    console.error('检查争议状态失败:', error)
    alert(error?.message || '检查争议状态失败，请稍后重试')
  }
}

const contactOther = () => {
  if (!order.value) return

  let targetUserId
  let targetNickname
  let targetAvatar

  if (isBuyer.value) {
    targetUserId = order.value.sellerId || order.value.seller?.id
    targetNickname = order.value.sellerNickname || order.value.seller?.nickname || order.value.seller?.username
    targetAvatar = order.value.sellerAvatar || order.value.seller?.avatar
  } else if (isSeller.value) {
    targetUserId = order.value.buyerId || order.value.buyer?.id
    targetNickname = order.value.buyerNickname || order.value.buyer?.nickname || order.value.buyer?.username
    targetAvatar = order.value.buyerAvatar || order.value.buyer?.avatar
  }

  if (!targetUserId) {
    alert('无法获取联系对象')
    return
  }

  router.push({
    path: '/messages',
    query: {
      userId: targetUserId,
      username: targetNickname || `用户${targetUserId}`,
      nickname: targetNickname || `用户${targetUserId}`,
      avatar: targetAvatar || '',
      productId: order.value.productId,
      orderNo: order.value.orderNo
    }
  })
}

const viewUserProfile = (userId) => {
  if (!userId) return
  router.push(`/profile/${userId}`)
}

const openEvaluationDialog = () => {
  if (!order.value) return

  if (isBuyer.value) {
    targetUser.value = {
      id: order.value.sellerId,
      nickname: order.value.sellerNickname,
      username: order.value.sellerNickname,
      avatar: order.value.sellerAvatar
    }
  } else if (isSeller.value) {
    targetUser.value = {
      id: order.value.buyerId,
      nickname: order.value.buyerNickname,
      username: order.value.buyerNickname,
      avatar: order.value.buyerAvatar
    }
  } else {
    alert('无法确定评价对象')
    return
  }

  if (!targetUser.value.id) {
    alert('无法获取评价对象信息')
    return
  }

  showEvaluationDialog.value = true
}

const handleEvaluationSuccess = () => {
  hasEvaluated.value = true
  alert('评价提交成功')
}

onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped>
.page-container {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px;
}

.order-detail-container {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e5ebf4;
  padding: 20px;
}

.back-btn {
  border: 1px solid #ced8ea;
  background: #f8fbff;
  border-radius: 8px;
  padding: 6px 12px;
  cursor: pointer;
}

.loading,
.empty {
  padding: 48px 0;
  text-align: center;
  color: #6b7280;
}

.order-detail {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.status-section {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f8fbff;
  border: 1px solid #dce5f5;
  border-radius: 10px;
  padding: 12px;
}

.status-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.status-text {
  font-size: 16px;
  font-weight: 600;
}

.status-tip {
  margin-top: 2px;
  font-size: 12px;
  color: #6b7280;
}

.card-section {
  border: 1px solid #e5ebf4;
  border-radius: 10px;
  padding: 14px;
}

.section-title {
  margin: 0 0 10px;
  font-size: 15px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #f8fbff;
}

.info-item .k {
  color: #6b7280;
}

.product-card {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.product-img {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  object-fit: cover;
}

.product-title {
  font-weight: 600;
}

.product-price {
  margin-top: 4px;
  color: #dc2626;
}

.parties-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.party-card {
  border: 1px solid #e5ebf4;
  border-radius: 8px;
  padding: 10px;
  cursor: pointer;
}

.party-label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
}

.party-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.party-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
}

.amount-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: 8px;
  background: #f8fbff;
}

.amount-item.total {
  background: #eef6ff;
}

.actions-section {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn {
  border: 1px solid #cfd9e9;
  background: #fff;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.btn-primary {
  background: #2f7dff;
  color: #fff;
  border-color: #2f7dff;
}

.btn-warning {
  background: #fff5e8;
  border-color: #ffddb0;
  color: #b45309;
}

.btn-info {
  background: #ecfeff;
  border-color: #a5f3fc;
  color: #0f766e;
}

.btn-success {
  background: #ecfdf3;
  border-color: #bbf7d0;
  color: #15803d;
}

.evaluation-status {
  color: #15803d;
  font-weight: 600;
}

@media (max-width: 768px) {
  .info-grid,
  .parties-grid {
    grid-template-columns: 1fr;
  }
}
</style>
