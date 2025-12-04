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
export function getRecipeDetail(id: number): Promise<Recipe> {
    return request.get('/recipe/getRecipeById', {
        params: { id }
    }) as Promise<Recipe>
}
