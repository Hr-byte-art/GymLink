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
                <el-option label="30分钟以下" value="0-30"></el-option>
                <el-option label="30-60分钟" value="30-60"></el-option>
                <el-option label="60-90分钟" value="60-90"></el-option>
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

      <!-- 加载状态 -->
      <div v-if="courseStore.loading" class="loading-container">
        <el-loading :loading="true" text="加载中..."></el-loading>
      </div>

      <!-- 错误状态 -->
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
          <!-- 无数据状态 -->
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
                    <img :src="course.instructorAvatar || '/avatar-placeholder.svg'" :alt="course.coachName" />
                  </div>
                  <span class="instructor-name">{{ course.coachName || '未知教练' }}</span>
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
            <el-pagination background layout="prev, pager, next" :total="courseStore.total" :page-size="pageSize.value"
              :current-page="currentPage.value" @current-change="handlePageChange">
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
import NavBar from '@/components/NavBar.vue'
import AppLayout from '@/components/AppLayout.vue'
import { courseTypeOptions } from '@/constants/categories'
import { RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { purchaseCourse, getPurchasedCourseIds } from '@/api/student'

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
const handlePurchase = async (course: any) => {
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

  // 获取学员ID（使用 associatedUserId）
  const studentId = authStore.user?.associatedUserId
  if (!studentId) {
    ElMessage.error('无法获取学员信息，请重新登录')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要购买课程「${course.name || course.title}」吗？价格：¥${course.price}`,
      '确认购买',
      { confirmButtonText: '确定购买', cancelButtonText: '取消', type: 'info' }
    )

    await purchaseCourse(studentId, course.id)
    ElMessage.success('购买成功！')
    // 刷新已购课程列表
    await loadPurchasedCourseIds()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '购买失败，请稍后重试')
    }
  }
}

// 加载课程数据
const loadCourses = () => {
  // 构建查询参数（与后端 CourseQueryPageRequest 对应）
  const params: any = {
    pageNum: currentPage.value,
    pageSize: pageSize.value
  }

  // 添加类别筛选（后端字段是 type）
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

  // 添加关键词搜索（后端字段是 name）
  if (searchKeyword.value) {
    params.name = searchKeyword.value
  }

  // 调用API获取课程数据
  courseStore.fetchCourses(params)
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
  console.log('loadPurchasedCourseIds - isAuthenticated:', authStore.isAuthenticated)
  console.log('loadPurchasedCourseIds - user:', authStore.user)
  console.log('loadPurchasedCourseIds - role:', authStore.user?.role)
  console.log('loadPurchasedCourseIds - associatedUserId:', authStore.user?.associatedUserId)
  
  if (!authStore.isAuthenticated) {
    console.log('用户未登录，跳过加载已购课程')
    return
  }
  
  // 只有学员角色才加载已购课程
  const role = authStore.user?.role
  if (role !== 'student' && role !== 'user') {
    console.log('非学员角色，跳过加载已购课程')
    return
  }
  
  const studentId = authStore.user?.associatedUserId
  if (!studentId) {
    console.log('associatedUserId为空，跳过加载已购课程')
    return
  }
  
  try {
    console.log('正在调用 getPurchasedCourseIds，studentId:', studentId)
    purchasedCourseIds.value = await getPurchasedCourseIds(studentId)
    console.log('已购课程ID列表:', purchasedCourseIds.value)
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
    console.log('等待用户信息加载...')
    await authStore.initAuth()
  }
  
  await loadPurchasedCourseIds()
})

// 监听 associatedUserId 变化
watch(() => authStore.user?.associatedUserId, (newVal) => {
  if (newVal) {
    console.log('associatedUserId 已更新:', newVal)
    loadPurchasedCourseIds()
  }
})
</script>

<style scoped>
.courses-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 页面头部样式 */
.page-header {
  background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
  color: white;
  padding: 80px 0 60px;
  text-align: center;
}

.header-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 20px;
}

.page-subtitle {
  font-size: 18px;
  line-height: 1.6;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto;
}

/* 主要内容样式 */
.main-content {
  flex: 1;
}

/* 筛选区域样式 */
.filter-section {
  background: #f8f9fa;
  padding: 30px 0;
  border-bottom: 1px solid #e9ecef;
}

.filter-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}

.category-filter {
  width: 250px;
}

.category-filter :deep(.el-select) {
  width: 100%;
}

.difficulty-filter,
.duration-filter {
  display: flex;
  align-items: center;
}

.filter-label {
  margin-right: 8px;
  font-weight: 500;
  color: #555;
}

/* 增加下拉框宽度以改善可读性 */
.difficulty-filter :deep(.el-select) {
  width: 200px;
}

/* 时长筛选下拉框宽度 */
.duration-filter :deep(.el-select) {
  width: 200px;
}

.search-box {
  flex: 1;
  min-width: 250px;
}

/* 加载和错误状态样式 */
.loading-container,
.error-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

/* 课程区域样式 */
.courses-section {
  padding: 60px 0;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 30px;
  margin-bottom: 50px;
}

.course-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.course-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.course-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.course-card:hover .course-image img {
  transform: scale(1.05);
}

.course-category {
  position: absolute;
  top: 15px;
  left: 15px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.course-difficulty {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.course-difficulty.初级 {
  background: #67c23a;
  color: white;
}

.course-difficulty.中级 {
  background: #e6a23c;
  color: white;
}

.course-difficulty.高级 {
  background: #f56c6c;
  color: white;
}

.purchased-tag {
  position: absolute;
  bottom: 15px;
  left: 15px;
  font-size: 12px;
}

.course-content {
  padding: 25px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.course-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 15px;
}

.course-instructor {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.instructor-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 10px;
}

.instructor-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.instructor-name {
  font-size: 14px;
  color: #666;
}

.course-description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #666;
}

.info-item .info-icon {
  width: 16px;
  height: 16px;
  margin-right: 8px;
  filter: invert(45%) sepia(98%) saturate(1500%) hue-rotate(196deg) brightness(100%) contrast(96%);
}

.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.course-price {
  display: flex;
  align-items: baseline;
}

.price-label {
  font-size: 14px;
  color: #666;
  margin-right: 5px;
}

.price-value {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.price-unit {
  font-size: 14px;
  color: #666;
  margin-left: 2px;
}

.book-btn {
  background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
  padding: 8px 20px;
  font-weight: 500;
  font-size: 16px;
}

/* 分页样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .courses-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 32px;
  }

  .filter-tabs {
    flex-wrap: nowrap;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .filter-options {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    min-width: auto;
  }

  .courses-grid {
    grid-template-columns: 1fr;
  }

  .course-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .book-btn {
    width: 100%;
  }
}
</style>