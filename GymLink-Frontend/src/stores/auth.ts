import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface User {
  id: number
  username: string
  email: string
  avatar?: string
  role?: string
}

export const useAuthStore = defineStore('auth', () => {
  // 用户状态
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

  // 计算属性
  const isAuthenticated = computed(() => !!token.value && !!user.value)

  // 登录方法
  const login = async (credentials: { username: string; password: string }) => {
    try {
      // 这里应该是实际的API调用，现在使用模拟数据
      // const response = await api.post('/auth/login', credentials)

      // 模拟登录成功
      const mockUser: User = {
        id: 1,
        username: credentials.username,
        email: `${credentials.username}@example.com`,
        avatar: '/avatar-placeholder.svg',
        role: 'user'
      }

      const mockToken = 'mock-jwt-token'

      // 保存用户信息和token
      user.value = mockUser
      token.value = mockToken

      // 将token保存到localStorage
      localStorage.setItem('token', mockToken)

      return { success: true, user: mockUser }
    } catch (error) {
      console.error('登录失败:', error)
      return { success: false, error: '登录失败，请检查用户名和密码' }
    }
  }

  // 注册方法
  const register = async (userData: { username: string; email: string; password: string }) => {
    try {
      // 这里应该是实际的API调用
      // const response = await api.post('/auth/register', userData)

      // 模拟注册成功
      const mockUser: User = {
        id: Date.now(),
        username: userData.username,
        email: userData.email,
        avatar: '/avatar-placeholder.svg',
        role: 'user'
      }

      const mockToken = 'mock-jwt-token'

      // 保存用户信息和token
      user.value = mockUser
      token.value = mockToken

      // 将token保存到localStorage
      localStorage.setItem('token', mockToken)

      return { success: true, user: mockUser }
    } catch (error) {
      console.error('注册失败:', error)
      return { success: false, error: '注册失败，请稍后再试' }
    }
  }

  // 退出登录方法
  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
  }

  // 初始化用户信息（从token获取）
  const initAuth = async () => {
    if (token.value) {
      try {
        // 这里应该是实际的API调用，根据token获取用户信息
        // const response = await api.get('/auth/me', {
        //   headers: { Authorization: `Bearer ${token.value}` }
        // })

        // 模拟获取用户信息
        const mockUser: User = {
          id: 1,
          username: 'demo_user',
          email: 'demo@example.com',
          avatar: '/avatar-placeholder.svg',
          role: 'user'
        }

        user.value = mockUser
      } catch (error) {
        console.error('获取用户信息失败:', error)
        // 如果token无效，清除它
        logout()
      }
    }
  }

  return {
    // 状态
    user,
    token,
    // 计算属性
    isAuthenticated,
    // 方法
    login,
    register,
    logout,
    initAuth
  }
})