<template>
  <footer class="footer">
    <div class="footer-container">
      <div class="footer-content">
        <div class="footer-section">
          <h3 class="footer-title">关于GymLink</h3>
          <p class="footer-text">
            GymLink是您身边的健身专家，提供专业的健身指导、先进的健身设备和舒适的健身环境。
          </p>
          <div class="social-links">
            <a href="#" class="social-link">
              <i class="icon-wechat"></i>
            </a>
            <a href="#" class="social-link">
              <i class="icon-weibo"></i>
            </a>
            <a href="#" class="social-link">
              <i class="icon-douyin"></i>
            </a>
          </div>
        </div>
        
        <div class="footer-section">
          <h3 class="footer-title">快速链接</h3>
          <ul class="footer-links">
            <li><router-link to="/" class="footer-link">首页</router-link></li>
            <li><router-link to="/courses" class="footer-link">课程</router-link></li>
            <li><router-link to="/coaches" class="footer-link">教练</router-link></li>
            <li><router-link to="/facilities" class="footer-link">设施</router-link></li>
          </ul>
        </div>
        
        <div class="footer-section">
          <h3 class="footer-title">联系方式</h3>
          <ul class="contact-info">
            <li class="contact-item">
              <i class="icon-location"></i>
              <span>北京市朝阳区建国路88号</span>
            </li>
            <li class="contact-item">
              <i class="icon-phone"></i>
              <span>400-888-8888</span>
            </li>
            <li class="contact-item">
              <i class="icon-email"></i>
              <span>info@gymlink.com</span>
            </li>
          </ul>
        </div>
        
        <div class="footer-section">
          <h3 class="footer-title">营业时间</h3>
          <ul class="opening-hours">
            <li class="hours-item">
              <span class="day">周一至周五</span>
              <span class="time">06:00 - 22:00</span>
            </li>
            <li class="hours-item">
              <span class="day">周六、周日</span>
              <span class="time">07:00 - 21:00</span>
            </li>
            <li class="hours-item">
              <span class="day">节假日</span>
              <span class="time">08:00 - 20:00</span>
            </li>
          </ul>
        </div>
      </div>
      
      <div class="footer-bottom">
        <div class="copyright">
          <p>&copy; {{ currentYear }} GymLink. 保留所有权利。</p>
        </div>
        <div class="footer-bottom-links">
          <a href="#" class="bottom-link">隐私政策</a>
          <a href="#" class="bottom-link">使用条款</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'

// 页脚组件，包含公司信息、快速链接、联系方式和营业时间
const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.footer {
  background-color: #222;
  color: #ccc;
  padding: 60px 0 20px;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
  margin-bottom: 40px;
}

.footer-section {
  margin-bottom: 20px;
}

.footer-title {
  color: #fff;
  font-size: 18px;
  margin-bottom: 20px;
  position: relative;
  padding-bottom: 10px;
}

.footer-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 2px;
  background-color: #4a6cf7;
}

.footer-text {
  line-height: 1.6;
  margin-bottom: 20px;
}

.social-links {
  display: flex;
  gap: 15px;
}

.social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  color: #ccc;
  text-decoration: none;
  transition: all 0.3s ease;
}

.social-link:hover {
  background-color: #4a6cf7;
  color: #fff;
  transform: translateY(-3px);
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-links li {
  margin-bottom: 10px;
}

.footer-link {
  color: #ccc;
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-link:hover {
  color: #4a6cf7;
}

.contact-info {
  list-style: none;
  padding: 0;
  margin: 0;
}

.contact-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  color: #ccc;
}

.contact-item i {
  margin-right: 10px;
  color: #4a6cf7;
}

.opening-hours {
  list-style: none;
  padding: 0;
  margin: 0;
}

.hours-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.day {
  color: #ccc;
}

.time {
  color: #4a6cf7;
  font-weight: 500;
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.copyright p {
  margin: 0;
  font-size: 14px;
}

.footer-bottom-links {
  display: flex;
  gap: 20px;
}

.bottom-link {
  color: #ccc;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.bottom-link:hover {
  color: #4a6cf7;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 30px;
  }
  
  .footer-bottom {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
}

/* 图标样式 - 使用简单的CSS创建图标 */
.icon-wechat::before {
  content: '微';
  font-family: Arial, sans-serif;
}

.icon-weibo::before {
  content: '微';
  font-family: Arial, sans-serif;
}

.icon-douyin::before {
  content: '抖';
  font-family: Arial, sans-serif;
}

.icon-location::before {
  content: '📍';
}

.icon-phone::before {
  content: '📞';
}

.icon-email::before {
  content: '✉️';
}
</style>