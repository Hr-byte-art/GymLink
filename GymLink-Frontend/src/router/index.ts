import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue')
    },
    // 管理员路由
    {
      path: '/admin',
      redirect: '/admin/students'
    },

    {
      path: '/admin/students',
      name: 'admin-students',
      component: () => import('../views/admin/StudentManage.vue'),
      meta: { requiresAuth: true, role: 'admin' }
    },
    {
      path: '/admin/coaches',
      name: 'admin-coaches',
      component: () => import('../views/admin/CoachManage.vue'),
      meta: { requiresAuth: true, role: 'admin' }
    },
    {
      path: '/admin/courses',
      name: 'admin-courses',
      component: () => import('../views/admin/CourseManage.vue'),
      meta: { requiresAuth: true, role: 'admin' }
    },
    {
      path: '/admin/equipment',
      name: 'admin-equipment',
      component: () => import('../views/admin/EquipmentManage.vue'),
      meta: { requiresAuth: true, role: 'admin' }
    },
    {
      path: '/admin/recipes',
      name: 'admin-recipes',
      component: () => import('../views/admin/RecipeManage.vue'),
      meta: { requiresAuth: true, role: 'admin' }
    },
    {
      path: '/admin/posts',
      name: 'admin-posts',
      component: () => import('../views/admin/PostManage.vue'),
      meta: { requiresAuth: true, role: 'admin' }
    },
    {
      path: '/admin/equipment-stats',
      name: 'admin-equipment-stats',
      component: () => import('../views/admin/EquipmentStatistics.vue'),
      meta: { requiresAuth: true, role: 'admin' }
    },
    {
      path: '/admin/course-stats',
      name: 'admin-course-stats',
      component: () => import('../views/admin/CourseStatistics.vue'),
      meta: { requiresAuth: true, role: 'admin' }
    },
    {
      path: '/admin/coach-stats',
      name: 'admin-coach-stats',
      component: () => import('../views/admin/CoachStatistics.vue'),
      meta: { requiresAuth: true, role: 'admin' }
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
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue')
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