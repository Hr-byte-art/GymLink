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
            <el-tag type="primary">{{ getCourseTypeName(course.type) }}</el-tag>
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
      </div>
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
          <el-descriptions-item label="性别">{{ coachInfo.gender === 1 ? '男' : '女' }}</el-descriptions-item>
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
import request from '@/utils/request'
import { getCourseTypeName, getCoachSpecialtyName } from '@/constants/categories'
import { useAuthStore } from '@/stores/auth'
import { purchaseCourse, getPurchasedCourseIds } from '@/api/student'
import { getCoachDetail, type Coach } from '@/api/coach'
import { toggleFavorite as toggleFavoriteApi, checkFavorite, FavoriteType } from '@/api/favorite'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const course = ref<any>(null)
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

// 获取难度样式类
const getDifficultyClass = (difficulty: string) => {
  const map: Record<string, string> = {
    '初级': 'easy',
    '中级': 'medium',
    '高级': 'hard'
  }
  return map[difficulty] || 'easy'
}

// 返回列表
const goBack = () => {
  router.push('/courses')
}

// 加载课程详情
const loadCourseDetail = async () => {
  const id = route.params.id as string
  if (!id) {
    error.value = '课程ID不存在'
    return
  }

  loading.value = true
  error.value = null

  try {
    const res = await request.get('/course/getCourseById', { params: { id } })
    course.value = res
    // 检查是否已购买
    await checkPurchaseStatus()
  } catch (e: any) {
    error.value = e.message || '获取课程详情失败'
  } finally {
    loading.value = false
  }
}

// 检查课程是否已购买
const checkPurchaseStatus = async () => {
  console.log('checkPurchaseStatus - isAuthenticated:', authStore.isAuthenticated)
  console.log('checkPurchaseStatus - role:', authStore.user?.role)
  console.log('checkPurchaseStatus - associatedUserId:', authStore.user?.associatedUserId)
  
  if (!authStore.isAuthenticated) {
    console.log('用户未登录，跳过检查购买状态')
    isPurchased.value = false
    return
  }
  
  // 只有学员角色才检查购买状态
  const role = authStore.user?.role
  if (role !== 'student' && role !== 'user') {
    console.log('非学员角色，跳过检查购买状态')
    isPurchased.value = false
    return
  }
  
  const studentId = authStore.user?.associatedUserId
  if (!studentId || !course.value?.id) {
    console.log('studentId或courseId为空，跳过检查购买状态')
    isPurchased.value = false
    return
  }

  try {
    console.log('正在检查购买状态，studentId:', studentId, 'courseId:', course.value.id)
    const purchasedIds = await getPurchasedCourseIds(studentId)
    console.log('已购课程ID列表:', purchasedIds)
    // 将两边都转为字符串进行比较，避免类型不匹配问题
    const courseIdStr = String(course.value.id)
    isPurchased.value = purchasedIds.some(id => String(id) === courseIdStr)
    console.log('当前课程是否已购买:', isPurchased.value)
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

  // 获取学员ID（使用 associatedUserId）
  const studentId = authStore.user?.associatedUserId
  if (!studentId) {
    ElMessage.error('无法获取学员信息，请重新登录')
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
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '购买失败，请稍后重试')
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
  } catch (e: any) {
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
    // request.ts 响应拦截器已解包，res 直接就是 boolean
    isFavorite.value = res as unknown as boolean
    ElMessage.success(isFavorite.value ? '已添加到收藏' : '已取消收藏')
  } catch (error) {
    ElMessage.error('操作失败，请先登录')
  }
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  try {
    const res = await checkFavorite(Number(route.params.id), FavoriteType.COURSE)
    isFavorite.value = res.data
  } catch (error) {
    // 未登录时忽略错误
  }
}

onMounted(async () => {
  // 确保用户信息已加载
  if (authStore.isAuthenticated && !authStore.user?.associatedUserId) {
    console.log('等待用户信息加载...')
    await authStore.initAuth()
  }
  
  loadCourseDetail()
  checkFavoriteStatus()
})

// 监听 associatedUserId 变化，重新检查购买状态
watch(() => authStore.user?.associatedUserId, (newVal) => {
  if (newVal && course.value?.id) {
    console.log('associatedUserId 已更新，重新检查购买状态')
    checkPurchaseStatus()
  }
})
</script>


<style scoped>
.loading-container,
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 40px;
}

.course-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
}

.course-header {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.course-image {
  position: relative;
  flex: 0 0 400px;
  height: 300px;
  border-radius: 12px;
  overflow: hidden;
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-difficulty {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: white;
}

.course-difficulty.easy {
  background: #67c23a;
}

.course-difficulty.medium {
  background: #e6a23c;
}

.course-difficulty.hard {
  background: #f56c6c;
}

.purchased-tag {
  position: absolute;
  bottom: 15px;
  left: 15px;
  font-size: 14px;
}

.purchased-btn {
  background: #67c23a !important;
  border-color: #67c23a !important;
}

.course-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.course-category {
  margin-bottom: 15px;
}

.course-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 20px;
}

.coach-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 25px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.coach-detail {
  display: flex;
  flex-direction: column;
}

.coach-name {
  font-weight: 600;
  color: #2c3e50;
}

.coach-label {
  font-size: 12px;
  color: #909399;
}

.info-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.info-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 20px;
  background: #f8f9fa;
  border-radius: 8px;
  flex: 1;
}

.info-icon {
  width: 28px;
  height: 28px;
  filter: invert(45%) sepia(98%) saturate(1500%) hue-rotate(196deg) brightness(100%) contrast(96%);
}

.info-value {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.info-label {
  font-size: 12px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: auto;
}

.book-btn {
  background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
  border: none;
  padding: 12px 40px;
  font-size: 16px;
}

.course-content {
  margin-top: 30px;
}

.detail-card {
  margin-bottom: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.description-content {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

/* 教练弹窗样式 */
.coach-dialog-loading,
.coach-dialog-error {
  padding: 20px;
}

.coach-dialog-content {
  padding: 10px 0;
}

.coach-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.coach-dialog-name {
  margin-top: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

@media (max-width: 768px) {
  .course-header {
    flex-direction: column;
  }

  .course-image {
    flex: none;
    width: 100%;
    height: 200px;
  }

  .info-cards {
    flex-direction: column;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>
