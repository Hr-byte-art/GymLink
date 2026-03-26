<template>
  <main class="auth-page" aria-label="GymLink 用户认证页面">
    <section class="auth-shell">
      <aside class="brand-panel">
        <div class="brand-overlay"></div>
        <div class="brand-content">
          <img src="/logo.png" alt="GymLink" class="logo" />
          <h1>GymLink</h1>
          <p class="brand-subtitle">智能训练与健身生活管理平台</p>
          <ul class="brand-points">
            <li>个性化训练计划</li>
            <li>课程预约与教练管理</li>
            <li>器材使用与进度追踪</li>
          </ul>
        </div>
      </aside>

      <section class="form-panel">
        <div class="form-card">
          <header class="form-header">
            <h2>{{ isLogin ? '欢迎回来' : '创建账号' }}</h2>
            <p>{{ isLogin ? '登录后继续你的健身旅程' : '加入 GymLink，开启更科学的训练' }}</p>
          </header>

          <div v-show="isLogin" class="form-wrapper">
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" class="auth-form">
              <el-form-item prop="userAccount">
                <el-input v-model="loginForm.userAccount" placeholder="用户名" :prefix-icon="User" size="large" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="密码"
                  :prefix-icon="Lock"
                  show-password
                  size="large"
                  @keyup.enter="handleLogin"
                />
              </el-form-item>
              <el-form-item prop="role">
                <el-radio-group v-model="loginForm.role" class="role-radio-group" aria-label="角色选择">
                  <el-radio value="user">学员</el-radio>
                  <el-radio value="coach">教练</el-radio>
                  <el-radio value="admin">管理员</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item>
                <el-checkbox v-model="loginForm.rememberMe" class="remember-me">记住我</el-checkbox>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" class="submit-btn" @click="handleLogin" :loading="loading" size="large">
                  登录
                </el-button>
              </el-form-item>
            </el-form>

            <footer class="form-footer">
              <span>还没有账号？</span>
              <a href="javascript:;" @click="isLogin = false" class="switch-link">立即注册</a>
            </footer>
          </div>

          <div v-show="!isLogin" class="form-wrapper">
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" class="auth-form">
              <el-form-item prop="userAccount">
                <el-input v-model="registerForm.userAccount" placeholder="用户名（至少 4 位）" :prefix-icon="User" size="large" />
              </el-form-item>
              <el-form-item prop="userPassword">
                <el-input
                  v-model="registerForm.userPassword"
                  type="password"
                  placeholder="密码（至少 8 位）"
                  :prefix-icon="Lock"
                  show-password
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="checkPassword">
                <el-input
                  v-model="registerForm.checkPassword"
                  type="password"
                  placeholder="确认密码"
                  :prefix-icon="Lock"
                  show-password
                  size="large"
                  @keyup.enter="handleRegister"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" class="submit-btn" @click="handleRegister" :loading="loading" size="large">
                  注册
                </el-button>
              </el-form-item>
            </el-form>

            <footer class="form-footer">
              <span>已有账号？</span>
              <a href="javascript:;" @click="isLogin = true" class="switch-link">去登录</a>
            </footer>
          </div>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const authStore = useAuthStore()

// 根据路由参数判断显示登录还是注册
const isLogin = ref(route.query.type !== 'register')

// 登录表单
const loginForm = reactive({
  userAccount: '', // 用户名
  password: '', // 密码
  role: 'user', // 默认角色为普通用户
  rememberMe: false  // 默认不记住我
})

const loginRules = reactive<FormRules>({
  userAccount: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

// 注册表单
const registerForm = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

const validatePass2 = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules = reactive<FormRules>({
  userAccount: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, message: '用户名至少4个字符', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码至少8个字符', trigger: 'blur' }
  ],
  checkPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }]
})

// 登录处理
const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 使用认证状态管理的登录方法
        const result = await authStore.login({
          username: loginForm.userAccount,
          password: loginForm.password,
          role: loginForm.role,
          rememberMe: loginForm.rememberMe // 添加记住我参数
        })

        if (result.success) {
          ElMessage.success({ message: '登录成功', duration: 1500 })
          // 登录成功后获取用户详细信息（根据角色）
          await authStore.fetchUserDetailInfo()
          // 根据角色跳转到不同页面
          if (loginForm.role === 'admin') {
            router.push('/admin/students')
          } else {
            // 如果有重定向地址，跳转回原来想访问的页面
            const redirectPath = route.query.redirect as string
            router.push(redirectPath || '/')
          }
        } else {
          ElMessage.error(result.error || '登录失败')
        }
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error('登录失败，请稍后再试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 注册处理
const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 使用认证状态管理的注册方法
        const result = await authStore.register({
          username: registerForm.userAccount,
          password: registerForm.userPassword,
          checkPassword: registerForm.checkPassword
        })

        if (result.success) {
          ElMessage.success({ message: '注册成功，请登录', duration: 2000 })
          // 注册成功后切换到登录界面
          isLogin.value = true
          // 清空注册表单
          registerForm.userAccount = ''
          registerForm.userPassword = ''
          registerForm.checkPassword = ''
          if (registerFormRef.value) {
            registerFormRef.value.clearValidate()
          }
        } else {
          ElMessage.error(result.error || '注册失败')
        }
      } catch (error) {
        console.error('注册失败:', error)
        ElMessage.error('注册失败，请稍后再试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 清空表单方法
const resetForms = () => {
  // 清空登录表单
  loginForm.userAccount = ''
  loginForm.password = ''
  loginForm.role = 'user'
  loginForm.rememberMe = false

  // 清空注册表单
  registerForm.userAccount = ''
  registerForm.userPassword = ''
  registerForm.checkPassword = ''

  // 清空表单验证状态
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
  }
  if (registerFormRef.value) {
    registerFormRef.value.clearValidate()
  }
}

// 组件挂载时清空表单
onMounted(() => {
  resetForms()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Barlow+Condensed:wght@500;600;700&family=Barlow:wght@400;500;600;700&display=swap');

.auth-page {
  --primary: #f97316;
  --primary-dark: #ea580c;
  --success: #22c55e;
  --ink: #0f172a;
  --muted: #475569;
  min-height: 100vh;
  padding: 24px;
  display: grid;
  place-items: center;
  background:
    radial-gradient(circle at 10% 10%, rgba(249, 115, 22, 0.24), transparent 40%),
    radial-gradient(circle at 90% 80%, rgba(34, 197, 94, 0.18), transparent 45%),
    #020617;
}

.auth-shell {
  width: min(1120px, 100%);
  min-height: 680px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  border-radius: 24px;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.35);
  box-shadow: 0 40px 90px rgba(2, 6, 23, 0.45);
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(10px);
}

.brand-panel {
  position: relative;
  display: flex;
  align-items: flex-end;
  background-image: url('/auth-fitness-bg-placeholder.png');
  background-size: cover;
  background-position: center;
}

.brand-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(20deg, rgba(2, 6, 23, 0.88) 0%, rgba(2, 6, 23, 0.46) 60%, rgba(2, 6, 23, 0.3) 100%);
}

.brand-content {
  position: relative;
  z-index: 1;
  padding: 44px;
  color: #f8fafc;
  font-family: 'Barlow', sans-serif;
}

.logo {
  width: 128px;
  height: auto;
  margin-bottom: 16px;
}

.brand-content h1 {
  margin: 0;
  font-family: 'Barlow Condensed', sans-serif;
  font-size: 52px;
  font-weight: 700;
  letter-spacing: 1px;
}

.brand-subtitle {
  margin: 10px 0 24px;
  font-size: 18px;
  color: #cbd5e1;
}

.brand-points {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 10px;
}

.brand-points li {
  padding: 10px 12px;
  border: 1px solid rgba(203, 213, 225, 0.24);
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.45);
}

.form-panel {
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 34px;
}

.form-card {
  width: 100%;
  max-width: 390px;
}

.form-header h2 {
  margin: 0;
  color: var(--ink);
  font-family: 'Barlow Condensed', sans-serif;
  font-size: 40px;
  font-weight: 700;
}

.form-header p {
  margin: 10px 0 24px;
  color: var(--muted);
}

.form-wrapper {
  animation: cardIn 0.28s ease;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.auth-form :deep(.el-input__wrapper) {
  min-height: 46px;
  border-radius: 12px;
  border: 1px solid #cbd5e1;
  box-shadow: none;
  transition: all 0.2s ease;
}

.auth-form :deep(.el-input__wrapper:hover) {
  border-color: #94a3b8;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.18);
}

.role-radio-group {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.role-radio-group :deep(.el-radio__input.is-checked .el-radio__inner) {
  border-color: var(--primary);
  background: var(--primary);
}

.role-radio-group :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: var(--primary-dark);
}

.submit-btn {
  width: 100%;
  min-height: 46px;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  letter-spacing: 0.2px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(249, 115, 22, 0.28);
}

.form-footer {
  margin-top: 4px;
  color: var(--muted);
  display: flex;
  gap: 8px;
  justify-content: center;
}

.switch-link {
  color: var(--primary-dark);
  font-weight: 700;
  text-decoration: none;
}

.switch-link:hover {
  text-decoration: underline;
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 980px) {
  .auth-page {
    padding: 14px;
  }

  .auth-shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .brand-panel {
    min-height: 280px;
  }

  .brand-content {
    padding: 26px;
  }

  .brand-content h1 {
    font-size: 40px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .form-wrapper,
  .submit-btn {
    animation: none;
    transition: none;
  }
}
</style>
