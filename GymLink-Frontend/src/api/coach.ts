import request from '@/utils/request'
import type { Page } from './types'

// 教练接口类型
export interface Coach {
  id: number | string // 支持大数字转字符串
  username: string
  name: string
  gender: number // 1:男 2:女
  phone: string
  avatar: string
  age: number
  specialty: string
  intro: string
  createTime: string
  price: number // 预约价格
}

// 教练查询参数
export interface CoachQueryParams {
  current?: number
  pageSize?: number
  name?: string
  specialty?: string
  gender?: number
  sortField?: string
  sortOrder?: string
}

// 获取教练列表
export function getCoachList(params: CoachQueryParams = {}): Promise<Page<Coach>> {
  return request.post('/coach/ListCoach', params) as Promise<Page<Coach>>
}

// 获取教练详情
export function getCoachDetail(id: string | number): Promise<Coach> {
  return request.get('/coach/getCoachById', {
    params: { id: String(id) }
  }) as Promise<Coach>
}

// 提交教练预约
export interface BookCoachRequest {
  coachId: string | number
  studentId: string | number
  appointTime: string  // 预约开始时间
  endTime: string      // 预约结束时间
  message?: string     // 备注信息
}

export function bookCoach(data: BookCoachRequest): Promise<boolean> {
  return request.post('/appointment/bookingCoach', {
    ...data,
    coachId: String(data.coachId),
    studentId: String(data.studentId)
  }) as Promise<boolean>
}

// 取消教练预约
export function cancelCoachBooking(appointmentId: number): Promise<boolean> {
  return request.get('/appointment/cancelCoachAppointment', {
    params: { appointmentId }
  }) as Promise<boolean>
}

// 根据用户ID获取教练信息
export function getCoachByUserId(userId: string | number): Promise<Coach> {
  return request.get('/coach/getCoachByUserId', {
    params: { userId: String(userId) }
  }) as Promise<Coach>
}

// 更新教练信息请求参数
export interface UpdateCoachRequest {
  name?: string
  gender?: number
  phone?: string
  avatar?: string
  age?: number
  specialty?: string
  intro?: string
}

// 更新教练信息
export function updateCoach(id: string | number, data: UpdateCoachRequest): Promise<boolean> {
  console.log('updateCoach - id:', id, 'data:', data)
  return request.post(`/coach/updateCoach?id=${id}`, data) as Promise<boolean>
}


// 上传教练头像
export function uploadCoachAvatar(coachId: string | number, file: File): Promise<string> {
  const formData = new FormData()
  formData.append('avatar', file)
  formData.append('coachId', String(coachId))
  return request.post('/coach/updateCoachAvatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }) as Promise<string>
}
