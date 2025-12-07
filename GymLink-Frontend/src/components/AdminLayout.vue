<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="220px" class="admin-aside">
        <div class="admin-logo">
          <img src="/logo.png" alt="GymLink" />
          <span>管理后台</span>
        </div>
        <el-menu :default-active="activeMenu" class="admin-menu" router background-color="#304156" text-color="#bfcbd9"
          active-text-color="#409EFF">
          <el-menu-item index="/admin/students">
            <el-icon>
              <User />
            </el-icon>
            <span>学员管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/coaches">
            <el-icon>
              <Avatar />
            </el-icon>
            <span>教练管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/courses">
            <el-icon>
              <Reading />
            </el-icon>
            <span>课程管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/equipment">
            <el-icon>
              <SetUp />
            </el-icon>
            <span>器材管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/recipes">
            <el-icon>
              <Food />
            </el-icon>
            <span>菜谱管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/posts">
            <el-icon>
              <Document />
            </el-icon>
            <span>帖子管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/equipment-stats">
            <el-icon>
              <DataLine />
            </el-icon>
            <span>器材数据分析</span>
          </el-menu-item>
          <el-menu-item index="/admin/course-stats">
            <el-icon>
              <TrendCharts />
            </el-icon>
            <span>课程数据分析</span>
          </el-menu-item>
          <el-menu-item index="/admin/coach-stats">
            <el-icon>
              <Histogram />
            </el-icon>
            <span>教练数据分析</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="admin-header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin' }">管理后台</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <span class="admin-name">{{ authStore.user?.name || '管理员' }}</span>
            <el-dropdown @command="handleCommand">
              <el-avatar :size="36" :src="authStore.user?.avatar || '/avatar-placeholder.svg'" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="home">返回首页</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="admin-main">
          <slot></slot>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { DataAnalysis, User, Avatar, Reading, SetUp, Food, Document, DataLine, TrendCharts, Histogram } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)

const pageTitles: Record<string, string> = {
  '/admin/students': '学员管理',
  '/admin/coaches': '教练管理',
  '/admin/courses': '课程管理',
  '/admin/equipment': '器材管理',
  '/admin/recipes': '菜谱管理',
  '/admin/posts': '帖子管理',
  '/admin/equipment-stats': '器材数据分析',
  '/admin/course-stats': '课程数据分析',
  '/admin/coach-stats': '教练数据分析'
}

const currentPageTitle = computed(() => pageTitles[route.path] || '管理后台')

const handleCommand = (command: string) => {
  if (command === 'logout') {
    authStore.logout()
    router.push('/auth')
  } else if (command === 'home') {
    router.push('/')
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  height: 100vh;
  overflow: hidden;
}

.admin-layout>.el-container {
  height: 100%;
}

.admin-aside {
  background-color: #304156;
  min-height: 100vh;
  overflow-y: auto;
}

.admin-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #3a4a5c;
}

.admin-logo img {
  height: 32px;
}

.admin-menu {
  border-right: none;
}

.admin-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.admin-name {
  color: #606266;
}

.admin-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
  height: calc(100vh - 60px);
}
</style>
