<template>
  <AppLayout>
    <!-- 加载状态 -->
    <div v-if="coachStore.loading" class="loading-container">
      <el-loading :loading="true" text="加载中..."></el-loading>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="coachStore.error" class="error-container">
      <el-result icon="warning" title="加载失败" :sub-title="coachStore.error">
        <template #extra>
          <el-button type="primary" @click="loadCoachDetail">重新加载</el-button>
        </template>
      </el-result>
    </div>

    <!-- 教练详情内容 -->
    <div v-else-if="coachStore.coachDetail" class="coach-detail-content">
      <!-- 返回按钮 -->
      <div class="back-button">
        <el-button @click="goBack" icon="ArrowLeft">返回教练列表</el-button>
      </div>

      <!-- 教练基本信息 -->
      <section class="coach-hero">
        <div class="coach-hero-container">
          <div class="coach-avatar">
            <img :src="coachStore.coachDetail.avatar" :alt="coachStore.coachDetail.name" />
          </div>
          <div class="coach-basic-info">
            <h1 class="coach-name">{{ coachStore.coachDetail.name }}</h1>
            <div class="coach-specialty">{{ getCoachSpecialtyName(coachStore.coachDetail.specialty) }}</div>
            <!-- 评分显示 -->
            <div class="coach-rating" v-if="coachStats">
              <el-rate v-model="coachStats.avgRating" disabled show-score text-color="#ff9900"></el-rate>
              <span class="review-count">({{ coachStats.reviewCount }}条评价)</span>
            </div>
            <!-- 基本信息：性别、年龄、电话 -->
            <div class="coach-basic-meta">
              <div class="basic-meta-item">
                <img src="/gender.svg" alt="性别" class="icon-svg" />
                <span>{{ getGenderName(coachStore.coachDetail.gender) }}</span>
              </div>
              <div class="basic-meta-item">
                <img src="/age.svg" alt="年龄" class="icon-svg" />
                <span>{{ coachStore.coachDetail.age || '-' }}岁</span>
              </div>
              <div class="basic-meta-item">
                <img src="/phone.svg" alt="电话" class="icon-svg" />
                <span>{{ coachStore.coachDetail.phone || '-' }}</span>
              </div>
            </div>
            <!-- 统计信息：课程数、学员数 -->
            <div class="coach-stats" v-if="coachStats">
              <div class="stat-item">
                <img src="/courses.svg" alt="课程" class="icon-svg" />
                <span>{{ coachStats.courseCount }}门课程</span>
              </div>
              <div class="stat-item">
                <img src="/students.svg" alt="学员" class="icon-svg" />
                <span>{{ coachStats.studentCount }}名学员</span>
              </div>
            </div>

            <!-- 预约价格 -->
            <div class="coach-price" v-if="coachStore.coachDetail.price">
              <span class="price-label">预约价格：</span>
              <span class="price-value">¥{{ coachStore.coachDetail.price }}</span>
              <span class="price-unit">/小时</span>
            </div>

            <div class="coach-actions">
              <el-button type="primary" size="large" class="book-btn" @click="bookCoach">预约教练</el-button>
              <el-button size="large" class="contact-btn" @click="contactCoach">联系教练</el-button>
              <el-button size="large" class="favorite-btn" @click="handleToggleFavorite">
                {{ isFavorite ? '❤️ 已收藏' : '🤍 收藏' }}
              </el-button>
            </div>
          </div>
        </div>
      </section>

      <!-- 个人简介区域 -->
      <section class="coach-intro-section" v-if="coachStore.coachDetail.intro">
        <div class="intro-container">
          <div class="intro-content">
            <h3 class="section-title">个人简介</h3>
            <p class="intro-text">{{ coachStore.coachDetail.intro }}</p>
          </div>
        </div>
      </section>

      <!-- 教练课程区域 -->
      <section class="coach-courses-section">
        <div class="courses-container">
          <div class="courses-content">
            <h3 class="section-title">在售课程</h3>
            <div v-if="coursesLoading" class="courses-loading">
              <el-loading :loading="true" text="加载课程中..."></el-loading>
            </div>
            <div v-else-if="coachCourses.length > 0" class="courses-grid">
              <div v-for="course in coachCourses" :key="course.id" class="course-card" @click="viewCourseDetail(course.id)">
                <div class="course-image">
                  <img :src="course.image || '/course1.svg'" :alt="course.name" />
                </div>
                <div class="course-info">
                  <h4 class="course-name">{{ course.name }}</h4>
                  <div class="course-meta">
                    <span class="meta-item">{{ course.duration }}分钟</span>
                    <span class="meta-item">{{ course.difficulty }}</span>
                  </div>
                  <div class="course-price">¥{{ course.price }}</div>
                </div>
              </div>
            </div>
            <div v-else class="no-courses">
              <el-empty description="暂无在售课程"></el-empty>
            </div>
          </div>
        </div>
      </section>

      <!-- 学员评价区域 -->
      <section class="coach-reviews-section">
        <div class="reviews-container">
          <div class="reviews-content">
            <h3 class="section-title">学员评价</h3>

            <!-- 评价统计 -->
            <div class="review-summary" v-if="coachStats && coachStats.reviewCount > 0">
              <div class="rating-overview">
                <div class="rating-score">{{ coachStats.avgRating }}</div>
                <div class="rating-stars">
                  <el-rate v-model="coachStats.avgRating" disabled></el-rate>
                </div>
                <div class="rating-count">{{ coachStats.reviewCount }}条评价</div>
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
            <div v-if="reviewsLoading" class="reviews-loading">
              <el-loading :loading="true" text="加载评价中..."></el-loading>
            </div>
            <div v-else-if="reviews.length > 0" class="reviews-list">
              <div v-for="review in reviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <div class="reviewer-info">
                    <div class="reviewer-avatar">
                      <img :src="review.studentAvatar || '/avatar-placeholder.svg'" :alt="review.studentName" />
                    </div>
                    <div class="reviewer-details">
                      <div class="reviewer-name">{{ review.studentName || '匿名用户' }}</div>
                      <div class="review-course">课程：{{ review.courseName }}</div>
                    </div>
                  </div>
                  <div class="review-rating">
                    <el-rate v-model="review.rating" disabled></el-rate>
                    <span class="review-date">{{ formatDate(review.createTime) }}</span>
                  </div>
                </div>
                <div class="review-content" v-if="review.content">{{ review.content }}</div>
              </div>

              <!-- 分页 -->
              <div class="reviews-pagination" v-if="reviewTotal > reviewPageSize">
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
            <div v-else class="no-reviews">
              <el-empty description="暂无评价"></el-empty>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- 预约教练对话框 -->
    <el-dialog v-model="bookingDialogVisible" title="预约教练" width="500px" :close-on-click-modal="false">
      <el-form :model="bookingForm" :rules="bookingRules" ref="bookingFormRef" label-width="100px">
        <el-form-item label="教练" prop="coachName">
          <el-input v-model="bookingForm.coachName" disabled />
        </el-form-item>
        <el-form-item label="开始时间" prop="appointTime">
          <el-date-picker
            v-model="bookingForm.appointTime"
            type="datetime"
            placeholder="选择开始时间"
            :disabled-date="disabledDate"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm"
          />
        </el-form-item>
        <el-form-item label="预约时长" prop="duration">
          <el-radio-group v-model="bookingForm.duration" class="duration-radio-group">
            <el-radio-button v-for="opt in durationOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注信息" prop="message">
          <el-input
            v-model="bookingForm.message"
            type="textarea"
            :rows="3"
            placeholder="请输入预约备注（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBooking" :loading="bookingLoading">确认预约</el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCoachStore } from '@/stores/coach'
import { useAuthStore } from '@/stores/auth'
import { bookCoach as bookCoachApi } from '@/api/coach'
import { getCoachReviewStats, getReviewList, type CourseReview, type CoachReviewStats } from '@/api/review'
import { getCourseList, type Course } from '@/api/course'
import { toggleFavorite as toggleFavoriteApi, checkFavorite, FavoriteType } from '@/api/favorite'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

import AppLayout from '@/components/AppLayout.vue'
import { getCoachSpecialtyName, getGenderName } from '@/constants/categories'

// 使用路由和教练状态管理
const route = useRoute()
const router = useRouter()
const coachStore = useCoachStore()
const authStore = useAuthStore()

// 评价相关状态
const coachStats = ref<CoachReviewStats | null>(null)
const reviews = ref<CourseReview[]>([])
const reviewsLoading = ref(false)
const reviewPage = ref(1)
const reviewPageSize = ref(10)
const reviewTotal = ref(0)

// 课程相关状态
const coachCourses = ref<Course[]>([])
const coursesLoading = ref(false)

// 收藏状态
const isFavorite = ref(false)

// 预约相关状态
const bookingDialogVisible = ref(false)
const bookingLoading = ref(false)
const bookingFormRef = ref<FormInstance>()
const bookingForm = reactive({
  coachName: '',
  appointTime: null as Date | null,
  duration: 60, // 默认1小时
  message: ''
})

// 时长选项（分钟）
const durationOptions = [
  { label: '30分钟', value: 30 },
  { label: '1小时', value: 60 },
  { label: '1.5小时', value: 90 },
  { label: '2小时', value: 120 }
]

const bookingRules = reactive<FormRules>({
  appointTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  duration: [{ required: true, message: '请选择时长', trigger: 'change' }]
})

// 禁用过去的日期
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7 // 禁用今天之前的日期
}

// 获取教练ID（保持字符串类型，避免大数精度丢失）
const coachId = computed(() => {
  return route.params.id as string
})



// 返回教练列表
const goBack = () => {
  router.push('/coaches')
}

// 打开预约对话框
const openBookingDialog = () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录后再预约教练')
    router.push({ name: 'auth', query: { redirect: route.fullPath } })
    return
  }

  if (authStore.user?.role === 'coach') {
    ElMessage.warning('教练不能预约其他教练')
    return
  }

  // 重置表单
  bookingForm.coachName = coachStore.coachDetail?.name || ''
  bookingForm.appointTime = null
  bookingForm.duration = 60
  bookingForm.message = ''
  bookingDialogVisible.value = true
}

// 提交预约
const submitBooking = async () => {
  if (!bookingFormRef.value) return

  await bookingFormRef.value.validate(async (valid) => {
    if (!valid) return

    bookingLoading.value = true
    try {
      // 获取学员标识（使用关联用户标识）
      const studentId = authStore.user?.associatedUserId
      if (!studentId) {
        ElMessage.error('无法获取学员信息，请重新登录')
        return
      }

      // 计算结束时间
      const appointTime = new Date(bookingForm.appointTime!)
      const endTime = new Date(appointTime.getTime() + bookingForm.duration * 60 * 1000)

      await bookCoachApi({
        coachId: coachId.value,
        studentId: studentId,
        appointTime: appointTime.toISOString(),
        endTime: endTime.toISOString(),
        message: bookingForm.message || undefined
      })

      ElMessage.success('预约成功！请等待教练确认')
      bookingDialogVisible.value = false
    } catch (error: unknown) {
      const message =
        error && typeof error === 'object' && 'message' in error
          ? String((error as { message?: unknown }).message || '')
          : ''
      ElMessage.error(message || '预约失败，请稍后重试')
    } finally {
      bookingLoading.value = false
    }
  })
}

// 预约教练（打开对话框）
const bookCoach = () => {
  openBookingDialog()
}

// 联系教练
const contactCoach = () => {
  if (!coachStore.coachDetail?.phone) {
    ElMessage.info('暂无教练联系方式')
    return
  }
  ElMessage.success({ message: `教练电话：${coachStore.coachDetail.phone}`, duration: 3000 })
}



// 加载教练详情
const loadCoachDetail = () => {
  if (coachId.value) {
    coachStore.fetchCoachDetail(coachId.value)
  }
}

// 加载教练课程
const loadCoachCourses = async () => {
  if (!coachId.value) return
  coursesLoading.value = true
  try {
    const res = await getCourseList({
      coachId: Number(coachId.value),
      current: 1,
      pageSize: 20
    })
    coachCourses.value = res.records || []
  } catch (error) {
    console.error('获取教练课程失败:', error)
  } finally {
    coursesLoading.value = false
  }
}

// 查看课程详情
const viewCourseDetail = (courseId: number) => {
  router.push(`/courses/${courseId}`)
}

// 加载教练评价统计
const loadCoachStats = async () => {
  if (!coachId.value) return
  try {
    coachStats.value = await getCoachReviewStats(coachId.value)
  } catch (error) {
    console.error('获取教练统计失败:', error)
  }
}

// 加载评价列表
const loadReviews = async () => {
  if (!coachId.value) return
  reviewsLoading.value = true
  try {
    const res = await getReviewList({
      coachId: coachId.value,
      pageNum: reviewPage.value,
      pageSize: reviewPageSize.value
    })
    reviews.value = res.records || []
    reviewTotal.value = res.total || 0
  } catch (error) {
    console.error('获取评价列表失败:', error)
  } finally {
    reviewsLoading.value = false
  }
}

// 处理评价分页变化
const handleReviewPageChange = (page: number) => {
  reviewPage.value = page
  loadReviews()
}

// 获取评分百分比
const getRatingPercentage = (star: number) => {
  if (!coachStats.value || !coachStats.value.ratingDistribution) {
    return 0
  }
  const count = coachStats.value.ratingDistribution[star] || 0
  const total = coachStats.value.reviewCount || 1
  return Math.round((count / total) * 100)
}

// 获取评分数量
const getRatingCount = (star: number) => {
  if (!coachStats.value || !coachStats.value.ratingDistribution) {
    return 0
  }
  return coachStats.value.ratingDistribution[star] || 0
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 切换收藏状态
const handleToggleFavorite = async () => {
  try {
    const res = await toggleFavoriteApi({
      targetId: coachId.value, // 保持字符串，避免大数精度丢失
      type: FavoriteType.COACH
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
  try {
    const res = await checkFavorite(coachId.value, FavoriteType.COACH)
    isFavorite.value = res.data
  } catch {
    // 未登录时忽略错误
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadCoachDetail()
  loadCoachStats()
  loadReviews()
  loadCoachCourses()
  checkFavoriteStatus()
})
</script>

<style scoped>
:deep(.el-card__header){border-bottom:1px solid #f3e9dc;}
.loading-container,.error-container{display:flex;justify-content:center;align-items:center;min-height:50vh;}
.back-button{max-width:1240px;margin:0 auto;padding:20px;}
.coach-hero{background:radial-gradient(circle at 14% 14%,rgba(255,244,229,.65) 0%,rgba(255,244,229,0) 35%),linear-gradient(145deg,#2d2114 0%,#4a2f1b 42%,#f97316 100%);color:#fff;padding:48px 0;}
.coach-hero-container{max-width:1240px;margin:0 auto;padding:0 20px;display:flex;align-items:center;gap:32px;}
.coach-avatar{width:196px;height:196px;border-radius:50%;overflow:hidden;border:4px solid rgba(255,255,255,.35);box-shadow:0 14px 30px rgba(0,0,0,.22);flex-shrink:0;}
.coach-avatar img{width:100%;height:100%;object-fit:cover;}
.coach-basic-info{flex:1;}
.coach-name{margin:0 0 8px;font-size:42px;line-height:1.1;}
.coach-specialty{margin-bottom:14px;font-size:20px;color:rgba(255,255,255,.9);}
.coach-rating{display:flex;align-items:center;gap:10px;margin-bottom:16px;}.review-count{font-size:14px;color:rgba(255,255,255,.85);}
.icon-svg{width:18px;height:18px;margin-right:8px;filter:brightness(0) invert(1);}
.coach-basic-meta,.coach-stats{display:flex;flex-wrap:wrap;gap:14px 22px;margin-bottom:14px;}
.basic-meta-item,.stat-item{display:inline-flex;align-items:center;font-size:15px;color:rgba(255,255,255,.92);}
.coach-price{display:flex;align-items:baseline;gap:4px;margin-bottom:20px;}
.price-label,.price-unit{color:rgba(255,255,255,.88);} .price-value{font-size:30px;line-height:1;font-weight:800;color:#ffd18c;}
.coach-actions{display:flex;flex-wrap:wrap;gap:12px;}
.book-btn,.contact-btn,.favorite-btn{min-width:132px;}
.book-btn{background:#fff;color:#7a3300;border:none;font-weight:700;}
.contact-btn{background:transparent;border:1px solid rgba(255,255,255,.65);color:#fff;}
.favorite-btn{background:rgba(255,255,255,.16);color:#fff;border:1px solid rgba(255,255,255,.26);}
.coach-intro-section,.coach-courses-section,.coach-reviews-section{padding:26px 0;}
.coach-reviews-section{background:#fffaf5;}
.intro-container,.courses-container,.reviews-container{max-width:1240px;margin:0 auto;padding:0 20px;}
.intro-content,.courses-content,.reviews-content{background:#fff;border:1px solid #f4e8da;border-radius:20px;padding:24px;box-shadow:0 10px 24px rgba(248,146,43,.06);}
.section-title{margin:0 0 16px;padding-bottom:12px;font-size:24px;font-weight:700;color:#2a1f12;border-bottom:1px solid #f4e8da;}
.intro-text{margin:0;line-height:1.85;color:#5c4532;}
.courses-loading,.reviews-loading,.no-courses,.no-reviews{padding:24px 0;}
.courses-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(230px,1fr));gap:14px;}
.course-card{background:#fffaf5;border:1px solid #f3e7d8;border-radius:14px;overflow:hidden;cursor:pointer;transition:transform .2s ease,box-shadow .2s ease;}
.course-card:hover{transform:translateY(-2px);box-shadow:0 10px 18px rgba(248,146,43,.16);}
.course-card .course-image{height:140px;overflow:hidden;}
.course-card .course-image img{width:100%;height:100%;object-fit:cover;}
.course-card .course-info{padding:12px;}
.course-card .course-name{margin:0 0 7px;color:#2a1f12;font-size:15px;font-weight:700;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}
.course-card .course-meta{display:flex;gap:8px;margin-bottom:8px;}
.course-card .meta-item{color:#8f7660;font-size:12px;}
.course-card .course-price{color:#ea580c;font-size:18px;font-weight:700;}
.review-summary{display:flex;gap:28px;margin-bottom:20px;padding-bottom:18px;border-bottom:1px solid #f4e8da;}
.rating-overview{min-width:130px;text-align:center;}
.rating-score{font-size:46px;line-height:1;color:#2a1f12;font-weight:800;}
.rating-count,.bar-label,.bar-count{font-size:12px;color:#8f7660;}
.rating-distribution{flex:1;display:flex;flex-direction:column;gap:8px;}
.rating-bar{display:flex;align-items:center;gap:8px;}
.bar-label,.bar-count{width:34px;}
.bar-container{flex:1;height:9px;background:#f4eee5;border-radius:999px;overflow:hidden;}
.bar-fill{height:100%;background:linear-gradient(90deg,#f97316 0%,#fdba74 100%);} .bar-count{text-align:right;}
.reviews-list{display:grid;gap:12px;}
.review-item{padding:14px;border-radius:12px;border:1px solid #f4e8da;background:#fffaf5;}
.review-header{display:flex;justify-content:space-between;align-items:flex-start;gap:10px;margin-bottom:8px;}
.reviewer-info{display:flex;align-items:center;gap:10px;}
.reviewer-avatar{width:42px;height:42px;border-radius:50%;overflow:hidden;}
.reviewer-avatar img{width:100%;height:100%;object-fit:cover;}
.reviewer-name{font-weight:700;color:#2a1f12;}
.review-course,.review-date{color:#8f7660;font-size:12px;}
.review-rating{display:flex;flex-direction:column;align-items:flex-end;gap:4px;}
.review-content{margin:0;line-height:1.7;color:#5c4532;}
.reviews-pagination{display:flex;justify-content:center;margin-top:22px;}
.duration-radio-group{display:flex;flex-wrap:wrap;gap:8px;}
.duration-radio-group .el-radio-button{margin-right:0;}
.duration-radio-group .el-radio-button__inner{border-radius:8px !important;}
@media (max-width:992px){.coach-hero-container{flex-direction:column;text-align:center;}.coach-basic-meta,.coach-stats,.coach-rating,.coach-actions{justify-content:center;}.review-summary{flex-direction:column;align-items:center;}}
@media (max-width:768px){.back-button,.coach-hero-container,.intro-container,.courses-container,.reviews-container{padding-left:14px;padding-right:14px;}.coach-name{font-size:32px;}.coach-actions{flex-direction:column;}.coach-actions .el-button{width:100%;}.intro-content,.courses-content,.reviews-content{padding:16px;border-radius:16px;}.review-header{flex-direction:column;}}
</style>
