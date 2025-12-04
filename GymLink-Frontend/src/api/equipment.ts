import request from '@/utils/request'
import type { Page } from './types'

// 健身器材接口类型 - 匹配后端 EquipmentVo
export interface Equipment {
  id: number
  name: string
  image: string
  location: string
  description: string
  status: number // 1:正常 2:损坏/维护中
  totalCount: number
  createTime: string
}

// 器材查询参数 - 匹配后端 EquipmentQueryPageRequest
export interface EquipmentQueryParams {
  current?: number
  pageSize?: number
  name?: string
  location?: string
  status?: number
  sortField?: string
  sortOrder?: string
}

// 获取器材列表 - POST /equipment/listEquipment
export function getEquipmentList(params: EquipmentQueryParams = {}): Promise<Page<Equipment>> {
  return request.post('/equipment/listEquipment', params) as Promise<Page<Equipment>>
}

// 获取器材详情 - GET /equipment/getEquipmentById
export function getEquipmentDetail(id: number): Promise<Equipment> {
  return request.get('/equipment/getEquipmentById', {
    params: { id }
  }) as Promise<Equipment>
}

// 预约器材 - POST /appointment/bookingEquipment
export interface BookEquipmentRequest {
  equipmentId: number
  studentId: number
  startTime: string
  endTime: string
}

export function reserveEquipment(data: BookEquipmentRequest): Promise<boolean> {
  return request.post('/appointment/bookingEquipment', data) as Promise<boolean>
}

// 取消器材预约 - GET /appointment/cancelBookingEquipment
export function cancelReservation(bookingId: number): Promise<boolean> {
  return request.get('/appointment/cancelBookingEquipment', {
    params: { bookingId }
  }) as Promise<boolean>
}