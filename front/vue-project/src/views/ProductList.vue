<template>
  <div class="product-list-container">
    <!-- 返回按钮 -->
    <div class="top-bar">
      <button @click="goHome" class="back-button" title="返回首页">
        ← 返回首页
      </button>
      <h2 class="page-title">商品列表</h2>
    </div>
    
    <!-- 搜索和筛选栏 -->
    <div class="search-bar">
      <div class="search-input-wrapper">
        <input
          v-model="searchForm.keyword"
          type="text"
          placeholder="搜索商品..."
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <button @click="handleSearch" class="search-button">搜索</button>
      </div>

      <div class="filter-wrapper">
        <!-- 分类筛选 -->
        <div class="filter-group">
          <span class="filter-label">分类：</span>
          <button
            :class="['filter-button', { active: !searchForm.categoryId }]"
            @click="selectCategory(null)"
          >
            全部
          </button>
          <button
            v-for="category in categories"
            :key="category.id"
            :class="['filter-button', { active: searchForm.categoryId === category.id }]"
            @click="selectCategory(category.id)"
          >
            {{ category.name }}
          </button>
        </div>

        <!-- 价格区间 -->
        <div class="filter-group">
          <span class="filter-label">价格：</span>
          <input
            v-model.number="searchForm.minPrice"
            type="number"
            placeholder="最低价"
            class="price-input"
            min="0"
          />
          <span class="price-separator">-</span>
          <input
            v-model.number="searchForm.maxPrice"
            type="number"
            placeholder="最高价"
            class="price-input"
            min="0"
          />
          <button @click="handleSearch" class="apply-button">应用</button>
        </div>

        <!-- 排序 -->
        <div class="filter-group">
          <span class="filter-label">排序：</span>
          <select v-model="sortField" @change="handleSort" class="sort-select">
            <option value="create_time">最新发布</option>
            <option value="price">价格</option>
            <option value="view_count">热门</option>
          </select>
          <button @click="toggleSortOrder" class="sort-order-button">
            {{ sortOrder === 'asc' ? '↑ 升序' : '↓ 降序' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-else-if="products.length === 0" class="empty">
      <p>暂无商品</p>
    </div>

    <div v-else class="product-grid">
      <div
        v-for="product in products"
        :key="product.id"
        class="product-card"
        @click="goToDetail(product.id)"
      >
        <div class="product-image-wrapper">
          <img
            :src="product.coverImage || '/placeholder.png'"
            :alt="product.title"
            class="product-image"
            @error="handleImageError"
          />
          <span v-if="product.status === 2" class="sold-badge">已售出</span>
        </div>
        
        <div class="product-info">
          <h3 class="product-title">{{ product.title }}</h3>
          
          <div class="product-price">
            <span class="current-price">¥{{ product.price.toFixed(2) }}</span>
            <span v-if="product.originalPrice" class="original-price">
              ¥{{ product.originalPrice.toFixed(2) }}
            </span>
          </div>
          
          <div class="product-meta">
            <span class="category">{{ product.categoryName }}</span>
            <span class="view-count">{{ product.viewCount }} 次浏览</span>
          </div>
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
      
      <select v-model.number="pagination.size" @change="handleSizeChange" class="page-size-select">
        <option :value="10">10条/页</option>
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
      </select>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategoryList, getProductList } from '@/api/product'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  categoryId: null,
  minPrice: null,
  maxPrice: null
})

// 排序
const sortField = ref('create_time')
const sortOrder = ref('desc')

// 分类列表
const categories = ref([])

// 商品列表
const products = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0,
  pages: 0
})

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.code === 200) {
      // 只显示一级分类
      categories.value = res.data.filter(c => c.level === 1)
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载商品列表
const loadProducts = async () => {
  loading.value = true
  
  try {
    const params = {
      keyword: searchForm.keyword || undefined,
      categoryId: searchForm.categoryId || undefined,
      minPrice: searchForm.minPrice || undefined,
      maxPrice: searchForm.maxPrice || undefined,
      sortField: sortField.value,
      sortOrder: sortOrder.value,
      pageNum: pagination.current,
      pageSize: pagination.size
    }
    
    const res = await getProductList(params)
    
    if (res.code === 200) {
      products.value = res.data.records
      pagination.total = res.data.total
      pagination.pages = res.data.pages
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
    alert('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 选择分类
const selectCategory = (categoryId) => {
  searchForm.categoryId = categoryId
  pagination.current = 1
  loadProducts()
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadProducts()
}

// 排序
const handleSort = () => {
  pagination.current = 1
  loadProducts()
}

// 切换排序方向
const toggleSortOrder = () => {
  sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  handleSort()
}

// 翻页
const changePage = (page) => {
  pagination.current = page
  loadProducts()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 改变每页条数
const handleSizeChange = () => {
  pagination.current = 1
  loadProducts()
}

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

// 图片加载失败处理
const handleImageError = (e) => {
  // 防止无限循环
  if (e.target.src === defaultPlaceholder || e.target.src.startsWith('data:image/svg')) {
    e.target.onerror = null
    return
  }
  e.target.src = defaultPlaceholder
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 页面加载时
onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.product-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 顶部栏 */
.top-bar {
  display: flex;
  align-items: center;
  gap: 15px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
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
  color: #333;
  margin: 0;
  flex: 1;
}

/* 搜索栏 */
.search-bar {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-input-wrapper {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.search-input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
}

.search-button {
  padding: 10px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
}

.search-button:hover {
  opacity: 0.9;
}

/* 筛选 */
.filter-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-label {
  font-weight: 500;
  color: #666;
  min-width: 50px;
}

.filter-button {
  padding: 6px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.filter-button:hover {
  border-color: #667eea;
  color: #667eea;
}

.filter-button.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.price-input {
  width: 100px;
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.price-separator {
  color: #999;
}

.apply-button {
  padding: 6px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.sort-select {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.sort-order-button {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

/* 加载和空状态 */
.loading,
.empty {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
}

/* 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.product-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sold-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.product-info {
  padding: 15px;
}

.product-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  margin-left: 8px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

/* 分页 */
.pagination {
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

.page-size-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 10px;
  }
  
  .filter-group {
    font-size: 12px;
  }
  
  .pagination {
    flex-wrap: wrap;
    font-size: 12px;
  }
}
</style>
