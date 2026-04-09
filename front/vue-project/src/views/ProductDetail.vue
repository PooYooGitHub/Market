<template>
  <div class="product-detail-container">
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-else-if="!product" class="empty">
      <p>商品不存在或已下架</p>
      <button @click="goBack" class="back-button">返回列表</button>
    </div>

    <div v-else class="product-detail">
      <!-- 返回按钮 -->
      <button @click="goBack" class="back-btn">← 返回</button>

      <div class="product-main">
        <!-- 左侧：图片展示 -->
        <div class="product-images">
          <div class="main-image-wrapper">
            <img
              :src="currentImage"
              alt="商品图片"
              class="main-image"
              @error="handleImageError"
            />
            <span v-if="product.status === 2" class="sold-overlay">
              <span class="sold-text">已售出</span>
            </span>
          </div>
          
          <div v-if="product.imageUrls && product.imageUrls.length > 1" class="image-thumbnails">
            <div
              v-for="(image, index) in product.imageUrls"
              :key="index"
              :class="['thumbnail', { active: currentImage === image }]"
              @click="currentImage = image"
            >
              <img :src="image" :alt="`图片${index + 1}`" @error="handleImageError" />
            </div>
          </div>
        </div>

        <!-- 右侧：商品信息 -->
        <div class="product-info">
          <h1 class="product-title">{{ product.title }}</h1>
          
          <div class="product-price-section">
            <div class="price-row">
              <span class="current-price">¥{{ product.price.toFixed(2) }}</span>
              <span v-if="product.originalPrice" class="original-price">
                原价：¥{{ product.originalPrice.toFixed(2) }}
              </span>
            </div>
            <div v-if="product.originalPrice" class="discount">
              省 ¥{{ (product.originalPrice - product.price).toFixed(2) }}
            </div>
          </div>

          <div class="product-meta-info">
            <div class="meta-item">
              <span class="meta-label">分类：</span>
              <span class="meta-value">{{ product.categoryName }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">浏览：</span>
              <span class="meta-value">{{ product.viewCount }} 次</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">发布时间：</span>
              <span class="meta-value">{{ formatDate(product.createTime) }}</span>
            </div>
          </div>

          <!-- 卖家信息 -->
          <div v-if="product.seller" class="seller-info">
            <h3 class="section-title">卖家信息</h3>
            <div class="seller-card">
              <img
                :src="product.seller.avatar || defaultAvatar"
                :alt="product.seller.nickname || '卖家'"
                class="seller-avatar"
                @error="handleAvatarError"
              />
              <div class="seller-details">
                <div class="seller-name">{{ product.seller.nickname || product.seller.username }}</div>
                <div class="seller-username">@{{ product.seller.username }}</div>
              </div>
            </div>
          </div>
          <div v-else class="seller-info">
            <h3 class="section-title">卖家信息</h3>
            <div class="seller-card">
              <img
                :src="defaultAvatar"
                alt="卖家"
                class="seller-avatar"
              />
              <div class="seller-details">
                <div class="seller-name">卖家信息加载中...</div>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <template v-if="isOwner">
              <button @click="editProduct" class="btn btn-primary">编辑商品</button>
              <button @click="confirmDelete" class="btn btn-danger">删除商品</button>
            </template>
            <template v-else>
              <button
                @click="addToCartHandler"
                class="btn btn-secondary"
                :disabled="product.status !== 1"
              >
                {{ product.status === 2 ? '已售出' : product.status !== 1 ? '不可购买' : '加入购物车' }}
              </button>
              <button
                @click="buyNow"
                class="btn btn-primary"
                :disabled="product.status !== 1"
              >
                {{ product.status === 2 ? '已售出' : product.status !== 1 ? '不可购买' : '立即购买' }}
              </button>
              <button
                @click="contactSeller"
                class="btn btn-default"
                :disabled="product.status === 2"
              >
                联系卖家
              </button>
            </template>
          </div>
        </div>
      </div>

      <!-- 商品描述 -->
      <div class="product-description">
        <h2 class="section-title">商品描述</h2>
        <div class="description-content">{{ product.description }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getProductDetail, deleteProduct } from '@/api/product'
import { addToCart, createOrder } from '@/api/trade'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const product = ref(null)
const loading = ref(true)
const currentImage = ref('')

// 是否是商品所有者
const isOwner = computed(() => {
  return product.value && userStore.userId === product.value.sellerId
})

// 默认占位图
const defaultPlaceholder = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgZmlsbD0iI2Y1ZjVmNSIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE4IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+XHU1NmZlXHU3MjQ3XHU2NTgwXHU1OTMxPC90ZXh0Pgo8L3N2Zz4='
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2Y1ZjVmNSIvPgogIDxjaXJjbGUgY3g9IjUwIiBjeT0iNDAiIHI9IjE1IiBmaWxsPSIjZGRkIi8+CiAgPHBhdGggZD0iTSAzMCA3MCBRIDMwIDU1IDUwIDU1IFEgNzAgNTUgNzAgNzAgTCA3MCA4MCBRIDcwIDkwIDUwIDkwIFEgMzAgOTAgMzAgODAgWiIgZmlsbD0iI2RkZCIvPgo8L3N2Zz4='

// 加载商品详情
const loadProductDetail = async () => {
  try {
    const productId = route.params.id
    console.log('开始加载商品详情，ID:', productId)
    
    const res = await getProductDetail(productId)
    console.log('商品详情API响应:', res)
    
    if (res.code === 200) {
      product.value = res.data
      console.log('商品数据:', product.value)
      
      // 检查 seller 字段
      if (!res.data.seller) {
        console.warn('警告：商品数据中缺少 seller 字段')
      }
      
      // 设置默认显示第一张图片
      if (res.data.imageUrls && res.data.imageUrls.length > 0) {
        currentImage.value = res.data.imageUrls[0]
      } else {
        // 如果没有图片，使用默认占位图
        currentImage.value = defaultPlaceholder
      }
    } else {
      console.error('加载失败，错误信息:', res.message)
    }
  } catch (error) {
    console.error('加载商品详情失败，错误详情:', error)
    if (error.response) {
      console.error('API响应错误:', error.response.status, error.response.data)
      
      // 401错误特殊处理
      if (error.response.status === 401 || error.response.data?.code === 401) {
        console.error('商品详情接口返回401，这可能是后端配置问题（该接口应该是公开的）')
        alert('查看商品详情需要登录，点击确定前往登录页')
        router.push('/login')
        return
      }
    } else if (error.message) {
      console.error('错误信息:', error.message)
    }
    product.value = null
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

// 头像加载失败
const handleAvatarError = (e) => {
  // 防止无限循环
  if (e.target.src === defaultAvatar || e.target.src.startsWith('data:image/svg')) {
    e.target.onerror = null
    return
  }
  e.target.src = defaultAvatar
}

// 返回
const goBack = () => {
  router.back()
}

// 编辑商品
const editProduct = () => {
  router.push(`/product/edit/${product.value.id}`)
}

// 确认删除
const confirmDelete = async () => {
  if (!confirm('确定要删除这件商品吗？删除后无法恢复。')) {
    return
  }
  
  try {
    const res = await deleteProduct(product.value.id)
    if (res.code === 200) {
      alert('删除成功')
      router.push('/my-products')
    } else {
      alert(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    alert('删除失败，请稍后重试')
  }
}

// 联系卖家
const contactSeller = () => {
  if (!userStore.isLoggedIn) {
    alert('请先登录')
    router.push('/user/login')
    return
  }
  
  if (!product.value?.seller) {
    alert('卖家信息加载中，请稍后再试')
    return
  }
  
  // 跳转到消息页面，并传递卖家信息
  router.push({
    path: '/messages',
    query: {
      userId: product.value.seller.id,
      username: product.value.seller.username,
      nickname: product.value.seller.nickname,
      avatar: product.value.seller.avatar,
      productId: product.value.id
    }
  })
}

// 加入购物车
const addToCartHandler = async () => {
  if (!userStore.isLoggedIn) {
    alert('请先登录')
    router.push('/user/login')
    return
  }
  
  if (product.value.status !== 1) {
    alert('该商品不可购买')
    return
  }
  
  try {
    await addToCart({ productId: product.value.id, quantity: 1 })
    alert('已加入购物车')
  } catch (error) {
    console.error('加入购物车失败:', error)
    alert(error.message || '加入购物车失败')
  }
}

// 立即购买
const buyNow = async () => {
  if (!userStore.isLoggedIn) {
    alert('请先登录')
    router.push('/user/login')
    return
  }
  
  if (product.value.status !== 1) {
    alert('该商品不可购买')
    return
  }
  
  if (!confirm(`确认购买"${product.value.title}"？\n\n💡 下单后商品将被锁定为"已售出"状态，其他用户无法购买。`)) {
    return
  }
  
  try {
    const res = await createOrder({ productId: product.value.id })
    alert('✅ 下单成功！\n\n商品已锁定，请尽快完成支付。')
    router.push(`/order/${res.data.id}`)
  } catch (error) {
    console.error('创建订单失败:', error)
    alert(error.message || '创建订单失败')
  }
}

onMounted(() => {
  loadProductDetail()
})
</script>

<style scoped>
.product-detail-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.loading,
.empty {
  text-align: center;
  padding: 100px 20px;
  color: #999;
  font-size: 18px;
}

.back-button {
  margin-top: 20px;
  padding: 10px 30px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.product-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.back-btn {
  background: white;
  border: 1px solid #ddd;
  padding: 8px 20px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 20px;
  font-size: 14px;
  transition: all 0.3s;
}

.back-btn:hover {
  background: #f5f5f5;
}

.product-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

/* 图片区域 */
.product-images {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.main-image-wrapper {
  position: relative;
  width: 100%;
  height: 500px;
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 15px;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.sold-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
}

.sold-text {
  color: white;
  font-size: 48px;
  font-weight: bold;
}

.image-thumbnails {
  display: flex;
  gap: 10px;
  overflow-x: auto;
}

.thumbnail {
  width: 80px;
  height: 80px;
  border: 2px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s;
}

.thumbnail:hover,
.thumbnail.active {
  border-color: #667eea;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 商品信息区域 */
.product-info {
  background: white;
  border-radius: 8px;
  padding: 30px;
}

.product-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px;
  line-height: 1.4;
}

.product-price-section {
  background: #fff5f5;
  border: 2px solid #ffe0e0;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 15px;
}

.current-price {
  color: #f56c6c;
  font-size: 36px;
  font-weight: bold;
}

.original-price {
  color: #999;
  font-size: 16px;
  text-decoration: line-through;
}

.discount {
  color: #f56c6c;
  font-size: 14px;
  margin-top: 8px;
}

.product-meta-info {
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
  padding: 20px 0;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  padding: 8px 0;
  font-size: 14px;
}

.meta-label {
  color: #999;
  min-width: 80px;
}

.meta-value {
  color: #333;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 15px;
}

.seller-info {
  margin-bottom: 30px;
}

.seller-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
}

.seller-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.seller-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.seller-username {
  font-size: 14px;
  color: #999;
}

.action-buttons {
  display: flex;
  gap: 15px;
}

.btn {
  flex: 1;
  padding: 14px 0;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(240, 147, 251, 0.4);
}

.btn-default {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-default:hover:not(:disabled) {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

.btn-danger {
  background: #f56c6c;
  color: white;
}

.btn-danger:hover {
  background: #f45454;
}

/* 商品描述 */
.product-description {
  background: white;
  border-radius: 8px;
  padding: 30px;
}

.description-content {
  color: #666;
  font-size: 15px;
  line-height: 1.8;
  white-space: pre-wrap;
}

/* 响应式 */
@media (max-width: 768px) {
  .product-main {
    grid-template-columns: 1fr;
  }
  
  .main-image-wrapper {
    height: 300px;
  }
  
  .product-title {
    font-size: 22px;
  }
  
  .current-price {
    font-size: 28px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>
