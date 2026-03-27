<template>
  <AppLayout>
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">课程中心</h1>
        <p class="page-subtitle">探索我们丰富的健身课程，找到最适合您的训练方式</p>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 课程筛选区域 -->
      <section class="filter-section">
        <div class="filter-container">
          <div class="filter-options">
            <div class="category-filter">
              <el-select v-model="activeCategory" placeholder="请选择分类" clearable filterable>
                <el-option v-for="category in courseCategories" :key="category.value" :label="category.label"
                  :value="category.value">
                </el-option>
              </el-select>
            </div>

            <div class="difficulty-filter">
              <span class="filter-label">难度：</span>
              <el-select v-model="selectedDifficulty" placeholder="选择难度" clearable>
                <el-option label="初级" value="初级"></el-option>
                <el-option label="中级" value="中级"></el-option>
                <el-option label="高级" value="高级"></el-option>
              </el-select>
            </div>

            <div class="duration-filter">
              <span class="filter-label">时长：</span>
              <el-select v-model="selectedDuration" placeholder="选择时长范围" clearable>
                <el-option label="30分钟以下" value="0-29"></el-option>
                <el-option label="30-60分钟" value="30-59"></el-option>
                <el-option label="60-90分钟" value="60-89"></el-option>
                <el-option label="90分钟以上" value="90-"></el-option>
              </el-select>
            </div>

            <div class="search-box">
              <el-input v-model="searchKeyword" placeholder="搜索课程名称或教练" prefix-icon="Search" clearable></el-input>
            </div>

            <el-button @click="resetFilters" :icon="RefreshRight">重置</el-button>
          </div>
        </div>
      </section>

      <!-- 加载状态-->
      <div v-if="courseStore.loading" class="loading-container">
        <el-loading :loading="true" text="加载中..."></el-loading>
      </div>

      <!-- 错误状态-->
      <div v-else-if="courseStore.error" class="error-container">
        <el-result icon="warning" title="加载失败" :sub-title="courseStore.error">
          <template #extra>
            <el-button type="primary" @click="loadCourses">重新加载</el-button>
          </template>
        </el-result>
      </div>

      <!-- 课程列表区域 -->
      <section v-else class="courses-section">
        <div class="section-container">
          <!-- 无数据状态-->
          <div v-if="!courseStore.hasCourses" class="empty-container">
            <el-empty description="暂无课程数据"></el-empty>
          </div>

          <!-- 课程列表 -->
          <div v-else class="courses-grid">
            <div class="course-card" v-for="course in courseStore.courses" :key="course.id"
              @click="viewCourseDetail(course.id)">
              <div class="course-image">
                <img :src="course.image" :alt="course.name" />
                <div class="course-category">{{ course.category }}</div>
                <div class="course-difficulty" :class="course.difficulty.toLowerCase()">
                  {{ course.difficulty }}
                </div>
                <el-tag v-if="isPurchased(course.id)" class="purchased-tag" type="success" effect="dark">已购买</el-tag>
              </div>
              <div class="course-content">
                <h3 class="course-title">{{ course.name }}</h3>
                <div class="course-instructor">
                  <div class="instructor-avatar">
                    <img src="/avatar-placeholder.svg" :alt="course.name" />
                  </div>
                  <span class="instructor-name">教练ID: {{ course.coachId }}</span>
                </div>
                <p class="course-description">{{ course.description }}</p>
                <div class="course-info">
                  <div class="info-item">
                    <img src="/duration.svg" alt="时长" class="info-icon" />
                    <span>{{ course.duration }}分钟</span>
                  </div>
                  <div class="info-item">
                    <img src="/star.svg" alt="难度" class="info-icon" />
                    <span>{{ course.difficulty }}</span>
                  </div>
                  <div class="info-item">
                    <img src="/price.svg" alt="价格" class="info-icon" />
                    <span>¥{{ course.price }}/节</span>
                  </div>
                </div>
                <div class="course-footer">
                  <div class="course-price">
                    <span class="price-label">价格</span>
                    <span class="price-value">¥{{ course.price }}</span>
                    <span class="price-unit">/节</span>
                  </div>
                  <el-button v-if="isPurchased(course.id)" type="success" class="book-btn" @click.stop="viewCourseDetail(course.id)">查看详情</el-button>
                  <el-button v-else type="primary" class="book-btn" @click.stop="handlePurchase(course)">购买课程</el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="courseStore.hasCourses" class="pagination-container">
            <el-pagination background layout="prev, pager, next" :total="courseStore.total" :page-size="pageSize"
              :current-page="currentPage" @current-change="handlePageChange">
            </el-pagination>
          </div>
        </div>
      </section>
    </main>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCourseStore } from '@/stores/course'
import AppLayout from '@/components/AppLayout.vue'
import { courseTypeOptions } from '@/constants/categories'
import { RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { purchaseCourse, getPurchasedCourseIds } from '@/api/student'
import type { Course, CourseQueryParams } from '@/api/course'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 已购课程ID列表
const purchasedCourseIds = ref<number[]>([])

// 使用统一的课程分类数据，添加"全部"选项
const courseCategories = [
  { value: 'all', label: '全部课程' },
  ...courseTypeOptions
]

// 使用课程状态管理
const courseStore = useCourseStore()

// 筛选状态
const activeCategory = ref('all')
const selectedDifficulty = ref('')
const selectedDuration = ref('')
const searchKeyword = ref('')

// 分页状态
const currentPage = ref(1)
const pageSize = ref(9)

// 处理分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 查看课程详情
const viewCourseDetail = (id: string | number) => {
  router.push(`/courses/${id}`)
}

// 购买课程
const handlePurchase = async (course: Course) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录后再购买课程')
    router.push({ name: 'auth', query: { redirect: route.fullPath } })
    return
  }

  // 检查用户角色
  if (authStore.user?.role !== 'student' && authStore.user?.role !== 'user') {
    ElMessage.warning('只有学员才能购买课程')
    return
  }

  // 获取学员标识（使用关联用户标识）
  const studentId = authStore.user?.associatedUserId
  if (!studentId) {
    ElMessage.error('无法获取学员信息，请重新登录')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要购买课程「${course.name}」吗？价格：¥${course.price}`,
      '确认购买',
      { confirmButtonText: '确定购买', cancelButtonText: '取消', type: 'info' }
    )

    await purchaseCourse(studentId, course.id)
    ElMessage.success('购买成功')
    // 刷新已购课程列表
    await loadPurchasedCourseIds()
  } catch (error: unknown) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '购买失败，请稍后重试')
    }
  }
}

// 加载课程数据
const loadCourses = () => {
  // 构建查询参数（与后端课程分页查询模型对应）
  const params: Record<string, string | number> = {
    pageNum: currentPage.value,
    pageSize: pageSize.value
  }

  // 添加类别筛选（对应后端分类字段）
  if (activeCategory.value && activeCategory.value !== 'all') {
    params.type = activeCategory.value
  }

  // 添加难度筛选
  if (selectedDifficulty.value) {
    params.difficulty = selectedDifficulty.value
  }

  // 添加时长范围筛选
  if (selectedDuration.value) {
    const [min, max] = selectedDuration.value.split('-').map(Number)
    if (min) params.minDuration = min
    if (max) params.maxDuration = max
  }

  // 添加关键词搜索（对应后端名称字段）
  if (searchKeyword.value) {
    params.name = searchKeyword.value
  }

  // 调用接口获取课程数据
  courseStore.fetchCourses(params as unknown as CourseQueryParams)
}

// 重置筛选条件
const resetFilters = () => {
  activeCategory.value = 'all'
  selectedDifficulty.value = ''
  selectedDuration.value = ''
  searchKeyword.value = ''
  currentPage.value = 1
}

// 监听筛选条件变化，重新加载数据
watch([activeCategory, selectedDifficulty, selectedDuration, searchKeyword], () => {
  currentPage.value = 1
  loadCourses()
})

// 监听页码变化，重新加载数据
watch(currentPage, () => {
  loadCourses()
})

// 监听用户登录状态变化，重新加载已购课程列表
watch(() => authStore.isAuthenticated, (newVal) => {
  if (newVal) {
    loadPurchasedCourseIds()
  } else {
    purchasedCourseIds.value = []
  }
})

// 加载已购课程ID列表
const loadPurchasedCourseIds = async () => {

  if (!authStore.isAuthenticated) {
    return
  }

  // 只有学员角色才加载已购课程
  const role = authStore.user?.role
  if (role !== 'student' && role !== 'user') {
    return
  }

  const studentId = authStore.user?.associatedUserId
  if (!studentId) {
    return
  }

  try {
    purchasedCourseIds.value = await getPurchasedCourseIds(studentId)
  } catch (e) {
    console.error('获取已购课程列表失败:', e)
  }
}

// 检查课程是否已购买
const isPurchased = (courseId: number | string) => {
  // 将两边都转为字符串进行比较，避免类型不匹配问题
  const courseIdStr = String(courseId)
  return purchasedCourseIds.value.some(id => String(id) === courseIdStr)
}

// 组件挂载时加载数据
onMounted(async () => {
  loadCourses()

  // 确保用户信息已加载
  if (authStore.isAuthenticated && !authStore.user?.associatedUserId) {
    await authStore.initAuth()
  }

  await loadPurchasedCourseIds()
})

// 监听关联用户标识变化
watch(() => authStore.user?.associatedUserId, (newVal) => {
  if (newVal) {
    loadPurchasedCourseIds()
  }
})
</script>

<style scoped>
.main-content {
  --primary: #f97316;
  --primary-dark: #ea580c;
  --ink: #0f172a;
  --muted: #475569;
  --line: #e2e8f0;
  --surface: #ffffff;
  --soft: #f8fafc;
}

.page-header {
  background:
    radial-gradient(circle at 10% 20%, rgba(249, 115, 22, 0.2), transparent 40%),
    linear-gradient(135deg, #0f172a 0%, #1e293b 45%, #334155 100%);
  color: #f8fafc;
  padding: 88px 0 70px;
  text-align: center;
}

.header-content {
  max-width: 860px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  margin: 0;
  font-size: clamp(34px, 5vw, 50px);
  font-weight: 800;
  letter-spacing: 0.4px;
}

.page-subtitle {
  margin: 16px auto 0;
  max-width: 700px;
  font-size: 18px;
  line-height: 1.7;
  color: #e2e8f0;
}

.filter-section {
  position: sticky;
  top: 74px;
  z-index: 90;
  background: rgba(248, 250, 252, 0.95);
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(10px);
}

.filter-container,
.section-container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 20px;
}

.filter-container {
  padding-top: 18px;
  padding-bottom: 18px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.category-filter { width: 220px; }
.search-box { flex: 1; min-width: 220px; }

.filter-label {
  color: var(--muted);
  margin-right: 6px;
  font-weight: 600;
  font-size: 14px;
}

.category-filter :deep(.el-select),
.difficulty-filter :deep(.el-select),
.duration-filter :deep(.el-select) {
  width: 180px;
}

.category-filter :deep(.el-select) { width: 100%; }

.filter-options :deep(.el-input__wrapper),
.filter-options :deep(.el-select__wrapper) {
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  box-shadow: none;
}

.filter-options :deep(.el-input__wrapper.is-focus),
.filter-options :deep(.el-select__wrapper.is-focused) {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.16);
}

.filter-options > .el-button {
  border-radius: 10px;
  min-height: 40px;
  border-color: #fdba74;
  color: #9a3412;
  background: #fff7ed;
}

.loading-container,
.error-container,
.empty-container {
  min-height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.courses-section {
  padding: 40px 0 72px;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 22px;
  margin-bottom: 42px;
}

.course-card {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 18px;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.course-card:hover {
  transform: translateY(-5px);
  border-color: rgba(249, 115, 22, 0.42);
  box-shadow: 0 18px 32px rgba(15, 23, 42, 0.14);
}

.course-image {
  position: relative;
  height: 210px;
  overflow: hidden;
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.25s ease;
}

.course-card:hover .course-image img { transform: scale(1.04); }

.course-category,
.course-difficulty {
  position: absolute;
  top: 14px;
  font-size: 12px;
  font-weight: 700;
  padding: 5px 10px;
  border-radius: 999px;
  color: #fff;
}

.course-category {
  left: 14px;
  background: rgba(2, 6, 23, 0.66);
}

.course-difficulty {
  right: 14px;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
}

.purchased-tag {
  position: absolute;
  left: 14px;
  bottom: 14px;
}

.course-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.course-title {
  margin: 0 0 12px;
  font-size: 20px;
  color: var(--ink);
  font-weight: 800;
}

.course-instructor {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.instructor-avatar {
  width: 30px;
  height: 30px;
  margin-right: 8px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid rgba(249, 115, 22, 0.35);
}

.instructor-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.instructor-name {
  color: var(--muted);
  font-size: 14px;
  font-weight: 600;
}

.course-description {
  color: var(--muted);
  line-height: 1.65;
  margin: 0 0 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-info {
  display: grid;
  gap: 8px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  color: #64748b;
  font-size: 13px;
}

.info-item .info-icon {
  width: 14px;
  height: 14px;
  margin-right: 8px;
}

.course-footer {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.course-price {
  display: flex;
  align-items: baseline;
}

.price-label,
.price-unit {
  font-size: 13px;
  color: #64748b;
}

.price-value {
  margin: 0 4px;
  color: #b91c1c;
  font-size: 22px;
  font-weight: 800;
}

.book-btn {
  border: none;
  border-radius: 10px;
  min-height: 40px;
  padding: 0 16px;
  font-size: 14px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
}

.pagination-container {
  margin-top: 6px;
  display: flex;
  justify-content: center;
}

@media (max-width: 1024px) {
  .filter-section { position: static; }
}

@media (max-width: 768px) {
  .page-header {
    padding: 66px 0 54px;
  }

  .page-subtitle {
    font-size: 16px;
  }

  .filter-options {
    flex-direction: column;
    align-items: stretch;
  }

  .category-filter,
  .search-box { width: 100%; }

  .difficulty-filter,
  .duration-filter {
    display: flex;
    justify-content: space-between;
  }

  .difficulty-filter :deep(.el-select),
  .duration-filter :deep(.el-select) {
    width: 68%;
  }

  .courses-grid {
    grid-template-columns: 1fr;
  }

  .course-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .book-btn {
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .course-card,
  .course-image img {
    transition: none;
  }

  .course-card:hover {
    transform: none;
  }
}
</style>
