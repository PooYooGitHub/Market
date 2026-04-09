// Axios 配置和请求拦截器
import axios from 'axios'

// 创建 axios 实例
const service = axios.create({
  baseURL: 'http://localhost:8100', // 指向Spring Cloud Gateway
  timeout: 30000 // 请求超时时间（改为30秒，避免RocketMQ发送消息时超时）
})

// 请求拦截器：自动添加 Token
service.interceptors.request.use(
  config => {
    // 优先使用普通用户token，如果没有则使用adminToken
    const token = localStorage.getItem('token') || localStorage.getItem('adminToken')
    
    // 调试日志
    console.log('📤 发送请求:', config.url)
    console.log('   Token存在:', !!token)
    if (token) {
      console.log('   Token前20字符:', token.substring(0, 20) + '...')
      // Sa-Token 配置了从 'satoken' header 读取，token 前缀为 'Bearer'
      config.headers['satoken'] = `Bearer ${token}`
      // 同时也添加标准的 Authorization header 以保持兼容性
      config.headers['Authorization'] = `Bearer ${token}`
    } else {
      console.warn('   ⚠️ 未找到 Token')
    }
    
    return config
  },
  error => {
    console.error('请求失败：', error)
    return Promise.reject(error)
  }
)

// 辅助函数：检查是否应该在 401 时跳转登录页
// 某些后台轮询请求不应该触发跳转，避免打断用户操作
const shouldRedirectOnAuth = (config) => {
  const url = config?.url || ''
  
  // 调试日志
  console.log('🔐 401 检查 - URL:', url)
  
  // 如果请求明确标记为静默请求，不跳转
  if (config && config.silent) {
    console.log('   → 静默请求，不跳转')
    return false
  }
  // 仲裁轮询请求不应触发跳转（在 MainLayout 中自动启动）
  if (url.includes('/api/arbitration/my') || url.includes('/api/arbitration/pending')) {
    console.log('   → 仲裁轮询请求，不跳转')
    return false
  }
  
  console.log('   → 将触发登录跳转')
  return true
}

// 响应拦截器：统一处理响应和错误
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果是文件下载等直接返回 response
    if (response.config.responseType === 'blob') {
      return response
    }

    // 如果响应成功
    if (res.code === 200) {
      return res
    }

    // 处理各种错误状态码
    if (res.code === 401) {
      console.warn('登录已过期，请重新登录')
      
      // 只有非静默请求才清除 token 并跳转
      if (shouldRedirectOnAuth(response.config)) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('adminToken')

        // 延迟导航，避免循环导入问题
        setTimeout(() => {
          // 检查当前是否在管理端
          if (window.location.pathname.startsWith('/admin')) {
            window.location.replace('/admin/login')
          } else {
            window.location.replace('/login')
          }
        }, 100)
      }
      return Promise.reject(new Error('登录已过期'))
    }

    if (res.code === 403) {
      console.warn('没有权限访问该资源')
      return Promise.reject(new Error(res.message || '权限不足'))
    }

    // 其他错误
    const errorMsg = res.message || '请求失败'
    console.error('API错误：', errorMsg)
    return Promise.reject(new Error(errorMsg))
  },
  error => {
    console.error('网络错误：', error)

    // 网络错误处理
    if (error.response) {
      const { status, config } = error.response

      switch (status) {
        case 401:
          console.warn('认证失败，请重新登录')
          
          // 只有非静默请求才清除 token 并跳转
          if (shouldRedirectOnAuth(config)) {
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            localStorage.removeItem('adminToken')

            // 延迟导航，避免循环导入问题
            setTimeout(() => {
              const currentPath = window.location.pathname
              // 如果已经在登录页，不要重复跳转
              if (currentPath === '/login' || currentPath === '/admin/login') {
                console.log('已在登录页，跳过跳转')
                return
              }
              
              // 检查当前是否在管理端
              if (currentPath.startsWith('/admin')) {
                window.location.replace('/admin/login')
              } else {
                window.location.replace('/login')
              }
            }, 100)
          }
          break
        case 403:
          console.warn('权限不足')
          break
        case 404:
          console.warn('请求的资源不存在')
          break
        case 500:
          console.error('服务器内部错误')
          break
        default:
          console.error(`请求失败：${status}`)
      }

      return Promise.reject(error.response.data || error.message)
    }

    if (error.code === 'ECONNABORTED') {
      return Promise.reject(new Error('请求超时，请稍后重试'))
    }

    return Promise.reject(error.message || '网络连接失败')
  }
)

export default service