<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 在应用启动时初始化认证状态
onMounted(() => {
  authStore.initAuth()
})
</script>

<template>
  <RouterView />
</template>

<style scoped></style>
