import request from '@/utils/request'
import type { Page } from './types'

// 公告接口类型 - 匹配后端 AnnouncementVo
export interface Announcement {
    id: number
    title: string
    content: string
    adminId: number
    createTime: string
}

// 公告查询参数 - 匹配后端 AnnouncementQueryPageRequest
export interface AnnouncementQueryParams {
    current?: number
    pageSize?: number
    title?: string
    sortField?: string
    sortOrder?: string
}

// 获取公告列表 - POST /announcement/listAnnouncement
export function getAnnouncementList(params: AnnouncementQueryParams = {}): Promise<Page<Announcement>> {
    return request.post('/announcement/listAnnouncement', params) as Promise<Page<Announcement>>
}

// 获取公告详情 - GET /announcement/getAnnouncementById
export function getAnnouncementDetail(id: number): Promise<Announcement> {
    return request.get('/announcement/getAnnouncementById', {
        params: { announcementId: id }
    }) as Promise<Announcement>
}
