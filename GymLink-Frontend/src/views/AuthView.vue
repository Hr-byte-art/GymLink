<template>
  <div class="auth-container">
    <div class="auth-box">
      <!-- Left side - Brand -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo">
            <img src="/logo.png" alt="GymLink" class="logo-icon" />
            <h1 class="brand-name">GymLink</h1>
          </div>
          <p class="brand-slogan">智能健身管理平台</p>
          <div class="brand-features">
            <div class="feature-item">专业教练团队</div>
            <div class="feature-item">智能课程安排</div>
            <div class="feature-item">健康数据追踪</div>
          </div>
        </div>
      </div>

      <!-- Right side - Auth Form -->
      <div class="form-section">
        <div class="form-content">
          <!-- Login Form -->
          <div v-show="isLogin" class="form-wrapper">
            <h2 class="form-title">欢迎登录</h2>
            <p class="form-subtitle">请输入您的账号信息</p>

            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" class="auth-form">
              <el-form-item prop="userAccount">
                <el-input v-model="loginForm.userAccount" placeholder="用户名" :prefix-icon="User" size="large" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="loginForm.password" type="password" placeholder="密码" :prefix-icon="Lock"
                  show-password size="large" @keyup.enter="handleLogin" />
              </el-form-item>

              <el-form-item prop="role">
                <el-radio-group v-model="loginForm.role" class="role-radio-group">
                  <el-radio value="user">用户</el-radio>
                  <el-radio value="coach">教练</el-radio>
                  <el-radio value="admin">管理员</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item>
                <el-checkbox v-model="loginForm.rememberMe" class="remember-me">
                  记住我
                </el-checkbox>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" class="auth-btn" @click="handleLogin" :loading="loading" size="large">
                  登录
                </el-button>
              </el-form-item>
            </el-form>

            <div class="form-footer">
              <span>还没有账号？</span>
              <a href="javascript:;" @click="isLogin = false" class="link">立即注册</a>
            </div>
          </div>

          <!-- Register Form -->
          <div v-show="!isLogin" class="form-wrapper">
            <h2 class="form-title">欢迎注册</h2>
            <p class="form-subtitle">创建您的账号开始健身</p>

            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" class="auth-form">
              <el-form-item prop="userAccount">
                <el-input v-model="registerForm.userAccount" placeholder="用户名（至少4个字符）" :prefix-icon="User"
                  size="large" />
              </el-form-item>
              <el-form-item prop="userPassword">
                <el-input v-model="registerForm.userPassword" type="password" placeholder="密码（至少8个字符）"
                  :prefix-icon="Lock" show-password size="large" />
              </el-form-item>
              <el-form-item prop="checkPassword">
                <el-input v-model="registerForm.checkPassword" type="password" placeholder="确认密码" :prefix-icon="Lock"
                  show-password size="large" @keyup.enter="handleRegister" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" class="auth-btn" @click="handleRegister" :loading="loading" size="large">
                  注册
                </el-button>
              </el-form-item>
            </el-form>

            <div class="form-footer">
              <span>已有账号？</span>
              <a href="javascript:;" @click="isLogin = true" class="link">立即登录</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
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

// Login form
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

// Register form
const registerForm = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

const validatePass2 = (rule: any, value: any, callback: any) => {
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

// Login handler
const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 调试日志：打印表单数据
        console.log('AuthView - 表单数据:', loginForm)

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
            router.push('/')
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

// Register handler
const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 使用认证状态管理的注册方法
        const result = await authStore.register({
          username: registerForm.userAccount,
          email: `${registerForm.userAccount}@example.com`,
          password: registerForm.userPassword
        })

        if (result.success) {
          ElMessage.success({ message: '注册成功', duration: 1500 })
          router.push('/')
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
@import url('https://fonts.googleapis.com/css2?family=Dancing+Script:wght@700&display=swap');

.auth-container {
  position: relative;
  min-height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: url('/background.jpg') no-repeat center center;
  background-size: cover;
}

/* 背景遮罩层，增强对比度 */
.auth-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.25) 0%, rgba(118, 75, 162, 0.35) 100%);
  z-index: 1;
}

.auth-box {
  position: relative;
  z-index: 10;
  display: flex;
  width: 1100px;
  max-width: 95%;
  min-height: 680px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* Left Brand Section */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, rgba(49, 81, 228, 0.1) 0%, rgba(88, 153, 209, 0.1) 100%);
  backdrop-filter: blur(15px);
  border-right: 1px solid rgba(255, 255, 255, 0.3);
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

/* 添加背景纹理效果 */
.brand-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('/background.jpg') center/cover;
  opacity: 0.1;
  z-index: -1;
}

.brand-content {
  text-align: center;
  color: white;
  z-index: 1;
}

.logo {
  margin-bottom: 30px;
}

.logo-icon {
  font-size: 56px;
  width: 200px;
  margin-bottom: 15px;
  filter: drop-shadow(0 10px 30px rgba(255, 255, 255, 0.3));
  animation: logoFloat 3s ease-in-out infinite;
  object-fit: contain;
}

@keyframes logoFloat {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-15px);
  }
}

.brand-name {
  font-size: 48px;
  font-weight: 700;
  font-family: 'Dancing Script', cursive;
  margin: 0;
  background: linear-gradient(135deg, #ffffff 0%, #e0e7ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 4px 20px rgba(255, 255, 255, 0.3);
  letter-spacing: 2px;
}

.brand-slogan {
  font-size: 20px;
  margin: 15px 0 50px;
  opacity: 0.95;
  font-weight: 300;
  letter-spacing: 1px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.brand-features {
  text-align: center;
  max-width: 280px;
  margin: 0 auto;
}

.feature-item {
  margin: 18px 0;
  font-size: 17px;
  opacity: 0.92;
  padding-left: 0;
  font-weight: 400;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* Right Form Section */
.form-section {
  flex: 1;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  padding: 50px 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-content {
  width: 100%;
  max-width: 360px;
}

/* Form wrapper for smooth transition */
.form-wrapper {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-title {
  font-size: 36px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 10px 0;
  letter-spacing: 1px;
}

.form-subtitle {
  font-size: 15px;
  color: #7f8c8d;
  margin: 0 0 45px 0;
  font-weight: 400;
}

.auth-form {
  margin-bottom: 0;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 13px 16px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.auth-form :deep(.el-input__inner) {
  font-size: 16px;
}

.auth-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.15);
  border-color: rgba(102, 126, 234, 0.3);
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 18px rgba(102, 126, 234, 0.25);
  border-color: rgba(102, 126, 234, 0.5);
}

.auth-form :deep(.el-input__prefix) {
  color: #667eea;
}

.auth-btn {
  width: 100%;
  border-radius: 10px;
  height: 50px;
  font-size: 17px;
  font-weight: 600;
  background: linear-gradient(135deg, rgba(81, 155, 214, 0.84) 0%, #587fe4 100%);
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.35);
  letter-spacing: 1px;
}

.auth-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 28px rgba(102, 126, 234, 0.45);
}

.auth-btn:active {
  transform: translateY(-1px);
}

.form-footer {
  text-align: center;
  font-size: 15px;
  color: #7f8c8d;
  margin-top: 20px;
}

.form-footer .link {
  color: #667eea;
  text-decoration: none;
  margin-left: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.form-footer .link:hover {
  color: #764ba2;
  text-decoration: underline;
}

.remember-me {
  margin-bottom: 12px;
}

.remember-me :deep(.el-checkbox__label) {
  color: #7f8c8d;
  font-size: 14px;
}

.remember-me :deep(.el-checkbox__inner) {
  border-color: rgba(102, 126, 234, 0.3);
}

.remember-me :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #667eea;
  border-color: #667eea;
}

.role-radio-group {
  width: 100%;
  display: flex;
  justify-content: space-between;
}

.role-radio-group :deep(.el-radio) {
  margin-right: 0;
}

.role-radio-group :deep(.el-radio__input.is-checked .el-radio__inner) {
  border-color: #667eea;
  background: #667eea;
}

.role-radio-group :deep(.el-radio__label) {
  color: #7f8c8d;
}

.role-radio-group :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #667eea;
}

/* Responsive */
@media (max-width: 1200px) {
  .auth-box {
    width: 950px;
  }

}

@media (max-width: 968px) {
  .auth-box {
    flex-direction: column;
    width: 100%;
    max-width: 500px;
    min-height: auto;
  }

  .brand-section {
    width: 100%;
    padding: 50px 30px;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
    min-height: 350px;
  }

  .form-section {
    width: 100%;
    padding: 40px 30px;
  }

  .brand-slogan {
    font-size: 18px;
    margin-bottom: 40px;
  }
}

@media (max-width: 480px) {
  .auth-box {
    max-width: 95%;
  }

  .brand-section {
    padding: 40px 25px;
    min-height: 300px;
  }

  .form-section {
    padding: 35px 25px;
  }

  .brand-slogan {
    font-size: 16px;
  }

  .form-title {
    font-size: 26px;
  }

  .tab-item {
    font-size: 14px;
    padding: 10px 15px;
  }
}
</style>
