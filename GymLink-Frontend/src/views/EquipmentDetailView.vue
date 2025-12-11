<template>
  <AppLayout>
    <!-- 加载状态 -->
    <div v-if="equipmentStore.detailLoading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 错误状态 -->
    <div v-else-if="equipmentStore.detailError" class="error-container">
      <el-result icon="warning" title="加载失败" :sub-title="equipmentStore.detailError">
        <template #extra>
          <el-button type="primary" @click="loadEquipmentDetail">重新加载</el-button>
          <el-button @click="goBack">返回列表</el-button>
        </template>
      </el-result>
    </div>

    <!-- 器材详情内容 -->
    <div v-else-if="equipmentStore.equipmentDetail" class="equipment-detail">
      <!-- 返回按钮 -->
      <div class="back-button">
        <el-button @click="goBack" :icon="ArrowLeft">返回器材列表</el-button>
      </div>

      <!-- 器材头部信息 -->
      <div class="equipment-header">
        <div class="equipment-image">
          <img :src="equipmentStore.equipmentDetail.image" :alt="equipmentStore.equipmentDetail.name" />
          <div class="equipment-status" :class="'status-' + equipmentStore.equipmentDetail.status">
            {{ getStatusText(equipmentStore.equipmentDetail.status) }}
          </div>
        </div>
        <div class="equipment-info">
          <div class="equipment-category">
            <el-tag type="success" size="large">{{ getTypeText(equipmentStore.equipmentDetail.type) }}</el-tag>
          </div>
          <h1 class="equipment-title">{{ equipmentStore.equipmentDetail.name }}</h1>

          <!-- 器材基本信息 -->
          <div class="info-cards">
            <div class="info-card">
              <img src="/position.svg" alt="位置" class="info-icon" />
              <div class="info-content">
                <div class="info-value">{{ equipmentStore.equipmentDetail.location }}</div>
                <div class="info-label">放置位置</div>
              </div>
            </div>
            <div class="info-card">
              <img src="/state.svg" alt="状态" class="info-icon" />
              <div class="info-content">
                <div class="info-value" :class="'status-text-' + equipmentStore.equipmentDetail.status">
                  {{ getStatusText(equipmentStore.equipmentDetail.status) }}
                </div>
                <div class="info-label">当前状态</div>
              </div>
            </div>
            <div class="info-card">
              <div class="info-icon-text">📦</div>
              <div class="info-content">
                <div class="info-value">{{ equipmentStore.equipmentDetail.totalCount || 1 }} 台</div>
                <div class="info-label">器材数量</div>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button type="primary" size="large" class="book-btn"
              :disabled="equipmentStore.equipmentDetail.status !== 1" @click="openReservationDialog">
              {{ equipmentStore.equipmentDetail.status === 1 ? '预约器材' : '维护中' }}
            </el-button>
            <el-button size="large" @click="handleToggleFavorite">
              {{ isFavorite ? '❤️ 已收藏' : '🤍 收藏' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 器材介绍 -->
      <div class="equipment-content">
        <el-card class="detail-card">
          <template #header>
            <span class="card-title">器材介绍</span>
          </template>
          <div class="description-content">
            {{ equipmentStore.equipmentDetail.description || '暂无器材介绍' }}
          </div>
        </el-card>

        <!-- 其他信息 -->
        <el-card class="detail-card">
          <template #header>
            <span class="card-title">其他信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="器材类型">
              {{ getTypeText(equipmentStore.equipmentDetail.type) }}
            </el-descriptions-item>
            <el-descriptions-item label="放置位置">
              {{ equipmentStore.equipmentDetail.location }}
            </el-descriptions-item>
            <el-descriptions-item label="器材数量">
              {{ equipmentStore.equipmentDetail.totalCount || 1 }} 台
            </el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="equipmentStore.equipmentDetail.status === 1 ? 'success' : 'danger'">
                {{ getStatusText(equipmentStore.equipmentDetail.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="录入时间">
              {{ formatDate(equipmentStore.equipmentDetail.createTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 已预约时段 -->
        <el-card class="detail-card">
          <template #header>
            <div class="reservation-header">
              <span class="card-title">已预约时段</span>
              <el-button type="primary" link @click="loadReservations">
                <el-icon><Refresh /></el-icon> 刷新
              </el-button>
            </div>
          </template>
          <div v-if="reservationsLoading" class="reservations-loading">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="reservations.length === 0" class="no-reservations">
            <el-empty description="暂无预约记录，可随时预约" :image-size="80" />
          </div>
          <div v-else class="reservations-list">
            <el-table :data="reservations" stripe style="width: 100%">
              <el-table-column label="预约时段" min-width="200">
                <template #default="{ row }">
                  <div class="time-slot">
                    <span class="time-start">{{ formatDateTime(row.startTime) }}</span>
                    <span class="time-separator">至</span>
                    <span class="time-end">{{ formatDateTime(row.endTime) }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="studentName" label="预约人" width="120" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getReservationStatusType(row.status)" size="small">
                    {{ getReservationStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <div class="reservations-pagination" v-if="reservationsTotal > 5">
              <el-pagination
                small
                background
                layout="prev, pager, next"
                :total="reservationsTotal"
                :page-size="5"
                :current-page="reservationsPage"
                @current-change="handleReservationsPageChange"
              />
            </div>
          </div>
        </el-card>
      </div>
    </div>

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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useEquipmentStore } from '@/stores/equipment'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'
import { toggleFavorite as toggleFavoriteApi, checkFavorite, FavoriteType } from '@/api/favorite'
import { reserveEquipment as reserveEquipmentApi, getEquipmentReservations, type EquipmentReservation } from '@/api/equipment'

const route = useRoute()
const router = useRouter()
const equipmentStore = useEquipmentStore()
const authStore = useAuthStore()

// 是否收藏
const isFavorite = ref(false)

// 已预约时段相关状态
const reservations = ref<EquipmentReservation[]>([])
const reservationsLoading = ref(false)
const reservationsPage = ref(1)
const reservationsTotal = ref(0)

// 预约相关状态
const reservationDialogVisible = ref(false)
const reservationLoading = ref(false)
const reservationFormRef = ref<FormInstance>()
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

// 获取器材ID
const equipmentId = computed(() => {
  return route.params.id as string
})

// 类型映射
const typeMap: { [key: string]: string } = {
  '1': '有氧健身器材',
  '2': '力量训练器材',
  '3': '功能性训练器材',
  '4': '小型健身器械',
  '5': '康复与辅助器材',
  '6': '其他辅助设备',
  '7': '商用专用器材',
  '8': '家用专用器材',
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

// 获取类型文本
const getTypeText = (type: string) => {
  return typeMap[type] || type || '未分类'
}

// 获取状态文本
const getStatusText = (status: number) => {
  return status === 1 ? '可用' : '维护中'
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 返回器材列表
const goBack = () => {
  router.push('/equipment')
}

// 打开预约对话框
const openReservationDialog = () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录后再预约器材')
    router.push({ name: 'auth', query: { redirect: route.fullPath } })
    return
  }

  if (authStore.user?.role === 'coach') {
    ElMessage.warning('教练不能预约器材')
    return
  }

  // 重置表单
  reservationForm.equipmentName = equipmentStore.equipmentDetail?.name || ''
  reservationForm.startTime = null
  reservationForm.duration = 60
  reservationDialogVisible.value = true
}

// 提交预约
const submitReservation = async () => {
  if (!reservationFormRef.value) return

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
        equipmentId: Number(equipmentId.value),
        studentId: Number(studentId),
        startTime: startDate.toISOString(),
        endTime: endDate.toISOString()
      })

      ElMessage.success('预约成功！')
      reservationDialogVisible.value = false
      // 预约成功后刷新已预约时段列表
      loadReservations()
    } catch {
      // 错误已在 request 拦截器中处理，这里不重复显示
    } finally {
      reservationLoading.value = false
    }
  })
}

// 切换收藏状态
const handleToggleFavorite = async () => {
  try {
    const res = await toggleFavoriteApi({
      targetId: equipmentId.value,
      type: FavoriteType.EQUIPMENT
    })
    isFavorite.value = res as unknown as boolean
    ElMessage.success(isFavorite.value ? '已添加到收藏' : '已取消收藏')
  } catch (error) {
    ElMessage.error('操作失败，请先登录')
  }
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  try {
    const res = await checkFavorite(equipmentId.value as any, FavoriteType.EQUIPMENT)
    isFavorite.value = res.data
  } catch (error) {
    // 未登录时忽略错误
  }
}

// 加载器材详情
const loadEquipmentDetail = () => {
  if (equipmentId.value) {
    equipmentStore.fetchEquipmentDetail(equipmentId.value)
  }
}

// 加载已预约时段
const loadReservations = async () => {
  if (!equipmentId.value) return
  reservationsLoading.value = true
  try {
    const res = await getEquipmentReservations({
      equipmentId: Number(equipmentId.value),
      status: 1, // 只查询预约成功的记录
      pageNum: reservationsPage.value,
      pageSize: 5
    })
    reservations.value = res.records || []
    reservationsTotal.value = res.total || 0
  } catch (error) {
    console.error('获取预约记录失败:', error)
  } finally {
    reservationsLoading.value = false
  }
}

// 处理预约记录分页变化
const handleReservationsPageChange = (page: number) => {
  reservationsPage.value = page
  loadReservations()
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取预约状态文本
const getReservationStatusText = (status: number) => {
  const statusMap: { [key: number]: string } = {
    1: '已预约',
    2: '已取消',
    3: '已完成'
  }
  return statusMap[status] || '未知'
}

// 获取预约状态标签类型
const getReservationStatusType = (status: number) => {
  const typeMap: { [key: number]: string } = {
    1: 'warning',
    2: 'info',
    3: 'success'
  }
  return typeMap[status] || 'info'
}

// 组件挂载时加载数据
onMounted(() => {
  loadEquipmentDetail()
  checkFavoriteStatus()
  loadReservations()
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

.equipment-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
}

.equipment-header {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.equipment-image {
  position: relative;
  flex: 0 0 400px;
  height: 300px;
  border-radius: 12px;
  overflow: hidden;
}

.equipment-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.equipment-status {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: white;
}

.equipment-status.status-1 {
  background: #67c23a;
}

.equipment-status.status-2 {
  background: #f56c6c;
}

.equipment-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.equipment-category {
  margin-bottom: 15px;
}

.equipment-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 25px;
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
  filter: invert(45%) sepia(98%) saturate(1500%) hue-rotate(80deg) brightness(100%) contrast(96%);
}

.info-icon-text {
  font-size: 24px;
}

.info-content {
  display: flex;
  flex-direction: column;
}

.info-value {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.info-label {
  font-size: 12px;
  color: #909399;
}

.status-text-1 {
  color: #67c23a;
}

.status-text-2 {
  color: #f56c6c;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: auto;
}

.book-btn {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  padding: 12px 40px;
  font-size: 16px;
}

.book-btn:hover {
  background: linear-gradient(135deg, #85ce61 0%, #67c23a 100%);
}

.book-btn:disabled {
  background: #c0c4cc;
}

.equipment-content {
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
  min-height: 100px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .equipment-header {
    flex-direction: column;
  }

  .equipment-image {
    flex: none;
    width: 100%;
    height: 250px;
  }

  .info-cards {
    flex-wrap: wrap;
  }

  .info-card {
    flex: 1 1 calc(50% - 10px);
    min-width: 150px;
  }
}

@media (max-width: 768px) {
  .equipment-header {
    padding: 20px;
  }

  .equipment-title {
    font-size: 24px;
  }

  .info-cards {
    flex-direction: column;
  }

  .info-card {
    flex: none;
    width: 100%;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
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

/* 已预约时段样式 */
.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reservations-loading {
  padding: 20px 0;
}

.no-reservations {
  padding: 20px 0;
  text-align: center;
}

.reservations-list {
  padding: 10px 0;
}

.time-slot {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-start, .time-end {
  font-weight: 500;
  color: #2c3e50;
}

.time-separator {
  color: #909399;
  font-size: 12px;
}

.reservations-pagination {
  display: flex;
  justify-content: center;
  margin-top: 15px;
}
</style>
