<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import AiChatBot from '@/components/AiChatBot.vue'

const authStore = useAuthStore()

// 在应用启动时初始化认证状态
onMounted(() => {
  authStore.initAuth()
})
</script>

<template>
  <RouterView />
  <!-- AI 聊天机器人 -->
  <AiChatBot />
</template>

<style scoped></style>
