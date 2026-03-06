<template>
  <AppLayout>
    <div class="about-view">
      <div class="container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1 class="page-title">关于我们</h1>
          <p class="page-subtitle">了解 GymLink 的故事和使命</p>
        </div>

        <!-- 公司简介 -->
        <section class="company-intro">
          <div class="intro-content">
            <div class="intro-text">
              <h2>我们的故事</h2>
              <p>
                GymLink 成立于 2020 年，致力于打造一个连接健身爱好者、专业教练和优质健身资源的综合平台。
                我们相信每个人都应该拥有健康的生活方式，而我们的使命就是让健身变得更加便捷、有趣和高效。
              </p>
              <p>
                通过整合线上线下资源，我们为用户提供个性化的健身解决方案，包括专业课程、营养指导、
                设备推荐和社区交流等全方位服务。
              </p>
            </div>
            <div class="intro-image">
              <img src="/logo.png" alt="GymLink Logo" />
            </div>
          </div>
        </section>

        <!-- 核心价值观 -->
        <section class="core-values">
          <h2 class="section-title">核心价值观</h2>
          <div class="values-grid">
            <div class="value-card">
              <div class="value-icon">💪</div>
              <h3>专业至上</h3>
              <p>汇聚行业顶尖教练和最新健身理念，确保用户获得最专业的指导</p>
            </div>
            <div class="value-card">
              <div class="value-icon">🤝</div>
              <h3>用户第一</h3>
              <p>始终将用户需求放在首位，提供个性化的健身体验</p>
            </div>
            <div class="value-card">
              <div class="value-icon">🌟</div>
              <h3>持续创新</h3>
              <p>不断探索新的健身科技和方法，引领行业发展</p>
            </div>
            <div class="value-card">
              <div class="value-icon">🌍</div>
              <h3>健康生活</h3>
              <p>推广健康的生活方式，让更多人享受运动带来的快乐</p>
            </div>
          </div>
        </section>

        <!-- 团队介绍 -->
        <section class="team-section">
          <h2 class="section-title">我们的团队</h2>
          <div class="team-grid">
            <div class="team-member">
              <div class="member-avatar">
                <img src="/coach1.svg" alt="张明 - 创始人" />
              </div>
              <h3>张明</h3>
              <p class="member-title">创始人 & CEO</p>
              <p class="member-desc">10年健身行业经验，前知名连锁健身房高管</p>
            </div>
            <div class="team-member">
              <div class="member-avatar">
                <img src="/coach2.svg" alt="李娜 - 技术总监" />
              </div>
              <h3>李娜</h3>
              <p class="member-title">技术总监</p>
              <p class="member-desc">前互联网公司技术专家，专注健身科技研发</p>
            </div>
            <div class="team-member">
              <div class="member-avatar">
                <img src="/coach3.svg" alt="王强 - 内容总监" />
              </div>
              <h3>王强</h3>
              <p class="member-title">内容总监</p>
              <p class="member-desc">资深健身教练，国家级运动营养师</p>
            </div>
          </div>
        </section>

        <!-- 服务优势 -->
        <section class="advantages-section">
          <h2 class="section-title">为什么选择我们</h2>
          <div class="advantages-content">
            <div class="advantage-item">
              <div class="advantage-number">1000+</div>
              <div class="advantage-text">
                <h3>专业教练</h3>
                <p>汇聚全国各地优秀健身教练</p>
              </div>
            </div>
            <div class="advantage-item">
              <div class="advantage-number">500+</div>
              <div class="advantage-text">
                <h3>精品课程</h3>
                <p>涵盖各种健身需求的优质课程</p>
              </div>
            </div>
            <div class="advantage-item">
              <div class="advantage-number">50万+</div>
              <div class="advantage-text">
                <h3>活跃用户</h3>
                <p>遍布全国的健身爱好者社区</p>
              </div>
            </div>
            <div class="advantage-item">
              <div class="advantage-number">98%</div>
              <div class="advantage-text">
                <h3>用户满意度</h3>
                <p>获得用户一致好评</p>
              </div>
            </div>
          </div>
        </section>

        <!-- 联系我们 -->
        <section class="contact-section">
          <h2 class="section-title">联系我们</h2>
          <div class="contact-content">
            <div class="contact-info">
              <div class="contact-item">
                <div class="contact-icon">📧</div>
                <div class="contact-text">
                  <h3>邮箱</h3>
                  <p>contact@gymlink.com</p>
                </div>
              </div>
              <div class="contact-item">
                <div class="contact-icon">📞</div>
                <div class="contact-text">
                  <h3>电话</h3>
                  <p>400-123-4567</p>
                </div>
              </div>
              <div class="contact-item">
                <div class="contact-icon">📍</div>
                <div class="contact-text">
                  <h3>地址</h3>
                  <p>北京市朝阳区健身大厦8楼</p>
                </div>
              </div>
            </div>
            <div class="contact-form">
              <h3>发送消息</h3>
              <el-form :model="contactForm" label-position="top">
                <el-form-item label="姓名">
                  <el-input v-model="contactForm.name" placeholder="请输入您的姓名" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="contactForm.email" placeholder="请输入您的邮箱" />
                </el-form-item>
                <el-form-item label="消息">
                  <el-input
                    v-model="contactForm.message"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入您的消息"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitContact" class="submit-btn">
                    发送消息
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </section>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'

const contactForm = reactive({
  name: '',
  email: '',
  message: ''
})

const submitContact = () => {
  if (!contactForm.name || !contactForm.email || !contactForm.message) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  // 这里可以添加实际的提交逻辑
  ElMessage.success('消息发送成功！我们会尽快回复您')
  
  // 清空表单
  contactForm.name = ''
  contactForm.email = ''
  contactForm.message = ''
}
</script>

<style scoped>
.about-view {
  --primary: #f97316;
  --primary-strong: #c2410c;
  --ink: #1f2937;
  --muted: #64748b;
  --line: #f4e7d8;
  min-height: 100vh;
  padding: 52px 0 70px;
  background:
    radial-gradient(circle at 8% 4%, rgba(251, 146, 60, 0.2) 0%, rgba(251, 146, 60, 0) 32%),
    radial-gradient(circle at 94% 2%, rgba(251, 191, 36, 0.14) 0%, rgba(251, 191, 36, 0) 30%),
    linear-gradient(180deg, #fffaf5 0%, #ffffff 45%);
}

.container {
  max-width: 1220px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-title {
  margin: 0 0 10px;
  font-family: "Palatino Linotype", "Book Antiqua", Palatino, serif;
  font-size: clamp(40px, 6vw, 62px);
  font-weight: 700;
  letter-spacing: 0.4px;
  color: #2a1f12;
}

.page-subtitle {
  margin: 0 auto;
  max-width: 560px;
  font-size: 18px;
  color: var(--muted);
}

.company-intro,
.core-values,
.team-section,
.advantages-section,
.contact-section {
  border-radius: 22px;
  border: 1px solid var(--line);
  background: linear-gradient(180deg, #ffffff 0%, #fffbf7 100%);
  box-shadow: 0 12px 30px rgba(248, 146, 43, 0.08);
  padding: 30px;
  margin-bottom: 22px;
}

.intro-content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 24px;
  align-items: center;
}

.intro-text h2 {
  margin: 0 0 14px;
  font-size: 31px;
  color: #2a1f12;
}

.intro-text p {
  margin: 0 0 12px;
  font-size: 15px;
  line-height: 1.9;
  color: #4b5563;
}

.intro-image {
  text-align: center;
}

.intro-image img {
  width: 100%;
  max-width: 210px;
  border-radius: 16px;
  border: 1px solid var(--line);
  background: #fff;
  padding: 16px;
}

.section-title {
  margin: 0 0 22px;
  padding-bottom: 10px;
  font-size: 30px;
  color: #2a1f12;
  text-align: center;
  position: relative;
}

.section-title::after {
  content: "";
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  width: 74px;
  height: 4px;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--primary) 0%, #fdba74 100%);
}

.values-grid,
.team-grid,
.advantages-content {
  display: grid;
  gap: 14px;
}

.values-grid,
.team-grid {
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.advantages-content {
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
}

.value-card,
.team-member,
.advantage-item {
  border: 1px solid var(--line);
  border-radius: 14px;
  background: #fffaf5;
  padding: 16px;
  text-align: center;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.value-card:hover,
.team-member:hover,
.advantage-item:hover {
  transform: translateY(-3px);
  border-color: #fdba74;
  box-shadow: 0 14px 24px rgba(249, 115, 22, 0.14);
}

.value-icon {
  font-size: 34px;
  margin-bottom: 10px;
}

.value-card h3,
.team-member h3,
.advantage-text h3 {
  margin: 0 0 7px;
  font-size: 19px;
  color: #2a1f12;
}

.value-card p,
.member-desc,
.advantage-text p {
  margin: 0;
  color: var(--muted);
  line-height: 1.75;
  font-size: 14px;
}

.member-avatar {
  width: 96px;
  height: 96px;
  margin: 0 auto 10px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #fed7aa;
  background: #fff;
}

.member-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.member-title {
  margin: 0 0 7px;
  color: var(--primary-strong);
  font-weight: 700;
  font-size: 14px;
}

.advantage-number {
  font-family: "Trebuchet MS", "Segoe UI", Tahoma, sans-serif;
  font-size: 36px;
  line-height: 1;
  font-weight: 800;
  color: var(--primary);
  margin-bottom: 8px;
}

.contact-content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 20px;
}

.contact-info {
  display: grid;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fffaf5;
  padding: 12px;
}

.contact-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: #ffedd5;
}

.contact-text h3 {
  margin: 0 0 2px;
  font-size: 16px;
  color: #2a1f12;
}

.contact-text p {
  margin: 0;
  color: var(--muted);
  font-size: 14px;
}

.contact-form {
  border: 1px solid var(--line);
  border-radius: 14px;
  background: #fff;
  padding: 14px;
}

.contact-form h3 {
  margin: 0 0 10px;
  font-size: 22px;
  color: #2a1f12;
}

.contact-form :deep(.el-form-item__label) {
  color: #334155;
  font-weight: 600;
}

.contact-form :deep(.el-input__wrapper),
.contact-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  box-shadow: none;
}

.contact-form :deep(.el-input__wrapper.is-focus),
.contact-form :deep(.el-textarea__inner:focus) {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.14);
}

.submit-btn {
  width: 100%;
  border: none;
  border-radius: 10px;
  padding: 11px 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 20px rgba(249, 115, 22, 0.3);
}

@media (max-width: 992px) {
  .intro-content {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .intro-image {
    order: -1;
  }

  .contact-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .about-view {
    padding: 30px 0 44px;
  }

  .container {
    padding: 0 14px;
  }

  .company-intro,
  .core-values,
  .team-section,
  .advantages-section,
  .contact-section {
    border-radius: 16px;
    padding: 16px 14px;
    margin-bottom: 14px;
  }

  .section-title {
    font-size: 25px;
  }

  .page-subtitle {
    font-size: 16px;
  }
}
</style>
