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
            <el-tag type="success" size="large">{{ equipmentStore.equipmentDetail.location }}</el-tag>
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
              {{ equipmentStore.equipmentDetail.location }}
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
  } catch {
    ElMessage.error('操作失败，请先登录')
  }
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  try {
    const res = await checkFavorite(equipmentId.value, FavoriteType.EQUIPMENT)
    isFavorite.value = res.data
  } catch {
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
:deep(.el-card__header){border-bottom:1px solid #f4e8da;}
.loading-container,.error-container{display:flex;justify-content:center;align-items:center;min-height:380px;padding:32px;}
.equipment-detail{max-width:1240px;margin:0 auto;padding:24px 20px 40px;}
.back-button{margin-bottom:18px;}
.equipment-header{display:flex;gap:30px;margin-bottom:24px;padding:24px;border-radius:22px;border:1px solid #f4e8da;background:linear-gradient(165deg,#fff 0%,#fff9f3 100%);box-shadow:0 10px 24px rgba(248,146,43,.07);}
.equipment-image{position:relative;flex:0 0 410px;height:300px;border-radius:16px;overflow:hidden;}
.equipment-image img{width:100%;height:100%;object-fit:cover;}
.equipment-status{position:absolute;top:12px;right:12px;padding:7px 14px;border-radius:999px;color:#fff;font-size:13px;font-weight:600;}
.equipment-status.status-1{background:rgba(34,197,94,.92);} .equipment-status.status-2{background:rgba(239,68,68,.92);}
.equipment-info{flex:1;display:flex;flex-direction:column;}
.equipment-category{margin-bottom:10px;}
.equipment-title{margin:0 0 20px;font-size:34px;line-height:1.25;color:#2a1f12;}
.info-cards{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px;margin-bottom:22px;}
.info-card{display:flex;align-items:center;gap:10px;padding:12px;border-radius:12px;background:#fff;border:1px solid #f4e8da;}
.info-icon{width:26px;height:26px;filter:invert(52%) sepia(89%) saturate(853%) hue-rotate(349deg) brightness(97%) contrast(95%);}
.info-icon-text{width:26px;text-align:center;font-size:20px;}
.info-content{display:flex;flex-direction:column;}
.info-value{color:#2a1f12;font-size:16px;font-weight:700;}
.info-label{color:#8f7660;font-size:12px;}
.status-text-1{color:#16a34a;} .status-text-2{color:#dc2626;}
.action-buttons{display:flex;flex-wrap:wrap;gap:12px;margin-top:auto;}
.book-btn{background:linear-gradient(135deg,#f97316 0%,#fb923c 100%);border-color:transparent;color:#fff;font-weight:700;min-width:144px;}
.book-btn:disabled{background:#c2b09a;}
.equipment-content{margin-top:20px;}
.detail-card{margin-bottom:14px;border-radius:18px;border:1px solid #f4e8da;box-shadow:none;}
.card-title{font-size:18px;font-weight:700;color:#2a1f12;}
.description-content{min-height:100px;color:#5c4532;line-height:1.8;white-space:pre-wrap;}
.duration-radio-group{display:flex;flex-wrap:wrap;gap:8px;}
.duration-radio-group .el-radio-button{margin-right:0;}
.duration-radio-group .el-radio-button__inner{border-radius:8px !important;}
.reservation-header{display:flex;justify-content:space-between;align-items:center;}
.reservations-loading,.no-reservations{padding:18px 0;}
.reservations-list{padding:8px 0;}
.time-slot{display:flex;align-items:center;gap:6px;}
.time-start,.time-end{color:#2a1f12;font-weight:600;}
.time-separator{color:#8f7660;font-size:12px;}
.reservations-pagination{display:flex;justify-content:center;margin-top:16px;}
@media (max-width:992px){.equipment-header{flex-direction:column;padding:18px;}.equipment-image{width:100%;flex:none;height:240px;}.info-cards{grid-template-columns:1fr;}}
@media (max-width:768px){.equipment-detail{padding:18px 14px 30px;}.equipment-title{font-size:28px;}.action-buttons{flex-direction:column;}.action-buttons .el-button{width:100%;}}
</style>
