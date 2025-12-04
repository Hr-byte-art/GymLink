<template>
  <AppLayout>
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">健身器材</h1>
        <p class="page-subtitle">探索我们专业的健身器材，提升您的健身体验</p>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 器材筛选区域 -->
      <section class="filter-section">
        <div class="filter-container">
          <div class="filter-tabs">
            <div class="filter-tab" :class="{ active: activeCategory === 'all' }" @click="setActiveCategory('all')">
              全部器材
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === 'cardio' }"
              @click="setActiveCategory('cardio')">
              有氧器材
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === 'strength' }"
              @click="setActiveCategory('strength')">
              力量器材
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === 'free_weights' }"
              @click="setActiveCategory('free_weights')">
              自由重量
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === 'functional' }"
              @click="setActiveCategory('functional')">
              功能训练
            </div>
            <div class="filter-tab" :class="{ active: activeCategory === 'accessories' }"
              @click="setActiveCategory('accessories')">
              配件器材
            </div>
          </div>

          <div class="filter-options">
            <div class="status-filter">
              <span class="filter-label">状态：</span>
              <el-select v-model="selectedStatus" placeholder="选择状态" clearable>
                <el-option label="可用" value="available"></el-option>
                <el-option label="使用中" value="in_use"></el-option>
                <el-option label="维护中" value="maintenance"></el-option>
              </el-select>
            </div>

            <div class="location-filter">
              <span class="filter-label">位置：</span>
              <el-select v-model="selectedLocation" placeholder="选择位置" clearable>
                <el-option label="一楼有氧区" value="一楼有氧区"></el-option>
                <el-option label="二楼力量区" value="二楼力量区"></el-option>
                <el-option label="三楼功能训练区" value="三楼功能训练区"></el-option>
                <el-option label="四楼瑜伽区" value="四楼瑜伽区"></el-option>
              </el-select>
            </div>

            <div class="brand-filter">
              <span class="filter-label">品牌：</span>
              <el-select v-model="selectedBrand" placeholder="选择品牌" clearable>
                <el-option label="Life Fitness" value="Life Fitness"></el-option>
                <el-option label="Technogym" value="Technogym"></el-option>
                <el-option label="Hammer Strength" value="Hammer Strength"></el-option>
                <el-option label="Rogue" value="Rogue"></el-option>
                <el-option label="Concept2" value="Concept2"></el-option>
              </el-select>
            </div>

            <div class="search-box">
              <el-input v-model="searchKeyword" placeholder="搜索器材名称或型号" prefix-icon="Search" clearable></el-input>
            </div>
          </div>
        </div>
      </section>

      <!-- 加载状态 -->
      <div v-if="equipmentStore.loading" class="loading-container">
        <el-loading :loading="true" text="加载中..."></el-loading>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="equipmentStore.error" class="error-container">
        <el-result icon="warning" title="加载失败" :sub-title="equipmentStore.error">
          <template #extra>
            <el-button type="primary" @click="loadEquipment">重新加载</el-button>
          </template>
        </el-result>
      </div>

      <!-- 器材列表区域 -->
      <section v-else class="equipment-section">
        <div class="section-container">
          <!-- 无数据状态 -->
          <div v-if="!equipmentStore.hasEquipment" class="empty-container">
            <el-empty description="暂无器材数据"></el-empty>
          </div>

          <!-- 器材列表 -->
          <div v-else class="equipment-grid">
            <div class="equipment-card" v-for="equipment in equipmentStore.equipmentList" :key="equipment.id">
              <div class="equipment-image">
                <img :src="equipment.image" :alt="equipment.name" />
                <div class="equipment-category">{{ equipment.category }}</div>
                <div class="equipment-status" :class="equipment.status">
                  {{ getStatusText(equipment.status) }}
                </div>
              </div>
              <div class="equipment-content">
                <h3 class="equipment-title">{{ equipment.name }}</h3>
                <div class="equipment-brand">
                  <span class="brand-label">品牌：</span>
                  <span class="brand-name">{{ equipment.brand }}</span>
                  <span class="model-name">{{ equipment.model }}</span>
                </div>
                <p class="equipment-description">{{ equipment.description }}</p>
                <div class="equipment-info">
                  <div class="info-item">
                    <i class="icon-location"></i>
                    <span>{{ equipment.location }}</span>
                  </div>
                  <div class="info-item" v-if="equipment.rating">
                    <i class="icon-rating"></i>
                    <span>{{ equipment.rating.toFixed(1) }}</span>
                    <span class="review-count">({{ equipment.reviewCount || 0 }})</span>
                  </div>
                  <div class="info-item" v-if="equipment.usageCount">
                    <i class="icon-usage"></i>
                    <span>使用 {{ equipment.usageCount }} 次</span>
                  </div>
                </div>
                <div class="equipment-footer">
                  <div class="equipment-price" v-if="equipment.price">
                    <span class="price-label">预约费</span>
                    <span class="price-value">¥{{ equipment.price }}</span>
                    <span class="price-unit">/小时</span>
                  </div>
                  <div class="equipment-actions">
                    <el-button size="small" @click="viewEquipmentDetail(equipment.id)">查看详情</el-button>
                    <el-button type="primary" size="small" :disabled="equipment.status !== 'available'"
                      @click="reserveEquipment(equipment.id)">
                      预约器材
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="equipmentStore.hasEquipment" class="pagination-container">
            <el-pagination background layout="prev, pager, next" :total="equipmentStore.total" :page-size="pageSize"
              :current-page="currentPage" @current-change="handlePageChange">
            </el-pagination>
          </div>
        </div>
      </section>
    </main>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useEquipmentStore } from '@/stores/equipment'
import { useRouter } from 'vue-router'
import AppLayout from '@/components/AppLayout.vue'
import { ElMessage } from 'element-plus'

// 使用器材状态管理
const equipmentStore = useEquipmentStore()
const router = useRouter()

// 筛选状态
const activeCategory = ref('all')
const selectedStatus = ref('')
const selectedLocation = ref('')
const selectedBrand = ref('')
const searchKeyword = ref('')

// 分页状态
const currentPage = ref(1)
const pageSize = ref(12)

// 设置活动类别
const setActiveCategory = (category: string) => {
  activeCategory.value = category
  currentPage.value = 1
}

// 处理分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: { [key: string]: string } = {
    'available': '可用',
    'in_use': '使用中',
    'maintenance': '维护中'
  }
  return statusMap[status] || status
}

// 查看器材详情
const viewEquipmentDetail = (id: number) => {
  router.push(`/equipment/${id}`)
}

// 预约器材
const reserveEquipment = (id: number) => {
  // 这里可以打开预约对话框或跳转到预约页面
  ElMessage.info('预约功能开发中，敬请期待')
}

// 加载器材数据
const loadEquipment = () => {
  // 构建查询参数
  const params: any = {
    page: currentPage.value,
    pageSize: pageSize.value
  }

  // 添加类别筛选
  if (activeCategory.value !== 'all') {
    const categoryMap: { [key: string]: string } = {
      'cardio': '有氧器材',
      'strength': '力量器材',
      'free_weights': '自由重量',
      'functional': '功能训练',
      'accessories': '配件器材'
    }
    params.category = categoryMap[activeCategory.value]
  }

  // 添加状态筛选
  if (selectedStatus.value) {
    params.status = selectedStatus.value
  }

  // 添加位置筛选
  if (selectedLocation.value) {
    params.location = selectedLocation.value
  }

  // 添加品牌筛选
  if (selectedBrand.value) {
    params.brand = selectedBrand.value
  }

  // 添加关键词搜索
  if (searchKeyword.value) {
    params.keyword = searchKeyword.value
  }

  // 调用API获取器材数据
  equipmentStore.fetchEquipmentList(params)
}

// 监听筛选条件变化，重新加载数据
watch([activeCategory, selectedStatus, selectedLocation, selectedBrand, searchKeyword], () => {
  currentPage.value = 1
  loadEquipment()
})

// 监听页码变化，重新加载数据
watch(currentPage, () => {
  loadEquipment()
})

// 组件挂载时加载数据
onMounted(() => {
  loadEquipment()
})
</script>

<style scoped>
.equipment-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 页面头部样式 */
.page-header {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
  padding: 80px 0 60px;
  text-align: center;
}

.header-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 20px;
}

.page-subtitle {
  font-size: 18px;
  line-height: 1.6;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto;
}

/* 主要内容样式 */
.main-content {
  flex: 1;
}

/* 筛选区域样式 */
.filter-section {
  background: #f8f9fa;
  padding: 30px 0;
  border-bottom: 1px solid #e9ecef;
}

.filter-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.filter-tabs {
  display: flex;
  margin-bottom: 20px;
  overflow-x: auto;
  padding-bottom: 10px;
}

.filter-tab {
  padding: 10px 20px;
  margin-right: 10px;
  border-radius: 30px;
  background: white;
  color: #333;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-tab:hover {
  background: #f0f2f5;
}

.filter-tab.active {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}

.status-filter,
.location-filter,
.brand-filter {
  display: flex;
  align-items: center;
}

.filter-label {
  margin-right: 8px;
  font-weight: 500;
  color: #555;
}

.search-box {
  flex: 1;
  min-width: 250px;
}

/* 加载和错误状态样式 */
.loading-container,
.error-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

/* 器材区域样式 */
.equipment-section {
  padding: 60px 0;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 30px;
  margin-bottom: 50px;
}

.equipment-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.equipment-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.equipment-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.equipment-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.equipment-card:hover .equipment-image img {
  transform: scale(1.05);
}

.equipment-category {
  position: absolute;
  top: 15px;
  left: 15px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.equipment-status {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.equipment-status.available {
  background: #67c23a;
  color: white;
}

.equipment-status.in_use {
  background: #e6a23c;
  color: white;
}

.equipment-status.maintenance {
  background: #f56c6c;
  color: white;
}

.equipment-content {
  padding: 25px;
}

.equipment-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 15px;
}

.equipment-brand {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.brand-label {
  font-size: 14px;
  color: #666;
  margin-right: 5px;
}

.brand-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-right: 10px;
}

.model-name {
  font-size: 14px;
  color: #666;
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.equipment-description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.equipment-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #666;
}

.info-item i {
  margin-right: 8px;
  color: #67c23a;
}

.review-count {
  color: #999;
  margin-left: 5px;
}

.equipment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.equipment-price {
  display: flex;
  align-items: baseline;
}

.price-label {
  font-size: 14px;
  color: #666;
  margin-right: 5px;
}

.price-value {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.price-unit {
  font-size: 14px;
  color: #666;
  margin-left: 2px;
}

.equipment-actions {
  display: flex;
  gap: 10px;
}

/* 分页样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

/* 图标样式 */
.icon-location::before {
  content: '📍';
}

.icon-rating::before {
  content: '⭐';
}

.icon-usage::before {
  content: '📊';
}

/* 响应式设计 */
@media (max-width: 992px) {
  .equipment-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 32px;
  }

  .filter-tabs {
    flex-wrap: nowrap;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .filter-options {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    min-width: auto;
  }

  .equipment-grid {
    grid-template-columns: 1fr;
  }

  .equipment-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .equipment-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>