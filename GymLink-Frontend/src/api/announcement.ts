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
    timeRange?: number // 时间范围（天数）：7=7天内, 15=半个月内, 30=一个月内, 90=一季度内, 365=一年内
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

// 添加公告请求参数
export interface AddAnnouncementRequest {
    title: string
    content: string
}

// 更新公告请求参数
export interface UpdateAnnouncementRequest {
    id: number
    title?: string
    content?: string
}

// 添加公告 - POST /announcement/addAnnouncement
export function addAnnouncement(data: AddAnnouncementRequest): Promise<Announcement> {
    return request.post('/announcement/addAnnouncement', data) as Promise<Announcement>
}

// 更新公告 - POST /announcement/updateAnnouncement
export function updateAnnouncement(data: UpdateAnnouncementRequest): Promise<boolean> {
    return request.post('/announcement/updateAnnouncement', data) as Promise<boolean>
}

// 删除公告 - GET /announcement/deleteAnnouncement
export function deleteAnnouncement(id: number): Promise<boolean> {
    return request.get('/announcement/deleteAnnouncement', {
        params: { id }
    }) as Promise<boolean>
}

// 未读公告响应类型
export interface UnreadAnnouncementResponse {
    announcement: Announcement
    unreadCount: number
    missedCount: number
}

// 获取用户未读公告 - GET /announcement/getUnreadAnnouncement
export function getUnreadAnnouncement(userId: number): Promise<UnreadAnnouncementResponse | null> {
    return request.get('/announcement/getUnreadAnnouncement', {
        params: { userId }
    }) as Promise<UnreadAnnouncementResponse | null>
}

// 标记公告为已读 - POST /announcement/markAsRead
export function markAnnouncementAsRead(userId: number, announcementId: number): Promise<boolean> {
    return request.post('/announcement/markAsRead', null, {
        params: { userId, announcementId }
    }) as Promise<boolean>
}
