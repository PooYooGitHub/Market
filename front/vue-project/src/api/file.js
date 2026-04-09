import request from '@/utils/request'

/**
 * 上传单张商品图片
 * @param {File} file - 图片文件
 * @returns {Promise}
 */
export const uploadProductImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/file/upload/product',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传商品图片
 * @param {File[]} files - 图片文件数组（最多9张）
 * @returns {Promise}
 */
export const uploadProductImages = (files) => {
  const formData = new FormData()
  
  // 所有文件使用同一个 key "files"
  files.forEach(file => {
    formData.append('files', file)
  })
  
  return request({
    url: '/api/file/upload/product/batch',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传用户头像
 * @param {File} file - 头像文件（最大2MB）
 * @returns {Promise}
 */
export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/file/upload/avatar',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传仲裁证据
 * @param {File} file - 证据文件（最大10MB）
 * @returns {Promise}
 */
export const uploadArbitrationEvidence = (file) => {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/api/file/upload/arbitration',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 校验图片文件
 * @param {File} file - 文件对象
 * @param {Object} options - 配置选项
 * @param {number} options.maxSize - 最大文件大小（MB）
 * @param {string[]} options.allowedTypes - 允许的文件类型
 * @returns {Object} { valid: boolean, message: string }
 */
export const validateImageFile = (file, options = {}) => {
  const {
    maxSize = 5, // 默认5MB
    allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  } = options
  
  // 检查文件类型
  if (!allowedTypes.includes(file.type.toLowerCase())) {
    return {
      valid: false,
      message: `不支持的文件格式，请上传 ${allowedTypes.map(t => t.split('/')[1].toUpperCase()).join('、')} 格式的图片`
    }
  }
  
  // 检查文件大小
  const sizeMB = file.size / 1024 / 1024
  if (sizeMB > maxSize) {
    return {
      valid: false,
      message: `图片大小不能超过 ${maxSize}MB，当前文件大小为 ${sizeMB.toFixed(2)}MB`
    }
  }
  
  return {
    valid: true,
    message: ''
  }
}
