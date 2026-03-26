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
              <!-- 教练类型/特长下拉框 -->
              <div class="specialty-filter">
                <span class="filter-label">教练专业：</span>
                <el-select v-model="activeSpecialty" placeholder="请选择教练类型" clearable @change="setActiveSpecialty">
                  <el-option label="全部教练" value=""></el-option>
                  <el-option v-for="coachType in coachTypes" :key="coachType.value" :label="coachType.label"
                    :value="coachType.value">
                    <el-tooltip :content="coachType.description" placement="right">
                      <span>{{ coachType.label }}</span>
                    </el-tooltip>
                  </el-option>
                </el-select>
              </div>

              <div class="gender-filter">
                <span class="filter-label">性别：</span>
                <el-select v-model="selectedGender" placeholder="选择性别" clearable>
                  <el-option label="男" :value="1"></el-option>
                  <el-option label="女" :value="2"></el-option>
                </el-select>
              </div>

              <div class="age-filter">
                <span class="filter-label">年龄：</span>
                <el-select v-model="selectedAgeRange" placeholder="选择年龄段" clearable @change="handleAgeRangeChange">
                  <el-option v-for="range in ageRangeOptions" :key="range.value" :label="range.label" :value="range.value" />
                </el-select>
              </div>

              <div class="search-box">
                <el-input v-model="searchKeyword" placeholder="搜索教练姓名" prefix-icon="Search" clearable @keyup.enter="loadCoaches"></el-input>
              </div>
              <el-button @click="resetFilters">重置</el-button>
            </div>
          </div>
        </section>

        <!-- 加载状态-->
        <div v-if="coachStore.loading" class="loading-container">
          <el-loading :loading="true" text="加载中..."></el-loading>
        </div>

        <!-- 错误状态-->
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
            <!-- 无数据状态-->
            <div v-if="!coachStore.hasCoaches" class="empty-container">
              <el-empty description="暂无教练数据"></el-empty>
            </div>

            <!-- 教练列表 -->
            <div v-else class="coaches-grid">
              <div class="coach-card" v-for="coach in coachStore.coaches" :key="coach.id"
                @click="viewCoachDetail(coach.id)">
                <div class="coach-avatar">
                  <img :src="coach.avatar || '/avatar-placeholder.svg'" :alt="coach.name" />
                </div>
                <div class="coach-info">
                  <h3 class="coach-name">{{ coach.name }}</h3>
                  <div class="coach-specialty">{{ getCoachSpecialtyName(coach.specialty) || '综合健身' }}</div>
                  <p class="coach-description">{{ coach.intro || '暂无简介' }}</p>
                  <div class="coach-meta">
                    <div class="meta-item">
                      <img src="/gender.svg" alt="性别" class="icon-svg icon-gender" />
                      <span>{{ getGenderName(coach.gender) }}</span>
                    </div>
                    <div class="meta-item">
                      <img src="/age.svg" alt="年龄" class="icon-svg icon-age" />
                      <span>{{ coach.age || '-' }}岁</span>
                    </div>
                    <div class="meta-item">
                      <img src="/phone.svg" alt="电话" class="icon-svg icon-phone" />
                      <span>{{ coach.phone || '-' }}</span>
                    </div>
                  </div>
                  <div class="coach-footer">
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

    <!-- 预约教练对话框 -->
    <el-dialog v-model="bookingDialogVisible" title="预约教练" width="500px" :close-on-click-modal="false">
      <el-form :model="bookingForm" :rules="bookingRules" ref="bookingFormRef" label-width="100px">
        <el-form-item label="教练" prop="coachName">
          <el-input v-model="bookingForm.coachName" disabled />
        </el-form-item>
        <el-form-item label="开始时间" prop="appointTime">
          <el-date-picker v-model="bookingForm.appointTime" type="datetime" placeholder="选择开始时间"
            :disabled-date="disabledDate" style="width: 100%" format="YYYY-MM-DD HH:mm" />
        </el-form-item>
        <el-form-item label="预约时长" prop="duration">
          <el-radio-group v-model="bookingForm.duration" class="duration-radio-group">
            <el-radio-button v-for="opt in durationOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注信息" prop="message">
          <el-input v-model="bookingForm.message" type="textarea" :rows="3" placeholder="请输入预约备注（可选）" />
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
import { ref, onMounted, watch, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useCoachStore } from '@/stores/coach'
import { useAuthStore } from '@/stores/auth'
import { bookCoach as bookCoachApi } from '@/api/coach'
import type { Coach, CoachQueryParams } from '@/api/coach'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'
import { coachSpecialtyOptions, getCoachSpecialtyName, getGenderName } from '@/constants/categories'

// 使用路由和教练状态管理
const router = useRouter()
const coachStore = useCoachStore()
const authStore = useAuthStore()

// 预约相关状态
const bookingDialogVisible = ref(false)
const bookingLoading = ref(false)
const bookingFormRef = ref<FormInstance>()
const selectedCoach = ref<Coach | null>(null)
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
  return time.getTime() < Date.now() - 8.64e7
}

// 使用统一的教练类型数据
const coachTypes = coachSpecialtyOptions



// 年龄区间选项
const ageRangeOptions = [
  { label: '20岁以下', value: '0-19', min: 0, max: 19 },
  { label: '20-30岁', value: '20-29', min: 20, max: 29 },
  { label: '30-40岁', value: '30-39', min: 30, max: 39 },
  { label: '40-50岁', value: '40-49', min: 40, max: 49 },
  { label: '50岁以上', value: '50-100', min: 50, max: 100 }
]

// 筛选状态
const activeSpecialty = ref('')
const selectedGender = ref<number | undefined>(undefined)
const selectedAgeRange = ref('')
const minAge = ref<number | undefined>(undefined)
const maxAge = ref<number | undefined>(undefined)
const searchKeyword = ref('')

// 处理年龄区间变化
const handleAgeRangeChange = (value: string) => {
  if (!value) {
    minAge.value = undefined
    maxAge.value = undefined
  } else {
    const range = ageRangeOptions.find(r => r.value === value)
    if (range) {
      minAge.value = range.min
      maxAge.value = range.max
    }
  }
}

// 分页状态
const currentPage = ref(1)
const pageSize = ref(9)

// 设置活动专长
const setActiveSpecialty = () => {
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
const viewCoachDetail = (id: string | number) => {
  router.push(`/coaches/${id}`)
}

// 预约教练
const bookCoach = (coach: Coach) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录后再预约教练')
    router.push({ name: 'auth', query: { redirect: '/coaches' } })
    return
  }

  if (authStore.user?.role === 'coach') {
    ElMessage.warning('教练不能预约其他教练')
    return
  }

// 保存选中的教练并打开对话框
  selectedCoach.value = coach
  bookingForm.coachName = coach.name
  bookingForm.appointTime = null
  bookingForm.duration = 60
  bookingForm.message = ''
  bookingDialogVisible.value = true
}

// 提交预约
const submitBooking = async () => {
  const currentSelectedCoach = selectedCoach.value
  if (!bookingFormRef.value || !currentSelectedCoach) return

  await bookingFormRef.value.validate(async (valid) => {
    if (!valid) return

    bookingLoading.value = true
    try {
      const studentId = authStore.user?.associatedUserId
      if (!studentId) {
        ElMessage.error('无法获取学员信息，请重新登录')
        return
      }

      // 计算结束时间
      const appointTime = new Date(bookingForm.appointTime!)
      const endTime = new Date(appointTime.getTime() + bookingForm.duration * 60 * 1000)

      await bookCoachApi({
        coachId: currentSelectedCoach.id,
        studentId: studentId,
        appointTime: appointTime.toISOString(),
        endTime: endTime.toISOString(),
        message: bookingForm.message || undefined
      })

      ElMessage.success('预约成功！请等待教练确认')
      bookingDialogVisible.value = false
    } catch (error: unknown) {
      ElMessage.error(error instanceof Error ? error.message : '预约失败，请稍后重试')
    } finally {
      bookingLoading.value = false
    }
  })
}

// 加载教练数据
const loadCoaches = () => {
  // 构建查询参数，与后端教练分页查询模型对应
  const params: Record<string, string | number> = {
    pageNum: currentPage.value,
    pageSize: pageSize.value
  }

  // 添加姓名搜索
  if (searchKeyword.value) {
    params.name = searchKeyword.value
  }

  // 添加性别筛选
  if (selectedGender.value !== undefined) {
    params.gender = selectedGender.value
  }

  // 添加年龄范围筛选
  if (minAge.value !== undefined) {
    params.minAge = minAge.value
  }
  if (maxAge.value !== undefined) {
    params.maxAge = maxAge.value
  }

  // 添加专长筛选
  if (activeSpecialty.value) {
    params.specialty = activeSpecialty.value
  }

  // 调用接口获取教练数据
  coachStore.fetchCoaches(params as unknown as CoachQueryParams)
}

// 加载教练专长
const loadSpecialties = () => {
  coachStore.fetchSpecialties()
}

// 防抖定时器
let searchDebounceTimer: ReturnType<typeof setTimeout> | null = null

// 监听下拉筛选条件变化，立即加载
watch([activeSpecialty, selectedGender, selectedAgeRange], () => {
  currentPage.value = 1
  loadCoaches()
})

// 监听搜索关键词变化，添加防抖（500ms）
watch(searchKeyword, () => {
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer)
  }
  searchDebounceTimer = setTimeout(() => {
    currentPage.value = 1
    loadCoaches()
  }, 500)
})

// 重置所有筛选条件
const resetFilters = () => {
  activeSpecialty.value = ''
  selectedGender.value = undefined
  selectedAgeRange.value = ''
  minAge.value = undefined
  maxAge.value = undefined
  searchKeyword.value = ''
  currentPage.value = 1
  loadCoaches()
}

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
  --primary: #f97316;
  --primary-dark: #ea580c;
  --ink: #0f172a;
  --muted: #475569;
  --line: #e2e8f0;
  --surface: #ffffff;
  --soft: #f8fafc;
  min-height: 100vh;
}

.page-header {
  background:
    radial-gradient(circle at 8% 20%, rgba(249, 115, 22, 0.2), transparent 40%),
    linear-gradient(135deg, #0f172a 0%, #1e293b 48%, #334155 100%);
  color: #f8fafc;
  text-align: center;
  padding: 88px 0 70px;
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
}

.page-subtitle {
  margin: 14px auto 0;
  max-width: 700px;
  color: #e2e8f0;
  font-size: 18px;
  line-height: 1.7;
}

.filter-section {
  position: sticky;
  top: 74px;
  z-index: 90;
  border-bottom: 1px solid var(--line);
  background: rgba(248, 250, 252, 0.95);
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

.gender-filter,
.age-filter,
.specialty-filter {
  display: flex;
  align-items: center;
}

.filter-label {
  margin-right: 6px;
  color: var(--muted);
  font-weight: 600;
  font-size: 14px;
}

.specialty-filter :deep(.el-select) { width: 220px; }
.gender-filter :deep(.el-select) { width: 120px; }
.age-filter :deep(.el-select) { width: 140px; }
.search-box { flex: 1; min-width: 220px; }

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

.main-content {
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
}

.loading-container,
.error-container,
.empty-container {
  min-height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.coaches-section {
  padding: 40px 0 72px;
}

.coaches-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 22px;
  margin-bottom: 42px;
}

.coach-card {
  border-radius: 18px;
  background: var(--surface);
  border: 1px solid var(--line);
  overflow: hidden;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  cursor: pointer;
}

.coach-card:hover {
  transform: translateY(-5px);
  border-color: rgba(249, 115, 22, 0.42);
  box-shadow: 0 18px 32px rgba(15, 23, 42, 0.14);
}

.coach-avatar {
  height: 230px;
  overflow: hidden;
  position: relative;
}

.coach-avatar::after {
  content: '';
  position: absolute;
  inset: auto 0 0;
  height: 84px;
  background: linear-gradient(transparent, rgba(2, 6, 23, 0.3));
}

.coach-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.25s ease;
}

.coach-card:hover .coach-avatar img { transform: scale(1.04); }

.coach-info {
  padding: 20px;
}

.coach-name {
  margin: 0 0 8px;
  color: var(--ink);
  font-size: 22px;
  font-weight: 800;
}

.coach-specialty {
  display: inline-flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid #fdba74;
  background: #fff7ed;
  color: #9a3412;
  font-size: 12px;
  font-weight: 700;
}

.coach-description {
  margin: 0 0 14px;
  color: var(--muted);
  line-height: 1.65;
  min-height: 44px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.coach-meta {
  background: var(--soft);
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 10px;
  display: grid;
  gap: 8px;
  margin-bottom: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  color: #64748b;
  font-size: 13px;
}

.meta-item .icon-svg {
  width: 14px;
  height: 14px;
  margin-right: 8px;
}

.coach-footer {
  border-top: 1px solid var(--line);
  padding-top: 14px;
  display: flex;
  justify-content: flex-end;
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

.duration-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.duration-radio-group .el-radio-button {
  margin-right: 0;
}

.duration-radio-group .el-radio-button__inner {
  border-radius: 6px !important;
  border: 1px solid #dcdfe6;
}

.duration-radio-group .el-radio-button:first-child .el-radio-button__inner,
.duration-radio-group .el-radio-button:last-child .el-radio-button__inner {
  border-radius: 6px !important;
}

.pagination-container {
  display: flex;
  justify-content: center;
}

.icon-svg {
  vertical-align: middle;
}

@media (max-width: 1024px) {
  .filter-section { position: static; }
}

@media (max-width: 768px) {
  .page-header { padding: 66px 0 54px; }

  .page-subtitle { font-size: 16px; }

  .filter-options {
    flex-direction: column;
    align-items: stretch;
  }

  .specialty-filter,
  .gender-filter,
  .age-filter {
    justify-content: space-between;
  }

  .specialty-filter :deep(.el-select),
  .gender-filter :deep(.el-select),
  .age-filter :deep(.el-select) {
    width: 68%;
  }

  .search-box { min-width: auto; }

  .coaches-grid {
    grid-template-columns: 1fr;
  }

  .book-btn { width: 100%; }
}

@media (prefers-reduced-motion: reduce) {
  .coach-card,
  .coach-avatar img {
    transition: none;
  }

  .coach-card:hover {
    transform: none;
  }
}
</style>
