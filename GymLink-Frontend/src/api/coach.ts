import request from '@/utils/request'
import type { Page } from './types'

// 教练接口类型 - 匹配后端 CoachVo
export interface Coach {
  id: number
  username: string
  name: string
  gender: number // 1:男 2:女
  phone: string
  avatar: string
  age: number
  specialty: string
  intro: string
  createTime: string
}

// 教练查询参数 - 匹配后端 CoachQueryPageRequest
export interface CoachQueryParams {
  current?: number
  pageSize?: number
  name?: string
  specialty?: string
  gender?: number
  sortField?: string
  sortOrder?: string
}

// 获取教练列表 - POST /coach/ListCoach
export function getCoachList(params: CoachQueryParams = {}): Promise<Page<Coach>> {
  return request.post('/coach/ListCoach', params) as Promise<Page<Coach>>
}

// 获取教练详情 - GET /coach/getCoachById
export function getCoachDetail(id: number): Promise<Coach> {
  return request.get('/coach/getCoachById', {
    params: { id }
  }) as Promise<Coach>
}

// 预约教练 - POST /appointment/bookingCoach
export interface BookCoachRequest {
  coachId: number
  studentId: number
  appointmentTime: string
  purpose: string
}

export function bookCoach(data: BookCoachRequest): Promise<boolean> {
  return request.post('/appointment/bookingCoach', data) as Promise<boolean>
}

// 取消预约教练 - GET /appointment/cancelCoachAppointment
export function cancelCoachBooking(appointmentId: number): Promise<boolean> {
  return request.get('/appointment/cancelCoachAppointment', {
    params: { appointmentId }
  }) as Promise<boolean>
}