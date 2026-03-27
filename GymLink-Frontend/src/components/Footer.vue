<template>
  <footer class="footer">
    <div class="footer-container">
      <div class="footer-grid">
        <section class="footer-block about">
          <h3>GymLink</h3>
          <p>
            GymLink 为健身房会员提供课程预约、教练管理、器材预约和健康数据追踪，
            让训练过程更高效、更可持续。
          </p>
          <div class="social-links" aria-label="社交链接">
            <a href="#" class="social-link" aria-label="微信">微</a>
            <a href="#" class="social-link" aria-label="微博">博</a>
            <a href="#" class="social-link" aria-label="抖音">抖</a>
          </div>
        </section>

        <section class="footer-block">
          <h4>快速导航</h4>
          <ul class="link-list">
            <li><router-link to="/">首页</router-link></li>
            <li><router-link to="/courses">课程</router-link></li>
            <li><router-link to="/coaches">教练</router-link></li>
            <li><router-link to="/equipment">器材</router-link></li>
            <li><router-link to="/recipes">饮食计划</router-link></li>
          </ul>
        </section>

        <section class="footer-block">
          <h4>联系信息</h4>
          <ul class="meta-list">
            <li><span class="dot"></span>成都市高新西区百叶路1号</li>
            <li><span class="dot"></span>188-8888-8888</li>
            <li><span class="dot"></span>liderun@gmail.com</li>
          </ul>
        </section>

        <section class="footer-block">
          <h4>营业时间</h4>
          <ul class="meta-list">
            <li><span>周一至周五</span><b>06:00 - 22:00</b></li>
            <li><span>周六至周日</span><b>07:00 - 21:00</b></li>
            <li><span>节假日</span><b>08:00 - 20:00</b></li>
          </ul>
        </section>
      </div>

      <div class="footer-bottom">
        <p>© {{ currentYear }} GymLink. 保留所有权利。</p>
        <div class="bottom-links">
          <a href="#">隐私政策</a>
          <a href="#">使用条款</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'

defineOptions({
  name: 'AppFooter'
})

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.footer {
  margin-top: 40px;
  background:
    radial-gradient(circle at 10% 10%, rgba(249, 115, 22, 0.2), transparent 34%),
    linear-gradient(135deg, #0f172a 0%, #1e293b 48%, #334155 100%);
  color: #cbd5e1;
  border-top: 1px solid rgba(148, 163, 184, 0.2);
}

.footer-container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 46px 20px 18px;
}

.footer-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr 1fr;
  gap: 26px;
}

.footer-block h3,
.footer-block h4 {
  margin: 0 0 14px;
  color: #f8fafc;
}

.footer-block h3 {
  font-size: 28px;
  font-weight: 800;
}

.footer-block h4 {
  font-size: 16px;
  font-weight: 700;
}

.about p {
  margin: 0 0 14px;
  line-height: 1.75;
  color: #cbd5e1;
}

.social-links {
  display: flex;
  gap: 8px;
}

.social-link {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  color: #f8fafc;
  font-size: 12px;
  font-weight: 700;
  border: 1px solid rgba(203, 213, 225, 0.35);
  background: rgba(15, 23, 42, 0.45);
  transition: all 0.2s ease;
}

.social-link:hover {
  border-color: #fdba74;
  background: rgba(249, 115, 22, 0.2);
}

.link-list,
.meta-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 9px;
}

.link-list a {
  color: #cbd5e1;
  text-decoration: none;
  transition: color 0.2s ease;
}

.link-list a:hover {
  color: #fb923c;
}

.meta-list li {
  color: #cbd5e1;
  display: flex;
  justify-content: space-between;
  gap: 10px;
  line-height: 1.6;
}

.meta-list li .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-top: 8px;
  background: #fb923c;
  flex-shrink: 0;
}

.meta-list b {
  color: #f8fafc;
  font-weight: 700;
}

.footer-bottom {
  margin-top: 24px;
  padding-top: 14px;
  border-top: 1px solid rgba(148, 163, 184, 0.25);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.footer-bottom p {
  margin: 0;
  color: #94a3b8;
  font-size: 13px;
}

.bottom-links {
  display: flex;
  gap: 12px;
}

.bottom-links a {
  color: #94a3b8;
  text-decoration: none;
  font-size: 13px;
}

.bottom-links a:hover {
  color: #fb923c;
}

@media (max-width: 980px) {
  .footer-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .footer-grid {
    grid-template-columns: 1fr;
  }

  .footer-bottom {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
