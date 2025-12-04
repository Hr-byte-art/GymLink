import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8080/api', // Backend API base URL
  timeout: 5000,
})

// Request interceptor
request.interceptors.request.use(
  (config) => {
    // You can add token here if needed
    // const token = localStorage.getItem('token')
    // if (token) {
    //   config.headers['Authorization'] = token
    // }
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
    ElMessage.error(error.message || 'Request Error')
    return Promise.reject(error)
  }
)

export default request
