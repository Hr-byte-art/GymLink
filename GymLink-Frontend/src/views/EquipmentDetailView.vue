<template>
  <AppLayout>
    <!-- 加载状态 -->
    <div v-if="equipmentStore.detailLoading" class="loading-container">
      <el-loading :loading="true" text="加载中..."></el-loading>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="equipmentStore.detailError" class="error-container">
      <el-result icon="warning" title="加载失败" :sub-title="equipmentStore.detailError">
        <template #extra>
          <el-button type="primary" @click="loadEquipmentDetail">重新加载</el-button>
        </template>
      </el-result>
    </div>

    <!-- 器材详情内容 -->
    <div v-else-if="equipmentStore.equipmentDetail" class="equipment-detail-content">
      <!-- 返回按钮 -->
      <div class="back-button">
        <el-button @click="goBack" icon="ArrowLeft">返回器材列表</el-button>
      </div>

      <!-- 器材基本信息 -->
      <section class="equipment-hero">
        <div class="equipment-hero-container">
          <div class="equipment-image-gallery">
            <div class="main-image">
              <img :src="currentImage" :alt="equipmentStore.equipmentDetail.name" />
            </div>
            <div class="image-thumbnails"
              v-if="equipmentStore.equipmentDetail.images && equipmentStore.equipmentDetail.images.length > 1">
              <div v-for="(image, index) in equipmentStore.equipmentDetail.images" :key="index" class="thumbnail"
                :class="{ active: currentImage === image }" @click="currentImage = image">
                <img :src="image" :alt="`${equipmentStore.equipmentDetail.name} ${index + 1}`" />
              </div>
            </div>
          </div>
          <div class="equipment-basic-info">
            <h1 class="equipment-name">{{ equipmentStore.equipmentDetail.name }}</h1>
            <div class="equipment-category">{{ equipmentStore.equipmentDetail.category }}</div>
            <div class="equipment-rating" v-if="equipmentStore.equipmentDetail.rating">
              <el-rate v-model="equipmentStore.equipmentDetail.rating" disabled show-score
                text-color="#ff9900"></el-rate>
              <span class="review-count">({{ equipmentStore.equipmentDetail.reviewCount }}条评价)</span>
            </div>
            <p class="equipment-description">{{ equipmentStore.equipmentDetail.description }}</p>
            <div class="equipment-meta">
              <div class="meta-item">
                <i class="icon-brand"></i>
                <span>{{ equipmentStore.equipmentDetail.brand }} {{ equipmentStore.equipmentDetail.model }}</span>
              </div>
              <div class="meta-item">
                <i class="icon-location"></i>
                <span>{{ equipmentStore.equipmentDetail.location }}</span>
              </div>
              <div class="meta-item">
                <i class="icon-status"></i>
                <span :class="`status-${equipmentStore.equipmentDetail.status}`">
                  {{ getStatusText(equipmentStore.equipmentDetail.status) }}
                </span>
              </div>
              <div class="meta-item" v-if="equipmentStore.equipmentDetail.usageCount">
                <i class="icon-usage"></i>
                <span>使用 {{ equipmentStore.equipmentDetail.usageCount }} 次</span>
              </div>
            </div>
            <div class="equipment-price" v-if="equipmentStore.equipmentDetail.price">
              <span class="price-label">¥{{ equipmentStore.equipmentDetail.price }}</span>
              <span class="price-unit">/小时</span>
            </div>
            <div class="equipment-actions">
              <el-button type="primary" size="large" class="reserve-btn"
                :disabled="equipmentStore.equipmentDetail.status !== 'available'" @click="reserveEquipment">
                预约器材
              </el-button>
              <el-button size="large" class="favorite-btn" @click="toggleFavorite">
                <i :class="isFavorite ? 'icon-favorite-filled' : 'icon-favorite'"></i>
                {{ isFavorite ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </div>
        </div>
      </section>

      <!-- 标签页内容 -->
      <section class="equipment-tabs-section">
        <div class="equipment-tabs-container">
          <el-tabs v-model="activeTab" class="equipment-tabs">
            <!-- 详细介绍 -->
            <el-tab-pane label="详细介绍" name="details">
              <div class="tab-content">
                <div class="equipment-details">
                  <h3 class="section-title">详细介绍</h3>
                  <div class="details-content" v-html="equipmentStore.equipmentDetail.detailedDescription"></div>

                  <!-- 器材特点 -->
                  <h3 class="section-title">器材特点</h3>
                  <div class="features-list">
                    <div v-for="(feature, index) in equipmentStore.equipmentDetail.features" :key="index"
                      class="feature-item">
                      <div class="feature-icon">
                        <i class="icon-check"></i>
                      </div>
                      <div class="feature-text">{{ feature }}</div>
                    </div>
                  </div>

                  <!-- 技术规格 -->
                  <h3 class="section-title">技术规格</h3>
                  <div class="specifications-table">
                    <div v-for="(spec, index) in equipmentStore.equipmentDetail.specifications" :key="index"
                      class="spec-row">
                      <div class="spec-name">{{ spec.name }}</div>
                      <div class="spec-value">{{ spec.value }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 安全须知 -->
            <el-tab-pane label="安全须知" name="safety">
              <div class="tab-content">
                <div class="equipment-safety">
                  <h3 class="section-title">安全须知</h3>
                  <div class="safety-instructions">
                    <div v-for="(instruction, index) in equipmentStore.equipmentDetail.safetyInstructions" :key="index"
                      class="safety-item">
                      <div class="safety-number">{{ index + 1 }}</div>
                      <div class="safety-text">{{ instruction }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 维护记录 -->
            <el-tab-pane label="维护记录" name="maintenance">
              <div class="tab-content">
                <div class="equipment-maintenance">
                  <h3 class="section-title">维护记录</h3>
                  <div class="maintenance-timeline">
                    <div v-for="(record, index) in equipmentStore.equipmentDetail.maintenanceHistory" :key="index"
                      class="timeline-item">
                      <div class="timeline-dot" :class="`type-${record.type}`"></div>
                      <div class="timeline-content">
                        <div class="timeline-header">
                          <div class="timeline-date">{{ record.date }}</div>
                          <div class="timeline-type" :class="`type-${record.type}`">
                            {{ getMaintenanceTypeText(record.type) }}
                          </div>
                        </div>
                        <div class="timeline-description">{{ record.description }}</div>
                        <div class="timeline-technician">技术员：{{ record.technician }}</div>
                        <div v-if="record.cost" class="timeline-cost">费用：¥{{ record.cost }}</div>
                        <div v-if="record.nextMaintenanceDate" class="timeline-next">
                          下次维护：{{ record.nextMaintenanceDate }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 预约情况 -->
            <el-tab-pane label="预约情况" name="reservations">
              <div class="tab-content">
                <div class="equipment-reservations">
                  <h3 class="section-title">预约情况</h3>

                  <!-- 预约日历 -->
                  <div class="reservation-calendar">
                    <div class="calendar-header">
                      <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期" @change="loadAvailability"
                        :disabled-date="disabledDate" />
                      <el-button type="primary" @click="loadAvailability">查看可用时间</el-button>
                    </div>

                    <div v-if="equipmentStore.availabilityLoading" class="availability-loading">
                      <el-loading :loading="true" text="加载中..."></el-loading>
                    </div>

                    <div v-else-if="equipmentStore.equipmentAvailability" class="time-slots">
                      <div v-for="slot in equipmentStore.equipmentAvailability.timeSlots" :key="slot.time"
                        class="time-slot" :class="{
                          available: slot.available,
                          booked: !slot.available,
                          selected: selectedTimeSlot === slot.time
                        }" @click="selectTimeSlot(slot.time, slot.available)">
                        <div class="slot-time">{{ slot.time }}</div>
                        <div class="slot-status">{{ slot.available ? '可预约' : '已预约' }}</div>
                      </div>
                    </div>

                    <div v-else class="no-availability">
                      <p>请选择日期查看可用时间段</p>
                    </div>
                  </div>

                  <!-- 预约按钮 -->
                  <div v-if="selectedTimeSlot" class="reservation-actions">
                    <el-button type="primary" size="large" @click="confirmReservation">
                      确认预约
                    </el-button>
                  </div>

                  <!-- 预约记录 -->
                  <div class="reservation-history">
                    <h4>预约记录</h4>
                    <div
                      v-if="equipmentStore.equipmentDetail.reservations && equipmentStore.equipmentDetail.reservations.length > 0"
                      class="reservations-list">
                      <div v-for="reservation in equipmentStore.equipmentDetail.reservations" :key="reservation.id"
                        class="reservation-item">
                        <div class="reservation-info">
                          <div class="reservation-user">{{ reservation.userName }}</div>
                          <div class="reservation-time">
                            {{ reservation.startTime }} - {{ reservation.endTime }}
                          </div>
                          <div class="reservation-purpose">用途：{{ reservation.purpose }}</div>
                        </div>
                        <div class="reservation-status" :class="`status-${reservation.status}`">
                          {{ getReservationStatusText(reservation.status) }}
                        </div>
                      </div>
                    </div>
                    <div v-else class="no-reservations">
                      <p>暂无预约记录</p>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 用户评价 -->
            <el-tab-pane label="用户评价" name="reviews">
              <div class="tab-content">
                <div class="equipment-reviews">
                  <h3 class="section-title">用户评价</h3>

                  <!-- 评价统计 -->
                  <div class="review-summary">
                    <div class="rating-overview">
                      <div class="rating-score">{{ equipmentStore.equipmentDetail.rating || 0 }}</div>
                      <div class="rating-stars">
                        <el-rate v-model="equipmentStore.equipmentDetail.rating" disabled></el-rate>
                      </div>
                      <div class="rating-count">{{ equipmentStore.equipmentDetail.reviewCount || 0 }}条评价</div>
                    </div>
                    <div class="write-review">
                      <el-button type="primary" @click="showReviewDialog = true">写评价</el-button>
                    </div>
                  </div>

                  <!-- 评价列表 -->
                  <div class="reviews-list">
                    <div v-for="review in equipmentStore.equipmentDetail.reviews" :key="review.id" class="review-item">
                      <div class="review-header">
                        <div class="reviewer-info">
                          <div class="reviewer-avatar">
                            <img :src="review.avatar" :alt="review.userName" />
                          </div>
                          <div class="reviewer-details">
                            <div class="reviewer-name">{{ review.userName }}</div>
                            <div class="review-date">{{ review.createdAt }}</div>
                          </div>
                        </div>
                        <div class="review-rating">
                          <el-rate v-model="review.rating" disabled></el-rate>
                        </div>
                      </div>
                      <div class="review-content">{{ review.content }}</div>
                      <div v-if="review.images && review.images.length > 0" class="review-images">
                        <img v-for="(image, index) in review.images" :key="index" :src="image"
                          :alt="`评价图片 ${index + 1}`" />
                      </div>
                      <div v-if="review.reply" class="review-reply">
                        <div class="reply-header">
                          <span class="reply-label">管理员回复</span>
                          <span class="reply-date">{{ review.reply.createdAt }}</span>
                        </div>
                        <div class="reply-content">{{ review.reply.content }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </section>
    </div>

    <!-- 评价对话框 -->
    <el-dialog v-model="showReviewDialog" title="写评价" width="500px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating"></el-rate>
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="请输入您的评价..."></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showReviewDialog = false">取消</el-button>
          <el-button type="primary" @click="submitReview">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useEquipmentStore } from '@/stores/equipment'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import AppLayout from '@/components/AppLayout.vue'

// 使用路由和器材状态管理
const route = useRoute()
const router = useRouter()
const equipmentStore = useEquipmentStore()

// 当前活动标签页
const activeTab = ref('details')

// 当前显示的图片
const currentImage = ref('')

// 是否收藏
const isFavorite = ref(false)

// 选中的日期
const selectedDate = ref(new Date())

// 选中的时间段
const selectedTimeSlot = ref('')

// 显示评价对话框
const showReviewDialog = ref(false)

// 评价表单
const reviewForm = ref({
  rating: 5,
  content: ''
})

// 获取器材ID（保持字符串类型，避免大数精度丢失）
const equipmentId = computed(() => {
  return route.params.id as string
})

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: { [key: string]: string } = {
    'available': '可用',
    'in_use': '使用中',
    'maintenance': '维护中'
  }
  return statusMap[status] || status
}

// 获取维护类型文本
const getMaintenanceTypeText = (type: string) => {
  const typeMap: { [key: string]: string } = {
    'routine': '常规维护',
    'repair': '故障维修',
    'inspection': '安全检查'
  }
  return typeMap[type] || type
}

// 获取预约状态文本
const getReservationStatusText = (status: string) => {
  const statusMap: { [key: string]: string } = {
    'pending': '待确认',
    'confirmed': '已确认',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return statusMap[status] || status
}

// 返回器材列表
const goBack = () => {
  router.push('/equipment')
}

// 切换收藏状态
const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value
  ElMessage.success(isFavorite.value ? '已添加到收藏' : '已取消收藏')
}

// 预约器材
const reserveEquipment = () => {
  activeTab.value = 'reservations'
}

// 选择时间段
const selectTimeSlot = (time: string, available: boolean) => {
  if (available) {
    selectedTimeSlot.value = time
  }
}

// 确认预约
const confirmReservation = async () => {
  if (!selectedDate.value || !selectedTimeSlot.value) {
    ElMessage.warning('请选择日期和时间段')
    return
  }

  try {
    // 构建预约时间
    const date = new Date(selectedDate.value)
    const [hours, minutes] = selectedTimeSlot.value.split(':')
    const startTime = new Date(date.setHours(parseInt(hours), parseInt(minutes), 0, 0))
    const endTime = new Date(date.setHours(parseInt(hours) + 1, parseInt(minutes), 0, 0))

    // 调用预约API
    await equipmentStore.reserveEquipmentItem(
      equipmentId.value,
      startTime.toISOString(),
      endTime.toISOString(),
      '健身训练'
    )

    ElMessage.success('预约成功')

    // 重置选择
    selectedTimeSlot.value = ''

    // 刷新可用时间段
    loadAvailability()
  } catch (error) {
    ElMessage.error('预约失败，请重试')
  }
}

// 加载可用时间段
const loadAvailability = async () => {
  if (!selectedDate.value) return

  const date = new Date(selectedDate.value).toISOString().split('T')[0]
  await equipmentStore.fetchEquipmentAvailability(equipmentId.value, date)
}

// 禁用过去的日期
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

// 提交评价
const submitReview = async () => {
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }

  try {
    await equipmentStore.submitReview(
      equipmentId.value,
      reviewForm.value.rating,
      reviewForm.value.content
    )

    ElMessage.success('评价提交成功')
    showReviewDialog.value = false

    // 重置表单
    reviewForm.value = {
      rating: 5,
      content: ''
    }
  } catch (error) {
    ElMessage.error('评价提交失败，请重试')
  }
}

// 加载器材详情
const loadEquipmentDetail = () => {
  if (equipmentId.value) {
    equipmentStore.fetchEquipmentDetail(equipmentId.value).then(() => {
      // 设置默认图片
      if (equipmentStore.equipmentDetail) {
        currentImage.value = equipmentStore.equipmentDetail.image
      }
    })
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadEquipmentDetail()
})
</script>

<style scoped>
.equipment-detail-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 加载和错误状态样式 */
.loading-container,
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
}

/* 返回按钮样式 */
.back-button {
  padding: 20px 0;
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 20px;
}

/* 器材基本信息样式 */
.equipment-hero {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
  padding: 60px 0;
}

.equipment-hero-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 40px;
}

.equipment-image-gallery {
  flex-shrink: 0;
  width: 500px;
}

.main-image {
  width: 100%;
  height: 300px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-thumbnails {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 5px;
}

.thumbnail {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  opacity: 0.7;
  transition: all 0.3s ease;
}

.thumbnail.active {
  opacity: 1;
  box-shadow: 0 0 0 2px white;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.equipment-basic-info {
  flex: 1;
}

.equipment-name {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 10px;
}

.equipment-category {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 15px;
  opacity: 0.9;
}

.equipment-rating {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.review-count {
  font-size: 16px;
  opacity: 0.9;
}

.equipment-description {
  font-size: 18px;
  line-height: 1.6;
  margin-bottom: 25px;
  opacity: 0.9;
}

.equipment-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 30px;
}

.meta-item {
  display: flex;
  align-items: center;
  font-size: 16px;
}

.meta-item i {
  margin-right: 8px;
  font-size: 18px;
}

.status-available {
  color: #67c23a;
}

.status-in_use {
  color: #e6a23c;
}

.status-maintenance {
  color: #f56c6c;
}

.equipment-price {
  display: flex;
  align-items: baseline;
  margin-bottom: 30px;
}

.price-label {
  font-size: 28px;
  font-weight: 700;
}

.price-unit {
  font-size: 16px;
  margin-left: 5px;
  opacity: 0.9;
}

.equipment-actions {
  display: flex;
  gap: 20px;
}

.reserve-btn {
  background: white;
  color: #67c23a;
  border: none;
  font-weight: 600;
  padding: 12px 30px;
}

.favorite-btn {
  background: transparent;
  color: white;
  border: 2px solid white;
  font-weight: 600;
  padding: 12px 30px;
}

/* 标签页区域样式 */
.equipment-tabs-section {
  flex: 1;
  padding: 40px 0;
}

.equipment-tabs-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.equipment-tabs {
  margin-bottom: 30px;
}

.tab-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  padding: 30px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f2f5;
}

/* 详细介绍样式 */
.details-content {
  line-height: 1.8;
  color: #555;
  margin-bottom: 30px;
}

.features-list {
  margin-bottom: 30px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  margin-bottom: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.feature-icon {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  background: #67c23a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
}

.feature-text {
  flex: 1;
  line-height: 1.6;
}

.specifications-table {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.spec-row {
  display: flex;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.spec-name {
  flex: 1;
  font-weight: 600;
  color: #2c3e50;
}

.spec-value {
  flex: 2;
  color: #555;
}

/* 安全须知样式 */
.safety-instructions {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.safety-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.safety-number {
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  background: #f56c6c;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
}

.safety-text {
  flex: 1;
  line-height: 1.6;
}

/* 维护记录样式 */
.maintenance-timeline {
  position: relative;
  padding-left: 30px;
}

.maintenance-timeline::before {
  content: '';
  position: absolute;
  left: 10px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e9ecef;
}

.timeline-item {
  position: relative;
  margin-bottom: 30px;
}

.timeline-dot {
  position: absolute;
  left: -24px;
  top: 5px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #409eff;
}

.timeline-dot.type-routine {
  background: #67c23a;
}

.timeline-dot.type-repair {
  background: #f56c6c;
}

.timeline-dot.type-inspection {
  background: #e6a23c;
}

.timeline-content {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.timeline-date {
  font-weight: 600;
  color: #2c3e50;
}

.timeline-type {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.timeline-type.type-routine {
  background: #67c23a;
}

.timeline-type.type-repair {
  background: #f56c6c;
}

.timeline-type.type-inspection {
  background: #e6a23c;
}

.timeline-description {
  margin-bottom: 10px;
  line-height: 1.6;
}

.timeline-technician,
.timeline-cost,
.timeline-next {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

/* 预约情况样式 */
.reservation-calendar {
  margin-bottom: 30px;
}

.calendar-header {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  align-items: center;
}

.availability-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100px;
}

.time-slots {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
}

.time-slot {
  padding: 15px;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.time-slot.available {
  background: #e8f5e9;
  color: #2e7d32;
}

.time-slot.booked {
  background: #ffebee;
  color: #c62828;
  cursor: not-allowed;
}

.time-slot.selected {
  background: #67c23a;
  color: white;
}

.slot-time {
  font-weight: 600;
  margin-bottom: 5px;
}

.slot-status {
  font-size: 14px;
}

.no-availability {
  text-align: center;
  padding: 40px;
  color: #666;
}

.reservation-actions {
  display: flex;
  justify-content: center;
  margin: 30px 0;
}

.reservation-history h4 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #2c3e50;
}

.reservations-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.reservation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.reservation-info {
  flex: 1;
}

.reservation-user {
  font-weight: 600;
  margin-bottom: 5px;
}

.reservation-time {
  margin-bottom: 5px;
}

.reservation-purpose {
  font-size: 14px;
  color: #666;
}

.reservation-status {
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  color: white;
}

.reservation-status.status-pending {
  background: #e6a23c;
}

.reservation-status.status-confirmed {
  background: #67c23a;
}

.reservation-status.status-completed {
  background: #409eff;
}

.reservation-status.status-cancelled {
  background: #f56c6c;
}

.no-reservations {
  text-align: center;
  padding: 40px;
  color: #666;
}

/* 用户评价样式 */
.review-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f2f5;
}

.rating-overview {
  text-align: center;
}

.rating-score {
  font-size: 48px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 10px;
}

.rating-stars {
  margin-bottom: 10px;
}

.rating-count {
  color: #666;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.reviewer-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
}

.reviewer-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.reviewer-name {
  font-weight: 600;
  color: #2c3e50;
}

.review-date {
  font-size: 14px;
  color: #666;
}

.review-content {
  line-height: 1.6;
  color: #555;
  margin-bottom: 15px;
}

.review-images {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.review-images img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.review-reply {
  padding: 15px;
  background: white;
  border-radius: 8px;
  border-left: 3px solid #67c23a;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.reply-label {
  font-weight: 600;
  color: #67c23a;
}

.reply-date {
  font-size: 14px;
  color: #666;
}

.reply-content {
  line-height: 1.6;
  color: #555;
}

/* 图标样式 */
.icon-brand::before {
  content: '🏷️';
}

.icon-location::before {
  content: '📍';
}

.icon-status::before {
  content: '🔶';
}

.icon-usage::before {
  content: '📊';
}

.icon-favorite::before {
  content: '🤍';
}

.icon-favorite-filled::before {
  content: '❤️';
}

.icon-check::before {
  content: '✓';
}

/* 响应式设计 */
@media (max-width: 992px) {
  .equipment-hero-container {
    flex-direction: column;
  }

  .equipment-image-gallery {
    width: 100%;
  }

  .specifications-table {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .equipment-name {
    font-size: 32px;
  }

  .equipment-category {
    font-size: 18px;
  }

  .equipment-description {
    font-size: 16px;
  }

  .equipment-actions {
    flex-direction: column;
    gap: 10px;
  }

  .reserve-btn,
  .favorite-btn {
    width: 100%;
  }

  .time-slots {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }

  .reservation-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .review-summary {
    flex-direction: column;
    gap: 20px;
  }

  .tab-content {
    padding: 20px;
  }
}
</style>