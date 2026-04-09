<template>
  <div class="product-form-container">
    <div class="form-card">
      <div class="form-header">
        <button @click="goBack" class="back-button" title="返回">
          ← 返回
        </button>
        <h2 class="page-title">{{ isEdit ? '编辑商品' : '发布商品' }}</h2>
      </div>
      
      <form @submit.prevent="handleSubmit" class="product-form">
        <!-- 商品标题 -->
        <div class="form-group">
          <label for="title" class="form-label">
            商品标题 <span class="required">*</span>
          </label>
          <input
            id="title"
            v-model="form.title"
            type="text"
            placeholder="请输入商品标题（不超过100字）"
            maxlength="100"
            required
            class="form-input"
          />
          <div class="form-tip">{{ form.title.length }}/100</div>
        </div>

        <!-- 商品分类 -->
        <div class="form-group">
          <label for="categoryId" class="form-label">
            商品分类 <span class="required">*</span>
          </label>
          <select
            id="categoryId"
            v-model="form.categoryId"
            required
            class="form-select"
          >
            <option :value="null" disabled>请选择分类</option>
            <option
              v-for="category in categories"
              :key="category.id"
              :value="category.id"
            >
              {{ category.name }}
            </option>
          </select>
        </div>

        <!-- 价格 -->
        <div class="form-row">
          <div class="form-group">
            <label for="price" class="form-label">
              当前价格 <span class="required">*</span>
            </label>
            <div class="input-with-prefix">
              <span class="input-prefix">¥</span>
              <input
                id="price"
                v-model.number="form.price"
                type="number"
                step="0.01"
                min="0.01"
                max="999999.99"
                placeholder="0.00"
                required
                class="form-input"
              />
            </div>
          </div>

          <div class="form-group">
            <label for="originalPrice" class="form-label">
              原价（选填）
            </label>
            <div class="input-with-prefix">
              <span class="input-prefix">¥</span>
              <input
                id="originalPrice"
                v-model.number="form.originalPrice"
                type="number"
                step="0.01"
                min="0.01"
                max="999999.99"
                placeholder="0.00"
                class="form-input"
              />
            </div>
          </div>
        </div>

        <!-- 商品描述 -->
        <div class="form-group">
          <label for="description" class="form-label">
            商品描述 <span class="required">*</span>
          </label>
          <textarea
            id="description"
            v-model="form.description"
            rows="6"
            placeholder="请详细描述商品的外观、成色、使用情况等信息..."
            maxlength="2000"
            required
            class="form-textarea"
          ></textarea>
          <div class="form-tip">{{ form.description.length }}/2000</div>
        </div>

        <!-- 商品图片 -->
        <div class="form-group">
          <label class="form-label">
            商品图片 <span class="required">*</span>
            <span class="form-tip-inline">（1-9张，最大5MB/张，支持JPG、PNG、GIF、WEBP格式）</span>
          </label>
          
          <div class="image-upload-wrapper">
            <!-- 已上传的图片 -->
            <div
              v-for="(image, index) in uploadedImages"
              :key="'uploaded-' + index"
              class="image-preview"
            >
              <img :src="image.url" alt="商品图片" @error="handleImageError" />
              <button
                type="button"
                @click="removeImage(index, 'uploaded')"
                class="remove-image-btn"
                title="删除"
              >
                ×
              </button>
            </div>
            
            <!-- 正在上传的图片 -->
            <div
              v-for="(file, index) in uploadingFiles"
              :key="'uploading-' + index"
              class="image-preview uploading"
            >
              <img :src="file.preview" alt="上传中" />
              <div class="upload-progress">
                <div class="progress-bar"></div>
                <span class="progress-text">上传中...</span>
              </div>
            </div>
            
            <!-- 上传按钮 -->
            <div
              v-if="canUploadMore"
              class="upload-placeholder"
              @click="triggerFileInput"
            >
              <span class="upload-icon">+</span>
              <span class="upload-text">添加图片</span>
            </div>
          </div>
          
          <div v-if="uploadedImages.length === 0" class="form-error">
            请至少上传1张商品图片
          </div>
          
          <div class="form-tip">
            已上传 {{ uploadedImages.length }}/9 张图片
          </div>
          
          <!-- 隐藏的文件输入框 -->
          <input
            ref="fileInput"
            type="file"
            accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
            multiple
            style="display: none"
            @change="handleFileSelect"
          />
        </div>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <button
            type="button"
            @click="goBack"
            class="btn btn-cancel"
          >
            取消
          </button>
          <button
            type="submit"
            :disabled="loading"
            class="btn btn-submit"
          >
            {{ loading ? '提交中...' : '立即发布' }}
          </button>
        </div>
      </form>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCategoryList, publishProduct, updateProduct, getProductDetail } from '@/api/product'
import { uploadProductImages, validateImageFile } from '@/api/file'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 是否是编辑模式
const isEdit = computed(() => !!route.params.id)

// 表单数据
const form = reactive({
  title: '',
  description: '',
  price: null,
  originalPrice: null,
  categoryId: null,
  imageUrls: []
})

// 分类列表
const categories = ref([])

// 加载状态
const loading = ref(false)

// 图片上传相关
const fileInput = ref(null)
const uploadedImages = ref([]) // 已上传的图片 [{ url: string, file?: File }]
const uploadingFiles = ref([]) // 正在上传的文件 [{ file: File, preview: string }]

// 是否可以继续上传
const canUploadMore = computed(() => {
  return uploadedImages.value.length + uploadingFiles.value.length < 9
})

// 默认占位图（base64 编码的 SVG）
const defaultPlaceholder = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgZmlsbD0iI2Y1ZjVmNSIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE4IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+XHU1NmZlXHU3MjQ3XHU2NTgwXHU1OTMxPC90ZXh0Pgo8L3N2Zz4='

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.code === 200) {
      categories.value = res.data.filter(c => c.level === 1)
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载商品详情（编辑模式）
const loadProductDetail = async () => {
  try {
    const res = await getProductDetail(route.params.id)
    if (res.code === 200) {
      const product = res.data
      
      // 检查权限
      if (product.sellerId !== userStore.userId) {
        alert('无权编辑此商品')
        router.back()
        return
      }
      
      // 填充表单
      form.title = product.title
      form.description = product.description
      form.price = product.price
      form.originalPrice = product.originalPrice || null
      form.categoryId = product.categoryId
      form.imageUrls = [...product.imageUrls]
      
      // 将已有的图片URL转换为uploadedImages格式
      uploadedImages.value = product.imageUrls.map(url => ({ url }))
    } else {
      alert('商品不存在')
      router.back()
    }
  } catch (error) {
    console.error('加载商品详情失败:', error)
    alert('加载失败')
    router.back()
  }
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value?.click()
}

// 文件选择处理
const handleFileSelect = async (event) => {
  const files = Array.from(event.target.files || [])
  
  if (files.length === 0) return
  
  // 检查总数量
  const totalCount = uploadedImages.value.length + uploadingFiles.value.length + files.length
  if (totalCount > 9) {
    alert(`最多上传9张图片，当前已有${uploadedImages.value.length + uploadingFiles.value.length}张，只能再上传${9 - uploadedImages.value.length - uploadingFiles.value.length}张`)
    event.target.value = ''
    return
  }
  
  // 验证每个文件
  for (const file of files) {
    const validation = validateImageFile(file, { maxSize: 5 })
    if (!validation.valid) {
      alert(validation.message)
      event.target.value = ''
      return
    }
  }
  
  // 创建本地预览
  const filePreviews = files.map(file => ({
    file,
    preview: URL.createObjectURL(file)
  }))
  
  uploadingFiles.value.push(...filePreviews)
  
  // 清空 input，以便可以重复选择同一文件
  event.target.value = ''
  
  // 上传文件
  try {
    const res = await uploadProductImages(files)
    
    if (res.code === 200) {
      // 上传成功，将 URL 添加到已上传列表
      const urls = res.data // 数组
      urls.forEach(url => {
        uploadedImages.value.push({ url })
      })
      
      // 移除上传中的文件
      filePreviews.forEach(fp => {
        URL.revokeObjectURL(fp.preview) // 释放内存
        const index = uploadingFiles.value.findIndex(uf => uf.file === fp.file)
        if (index > -1) {
          uploadingFiles.value.splice(index, 1)
        }
      })
    } else {
      throw new Error(res.message || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    alert(error.message || '上传失败，请重试')
    
    // 移除失败的文件
    filePreviews.forEach(fp => {
      URL.revokeObjectURL(fp.preview)
      const index = uploadingFiles.value.findIndex(uf => uf.file === fp.file)
      if (index > -1) {
        uploadingFiles.value.splice(index, 1)
      }
    })
  }
}

// 删除图片
const removeImage = (index, type) => {
  if (type === 'uploaded') {
    uploadedImages.value.splice(index, 1)
  }
}

// 图片加载失败处理
const handleImageError = (e) => {
  // 防止无限循环：如果已经是默认占位图，移除 error 监听
  if (e.target.src === defaultPlaceholder || e.target.src.startsWith('data:image/svg')) {
    e.target.onerror = null
    return
  }
  e.target.src = defaultPlaceholder
}

// 表单验证
const validateForm = () => {
  if (!form.title || form.title.trim().length === 0) {
    alert('请输入商品标题')
    return false
  }
  
  if (form.title.length > 100) {
    alert('商品标题不能超过100字')
    return false
  }
  
  if (!form.categoryId) {
    alert('请选择商品分类')
    return false
  }
  
  if (!form.price || form.price < 0.01 || form.price > 999999.99) {
    alert('请输入正确的价格（0.01-999999.99）')
    return false
  }
  
  if (form.originalPrice && (form.originalPrice < 0.01 || form.originalPrice > 999999.99)) {
    alert('原价格式不正确（0.01-999999.99）')
    return false
  }
  
  if (!form.description || form.description.trim().length === 0) {
    alert('请输入商品描述')
    return false
  }
  
  if (form.description.length > 2000) {
    alert('商品描述不能超过2000字')
    return false
  }
  
  if (uploadedImages.value.length === 0) {
    alert('请至少上传1张商品图片')
    return false
  }
  
  if (uploadedImages.value.length > 9) {
    alert('最多上传9张图片')
    return false
  }
  
  return true
}

// 提交表单
const handleSubmit = async () => {
  // 验证表单
  if (!validateForm()) {
    return
  }
  
  // 确保所有图片都已上传完成
  if (uploadingFiles.value.length > 0) {
    alert('图片正在上传中，请稍候...')
    return
  }
  
  loading.value = true
  
  try {
    // 准备提交数据，使用已上传的图片URL
    const submitData = {
      ...form,
      imageUrls: uploadedImages.value.map(img => img.url)
    }
    
    let res
    if (isEdit.value) {
      // 编辑商品
      res = await updateProduct({
        id: parseInt(route.params.id),
        ...submitData
      })
    } else {
      // 发布商品
      res = await publishProduct(submitData)
    }
    
    if (res.code === 200) {
      alert(isEdit.value ? '更新成功' : '发布成功')
      
      if (isEdit.value) {
        router.push(`/product/${route.params.id}`)
      } else {
        router.push(`/product/${res.data}`)
      }
    } else {
      alert(res.message || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    alert('操作失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 返回
const goBack = () => {
  if (confirm('确定要放弃编辑吗？')) {
    router.back()
  }
}

// 页面加载
onMounted(async () => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    alert('请先登录')
    router.push('/login')
    return
  }
  
  await loadCategories()
  
  // 编辑模式下加载商品详情
  if (isEdit.value) {
    await loadProductDetail()
  }
})
</script>

<style scoped>
.product-form-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.form-card {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.form-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
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
}

.back-button:hover {
  background: #e8e8e8;
  border-color: #667eea;
  color: #667eea;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex: 1;
  text-align: center;
}

.product-form {
  max-width: 600px;
  margin: 0 auto;
}

.form-group {
  margin-bottom: 24px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-label {
  display: block;
  font-weight: 500;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
}

.required {
  color: #f56c6c;
}

.form-tip-inline {
  color: #999;
  font-size: 12px;
  font-weight: 400;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-textarea {
  resize: vertical;
  font-family: inherit;
}

.input-with-prefix {
  position: relative;
}

.input-prefix {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
  font-size: 14px;
}

.input-with-prefix .form-input {
  padding-left: 35px;
}

.form-tip {
  margin-top: 4px;
  color: #999;
  font-size: 12px;
}

.form-error {
  margin-top: 4px;
  color: #f56c6c;
  font-size: 12px;
}

/* 图片上传 */
.image-upload-wrapper {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.image-preview {
  position: relative;
  width: 100%;
  height: 120px;
  border: 1px solid #ddd;
  border-radius: 6px;
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-preview.uploading {
  border-color: #667eea;
}

.image-preview.uploading img {
  opacity: 0.6;
}

.upload-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  padding: 8px;
  text-align: center;
}

.progress-bar {
  height: 3px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
  margin-bottom: 4px;
  animation: progress-animation 1.5s ease-in-out infinite;
}

@keyframes progress-animation {
  0% {
    width: 0%;
  }
  50% {
    width: 70%;
  }
  100% {
    width: 100%;
  }
}

.progress-text {
  color: white;
  font-size: 11px;
}

.remove-image-btn {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 24px;
  height: 24px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  transition: all 0.3s;
}

.remove-image-btn:hover {
  background: rgba(0, 0, 0, 0.8);
}

.upload-placeholder {
  width: 100%;
  height: 120px;
  border: 2px dashed #ddd;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-placeholder:hover {
  border-color: #667eea;
  background: #f9f9ff;
}

.upload-icon {
  font-size: 32px;
  color: #999;
}

.upload-text {
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}

/* 操作按钮 */
.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 40px;
}

.btn {
  flex: 1;
  padding: 14px 0;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e8e8e8;
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 响应式 */
@media (max-width: 768px) {
  .form-card {
    padding: 20px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .page-title {
    font-size: 22px;
  }
  
  .image-upload-wrapper {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }
  
  .image-preview,
  .upload-placeholder {
    height: 100px;
  }
}
</style>
