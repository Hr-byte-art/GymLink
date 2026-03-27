<template>
  <AppLayout>
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">健身器材</h1>
        <p class="page-subtitle">探索我们专业的健身器材，提升您的健身体验</p>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 器筛选区域 -->
      <section class="filter-section">
        <div class="filter-container">
          <div class="filter-tabs">
            <div class="filter-tab" :class="{ active: activeCategory === 'all' }" @click="setActiveCategory('all')">
              全部器材
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === '1' }" @click="setActiveCategory('1')">
              有氧健身器材
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === '2' }" @click="setActiveCategory('2')">
              力量训练器材
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === '3' }" @click="setActiveCategory('3')">
              功能性训练器械
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === '4' }" @click="setActiveCategory('4')">
              小型健身器械
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === '5' }" @click="setActiveCategory('5')">
              康复与辅助器械
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === '6' }" @click="setActiveCategory('6')">
              其他辅助设备
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === '7' }" @click="setActiveCategory('7')">
              商用专用器材
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === '8' }" @click="setActiveCategory('8')">
              家用专用器材
            </div>
          </div>

          <!-- 子分类筛选 -->
          <div v-if="activeCategory !== 'all' && subCategories[activeCategory]" class="sub-filter-tabs">
            <div class="filter-tab" :class="{ active: activeSubCategory === sub.value }"
              v-for="sub in subCategories[activeCategory]" :key="sub.value" @click="setActiveSubCategory(sub.value)">
              {{ sub.label }}
            </div>
          </div>

          <div class="filter-options">
            <div class="status-filter">
              <span class="filter-label">状态：</span>
              <el-select v-model="selectedStatus" placeholder="选择状态" clearable>
                <el-option label="可用" :value="1"></el-option>
                <el-option label="维护中" :value="2"></el-option>
              </el-select>
            </div>

            <div class="location-filter">
              <span class="filter-label">位置：</span>
              <el-input v-model="selectedLocation" placeholder="请输入位置" clearable style="width: 200px;" />
            </div>



            <div class="search-box">
              <el-input v-model="searchKeyword" placeholder="搜索器材名称或型号" prefix-icon="Search" clearable></el-input>
            </div>
          </div>
        </div>
      </section>

      <!-- 加载状态-->
      <div v-if="equipmentStore.loading" class="loading-container">
        <el-loading :loading="true" text="加载中..."></el-loading>
      </div>

      <!-- 错误状态-->
      <div v-else-if="equipmentStore.error" class="error-container">
        <el-result icon="warning" title="加载失败" :sub-title="equipmentStore.error">
          <template #extra>
            <el-button type="primary" @click="loadEquipment">重新加载</el-button>
          </template>
        </el-result>
      </div>

      <!-- 器材列表区域 -->
      <section v-else class="equipment-section">
        <div class="section-container">
          <!-- 无数据状态-->
          <div v-if="!equipmentStore.hasEquipment" class="empty-container">
            <el-empty description="暂无器材数据"></el-empty>
          </div>

          <!-- 器材列表 -->
          <div v-else class="equipment-grid">
            <div class="equipment-card" v-for="equipment in equipmentStore.equipmentList" :key="equipment.id"
              @click="viewEquipmentDetail(equipment.id)">
              <div class="equipment-image">
                <img :src="equipment.image" :alt="equipment.name" />
                <div class="equipment-category">{{ equipment.location }}</div>
                <div class="equipment-status" :class="'status-' + equipment.status">
                  {{ getStatusText(equipment.status) }}
                </div>
              </div>
              <div class="equipment-content">
                <h3 class="equipment-title">{{ equipment.name }}</h3>

                <p class="equipment-description">{{ equipment.description }}</p>
                <div class="equipment-info">
                  <div class="info-item">
                    <i class="icon-location"></i>
                    <span>{{ equipment.location }}</span>
                  </div>
                </div>
                <div class="equipment-footer">
                  <div class="equipment-price">
                    <span class="price-label">总数量</span>
                    <span class="price-value">{{ equipment.totalCount }}</span>
                    <span class="price-unit">台</span>
                  </div>
                  <div class="equipment-actions">
                    <el-button type="primary" class="reserve-btn" :disabled="equipment.status !== 1"
                      @click.stop="reserveEquipment({ id: equipment.id, name: equipment.name })">
                      预约器材
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="equipmentStore.hasEquipment" class="pagination-container">
            <el-pagination background layout="prev, pager, next" :total="equipmentStore.total" :page-size="pageSize"
              :current-page="currentPage" @current-change="handlePageChange">
            </el-pagination>
          </div>
        </div>
      </section>
    </main>

    <!-- 预约器材对话框 -->
    <el-dialog v-model="reservationDialogVisible" title="预约器材" width="500px" :close-on-click-modal="false">
      <el-form :model="reservationForm" :rules="reservationRules" ref="reservationFormRef" label-width="100px">
        <el-form-item label="器材名称">
          <el-input v-model="reservationForm.equipmentName" disabled />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="reservationForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            :disabled-date="disabledDate"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="使用时长" prop="duration">
          <el-radio-group v-model="reservationForm.duration" class="duration-radio-group">
            <el-radio-button v-for="opt in durationOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="结束时间">
          <el-input :value="computedEndTime" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reservationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReservation" :loading="reservationLoading">确认预约</el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useEquipmentStore } from '@/stores/equipment'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import AppLayout from '@/components/AppLayout.vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { reserveEquipment as reserveEquipmentApi } from '@/api/equipment'

// 使用器材状态管理
const equipmentStore = useEquipmentStore()
const authStore = useAuthStore()
const router = useRouter()

// 预约相关状态
const reservationDialogVisible = ref(false)
const reservationLoading = ref(false)
const reservationFormRef = ref<FormInstance>()
const currentEquipment = ref<{ id: number; name: string } | null>(null)
const reservationForm = reactive({
  equipmentName: '',
  startTime: null as string | null,
  duration: 60 // 默认1小时
})

// 时长选项（分钟）
const durationOptions = [
  { label: '30分钟', value: 30 },
  { label: '1小时', value: 60 },
  { label: '1.5小时', value: 90 },
  { label: '2小时', value: 120 }
]

const reservationRules = reactive<FormRules>({
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  duration: [{ required: true, message: '请选择使用时长', trigger: 'change' }]
})

// 禁用过去的日期
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 计算结束时间
const computedEndTime = computed(() => {
  if (!reservationForm.startTime) return ''
  const startDate = new Date(reservationForm.startTime)
  const endDate = new Date(startDate.getTime() + reservationForm.duration * 60 * 1000)
  return endDate.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
})

// 筛选状态
const activeCategory = ref('all')
const activeSubCategory = ref('')
const selectedStatus = ref<number | undefined>(undefined)
const selectedLocation = ref('')
const searchKeyword = ref('')

// 分页状态
const currentPage = ref(1)
const pageSize = ref(12)

// 子分类数据
const subCategories: Record<string, Array<{ value: string; label: string }>> = {
  '1': [
    { value: '1-1', label: '跑步机' },
    { value: '1-2', label: '椭圆机' },
    { value: '1-3', label: '动感单车' },
    { value: '1-4', label: '划船机' },
    { value: '1-5', label: '健身车' },
    { value: '1-6', label: '登楼机' },
    { value: '1-7', label: '体适能运动机' }
  ],
  '2': [
    { value: '2-1', label: '固定器械' },
    { value: '2-2', label: '自由重量器材' },
    { value: '2-3', label: '综合训练器材' }
  ],
  '3': [],
  '4': [],
  '5': [],
  '6': [],
  '7': [],
  '8': []
}

// 设置活动类别
const setActiveCategory = (category: string) => {
  activeCategory.value = category
  activeSubCategory.value = '' // 重置子分类
  currentPage.value = 1
}

// 设置活动子类
const setActiveSubCategory = (subCategory: string) => {
  activeSubCategory.value = subCategory
  currentPage.value = 1
}

// 处理分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 获取状态文本
const getStatusText = (status: number) => {
  const statusMap: { [key: number]: string } = {
    1: '可用',
    2: '维护中'
  }
  return statusMap[status] || '未知'
}

// 查看器材详情
const viewEquipmentDetail = (id: number) => {
  router.push(`/equipment/${id}`)
}

// 预约器材 - 打开预约对话框
const reserveEquipment = (equipment: { id: number; name: string }) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录后再预约器材')
    router.push({ name: 'auth', query: { redirect: '/equipment' } })
    return
  }

  if (authStore.user?.role === 'coach') {
    ElMessage.warning('教练不能预约器材')
    return
  }

  // 设置当前器材并重置表单
  currentEquipment.value = equipment
  reservationForm.equipmentName = equipment.name
  reservationForm.startTime = null
  reservationForm.duration = 60
  reservationDialogVisible.value = true
}

// 提交预约
const submitReservation = async () => {
  const selectedEquipment = currentEquipment.value
  if (!reservationFormRef.value || !selectedEquipment) return

  await reservationFormRef.value.validate(async (valid) => {
    if (!valid) return

    reservationLoading.value = true
    try {
      const studentId = authStore.user?.associatedUserId
      if (!studentId) {
        ElMessage.error('无法获取学员信息，请重新登录')
        return
      }

      const startDate = new Date(reservationForm.startTime!)
      const endDate = new Date(startDate.getTime() + reservationForm.duration * 60 * 1000)

      await reserveEquipmentApi({
        equipmentId: selectedEquipment.id,
        studentId: Number(studentId),
        startTime: startDate.toISOString(),
        endTime: endDate.toISOString()
      })

      ElMessage.success('预约成功')
      reservationDialogVisible.value = false
    } catch {
      // 错误已在请求拦截器中处理，这里不重复提示
    } finally {
      reservationLoading.value = false
    }
  })
}

// 主分类标签映射
const mainCategoryLabels: { [key: string]: string } = {
  '1': '有氧健身器材',
  '2': '力量训练器材',
  '3': '功能性训练器械',
  '4': '小型健身器械',
  '5': '康复与辅助器械',
  '6': '其他辅助设备',
  '7': '商用专用器材',
  '8': '家用专用器材'
}

// 子分类标签映射
const subCategoryLabelMap: { [key: string]: string } = {
  '1-1': '跑步机',
  '1-2': '椭圆机',
  '1-3': '动感单车',
  '1-4': '划船机',
  '1-5': '健身车',
  '1-6': '登楼机',
  '1-7': '体适能运动机',
  '2-1': '固定器械',
  '2-2': '自由重量器材',
  '2-3': '综合训练器材'
}

// 加载器材数据
const loadEquipment = () => {
  // 构建查询参数，与后端器材分页查询模型对应
  const params: Record<string, string | number> = {
    pageNum: currentPage.value,
    pageSize: pageSize.value
  }

  // 添加类别筛选（对应后端分类字段）
  if (activeCategory.value !== 'all') {
    // 根据后台字段，需要传递编号（如 '1', '1-1'）而不是中文描述
    if (activeSubCategory.value) {
      params.type = activeSubCategory.value
    } else {
      params.type = activeCategory.value
    }
  }

  // 添加状态筛选
  if (selectedStatus.value !== undefined) {
    params.status = selectedStatus.value
  }

  // 添加位置筛选
  if (selectedLocation.value) {
    params.location = selectedLocation.value
  }

  // 添加名称搜索（对应后端名称字段）
  if (searchKeyword.value) {
    params.name = searchKeyword.value
  }

  // 调用接口获取器材数据
  equipmentStore.fetchEquipmentList(params)
}

// 监听筛选条件变化，重新加载数据
watch([activeCategory, activeSubCategory, selectedStatus, selectedLocation, searchKeyword], () => {
  currentPage.value = 1
  loadEquipment()
})

// 监听页码变化，重新加载数据
watch(currentPage, () => {
  loadEquipment()
})

// 组件挂载时加载数据
onMounted(() => {
  loadEquipment()
})
</script>

<style scoped>
.equipment-container {
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
    radial-gradient(circle at 10% 20%, rgba(249, 115, 22, 0.2), transparent 42%),
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

.filter-tabs,
.sub-filter-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tabs { margin-bottom: 12px; }
.sub-filter-tabs { margin-bottom: 12px; }

.filter-tab {
  border-radius: 999px;
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #334155;
  padding: 7px 13px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-tab:hover {
  border-color: #fdba74;
  color: #9a3412;
  background: #fff7ed;
}

.filter-tab.active {
  border-color: transparent;
  color: #fff;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
}

.sub-filter-tabs .filter-tab {
  font-size: 12px;
  padding: 5px 12px;
  background: #f8fafc;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.status-filter,
.location-filter {
  display: flex;
  align-items: center;
}

.filter-label {
  margin-right: 6px;
  color: var(--muted);
  font-weight: 600;
  font-size: 14px;
}

.status-filter :deep(.el-select) { width: 150px; }
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

.equipment-section {
  padding: 40px 0 72px;
}

.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 22px;
  margin-bottom: 42px;
}

.equipment-card {
  border-radius: 18px;
  background: var(--surface);
  border: 1px solid var(--line);
  overflow: hidden;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  cursor: pointer;
}

.equipment-card:hover {
  transform: translateY(-5px);
  border-color: rgba(249, 115, 22, 0.42);
  box-shadow: 0 18px 32px rgba(15, 23, 42, 0.14);
}

.equipment-image {
  position: relative;
  height: 210px;
  overflow: hidden;
}

.equipment-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.25s ease;
}

.equipment-card:hover .equipment-image img {
  transform: scale(1.04);
}

.equipment-category,
.equipment-status {
  position: absolute;
  top: 14px;
  font-size: 12px;
  font-weight: 700;
  padding: 5px 10px;
  border-radius: 999px;
  color: #fff;
}

.equipment-category {
  left: 14px;
  background: rgba(2, 6, 23, 0.66);
}

.equipment-status {
  right: 14px;
}

.equipment-status.status-1 {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
}

.equipment-status.status-2 {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.equipment-content {
  padding: 20px;
}

.equipment-title {
  margin: 0 0 10px;
  color: var(--ink);
  font-size: 22px;
  font-weight: 800;
}

.equipment-description {
  margin: 0 0 14px;
  color: var(--muted);
  line-height: 1.65;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.equipment-info {
  background: var(--soft);
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 10px;
  display: grid;
  gap: 8px;
  margin-bottom: 14px;
}

.info-item {
  display: flex;
  align-items: center;
  color: #64748b;
  font-size: 13px;
}

.info-item i {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  margin-right: 8px;
}

.review-count {
  margin-left: 5px;
  color: #94a3b8;
}

.equipment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.equipment-price {
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

.equipment-actions {
  margin-left: auto;
}

.reserve-btn {
  border: none;
  border-radius: 10px;
  min-height: 40px;
  padding: 0 16px;
  font-size: 14px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
}

.reserve-btn:disabled {
  background: #cbd5e1;
}

.pagination-container {
  display: flex;
  justify-content: center;
}

.icon-location {
  background: url('/position.svg') no-repeat center center;
  background-size: 14px;
}

.icon-rating::before {
  content: '★';
  color: #f59e0b;
  font-size: 12px;
  line-height: 1;
}

.icon-usage::before {
  content: '';
  width: 11px;
  height: 11px;
  border-radius: 50%;
  border: 2px solid #22c55e;
  border-right-color: transparent;
  box-sizing: border-box;
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

  .status-filter,
  .location-filter {
    justify-content: space-between;
  }

  .status-filter :deep(.el-select),
  .location-filter :deep(.el-input) {
    width: 68%;
  }

  .search-box {
    min-width: auto;
  }

  .equipment-grid {
    grid-template-columns: 1fr;
  }

  .equipment-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .reserve-btn {
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .equipment-card,
  .equipment-image img {
    transition: none;
  }

  .equipment-card:hover {
    transform: none;
  }
}
</style>
