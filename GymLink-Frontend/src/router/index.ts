import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue')
    },
    {
      path: '/courses',
      name: 'courses',
      component: () => import('../views/CoursesView.vue')
    },
    {
      path: '/coaches',
      name: 'coaches',
      component: () => import('../views/CoachesView.vue')
    },
    {
      path: '/coaches/:id',
      name: 'coach-detail',
      component: () => import('../views/CoachDetailView.vue')
    },
    {
      path: '/equipment',
      name: 'equipment',
      component: () => import('../views/EquipmentView.vue')
    },
    {
      path: '/equipment/:id',
      name: 'equipment-detail',
      component: () => import('../views/EquipmentDetailView.vue')
    },
    {
      path: '/recipes',
      name: 'recipes',
      component: () => import('../views/RecipesView.vue')
    },
    {
      path: '/recipes/:id',
      name: 'recipe-detail',
      component: () => import('../views/RecipeDetailView.vue')
    },
    {
      path: '/posts',
      name: 'posts',
      component: () => import('../views/PostsView.vue')
    },
    {
      path: '/posts/:id',
      name: 'post-detail',
      component: () => import('../views/PostDetailView.vue')
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
