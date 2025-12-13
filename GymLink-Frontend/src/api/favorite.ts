import request from '@/utils/request'
import axios from 'axios'

// 收藏类型枚举
export enum FavoriteType {
    EQUIPMENT = 1, // 设施
    COACH = 2, // 教练
    COURSE = 3, // 课程
    RECIPE = 4 // 食谱
}

// 收藏请求参数
export interface FavoriteRequest {
    targetId: number | string
    type: FavoriteType
}

// 收藏查询参数
export interface FavoriteQueryRequest {
    pageNum?: number
    pageSize?: number
    type?: FavoriteType
}

// 收藏信息
export interface FavoriteVo {
    id: number
    targetId: number
    type: FavoriteType
    typeName: string
    targetName: string
    targetImage: string
    targetDescription: string
    createTime: string
}

// 添加收藏
export function addFavorite(data: FavoriteRequest) {
    return request.post('/favorite/add', data)
}

// 取消收藏
export function removeFavorite(data: FavoriteRequest) {
    return request.post('/favorite/remove', data)
}

// 切换收藏状态
export function toggleFavorite(data: FavoriteRequest) {
    // 确保 targetId 以字符串形式发送，避免大数精度丢失
    return request.post<boolean>('/favorite/toggle', {
        ...data,
        targetId: String(data.targetId)
    })
}

// 检查是否已收藏（静默请求，不弹出错误提示）
export async function checkFavorite(targetId: number | string, type: FavoriteType) {
    try {
        const token = localStorage.getItem('token')
        const res = await axios.get('http://localhost:8080/api/favorite/check', {
            params: { targetId, type },
            headers: token ? { GymLink: token } : {}
        })
        return { data: res.data?.data ?? false }
    } catch {
        return { data: false }
    }
}

// 获取收藏列表
export function getFavoriteList(params: FavoriteQueryRequest): Promise<{ records: FavoriteVo[]; total: number }> {
    return request.get('/favorite/list', { params }) as Promise<{ records: FavoriteVo[]; total: number }>
}

// 获取收藏数量
export function getFavoriteCount(type?: FavoriteType) {
    return request.get<number>('/favorite/count', { params: { type } })
}
