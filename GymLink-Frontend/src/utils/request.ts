import axios from 'axios'
import { ElMessage } from 'element-plus'

// 自定义 JSON 解析，将大数字转换为字符串，避免精度丢失
// JavaScript 安全整数范围: -9007199254740991 到 9007199254740991 (约15-16位)
const transformResponse = (data: string) => {
  if (!data || typeof data !== 'string') return data
  try {
    // 匹配 JSON 中所有超过15位的数字（不在引号内），将其转为字符串
    // 这个正则会匹配: 冒号后、逗号后、方括号后的大数字
    // 使用全局替换，处理所有情况
    const transformed = data
      // 处理 "key": 1234567890123456789 的情况
      .replace(/:(\s*)(\d{15,})(\s*)([,\}\]])/g, (_match, s1, num, s2, end) => {
        return `:"${num}"${end}`
      })
      // 处理数组中的大数字 [1234567890123456789, ...] 或 [..., 1234567890123456789]
      .replace(/([,\[])(\s*)(\d{15,})(\s*)([,\]])/g, (_match, start, s1, num, s2, end) => {
        return `${start}"${num}"${end}`
      })
    return JSON.parse(transformed)
  } catch (e) {
    console.error('JSON parse error:', e, 'Original data:', data?.substring(0, 500))
    // 如果解析失败，尝试直接解析原始数据
    try {
      return JSON.parse(data)
    } catch {
      return data
    }
  }
}

const request = axios.create({
  baseURL: 'http://localhost:8080/api', // Backend API base URL
  timeout: 10000, // 增加超时时间到30秒，支持文件上传
  // 前后端分离模式下，使用 Header 传递 token，不需要 Cookie
  // withCredentials: true,
  transformResponse: [transformResponse]
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
      ElMessage.error({ message: res.message || 'Error', duration: 2000, grouping: true })
      return Promise.reject(new Error(res.message || 'Error'))
    }
    // Return the unwrapped data field so stores don't need to access res.data
    return res.data
  },
  (error) => {
    console.log('Response Interceptor - Error:', error)
    ElMessage.error({ message: error.message || 'Request Error', duration: 2000, grouping: true })
    return Promise.reject(error)
  }
)

export default request
