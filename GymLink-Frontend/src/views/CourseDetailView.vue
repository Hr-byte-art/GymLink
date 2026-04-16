<template>
  <AppLayout>
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="error" class="error-container">
      <el-result icon="warning" title="加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="loadCourseDetail">重新加载</el-button>
          <el-button @click="goBack">返回列表</el-button>
        </template>
      </el-result>
    </div>

    <div v-else-if="course" class="course-detail">
      <div class="back-button">
        <el-button :icon="ArrowLeft" @click="goBack">返回课程列表</el-button>
      </div>

      <div class="course-header">
        <div class="course-image">
          <img :src="course.image || '/course1.svg'" :alt="course.name" />
          <div class="course-difficulty" :class="getDifficultyClass(course.difficulty)">
            {{ course.difficulty }}
          </div>
          <el-tag
            v-if="isPurchased"
            class="purchased-tag"
            :type="hasRemainingSessions ? 'success' : 'info'"
            effect="dark"
            size="large"
          >
            {{ purchaseTagText }}
          </el-tag>
        </div>

        <div class="course-info">
          <div class="course-category">
            <el-tag type="primary">{{ getCourseTypeName(course.type || '') }}</el-tag>
            <el-tag type="warning">{{ getDeliveryModeName(course.deliveryMode) }}</el-tag>
          </div>
          <h1 class="course-title">{{ course.name }}</h1>

          <div v-if="course.coachName" class="coach-info">
            <el-avatar :size="40" :src="course.coachAvatar || '/avatar-placeholder.svg'" />
            <div class="coach-detail">
              <span class="coach-name">{{ course.coachName }}</span>
              <span class="coach-label">授课教练</span>
            </div>
          </div>

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
            <div class="info-card">
              <div class="info-content">
                <div class="info-value">{{ course.totalSessions || 1 }}</div>
                <div class="info-label">总课次</div>
              </div>
            </div>
          </div>

          <div class="purchase-summary">
            <div class="summary-item">
              <span class="summary-label">授课形式</span>
              <span class="summary-value">{{ getDeliveryModeName(course.deliveryMode) }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">已购订单</span>
              <span class="summary-value">{{ purchaseOrderCount }} 单</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">剩余课次</span>
              <span class="summary-value">{{ totalRemainingSessions }} 次</span>
            </div>
          </div>

          <div class="purchase-tips">
            <span v-if="Number(course.deliveryMode) === 1">
              私教课需先购买该教练名下课程，且订单仍有剩余课次，才可以预约教练。
            </span>
            <span v-else>
              团体课购买后可直接选择具体场次报名，每次报名会扣减 1 次剩余课次。
            </span>
          </div>

          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              class="book-btn"
              :loading="purchasing"
              @click="handlePrimaryAction"
            >
              {{ primaryActionText }}
            </el-button>
            <el-button size="large" @click="showCoachDialog">联系教练</el-button>
            <el-button size="large" @click="handleToggleFavorite">
              {{ isFavorite ? '❤️ 已收藏' : '🤍 收藏' }}
            </el-button>
          </div>
        </div>
      </div>

      <div class="course-content">
        <el-card class="detail-card">
          <template #header>
            <span class="card-title">课程介绍</span>
          </template>
          <div class="description-content">
            {{ course.description || '暂无课程介绍' }}
          </div>
        </el-card>

        <div v-if="Number(course.deliveryMode) === 2" ref="scheduleCardRef">
          <el-card class="detail-card schedule-card">
            <template #header>
              <div class="schedule-header">
                <span class="card-title">可选场次</span>
                <span class="schedule-subtitle">当前剩余课次：{{ totalRemainingSessions }} 次</span>
              </div>
            </template>

            <div v-loading="scheduleLoading" class="schedule-content">
              <div v-if="availableSchedules.length > 0" class="schedule-list">
                <div v-for="schedule in availableSchedules" :key="schedule.id" class="schedule-item">
                  <div class="schedule-main">
                    <div class="schedule-time">
                      {{ formatDateTime(schedule.startTime) }} - {{ formatDateTime(schedule.endTime) }}
                    </div>
                    <div class="schedule-meta">
                      <span>教练：{{ schedule.coachName || course.coachName || '未知' }}</span>
                      <span>已报 {{ schedule.bookedCount || 0 }} / {{ schedule.capacity || 0 }}</span>
                      <span>剩余名额 {{ schedule.remainingCapacity || 0 }}</span>
                    </div>
                  </div>
                  <div class="schedule-actions">
                    <el-tag :type="(schedule.remainingCapacity || 0) > 0 ? 'success' : 'danger'">
                      {{ (schedule.remainingCapacity || 0) > 0 ? '可报名' : '已满员' }}
                    </el-tag>
                    <el-button
                      type="primary"
                      :disabled="(schedule.remainingCapacity || 0) <= 0"
                      :loading="bookingScheduleId === schedule.id"
                      @click="handleScheduleAction(schedule)"
                    >
                      {{ hasRemainingSessions ? '立即约课' : '购买后约课' }}
                    </el-button>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无可报名场次" />
            </div>
          </el-card>
        </div>

        <el-card class="detail-card reviews-card">
          <template #header>
            <div class="reviews-header">
              <span class="card-title">用户评价</span>
            </div>
          </template>
          <div v-loading="reviewLoading" class="reviews-content">
            <div v-if="courseStats && courseStats.reviewCount > 0" class="review-summary">
              <div class="rating-overview">
                <div class="rating-score">{{ courseStats.avgRating }}</div>
                <div class="rating-stars">
                  <el-rate v-model="courseStats.avgRating" disabled />
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
                    <el-rate v-model="review.rating" disabled />
                    <span class="review-date">{{ formatDate(review.createTime) }}</span>
                  </div>
                </div>
                <div v-if="review.content" class="review-text">{{ review.content }}</div>
                <div v-else class="review-text empty">用户未填写评价内容</div>
              </div>
            </div>
            <el-empty v-else description="暂无评价" />

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
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'
import {
  getCoachSpecialtyName,
  getCourseTypeName,
  getDeliveryModeName,
  getGenderName
} from '@/constants/categories'
import { useAuthStore } from '@/stores/auth'
import { getPurchasedCourses, purchaseCourse, type PurchasedCourse } from '@/api/student'
import { bookCourseSchedule } from '@/api/appointment'
import { getCoachDetail, type Coach } from '@/api/coach'
import { getCourseDetail, listAvailableSchedules, type CourseSchedule } from '@/api/course'
import { toggleFavorite as toggleFavoriteApi, checkFavorite, FavoriteType } from '@/api/favorite'
import { getReviewList, getCourseReviewStats, type CourseReview, type CourseReviewStats } from '@/api/review'

type CourseDetail = {
  id: number | string
  name?: string
  image?: string
  difficulty?: string
  type?: string
  deliveryMode?: number
  totalSessions?: number
  coachId?: number | string
  coachName?: string
  coachAvatar?: string
  duration?: number
  price?: number
  description?: string
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const course = ref<CourseDetail | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const purchasing = ref(false)
const scheduleLoading = ref(false)
const bookingScheduleId = ref<number | string | null>(null)
const purchasedOrders = ref<PurchasedCourse[]>([])
const availableSchedules = ref<CourseSchedule[]>([])
const scheduleCardRef = ref<HTMLDivElement | null>(null)

const coachDialogVisible = ref(false)
const coachLoading = ref(false)
const coachInfo = ref<Coach | null>(null)

const isFavorite = ref(false)

const reviews = ref<CourseReview[]>([])
const reviewLoading = ref(false)
const reviewTotal = ref(0)
const reviewPage = ref(1)
const reviewPageSize = 5
const courseStats = ref<CourseReviewStats | null>(null)

const isStudentUser = computed(() => {
  const role = authStore.user?.role
  return role === 'student' || role === 'user'
})

const purchaseOrderCount = computed(() => purchasedOrders.value.length)
const totalRemainingSessions = computed(() =>
  purchasedOrders.value.reduce((sum, item) => sum + (item.remainingSessions || 0), 0)
)
const isPurchased = computed(() => purchaseOrderCount.value > 0)
const hasRemainingSessions = computed(() => totalRemainingSessions.value > 0)
const purchaseTagText = computed(() => (hasRemainingSessions.value ? `已购剩余${totalRemainingSessions.value}次` : '已购无剩余'))

const primaryActionText = computed(() => {
  if (!course.value) return '立即购买'
  if (Number(course.value.deliveryMode) === 1) {
    return hasRemainingSessions.value ? '去预约教练' : '立即购买'
  }
  return hasRemainingSessions.value ? '立即约课' : '立即购买'
})

const getErrorMessage = (err: unknown, fallback: string) => {
  if (err && typeof err === 'object' && 'message' in err) {
    const message = (err as { message?: unknown }).message
    if (typeof message === 'string' && message) return message
  }
  return fallback
}

const getDifficultyClass = (difficulty?: string) => {
  const map: Record<string, string> = {
    初级: 'easy',
    中级: 'medium',
    高级: 'hard'
  }
  return map[difficulty || ''] || 'easy'
}

const goBack = () => {
  router.push('/courses')
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const formatDateTime = (dateString?: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const ensureStudentIdentity = async () => {
  if (authStore.isAuthenticated && !authStore.user?.associatedUserId) {
    await authStore.initAuth()
  }
}

const loadPurchaseStatus = async () => {
  if (!authStore.isAuthenticated || !isStudentUser.value || !course.value?.id) {
    purchasedOrders.value = []
    return
  }

  const studentId = authStore.user?.associatedUserId
  if (!studentId) {
    purchasedOrders.value = []
    return
  }

  try {
    const res = await getPurchasedCourses({
      studentId,
      courseId: course.value.id,
      status: 1,
      pageNum: 1,
      pageSize: 20
    })
    purchasedOrders.value = res.records || []
  } catch (err) {
    console.error('获取已购课程状态失败:', err)
    purchasedOrders.value = []
  }
}

const loadAvailableScheduleList = async () => {
  if (!course.value?.id || Number(course.value.deliveryMode) !== 2) {
    availableSchedules.value = []
    return
  }

  scheduleLoading.value = true
  try {
    availableSchedules.value = await listAvailableSchedules(course.value.id)
  } catch (err) {
    console.error('获取团课排期失败:', err)
    availableSchedules.value = []
  } finally {
    scheduleLoading.value = false
  }
}

const refreshPurchaseRelatedState = async () => {
  await Promise.all([loadPurchaseStatus(), loadAvailableScheduleList()])
}

const loadCourseDetail = async () => {
  const id = route.params.id as string
  if (!id) {
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
    await refreshPurchaseRelatedState()
  } catch (err: unknown) {
    course.value = null
    error.value = getErrorMessage(err, '获取课程详情失败')
  } finally {
    loading.value = false
  }
}

const scrollToSchedules = async () => {
  await nextTick()
  scheduleCardRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const handlePurchase = async () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录后再购买课程')
    router.push({ name: 'auth', query: { redirect: route.fullPath } })
    return
  }

  if (!isStudentUser.value) {
    ElMessage.warning('只有学员才能购买课程')
    return
  }

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
    await refreshPurchaseRelatedState()
  } catch (err: unknown) {
    if (err !== 'cancel') {
      ElMessage.error(getErrorMessage(err, '购买失败，请稍后重试'))
    }
  } finally {
    purchasing.value = false
  }
}

const handlePrimaryAction = async () => {
  if (!course.value) return

  if (Number(course.value.deliveryMode) === 1) {
    if (hasRemainingSessions.value && course.value.coachId) {
      router.push(`/coaches/${course.value.coachId}`)
      return
    }
    await handlePurchase()
    return
  }

  if (hasRemainingSessions.value) {
    await scrollToSchedules()
    return
  }

  await handlePurchase()
}

const handleScheduleAction = async (schedule: CourseSchedule) => {
  if (!hasRemainingSessions.value) {
    await handlePurchase()
    return
  }

  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录后再约课')
    router.push({ name: 'auth', query: { redirect: route.fullPath } })
    return
  }

  if (!isStudentUser.value) {
    ElMessage.warning('只有学员才能报名团课')
    return
  }

  const studentId = authStore.user?.associatedUserId
  if (!studentId) {
    ElMessage.error('无法获取学员信息，请重新登录')
    return
  }

  try {
    bookingScheduleId.value = schedule.id
    await bookCourseSchedule(studentId, schedule.id)
    ElMessage.success('团课报名成功')
    await refreshPurchaseRelatedState()
  } catch (err: unknown) {
    ElMessage.error(getErrorMessage(err, '团课报名失败，请稍后重试'))
  } finally {
    bookingScheduleId.value = null
  }
}

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

const handleToggleFavorite = async () => {
  try {
    const res = await toggleFavoriteApi({
      targetId: String(route.params.id),
      type: FavoriteType.COURSE
    })
    isFavorite.value = res as unknown as boolean
    ElMessage.success(isFavorite.value ? '已添加到收藏' : '已取消收藏')
  } catch {
    ElMessage.error('操作失败，请先登录')
  }
}

const checkFavoriteStatus = async () => {
  if (!authStore.isAuthenticated) {
    isFavorite.value = false
    return
  }

  try {
    const res = await checkFavorite(String(route.params.id), FavoriteType.COURSE)
    isFavorite.value = res.data
  } catch {
    isFavorite.value = false
  }
}

const loadReviews = async () => {
  const courseId = route.params.id as string
  if (!courseId) return

  reviewLoading.value = true
  try {
    const res = await getReviewList({
      courseId,
      pageNum: reviewPage.value,
      pageSize: reviewPageSize
    })
    reviews.value = res.records || []
    reviewTotal.value = res.total || 0
  } catch (err) {
    console.error('获取评价列表失败:', err)
  } finally {
    reviewLoading.value = false
  }
}

const handleReviewPageChange = (page: number) => {
  reviewPage.value = page
  loadReviews()
}

const loadCourseStats = async () => {
  const courseId = route.params.id as string
  if (!courseId) return
  try {
    courseStats.value = await getCourseReviewStats(courseId)
  } catch (err) {
    console.error('获取课程评价统计失败:', err)
  }
}

const getRatingPercentage = (star: number) => {
  if (!courseStats.value?.ratingDistribution) return 0
  const count = courseStats.value.ratingDistribution[star] || 0
  const total = courseStats.value.reviewCount || 1
  return Math.round((count / total) * 100)
}

const getRatingCount = (star: number) => {
  if (!courseStats.value?.ratingDistribution) return 0
  return courseStats.value.ratingDistribution[star] || 0
}

onMounted(async () => {
  await ensureStudentIdentity()
  await Promise.all([loadCourseDetail(), checkFavoriteStatus(), loadCourseStats(), loadReviews()])
})

watch(
  () => authStore.user?.associatedUserId,
  () => {
    if (course.value?.id) {
      loadPurchaseStatus()
    }
  }
)

watch(
  () => route.params.id,
  async () => {
    reviewPage.value = 1
    course.value = null
    purchasedOrders.value = []
    availableSchedules.value = []
    isFavorite.value = false
    await Promise.all([loadCourseDetail(), checkFavoriteStatus(), loadCourseStats(), loadReviews()])
  }
)
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
.course-info{flex:1;display:flex;flex-direction:column;}
.course-category{display:flex;gap:10px;margin-bottom:12px;flex-wrap:wrap;}
.course-title{margin:0 0 18px;font-size:34px;line-height:1.25;color:#2a1f12;}
.coach-info{display:flex;align-items:center;gap:12px;margin-bottom:22px;padding:12px 14px;background:#fff7ee;border:1px solid #f6e6d4;border-radius:14px;}
.coach-detail{display:flex;flex-direction:column;}
.coach-name{color:#2a1f12;font-weight:700;}.coach-label{font-size:12px;color:#8f7660;}
.info-cards{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:14px;margin-bottom:18px;}
.info-card{display:flex;align-items:center;gap:12px;padding:14px;border-radius:14px;background:#fff;border:1px solid #f5e8da;min-height:76px;}
.info-icon{width:28px;height:28px;filter:invert(52%) sepia(89%) saturate(853%) hue-rotate(349deg) brightness(97%) contrast(95%);}
.info-value{font-size:18px;font-weight:700;color:#2a1f12;}.info-label{font-size:12px;color:#8f7660;}
.purchase-summary{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px;margin-bottom:14px;}
.summary-item{padding:12px 14px;border-radius:14px;background:#fff;border:1px solid #f5e8da;}
.summary-label{display:block;font-size:12px;color:#8f7660;margin-bottom:6px;}
.summary-value{font-size:16px;font-weight:700;color:#2a1f12;}
.purchase-tips{margin-bottom:20px;padding:12px 14px;border-radius:14px;background:#fff7ee;border:1px solid #f6e6d4;color:#8f5f2f;line-height:1.7;}
.action-buttons{display:flex;flex-wrap:wrap;gap:12px;margin-top:auto;}
.book-btn{background:linear-gradient(135deg,#f97316 0%,#fb923c 100%);border-color:transparent;color:#fff;font-weight:600;min-width:160px;}
.course-content{margin-top:22px;}
.detail-card{margin-bottom:16px;border-radius:20px;border:1px solid #f4e7d8;box-shadow:none;}
.card-title{font-size:19px;font-weight:700;color:#2a1f12;}
.description-content{line-height:1.85;color:#5c4532;white-space:pre-wrap;}
.schedule-header{display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap;}
.schedule-subtitle{font-size:13px;color:#8f7660;}
.schedule-content{min-height:140px;}
.schedule-list{display:grid;gap:14px;}
.schedule-item{display:flex;justify-content:space-between;align-items:center;gap:16px;padding:16px;border-radius:16px;background:#fffaf5;border:1px solid #f4e7d8;}
.schedule-main{display:flex;flex-direction:column;gap:8px;}
.schedule-time{font-size:16px;font-weight:700;color:#2a1f12;}
.schedule-meta{display:flex;flex-wrap:wrap;gap:14px;color:#8f7660;font-size:13px;}
.schedule-actions{display:flex;align-items:center;gap:10px;flex-shrink:0;}
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
@media (max-width:992px){.course-header{flex-direction:column;gap:20px;}.course-image{width:100%;flex:none;height:250px;}.info-cards,.purchase-summary{grid-template-columns:repeat(2,minmax(0,1fr));}.schedule-item{flex-direction:column;align-items:flex-start;}.schedule-actions{width:100%;justify-content:space-between;}}
@media (max-width:768px){.course-detail{padding:18px 14px 30px;}.course-header{border-radius:18px;padding:16px;}.course-title{font-size:28px;}.action-buttons{flex-direction:column;}.action-buttons .el-button{width:100%;}.review-summary,.review-header{flex-direction:column;}.info-cards,.purchase-summary{grid-template-columns:1fr;}.schedule-actions{flex-direction:column;align-items:stretch;}.schedule-actions .el-button{width:100%;}}
</style>
