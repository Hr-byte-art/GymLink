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

// 器材预约记录
export interface EquipmentReservation {
  id: number
  studentId: number
  equipmentId: number
  startTime: string
  endTime: string
  status: number // 1:预约成功 2:已取消 3:已完成
  createTime: string
  equipmentName?: string
  studentName?: string
}

// 器材查询参数 - 匹配后端 EquipmentQueryPageRequest
export interface EquipmentQueryParams {
  pageNum?: number
  pageSize?: number
  name?: string
  location?: string
  type?: string
  status?: number
}

// 获取器材列表 - POST /equipment/listEquipment
export function getEquipmentList(params: EquipmentQueryParams = {}): Promise<Page<Equipment>> {
  return request.post('/equipment/listEquipment', params) as Promise<Page<Equipment>>
}

// 获取器材详情 - GET /equipment/getEquipmentById
export function getEquipmentDetail(id: string | number): Promise<Equipment> {
  return request.get('/equipment/getEquipmentById', {
    params: { id: String(id) }
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

// 获取器材的预约记录 - POST /appointment/listEquipmentReservations
export interface EquipmentReservationQueryParams {
  equipmentId: number
  status?: number
  pageNum?: number
  pageSize?: number
}

export function getEquipmentReservations(params: EquipmentReservationQueryParams): Promise<Page<EquipmentReservation>> {
  return request.post('/appointment/listEquipmentReservations', params) as Promise<Page<EquipmentReservation>>
}