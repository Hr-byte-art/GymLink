import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Coach } from '@/api/coach'
import type { Student } from '@/api/student'
import { getCoachByUserId } from '@/api/coach'
import { getStudentByUserId } from '@/api/student'
import { userLogin } from '@/api/user'
import request from '@/utils/request'

export interface User {
  id: string | number
  username: string
  email: string
  avatar?: string
  role?: string
  name?: string
  associatedUserId?: string | number
  detailedInfo?: Coach | Student
}

interface LoginResponse {
  id: string | number
  username?: string
  email?: string
  avatar?: string
  role?: string
  name?: string
  associatedUserId?: string | number
  token?: string
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const displayName = computed(() => {
    if (!user.value) return ''
    return user.value.name || user.value.username
  })

  const login = async (credentials: {
    username: string
    password: string
    role?: string
    rememberMe?: boolean
  }) => {
    try {
      const normalizedRole = credentials.role === 'user' ? 'student' : (credentials.role || 'student')
      const loginParams = {
        userAccount: credentials.username,
        password: credentials.password,
        role: normalizedRole,
        rememberMe: credentials.rememberMe || false
      }

      const response = await userLogin(loginParams) as LoginResponse

      const userData: User = {
        id: response.id,
        username: response.username || credentials.username,
        email: response.email || `${credentials.username}@example.com`,
        avatar: response.avatar || '/avatar-placeholder.svg',
        role: normalizedRole,
        name: response.name,
        associatedUserId: response.associatedUserId
      }

      user.value = userData
      token.value = response.token || ''

      if (token.value) {
        localStorage.setItem('token', token.value)
      }

      if (credentials.rememberMe) {
        localStorage.setItem('rememberedUser', credentials.username)
      } else {
        localStorage.removeItem('rememberedUser')
      }

      return { success: true, user: userData }
    } catch {
      return { success: false, error: '登录失败，请检查用户名和密码' }
    }
  }

  const register = async (userData: { username: string; password: string; checkPassword: string }) => {
    try {
      const response = await request.post('/user/userRegister', null, {
        params: {
          userAccount: userData.username,
          userPassword: userData.password,
          checkPassword: userData.checkPassword
        }
      })
      return { success: true, userId: response }
    } catch (error: unknown) {
      return { success: false, error: error instanceof Error ? error.message : '注册失败，请稍后再试' }
    }
  }

  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
  }

  const initAuth = async () => {
    if (!token.value) return

    try {
      const response = await request.post('/user/getLoginUserInfo') as LoginResponse | null
      if (!response?.id) {
        logout()
        return
      }

      user.value = {
        id: response.id,
        username: response.username || '未知用户',
        email: response.email || `${response.username || 'user'}@example.com`,
        avatar: response.avatar || '/avatar-placeholder.svg',
        role: response.role || 'student',
        name: response.name,
        associatedUserId: response.associatedUserId
      }

      if (!user.value.name) {
        await fetchUserDetailInfo()
      }
    } catch {
      logout()
    }
  }

  const fetchUserDetailInfo = async () => {
    if (!user.value) return

    try {
      const normalizedRole = user.value.role === 'user' ? 'student' : user.value.role

      if (normalizedRole === 'coach') {
        const coachInfo = await getCoachByUserId(user.value.id)
        user.value.name = coachInfo.name
        user.value.detailedInfo = coachInfo
        user.value.avatar = coachInfo.avatar
      } else if (normalizedRole === 'student') {
        const studentInfo = await getStudentByUserId(user.value.id)
        user.value.name = studentInfo.name
        user.value.detailedInfo = studentInfo
        user.value.avatar = studentInfo.avatar
      }
    } catch {
      const normalizedRole = user.value.role === 'user' ? 'student' : user.value.role

      if (normalizedRole === 'coach') {
        user.value.name = user.value.name || '教练用户'
        user.value.detailedInfo = {
          id: user.value.id,
          username: user.value.username,
          name: user.value.name,
          gender: 1,
          phone: '13800138000',
          avatar: user.value.avatar || '/avatar-placeholder.svg',
          age: 30,
          specialty: '综合训练',
          intro: '专业健身教练',
          price: 0,
          createTime: new Date().toISOString()
        }
      } else if (normalizedRole === 'student') {
        user.value.name = user.value.name || '学员用户'
        user.value.detailedInfo = {
          id: user.value.id,
          username: user.value.username,
          name: user.value.name,
          gender: 1,
          phone: '13900139000',
          avatar: user.value.avatar || '/avatar-placeholder.svg',
          height: 175,
          weight: 70,
          balance: 0,
          createTime: new Date().toISOString()
        }
      }
    }
  }

  return {
    user,
    token,
    isAuthenticated,
    displayName,
    login,
    register,
    logout,
    initAuth,
    fetchUserDetailInfo
  }
})
