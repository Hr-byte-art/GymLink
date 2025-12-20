import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Coach } from '@/api/coach'
import type { Student } from '@/api/student'
import { getCoachByUserId } from '@/api/coach'
import { getStudentByUserId } from '@/api/student'
import { userLogin } from '@/api/user'
import request from '@/utils/request'

export interface User {
  id: string | number  // 后端返回字符串类型，避免 JavaScript 大数精度丢失
  username: string
  email: string
  avatar?: string
  role?: string
  name?: string // 真实姓名
  associatedUserId?: string | number // 关联的学员/教练ID
  detailedInfo?: Coach | Student // 详细信息
}

export const useAuthStore = defineStore('auth', () => {
  // 用户状态
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

  // 计算属性
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const displayName = computed(() => {
    if (!user.value) return ''
    return user.value.name || user.value.username
  })

  // 登录方法
  const login = async (credentials: {
    username: string;
    password: string;
    role?: string;
    rememberMe?: boolean;
  }) => {
    try {
      // 调试日志：打印接收到的参数
      console.log('AuthStore login - 接收到的参数:', credentials)

      // 标准化角色名称
      const normalizedRole = credentials.role === 'user' ? 'student' : (credentials.role || 'student')

      // 准备发送到后端的参数
      const loginParams = {
        userAccount: credentials.username,
        password: credentials.password, // 修正字段名：后端期望的是 password 而不是 userPassword
        role: normalizedRole, // 修正字段名：后端期望的是 role 而不是 userRole
        rememberMe: credentials.rememberMe || false // 添加记住我选项
      }

      // 调试日志：打印发送到后端的参数
      console.log('AuthStore login - 发送到后端的参数:', loginParams)

      // 调用后端登录接口，传递所有必要的参数
      const response = await userLogin(loginParams)

      // 🔍 调试日志：查看后端返回的完整数据
      console.log('=== 登录接口返回数据 ===')
      console.log('response.id:', response.id)
      console.log('response.name:', response.name)
      console.log('response.avatar:', response.avatar)
      console.log('response.username:', response.username)
      console.log('response.role:', response.role)
      console.log('完整 response:', JSON.stringify(response, null, 2))

      // 登录成功，保存用户信息和token（后端已返回完整信息）
      const userData: User = {
        id: response.id,
        username: response.username || credentials.username,
        email: response.email || `${credentials.username}@example.com`,
        avatar: response.avatar || '/avatar-placeholder.svg',
        role: normalizedRole,
        name: response.name, // 后端已返回真实姓名
        associatedUserId: response.associatedUserId // 关联的学员/教练ID
      }

      // 🔍 调试日志：查看保存的用户数据
      console.log('=== 保存的用户数据 ===')
      console.log('userData.id:', userData.id)
      console.log('userData.name:', userData.name)
      console.log('userData.avatar:', userData.avatar)

      // 保存用户信息和token
      user.value = userData

      // 检查后端是否返回了token
      if (!response.token) {
        console.warn('后端未返回token，使用session认证')
      }
      token.value = response.token || ''

      // 将token保存到localStorage（如果有的话）
      if (token.value) {
        localStorage.setItem('token', token.value)
      }

      // 如果选择记住我，可以在这里保存额外的信息
      if (credentials.rememberMe) {
        localStorage.setItem('rememberedUser', credentials.username)
      } else {
        localStorage.removeItem('rememberedUser')
      }

      return { success: true, user: userData }
    } catch (error) {
      console.error('登录失败:', error)
      return { success: false, error: '登录失败，请检查用户名和密码' }
    }
  }

  // 注册方法
  const register = async (userData: { username: string; password: string; checkPassword: string }) => {
    try {
      // 调用真实的后端注册接口
      const response = await request.post('/user/userRegister', null, {
        params: {
          userAccount: userData.username,
          userPassword: userData.password,
          checkPassword: userData.checkPassword
        }
      })

      // 注册成功，返回用户ID（不自动登录）
      return { success: true, userId: response }
    } catch (error: any) {
      console.error('注册失败:', error)
      return { success: false, error: error.message || '注册失败，请稍后再试' }
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
        // 🔄 调用真实的后端接口获取当前登录用户信息
        const response = await request.post('/user/getLoginUserInfo')

        console.log('=== initAuth 获取用户数据 ===')
        console.log('response:', response)

        // 如果后端返回 null，说明用户未登录或 session 已过期
        if (!response || !response.id) {
          console.log('用户未登录或 session 已过期，清除本地 token')
          logout()
          return
        }

        console.log('response.id:', response.id)
        console.log('response.associatedUserId:', response.associatedUserId)
        console.log('response.username:', response.username)
        console.log('response.role:', response.role)

        // 使用后端返回的真实用户信息
        user.value = {
          id: response.id,  // User表的ID
          username: response.username,
          email: response.email || `${response.username}@example.com`,
          avatar: response.avatar || '/avatar-placeholder.svg',
          role: response.role || 'student',
          name: response.name,
          associatedUserId: response.associatedUserId // 关联的学员/教练ID
        }

        console.log('=== initAuth 保存的用户数据 ===')
        console.log('user.value.id:', user.value.id)
        console.log('user.value.name:', user.value.name)
        console.log('user.value.avatar:', user.value.avatar)

        // 后端已经返回完整的用户信息（包括 name 和 avatar），不需要再次请求
        // 如果后端返回的 name 为空，才尝试获取详细信息
        if (!user.value.name) {
          setTimeout(() => {
            fetchUserDetailInfo().catch(error => {
              console.error('初始化获取用户详细信息失败:', error)
            })
          }, 500)
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        // 如果token无效，清除它
        logout()
      }
    }
  }

  // 获取用户详细信息（根据角色）
  const fetchUserDetailInfo = async () => {
    if (!user.value) return

    try {
      // 标准化角色名称
      const normalizedRole = user.value.role === 'user' ? 'student' : user.value.role

      if (normalizedRole === 'coach') {
        // 尝试获取教练详细信息，使用真实的userId
        console.log('🔍 调用 getCoachByUserId，传入 userId:', user.value.id)
        const coachInfo = await getCoachByUserId(user.value.id)
        user.value.name = coachInfo.name
        user.value.detailedInfo = coachInfo
        user.value.avatar = coachInfo.avatar
      } else if (normalizedRole === 'student') {
        // 尝试获取学员详细信息，使用真实的userId
        console.log('🔍 调用 getStudentByUserId，传入 userId:', user.value.id)
        console.log('🔍 user.value 完整数据:', JSON.stringify(user.value, null, 2))
        const studentInfo = await getStudentByUserId(user.value.id)
        user.value.name = studentInfo.name
        user.value.detailedInfo = studentInfo
        user.value.avatar = studentInfo.avatar
      }
    } catch (error) {
      console.error('获取用户详细信息失败:', error)
      // 如果获取详细信息失败，使用模拟数据
      const normalizedRole = user.value.role === 'user' ? 'student' : user.value.role

      if (normalizedRole === 'coach') {
        // 教练模拟数据，使用真实的userId
        user.value.name = user.value.name || '张教练'
        user.value.detailedInfo = {
          id: user.value.id, // 使用真实的userId
          username: user.value.username,
          name: user.value.name || '张教练',
          gender: 1,
          phone: '13800138000',
          avatar: user.value.avatar,
          age: 30,
          specialty: '健身训练',
          intro: '专业健身教练，拥有多年教学经验',
          createTime: new Date().toISOString()
        }
      } else if (normalizedRole === 'student') {
        // 学员模拟数据，使用真实的userId
        user.value.name = user.value.name || '王学员'
        user.value.detailedInfo = {
          id: user.value.id, // 使用真实的userId
          username: user.value.username,
          name: user.value.name || '王学员',
          gender: 1,
          phone: '13900139000',
          avatar: user.value.avatar,
          age: 25,
          height: 175,
          weight: 70,
          fitnessGoal: '增肌减脂',
          createTime: new Date().toISOString()
        }
      }
    }
  }

  return {
    // 状态
    user,
    token,
    // 计算属性
    isAuthenticated,
    displayName,
    // 方法
    login,
    register,
    logout,
    initAuth,
    fetchUserDetailInfo
  }
})