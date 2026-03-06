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
            <img :src="recipe.coverImage" :alt="recipe.title" />
          </div>
          <div class="recipe-info">
            <div class="recipe-category">
              <el-tag type="primary">{{ getRecipeCategory(recipe.tags) }}</el-tag>
            </div>
            <h1 class="recipe-title">{{ recipe.title }}</h1>
            <p class="recipe-description">{{ getRecipeDescription(recipe.content) }}</p>

            <!-- 基本信息卡片 -->
            <div class="info-cards">
              <div class="info-card">
                <div class="info-icon">
                  <el-icon size="24">
                    <Clock />
                  </el-icon>
                </div>
                <div class="info-content">
                  <div class="info-value">{{ (recipe.prepTime ?? 0) + (recipe.cookTime ?? 0) }}</div>
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

            <!-- 收藏按钮 -->
            <div class="recipe-actions">
              <el-button size="large" @click="handleToggleFavorite">
                {{ isFavorite ? '❤️ 已收藏' : '🤍 收藏' }}
              </el-button>
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
          </div>
        </section>

        <!-- 食材清单 -->
        <section class="ingredients-section">
          <h2>食材清单</h2>
          <div class="ingredients-list">
            <div v-for="(ingredient, index) in ingredients" :key="index" class="ingredient-item">
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
            <div v-for="(instruction, index) in instructions" :key="index" class="instruction-item">
              <div class="instruction-number">{{ index + 1 }}</div>
              <div class="instruction-text">{{ instruction }}</div>
            </div>
          </div>
        </section>

        <!-- 小贴士 -->
        <section v-if="tips.length > 0" class="tips-section">
          <h2>小贴士</h2>
          <div class="tips-list">
            <div v-for="(tip, index) in tips" :key="index" class="tip-item">
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useRecipeStore } from '@/stores/recipe'
import { ArrowLeft, Clock, User, Star, InfoFilled } from '@element-plus/icons-vue'
import AppLayout from '@/components/AppLayout.vue'
import { toggleFavorite as toggleFavoriteApi, checkFavorite, FavoriteType } from '@/api/favorite'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const recipeStore = useRecipeStore()

// 食材复选框状态
const checkedIngredients = ref<boolean[]>([])

// 收藏状态
const isFavorite = ref(false)

// 获取当前食谱
const recipe = computed(() => recipeStore.recipeDetail)
const ingredients = computed(() => {
  if (!recipe.value?.content) return []
  return recipe.value.content
    .split('\n')
    .map(line => line.trim())
    .filter(Boolean)
    .slice(0, 8)
})
const instructions = computed(() => {
  if (!recipe.value?.content) return []
  return recipe.value.content
    .split('\n')
    .map(line => line.trim())
    .filter(Boolean)
    .slice(0, 10)
})
const tips = computed(() => [])

// 监听食谱变化，初始化食材复选框状态
const initializeCheckedIngredients = () => {
  checkedIngredients.value = new Array(ingredients.value.length).fill(false)
}

const getRecipeCategory = (tags?: string) => {
  if (!tags) return '未分类'
  return tags.split(',')[0] || '未分类'
}

const getRecipeDescription = (content?: string) => {
  if (!content) return '暂无描述'
  return content.length > 120 ? `${content.slice(0, 120)}...` : content
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

// 返回上一页
const goBack = () => {
  router.push('/recipes')
}

// 切换收藏状态
const handleToggleFavorite = async () => {
  try {
    const res = await toggleFavoriteApi({
      targetId: Number(route.params.id),
      type: FavoriteType.RECIPE
    })
    // request.ts 响应拦截器已解包，res 直接就是 boolean
    isFavorite.value = res as unknown as boolean
    ElMessage.success(isFavorite.value ? '已添加到收藏' : '已取消收藏')
  } catch {
    ElMessage.error('操作失败，请先登录')
  }
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  try {
    const res = await checkFavorite(Number(route.params.id), FavoriteType.RECIPE)
    isFavorite.value = res.data
  } catch {
    // 未登录时忽略错误
  }
}

// 页面加载时获取食谱详情
onMounted(() => {
  const id = route.params.id as string
  if (id) {
    recipeStore.fetchRecipeDetail(id)
  }
  checkFavoriteStatus()
})

// 监听recipe变化
watch(recipe, () => {
  initializeCheckedIngredients()
}, { immediate: true })

</script>

<style scoped>
.container{max-width:1120px;margin:0 auto;padding:22px 20px 34px;}
.back-button{margin-bottom:16px;}
.loading-container,.error-container{margin:18px 0;}
.recipe-detail{border-radius:22px;border:1px solid #f4e8da;background:#fff;box-shadow:0 10px 24px rgba(248,146,43,.06);overflow:hidden;}
.recipe-header{display:grid;grid-template-columns:minmax(0,1fr);gap:18px;padding:20px;background:linear-gradient(170deg,#fff 0%,#fff9f3 100%);}
@media (min-width:900px){.recipe-header{grid-template-columns:46% 54%;gap:24px;padding:24px;}}
.recipe-image{border-radius:16px;overflow:hidden;height:300px;}
.recipe-image img{width:100%;height:100%;object-fit:cover;}
.recipe-info{display:flex;flex-direction:column;}
.recipe-category{margin-bottom:12px;}
.recipe-title{margin:0 0 12px;font-size:34px;line-height:1.2;color:#2a1f12;}
.recipe-description{margin:0 0 16px;color:#5c4532;line-height:1.75;}
.info-cards{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:10px;}
.info-card{display:flex;align-items:center;gap:8px;border:1px solid #f4e8da;border-radius:12px;background:#fff;padding:10px;}
.info-icon{color:#ea580c;}
.info-value{font-size:17px;font-weight:700;color:#2a1f12;}
.info-label{color:#8f7660;font-size:12px;}
.recipe-actions{margin-top:14px;}
.recipe-actions .el-button{background:#fff7ec;border-color:#fed7aa;color:#9a3412;}
section{padding:22px;border-top:1px solid #f4e8da;}
section h2{margin:0 0 14px;font-size:24px;color:#2a1f12;}
.nutrition-cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(130px,1fr));gap:10px;margin-bottom:14px;}
.nutrition-card{text-align:center;border:1px solid #f4e8da;border-radius:12px;background:#fffaf5;padding:14px 8px;}
.nutrition-value{color:#ea580c;font-size:24px;font-weight:800;margin-bottom:4px;}
.nutrition-label{font-size:12px;color:#8f7660;}
.nutrition-notes{margin-top:12px;}
.ingredients-list{display:grid;grid-template-columns:repeat(auto-fit,minmax(250px,1fr));gap:8px;}
.ingredient-item{display:flex;align-items:center;gap:8px;border:1px solid #f4e8da;border-radius:10px;background:#fffaf5;padding:10px;}
.ingredient-text{color:#5c4532;}
.instructions-list{display:grid;gap:10px;}
.instruction-item{display:flex;gap:10px;}
.instruction-number{width:34px;height:34px;border-radius:50%;display:inline-flex;align-items:center;justify-content:center;font-weight:700;color:#fff;background:linear-gradient(135deg,#f97316 0%,#fb923c 100%);flex-shrink:0;}
.instruction-text{color:#5c4532;line-height:1.8;padding-top:4px;}
.tips-list{display:grid;gap:8px;}
.tip-item{display:flex;align-items:flex-start;gap:8px;border-radius:10px;border:1px solid #fde3bf;background:#fff4e3;padding:10px;line-height:1.7;color:#5c4532;}
.tip-icon{color:#ea580c;margin-top:2px;}
@media (max-width:768px){.container{padding:18px 14px 28px;}.recipe-header{padding:14px;}.recipe-image{height:230px;}.recipe-title{font-size:28px;}.info-cards{grid-template-columns:1fr;}section{padding:16px 14px;}}
</style>
