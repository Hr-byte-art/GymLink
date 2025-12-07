import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8080/api', // Backend API base URL
  timeout: 10000, // 增加超时时间到30秒，支持文件上传
  // 前后端分离模式下，使用 Header 传递 token，不需要 Cookie
  // withCredentials: true,
})

// Request interceptor
request.interceptors.request.use(
  (config) => {
    // 调试日志：打印请求信息
    console.log('Request Interceptor - URL:', config.url)
    console.log('Request Interceptor - Method:', config.method)
    console.log('Request Interceptor - Params:', config.params)
    console.log('Request Interceptor - Data:', config.data)

    // 从 localStorage 获取 token 并添加到请求头
    // Sa-Token 默认会从名为 "GymLink" 的 header 中读取 token（与后端配置的 token-name 一致）
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['GymLink'] = token
      console.log('Request Interceptor - Token added to header:', token)
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
request.interceptors.response.use(
  (response) => {
    const res = response.data
    console.log('Response Interceptor - Response:', res)
    // Backend returns BaseResponse<T> with structure: { code, data, message }
    // Success code is 0
    if (res.code !== 0) {
      ElMessage.error(res.message || 'Error')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    // Return the unwrapped data field so stores don't need to access res.data
    return res.data
  },
  (error) => {
    console.log('Response Interceptor - Error:', error)
    ElMessage.error(error.message || 'Request Error')
    return Promise.reject(error)
  }
)

export default request
