<template>
  <AppLayout>
    <div class="container">
      <!-- 返回按钮 -->
      <div class="back-button">
        <el-button @click="goBack" :icon="ArrowLeft">返回食谱列表</el-button>
      </div>

      <!-- 加载状态 -->
      <div v-if="recipeStore.loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 错误状态 -->
      <div v-else-if="recipeStore.error" class="error-container">
        <el-alert :title="recipeStore.error" type="error" show-icon :closable="false" />
      </div>

      <!-- 食谱详情 -->
      <div v-else-if="recipe" class="recipe-detail">
        <!-- 食谱头部 -->
        <header class="recipe-header">
          <div class="recipe-image">
            <img :src="recipe.image" :alt="recipe.title" />
          </div>
          <div class="recipe-info">
            <div class="recipe-category">
              <el-tag type="primary">{{ recipe.category }}</el-tag>
            </div>
            <h1 class="recipe-title">{{ recipe.title }}</h1>
            <p class="recipe-description">{{ recipe.description }}</p>

            <!-- 基本信息卡片 -->
            <div class="info-cards">
              <div class="info-card">
                <div class="info-icon">
                  <el-icon size="24">
                    <Clock />
                  </el-icon>
                </div>
                <div class="info-content">
                  <div class="info-value">{{ recipe.prepTime + recipe.cookTime }}</div>
                  <div class="info-label">总时长(分钟)</div>
                </div>
              </div>

              <div class="info-card">
                <div class="info-icon">
                  <el-icon size="24">
                    <User />
                  </el-icon>
                </div>
                <div class="info-content">
                  <div class="info-value">{{ recipe.servings }}</div>
                  <div class="info-label">人份</div>
                </div>
              </div>

              <div class="info-card">
                <div class="info-icon">
                  <el-icon size="24">
                    <Star />
                  </el-icon>
                </div>
                <div class="info-content">
                  <div class="info-value">{{ getDifficultyText(recipe.difficulty) }}</div>
                  <div class="info-label">难度</div>
                </div>
              </div>
            </div>
          </div>
        </header>

        <!-- 营养信息 -->
        <section class="nutrition-section">
          <h2>营养成分</h2>
          <div class="nutrition-cards">
            <div class="nutrition-card">
              <div class="nutrition-value">{{ recipe.calories }}</div>
              <div class="nutrition-label">热量 (kcal)</div>
            </div>
            <div class="nutrition-card">
              <div class="nutrition-value">{{ recipe.protein }}g</div>
              <div class="nutrition-label">蛋白质</div>
            </div>
            <div class="nutrition-card">
              <div class="nutrition-value">{{ recipe.carbs }}g</div>
              <div class="nutrition-label">碳水化合物</div>
            </div>
            <div class="nutrition-card">
              <div class="nutrition-value">{{ recipe.fat }}g</div>
              <div class="nutrition-label">脂肪</div>
            </div>
            <div class="nutrition-card">
              <div class="nutrition-value">{{ recipe.fiber }}g</div>
              <div class="nutrition-label">纤维</div>
            </div>
          </div>

          <div v-if="recipe.nutritionNotes" class="nutrition-notes">
            <el-alert :title="recipe.nutritionNotes" type="info" :closable="false" show-icon />
          </div>
        </section>

        <!-- 食材清单 -->
        <section class="ingredients-section">
          <h2>食材清单</h2>
          <div class="ingredients-list">
            <div v-for="(ingredient, index) in recipe.ingredients" :key="index" class="ingredient-item">
              <div class="ingredient-checkbox">
                <el-checkbox v-model="checkedIngredients[index]" />
              </div>
              <div class="ingredient-text">{{ ingredient }}</div>
            </div>
          </div>
        </section>

        <!-- 制作步骤 -->
        <section class="instructions-section">
          <h2>制作步骤</h2>
          <div class="instructions-list">
            <div v-for="(instruction, index) in recipe.instructions" :key="index" class="instruction-item">
              <div class="instruction-number">{{ index + 1 }}</div>
              <div class="instruction-text">{{ instruction }}</div>
            </div>
          </div>
        </section>

        <!-- 小贴士 -->
        <section v-if="recipe.tips && recipe.tips.length > 0" class="tips-section">
          <h2>小贴士</h2>
          <div class="tips-list">
            <div v-for="(tip, index) in recipe.tips" :key="index" class="tip-item">
              <el-icon class="tip-icon">
                <InfoFilled />
              </el-icon>
              <span>{{ tip }}</span>
            </div>
          </div>
        </section>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useRecipeStore } from '@/stores/recipe'
import { ArrowLeft, Clock, User, Star, InfoFilled } from '@element-plus/icons-vue'
import AppLayout from '@/components/AppLayout.vue'

const route = useRoute()
const router = useRouter()
const recipeStore = useRecipeStore()

// 食材复选框状态
const checkedIngredients = ref<boolean[]>([])

// 获取当前食谱
const recipe = computed(() => recipeStore.recipeDetail)

// 监听食谱变化，初始化食材复选框状态
const initializeCheckedIngredients = () => {
  if (recipe.value && recipe.value.ingredients) {
    checkedIngredients.value = new Array(recipe.value.ingredients.length).fill(false)
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

// 返回上一页
const goBack = () => {
  router.push('/recipes')
}

// 页面加载时获取食谱详情
onMounted(() => {
  const id = Number(route.params.id)
  if (id) {
    recipeStore.fetchRecipeDetail(id)
  }
})

// 监听recipe变化
watch(recipe, () => {
  initializeCheckedIngredients()
}, { immediate: true })

// 页面卸载时清除食谱详情
onUnmounted(() => {
  recipeStore.clearRecipeDetail()
})
</script>

<style scoped>
.recipe-detail-view {
  padding: 2rem 0;
  min-height: calc(100vh - 140px);
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 1rem;
}

.back-button {
  margin-bottom: 1.5rem;
}

.loading-container,
.error-container {
  margin: 2rem 0;
}

.recipe-detail {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.recipe-header {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

@media (min-width: 768px) {
  .recipe-header {
    flex-direction: row;
  }

  .recipe-image {
    flex: 0 0 40%;
  }

  .recipe-info {
    flex: 1;
  }
}

.recipe-image {
  position: relative;
  overflow: hidden;
  border-radius: 12px;
  height: 250px;
}

.recipe-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.recipe-info {
  padding: 0 1rem;
}

.recipe-category {
  margin-bottom: 1rem;
}

.recipe-title {
  font-size: 2rem;
  margin-bottom: 1rem;
  color: #2c3e50;
}

.recipe-description {
  font-size: 1.1rem;
  color: #7f8c8d;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.info-cards {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.info-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
  flex: 1;
  min-width: 120px;
}

.info-icon {
  color: #3498db;
}

.info-value {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
}

.info-label {
  font-size: 0.875rem;
  color: #7f8c8d;
}

section {
  padding: 2rem 1rem;
  border-top: 1px solid #eee;
}

section h2 {
  font-size: 1.5rem;
  margin-bottom: 1.5rem;
  color: #2c3e50;
}

.nutrition-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.nutrition-card {
  background-color: #f8f9fa;
  padding: 1.5rem 1rem;
  border-radius: 8px;
  text-align: center;
}

.nutrition-value {
  font-size: 1.5rem;
  font-weight: 600;
  color: #3498db;
  margin-bottom: 0.5rem;
}

.nutrition-label {
  font-size: 0.875rem;
  color: #7f8c8d;
}

.nutrition-notes {
  margin-top: 1rem;
}

.ingredients-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 0.75rem;
}

.ingredient-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.ingredient-text {
  flex: 1;
}

.instructions-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.instruction-item {
  display: flex;
  gap: 1rem;
}

.instruction-number {
  flex: 0 0 36px;
  height: 36px;
  background-color: #3498db;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.instruction-text {
  flex: 1;
  line-height: 1.6;
  padding-top: 0.25rem;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem;
  background-color: #fff8e1;
  border-radius: 8px;
  line-height: 1.6;
}

.tip-icon {
  color: #f39c12;
  margin-top: 0.125rem;
}
</style>