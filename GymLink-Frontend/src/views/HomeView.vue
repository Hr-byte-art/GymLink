<template>
    <AppLayout>
        <!-- 主要内容区域 -->
        <main class="main-content">
            <!-- 加载状态 -->
            <div v-if="homeStore.loading" class="loading-container">
                <el-loading :loading="true" text="加载中..."></el-loading>
            </div>

            <!-- 错误状态 -->
            <div v-else-if="homeStore.error" class="error-container">
                <el-result icon="warning" title="加载失败" :sub-title="homeStore.error">
                    <template #extra>
                        <el-button type="primary" @click="loadHomeData">重新加载</el-button>
                    </template>
                </el-result>
            </div>

            <!-- 正常内容 -->
            <template v-else>
                <!-- 轮播图区域 -->
                <section v-if="homeStore.hasCarouselItems" class="hero-section">
                    <el-carousel :interval="5000" arrow="always" height="500px">
                        <el-carousel-item v-for="item in homeStore.carouselItems" :key="item.id">
                            <div class="carousel-item" :style="{ backgroundImage: `url(${item.image})` }">
                                <div class="carousel-content">
                                    <h2>{{ item.title }}</h2>
                                    <p>{{ item.description }}</p>
                                    <el-button type="primary" class="carousel-btn"
                                        @click="navigateToLink(item.link)">了解更多</el-button>
                                </div>
                            </div>
                        </el-carousel-item>
                    </el-carousel>
                </section>

                <!-- 特色服务区域 -->
                <section v-if="homeStore.hasFeatures" class="features-section">
                    <div class="section-container">
                        <h2 class="section-title">我们的特色服务</h2>
                        <div class="features-grid">
                            <div class="feature-card" v-for="feature in homeStore.features" :key="feature.id">
                                <div class="feature-icon">
                                    <img :src="feature.icon" :alt="feature.title" />
                                </div>
                                <h3 class="feature-title">{{ feature.title }}</h3>
                                <p class="feature-description">{{ feature.description }}</p>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- 课程推荐区域 -->
                <section v-if="homeStore.hasCourses" class="courses-section">
                    <div class="section-container">
                        <h2 class="section-title">热门课程</h2>
                        <div class="courses-grid">
                            <div class="course-card" v-for="course in homeStore.courses" :key="course.id">
                                <div class="course-image">
                                    <img :src="course.image" :alt="course.title" />
                                </div>
                                <div class="course-content">
                                    <h3 class="course-title">{{ course.title }}</h3>
                                    <p class="course-instructor">教练: {{ course.instructor }}</p>
                                    <div class="course-meta">
                                        <span class="course-time">{{ course.time }}</span>
                                        <span class="course-difficulty">{{ course.difficulty }}</span>
                                    </div>
                                    <el-button type="primary" class="course-btn">预约课程</el-button>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- 教练团队区域 -->
                <section v-if="homeStore.hasCoaches" class="coaches-section">
                    <div class="section-container">
                        <h2 class="section-title">专业教练团队</h2>
                        <div class="coaches-grid">
                            <div class="coach-card" v-for="coach in homeStore.coaches" :key="coach.id">
                                <div class="coach-avatar">
                                    <img :src="coach.avatar" :alt="coach.name" />
                                </div>
                                <h3 class="coach-name">{{ coach.name }}</h3>
                                <p class="coach-specialty">{{ coach.specialty }}</p>
                                <p class="coach-description">{{ coach.description }}</p>
                                <el-button type="primary" class="coach-btn">预约教练</el-button>
                            </div>
                        </div>
                    </div>
                </section>
            </template>
        </main>
    </AppLayout>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useHomeStore } from '@/stores/home'
import AppLayout from '@/components/AppLayout.vue'

// 使用路由和首页状态管理
const router = useRouter()
const homeStore = useHomeStore()

// 加载首页数据
const loadHomeData = () => {
    homeStore.fetchHomeData()
}

// 导航到链接
const navigateToLink = (link?: string) => {
    if (link) {
        router.push(link)
    }
}

// 组件挂载时加载数据
onMounted(() => {
    loadHomeData()
})
</script>

<style scoped>
    /* 加载和错误状态样式 */
    .loading-container,
    .error-container {
        display: flex;
        justify-content: center;
        align-items: center;
        min-height: 300px;
    }

    .home {
        min-height: 100vh;
        display: flex;
        flex-direction: column;
    }

    /* 主要内容样式 */
    .main-content {
        flex: 1;
    }

    /* 轮播图样式 */
    .hero-section {
        margin-bottom: 50px;
    }

    .carousel-item {
        height: 100%;
        background-size: cover;
        background-position: center;
        position: relative;
    }

    .carousel-content {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        background: rgba(0, 0, 0, 0.4);
        color: white;
        padding: 0 20px;
    }

    .carousel-content h2 {
        font-size: 48px;
        margin-bottom: 20px;
        font-weight: 700;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
        width: 600px;
        max-width: 100%;
        text-align: left;
    }

    .carousel-content p {
        font-size: 20px;
        width: 600px;
        max-width: 100%;
        margin-bottom: 30px;
        line-height: 1.6;
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
        text-align: left;
    }

    .carousel-btn {
        background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
        border: none;
        padding: 12px 30px;
        font-size: 18px;
        font-weight: 600;
        width: 600px;
        max-width: 100%;
        text-align: center;
    }

    /* 移动端样式 */
    @media (max-width: 768px) {
        .carousel-content {
            align-items: center;
            text-align: center;
            padding: 0 20px;
        }
    }

    /* Element Plus轮播图样式覆盖 */
    :deep(.el-carousel) {
        overflow: hidden;
    }

    :deep(.el-carousel__container) {
        height: 100%;
    }

    :deep(.el-carousel__item) {
        overflow: hidden;
    }

    /* 通用区域样式 */
    .section-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
    }

    .section-title {
        text-align: center;
        font-size: 36px;
        font-weight: 700;
        color: #2c3e50;
        margin-bottom: 50px;
        position: relative;
    }
/* 通用区域样式 */
.section-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
}

.section-title {
    text-align: center;
    font-size: 36px;
    font-weight: 700;
    color: #2c3e50;
    margin-bottom: 50px;
    position: relative;
}

.section-title::after {
    content: '';
    display: block;
    width: 80px;
    height: 4px;
    background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
    margin: 15px auto 0;
    border-radius: 2px;
}

/* 特色服务样式 */
.features-section {
    padding: 80px 0;
    background: #f8f9fa;
}

.features-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 30px;
}

.feature-card {
    background: white;
    border-radius: 10px;
    padding: 30px;
    text-align: center;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
}


/* 课程样式 */
.courses-section {
    padding: 80px 0;
}

.courses-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 30px;
}

.course-card {
    background: white;
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
}

.course-card:hover {
    transform: translateY(-10px);
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.course-image {
    height: 200px;
    overflow: hidden;
}

.course-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
}

.course-card:hover .course-image img {
    transform: scale(1.05);
}

.course-content {
    padding: 20px;
}

.course-title {
    font-size: 18px;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 10px;
}

.course-instructor {
    color: #7f8c8d;
    margin-bottom: 15px;
}

.course-meta {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
}

.course-time,
.course-difficulty {
    font-size: 14px;
    color: #7f8c8d;
}

.course-difficulty {
    background: #f0f2f5;
    padding: 2px 8px;
    border-radius: 4px;
}

.course-btn {
    width: 100%;
    background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
    border: none;
}

/* 教练样式 */
.coaches-section {
    padding: 80px 0;
    background: #f8f9fa;
}

.coaches-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 30px;
}

.coach-card {
    background: white;
    border-radius: 10px;
    padding: 30px;
    text-align: center;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
}

.coach-card:hover {
    transform: translateY(-10px);
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.coach-avatar {
    width: 120px;
    height: 120px;
    margin: 0 auto 20px;
    border-radius: 50%;
    overflow: hidden;
}

.coach-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.coach-name {
    font-size: 20px;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 5px;
}

.coach-specialty {
    color: #409eff;
    font-weight: 500;
    margin-bottom: 15px;
}

.coach-description {
    color: #7f8c8d;
    line-height: 1.6;
    margin-bottom: 20px;
}

.coach-btn {
    width: 100%;
    background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
    border: none;
}

/* 响应式设计 */
@media (max-width: 992px) {
    .carousel-content h2 {
        font-size: 36px;
    }

    .carousel-content p {
        font-size: 16px;
    }
}

@media (max-width: 768px) {
    .section-title {
        font-size: 28px;
    }

    .features-grid,
    .courses-grid,
    .coaches-grid {
        grid-template-columns: 1fr;
    }
}
</style>