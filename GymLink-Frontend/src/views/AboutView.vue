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
  min-height: 100vh;
  background-color: #f8f9fa;
  padding: 60px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 60px;
}

.page-title {
  font-size: 48px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 15px;
}

.page-subtitle {
  font-size: 20px;
  color: #7f8c8d;
  margin: 0;
}

/* 公司简介 */
.company-intro {
  background: white;
  border-radius: 15px;
  padding: 60px;
  margin-bottom: 60px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
}

.intro-content {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 40px;
  align-items: center;
}

.intro-text h2 {
  font-size: 32px;
  color: #2c3e50;
  margin-bottom: 25px;
}

.intro-text p {
  font-size: 16px;
  line-height: 1.8;
  color: #555;
  margin-bottom: 20px;
}

.intro-image img {
  width: 100%;
  height: auto;
  border-radius: 10px;
}

/* 核心价值观 */
.core-values {
  background: white;
  border-radius: 15px;
  padding: 60px;
  margin-bottom: 60px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 36px;
  color: #2c3e50;
  text-align: center;
  margin-bottom: 50px;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
  border-radius: 2px;
}

.values-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.value-card {
  text-align: center;
  padding: 30px 20px;
  border-radius: 10px;
  background: #f8f9fa;
  transition: transform 0.3s ease;
}

.value-card:hover {
  transform: translateY(-5px);
}

.value-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.value-card h3 {
  font-size: 20px;
  color: #2c3e50;
  margin-bottom: 15px;
}

.value-card p {
  font-size: 14px;
  color: #7f8c8d;
  line-height: 1.6;
}

/* 团队介绍 */
.team-section {
  background: white;
  border-radius: 15px;
  padding: 60px;
  margin-bottom: 60px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
}

.team-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
}

.team-member {
  text-align: center;
}

.member-avatar {
  width: 120px;
  height: 120px;
  margin: 0 auto 20px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid #f0f2f5;
}

.member-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.team-member h3 {
  font-size: 20px;
  color: #2c3e50;
  margin-bottom: 5px;
}

.member-title {
  font-size: 16px;
  color: #409eff;
  margin-bottom: 10px;
}

.member-desc {
  font-size: 14px;
  color: #7f8c8d;
  line-height: 1.6;
}

/* 服务优势 */
.advantages-section {
  background: white;
  border-radius: 15px;
  padding: 60px;
  margin-bottom: 60px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
}

.advantages-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
}

.advantage-item {
  text-align: center;
  padding: 20px;
}

.advantage-number {
  font-size: 48px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 15px;
}

.advantage-text h3 {
  font-size: 18px;
  color: #2c3e50;
  margin-bottom: 10px;
}

.advantage-text p {
  font-size: 14px;
  color: #7f8c8d;
}

/* 联系我们 */
.contact-section {
  background: white;
  border-radius: 15px;
  padding: 60px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
}

.contact-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.contact-icon {
  font-size: 24px;
  width: 50px;
  height: 50px;
  background: #f0f2f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.contact-text h3 {
  font-size: 18px;
  color: #2c3e50;
  margin-bottom: 5px;
}

.contact-text p {
  font-size: 16px;
  color: #7f8c8d;
  margin: 0;
}

.contact-form h3 {
  font-size: 24px;
  color: #2c3e50;
  margin-bottom: 30px;
}

.submit-btn {
  width: 100%;
  background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
  border: none;
  padding: 12px 30px;
  font-size: 16px;
  font-weight: 600;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(64, 158, 255, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .about-view {
    padding: 40px 0;
  }

  .page-title {
    font-size: 36px;
  }

  .page-subtitle {
    font-size: 18px;
  }

  .company-intro,
  .core-values,
  .team-section,
  .advantages-section,
  .contact-section {
    padding: 40px 30px;
  }

  .intro-content {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .intro-image {
    order: -1;
    margin-bottom: 30px;
  }

  .values-grid,
  .team-grid,
  .advantages-content {
    grid-template-columns: 1fr;
  }

  .contact-content {
    grid-template-columns: 1fr;
    gap: 40px;
  }
}
</style>