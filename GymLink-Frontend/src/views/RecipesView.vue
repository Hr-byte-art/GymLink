<template>
  <AppLayout>
    <div class="container">
      <header class="page-header">
        <h1>健身食谱</h1>
        <p>科学营养搭配，助力您的健身目标</p>
      </header>

      <!-- 筛选和搜索区域 -->
      <div class="filter-section">
        <div class="filter-row">
          <div class="category-filter">
            <el-select v-model="selectedCategory" placeholder="选择分类" clearable>
              <el-option v-for="category in recipeStore.categories" :key="category" :label="category" :value="category">
                <el-tooltip v-if="category !== '全部'" :content="getCategoryDescription(category)" placement="right">
                  <div style="display: flex; justify-content: space-between; width: 100%;">
                    <span>{{ category }}</span>
                  </div>
                </el-tooltip>
                <span v-else>{{ category }}</span>
              </el-option>
            </el-select>
          </div>
          <div class="search-filter">
            <el-input v-model="searchTerm" placeholder="搜索食谱名称或描述" prefix-icon="Search" clearable />
          </div>
        </div>
      </div>

      <!-- 食谱列表 -->
      <div v-if="recipeStore.loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="recipeStore.error" class="error-container">
        <el-alert :title="recipeStore.error" type="error" show-icon :closable="false" />
      </div>

      <div v-else-if="displayedRecipes.length === 0" class="empty-container">
        <el-empty description="暂无食谱数据" />
      </div>

      <div v-else class="recipes-grid">
        <div v-for="recipe in displayedRecipes" :key="recipe.id" class="recipe-card"
          @click="navigateToDetail(recipe.id)">
          <div class="recipe-image">
            <img :src="recipe.image" :alt="recipe.title" />
            <div class="recipe-category">{{ recipe.category }}</div>
          </div>
          <div class="recipe-content">
            <h3 class="recipe-title">{{ recipe.title }}</h3>
            <p class="recipe-description">{{ recipe.description }}</p>
            <div class="recipe-stats">
              <div class="stat-item">
                <span class="stat-label">热量</span>
                <span class="stat-value">{{ recipe.calories }} kcal</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">蛋白质</span>
                <span class="stat-value">{{ recipe.protein }}g</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">碳水</span>
                <span class="stat-value">{{ recipe.carbs }}g</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">脂肪</span>
                <span class="stat-value">{{ recipe.fat }}g</span>
              </div>
            </div>
            <div class="recipe-meta">
              <div class="meta-item">
                <el-icon>
                  <Clock />
                </el-icon>
                <span>{{ recipe.prepTime + recipe.cookTime }} 分钟</span>
              </div>
              <div class="meta-item">
                <el-icon>
                  <User />
                </el-icon>
                <span>{{ recipe.servings }} 人份</span>
              </div>
              <div class="meta-item">
                <el-tag :type="getDifficultyType(recipe.difficulty)" size="small">
                  {{ getDifficultyText(recipe.difficulty) }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useRecipeStore } from '@/stores/recipe'
import { Clock, User } from '@element-plus/icons-vue'
import AppLayout from '@/components/AppLayout.vue'
import { recipeTagOptions, getRecipeTagName } from '@/constants/categories'

const router = useRouter()
const recipeStore = useRecipeStore()

// 从统一常量获取分类描述
const categoryDescriptions: Record<string, string> = Object.fromEntries(
  recipeTagOptions.map(item => [item.value, item.description])
)

// 获取分类描述
const getCategoryDescription = (category: string) => {
  return categoryDescriptions[category] || ''
}

// 筛选条件
const selectedCategory = ref('全部')
const searchTerm = ref('')

// 根据筛选条件过滤食谱
const displayedRecipes = computed(() => {
  return recipeStore.filteredRecipes(selectedCategory.value, searchTerm.value)
})

// 获取难度类型
const getDifficultyType = (difficulty: string) => {
  switch (difficulty) {
    case 'easy':
      return 'success'
    case 'medium':
      return 'warning'
    case 'hard':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取难度文本
const getDifficultyText = (difficulty: string) => {
  switch (difficulty) {
    case 'easy':
      return '简单'
    case 'medium':
      return '中等'
    case 'hard':
      return '困难'
    default:
      return '未知'
  }
}

// 导航到详情页
const navigateToDetail = (id: number) => {
  router.push(`/recipes/${id}`)
}

// 页面加载时获取食谱列表
onMounted(() => {
  recipeStore.fetchRecipes()
})
</script>

<style scoped>
.recipes-view {
  padding: 2rem 0;
  min-height: calc(100vh - 140px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.page-header p {
  font-size: 1.1rem;
  color: #7f8c8d;
}

.filter-section {
  margin-bottom: 2rem;
  background-color: #f8f9fa;
  padding: 1.5rem;
  border-radius: 8px;
}

.filter-row {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.category-filter {
  flex: 0 0 250px;
}

.search-filter {
  flex: 1;
  min-width: 250px;
}

.loading-container,
.error-container,
.empty-container {
  margin: 2rem 0;
}

.recipes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 2rem;
}

.recipe-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.recipe-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.recipe-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.recipe-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.recipe-card:hover .recipe-image img {
  transform: scale(1.05);
}

.recipe-category {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
}

.recipe-content {
  padding: 1.5rem;
}

.recipe-title {
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.recipe-description {
  color: #7f8c8d;
  margin-bottom: 1rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.recipe-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
  padding: 0.75rem 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 0.75rem;
  color: #95a5a6;
  margin-bottom: 0.25rem;
}

.stat-value {
  font-weight: 600;
  color: #2c3e50;
}

.recipe-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 0.875rem;
  color: #7f8c8d;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

@media (max-width: 768px) {
  .recipes-grid {
    grid-template-columns: 1fr;
  }

  .filter-row {
    flex-direction: column;
  }

  .category-filter,
  .search-filter {
    flex: 1;
    width: 100%;
  }
}
</style>