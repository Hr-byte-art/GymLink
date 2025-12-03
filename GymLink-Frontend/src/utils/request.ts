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
    // Assuming backend returns standard response structure with code, data, message
    // Adjust based on actual BaseResponse
    if (res.code !== 0 && res.code !== 200) { // Assuming 0 or 200 is success
      ElMessage.error(res.message || 'Error')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  (error) => {
    ElMessage.error(error.message || 'Request Error')
    return Promise.reject(error)
  }
)

export default request
