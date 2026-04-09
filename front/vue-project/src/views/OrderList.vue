<template>
  <div class="order-container">
    <h2 class="page-title">📦 我的订单</h2>

        <!-- Tab 切换 -->
        <div class="order-tabs">
          <button
            :class="['tab-btn', { active: activeTab === 'buy' }]"
            @click="activeTab = 'buy'"
          >
            我买到的
          </button>
          <button
            :class="['tab-btn', { active: activeTab === 'sell' }]"
            @click="activeTab = 'sell'"
          >
            我卖出的
          </button>
        </div>

        <!-- 状态筛选 -->
        <div class="order-filters">
          <button
            v-for="status in statusFilters"
            :key="status.value"
            :class="['filter-btn', { active: currentStatus === status.value }]"
            @click="filterByStatus(status.value)"
          >
            {{ status.label }}
          </button>
        </div>

        <div v-if="loading" class="loading">加载中...</div>

        <div v-else-if="orderList.length === 0" class="empty-orders">
          <div class="empty-icon">📦</div>
          <p>暂无订单</p>
          <button class="btn btn-primary" @click="router.push('/products')">去购物</button>
        </div>

        <div v-else class="order-list">
          <div v-for="order in orderList" :key="order.id" class="order-card">
            <!-- 订单头部 -->
            <div class="order-header">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span :class="['order-status', `status-${order.status}`]">
                {{ order.statusDesc }}
              </span>
            </div>

            <!-- 订单内容 -->
            <div class="order-body" @click="goToDetail(order.id)">
              <div class="order-product">
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
              </div>

              <div class="order-other">
                <div class="other-party">
                  <img
                    :src="(activeTab === 'buy' ? order.sellerAvatar : order.buyerAvatar) || defaultAvatar"
                    :alt="activeTab === 'buy' ? order.sellerNickname : order.buyerNickname"
                    class="avatar"
                    @error="e => e.target.src = defaultAvatar"
                  />
                  <span>{{ activeTab === 'buy' ? order.sellerNickname : order.buyerNickname }}</span>
                </div>
                <div class="order-total">
                  <span class="total-label">实付款：</span>
                  <span class="total-amount">¥{{ Number(order.totalAmount).toFixed(2) }}</span>
                </div>
              </div>
            </div>

            <!-- 订单操作 -->
            <div class="order-actions">
              <button
                v-if="order.status === 0 && activeTab === 'buy'"
                class="btn btn-primary"
                @click.stop="handlePay(order.id)"
              >
                去支付
              </button>
              <button
                v-if="order.status === 0"
                class="btn btn-default"
                @click.stop="handleCancel(order.id)"
              >
                取消订单
              </button>
              <button
                v-if="order.status === 1 && activeTab === 'sell'"
                class="btn btn-primary"
                @click.stop="handleShip(order.id)"
              >
                确认发货
              </button>
              <button
                v-if="order.status === 2 && activeTab === 'buy'"
                class="btn btn-primary"
                @click.stop="handleReceive(order.id)"
              >
                确认收货
              </button>
              <button
                v-if="order.status === 3 && !order.hasEvaluated"
                class="btn btn-success"
                @click.stop="openEvaluationDialog(order)"
              >
                ⭐ 评价
              </button>
              <div
                v-if="order.status === 3 && order.hasEvaluated"
                class="evaluation-completed"
              >
                ✅ 已评价
              </div>
              <button
                class="btn btn-default"
                @click.stop="goToDetail(order.id)"
              >
                查看详情
              </button>
            </div>

            <!-- 订单时间 -->
            <div class="order-footer">
              <span class="order-time">下单时间：{{ formatTime(order.createTime) }}</span>
              <span v-if="order.payTime" class="order-time">支付时间：{{ formatTime(order.payTime) }}</span>
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

  <!-- 评价表单对话框 -->
  <EvaluationForm
    v-model:visible="showEvaluationDialog"
    :order-info="selectedOrder"
    :target-user="targetUser"
    :evaluator-id="userStore.userInfo?.id"
    @success="handleEvaluationSuccess"
  />
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getMyOrders, getMySalesOrders, cancelOrder, payOrder, shipOrder, receiveOrder } from '@/api/trade'
import { checkEvaluationStatus } from '@/api/credit'
import { useUserStore } from '@/stores/user'
import EvaluationForm from '@/components/EvaluationForm.vue'

const router = useRouter()
const userStore = useUserStore()

// 状态
const activeTab = ref('buy') // 'buy' | 'sell'
const currentStatus = ref(null) // null 表示全部
const orderList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showEvaluationDialog = ref(false)
const selectedOrder = ref(null)
const targetUser = ref({})

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100"%3E%3Crect fill="%23ddd"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999"%3E暂无图片%3C/text%3E%3C/svg%3E'
const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="40" height="40"%3E%3Ccircle cx="20" cy="20" r="20" fill="%23ddd"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-size="12"%3E头像%3C/text%3E%3C/svg%3E'

// 状态筛选选项
const statusFilters = [
  { label: '全部', value: null },
  { label: '待支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '已发货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]

// 计算总页数
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      status: currentStatus.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }

    const apiCall = activeTab.value === 'buy' ? getMyOrders : getMySalesOrders
    const res = await apiCall(params)

    orderList.value = res.data.records || []
    total.value = res.data.total || 0

    // 为已完成的订单检查评价状态
    if (orderList.value.length > 0) {
      await checkOrdersEvaluationStatus()
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    alert(error.message || '加载订单失败')
  } finally {
    loading.value = false
  }
}

// 状态筛选
const filterByStatus = (status) => {
  currentStatus.value = status
  pageNum.value = 1
  loadOrders()
}

// 切换页码
const changePage = (page) => {
  pageNum.value = page
  loadOrders()
}

// 去订单详情
const goToDetail = (orderId) => {
  router.push(`/order/${orderId}`)
}

// 跳转到支付页面
const goToPayment = (orderId) => {
  router.push(`/payment/${orderId}`)
}

// 支付订单（快速支付，不跳转）
const handlePay = async (orderId) => {
  if (!confirm('确认快速支付该订单？\n\n💡 这将使用默认支付方式完成支付')) return

  try {
    await payOrder(orderId)
    alert('✅ 支付成功！\n\n等待卖家发货...')
    loadOrders()
  } catch (error) {
    alert(error.message || '支付失败')
  }
}

// 取消订单
const handleCancel = async (orderId) => {
  if (!confirm('确认取消该订单？\n\n💡 取消后商品将恢复为"已发布"状态，其他用户可以重新购买。')) return
  
  try {
    await cancelOrder(orderId)
    alert('✅ 订单已取消\n\n💡 商品已重新上架，其他用户现在可以购买了。')
    loadOrders()
  } catch (error) {
    alert(error.message || '取消订单失败')
  }
}

// 确认发货
const handleShip = async (orderId) => {
  if (!confirm('确认发货？')) return
  
  try {
    await shipOrder(orderId)
    alert('✅ 发货成功！\n\n等待买家确认收货...')
    loadOrders()
  } catch (error) {
    alert(error.message || '发货失败')
  }
}

// 确认收货
const handleReceive = async (orderId) => {
  if (!confirm('确认收货？\n\n收货后订单将完成。')) return
  
  try {
    await receiveOrder(orderId)
    alert('✅ 确认收货成功！\n\n交易已完成，感谢您的购买！')
    loadOrders()
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
    minute: '2-digit'
  })
}

// 监听 tab 切换
watch(activeTab, () => {
  pageNum.value = 1
  currentStatus.value = null
  loadOrders()
})

onMounted(() => {
  loadOrders()
})

// 检查订单评价状态
const checkOrdersEvaluationStatus = async () => {
  const completedOrders = orderList.value.filter(order => order.status === 3)

  for (const order of completedOrders) {
    try {
      const res = await checkEvaluationStatus(order.id)
      order.hasEvaluated = res.data?.hasEvaluated || false
    } catch (error) {
      console.error(`检查订单 ${order.id} 评价状态失败:`, error)
      order.hasEvaluated = false
    }
  }
}

// 打开评价对话框
const openEvaluationDialog = (order) => {
  selectedOrder.value = order

  // 设置评价目标用户
  if (activeTab.value === 'buy') {
    // 买家评价卖家
    targetUser.value = {
      id: order.sellerId,
      nickname: order.sellerNickname,
      avatar: order.sellerAvatar
    }
  } else {
    // 卖家评价买家
    targetUser.value = {
      id: order.buyerId,
      nickname: order.buyerNickname,
      avatar: order.buyerAvatar
    }
  }

  showEvaluationDialog.value = true
}

// 评价成功回调
const handleEvaluationSuccess = (evaluation) => {
  if (selectedOrder.value) {
    selectedOrder.value.hasEvaluated = true
  }
  alert('评价提交成功！感谢您的反馈。')
}
</script>

<style scoped>
.order-container {
  max-width: 1200px;
  margin: 20px auto;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.page-title {
  font-size: 28px;
  margin-bottom: 30px;
  color: #333;
}

.order-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.tab-btn {
  padding: 12px 30px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  position: relative;
  transition: all 0.3s;
}

.tab-btn.active {
  color: #667eea;
  font-weight: bold;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #667eea, #764ba2);
}

.order-filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 8px 20px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.filter-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.filter-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

.loading {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}

.empty-orders {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-orders p {
  color: #999;
  font-size: 18px;
  margin-bottom: 30px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.order-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
}

.order-no {
  font-size: 14px;
  color: #666;
}

.order-status {
  font-size: 14px;
  font-weight: bold;
  padding: 4px 12px;
  border-radius: 12px;
}

.status-0 {
  color: #ff9800;
  background: #fff3e0;
}

.status-1 {
  color: #2196f3;
  background: #e3f2fd;
}

.status-2 {
  color: #9c27b0;
  background: #f3e5f5;
}

.status-3 {
  color: #4caf50;
  background: #e8f5e9;
}

.status-4 {
  color: #999;
  background: #f5f5f5;
}

.order-body {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  cursor: pointer;
}

.order-product {
  display: flex;
  gap: 15px;
  flex: 1;
}

.product-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.product-price {
  font-size: 18px;
  color: #ff4081;
  font-weight: bold;
}

.order-other {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 15px;
}

.other-party {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.order-total {
  display: flex;
  align-items: center;
  gap: 5px;
}

.total-label {
  font-size: 14px;
  color: #999;
}

.total-amount {
  font-size: 20px;
  color: #ff4081;
  font-weight: bold;
}

.order-actions {
  padding: 15px 20px;
  background: #fafafa;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid #e0e0e0;
}

.btn {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-default {
  background: white;
  border: 1px solid #ddd;
  color: #666;
}

.btn-default:hover {
  border-color: #667eea;
  color: #667eea;
}

.btn-success {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
}

.btn-success:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.4);
}

.evaluation-completed {
  padding: 6px 12px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 6px;
  color: #52c41a;
  font-size: 12px;
  text-align: center;
}

.order-footer {
  padding: 10px 20px;
  background: #f8f9fa;
  border-top: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
}

.page-btn {
  padding: 8px 20px;
  border: 1px solid #ddd;
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
