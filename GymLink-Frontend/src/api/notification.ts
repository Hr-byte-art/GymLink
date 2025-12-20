import request from '@/utils/request'
import type { Page } from './types'

// 通知接口类型
export interface Notification {
    id: number
    userId: number
    type: number // 1-预约通知 2-系统通知
    title: string
    content: string
    relatedId: number | null
    isRead: number // 0-未读 1-已读
    createTime: string
}

// 获取用户通知列表
export function getNotificationList(userId: number, pageNum = 1, pageSize = 10): Promise<Page<Notification>> {
    return request.get('/notification/list', {
        params: { userId, pageNum, pageSize }
    }) as Promise<Page<Notification>>
}

// 获取未读通知数量
export function getUnreadCount(userId: number): Promise<number> {
    return request.get('/notification/unreadCount', {
        params: { userId }
    }) as Promise<number>
}

// 标记通知为已读
export function markAsRead(notificationId: number): Promise<boolean> {
    return request.post('/notification/markAsRead', null, {
        params: { notificationId }
    }) as Promise<boolean>
}

// 标记所有通知为已读
export function markAllAsRead(userId: number): Promise<boolean> {
    return request.post('/notification/markAllAsRead', null, {
        params: { userId }
    }) as Promise<boolean>
}
