<template>
  <header class="navbar">
    <div class="navbar-container">
      <router-link to="/" class="brand-link" aria-label="GymLink 首页">
        <img src="/logo.png" alt="GymLink" class="logo" />
        <span class="brand-name">GymLink</span>
      </router-link>

      <nav class="navbar-menu" aria-label="主导航">
        <ul class="menu-list">
          <li><router-link to="/" class="menu-link">首页</router-link></li>
          <li><router-link to="/courses" class="menu-link">课程</router-link></li>
          <li><router-link to="/coaches" class="menu-link">教练</router-link></li>
          <li><router-link to="/equipment" class="menu-link">器材</router-link></li>
          <li><router-link to="/recipes" class="menu-link">饮食计划</router-link></li>
          <li><router-link to="/posts" class="menu-link">社区</router-link></li>
          <li><router-link to="/about" class="menu-link">关于我们</router-link></li>
        </ul>
      </nav>

      <div class="navbar-actions">
        <router-link v-if="!isAuthenticated" to="/auth" class="auth-btn">登录 / 注册</router-link>

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

const isAuthenticated = computed(() => authStore.isAuthenticated)
const user = computed(() => authStore.user)
const displayName = computed(() => authStore.displayName)

const handleLogout = () => {
  authStore.logout()
  router.push('/')
}
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid #e2e8f0;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
}

.navbar-container {
  max-width: 1240px;
  height: 74px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.brand-link {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.logo {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.brand-name {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: 0.3px;
}

.navbar-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.menu-list {
  list-style: none;
  display: flex;
  align-items: center;
  gap: 20px;
  margin: 0;
  padding: 0;
}

.menu-link {
  position: relative;
  text-decoration: none;
  color: #334155;
  font-weight: 600;
  padding: 8px 0;
  transition: color 0.2s ease;
}

.menu-link::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -2px;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  transition: width 0.2s ease;
}

.menu-link:hover,
.menu-link.router-link-active {
  color: #c2410c;
}

.menu-link:hover::after,
.menu-link.router-link-active::after {
  width: 100%;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.auth-btn {
  text-decoration: none;
  border-radius: 999px;
  padding: 9px 18px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.auth-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(249, 115, 22, 0.3);
}

.user-info {
  display: flex;
  align-items: center;
  position: relative;
}

.user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid rgba(249, 115, 22, 0.45);
  margin-right: 8px;
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
  font-weight: 700;
  color: #334155;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.username::after {
  content: '';
  width: 0;
  height: 0;
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-top: 5px solid #475569;
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  min-width: 150px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.15);
  opacity: 0;
  visibility: hidden;
  transform: translateY(6px);
  transition: all 0.2s ease;
}

.user-dropdown:hover .dropdown-menu {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.dropdown-item {
  display: block;
  padding: 10px 14px;
  text-decoration: none;
  color: #334155;
  font-weight: 500;
}

.dropdown-item:hover {
  background: #f8fafc;
}

.dropdown-divider {
  height: 1px;
  background: #e2e8f0;
}

@media (max-width: 920px) {
  .navbar-menu {
    display: none;
  }

  .brand-name {
    font-size: 20px;
  }
}
</style>
