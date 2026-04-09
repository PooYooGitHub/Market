<template>
  <div class="order-page">
    <el-card class="order-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h3><el-icon><ShoppingCart /></el-icon> 我的订单</h3>
          <el-radio-group v-model="activeTab" @change="filterOrders">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="pending">待评价</el-radio-button>
            <el-radio-button label="completed">已评价</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div class="order-list" v-loading="loading">
        <div v-if="filteredOrders.length === 0" class="empty-state">
          <el-empty description="暂无订单记录" />
        </div>
        <div v-else>
          <div v-for="order in filteredOrders" :key="order.id" class="order-item">
            <div class="order-header">
              <div class="order-info">
                <span class="order-no">订单号: {{ order.orderNo }}</span>
                <el-tag :type="getOrderStatusType(order.status)" size="small">
                  {{ getOrderStatusText(order.status) }}
                </el-tag>
              </div>
              <div class="order-time">{{ formatDate(order.createTime) }}</div>
            </div>

            <div class="order-content">
              <div class="product-section">
                <el-image
                  :src="order.productImage"
                  :preview-src-list="[order.productImage]"
                  class="product-image"
                  fit="cover"
                />
                <div class="product-details">
                  <div class="product-title">{{ order.productTitle }}</div>
                  <div class="product-price">¥{{ order.totalAmount }}</div>
                  <div class="trade-user">
                    <el-avatar :src="order.tradeUser.avatar" size="small" />
                    <span>{{ getTradeUserLabel(order) }}: {{ order.tradeUser.nickname }}</span>
                    <!-- 显示交易对象的信用等级 -->
                    <el-tag :color="order.tradeUser.creditColor" size="small" class="credit-tag">
                      {{ order.tradeUser.creditLevel }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>

            <div class="order-actions">
              <!-- 评价按钮 -->
              <div v-if="order.status === 3" class="evaluation-actions">
                <template v-if="order.evaluationStatus === 'not_evaluated'">
                  <el-button
                    type="primary"
                    size="small"
                    @click="openEvaluationForm(order)"
                  >
                    <el-icon><Star /></el-icon>
                    立即评价
                  </el-button>
                </template>
                <template v-else>
                  <el-tag type="success" size="small">
                    <el-icon><Check /></el-icon>
                    已评价
                  </el-tag>
                  <el-button
                    text
                    type="primary"
                    size="small"
                    @click="viewEvaluation(order)"
                  >
                    查看评价
                  </el-button>
                </template>
              </div>

              <!-- 其他订单状态的操作按钮 -->
              <div v-else class="other-actions">
                <el-button
                  v-if="order.status === 1"
                  type="primary"
                  size="small"
                  @click="confirmReceipt(order)"
                >
                  确认收货
                </el-button>
                <el-button
                  v-if="order.status <= 2"
                  type="info"
                  size="small"
                  @click="contactSeller(order)"
                >
                  联系{{ getTradeUserLabel(order) }}
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 评价表单对话框 -->
    <EvaluationForm
      v-model:visible="evaluationFormVisible"
      :order-info="currentOrder"
      :target-user="currentOrder?.tradeUser || {}"
      :evaluator-id="currentUserId"
      @success="handleEvaluationSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ShoppingCart, Star, Check } from '@element-plus/icons-vue'
import EvaluationForm from '@/components/EvaluationForm.vue'
import { checkEvaluationStatus } from '@/api/credit'
import { format } from 'date-fns'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const loading = ref(false)
const activeTab = ref('all')
const evaluationFormVisible = ref(false)
const currentOrder = ref(null)
const currentUserId = ref(1) // 模拟当前用户ID

// 模拟订单数据
const orders = reactive([
  {
    id: 1,
    orderNo: 'ORDER2024040100001',
    buyerId: 1,
    sellerId: 2,
    productId: 101,
    productTitle: 'iPhone 13 Pro Max 256GB 深空灰色',
    productImage: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=300',
    totalAmount: 8999.00,
    status: 3, // 已收货/完成
    createTime: new Date('2024-03-28'),
    evaluationStatus: 'not_evaluated',
    tradeUser: {
      id: 2,
      nickname: '张小明',
      avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100',
      creditLevel: '优秀',
      creditColor: '#67C23A'
    }
  },
  {
    id: 2,
    orderNo: 'ORDER2024040100002',
    buyerId: 2,
    sellerId: 1,
    productId: 102,
    productTitle: 'MacBook Air M2 13英寸 午夜色',
    productImage: 'https://images.unsplash.com/photo-1541807084-5c52b6b3adef?w=300',
    totalAmount: 9999.00,
    status: 1, // 已支付
    createTime: new Date('2024-03-30'),
    evaluationStatus: 'not_evaluated',
    tradeUser: {
      id: 1,
      nickname: '李小红',
      avatar: 'https://images.unsplash.com/photo-1494790108755-2616b612d5db?w=100',
      creditLevel: '良好',
      creditColor: '#409EFF'
    }
  },
  {
    id: 3,
    orderNo: 'ORDER2024040100003',
    buyerId: 1,
    sellerId: 3,
    productId: 103,
    productTitle: 'Sony WH-1000XM4 无线降噪耳机',
    productImage: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=300',
    totalAmount: 1999.00,
    status: 3, // 已收货/完成
    createTime: new Date('2024-03-25'),
    evaluationStatus: 'evaluated',
    tradeUser: {
      id: 3,
      nickname: '王小华',
      avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100',
      creditLevel: '一般',
      creditColor: '#E6A23C'
    }
  }
])

// 计算属性
const filteredOrders = computed(() => {
  switch (activeTab.value) {
    case 'pending':
      return orders.filter(order => order.status === 3 && order.evaluationStatus === 'not_evaluated')
    case 'completed':
      return orders.filter(order => order.status === 3 && order.evaluationStatus === 'evaluated')
    default:
      return orders
  }
})

// 获取订单状态类型
const getOrderStatusType = (status) => {
  const types = {
    0: 'warning', // 待支付
    1: 'primary', // 已支付
    2: 'info',    // 已发货
    3: 'success', // 已收货/完成
    4: 'danger',  // 已取消
    5: 'warning'  // 售后中
  }
  return types[status] || 'info'
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const texts = {
    0: '待支付',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消',
    5: '售后中'
  }
  return texts[status] || '未知'
}

// 获取交易对象标签
const getTradeUserLabel = (order) => {
  return order.buyerId === currentUserId.value ? '卖家' : '买家'
}

// 格式化日期
const formatDate = (date) => {
  return format(new Date(date), 'yyyy-MM-dd HH:mm')
}

// 打开评价表单
const openEvaluationForm = (order) => {
  currentOrder.value = order
  evaluationFormVisible.value = true
}

// 查看评价
const viewEvaluation = (order) => {
  ElMessage.info('查看评价功能待实现')
}

// 确认收货
const confirmReceipt = async (order) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 更新订单状态
    order.status = 3
    order.evaluationStatus = 'not_evaluated'

    ElMessage.success('收货确认成功')
  } catch {
    // 用户取消
  }
}

// 联系对方
const contactSeller = (order) => {
  ElMessage.info('联系功能待实现，可跳转到聊天页面')
}

// 评价成功回调
const handleEvaluationSuccess = async () => {
  if (currentOrder.value) {
    currentOrder.value.evaluationStatus = 'evaluated'
  }
  ElMessage.success('评价提交成功，感谢您的反馈！')
}

// 过滤订单
const filterOrders = () => {
  // 触发计算属性重新计算
}

// 页面挂载时加载数据
onMounted(() => {
  loading.value = false // 模拟数据，无需加载
})
</script>

<style scoped>
.order-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.order-list {
  min-height: 400px;
}

.order-item {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  margin-bottom: 20px;
  background: white;
  transition: all 0.3s ease;
}

.order-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f5f7fa;
  background: #fafbfc;
  border-radius: 12px 12px 0 0;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.order-no {
  font-weight: 500;
  color: #303133;
}

.order-time {
  font-size: 14px;
  color: #999;
}

.order-content {
  padding: 20px;
}

.product-section {
  display: flex;
  gap: 16px;
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  flex-shrink: 0;
}

.product-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.4;
}

.product-price {
  font-size: 20px;
  font-weight: bold;
  color: #e6a23c;
  margin-bottom: 12px;
}

.trade-user {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.credit-tag {
  border: none;
  color: white;
  font-size: 12px;
  font-weight: 500;
}

.order-actions {
  padding: 16px 20px;
  border-top: 1px solid #f5f7fa;
  text-align: right;
}

.evaluation-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.other-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.empty-state {
  padding: 80px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .order-page {
    padding: 10px;
  }

  .card-header {
    flex-direction: column;
    gap: 15px;
  }

  .order-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .product-section {
    flex-direction: column;
    gap: 12px;
  }

  .product-image {
    width: 80px;
    height: 80px;
  }

  .evaluation-actions,
  .other-actions {
    justify-content: flex-start;
  }
}
</style>