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
            <div class="coach-specialty">{{ coachStore.coachDetail.specialty }}</div>
            <div class="coach-rating">
              <el-rate v-model="coachStore.coachDetail.rating" disabled show-score text-color="#ff9900"></el-rate>
              <span class="review-count">({{ coachStore.coachDetail.reviewCount }}条评价)</span>
            </div>
            <p class="coach-description">{{ coachStore.coachDetail.description }}</p>
            <div class="coach-meta">
              <div class="meta-item">
                <i class="icon-experience"></i>
                <span>{{ coachStore.coachDetail.experience }}</span>
              </div>
              <div class="meta-item">
                <i class="icon-courses"></i>
                <span>{{ coachStore.coachDetail.courses }}门课程</span>
              </div>
              <div class="meta-item">
                <i class="icon-students"></i>
                <span>{{ coachStore.coachDetail.students }}名学员</span>
              </div>
            </div>
            <div class="coach-price">
              <span class="price-label">¥{{ coachStore.coachDetail.price }}</span>
              <span class="price-unit">/小时</span>
            </div>
            <div class="coach-actions">
              <el-button type="primary" size="large" class="book-btn" @click="bookCoach">预约教练</el-button>
              <el-button size="large" class="contact-btn" @click="contactCoach">联系教练</el-button>
            </div>
          </div>
        </div>
      </section>

      <!-- 标签页内容 -->
      <section class="coach-tabs-section">
        <div class="coach-tabs-container">
          <el-tabs v-model="activeTab" class="coach-tabs">
            <!-- 个人简介 -->
            <el-tab-pane label="个人简介" name="profile">
              <div class="tab-content">
                <div class="coach-profile">
                  <h3 class="section-title">个人简介</h3>
                  <div class="profile-content" v-html="coachStore.coachDetail.profile"></div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 专业资质 -->
            <el-tab-pane label="专业资质" name="qualifications">
              <div class="tab-content">
                <div class="coach-qualifications">
                  <h3 class="section-title">专业资质</h3>
                  <div class="qualifications-list">
                    <div v-for="(qualification, index) in coachStore.coachDetail.qualifications" :key="index"
                      class="qualification-item">
                      <div class="qualification-icon">
                        <i class="icon-certificate"></i>
                      </div>
                      <div class="qualification-content">
                        <h4 class="qualification-title">{{ qualification.title }}</h4>
                        <div class="qualification-issuer">{{ qualification.issuer }}</div>
                        <div class="qualification-date">{{ qualification.date }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 课程安排 -->
            <el-tab-pane label="课程安排" name="schedule">
              <div class="tab-content">
                <div class="coach-schedule">
                  <h3 class="section-title">课程安排</h3>
                  <div class="schedule-container">
                    <div class="schedule-filters">
                      <el-select v-model="selectedWeek" placeholder="选择周" @change="filterSchedule">
                        <el-option label="本周" value="current"></el-option>
                        <el-option label="下周" value="next"></el-option>
                      </el-select>
                    </div>
                    <div class="schedule-grid">
                      <div v-for="(day, index) in filteredSchedule" :key="index" class="schedule-day">
                        <h4 class="day-title">{{ day.date }} ({{ day.weekday }})</h4>
                        <div class="time-slots">
                          <div v-for="slot in day.timeSlots" :key="slot.time" class="time-slot"
                            :class="{ available: slot.available, booked: !slot.available }">
                            <div class="slot-time">{{ slot.time }}</div>
                            <div class="slot-status">{{ slot.available ? '可预约' : '已预约' }}</div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 学员评价 -->
            <el-tab-pane label="学员评价" name="reviews">
              <div class="tab-content">
                <div class="coach-reviews">
                  <h3 class="section-title">学员评价</h3>

                  <!-- 评价统计 -->
                  <div class="review-summary">
                    <div class="rating-overview">
                      <div class="rating-score">{{ coachStore.coachDetail.rating }}</div>
                      <div class="rating-stars">
                        <el-rate v-model="coachStore.coachDetail.rating" disabled></el-rate>
                      </div>
                      <div class="rating-count">{{ coachStore.coachDetail.reviewCount }}条评价</div>
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
                  <div class="reviews-list">
                    <div v-for="review in coachStore.coachDetail.reviews" :key="review.id" class="review-item">
                      <div class="review-header">
                        <div class="reviewer-info">
                          <div class="reviewer-avatar">
                            <img :src="review.avatar" :alt="review.name" />
                          </div>
                          <div class="reviewer-details">
                            <div class="reviewer-name">{{ review.name }}</div>
                            <div class="review-date">{{ review.date }}</div>
                          </div>
                        </div>
                        <div class="review-rating">
                          <el-rate v-model="review.rating" disabled></el-rate>
                        </div>
                      </div>
                      <div class="review-content">{{ review.content }}</div>
                      <div v-if="review.reply" class="review-reply">
                        <div class="reply-header">
                          <span class="reply-label">教练回复</span>
                          <span class="reply-date">{{ review.reply.date }}</span>
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
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCoachStore } from '@/stores/coach'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import AppLayout from '@/components/AppLayout.vue'

// 使用路由和教练状态管理
const route = useRoute()
const router = useRouter()
const coachStore = useCoachStore()

// 当前活动标签页
const activeTab = ref('profile')

// 选中的周
const selectedWeek = ref('current')

// 获取教练ID
const coachId = computed(() => {
  return Number(route.params.id)
})

// 过滤后的课程安排
const filteredSchedule = computed(() => {
  if (!coachStore.coachDetail || !coachStore.coachDetail.schedule) {
    return []
  }

  if (selectedWeek.value === 'current') {
    return coachStore.coachDetail.schedule.currentWeek
  } else {
    return coachStore.coachDetail.schedule.nextWeek
  }
})

// 返回教练列表
const goBack = () => {
  router.push('/coaches')
}

// 预约教练
const bookCoach = () => {
  ElMessage.success({ message: '预约教练功能开发中...', duration: 1500 })
}

// 联系教练
const contactCoach = () => {
  ElMessage.success({ message: '联系教练功能开发中...', duration: 1500 })
}

// 过滤课程安排
const filterSchedule = () => {
  // 这个方法会在selectedWeek变化时自动触发计算属性重新计算
}

// 获取评分百分比
const getRatingPercentage = (star: number) => {
  if (!coachStore.coachDetail || !coachStore.coachDetail.ratingDistribution) {
    return 0
  }

  const distribution = coachStore.coachDetail.ratingDistribution
  const count = distribution[star] || 0
  const total = coachStore.coachDetail.reviewCount || 1

  return Math.round((count / total) * 100)
}

// 获取评分数量
const getRatingCount = (star: number) => {
  if (!coachStore.coachDetail || !coachStore.coachDetail.ratingDistribution) {
    return 0
  }

  return coachStore.coachDetail.ratingDistribution[star] || 0
}

// 加载教练详情
const loadCoachDetail = () => {
  if (coachId.value) {
    coachStore.fetchCoachDetail(coachId.value)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadCoachDetail()
})
</script>

<style scoped>
.coach-detail-container {
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

/* 教练基本信息样式 */
.coach-hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 60px 0;
}

.coach-hero-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 40px;
}

.coach-avatar {
  flex-shrink: 0;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  overflow: hidden;
  border: 5px solid rgba(255, 255, 255, 0.3);
}

.coach-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.coach-basic-info {
  flex: 1;
}

.coach-name {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 10px;
}

.coach-specialty {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 15px;
  opacity: 0.9;
}

.coach-rating {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.review-count {
  font-size: 16px;
  opacity: 0.9;
}

.coach-description {
  font-size: 18px;
  line-height: 1.6;
  margin-bottom: 25px;
  opacity: 0.9;
}

.coach-meta {
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

.coach-price {
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

.coach-actions {
  display: flex;
  gap: 20px;
}

.book-btn {
  background: white;
  color: #667eea;
  border: none;
  font-weight: 600;
  padding: 12px 30px;
}

.contact-btn {
  background: transparent;
  color: white;
  border: 2px solid white;
  font-weight: 600;
  padding: 12px 30px;
}

/* 标签页区域样式 */
.coach-tabs-section {
  flex: 1;
  padding: 40px 0;
}

.coach-tabs-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.coach-tabs {
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

/* 个人简介样式 */
.profile-content {
  line-height: 1.8;
  color: #555;
}

/* 专业资质样式 */
.qualifications-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.qualification-item {
  display: flex;
  gap: 15px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.qualification-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.qualification-icon {
  flex-shrink: 0;
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.qualification-content h4 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 5px;
}

.qualification-issuer {
  font-size: 16px;
  color: #667eea;
  margin-bottom: 5px;
}

.qualification-date {
  font-size: 14px;
  color: #666;
}

/* 课程安排样式 */
.schedule-filters {
  margin-bottom: 20px;
}

.schedule-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.week-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.week-selector :deep(.el-select) {
  width: 200px;
}

.week-nav {
  display: flex;
  align-items: center;
  gap: 15px;
}

.schedule-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.schedule-day {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 20px;
}

.day-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 15px;
  text-align: center;
}

.time-slots {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.time-slot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.time-slot.available {
  background: #e8f5e9;
  color: #2e7d32;
}

.time-slot.booked {
  background: #ffebee;
  color: #c62828;
}

.slot-time {
  font-weight: 500;
}

.slot-status {
  font-size: 14px;
}

/* 学员评价样式 */
.review-summary {
  display: flex;
  gap: 40px;
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

.rating-distribution {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rating-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bar-label {
  width: 30px;
  font-size: 14px;
  color: #666;
}

.bar-container {
  flex: 1;
  height: 10px;
  background: #f0f2f5;
  border-radius: 5px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.bar-count {
  width: 30px;
  text-align: right;
  font-size: 14px;
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

.review-reply {
  padding: 15px;
  background: white;
  border-radius: 8px;
  border-left: 3px solid #667eea;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.reply-label {
  font-weight: 600;
  color: #667eea;
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
.icon-experience::before {
  content: '🏆';
}

.icon-courses::before {
  content: '📚';
}

.icon-students::before {
  content: '👥';
}

.icon-certificate::before {
  content: '🏅';
}

/* 响应式设计 */
@media (max-width: 992px) {
  .coach-hero-container {
    flex-direction: column;
    text-align: center;
  }

  .coach-meta {
    justify-content: center;
  }

  .coach-actions {
    justify-content: center;
  }

  .review-summary {
    flex-direction: column;
    align-items: center;
  }
}

@media (max-width: 768px) {
  .coach-name {
    font-size: 32px;
  }

  .coach-specialty {
    font-size: 18px;
  }

  .coach-description {
    font-size: 16px;
  }

  .coach-actions {
    flex-direction: column;
    gap: 10px;
  }

  .book-btn,
  .contact-btn {
    width: 100%;
  }

  .qualifications-list {
    grid-template-columns: 1fr;
  }

  .schedule-grid {
    grid-template-columns: 1fr;
  }

  .tab-content {
    padding: 20px;
  }
}
</style>