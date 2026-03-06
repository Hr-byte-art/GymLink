<template>
  <el-popover placement="bottom" :width="360" trigger="click" @show="loadNotifications">
    <template #reference>
      <button class="notification-bell" type="button" aria-label="notifications">
        <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99">
          <el-icon :size="20"><Bell /></el-icon>
        </el-badge>
        <el-icon v-else :size="20"><Bell /></el-icon>
      </button>
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
              <el-icon v-if="item.type === 1" color="#f97316"><Calendar /></el-icon>
              <el-icon v-else color="#22c55e"><Bell /></el-icon>
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

const getAuthUserId = (): number | null => {
  const rawId = authStore.user?.id
  if (rawId === undefined || rawId === null) return null
  const userId = Number(rawId)
  return Number.isFinite(userId) ? userId : null
}

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

const loadUnreadCount = async () => {
  const userId = getAuthUserId()
  if (!authStore.isAuthenticated || userId === null) return
  try {
    unreadCount.value = await getUnreadCount(userId)
  } catch (e) {
    console.error('获取未读数量失败:', e)
  }
}

const loadNotifications = async () => {
  const userId = getAuthUserId()
  if (!authStore.isAuthenticated || userId === null) return
  loading.value = true
  try {
    const res = await getNotificationList(userId, 1, 10)
    notifications.value = res.records || []
  } catch (e) {
    console.error('获取通知列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleClickNotification = async (item: Notification) => {
  if (item.isRead === 0) {
    await markAsRead(item.id)
    item.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }

  if (item.type === 1) {
    const role = authStore.user?.role
    if (role === 'coach') {
      router.push({ path: '/profile', query: { tab: 'coach-appointments' } })
    } else {
      router.push({ path: '/profile', query: { tab: 'appointments' } })
    }
  } else {
    router.push({ path: '/profile', query: { tab: 'notifications' } })
  }
}

const handleMarkAllRead = async () => {
  const userId = getAuthUserId()
  if (userId === null) return
  await markAllAsRead(userId)
  notifications.value.forEach(n => n.isRead = 1)
  unreadCount.value = 0
}

const goToNotificationCenter = () => {
  router.push({ path: '/profile', query: { tab: 'notifications' } })
}

const startPolling = () => {
  loadUnreadCount()
  pollTimer = window.setInterval(loadUnreadCount, 30000)
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
  width: 40px;
  height: 40px;
  border: 1px solid #e2e8f0;
  border-radius: 50%;
  background: #ffffff;
  color: #334155;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.notification-bell:hover {
  color: #c2410c;
  border-color: #fdba74;
  background: #fff7ed;
}

.notification-panel {
  margin: -12px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
}

.notification-header .title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.notification-list {
  max-height: 360px;
  overflow-y: auto;
}

.notification-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.notification-item:hover {
  background: #f8fafc;
}

.notification-item.unread {
  background: #fff7ed;
}

.notification-icon {
  margin-right: 10px;
  margin-top: 2px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  margin-bottom: 4px;
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}

.notification-text {
  color: #475569;
  font-size: 13px;
  line-height: 1.45;
  white-space: pre-wrap;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-time {
  margin-top: 4px;
  color: #94a3b8;
  font-size: 12px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-left: 8px;
  margin-top: 6px;
  background: #ef4444;
  flex-shrink: 0;
}

.notification-footer {
  padding: 10px 16px;
  border-top: 1px solid #e2e8f0;
  text-align: center;
}
</style>
