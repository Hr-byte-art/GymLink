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
            <img :src="recipe.coverImage" :alt="recipe.title" />
            <div class="recipe-category">{{ getRecipeCategory(recipe.tags) }}</div>
          </div>
          <div class="recipe-content">
            <h3 class="recipe-title">{{ recipe.title }}</h3>
            <p class="recipe-description">{{ getRecipeDescription(recipe.content) }}</p>
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
                <span>{{ (recipe.prepTime ?? 0) + (recipe.cookTime ?? 0) }} 分钟</span>
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
import { recipeTagOptions } from '@/constants/categories'

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

const getRecipeCategory = (tags?: string) => {
  if (!tags) return '未分类'
  return tags.split(',')[0] || '未分类'
}

const getRecipeDescription = (content?: string) => {
  if (!content) return '暂无描述'
  return content.length > 120 ? `${content.slice(0, 120)}...` : content
}

// 筛选条件
const selectedCategory = ref('全部')
const searchTerm = ref('')

// 根据筛选条件过滤食谱
const displayedRecipes = computed(() => {
  return recipeStore.filteredRecipes(selectedCategory.value, searchTerm.value)
})

// 获取难度类型
const getDifficultyType = (difficulty?: string) => {
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
const getDifficultyText = (difficulty?: string) => {
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
  --primary: #f97316;
  --primary-dark: #ea580c;
  --ink: #0f172a;
  --muted: #475569;
  --line: #e2e8f0;
  --surface: #ffffff;
  --soft: #f8fafc;
  min-height: calc(100vh - 140px);
  padding: 30px 0 70px;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
}

.container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  border-radius: 20px;
  padding: 42px 28px;
  margin-bottom: 24px;
  text-align: center;
  background:
    radial-gradient(circle at 10% 20%, rgba(249, 115, 22, 0.2), transparent 42%),
    linear-gradient(135deg, #0f172a 0%, #1e293b 48%, #334155 100%);
  color: #f8fafc;
}

.page-header h1 {
  margin: 0;
  font-size: clamp(32px, 5vw, 46px);
  font-weight: 800;
}

.page-header p {
  margin: 12px 0 0;
  color: #e2e8f0;
  font-size: 17px;
}

.filter-section {
  border: 1px solid var(--line);
  border-radius: 16px;
  background: var(--surface);
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  padding: 14px;
  margin-bottom: 24px;
}

.filter-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.category-filter {
  flex: 0 0 220px;
}

.search-filter {
  flex: 1;
  min-width: 240px;
}

.filter-section :deep(.el-input__wrapper),
.filter-section :deep(.el-select__wrapper) {
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  box-shadow: none;
}

.filter-section :deep(.el-input__wrapper.is-focus),
.filter-section :deep(.el-select__wrapper.is-focused) {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.16);
}

.loading-container,
.error-container,
.empty-container {
  margin: 24px 0;
}

.recipes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 18px;
}

.recipe-card {
  border: 1px solid var(--line);
  border-radius: 16px;
  overflow: hidden;
  background: var(--surface);
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  cursor: pointer;
}

.recipe-card:hover {
  transform: translateY(-4px);
  border-color: rgba(249, 115, 22, 0.42);
  box-shadow: 0 18px 32px rgba(15, 23, 42, 0.14);
}

.recipe-image {
  position: relative;
  height: 210px;
  overflow: hidden;
}

.recipe-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.25s ease;
}

.recipe-card:hover .recipe-image img {
  transform: scale(1.04);
}

.recipe-category {
  position: absolute;
  top: 12px;
  right: 12px;
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 12px;
  font-weight: 700;
  background: rgba(2, 6, 23, 0.66);
  color: #fff;
}

.recipe-content {
  padding: 18px;
}

.recipe-title {
  margin: 0 0 8px;
  color: var(--ink);
  font-size: 22px;
  font-weight: 800;
}

.recipe-description {
  margin: 0 0 12px;
  color: var(--muted);
  line-height: 1.65;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.recipe-stats {
  background: var(--soft);
  border: 1px solid var(--line);
  border-radius: 12px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  padding: 10px;
  margin-bottom: 12px;
}

.stat-item {
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
  padding: 8px;
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 4px;
  font-weight: 600;
}

.stat-value {
  color: var(--ink);
  font-size: 13px;
  font-weight: 700;
}

.recipe-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #64748b;
  font-size: 13px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

@media (max-width: 900px) {
  .recipes-grid {
    grid-template-columns: 1fr;
  }

  .filter-row {
    flex-direction: column;
  }

  .category-filter,
  .search-filter {
    width: 100%;
    flex: 1;
  }
}

@media (prefers-reduced-motion: reduce) {
  .recipe-card,
  .recipe-image img {
    transition: none;
  }

  .recipe-card:hover {
    transform: none;
  }
}
</style>
