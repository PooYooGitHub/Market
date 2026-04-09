<template>
  <div class="page-container">
    <main class="main-content">
      <div class="order-detail-container">
        <button @click="router.back()" class="back-btn">← 返回</button>

        <div v-if="loading" class="loading">加载中...</div>

        <div v-else-if="!order" class="empty">
          <p>订单不存在或无权访问</p>
          <button @click="router.push('/orders')" class="btn btn-primary">返回订单列表</button>
        </div>

        <div v-else class="order-detail">
          <!-- 订单状态 -->
          <div class="status-section">
            <div :class="['status-icon', `status-${order.status}`]">
              {{ getStatusIcon(order.status) }}
            </div>
            <div class="status-info">
              <div :class="['status-text', `status-${order.status}`]">
                {{ order.statusDesc }}
              </div>
              <div class="status-tip">{{ getStatusTip(order.status) }}</div>
            </div>
          </div>

          <!-- 订单信息 -->
          <div class="info-section">
            <h3 class="section-title">订单信息</h3>
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">订单编号：</span>
                <span class="info-value">{{ order.orderNo }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">下单时间：</span>
                <span class="info-value">{{ formatTime(order.createTime) }}</span>
              </div>
              <div class="info-item" v-if="order.payTime">
                <span class="info-label">支付时间：</span>
                <span class="info-value">{{ formatTime(order.payTime) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">订单状态：</span>
                <span :class="['info-value', `status-${order.status}`]">{{ order.statusDesc }}</span>
              </div>
            </div>
          </div>

          <!-- 商品信息 -->
          <div class="product-section">
            <h3 class="section-title">商品信息</h3>
            <div class="product-card" @click="router.push(`/product/${order.productId}`)">
              <img
                :src="order.productImage || defaultImage"
                :alt="order.productTitle"
                class="product-img"
                @error="e => e.target.src = defaultImage"
              />
              <div class="product-info">
                <div class="product-title">{{ order.productTitle }}</div>
                <div class="product-price">¥{{ Number(order.productPrice).toFixed(2) }}</div>
              </div>
              <div class="product-arrow">›</div>
            </div>
          </div>

          <!-- 交易双方 -->
          <div class="parties-section">
            <h3 class="section-title">交易双方</h3>
            <div class="parties-grid">
              <!-- 买家 -->
              <div class="party-card">
                <div class="party-label">买家</div>
                <div class="party-info" @click="viewUserProfile(order.buyerId)" :class="{ 'clickable': true }">
                  <img
                    :src="order.buyerAvatar || defaultAvatar"
                    :alt="order.buyerNickname"
                    class="party-avatar"
                    @error="e => e.target.src = defaultAvatar"
                  />
                  <span class="party-name">{{ order.buyerNickname || `用户${order.buyerId}` }}</span>
                  <div class="view-hint">点击查看信息</div>
                </div>
              </div>
              <!-- 卖家 -->
              <div class="party-card">
                <div class="party-label">卖家</div>
                <div class="party-info" @click="viewUserProfile(order.sellerId)" :class="{ 'clickable': true }">
                  <img
                    :src="order.sellerAvatar || defaultAvatar"
                    :alt="order.sellerNickname"
                    class="party-avatar"
                    @error="e => e.target.src = defaultAvatar"
                  />
                  <span class="party-name">{{ order.sellerNickname || `用户${order.sellerId}` }}</span>
                  <div class="view-hint">点击查看信息</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 金额信息 -->
          <div class="amount-section">
            <h3 class="section-title">金额信息</h3>
            <div class="amount-list">
              <div class="amount-item">
                <span class="amount-label">商品价格</span>
                <span class="amount-value">¥{{ Number(order.productPrice).toFixed(2) }}</span>
              </div>
              <div class="amount-item total">
                <span class="amount-label">订单总额</span>
                <span class="amount-value">¥{{ Number(order.totalAmount).toFixed(2) }}</span>
              </div>
            </div>
          </div>

          <!-- 订单操作 -->
          <div class="actions-section">
            <button
              v-if="order.status === 0 && isBuyer"
              class="btn btn-primary btn-large"
              @click="handlePay"
            >
              去支付
            </button>
            <button
              v-if="order.status === 0"
              class="btn btn-default btn-large"
              @click="handleCancel"
            >
              取消订单
            </button>
            <button
              v-if="order.status === 1 && isSeller"
              class="btn btn-primary btn-large"
              @click="handleShip"
            >
              确认发货
            </button>
            <button
              v-if="order.status === 2 && isBuyer"
              class="btn btn-primary btn-large"
              @click="handleReceive"
            >
              确认收货
            </button>

            <!-- 仲裁入口 -->
            <button
              v-if="canShowArbitrationAction"
              class="btn btn-warning btn-large"
              @click="applyArbitration"
            >
              ⚖️ {{ arbitrationActionText }}
            </button>

            <!-- 联系对方 -->
            <button
              v-if="order.status >= 1 && order.status <= 2"
              class="btn btn-info btn-large"
              @click="contactOther"
            >
              💬 联系{{ isBuyer ? '卖家' : '买家' }}
            </button>

            <!-- 评价功能 -->
            <button
              v-if="order.status === 3 && !hasEvaluated"
              class="btn btn-success btn-large"
              @click="openEvaluationDialog"
            >
              ⭐ 评价{{ isBuyer ? '卖家' : '买家' }}
            </button>

            <div v-if="order.status === 3 && hasEvaluated" class="evaluation-status">
              ✅ 已评价
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 评价表单对话框 -->
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
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
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
const currentArbitration = ref(null)

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100"%3E%3Crect fill="%23ddd"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999"%3E暂无图片%3C/text%3E%3C/svg%3E'
const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="40" height="40"%3E%3Ccircle cx="20" cy="20" r="20" fill="%23ddd"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-size="12"%3E头像%3C/text%3E%3C/svg%3E'

// 判断当前用户是否是买家或卖家
const isBuyer = computed(() => order.value && userStore.userInfo && order.value.buyerId === userStore.userInfo.id)
const isSeller = computed(() => order.value && userStore.userInfo && order.value.sellerId === userStore.userInfo.id)

// 判断是否可以申请仲裁 (已支付但未完成的订单)
const canApplyArbitration = computed(() => {
  return order.value &&
         (order.value.status === 1 || order.value.status === 2) && // 已支付或已发货状态
         (isBuyer.value || isSeller.value) // 当前用户是买家或卖家
})

// 加载订单详情
const canShowArbitrationAction = computed(() => {
  if (!order.value) return false
  if (!(order.value.status === 1 || order.value.status === 2)) return false
  if (!(isBuyer.value || isSeller.value)) return false
  if (!currentArbitration.value) return true
  return [0, 1, 3].includes(currentArbitration.value.status)
})

const arbitrationActionText = computed(() => {
  if (!currentArbitration.value) return '申请仲裁'
  if ([0, 1].includes(currentArbitration.value.status)) return '修改仲裁'
  if (currentArbitration.value.status === 3) return '重新申请仲裁'
  return '申请仲裁'
})

const loadOrderArbitration = async (orderId) => {
  currentArbitration.value = null

  try {
    const byOrder = await arbitrationApi.getMyArbitrationByOrderId(orderId)
    if (byOrder?.data) {
      currentArbitration.value = byOrder.data
      return
    }
  } catch (_) {
    // fallback: compatible with old backend
  }

  try {
    const listRes = await arbitrationApi.getUserArbitrationList({ current: 1, size: 100 })
    const records = listRes?.data?.records || []
    currentArbitration.value = records.find(item => Number(item.orderId) === Number(orderId)) || null
  } catch (error) {
    console.warn('加载订单仲裁状态失败:', error)
  }
}

const loadOrderDetail = async () => {
  loading.value = true
  try {
    const orderId = route.params.id
    const res = await getOrderDetail(orderId)
    order.value = res.data
    await loadOrderArbitration(Number(orderId))

    // 如果订单已完成，检查是否已评价
    if (res.data.status === 3) {
      await checkOrderEvaluationStatus()
    }

    console.log('订单详情数据:', JSON.stringify(res.data, null, 2))
  } catch (error) {
    console.error('加载订单详情失败:', error)
    alert(error.message || '加载订单详情失败')
    order.value = null
  } finally {
    loading.value = false
  }
}

// 检查评价状态
const checkOrderEvaluationStatus = async () => {
  try {
    const res = await checkEvaluationStatus(order.value.id)
    hasEvaluated.value = res.data?.hasEvaluated || false
  } catch (error) {
    console.error('检查评价状态失败:', error)
    hasEvaluated.value = false
  }
}

// 获取状态图标
const getStatusIcon = (status) => {
  const icons = {
    0: '⏰',
    1: '💰',
    2: '🚚',
    3: '✅',
    4: '❌',
    5: '🔧'
  }
  return icons[status] || '📦'
}

// 获取状态提示
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

// 支付订单
const handlePay = async () => {
  if (!confirm('确认支付该订单？')) return
  
  try {
    await payOrder(order.value.id)
    alert('✅ 支付成功！\n\n等待卖家发货...')
    loadOrderDetail()
  } catch (error) {
    alert(error.message || '支付失败')
  }
}

// 取消订单
const handleCancel = async () => {
  if (!confirm('确认取消该订单？\n\n💡 取消后商品将恢复为"已发布"状态，其他用户可以重新购买。')) return
  
  try {
    await cancelOrder(order.value.id)
    alert('✅ 订单已取消\n\n💡 商品已重新上架，其他用户现在可以购买了。')
    loadOrderDetail()
  } catch (error) {
    alert(error.message || '取消订单失败')
  }
}

// 确认发货
const handleShip = async () => {
  if (!confirm('确认已发货？')) return
  
  try {
    await shipOrder(order.value.id)
    alert('✅ 发货成功！\n\n等待买家确认收货...')
    loadOrderDetail()
  } catch (error) {
    alert(error.message || '发货失败')
  }
}

// 确认收货
const handleReceive = async () => {
  if (!confirm('确认收货？\n\n收货后订单将完成。')) return
  
  try {
    await receiveOrder(order.value.id)
    alert('✅ 确认收货成功！\n\n交易已完成，感谢您的购买！')
    loadOrderDetail()
  } catch (error) {
    alert(error.message || '确认收货失败')
  }
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

// 申请仲裁
const applyArbitration = async () => {
  if (!order.value) return

  const actionText = arbitrationActionText.value || '申请仲裁'
  if (!confirm(`确认要${actionText}吗？\n\n请确保您有充分的理由和证据。`)) return

  // 跳转到仲裁申请页面，并传递订单号
  try {
    const orderId = order.value.id
    const orderNo = order.value.orderNo || `ORDER${String(orderId).padStart(8, '0')}`
    await loadOrderArbitration(orderId)
    const arbitration = currentArbitration.value

    if (arbitration && (arbitration.status === 0 || arbitration.status === 1)) {
      router.push({
        path: '/arbitration/apply',
        query: {
          orderNo,
          arbitrationId: arbitration.id
        }
      })
      return
    }

    if (arbitration && arbitration.status === 2) {
      alert('该订单已有已处理完成的仲裁申请，不能重复创建')
      return
    }

    router.push({
      path: '/arbitration/apply',
      query: { orderNo }
    })
  } catch (error) {
    console.error('检查仲裁状态失败:', error)
    alert(error?.message || '检查仲裁状态失败，请稍后重试')
  }
}

// 联系对方
const contactOther = () => {
  if (!order.value) {
    alert('订单信息加载中，请稍后再试')
    return
  }

  // 打印订单数据用于调试
  console.log('当前订单数据:', order.value)
  console.log('用户身份判断:', { isBuyer: isBuyer.value, isSeller: isSeller.value })

  let targetUserId, targetNickname, targetAvatar, contactType

  if (isBuyer.value) {
    // 买家联系卖家 - 尝试多种可能的字段结构
    targetUserId = order.value.sellerId || order.value.seller?.id
    targetNickname = order.value.sellerNickname || order.value.seller?.nickname || order.value.seller?.username || `用户${order.value.sellerId}`
    targetAvatar = order.value.sellerAvatar || order.value.seller?.avatar
    contactType = '卖家'

    console.log('买家联系卖家:', {
      sellerId: order.value.sellerId,
      sellerNickname: order.value.sellerNickname,
      sellerAvatar: order.value.sellerAvatar,
      seller: order.value.seller
    })
  } else if (isSeller.value) {
    // 卖家联系买家 - 尝试多种可能的字段结构
    targetUserId = order.value.buyerId || order.value.buyer?.id
    targetNickname = order.value.buyerNickname || order.value.buyer?.nickname || order.value.buyer?.username || `用户${order.value.buyerId}`
    targetAvatar = order.value.buyerAvatar || order.value.buyer?.avatar
    contactType = '买家'

    console.log('卖家联系买家:', {
      buyerId: order.value.buyerId,
      buyerNickname: order.value.buyerNickname,
      buyerAvatar: order.value.buyerAvatar,
      buyer: order.value.buyer
    })
  } else {
    alert('无法确定联系对象')
    return
  }

  // 修正验证逻辑：只要有用户ID即可，昵称为空时使用默认值
  if (!targetUserId) {
    console.error('目标用户ID缺失:', { targetUserId, targetNickname, targetAvatar })
    console.log('订单完整数据:', order.value)
    alert(`无法获取${contactType}的用户ID，无法建立联系`)
    return
  }

  // 确保昵称不为空，如果为空则使用默认值
  if (!targetNickname || targetNickname.trim() === '') {
    targetNickname = `用户${targetUserId}`
  }

  console.log('最终联系参数:', { targetUserId, targetNickname, targetAvatar })

  // 跳转到消息页面，并传递参数自动开始会话
  router.push({
    path: '/messages',
    query: {
      userId: targetUserId,
      username: targetNickname,
      nickname: targetNickname,
      avatar: targetAvatar || '',
      productId: order.value.productId, // 传递商品ID用于上下文
      orderNo: order.value.orderNo // 传递订单号用于上下文
    }
  })
}

// 查看用户详细信息
const viewUserProfile = (userId) => {
  if (!userId) {
    alert('无法获取用户信息')
    return
  }
  router.push(`/profile/${userId}`)
}

// 打开评价对话框
const openEvaluationDialog = () => {
  if (!order.value) {
    console.error('订单信息为空，无法打开评价对话框')
    return
  }

  console.log('打开评价对话框，订单信息:', order.value)
  console.log('当前用户身份 - isBuyer:', isBuyer.value, 'isSeller:', isSeller.value)

  // 设置评价目标用户
  if (isBuyer.value) {
    // 买家评价卖家
    targetUser.value = {
      id: order.value.sellerId,
      nickname: order.value.sellerNickname,
      username: order.value.sellerNickname, // 添加username字段以兼容
      avatar: order.value.sellerAvatar
    }
    console.log('买家评价卖家，目标用户:', targetUser.value)
  } else if (isSeller.value) {
    // 卖家评价买家
    targetUser.value = {
      id: order.value.buyerId,
      nickname: order.value.buyerNickname,
      username: order.value.buyerNickname, // 添加username字段以兼容
      avatar: order.value.buyerAvatar
    }
    console.log('卖家评价买家，目标用户:', targetUser.value)
  } else {
    console.error('无法确定用户身份，无法打开评价对话框')
    alert('无法确定您在此订单中的身份，请刷新页面重试')
    return
  }

  // 详细验证目标用户数据
  console.log('验证目标用户数据:', {
    hasId: !!targetUser.value.id,
    id: targetUser.value.id,
    idType: typeof targetUser.value.id,
    fullTargetUser: targetUser.value
  })

  if (!targetUser.value.id) {
    console.error('目标用户ID为空:', {
      targetUser: targetUser.value,
      order: order.value,
      isBuyer: isBuyer.value,
      isSeller: isSeller.value
    })
    alert('无法获取评价对象信息，请刷新页面重试')
    return
  }

  showEvaluationDialog.value = true
}

// 评价成功回调
const handleEvaluationSuccess = (evaluation) => {
  hasEvaluated.value = true
  alert('评价提交成功！感谢您的反馈。')
}

onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped>
.page-container {
  /* 移除背景，由 MainLayout 控制 */
}

.main-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
}

.order-detail-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.15);
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.back-btn {
  padding: 10px 20px;
  border: 2px solid rgba(102, 126, 234, 0.3);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  cursor: pointer;
  margin-bottom: 24px;
  transition: all 0.3s ease;
  color: #667eea;
  font-weight: 500;
}

.back-btn:hover {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.05);
  transform: translateX(-2px);
}

.loading {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}

.empty {
  text-align: center;
  padding: 80px 20px;
}

.empty p {
  color: #999;
  font-size: 18px;
  margin-bottom: 30px;
}

.order-detail {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* 状态部分 */
.status-section {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.status-icon {
  font-size: 60px;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
}

.status-info {
  flex: 1;
}

.status-text {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 8px;
}

.status-tip {
  font-size: 14px;
  opacity: 0.9;
}

/* 区块标题 */
.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

/* 订单信息 */
.info-section {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.info-item {
  display: flex;
  gap: 10px;
}

.info-label {
  color: #666;
  font-size: 14px;
}

.info-value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.info-value.status-0 { color: #ff9800; }
.info-value.status-1 { color: #2196f3; }
.info-value.status-2 { color: #9c27b0; }
.info-value.status-3 { color: #4caf50; }
.info-value.status-4 { color: #999; }

/* 商品信息 */
.product-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.product-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.product-price {
  font-size: 24px;
  color: #ff4081;
  font-weight: bold;
}

.product-arrow {
  font-size: 24px;
  color: #ccc;
}

/* 交易双方 */
.parties-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.party-card {
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  text-align: center;
}

.party-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 15px;
}

.party-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 10px;
  transition: all 0.3s ease;
}

.party-info.clickable {
  cursor: pointer;
  border-radius: 8px;
}

.party-info.clickable:hover {
  background-color: #f5f5f5;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.view-hint {
  font-size: 12px;
  color: #1890ff;
  margin-top: 5px;
}

.party-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.party-name {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

/* 金额信息 */
.amount-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.amount-item.total {
  background: linear-gradient(135deg, #fff5f8 0%, #ffe8f0 100%);
  border: 2px solid #ff4081;
}

.amount-label {
  font-size: 16px;
  color: #666;
}

.amount-item.total .amount-label {
  font-weight: bold;
  color: #333;
}

.amount-value {
  font-size: 18px;
  color: #333;
  font-weight: 500;
}

.amount-item.total .amount-value {
  font-size: 24px;
  color: #ff4081;
  font-weight: bold;
}

/* 订单操作 */
.actions-section {
  display: flex;
  justify-content: center;
  gap: 15px;
  padding-top: 20px;
  border-top: 2px solid #f0f0f0;
}

.btn {
  padding: 12px 30px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.btn-large {
  padding: 15px 40px;
  font-size: 18px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-default {
  background: white;
  border: 2px solid #ddd;
  color: #666;
}

.btn-default:hover {
  border-color: #667eea;
  color: #667eea;
}

.btn-warning {
  background: linear-gradient(135deg, #ff9500 0%, #ff6b35 100%);
  color: white;
  border: none;
}

.btn-warning:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 149, 0, 0.4);
}

.btn-info {
  background: linear-gradient(135deg, #36d1dc 0%, #5b86e5 100%);
  color: white;
  border: none;
}

.btn-info:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(54, 209, 220, 0.4);
}

.btn-success {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
  border: none;
}

.btn-success:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(82, 196, 26, 0.4);
}

.evaluation-status {
  padding: 8px 16px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 6px;
  color: #52c41a;
  font-size: 14px;
  text-align: center;
  font-weight: 500;
}

@media (max-width: 768px) {
  .info-grid,
  .parties-grid {
    grid-template-columns: 1fr;
  }
  
  .actions-section {
    flex-direction: column;
  }
}
</style>
