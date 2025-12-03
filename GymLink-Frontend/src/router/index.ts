import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/auth'
    },
    {
      path: '/auth',
      name: 'auth',
      component: () => import('../views/AuthView.vue')
    },
    // 保留旧路由以兼容
    {
      path: '/login',
      redirect: '/auth'
    },
    {
      path: '/register',
      redirect: { path: '/auth', query: { type: 'register' } }
    }
  ],
})

export default router
