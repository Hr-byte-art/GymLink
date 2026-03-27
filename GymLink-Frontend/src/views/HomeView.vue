<template>
  <AppLayout>
    <el-dialog
      v-model="announcementVisible"
      width="520px"
      :close-on-click-modal="false"
      class="announcement-dialog"
    >
      <template #header>
        <div class="announcement-header">
          <img src="/announcement.svg" alt="系统公告" class="announcement-icon" />
          <span>系统公告</span>
        </div>
      </template>
      <div v-if="currentAnnouncement" class="announcement-content">
        <h3 class="announcement-title">{{ currentAnnouncement.title }}</h3>
        <div class="announcement-time">{{ formatDate(currentAnnouncement.createTime) }}</div>
        <div class="announcement-text">{{ currentAnnouncement.content }}</div>
        <div v-if="missedCount > 0" class="announcement-missed">
          你还有 {{ missedCount }} 条未读公告。
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="closeAnnouncement">知道了</el-button>
      </template>
    </el-dialog>

    <main class="main-content">
      <div v-if="homeStore.loading" class="loading-container">
        <el-loading :loading="true" text="加载中..."></el-loading>
      </div>

      <div v-else-if="homeStore.error" class="error-container">
        <el-result icon="warning" title="加载失败" :sub-title="homeStore.error">
          <template #extra>
            <el-button type="primary" @click="loadHomeData">重试</el-button>
          </template>
        </el-result>
      </div>

      <template v-else>
        <section class="hero-section">
          <el-carousel v-if="homeStore.hasCarouselItems" :interval="5000" arrow="always" height="560px" class="hero-carousel">
            <el-carousel-item v-for="item in homeStore.carouselItems" :key="item.id">
              <article class="hero-slide" :style="getHeroStyle(item.image)">
                <div class="hero-overlay"></div>
                <div class="hero-content">
                  <span class="hero-kicker">GymLink 会员平台</span>
                  <h1>{{ item.title }}</h1>
                  <p>{{ item.description }}</p>
                  <div class="hero-actions">
                    <el-button type="primary" class="hero-btn" @click="navigateToLink(item.link)">立即查看</el-button>
                    <el-button class="hero-btn ghost" @click="router.push('/courses')">浏览课程</el-button>
                  </div>
                </div>
              </article>
            </el-carousel-item>
          </el-carousel>

          <article v-else class="hero-slide fallback" :style="getHeroStyle(undefined)">
            <div class="hero-overlay"></div>
            <div class="hero-content">
              <span class="hero-kicker">GymLink 会员平台</span>
              <h1>科学训练，持续进步</h1>
              <p>在一个平台内发现课程、教练、器材与饮食计划。</p>
              <div class="hero-actions">
                <el-button type="primary" class="hero-btn" @click="router.push('/courses')">立即开始</el-button>
              </div>
            </div>
          </article>
        </section>

        <section v-if="homeStore.hasFeatures" class="features-section">
          <div class="section-container">
            <div class="section-head">
              <h2>你可以做什么</h2>
              <p>会员所需功能一站式覆盖</p>
            </div>
            <div class="features-grid">
              <article class="feature-card" v-for="feature in homeStore.features" :key="feature.id">
                <div class="feature-icon-wrap">
                  <img :src="feature.icon" :alt="feature.title" class="feature-icon" />
                </div>
                <h3>{{ feature.title }}</h3>
                <p>{{ feature.description }}</p>
              </article>
            </div>
          </div>
        </section>

        <section v-if="homeStore.hasCourses" class="courses-section">
          <div class="section-container">
            <div class="section-head">
              <h2>热门课程</h2>
              <p>选择一门课程，预约下一次训练</p>
            </div>
            <div class="courses-grid">
              <article class="course-card" v-for="course in homeStore.courses" :key="course.id" @click="viewCourseDetail(course.id)">
                <div class="course-image">
                  <img :src="course.image" :alt="course.name" />
                </div>
                <div class="course-content">
                  <h3>{{ course.name }}</h3>
                  <p class="course-instructor">教练编号：{{ course.coachId }}</p>
                  <div class="course-meta">
                    <span>{{ course.duration }} 分钟</span>
                    <span class="course-chip">{{ course.difficulty }}</span>
                  </div>
                </div>
              </article>
            </div>
          </div>
        </section>

        <section v-if="homeStore.hasCoaches" class="coaches-section">
          <div class="section-container">
            <div class="section-head">
              <h2>专业教练</h2>
              <p>与专业教练一起训练，更快提升</p>
            </div>
            <div class="coaches-grid">
              <article class="coach-card" v-for="coach in homeStore.coaches" :key="coach.id" @click="viewCoachDetail(coach.id)">
                <div class="coach-avatar">
                  <img :src="coach.avatar" :alt="coach.name" />
                </div>
                <h3>{{ coach.name }}</h3>
                <p class="coach-specialty">{{ getCoachSpecialtyName(coach.specialty) }}</p>
                <p class="coach-description">{{ coach.intro }}</p>
              </article>
            </div>
          </div>
        </section>
      </template>
    </main>
  </AppLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useHomeStore } from '@/stores/home'
import { useAuthStore } from '@/stores/auth'
import AppLayout from '@/components/AppLayout.vue'
import { getCoachSpecialtyName } from '@/constants/categories'
import { getUnreadAnnouncement, markAnnouncementAsRead, type Announcement } from '@/api/announcement'

const router = useRouter()
const homeStore = useHomeStore()
const authStore = useAuthStore()

const announcementVisible = ref(false)
const currentAnnouncement = ref<Announcement | null>(null)
const missedCount = ref(0)

const getAuthUserId = (): number | null => {
  const rawId = authStore.user?.id
  if (rawId === undefined || rawId === null) return null
  const userId = Number(rawId)
  return Number.isFinite(userId) ? userId : null
}

const checkAnnouncements = async () => {
  const userId = getAuthUserId()
  if (!authStore.isAuthenticated || userId === null) {
    return
  }
  try {
    const result = await getUnreadAnnouncement(userId)
    if (result && result.announcement) {
      currentAnnouncement.value = result.announcement
      missedCount.value = result.missedCount || 0
      announcementVisible.value = true
    }
  } catch (e) {
    console.error('获取公告失败:', e)
  }
}

const closeAnnouncement = async () => {
  const userId = getAuthUserId()
  if (currentAnnouncement.value && userId !== null) {
    try {
      await markAnnouncementAsRead(userId, currentAnnouncement.value.id)
    } catch (e) {
      console.error('标记已读失败:', e)
    }
  }
  announcementVisible.value = false
}

const formatDate = (date: string) => (date ? new Date(date).toLocaleString('zh-CN') : '')

const loadHomeData = () => {
  homeStore.fetchHomeData()
}

const navigateToLink = (link?: string) => {
  if (link) {
    router.push(link)
  }
}

const viewCourseDetail = (id: number | string | undefined) => {
  if (id !== undefined && id !== null) {
    router.push(`/courses/${id}`)
  }
}

const viewCoachDetail = (id: number | string | undefined) => {
  if (id !== undefined && id !== null) {
    router.push(`/coaches/${id}`)
  }
}

const getHeroStyle = (image?: string) => ({
  backgroundImage: `linear-gradient(135deg, rgba(15, 23, 42, 0.72) 0%, rgba(15, 23, 42, 0.28) 100%), url(${image || '/home-hero-fallback.png'})`
})

onMounted(() => {
  loadHomeData()
  checkAnnouncements()
})
</script>

<style scoped>
.main-content {
  --primary: #f97316;
  --primary-dark: #ea580c;
  --ink: #0f172a;
  --muted: #475569;
  --surface: #ffffff;
  --surface-soft: #f8fafc;
  --line: #e2e8f0;
}

.loading-container,
.error-container {
  min-height: 320px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.hero-section {
  padding: 24px 18px 0;
}

.hero-carousel,
.hero-slide {
  border-radius: 24px;
  overflow: hidden;
}

.hero-slide {
  min-height: 560px;
  position: relative;
  background-size: cover;
  background-position: center;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 80% 20%, rgba(249, 115, 22, 0.28), transparent 45%);
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 760px;
  padding: 120px 68px;
  color: #f8fafc;
}

.hero-kicker {
  display: inline-block;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.46);
  border: 1px solid rgba(226, 232, 240, 0.35);
  margin-bottom: 18px;
  font-size: 13px;
  letter-spacing: 0.5px;
}

.hero-content h1 {
  margin: 0;
  font-size: clamp(34px, 4.2vw, 54px);
  line-height: 1.08;
}

.hero-content p {
  margin: 16px 0 30px;
  font-size: 18px;
  line-height: 1.6;
  color: #e2e8f0;
}

.hero-actions {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.hero-btn {
  min-width: 136px;
  min-height: 44px;
  border-radius: 10px;
  border: none;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
}

.hero-btn.ghost {
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.38);
  color: #ffffff;
}

.section-container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-head {
  text-align: center;
  margin-bottom: 34px;
}

.section-head h2 {
  margin: 0;
  font-size: clamp(30px, 3vw, 40px);
  color: var(--ink);
}

.section-head p {
  margin: 12px 0 0;
  color: var(--muted);
  font-size: 16px;
}

.features-section,
.courses-section,
.coaches-section {
  padding: 84px 0;
}

.features-section,
.coaches-section {
  background:
    radial-gradient(circle at 10% 0%, rgba(249, 115, 22, 0.08), transparent 32%),
    var(--surface-soft);
}

.features-grid,
.courses-grid,
.coaches-grid {
  display: grid;
  gap: 22px;
}

.features-grid {
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
}

.courses-grid {
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
}

.coaches-grid {
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
}

.feature-card,
.course-card,
.coach-card {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 18px;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.06);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.feature-card:hover,
.course-card:hover,
.coach-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.12);
  border-color: rgba(249, 115, 22, 0.42);
}

.feature-card {
  padding: 26px;
}

.feature-icon-wrap {
  width: 58px;
  height: 58px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.16) 0%, rgba(34, 197, 94, 0.16) 100%);
  display: grid;
  place-items: center;
}

.feature-icon {
  width: 28px;
  height: 28px;
}

.feature-card h3,
.course-content h3,
.coach-card h3 {
  margin: 16px 0 10px;
  color: var(--ink);
  font-size: 22px;
}

.feature-card p,
.coach-description {
  margin: 0;
  color: var(--muted);
  line-height: 1.7;
}

.course-card,
.coach-card {
  overflow: hidden;
  cursor: pointer;
}

.course-image {
  height: 220px;
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-content {
  padding: 22px;
}

.course-instructor {
  margin: 0 0 14px;
  color: var(--muted);
}

.course-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--muted);
}

.course-chip {
  background: rgba(249, 115, 22, 0.12);
  border: 1px solid rgba(249, 115, 22, 0.36);
  color: #9a3412;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 13px;
}

.coach-card {
  padding: 26px;
  text-align: center;
}

.coach-avatar {
  width: 110px;
  height: 110px;
  margin: 0 auto;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid rgba(249, 115, 22, 0.26);
}

.coach-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.coach-specialty {
  color: #c2410c;
  font-weight: 700;
  margin: 0 0 10px;
}

.announcement-content {
  padding: 8px 0;
}

.announcement-title {
  margin: 0 0 8px;
  color: var(--ink);
}

.announcement-time {
  color: #94a3b8;
  font-size: 13px;
  margin-bottom: 12px;
}

.announcement-text {
  color: var(--muted);
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.announcement-missed {
  margin-top: 14px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  color: #9a3412;
}

.announcement-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}

.announcement-icon {
  width: 22px;
  height: 22px;
}

:deep(.announcement-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 15px 20px;
  background: linear-gradient(135deg, var(--primary) 0%, #fb923c 100%);
}

:deep(.announcement-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
}

@media (max-width: 860px) {
  .hero-section {
    padding: 12px 12px 0;
  }

  .hero-slide {
    min-height: 460px;
  }

  .hero-content {
    padding: 80px 24px;
  }

  .hero-content p {
    font-size: 16px;
  }

  .features-section,
  .courses-section,
  .coaches-section {
    padding: 56px 0;
  }

  .features-grid,
  .courses-grid,
  .coaches-grid {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .feature-card,
  .course-card,
  .coach-card {
    transition: none;
  }

  .feature-card:hover,
  .course-card:hover,
  .coach-card:hover {
    transform: none;
  }
}
</style>
