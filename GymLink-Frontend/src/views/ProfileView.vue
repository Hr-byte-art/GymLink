<template>
  <AppLayout>
    <div class="profile-container">
      <div class="profile-header">
        <div class="profile-banner">
          <div class="profile-info">
            <div class="avatar-wrapper">
              <img :src="userDetailInfo?.avatar || '/avatar-placeholder.svg'" :alt="displayName" class="user-avatar" />
              <label class="avatar-upload-btn" :class="{ 'uploading': loading.avatar }">
                <input type="file" accept="image/*" @change="handleAvatarChange" :disabled="loading.avatar" />
                <span v-if="loading.avatar">上传中...</span>
                <span v-else>更换头像</span>
              </label>
            </div>
            <div class="user-details">
              <h1 class="username">{{ displayName }}</h1>
              <p class="user-role">用户身份: {{ getUserRoleText }}</p>
              <p class="join-date">加入时间: {{ formatDate(userDetailInfo?.createTime) }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="profile-content">
        <div class="profile-sidebar">
          <el-menu :default-active="activeMenu" class="profile-menu" @select="handleMenuSelect">
            <el-menu-item index="basic">
              <span>基本信息</span>
            </el-menu-item>
            <el-menu-item index="security">
              <span>安全设置</span>
            </el-menu-item>
            <el-menu-item index="courses" v-if="isStudent">
              <span>我的课程</span>
            </el-menu-item>
            <el-menu-item index="appointments" v-if="isStudent">
              <span>我的预约</span>
            </el-menu-item>
          </el-menu>
        </div>

        <div class="profile-main">
          <!-- 基本信息 - 学员 -->
          <div v-if="activeMenu === 'basic' && isStudent" class="profile-section">
            <h2>基本信息</h2>
            <el-form :model="studentForm" :rules="studentFormRules" ref="studentFormRef" label-width="120px"
              class="user-info-form" v-loading="loading.userInfo">
              <el-form-item label="用户名">
                <el-input v-model="studentForm.username" disabled></el-input>
              </el-form-item>
              <el-form-item label="真实姓名" prop="name">
                <el-input v-model="studentForm.name"></el-input>
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="studentForm.phone"></el-input>
              </el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="studentForm.gender">
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="2">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="身高(cm)">
                <el-input-number v-model="studentForm.height" :min="0" :max="300" :precision="1"
                  controls-position="right"></el-input-number>
              </el-form-item>
              <el-form-item label="体重(kg)">
                <el-input-number v-model="studentForm.weight" :min="0" :max="500" :precision="1"
                  controls-position="right"></el-input-number>
              </el-form-item>
              <el-form-item label="账户余额">
                <el-input :model-value="'¥' + (studentForm.balance || 0)" disabled></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="updateStudentInfo" :loading="loading.submit">保存信息</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 基本信息 - 教练 -->
          <div v-if="activeMenu === 'basic' && isCoach" class="profile-section">
            <h2>基本信息</h2>
            <el-form :model="coachForm" :rules="coachFormRules" ref="coachFormRef" label-width="120px"
              class="user-info-form" v-loading="loading.userInfo">
              <el-form-item label="用户名">
                <el-input v-model="coachForm.username" disabled></el-input>
              </el-form-item>
              <el-form-item label="真实姓名" prop="name">
                <el-input v-model="coachForm.name"></el-input>
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="coachForm.phone"></el-input>
              </el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="coachForm.gender">
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="2">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="年龄">
                <el-input-number v-model="coachForm.age" :min="18" :max="100"
                  controls-position="right"></el-input-number>
              </el-form-item>
              <el-form-item label="专业方向" prop="specialty">
                <el-input v-model="coachForm.specialty"></el-input>
              </el-form-item>
              <el-form-item label="个人简介">
                <el-input v-model="coachForm.intro" type="textarea" :rows="3"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="updateCoachInfo" :loading="loading.submit">保存信息</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 安全设置 -->
          <div v-if="activeMenu === 'security'" class="profile-section">
            <h2>安全设置</h2>
            <el-form :model="securityForm" :rules="securityRules" ref="securityFormRef" label-width="120px"
              class="security-form">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="securityForm.oldPassword" type="password" show-password></el-input>
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="securityForm.newPassword" type="password" show-password></el-input>
              </el-form-item>
              <el-form-item label="确认密码" prop="checkedPassword">
                <el-input v-model="securityForm.checkedPassword" type="password" show-password></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleChangePassword" :loading="loading.password">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 我的课程 -->
          <div v-if="activeMenu === 'courses'" class="profile-section">
            <h2>我的课程</h2>
            <div class="courses-filter">
              <el-input v-model="courseSearchKeyword" placeholder="搜索课程名称" clearable style="width: 200px; margin-right: 15px;" @clear="loadPurchasedCourses" @keyup.enter="loadPurchasedCourses" />
              <el-select v-model="courseStatusFilter" placeholder="订单状态" clearable style="width: 120px; margin-right: 15px;" @change="loadPurchasedCourses">
                <el-option label="已支付" :value="1"></el-option>
                <el-option label="已退款" :value="2"></el-option>
              </el-select>
              <el-button type="primary" @click="loadPurchasedCourses">搜索</el-button>
            </div>
            <div v-loading="loading.purchasedCourses" class="purchased-courses-list">
              <div v-if="purchasedCourses.length > 0" class="courses-grid">
                <div v-for="course in purchasedCourses" :key="course.orderId" class="purchased-course-card" @click="viewCourseDetail(course.courseId)">
                  <div class="course-image">
                    <img :src="course.courseImage || '/course1.svg'" :alt="course.courseName" />
                    <el-tag class="status-tag" :type="course.status === 1 ? 'success' : 'info'">{{ course.status === 1 ? '已购买' : '已退款' }}</el-tag>
                  </div>
                  <div class="course-info">
                    <h3 class="course-name">{{ course.courseName }}</h3>
                    <div class="course-meta">
                      <span class="meta-item"><img src="/duration.svg" class="meta-icon" />{{ course.duration }}分钟</span>
                      <span class="meta-item"><img src="/star.svg" class="meta-icon" />{{ course.difficulty }}</span>
                    </div>
                    <div class="course-coach">授课教练：{{ course.coachName || '未知' }}</div>
                    <div class="course-price">购买价格：¥{{ course.price }}</div>
                    <div class="course-time">购买时间：{{ formatDate(course.purchaseTime) }}</div>
                    <div class="course-actions">
                      <el-button type="primary" size="small" @click.stop="viewCourseDetail(course.courseId)">查看详情</el-button>
                      <el-button v-if="course.status === 1 && course.canReview" type="success" size="small" @click.stop="openReviewDialog(course)">评价课程</el-button>
                      <el-tag v-else-if="course.status === 1 && !course.canReview" type="info" size="small">已评价</el-tag>
                    </div>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无已购课程" />
            </div>
            <el-pagination v-if="purchasedCoursesPagination.total > 0" layout="prev, pager, next" :total="purchasedCoursesPagination.total" :page-size="purchasedCoursesPagination.pageSize" :current-page="purchasedCoursesPagination.currentPage" @current-change="handlePurchasedCoursesPageChange" class="pagination" />
          </div>

          <!-- 我的预约 -->
          <div v-if="activeMenu === 'appointments'" class="profile-section">
            <h2>我的预约</h2>
            <el-tabs v-model="appointmentTab" @tab-change="handleAppointmentTabChange">
              <el-tab-pane label="教练预约" name="coach">
                <el-table :data="coachAppointments" style="width: 100%" v-loading="loading.coachAppointments">
                  <el-table-column prop="coachName" label="教练"></el-table-column>
                  <el-table-column label="开始时间" width="180">
                    <template #default="scope">{{ formatDateTime(scope.row.appointTime) }}</template>
                  </el-table-column>
                  <el-table-column label="结束时间" width="180">
                    <template #default="scope">{{ formatDateTime(scope.row.endTime) }}</template>
                  </el-table-column>
                  <el-table-column prop="message" label="备注"></el-table-column>
                  <el-table-column label="状态" width="120">
                    <template #default="scope">
                      <el-tag :type="getCoachAppointmentStatusType(scope.row.status)">{{
                        getCoachAppointmentStatusText(scope.row.status) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="180">
                    <template #default="scope">
                      <el-button v-if="scope.row.status === 0" size="small" type="danger"
                        @click="handleCancelCoachAppointment(scope.row.id)">取消</el-button>
                      <el-button v-if="scope.row.status === 1 && scope.row.canReview" size="small" type="success"
                        @click="openAppointmentReviewDialog(scope.row)">评价</el-button>
                      <el-tag v-else-if="scope.row.status === 1 && !scope.row.canReview" type="info" size="small">已评价</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
                <el-pagination v-if="coachAppointmentPagination.total > 0" layout="prev, pager, next"
                  :total="coachAppointmentPagination.total" :page-size="coachAppointmentPagination.pageSize"
                  :current-page="coachAppointmentPagination.currentPage"
                  @current-change="handleCoachAppointmentPageChange" class="pagination" />
                <el-empty v-if="!loading.coachAppointments && coachAppointments.length === 0" description="暂无教练预约记录" />
              </el-tab-pane>
              <el-tab-pane label="器材预约" name="equipment">
                <el-table :data="equipmentReservations" style="width: 100%" v-loading="loading.equipmentReservations">
                  <el-table-column prop="equipmentName" label="器材名称"></el-table-column>
                  <el-table-column label="开始时间" width="180">
                    <template #default="scope">{{ formatDateTime(scope.row.startTime) }}</template>
                  </el-table-column>
                  <el-table-column label="结束时间" width="180">
                    <template #default="scope">{{ formatDateTime(scope.row.endTime) }}</template>
                  </el-table-column>
                  <el-table-column label="状态" width="120">
                    <template #default="scope">
                      <el-tag :type="getEquipmentReservationStatusType(scope.row.status)">{{
                        getEquipmentReservationStatusText(scope.row.status) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button v-if="scope.row.status === 1" size="small" type="danger"
                        @click="handleCancelEquipmentReservation(scope.row.id)">取消</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-pagination v-if="equipmentReservationPagination.total > 0" layout="prev, pager, next"
                  :total="equipmentReservationPagination.total" :page-size="equipmentReservationPagination.pageSize"
                  :current-page="equipmentReservationPagination.currentPage"
                  @current-change="handleEquipmentReservationPageChange" class="pagination" />
                <el-empty v-if="!loading.equipmentReservations && equipmentReservations.length === 0"
                  description="暂无器材预约记录" />
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>
      </div>
    </div>

    <!-- 评价对话框 -->
    <el-dialog v-model="reviewDialogVisible" :title="reviewForm.reviewType === 1 ? '评价课程' : '评价教练'" width="500px" :close-on-click-modal="false">
      <el-form :model="reviewForm" :rules="reviewRules" ref="reviewFormRef" label-width="80px">
        <el-form-item v-if="reviewForm.reviewType === 1" label="课程">
          <el-input :model-value="reviewForm.courseName" disabled />
        </el-form-item>
        <el-form-item v-else label="教练">
          <el-input :model-value="reviewForm.coachName" disabled />
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="reviewForm.rating" show-text :texts="['很差', '较差', '一般', '满意', '非常满意']" />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" :placeholder="reviewForm.reviewType === 1 ? '请输入您对课程的评价（选填）' : '请输入您对教练的评价（选填）'" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="loading.review">提交评价</el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { changePassword } from '@/api/user'
import { getStudentByUserId, updateStudent, uploadStudentAvatar, getPurchasedCourses, type PurchasedCourse } from '@/api/student'
import { addCourseReview, canReviewCourse, canReviewAppointment } from '@/api/review'
import { getCoachByUserId, updateCoach, uploadCoachAvatar } from '@/api/coach'
import { listStudentCoachAppointment, listStudentEquipmentReservation, cancelCoachAppointment, cancelEquipmentReservation, type CoachAppointment, type EquipmentReservation } from '@/api/appointment'
import AppLayout from '@/components/AppLayout.vue'

const authStore = useAuthStore()
const router = useRouter()
const activeMenu = ref('basic')
const appointmentTab = ref('coach')
const studentFormRef = ref()
const coachFormRef = ref()
const securityFormRef = ref()
const userDetailInfo = ref<any>(null)

const studentForm = ref({ id: '' as string | number, username: '', name: '', phone: '', gender: 1, height: 0, weight: 0, balance: 0 })
const coachForm = ref({ id: '' as string | number, username: '', name: '', phone: '', gender: 1, age: 18, specialty: '', intro: '' })
const securityForm = ref({ oldPassword: '', newPassword: '', checkedPassword: '' })

const studentFormRules = {
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}
const coachFormRules = {
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  specialty: [{ required: true, message: '请输入专业方向', trigger: 'blur' }]
}
const securityRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 8, message: '密码长度不能少于8位', trigger: 'blur' }],
  checkedPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }, {
    validator: (_rule: any, value: string, callback: any) => {
      if (value !== securityForm.value.newPassword) callback(new Error('两次输入的密码不一致'))
      else callback()
    }, trigger: 'blur'
  }]
}

const loading = ref({ userInfo: false, submit: false, password: false, coachAppointments: false, equipmentReservations: false, avatar: false, purchasedCourses: false, review: false })
const coachAppointments = ref<(CoachAppointment & { canReview?: boolean })[]>([])
const equipmentReservations = ref<EquipmentReservation[]>([])
const coachAppointmentPagination = ref({ total: 0, pageSize: 10, currentPage: 1 })
const equipmentReservationPagination = ref({ total: 0, pageSize: 10, currentPage: 1 })

// 已购课程相关
const purchasedCourses = ref<(PurchasedCourse & { canReview?: boolean })[]>([])
const purchasedCoursesPagination = ref({ total: 0, pageSize: 6, currentPage: 1 })
const courseSearchKeyword = ref('')
const courseStatusFilter = ref<number | undefined>(undefined)

// 评价相关
const reviewDialogVisible = ref(false)
const reviewFormRef = ref()
const reviewForm = ref({ 
  courseId: 0, 
  courseName: '', 
  appointmentId: 0,
  coachName: '',
  reviewType: 1, // 1:课程评价 2:预约评价
  rating: 5, 
  content: '' 
})
const reviewRules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }]
}

const user = computed(() => authStore.user)
const displayName = computed(() => userDetailInfo.value?.name || authStore.displayName || user.value?.username || '')
const isStudent = computed(() => { const role = user.value?.role; return role === 'student' || role === 'user' })
const isCoach = computed(() => user.value?.role === 'coach')
const getUserRoleText = computed(() => {
  const roleMap: Record<string, string> = { admin: '管理员', coach: '教练', student: '学员', user: '学员' }
  return roleMap[user.value?.role || 'student'] || '学员'
})

const formatDate = (dateString?: string | Date) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}
const formatDateTime = (dateString?: string | Date) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

const handleMenuSelect = (index: string) => { 
  activeMenu.value = index
  if (index === 'appointments') loadCoachAppointments()
  else if (index === 'courses') loadPurchasedCourses()
}

// 查看课程详情
const viewCourseDetail = (courseId: number) => {
  router.push(`/courses/${courseId}`)
}

// 加载已购课程
const loadPurchasedCourses = async () => {
  const studentId = studentForm.value.id || user.value?.associatedUserId
  if (!studentId) return
  loading.value.purchasedCourses = true
  try {
    const res = await getPurchasedCourses({
      studentId,
      pageNum: purchasedCoursesPagination.value.currentPage,
      pageSize: purchasedCoursesPagination.value.pageSize,
      courseName: courseSearchKeyword.value || undefined,
      status: courseStatusFilter.value
    })
    const courses = res.records || []
    // 检查每个课程是否可以评价
    const coursesWithReviewStatus = await Promise.all(
      courses.map(async (course) => {
        if (course.status === 1) {
          try {
            const canReview = await canReviewCourse(studentId, course.courseId)
            return { ...course, canReview }
          } catch {
            return { ...course, canReview: false }
          }
        }
        return { ...course, canReview: false }
      })
    )
    purchasedCourses.value = coursesWithReviewStatus
    purchasedCoursesPagination.value.total = res.total || 0
  } catch (error) { console.error('获取已购课程失败:', error) }
  finally { loading.value.purchasedCourses = false }
}

const handlePurchasedCoursesPageChange = (page: number) => {
  purchasedCoursesPagination.value.currentPage = page
  loadPurchasedCourses()
}

// 打开课程评价对话框
const openReviewDialog = (course: PurchasedCourse) => {
  reviewForm.value = {
    courseId: course.courseId,
    courseName: course.courseName,
    appointmentId: 0,
    coachName: '',
    reviewType: 1,
    rating: 5,
    content: ''
  }
  reviewDialogVisible.value = true
}

// 打开预约评价对话框
const openAppointmentReviewDialog = (appointment: CoachAppointment) => {
  reviewForm.value = {
    courseId: 0,
    courseName: '',
    appointmentId: appointment.id,
    coachName: appointment.coachName || '',
    reviewType: 2,
    rating: 5,
    content: ''
  }
  reviewDialogVisible.value = true
}

// 提交评价
const submitReview = async () => {
  const valid = await reviewFormRef.value?.validate().catch(() => false)
  if (!valid) return

  const studentId = studentForm.value.id || user.value?.associatedUserId
  if (!studentId) {
    ElMessage.error('学员信息未加载完成')
    return
  }

  loading.value.review = true
  try {
    if (reviewForm.value.reviewType === 1) {
      // 课程评价
      await addCourseReview({
        studentId,
        courseId: reviewForm.value.courseId,
        reviewType: 1,
        rating: reviewForm.value.rating,
        content: reviewForm.value.content || undefined
      })
      // 刷新课程列表以更新评价状态
      loadPurchasedCourses()
    } else {
      // 预约评价
      await addCourseReview({
        studentId,
        appointmentId: reviewForm.value.appointmentId,
        reviewType: 2,
        rating: reviewForm.value.rating,
        content: reviewForm.value.content || undefined
      })
      // 刷新预约列表以更新评价状态
      loadCoachAppointments()
    }
    ElMessage.success('评价提交成功')
    reviewDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '评价提交失败')
  } finally {
    loading.value.review = false
  }
}
const handleAppointmentTabChange = (tab: string) => { if (tab === 'coach') loadCoachAppointments(); else if (tab === 'equipment') loadEquipmentReservations() }

// 头像上传
const handleAvatarChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  // 验证文件大小 (最大 5MB)
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }

  loading.value.avatar = true
  try {
    let newAvatarUrl: string
    const role = user.value?.role
    const isStudentRole = role === 'student' || role === 'user'
    
    if (isStudentRole) {
      // 学员上传头像
      const studentId = studentForm.value.id || user.value?.associatedUserId
      if (!studentId) {
        ElMessage.error('学员信息未加载完成')
        return
      }
      newAvatarUrl = await uploadStudentAvatar(studentId, file)
    } else {
      // 教练上传头像
      const coachId = coachForm.value.id || user.value?.associatedUserId
      if (!coachId) {
        ElMessage.error('教练信息未加载完成')
        return
      }
      newAvatarUrl = await uploadCoachAvatar(coachId, file)
    }

    console.log('头像上传返回的URL:', newAvatarUrl)
    
    // 添加时间戳参数强制浏览器刷新图片缓存
    const avatarWithTimestamp = newAvatarUrl + '?t=' + Date.now()
    
    // 直接使用返回的 URL 更新本地显示
    if (newAvatarUrl) {
      if (userDetailInfo.value) {
        userDetailInfo.value = { ...userDetailInfo.value, avatar: avatarWithTimestamp }
      }
      if (authStore.user) {
        authStore.user.avatar = avatarWithTimestamp
      }
    }
    ElMessage.success('头像更新成功')
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  } finally {
    loading.value.avatar = false
    // 清空 input 以便可以再次选择同一文件
    input.value = ''
  }
}

const loadStudentInfo = async () => {
  if (!user.value?.id) return
  loading.value.userInfo = true
  try {
    const studentId = user.value.associatedUserId
    console.log('loadStudentInfo - associatedUserId:', studentId)
    if (studentId) {
      const info = await getStudentByUserId(user.value.id)
      console.log('loadStudentInfo - 返回的学员信息:', info)
      userDetailInfo.value = info
      studentForm.value = {
        id: studentId,
        username: info?.username || user.value.username,
        name: info?.name || '',
        phone: info?.phone || '',
        gender: info?.gender || 1,
        height: info?.height || 0,
        weight: info?.weight || 0,
        balance: info?.balance || 0
      }
    } else {
      console.error('获取学员信息失败: associatedUserId为空')
    }
  } catch (error) { console.error('获取学员信息失败:', error) }
  finally { loading.value.userInfo = false }
}

const loadCoachInfo = async () => {
  if (!user.value?.id) return
  loading.value.userInfo = true
  try {
    const coachId = user.value.associatedUserId
    console.log('loadCoachInfo - associatedUserId:', coachId)
    if (coachId) {
      const info = await getCoachByUserId(user.value.id)
      console.log('loadCoachInfo - 返回的教练信息:', info)
      userDetailInfo.value = info
      coachForm.value = {
        id: coachId,
        username: info?.username || user.value.username,
        name: info?.name || '',
        phone: info?.phone || '',
        gender: info?.gender || 1,
        age: info?.age || 18,
        specialty: info?.specialty || '',
        intro: info?.intro || ''
      }
    } else {
      console.error('获取教练信息失败: associatedUserId为空')
    }
  } catch (error) { console.error('获取教练信息失败:', error) }
  finally { loading.value.userInfo = false }
}

const updateStudentInfo = async () => {
  const valid = await studentFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!studentForm.value.id) {
    ElMessage.error('学员信息未加载完成，请刷新页面重试')
    return
  }
  loading.value.submit = true
  try {
    console.log('updateStudentInfo - id:', studentForm.value.id)
    await updateStudent(studentForm.value.id, { name: studentForm.value.name, gender: studentForm.value.gender, phone: studentForm.value.phone, height: studentForm.value.height, weight: studentForm.value.weight })
    ElMessage.success('信息更新成功')
    if (authStore.user) authStore.user.name = studentForm.value.name
  } catch (error) { console.error('更新学员信息失败:', error) }
  finally { loading.value.submit = false }
}

const updateCoachInfo = async () => {
  const valid = await coachFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!coachForm.value.id) {
    ElMessage.error('教练信息未加载完成，请刷新页面重试')
    return
  }
  loading.value.submit = true
  try {
    console.log('updateCoachInfo - id:', coachForm.value.id)
    await updateCoach(coachForm.value.id, { name: coachForm.value.name, gender: coachForm.value.gender, phone: coachForm.value.phone, age: coachForm.value.age, specialty: coachForm.value.specialty, intro: coachForm.value.intro })
    ElMessage.success('信息更新成功')
    if (authStore.user) authStore.user.name = coachForm.value.name
  } catch (error) { console.error('更新教练信息失败:', error) }
  finally { loading.value.submit = false }
}

const handleChangePassword = async () => {
  const valid = await securityFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value.password = true
  try {
    await changePassword({ oldPassword: securityForm.value.oldPassword, newPassword: securityForm.value.newPassword, checkedPassword: securityForm.value.checkedPassword })
    ElMessage.success('密码修改成功，请重新登录')
    securityForm.value = { oldPassword: '', newPassword: '', checkedPassword: '' }
    authStore.logout()
    router.push('/login')
  } catch (error) { console.error('修改密码失败:', error) }
  finally { loading.value.password = false }
}

const loadCoachAppointments = async () => {
  const studentId = studentForm.value.id || user.value?.associatedUserId
  if (!studentId) return
  loading.value.coachAppointments = true
  try {
    const res = await listStudentCoachAppointment({ studentId, pageNum: coachAppointmentPagination.value.currentPage, pageSize: coachAppointmentPagination.value.pageSize })
    const appointments = res.records || []
    // 检查每个已确认的预约是否可以评价
    const appointmentsWithReviewStatus = await Promise.all(
      appointments.map(async (appointment) => {
        if (appointment.status === 1) {
          try {
            const canReview = await canReviewAppointment(studentId, appointment.id)
            return { ...appointment, canReview }
          } catch {
            return { ...appointment, canReview: false }
          }
        }
        return { ...appointment, canReview: false }
      })
    )
    coachAppointments.value = appointmentsWithReviewStatus
    coachAppointmentPagination.value.total = res.total || 0
  } catch (error) { console.error('获取教练预约记录失败:', error) }
  finally { loading.value.coachAppointments = false }
}

const loadEquipmentReservations = async () => {
  const studentId = studentForm.value.id || user.value?.associatedUserId
  if (!studentId) return
  loading.value.equipmentReservations = true
  try {
    const res = await listStudentEquipmentReservation({ studentId, pageNum: equipmentReservationPagination.value.currentPage, pageSize: equipmentReservationPagination.value.pageSize })
    equipmentReservations.value = res.records || []
    equipmentReservationPagination.value.total = res.total || 0
  } catch (error) { console.error('获取器材预约记录失败:', error) }
  finally { loading.value.equipmentReservations = false }
}

const handleCancelCoachAppointment = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要取消该预约吗？', '提示', { type: 'warning' })
    await cancelCoachAppointment(id)
    ElMessage.success('预约已取消')
    loadCoachAppointments()
  } catch (error) { if (error !== 'cancel') console.error('取消预约失败:', error) }
}

const handleCancelEquipmentReservation = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要取消该预约吗？', '提示', { type: 'warning' })
    await cancelEquipmentReservation(id)
    ElMessage.success('预约已取消')
    loadEquipmentReservations()
  } catch (error) { if (error !== 'cancel') console.error('取消预约失败:', error) }
}

const handleCoachAppointmentPageChange = (page: number) => { coachAppointmentPagination.value.currentPage = page; loadCoachAppointments() }
const handleEquipmentReservationPageChange = (page: number) => { equipmentReservationPagination.value.currentPage = page; loadEquipmentReservations() }

const getCoachAppointmentStatusType = (status: number) => ({ 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[status] || 'info')
const getCoachAppointmentStatusText = (status: number) => ({ 0: '待确认', 1: '已确认', 2: '已拒绝', 3: '已取消' }[status] || '未知')
const getEquipmentReservationStatusType = (status: number) => ({ 1: 'success', 2: 'info', 3: 'success' }[status] || 'info')
const getEquipmentReservationStatusText = (status: number) => ({ 1: '预约成功', 2: '已取消', 3: '已完成' }[status] || '未知')

onMounted(async () => {
  // 确保用户信息已加载，如果没有 associatedUserId 也需要重新获取
  if (!authStore.user || !authStore.user.associatedUserId) {
    await authStore.initAuth()
  }

  // 直接根据 authStore.user.role 判断
  const role = authStore.user?.role
  const isStudentRole = role === 'student' || role === 'user'
  const isCoachRole = role === 'coach'

  if (isStudentRole) {
    await loadStudentInfo()
  } else if (isCoachRole) {
    await loadCoachInfo()
  }
})
</script>

<style scoped>
.profile-container {
  min-height: calc(100vh - 140px);
  background-color: #f5f7fa;
}

.profile-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 0;
  color: white;
}

.profile-banner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.profile-info {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  position: relative;
  margin-right: 30px;
}

.user-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 4px solid rgba(255, 255, 255, 0.3);
  object-fit: cover;
}

.avatar-upload-btn {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.3s;
}

.avatar-upload-btn:hover {
  background: rgba(0, 0, 0, 0.8);
}

.avatar-upload-btn.uploading {
  cursor: not-allowed;
  opacity: 0.7;
}

.avatar-upload-btn input {
  display: none;
}

.user-details h1 {
  font-size: 32px;
  margin-bottom: 10px;
  font-weight: 600;
}

.user-role {
  font-size: 18px;
  margin-bottom: 10px;
  opacity: 0.9;
}

.join-date {
  font-size: 18px;
  opacity: 0.8;
}

.profile-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
  display: flex;
  gap: 30px;
}

.profile-sidebar {
  width: 250px;
  flex-shrink: 0;
}

.profile-menu {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.profile-main {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.profile-section h2 {
  margin-bottom: 20px;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.user-info-form,
.security-form {
  max-width: 500px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 992px) {
  .profile-content {
    flex-direction: column;
  }

  .profile-sidebar {
    width: 100%;
  }

  .profile-menu {
    display: flex;
    flex-wrap: wrap;
  }

  .profile-menu .el-menu-item {
    flex: 1;
    text-align: center;
  }
}

/* 已购课程样式 */
.courses-filter {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.purchased-courses-list {
  min-height: 200px;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.purchased-course-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.purchased-course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.purchased-course-card .course-image {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.purchased-course-card .course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.purchased-course-card .status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.purchased-course-card .course-info {
  padding: 15px;
}

.purchased-course-card .course-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.purchased-course-card .course-meta {
  display: flex;
  gap: 15px;
  margin-bottom: 8px;
}

.purchased-course-card .meta-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #666;
}

.purchased-course-card .meta-icon {
  width: 14px;
  height: 14px;
  margin-right: 4px;
  filter: invert(45%) sepia(98%) saturate(1500%) hue-rotate(196deg) brightness(100%) contrast(96%);
}

.purchased-course-card .course-coach,
.purchased-course-card .course-price,
.purchased-course-card .course-time {
  font-size: 13px;
  color: #666;
  margin-bottom: 5px;
}

.purchased-course-card .course-price {
  color: #f56c6c;
  font-weight: 500;
}

.purchased-course-card .course-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.purchased-course-card .course-actions .el-button {
  flex: 1;
}

.purchased-course-card .course-actions .el-tag {
  flex: 1;
  text-align: center;
  height: 32px;
  line-height: 30px;
}

@media (max-width: 768px) {
  .profile-header {
    padding: 20px 0;
  }

  .profile-info {
    flex-direction: column;
    text-align: center;
  }

  .avatar-wrapper {
    margin-right: 0;
    margin-bottom: 20px;
  }

  .profile-content {
    padding: 20px 10px;
  }

  .profile-main {
    padding: 20px;
  }

  .courses-grid {
    grid-template-columns: 1fr;
  }
}
</style>
