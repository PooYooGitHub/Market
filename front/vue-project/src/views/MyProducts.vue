<template>
  <div class="my-products-container">
    <div class="header">
      <button @click="goHome" class="back-button" title="返回首页">
        ← 返回首页
      </button>
      <h2 class="page-title">我的商品</h2>
      <button @click="goToPublish" class="btn-publish">+ 发布商品</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-else-if="products.length === 0" class="empty">
      <p>您还没有发布商品</p>
      <button @click="goToPublish" class="btn-publish-empty">立即发布</button>
    </div>

    <div v-else class="products-list">
      <div
        v-for="product in products"
        :key="product.id"
        class="product-item"
      >
        <div class="product-image" @click="goToDetail(product.id)" :title="product.status === 2 ? '点击查看交易详情' : '点击查看商品详情'">
          <img
            :src="product.coverImage || '/placeholder.png'"
            :alt="product.title"
            @error="handleImageError"
          />
          <span v-if="product.status === 2" class="status-badge sold">已售出</span>
          <span v-else-if="product.status === 3" class="status-badge offline">已下架</span>
        </div>

        <div class="product-info" @click="goToDetail(product.id)" :title="product.status === 2 ? '点击查看交易详情' : '点击查看商品详情'">
          <h3 class="product-title">{{ product.title }}</h3>
          <div class="product-price">
            <span class="current-price">¥{{ product.price.toFixed(2) }}</span>
            <span v-if="product.originalPrice" class="original-price">
              ¥{{ product.originalPrice.toFixed(2) }}
            </span>
          </div>
          <div class="product-meta">
            <span class="category">{{ product.categoryName }}</span>
            <span class="view-count">{{ product.viewCount }} 浏览</span>
          </div>
          <div class="product-time">
            发布于 {{ formatDate(product.createTime) }}
          </div>
          <!-- 添加状态提示 -->
          <div v-if="product.status === 2" class="click-hint">
            💰 点击查看交易详情
          </div>
          <div v-else class="click-hint">
            👁️ 点击查看商品详情
          </div>
        </div>
        
        <div class="product-actions">
          <button
            @click="goToEdit(product.id)"
            class="action-btn edit-btn"
            :disabled="product.status === 3"
          >
            编辑
          </button>
          <button
            @click="confirmDelete(product)"
            class="action-btn delete-btn"
          >
            删除
          </button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="pagination.total > 0" class="pagination">
      <button
        :disabled="pagination.current === 1"
        @click="changePage(pagination.current - 1)"
        class="page-button"
      >
        上一页
      </button>
      
      <span class="page-info">
        第 {{ pagination.current }} / {{ pagination.pages }} 页，共 {{ pagination.total }} 条
      </span>
      
      <button
        :disabled="pagination.current === pagination.pages"
        @click="changePage(pagination.current + 1)"
        class="page-button"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyProducts, deleteProduct } from '@/api/product'
import { getMySalesOrders } from '@/api/trade'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const products = ref([])
const loading = ref(false)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
  pages: 0
})

// 默认占位图
const defaultPlaceholder = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgZmlsbD0iI2Y1ZjVmNSIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE4IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+XHU1NmZlXHU3MjQ3XHU2NTgwXHU1OTMxPC90ZXh0Pgo8L3N2Zz4='

// 加载我的商品
const loadProducts = async () => {
  loading.value = true
  
  try {
    const res = await getMyProducts({
      pageNum: pagination.current,
      pageSize: pagination.size
    })
    
    if (res.code === 200) {
      products.value = res.data.records
      pagination.total = res.data.total
      pagination.pages = res.data.pages
    }
  } catch (error) {
    console.error('加载我的商品失败:', error)
    alert('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 图片加载失败
const handleImageError = (e) => {
  // 防止无限循环
  if (e.target.src === defaultPlaceholder || e.target.src.startsWith('data:image/svg')) {
    e.target.onerror = null
    return
  }
  e.target.src = defaultPlaceholder
}

// 跳转到发布页面
const goToPublish = () => {
  router.push('/product/publish')
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 跳转到详情页
const goToDetail = async (productId) => {
  // 查找当前商品信息
  const product = products.value.find(p => p.id === productId)

  if (!product) {
    router.push(`/product/${productId}`)
    return
  }

  // 如果商品已售出，尝试跳转到订单详情
  if (product.status === 2) {
    try {
      // 显示加载提示
      const loadingTip = '正在查找交易信息...'
      console.log(loadingTip)

      // 获取我的销售订单，查找该商品对应的订单
      const res = await getMySalesOrders({
        pageNum: 1,
        pageSize: 100  // 获取足够多的记录来查找
      })

      if (res.code === 200 && res.data && res.data.records) {
        // 查找该商品对应的订单
        const order = res.data.records.find(order => order.productId === productId)

        if (order) {
          // 跳转到订单详情页面
          router.push(`/order/${order.id}`)
          return
        }
      }

      // 如果没找到对应的订单
      alert('该商品已售出，但暂未找到对应的交易记录，将跳转到商品详情页')
    } catch (error) {
      console.error('获取订单信息失败:', error)
      alert('获取交易信息失败，将跳转到商品详情页')
    }
  }

  // 默认情况：跳转到商品详情页
  router.push(`/product/${productId}`)
}

// 跳转到编辑页
const goToEdit = (id) => {
  router.push(`/product/edit/${id}`)
}

// 确认删除
const confirmDelete = async (product) => {
  if (!confirm(`确定要删除"${product.title}"吗？删除后无法恢复。`)) {
    return
  }
  
  try {
    const res = await deleteProduct(product.id)
    if (res.code === 200) {
      alert('删除成功')
      // 重新加载列表
      loadProducts()
    } else {
      alert(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    alert('删除失败，请稍后重试')
  }
}

// 翻页
const changePage = (page) => {
  pagination.current = page
  loadProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 页面加载
onMounted(() => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    alert('请先登录')
    router.push('/login')
    return
  }
  
  loadProducts()
})
</script>

<style scoped>
.my-products-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7ff 0%, #e8f4fd 100%);
  padding: 20px;
}

.header {
  max-width: 1200px;
  margin: 0 auto 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.15);
  gap: 15px;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.back-button {
  padding: 8px 16px;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 6px;
  color: #666;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  white-space: nowrap;
}

.back-button:hover {
  background: #e8e8e8;
  border-color: #667eea;
  color: #667eea;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.btn-publish {
  padding: 10px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-publish:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.loading,
.empty {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 8px;
  color: #999;
  font-size: 16px;
}

.btn-publish-empty {
  margin-top: 20px;
  padding: 12px 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
}

.products-list {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.product-item {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  display: grid;
  grid-template-columns: 150px 1fr auto;
  gap: 20px;
  align-items: start;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.product-item:hover {
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.2);
  transform: translateY(-2px);
}

.product-image {
  position: relative;
  width: 150px;
  height: 150px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f5f5;
  cursor: pointer;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.status-badge.sold {
  background: rgba(0, 0, 0, 0.7);
}

.status-badge.offline {
  background: rgba(255, 0, 0, 0.7);
}

.product-info {
  flex: 1;
  cursor: pointer;
}

.product-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  margin-bottom: 10px;
}

.current-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.original-price {
  color: #999;
  font-size: 14px;
  text-decoration: line-through;
  margin-left: 10px;
}

.product-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.product-time {
  font-size: 12px;
  color: #999;
}

.click-hint {
  font-size: 12px;
  color: #667eea;
  margin-top: 5px;
  font-weight: 500;
}

.product-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  min-width: 80px;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.edit-btn {
  background: #667eea;
  color: white;
}

.edit-btn:hover:not(:disabled) {
  background: #5568d3;
}

.delete-btn {
  background: #f5f5f5;
  color: #f56c6c;
}

.delete-btn:hover {
  background: #ffe0e0;
}

/* 分页 */
.pagination {
  max-width: 1200px;
  margin: 20px auto 0;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-button {
  padding: 8px 20px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.page-button:hover:not(:disabled) {
  border-color: #667eea;
  color: #667eea;
}

.page-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .product-item {
    grid-template-columns: 100px 1fr;
    grid-template-rows: auto auto;
  }
  
  .product-image {
    width: 100px;
    height: 100px;
  }
  
  .product-actions {
    grid-column: 1 / -1;
    flex-direction: row;
  }
  
  .pagination {
    flex-wrap: wrap;
    font-size: 12px;
  }
}
</style>
