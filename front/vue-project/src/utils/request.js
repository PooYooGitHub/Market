// Axios 配置和请求拦截器
import axios from 'axios'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
  baseURL: 'http://localhost:9000', // API 网关地址
  timeout: 10000 // 请求超时时间
})

// 请求拦截器：自动添加 Token
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['satoken'] = token
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理错误
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // code 不是 200 则认为失败
    if (res.code !== 200) {
      // 401 未登录
      if (res.code === 401) {
        alert('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
        return Promise.reject(new Error(res.message || '未登录'))
      }
      
      // 其他错误
      alert(res.message || '操作失败')
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res
    }
  },
  error => {
    console.error('响应错误:', error)
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          alert('登录已过期，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          router.push('/login')
          break
        case 403:
          alert('无权限访问')
          break
        case 404:
          alert('请求的资源不存在')
          break
        case 500:
          alert('服务器错误，请稍后重试')
          break
        default:
          alert('网络错误，请检查网络连接')
      }
    } else {
      alert('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

export default service
