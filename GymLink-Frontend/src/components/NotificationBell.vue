<template>
  <el-popover placement="bottom" :width="360" trigger="click" @show="loadNotifications">
    <template #reference>
      <div class="notification-bell">
        <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99">
          <el-icon :size="22"><Bell /></el-icon>
        </el-badge>
        <el-icon v-else :size="22"><Bell /></el-icon>
      </div>
    </template>
    <div class="notification-panel">
      <div class="notification-header">
        <span class="title">消息通知</span>
        <el-button v-if="unreadCount > 0" link type="primary" @click="handleMarkAllRead">全部已读</el-button>
      </div>
      <div class="notification-list" v-loading="loading">
        <div v-if="notifications.length > 0">
          <div
            v-for="item in notifications"
            :key="item.id"
            class="notification-item"
            :class="{ unread: item.isRead === 0 }"
            @click="handleClickNotification(item)"
          >
            <div class="notification-icon">
              <el-icon v-if="item.type === 1" color="#409eff"><Calendar /></el-icon>
              <el-icon v-else color="#67c23a"><Bell /></el-icon>
            </div>
            <div class="notification-content">
              <div class="notification-title">{{ item.title }}</div>
              <div class="notification-text">{{ item.content }}</div>
              <div class="notification-time">{{ formatTime(item.createTime) }}</div>
            </div>
            <div v-if="item.isRead === 0" class="unread-dot"></div>
          </div>
        </div>
        <el-empty v-else description="暂无通知" :image-size="60" />
      </div>
      <div class="notification-footer" v-if="notifications.length > 0">
        <el-button link type="primary" @click="goToNotificationCenter">查看全部</el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Calendar } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getNotificationList, getUnreadCount, markAsRead, markAllAsRead, type Notification } from '@/api/notification'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const unreadCount = ref(0)
const notifications = ref<Notification[]>([])
let pollTimer: number | null = null

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

// 加载未读数量
const loadUnreadCount = async () => {
  if (!authStore.isAuthenticated || !authStore.user?.id) return
  try {
    unreadCount.value = await getUnreadCount(authStore.user.id)
  } catch (e) {
    console.error('获取未读数量失败:', e)
  }
}

// 加载通知列表
const loadNotifications = async () => {
  if (!authStore.isAuthenticated || !authStore.user?.id) return
  loading.value = true
  try {
    const res = await getNotificationList(authStore.user.id, 1, 10)
    notifications.value = res.records || []
  } catch (e) {
    console.error('获取通知列表失败:', e)
  } finally {
    loading.value = false
  }
}

// 点击通知
const handleClickNotification = async (item: Notification) => {
  // 标记为已读
  if (item.isRead === 0) {
    await markAsRead(item.id)
    item.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  
  // 根据通知类型跳转到对应页面
  if (item.type === 1) {
    // 预约通知 - 跳转到个人中心的预约页面
    const role = authStore.user?.role
    if (role === 'coach') {
      // 教练跳转到预约管理
      router.push({ path: '/profile', query: { tab: 'coach-appointments' } })
    } else {
      // 学员跳转到我的预约
      router.push({ path: '/profile', query: { tab: 'appointments' } })
    }
  } else {
    // 其他通知 - 跳转到消息通知页面
    router.push({ path: '/profile', query: { tab: 'notifications' } })
  }
}

// 全部已读
const handleMarkAllRead = async () => {
  if (!authStore.user?.id) return
  await markAllAsRead(authStore.user.id)
  notifications.value.forEach(n => n.isRead = 1)
  unreadCount.value = 0
}

// 跳转到通知中心
const goToNotificationCenter = () => {
  router.push('/profile')
  // 可以通过 query 参数指定打开通知 tab
}

// 定时轮询未读数量
const startPolling = () => {
  loadUnreadCount()
  pollTimer = window.setInterval(loadUnreadCount, 30000) // 30秒轮询一次
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(() => {
  if (authStore.isAuthenticated) {
    startPolling()
  }
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.notification-bell {
  cursor: pointer;
  padding: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
  color: #606266;
}

.notification-bell:hover {
  background: rgba(74, 108, 247, 0.1);
  color: #4a6cf7;
}

.notification-panel {
  margin: -12px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.notification-header .title {
  font-weight: 600;
  font-size: 15px;
}

.notification-list {
  max-height: 360px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
}

.notification-item:hover {
  background: #f5f7fa;
}

.notification-item.unread {
  background: #ecf5ff;
}

.notification-icon {
  margin-right: 12px;
  margin-top: 2px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-weight: 500;
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.notification-text {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  white-space: pre-wrap;
}

.notification-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #f56c6c;
  border-radius: 50%;
  margin-left: 8px;
  margin-top: 6px;
  flex-shrink: 0;
}

.notification-footer {
  padding: 10px 16px;
  text-align: center;
  border-top: 1px solid #ebeef5;
}
</style>
