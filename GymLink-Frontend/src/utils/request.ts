import axios from 'axios'
import { ElMessage } from 'element-plus'

// 自定义响应解析：将超长数字转为字符串，避免精度丢失
const transformResponse = (data: string) => {
  if (!data || typeof data !== 'string') return data
  try {
    const transformed = data
      // 处理对象中的长数字字段
      .replace(/:(\s*)(\d{15,})(\s*)([,\}\]])/g, (_match, _s1, num, _s2, end) => {
        return `:"${num}"${end}`
      })
      // 处理数组中的长数字元素
      .replace(/([,\[])(\s*)(\d{15,})(\s*)([,\]])/g, (_match, start, _s1, num, _s2, end) => {
        return `${start}"${num}"${end}`
      })
    return JSON.parse(transformed)
  } catch (e) {
    console.error('JSON 解析失败:', e, '原始数据片段:', data?.substring(0, 500))
    try {
      return JSON.parse(data)
    } catch {
      return data
    }
  }
}

const request = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端 API 基础地址
  timeout: 10000, // 超时时间：10 秒
  transformResponse: [transformResponse]
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    console.log('请求拦截器 - URL:', config.url)
    console.log('请求拦截器 - 方法:', config.method)
    console.log('请求拦截器 - 参数:', config.params)
    console.log('请求拦截器 - 数据:', config.data)

    const token = localStorage.getItem('token')
    if (token) {
      config.headers['GymLink'] = token
      console.log('请求拦截器 - 已附加 Token 到请求头')
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    console.log('响应拦截器 - 响应数据:', res)

    // 后端统一返回结构：状态码、数据、消息
    if (res.code !== 0) {
      ElMessage.error({ message: res.message || '请求失败', duration: 2000, grouping: true })
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  (error) => {
    console.log('响应拦截器 - 错误:', error)
    ElMessage.error({ message: error.message || '网络请求异常', duration: 2000, grouping: true })
    return Promise.reject(error)
  }
)

export default request
