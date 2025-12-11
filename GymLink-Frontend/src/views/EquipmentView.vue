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
              功能性训练器材
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === '4' }" @click="setActiveCategory('4')">
              小型健身器械
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === '5' }" @click="setActiveCategory('5')">
              康复与辅助器材
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

      <!-- 加载状态 -->
      <div v-if="equipmentStore.loading" class="loading-container">
        <el-loading :loading="true" text="加载中..."></el-loading>
      </div>

      <!-- 错误状态 -->
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
          <!-- 无数据状态 -->
          <div v-if="!equipmentStore.hasEquipment" class="empty-container">
            <el-empty description="暂无器材数据"></el-empty>
          </div>

          <!-- 器材列表 -->
          <div v-else class="equipment-grid">
            <div class="equipment-card" v-for="equipment in equipmentStore.equipmentList" :key="equipment.id"
              @click="viewEquipmentDetail(equipment.id)">
              <div class="equipment-image">
                <img :src="equipment.image" :alt="equipment.name" />
                <div class="equipment-category">{{ equipment.category }}</div>
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
                  <div class="info-item" v-if="equipment.rating">
                    <i class="icon-rating"></i>
                    <span>{{ equipment.rating.toFixed(1) }}</span>
                    <span class="review-count">({{ equipment.reviewCount || 0 }})</span>
                  </div>
                  <div class="info-item" v-if="equipment.usageCount">
                    <i class="icon-usage"></i>
                    <span>使用 {{ equipment.usageCount }} 次</span>
                  </div>
                </div>
                <div class="equipment-footer">
                  <div class="equipment-price" v-if="equipment.price">
                    <span class="price-label">预约费</span>
                    <span class="price-value">¥{{ equipment.price }}</span>
                    <span class="price-unit">/小时</span>
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
const subCategories = {
  '1': [
    { value: '1-1', label: '跑步机' },
    { value: '1-2', label: '椭圆机' },
    { value: '1-3', label: '动感单车' },
    { value: '1-4', label: '划船机' },
    { value: '1-5', label: '健身车' },
    { value: '1-6', label: '楼梯机' },
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

// 设置活动子类别
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
  if (!reservationFormRef.value || !currentEquipment.value) return

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
        equipmentId: currentEquipment.value.id,
        studentId: Number(studentId),
        startTime: startDate.toISOString(),
        endTime: endDate.toISOString()
      })

      ElMessage.success('预约成功！')
      reservationDialogVisible.value = false
    } catch {
      // 错误已在 request 拦截器中处理，这里不重复显示
    } finally {
      reservationLoading.value = false
    }
  })
}

// 主分类标签映射
const mainCategoryLabels: { [key: string]: string } = {
  '1': '有氧健身器材',
  '2': '力量训练器材',
  '3': '功能性训练器材',
  '4': '小型健身器械',
  '5': '康复与辅助器材',
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
  '1-6': '楼梯机',
  '1-7': '体适能运动机',
  '2-1': '固定器械',
  '2-2': '自由重量器材',
  '2-3': '综合训练器材'
}

// 加载器材数据
const loadEquipment = () => {
  // 构建查询参数，与后端 EquipmentQueryPageRequest 对应
  const params: any = {
    pageNum: currentPage.value,
    pageSize: pageSize.value
  }

  // 添加类别筛选（后端字段是 type）
  if (activeCategory.value !== 'all') {
    // 如果有子分类选中，使用子分类；否则使用主分类
    if (activeSubCategory.value && subCategoryLabelMap[activeSubCategory.value]) {
      params.type = subCategoryLabelMap[activeSubCategory.value]
    } else {
      params.type = mainCategoryLabels[activeCategory.value]
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

  // 添加名称搜索（后端字段是 name）
  if (searchKeyword.value) {
    params.name = searchKeyword.value
  }

  // 调用API获取器材数据
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
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 页面头部样式 */
.page-header {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
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

.filter-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
}

.sub-filter-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
}

.sub-filter-tabs .filter-tab {
  background: #e9ecef;
  font-size: 12px;
  padding: 6px 12px;
}

.filter-tab {
  padding: 8px 16px;
  border-radius: 20px;
  background: white;
  color: #333;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  border: 1px solid #e0e0e0;
}

.filter-tab:hover {
  background: #f5f5f5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-tab.active {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
  border-color: transparent;
}

.filter-tab:hover {
  background: #f0f2f5;
}

.filter-tab.active {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}

.status-filter,
.location-filter {
  display: flex;
  align-items: center;
}

.filter-label {
  margin-right: 8px;
  font-weight: 500;
  color: #555;
}

/* 增加下拉框宽度以改善可读性 */
.status-filter :deep(.el-select) {
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

/* 器材区域样式 */
.equipment-section {
  padding: 60px 0;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 30px;
  margin-bottom: 50px;
}

.equipment-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
}

.equipment-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.equipment-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.equipment-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.equipment-card:hover .equipment-image img {
  transform: scale(1.05);
}

.equipment-category {
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

.equipment-status {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.equipment-status.status-1 {
  background: #67c23a;
  color: white;
}

.equipment-status.status-2 {
  background: #f56c6c;
  color: white;
}

.equipment-content {
  padding: 25px;
}

.equipment-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 15px;
}



.equipment-description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.equipment-info {
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
  color: #67c23a;
}

.review-count {
  color: #999;
  margin-left: 5px;
}

.equipment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.equipment-price {
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

.equipment-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

.equipment-actions .reserve-btn {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  padding: 8px 20px;
  font-weight: 500;
  font-size: 16px;
}

.equipment-actions .reserve-btn:hover {
  background: linear-gradient(135deg, #85ce61 0%, #67c23a 100%);
}

.equipment-actions .reserve-btn:disabled {
  background: #c0c4cc;
}

/* 分页样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

/* 图标样式 */
.icon-location {
  display: inline-block;
  width: 16px;
  height: 16px;
  background: url('/position.svg') no-repeat center center;
  background-size: contain;
}

.icon-rating::before {
  content: '⭐';
}

.icon-usage::before {
  content: '📊';
}

/* 响应式设计 */
@media (max-width: 992px) {
  .equipment-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 32px;
  }

  .filter-tabs {
    flex-wrap: wrap;
    overflow-x: hidden;
  }

  .filter-tab {
    padding: 6px 12px;
    font-size: 13px;
  }

  .sub-filter-tabs .filter-tab {
    padding: 4px 10px;
    font-size: 12px;
  }

  .filter-options {
    flex-direction: column;
    align-items: stretch;
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
    gap: 15px;
  }

  .equipment-actions {
    width: 100%;
    justify-content: space-between;
  }
}

/* 预约时长按钮样式 */
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
</style>