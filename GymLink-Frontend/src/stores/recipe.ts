import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import {
  getRecipeList,
  getRecipeDetail,
  type Recipe,
  type RecipeQueryParams
} from '@/api/recipe'
import type { Page } from '@/api/types'

export const useRecipeStore = defineStore('recipe', () => {
  // 状态
  const recipes = ref<Recipe[]>([])
  const categories = ref<string[]>([
    '全部',
    '增肌食谱',
    '减脂食谱',
    '维持期食谱',
    '高蛋白食谱',
    '低碳食谱',
    '力量训练专用',
    '耐力训练专用',
    '素食健身食谱',
    '清单饮食',
    '周期化食谱'
  ])
  const recipeDetail = ref<Recipe | null>(null)
  const total = ref(0)
  const loading = ref(false)
  const detailLoading = ref(false)
  const error = ref<string | null>(null)

  // 计算属性
  const hasRecipes = computed(() => recipes.value.length > 0)
  const hasRecipeDetail = computed(() => recipeDetail.value !== null)

  // 筛选食谱
  const filteredRecipes = (category: string, searchTerm: string) => {
    let result = recipes.value

    // 过滤分类
    if (category && category !== '全部') {
      // 假设 tags 字段包含分类信息，或者我们需要扩展 Recipe 类型
      // 这里做模糊匹配，因为 tags 可能是 "增肌食谱,早餐" 这样的字符串
      result = result.filter(r => r.tags && r.tags.includes(category))
    }

    // 过滤关键词
    if (searchTerm) {
      const term = searchTerm.toLowerCase()
      result = result.filter(r =>
        (r.title && r.title.toLowerCase().includes(term)) ||
        (r.content && r.content.toLowerCase().includes(term))
      )
    }

    return result
  }

  // 获取菜谱列表
  const fetchRecipes = async (params: RecipeQueryParams = {}) => {
    loading.value = true
    error.value = null

    try {
      const response: Page<Recipe> = await getRecipeList(params)
      // 数据适配：为缺失字段添加默认值，防止前端渲染报错
      const records = response.records || []
      recipes.value = records.map(item => ({
        ...item,
        // 适配字段映射
        image: item.coverImage || 'https://via.placeholder.com/300x200?text=No+Image', // 映射 image
        description: item.content ? item.content.substring(0, 100) + '...' : '', // 简略描述
        category: item.tags ? item.tags.split(',')[0] : '未分类', // 提取第一个标签作为分类

        // 填充 Mock 数据（因为后端没返回这些字段）
        calories: Math.floor(Math.random() * 500 + 200),
        protein: Math.floor(Math.random() * 30 + 10),
        carbs: Math.floor(Math.random() * 50 + 20),
        fat: Math.floor(Math.random() * 20 + 5),
        prepTime: 10,
        cookTime: 20,
        servings: 1,
        difficulty: ['easy', 'medium', 'hard'][Math.floor(Math.random() * 3)]
      })) as unknown as Recipe[] // 强制类型转换，实际应用中应该更新类型定义

      total.value = response.total || 0
    } catch (err) {
      error.value = err instanceof Error ? err.message : '获取菜谱列表失败'
      recipes.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  // 获取菜谱详情
  const fetchRecipeDetail = async (id: number) => {
    detailLoading.value = true
    error.value = null

    try {
      recipeDetail.value = await getRecipeDetail(id)
    } catch (err) {
      error.value = err instanceof Error ? err.message : '获取菜谱详情失败'
      recipeDetail.value = null
    } finally {
      detailLoading.value = false
    }
  }

  // 重置状态
  const resetState = () => {
    recipes.value = []
    recipeDetail.value = null
    total.value = 0
    loading.value = false
    detailLoading.value = false
    error.value = null
  }

  return {
    // 状态
    recipes,
    categories,
    recipeDetail,
    total,
    loading,
    detailLoading,
    error,

    // 计算属性
    hasRecipes,
    hasRecipeDetail,

    // 方法
    filteredRecipes,
    fetchRecipes,
    fetchRecipeDetail,
    resetState
  }
})