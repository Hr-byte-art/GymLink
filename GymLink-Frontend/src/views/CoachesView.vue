<template>
  <AppLayout>
    <div class="coaches-container">

      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-content">
          <h1 class="page-title">专业教练团队</h1>
          <p class="page-subtitle">我们的专业教练团队将为您提供个性化的健身指导</p>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <main class="main-content">
        <!-- 教练筛选区域 -->
        <section class="filter-section">
          <div class="filter-container">
            <div class="filter-options">
              <!-- 教练类型下拉框 -->
              <div class="filter-dropdown">
                <el-select v-model="activeSpecialty" placeholder="请选择教练类型" @change="setActiveSpecialty">
                  <el-option label="全部教练" value="all"></el-option>
                  <el-option v-for="coachType in coachTypes" :key="coachType.value" :label="coachType.label"
                    :value="coachType.value">
                    <el-tooltip :content="coachType.description" placement="right">
                      <span>{{ coachType.label }}</span>
                    </el-tooltip>
                  </el-option>
                </el-select>
              </div>

              <div class="rating-filter">
                <span class="filter-label">评分：</span>
                <el-select v-model="selectedRating" placeholder="选择评分" clearable>
                  <el-option label="5.0" :value="5"></el-option>
                  <el-option label="4.0及以上" :value="4"></el-option>
                  <el-option label="3.0及以上" :value="3"></el-option>
                </el-select>
              </div>

              <div class="price-filter">
                <span class="filter-label">价格：</span>
                <el-select v-model="selectedPriceRange" placeholder="选择价格区间" clearable>
                  <el-option label="¥100以下" value="low"></el-option>
                  <el-option label="¥100-200" value="medium"></el-option>
                  <el-option label="¥200以上" value="high"></el-option>
                </el-select>
              </div>

              <div class="search-box">
                <el-input v-model="searchKeyword" placeholder="搜索教练姓名" prefix-icon="Search" clearable></el-input>
              </div>
            </div>
          </div>
        </section>

        <!-- 加载状态 -->
        <div v-if="coachStore.loading" class="loading-container">
          <el-loading :loading="true" text="加载中..."></el-loading>
        </div>

        <!-- 错误状态 -->
        <div v-else-if="coachStore.error" class="error-container">
          <el-result icon="warning" title="加载失败" :sub-title="coachStore.error">
            <template #extra>
              <el-button type="primary" @click="loadCoaches">重新加载</el-button>
            </template>
          </el-result>
        </div>

        <!-- 教练列表区域 -->
        <section v-else class="coaches-section">
          <div class="section-container">
            <!-- 无数据状态 -->
            <div v-if="!coachStore.hasCoaches" class="empty-container">
              <el-empty description="暂无教练数据"></el-empty>
            </div>

            <!-- 教练列表 -->
            <div v-else class="coaches-grid">
              <div class="coach-card" v-for="coach in coachStore.coaches" :key="coach.id"
                @click="viewCoachDetail(coach.id)">
                <div class="coach-avatar">
                  <img :src="coach.avatar" :alt="coach.name" />
                </div>
                <div class="coach-info">
                  <h3 class="coach-name">{{ coach.name }}</h3>
                  <div class="coach-specialty">{{ coach.specialty }}</div>
                  <div class="coach-rating">
                    <el-rate v-model="coach.rating" disabled show-score text-color="#ff9900"></el-rate>
                  </div>
                  <p class="coach-description">{{ coach.description }}</p>
                  <div class="coach-meta">
                    <div class="meta-item">
                      <i class="icon-experience"></i>
                      <span>{{ coach.experience }}</span>
                    </div>
                    <div class="meta-item">
                      <i class="icon-courses"></i>
                      <span>{{ coach.courses }}门课程</span>
                    </div>
                    <div class="meta-item">
                      <i class="icon-students"></i>
                      <span>{{ coach.students }}名学员</span>
                    </div>
                  </div>
                  <div class="coach-footer">
                    <div class="coach-price">
                      <span class="price-label">¥{{ coach.price }}</span>
                      <span class="price-unit">/小时</span>
                    </div>
                    <el-button type="primary" class="book-btn" @click.stop="bookCoach(coach)">预约教练</el-button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div v-if="coachStore.hasCoaches" class="pagination-container">
              <el-pagination background layout="prev, pager, next" :total="coachStore.total" :page-size="pageSize"
                :current-page="currentPage" @current-change="handlePageChange">
              </el-pagination>
            </div>
          </div>
        </section>
      </main>

    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useCoachStore } from '@/stores/coach'
import { ElMessage } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'

// 使用路由和教练状态管理
const router = useRouter()
const coachStore = useCoachStore()

// 教练类型数据
const coachTypes = [
  {
    label: '私人健身教练',
    value: 'personal',
    description: '提供一对一训练指导，涵盖体能评估、计划制定、动作纠正与基础营养建议。'
  },
  {
    label: '团体课教练',
    value: 'group',
    description: '带领多人课程，如动感单车、有氧操、搏击操、Zumba、HIIT、瑜伽、普拉提等。'
  },
  {
    label: '力量与体能教练',
    value: 'strength',
    description: '面向运动员或进阶用户，提升爆发力、速度、耐力等运动表现，常持CSCS等认证。'
  },
  {
    label: '康复/矫正训练教练',
    value: 'rehabilitation',
    description: '针对体态问题、术后恢复或慢性疼痛，进行功能性筛查与矫正性训练。'
  },
  {
    label: '营养与生活方式教练',
    value: 'nutrition',
    description: '提供体重管理、饮食建议与生活习惯指导（非医疗性质）。'
  },
  {
    label: '专项运动教练',
    value: 'specialty',
    description: '专注细分领域，如瑜伽、普拉提（含Reformer）、CrossFit、老年/孕产/青少年体适能等。'
  },
  {
    label: '线上健身教练',
    value: 'online',
    description: '通过APP、视频或社群远程提供训练计划、饮食模板与打卡监督服务。'
  }
]



// 筛选状态
const activeSpecialty = ref('all')
const selectedRating = ref<number | undefined>(undefined)
const selectedPriceRange = ref('')
const searchKeyword = ref('')

// 分页状态
const currentPage = ref(1)
const pageSize = ref(9)

// 设置活动专长
const setActiveSpecialty = (specialty: string) => {
  activeSpecialty.value = specialty
  currentPage.value = 1
  // 重新加载数据
  loadCoaches()
}



// 处理分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 查看教练详情
const viewCoachDetail = (id: number) => {
  router.push(`/coaches/${id}`)
}

// 预约教练
const bookCoach = (coach: any) => {
  ElMessage.success({ message: `预约教练 ${coach.name} 功能开发中...`, duration: 1500 })
}

// 加载教练数据
const loadCoaches = () => {
  // 构建查询参数
  const params: any = {
    page: currentPage.value,
    pageSize: pageSize.value
  }

  // 添加专长筛选
  if (activeSpecialty.value !== 'all') {
    params.specialty = activeSpecialty.value
  }

  // 添加评分筛选
  if (selectedRating.value !== undefined) {
    params.rating = selectedRating.value
  }

  // 添加价格筛选
  if (selectedPriceRange.value) {
    const priceMap: { [key: string]: { min?: number; max?: number } } = {
      'low': { max: 100 },
      'medium': { min: 100, max: 200 },
      'high': { min: 200 }
    }
    const priceRange = priceMap[selectedPriceRange.value]
    if (priceRange.min !== undefined) {
      params.priceMin = priceRange.min
    }
    if (priceRange.max !== undefined) {
      params.priceMax = priceRange.max
    }
  }

  // 添加关键词搜索
  if (searchKeyword.value) {
    params.keyword = searchKeyword.value
  }

  // 调用API获取教练数据
  coachStore.fetchCoaches(params)
}

// 加载教练专长
const loadSpecialties = () => {
  coachStore.fetchSpecialties()
}

// 监听筛选条件变化，重新加载数据
watch([activeSpecialty, selectedRating, selectedPriceRange, searchKeyword], () => {
  currentPage.value = 1
  loadCoaches()
})

// 监听页码变化，重新加载数据
watch(currentPage, () => {
  loadCoaches()
})

// 组件挂载时加载数据
onMounted(() => {
  loadSpecialties()
  loadCoaches()
})
</script>

<style scoped>
.coaches-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 页面头部样式 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

.filter-dropdown {
  position: relative;
  width: 300px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}

.rating-filter,
.price-filter {
  display: flex;
  align-items: center;
}

/* 增加下拉框宽度以改善可读性 */
.rating-filter :deep(.el-select),
.price-filter :deep(.el-select) {
  width: 200px;
}

.filter-label {
  margin-right: 8px;
  font-weight: 500;
  color: #555;
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

/* 教练区域样式 */
.coaches-section {
  padding: 60px 0;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.coaches-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 30px;
  margin-bottom: 50px;
}

.coach-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
}

.coach-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.coach-avatar {
  height: 200px;
  overflow: hidden;
}

.coach-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.coach-card:hover .coach-avatar img {
  transform: scale(1.05);
}

.coach-info {
  padding: 25px;
}

.coach-name {
  font-size: 22px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.coach-specialty {
  color: #667eea;
  font-weight: 500;
  margin-bottom: 12px;
}

.coach-rating {
  margin-bottom: 15px;
}

.coach-description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.coach-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #666;
}

.meta-item i {
  margin-right: 8px;
  color: #667eea;
}

.coach-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.coach-price {
  display: flex;
  align-items: baseline;
}

.price-label {
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
.icon-experience::before {
  content: '🏆';
}

.icon-courses::before {
  content: '📚';
}

.icon-students::before {
  content: '👥';
}

/* 响应式设计 */
@media (max-width: 992px) {
  .coaches-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 32px;
  }

  .filter-options {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    min-width: auto;
  }

  .coaches-grid {
    grid-template-columns: 1fr;
  }

  .coach-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .book-btn {
    width: 100%;
  }
}
</style>