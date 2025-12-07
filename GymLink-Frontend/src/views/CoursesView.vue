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

            <div class="time-filter">
              <span class="filter-label">时间：</span>
              <el-select v-model="selectedTime" placeholder="选择时间段" clearable>
                <el-option label="早晨 (6:00-9:00)" value="morning"></el-option>
                <el-option label="上午 (9:00-12:00)" value="forenoon"></el-option>
                <el-option label="下午 (12:00-18:00)" value="afternoon"></el-option>
                <el-option label="晚上 (18:00-22:00)" value="evening"></el-option>
              </el-select>
            </div>

            <div class="search-box">
              <el-input v-model="searchKeyword" placeholder="搜索课程名称或教练" prefix-icon="Search" clearable></el-input>
            </div>
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
            <div class="course-card" v-for="course in courseStore.courses" :key="course.id">
              <div class="course-image">
                <img :src="course.image" :alt="course.title" />
                <div class="course-category">{{ course.category }}</div>
                <div class="course-difficulty" :class="course.difficulty.toLowerCase()">
                  {{ course.difficulty }}
                </div>
              </div>
              <div class="course-content">
                <h3 class="course-title">{{ course.title }}</h3>
                <div class="course-instructor">
                  <div class="instructor-avatar">
                    <img :src="course.instructorAvatar" :alt="course.instructor" />
                  </div>
                  <span class="instructor-name">{{ course.instructor }}</span>
                </div>
                <p class="course-description">{{ course.description }}</p>
                <div class="course-info">
                  <div class="info-item">
                    <i class="icon-time"></i>
                    <span>{{ course.time }}</span>
                  </div>
                  <div class="info-item">
                    <i class="icon-duration"></i>
                    <span>{{ course.duration }}</span>
                  </div>
                  <div class="info-item">
                    <i class="icon-location"></i>
                    <span>{{ course.location }}</span>
                  </div>
                </div>
                <div class="course-footer">
                  <div class="course-price">
                    <span class="price-label">价格</span>
                    <span class="price-value">¥{{ course.price }}</span>
                    <span class="price-unit">/节</span>
                  </div>
                  <el-button type="primary" class="book-btn">预约课程</el-button>
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
import { useCourseStore } from '@/stores/course'
import NavBar from '@/components/NavBar.vue'
import AppLayout from '@/components/AppLayout.vue'

// 课程分类数据数组
const courseCategories = [
  { value: 'all', label: '全部课程' },
  { value: '私教课程', label: '私教课程' },
  { value: '团体训练课程', label: '团体训练课程' },
  { value: '功能性训练课程', label: '功能性训练课程' },
  { value: '力量训练课程', label: '力量训练课程' },
  { value: '瑜伽课程', label: '瑜伽课程' },
  { value: '普拉提课程', label: '普拉提课程' },
  { value: '康复/矫正训练课程', label: '康复/矫正训练课程' },
  { value: '专项运动表现课程', label: '专项运动表现课程' },
  { value: '孕产/产后修复课程', label: '孕产/产后修复课程' },
  { value: '老年/青少年体适能课程', label: '老年/青少年体适能课程' },
  { value: '线上直播/录播课程', label: '线上直播/录播课程' }
]

// 使用课程状态管理
const courseStore = useCourseStore()

// 筛选状态
const activeCategory = ref('all')
const selectedDifficulty = ref('')
const selectedTime = ref('')
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

// 加载课程数据
const loadCourses = () => {
  // 构建查询参数
  const params: any = {
    page: currentPage.value,
    size: pageSize.value
  }

  // 添加类别筛选
  if (activeCategory.value !== 'all') {
    params.category = activeCategory.value
  }

  // 添加难度筛选
  if (selectedDifficulty.value) {
    params.difficulty = selectedDifficulty.value
  }

  // 添加时间段筛选
  if (selectedTime.value) {
    params.timeSlot = selectedTime.value
  }

  // 添加关键词搜索
  if (searchKeyword.value) {
    params.keyword = searchKeyword.value
  }

  // 调用API获取课程数据
  courseStore.fetchCourses(params)
}

// 监听筛选条件变化，重新加载数据
watch([activeCategory, selectedDifficulty, selectedTime, searchKeyword], () => {
  currentPage.value = 1
  loadCourses()
})

// 监听页码变化，重新加载数据
watch(currentPage, () => {
  loadCourses()
})

// 组件挂载时加载数据
onMounted(() => {
  loadCourses()
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
.time-filter {
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

/* 进一步增加时间下拉框宽度以显示完整信息 */
.time-filter :deep(.el-select) {
  width: 250px;
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

.course-content {
  padding: 25px;
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

.info-item i {
  margin-right: 8px;
  color: #409eff;
}

.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  border: none;
  padding: 8px 20px;
  font-weight: 500;
}

/* 分页样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

/* 图标样式 */
.icon-time::before {
  content: '⏰';
}

.icon-duration::before {
  content: '⏱️';
}

.icon-location::before {
  content: '📍';
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