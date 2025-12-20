import request from '@/utils/request'
import type { Page } from './types'

// 菜谱接口类型 - 匹配后端 RecipeVo
export interface Recipe {
    id: number
    title: string
    coverImage: string
    content: string
    tags: string
    adminId: number
    createTime: string
    // 营养分析字段（AI 分析生成）
    calories?: number      // 热量(千卡)
    protein?: number       // 蛋白质(克)
    carbs?: number         // 碳水化合物(克)
    fat?: number           // 脂肪(克)
    prepTime?: number      // 准备时间(分钟)
    cookTime?: number      // 烹饪时间(分钟)
    servings?: number      // 份数
    difficulty?: string    // 难度(easy/medium/hard)
}

// 菜谱查询参数 - 匹配后端 RecipeQueryPageRequest
export interface RecipeQueryParams {
    current?: number
    pageSize?: number
    title?: string
    tags?: string
    sortField?: string
    sortOrder?: string
}

// 获取菜谱列表 - POST /recipe/listRecipe
export function getRecipeList(params: RecipeQueryParams = {}): Promise<Page<Recipe>> {
    return request.post('/recipe/listRecipe', params) as Promise<Page<Recipe>>
}

// 获取菜谱详情 - GET /recipe/getRecipeById
export function getRecipeDetail(id: string | number): Promise<Recipe> {
    return request.get('/recipe/getRecipeById', {
        params: { id: String(id) }
    }) as Promise<Recipe>
}
