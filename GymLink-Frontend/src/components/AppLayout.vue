<template>
  <div class="app-layout">
    <NavBar />
    <main class="main-content">
      <slot></slot>
    </main>
    <Footer />
  </div>
</template>

<script setup lang="ts">
import NavBar from '@/components/NavBar.vue'
import Footer from '@/components/Footer.vue'
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}
</style>