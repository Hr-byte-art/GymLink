<template>
  <AppLayout>
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <el-result icon="warning" title="加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="loadCourseDetail">重新加载</el-button>
          <el-button @click="goBack">返回列表</el-button>
        </template>
      </el-result>
    </div>

    <!-- 课程详情内容 -->
    <div v-else-if="course" class="course-detail">
      <!-- 返回按钮 -->
      <div class="back-button">
        <el-button @click="goBack" :icon="ArrowLeft">返回课程列表</el-button>
      </div>

      <!-- 课程头部信息 -->
      <div class="course-header">
        <div class="course-image">
          <img :src="course.image || '/course1.svg'" :alt="course.name" />
          <div class="course-difficulty" :class="getDifficultyClass(course.difficulty)">
            {{ course.difficulty }}
          </div>
          <el-tag v-if="isPurchased" class="purchased-tag" type="success" effect="dark" size="large">已购买</el-tag>
        </div>
        <div class="course-info">
          <div class="course-category">
            <el-tag type="primary">{{ getCourseTypeName(course.type || '') }}</el-tag>
          </div>
          <h1 class="course-title">{{ course.name }}</h1>

          <!-- 教练信息 -->
          <div class="coach-info" v-if="course.coachName">
            <el-avatar :size="40" :src="course.coachAvatar || '/avatar-placeholder.svg'" />
            <div class="coach-detail">
              <span class="coach-name">{{ course.coachName }}</span>
              <span class="coach-label">授课教练</span>
            </div>
          </div>

          <!-- 课程基本信息 -->
          <div class="info-cards">
            <div class="info-card">
              <img src="/duration.svg" alt="时长" class="info-icon" />
              <div class="info-content">
                <div class="info-value">{{ course.duration }}</div>
                <div class="info-label">时长(分钟)</div>
              </div>
            </div>
            <div class="info-card">
              <img src="/star.svg" alt="难度" class="info-icon" />
              <div class="info-content">
                <div class="info-value">{{ course.difficulty }}</div>
                <div class="info-label">难度等级</div>
              </div>
            </div>
            <div class="info-card">
              <img src="/price.svg" alt="价格" class="info-icon" />
              <div class="info-content">
                <div class="info-value">¥{{ course.price }}</div>
                <div class="info-label">课程价格</div>
              </div>
            </div>
          </div>

          <!-- 购买按钮 -->
          <div class="action-buttons">
            <el-button v-if="isPurchased" type="success" size="large" class="book-btn purchased-btn" disabled>
              <el-icon style="margin-right: 5px;"><Check /></el-icon>
              课程已购买
            </el-button>
            <el-button v-else type="primary" size="large" class="book-btn" @click="handlePurchase" :loading="purchasing">
              立即购买
            </el-button>
            <el-button size="large" @click="showCoachDialog">联系教练</el-button>
            <el-button size="large" @click="handleToggleFavorite">
              {{ isFavorite ? '❤️ 已收藏' : '🤍 收藏' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 课程详情 -->
      <div class="course-content">
        <el-card class="detail-card">
          <template #header>
            <span class="card-title">课程介绍</span>
          </template>
          <div class="description-content">
            {{ course.description || '暂无课程介绍' }}
          </div>
        </el-card>

        <!-- 用户评价 -->
        <el-card class="detail-card reviews-card">
          <template #header>
            <div class="reviews-header">
              <span class="card-title">用户评价</span>
            </div>
          </template>
          <div v-loading="reviewLoading" class="reviews-content">
            <!-- 评价统计 -->
            <div class="review-summary" v-if="courseStats && courseStats.reviewCount > 0">
              <div class="rating-overview">
                <div class="rating-score">{{ courseStats.avgRating }}</div>
                <div class="rating-stars">
                  <el-rate v-model="courseStats.avgRating" disabled></el-rate>
                </div>
                <div class="rating-count">{{ courseStats.reviewCount }}条评价</div>
              </div>
              <div class="rating-distribution">
                <div v-for="star in 5" :key="star" class="rating-bar">
                  <div class="bar-label">{{ 6 - star }}星</div>
                  <div class="bar-container">
                    <div class="bar-fill" :style="{ width: getRatingPercentage(6 - star) + '%' }"></div>
                  </div>
                  <div class="bar-count">{{ getRatingCount(6 - star) }}</div>
                </div>
              </div>
            </div>

            <!-- 评价列表 -->
            <div v-if="reviews.length > 0" class="reviews-list">
              <div v-for="review in reviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <div class="reviewer-info">
                    <div class="reviewer-avatar">
                      <img :src="review.studentAvatar || '/avatar-placeholder.svg'" :alt="review.studentName" />
                    </div>
                    <div class="reviewer-details">
                      <div class="reviewer-name">{{ review.studentName || '匿名用户' }}</div>
                    </div>
                  </div>
                  <div class="review-rating">
                    <el-rate v-model="review.rating" disabled></el-rate>
                    <span class="review-date">{{ formatDate(review.createTime) }}</span>
                  </div>
                </div>
                <div class="review-text" v-if="review.content">{{ review.content }}</div>
                <div class="review-text empty" v-else>用户未填写评价内容</div>
              </div>
            </div>
            <el-empty v-else description="暂无评价" />

            <!-- 分页 -->
            <div v-if="reviewTotal > reviewPageSize" class="reviews-pagination">
              <el-pagination
                background
                layout="prev, pager, next"
                :total="reviewTotal"
                :page-size="reviewPageSize"
                :current-page="reviewPage"
                @current-change="handleReviewPageChange"
              />
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <div v-else class="error-container">
      <el-result icon="info" title="课程信息暂时无法显示" sub-title="请刷新页面重试，或返回课程列表重新进入。">
        <template #extra>
          <el-button type="primary" @click="loadCourseDetail">重新加载</el-button>
          <el-button @click="goBack">返回列表</el-button>
        </template>
      </el-result>
    </div>

    <!-- 教练信息弹窗 -->
    <el-dialog v-model="coachDialogVisible" title="教练信息" width="400px" center>
      <div v-if="coachLoading" class="coach-dialog-loading">
        <el-skeleton :rows="4" animated />
      </div>
      <div v-else-if="coachInfo" class="coach-dialog-content">
        <div class="coach-avatar-section">
          <el-avatar :size="80" :src="coachInfo.avatar || '/avatar-placeholder.svg'" />
          <h3 class="coach-dialog-name">{{ coachInfo.name }}</h3>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="性别">{{ getGenderName(coachInfo.gender) }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ coachInfo.age }}岁</el-descriptions-item>
          <el-descriptions-item label="联系电话">
            <el-link type="primary" :href="'tel:' + coachInfo.phone">{{ coachInfo.phone }}</el-link>
          </el-descriptions-item>
          <el-descriptions-item label="专业方向">{{ getCoachSpecialtyName(coachInfo.specialty) }}</el-descriptions-item>
          <el-descriptions-item label="个人简介">{{ coachInfo.intro || '暂无简介' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else class="coach-dialog-error">
        <el-empty description="无法获取教练信息" />
      </div>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'
import { getCourseTypeName, getCoachSpecialtyName, getGenderName } from '@/constants/categories'
import { useAuthStore } from '@/stores/auth'
import { purchaseCourse, getPurchasedCourseIds } from '@/api/student'
import { getCoachDetail, type Coach } from '@/api/coach'
import { getCourseDetail } from '@/api/course'
import { toggleFavorite as toggleFavoriteApi, checkFavorite, FavoriteType } from '@/api/favorite'
import { getReviewList, getCourseReviewStats, type CourseReview, type CourseReviewStats } from '@/api/review'

type CourseDetail = {
  id: number | string
  name?: string
  image?: string
  difficulty?: string
  type?: string
  coachId?: number | string
  coachName?: string
  coachAvatar?: string
  duration?: number
  price?: number
  description?: string
}

const getErrorMessage = (err: unknown, fallback: string) => {
  if (err && typeof err === 'object' && 'message' in err) {
    const message = (err as { message?: unknown }).message
    if (typeof message === 'string' && message) return message
  }
  return fallback
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const course = ref<CourseDetail | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const purchasing = ref(false)

// 已购买状态
const isPurchased = ref(false)

// 教练信息弹窗
const coachDialogVisible = ref(false)
const coachLoading = ref(false)
const coachInfo = ref<Coach | null>(null)

// 收藏状态
const isFavorite = ref(false)

// 评价相关
const reviews = ref<CourseReview[]>([])
const reviewLoading = ref(false)
const reviewTotal = ref(0)
const reviewPage = ref(1)
const reviewPageSize = 5
const courseStats = ref<CourseReviewStats | null>(null)

// 获取难度样式类
const getDifficultyClass = (difficulty?: string) => {
  const map: Record<string, string> = {
    '初级': 'easy',
    '中级': 'medium',
    '高级': 'hard'
  }
  return map[difficulty || ''] || 'easy'
}

// 返回列表
const goBack = () => {
  router.push('/courses')
}

// 加载课程详情
const loadCourseDetail = async () => {
  const id = Number(route.params.id)
  if (!Number.isFinite(id)) {
    course.value = null
    error.value = '课程ID无效'
    return
  }

  loading.value = true
  error.value = null

  try {
    const detail = await getCourseDetail(id)
    if (!detail) {
      course.value = null
      error.value = '课程不存在或已下架'
      return
    }

    course.value = detail as unknown as CourseDetail
    // 检查是否已购买
    await checkPurchaseStatus()
  } catch (e: unknown) {
    course.value = null
    error.value = getErrorMessage(e, '获取课程详情失败')
  } finally {
    loading.value = false
  }
}

// 检查课程是否已购买
const checkPurchaseStatus = async () => {

  if (!authStore.isAuthenticated) {
    isPurchased.value = false
    return
  }

  // 只有学员角色才检查购买状态
  const role = authStore.user?.role
  if (role !== 'student' && role !== 'user') {
    isPurchased.value = false
    return
  }

  const studentId = authStore.user?.associatedUserId
  if (!studentId || !course.value?.id) {
    isPurchased.value = false
    return
  }

  try {
    const purchasedIds = await getPurchasedCourseIds(studentId)
    // 将两边都转为字符串进行比较，避免类型不匹配问题
    const courseIdStr = String(course.value.id)
    isPurchased.value = purchasedIds.some(id => String(id) === courseIdStr)
  } catch (e) {
    console.error('检查购买状态失败:', e)
    isPurchased.value = false
  }
}

// 购买课程
const handlePurchase = async () => {
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

  if (!course.value) {
    ElMessage.warning('课程信息不存在')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要购买课程「${course.value.name}」吗？价格：¥${course.value.price}`,
      '确认购买',
      { confirmButtonText: '确定购买', cancelButtonText: '取消', type: 'info' }
    )

    purchasing.value = true
    await purchaseCourse(studentId, course.value.id)
    ElMessage.success('购买成功！')
    // 更新购买状态
    isPurchased.value = true
  } catch (e: unknown) {
    if (e !== 'cancel') {
      ElMessage.error(getErrorMessage(e, '购买失败，请稍后重试'))
    }
  } finally {
    purchasing.value = false
  }
}

// 显示教练信息弹窗
const showCoachDialog = async () => {
  if (!course.value?.coachId) {
    ElMessage.warning('该课程暂无关联教练')
    return
  }

  coachDialogVisible.value = true
  coachLoading.value = true
  coachInfo.value = null

  try {
    coachInfo.value = await getCoachDetail(course.value.coachId)
  } catch {
    ElMessage.error('获取教练信息失败')
  } finally {
    coachLoading.value = false
  }
}

// 切换收藏状态
const handleToggleFavorite = async () => {
  try {
    const res = await toggleFavoriteApi({
      targetId: Number(route.params.id),
      type: FavoriteType.COURSE
    })
    // 响应拦截器已解包，可直接作为布尔结果使用
    isFavorite.value = res as unknown as boolean
    ElMessage.success(isFavorite.value ? '已添加到收藏' : '已取消收藏')
  } catch {
    ElMessage.error('操作失败，请先登录')
  }
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  if (!authStore.isAuthenticated) {
    isFavorite.value = false
    return
  }

  try {
    const res = await checkFavorite(Number(route.params.id), FavoriteType.COURSE)
    isFavorite.value = res.data
  } catch {
    // 未登录时忽略错误
  }
}

// 加载评价列表
const loadReviews = async () => {
  const courseId = route.params.id as string
  if (!courseId) return

  reviewLoading.value = true
  try {
    const res = await getReviewList({
      courseId: Number(courseId),
      pageNum: reviewPage.value,
      pageSize: reviewPageSize
    })
    reviews.value = res.records || []
    reviewTotal.value = res.total || 0
  } catch (e) {
    console.error('获取评价列表失败:', e)
  } finally {
    reviewLoading.value = false
  }
}

// 评价分页变化
const handleReviewPageChange = (page: number) => {
  reviewPage.value = page
  loadReviews()
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 加载课程评价统计
const loadCourseStats = async () => {
  const courseId = route.params.id as string
  if (!courseId) return
  try {
    courseStats.value = await getCourseReviewStats(courseId)
  } catch (e) {
    console.error('获取课程评价统计失败:', e)
  }
}

// 获取评分百分比
const getRatingPercentage = (star: number) => {
  if (!courseStats.value || !courseStats.value.ratingDistribution) {
    return 0
  }
  const count = courseStats.value.ratingDistribution[star] || 0
  const total = courseStats.value.reviewCount || 1
  return Math.round((count / total) * 100)
}

// 获取评分数量
const getRatingCount = (star: number) => {
  if (!courseStats.value || !courseStats.value.ratingDistribution) {
    return 0
  }
  return courseStats.value.ratingDistribution[star] || 0
}

onMounted(async () => {
  // 确保用户信息已加载
  if (authStore.isAuthenticated && !authStore.user?.associatedUserId) {
    await authStore.initAuth()
  }

  loadCourseDetail()
  checkFavoriteStatus()
  loadCourseStats()
  loadReviews()
})

// 监听关联用户标识变化并重新检查购买状态
watch(() => authStore.user?.associatedUserId, (newVal) => {
  if (newVal && course.value?.id) {
    checkPurchaseStatus()
  }
})

watch(() => route.params.id, () => {
  reviewPage.value = 1
  course.value = null
  isFavorite.value = false
  loadCourseDetail()
  checkFavoriteStatus()
  loadCourseStats()
  loadReviews()
})
</script>


<style scoped>
:deep(.el-card__header) { border-bottom: 1px solid #f3e9dc; }
:deep(.el-card__body) { padding: 24px; }
.loading-container,.error-container{display:flex;justify-content:center;align-items:center;min-height:380px;padding:32px;}
.course-detail{max-width:1240px;margin:0 auto;padding:24px 20px 40px;}
.back-button{margin-bottom:20px;}
.course-header{display:flex;gap:32px;margin-bottom:26px;background:linear-gradient(170deg,#fff 0%,#fff9f3 100%);border:1px solid #f3e6d7;border-radius:24px;padding:26px;box-shadow:0 12px 30px rgba(248,146,43,.08);}
.course-image{position:relative;flex:0 0 420px;height:300px;border-radius:18px;overflow:hidden;}
.course-image img{width:100%;height:100%;object-fit:cover;}
.course-difficulty{position:absolute;top:14px;right:14px;padding:7px 16px;border-radius:999px;font-size:13px;font-weight:600;color:#fff;backdrop-filter:blur(3px);}
.course-difficulty.easy{background:rgba(34,197,94,.88);}.course-difficulty.medium{background:rgba(245,158,11,.9);}.course-difficulty.hard{background:rgba(239,68,68,.9);}
.purchased-tag{position:absolute;left:14px;bottom:14px;}
.purchased-btn{background:#22c55e !important;border-color:#22c55e !important;}
.course-info{flex:1;display:flex;flex-direction:column;}
.course-category{margin-bottom:12px;}
.course-title{margin:0 0 18px;font-size:34px;line-height:1.25;color:#2a1f12;}
.coach-info{display:flex;align-items:center;gap:12px;margin-bottom:22px;padding:12px 14px;background:#fff7ee;border:1px solid #f6e6d4;border-radius:14px;}
.coach-detail{display:flex;flex-direction:column;}
.coach-name{color:#2a1f12;font-weight:700;}.coach-label{font-size:12px;color:#8f7660;}
.info-cards{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:14px;margin-bottom:24px;}
.info-card{display:flex;align-items:center;gap:12px;padding:14px;border-radius:14px;background:#fff;border:1px solid #f5e8da;}
.info-icon{width:28px;height:28px;filter:invert(52%) sepia(89%) saturate(853%) hue-rotate(349deg) brightness(97%) contrast(95%);}
.info-value{font-size:18px;font-weight:700;color:#2a1f12;}.info-label{font-size:12px;color:#8f7660;}
.action-buttons{display:flex;flex-wrap:wrap;gap:12px;margin-top:auto;}
.book-btn{background:linear-gradient(135deg,#f97316 0%,#fb923c 100%);border-color:transparent;color:#fff;font-weight:600;min-width:160px;}
.course-content{margin-top:22px;}
.detail-card{margin-bottom:16px;border-radius:20px;border:1px solid #f4e7d8;box-shadow:none;}
.card-title{font-size:19px;font-weight:700;color:#2a1f12;}
.description-content{line-height:1.85;color:#5c4532;white-space:pre-wrap;}
.coach-dialog-loading,.coach-dialog-error{padding:16px;}
.coach-dialog-content{padding:6px 0;}
.coach-avatar-section{display:flex;flex-direction:column;align-items:center;gap:8px;margin-bottom:20px;}
.coach-dialog-name{margin:0;font-size:20px;font-weight:700;color:#2a1f12;}
.reviews-card{margin-top:18px;}
.reviews-header{display:flex;justify-content:space-between;align-items:center;}
.reviews-content{min-height:120px;}
.review-summary{display:flex;gap:30px;margin-bottom:24px;padding-bottom:20px;border-bottom:1px solid #f4e7d8;}
.rating-overview{min-width:132px;text-align:center;}
.rating-score{font-size:48px;line-height:1;font-weight:800;color:#2a1f12;margin-bottom:8px;}
.rating-stars{margin-bottom:8px;}.rating-count{font-size:13px;color:#8f7660;}
.rating-distribution{flex:1;display:flex;flex-direction:column;gap:8px;}
.rating-bar{display:flex;align-items:center;gap:10px;}
.bar-label,.bar-count{width:34px;font-size:13px;color:#8f7660;}
.bar-container{flex:1;height:10px;border-radius:999px;background:#f4eee5;overflow:hidden;}
.bar-fill{height:100%;background:linear-gradient(90deg,#f97316 0%,#fdba74 100%);} .bar-count{text-align:right;}
.reviews-list{display:grid;gap:14px;}
.review-item{padding:16px;border-radius:14px;border:1px solid #f4e7d8;background:#fffaf5;}
.review-header{display:flex;justify-content:space-between;align-items:flex-start;gap:12px;margin-bottom:10px;}
.reviewer-info{display:flex;align-items:center;gap:12px;}
.reviewer-avatar{width:46px;height:46px;border-radius:50%;overflow:hidden;}
.reviewer-avatar img{width:100%;height:100%;object-fit:cover;}
.reviewer-name{font-weight:600;color:#2a1f12;}
.review-rating{display:flex;flex-direction:column;align-items:flex-end;gap:4px;}
.review-date{font-size:12px;color:#8f7660;}
.review-text{line-height:1.7;color:#5c4532;}
.review-text.empty{color:#ad947d;font-style:italic;}
.reviews-pagination{display:flex;justify-content:center;margin-top:24px;}
@media (max-width:992px){.course-header{flex-direction:column;gap:20px;}.course-image{width:100%;flex:none;height:250px;}.info-cards{grid-template-columns:1fr;}}
@media (max-width:768px){.course-detail{padding:18px 14px 30px;}.course-header{border-radius:18px;padding:16px;}.course-title{font-size:28px;}.action-buttons{flex-direction:column;}.action-buttons .el-button{width:100%;}.review-summary,.review-header{flex-direction:column;}}
</style>

