<template>
  <header class="navbar">
    <div class="navbar-container">
      <div class="navbar-brand">
        <router-link to="/" class="brand-link">
          <img src="/logo.png" alt="GymLink" class="logo" />
          <span class="brand-name">GymLink</span>
        </router-link>
      </div>

      <nav class="navbar-menu">
        <ul class="menu-list">
          <li class="menu-item">
            <router-link to="/" class="menu-link">首页</router-link>
          </li>
          <li class="menu-item">
            <router-link to="/courses" class="menu-link">课程</router-link>
          </li>
          <li class="menu-item">
            <router-link to="/coaches" class="menu-link">教练</router-link>
          </li>
          <li class="menu-item">
            <router-link to="/equipment" class="menu-link">设施</router-link>
          </li>
          <li class="menu-item">
            <router-link to="/recipes" class="menu-link">健身食谱</router-link>
          </li>
          <li class="menu-item">
            <router-link to="/posts" class="menu-link">经验分享</router-link>
          </li>
          <li class="menu-item">
            <router-link to="/about" class="menu-link">关于我们</router-link>
          </li>
        </ul>
      </nav>

      <div class="navbar-actions">
        <!-- 未登录状态显示登录/注册按钮 -->
        <router-link v-if="!isAuthenticated" to="/auth" class="auth-btn">登录/注册</router-link>

        <!-- 已登录状态显示通知和用户信息 -->
        <template v-else>
          <NotificationBell />
          <div class="user-info">
          <div class="user-avatar">
            <img :src="user?.avatar || '/avatar-placeholder.svg'" alt="用户头像" />
          </div>
          <div class="user-dropdown">
            <span class="username">{{ displayName }}</span>
            <div class="dropdown-menu">
              <router-link to="/profile" class="dropdown-item">个人中心</router-link>
              <div class="dropdown-divider"></div>
              <a href="#" class="dropdown-item" @click.prevent="handleLogout">退出登录</a>
            </div>
          </div>
        </div>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import NotificationBell from '@/components/NotificationBell.vue'

const router = useRouter()
const authStore = useAuthStore()

// 计算属性
const isAuthenticated = computed(() => authStore.isAuthenticated)
const user = computed(() => authStore.user)
const displayName = computed(() => authStore.displayName)

// 方法
const handleLogout = () => {
  authStore.logout()
  router.push('/')
}
</script>

<style scoped>
.navbar {
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
  transition: all 0.3s ease;
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
}

.navbar-brand {
  display: flex;
  align-items: center;
}

.brand-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #333;
  transition: all 0.3s ease;
}

.logo {
  height: 40px;
  margin-right: 10px;
}

.brand-name {
  font-size: 24px;
  font-weight: bold;
  color: #4a6cf7;
}

.navbar-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.menu-list {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
}

.menu-item {
  margin: 0 15px;
}

.menu-link {
  text-decoration: none;
  color: #333;
  font-weight: 500;
  padding: 8px 0;
  position: relative;
  transition: color 0.3s ease;
}

.menu-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background-color: #4a6cf7;
  transition: width 0.3s ease;
}

.menu-link:hover {
  color: #4a6cf7;
}

.menu-link:hover::after,
.router-link-active::after {
  width: 100%;
}

.router-link-active {
  color: #4a6cf7;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.auth-btn {
  background-color: #4a6cf7;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 20px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s ease;
}

.auth-btn:hover {
  background-color: #3a5ce5;
  transform: translateY(-2px);
}

/* 用户信息样式 */
.user-info {
  display: flex;
  align-items: center;
  position: relative;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 10px;
  border: 2px solid #4a6cf7;
  cursor: pointer;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-dropdown {
  position: relative;
}

.username {
  font-weight: 500;
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.username::after {
  content: '';
  display: inline-block;
  width: 0;
  height: 0;
  margin-left: 5px;
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-top: 4px solid #333;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 150px;
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transform: translateY(10px);
  transition: all 0.3s ease;
}

.user-dropdown:hover .dropdown-menu {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.dropdown-item {
  display: block;
  padding: 10px 15px;
  color: #333;
  text-decoration: none;
  transition: background-color 0.2s;
}

.dropdown-item:first-child {
  border-radius: 8px 8px 0 0;
}

.dropdown-item:last-child {
  border-radius: 0 0 8px 8px;
}

.dropdown-item:hover {
  background-color: #f5f5f5;
}

.dropdown-divider {
  height: 1px;
  background-color: #eee;
  margin: 5px 0;
}

/* 响应式设计 */
@media (max-width: 968px) {
  .navbar-container {
    padding: 0 15px;
  }

  .menu-item {
    margin: 0 10px;
  }

  .brand-name {
    font-size: 20px;
  }
}

@media (max-width: 768px) {
  .navbar-menu {
    display: none;
  }

  .navbar-container {
    justify-content: space-between;
  }

  .brand-name {
    font-size: 18px;
  }

  .logo {
    height: 30px;
  }
}
</style>