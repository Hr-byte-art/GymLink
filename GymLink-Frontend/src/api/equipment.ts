import request from '@/utils/request'
import type { Page } from './types'

// 健身器材接口类型
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

// 器材查询参数
export interface EquipmentQueryParams {
  pageNum?: number
  pageSize?: number
  name?: string
  location?: string
  type?: string
  status?: number
}

// 获取器材列表
export function getEquipmentList(params: EquipmentQueryParams = {}): Promise<Page<Equipment>> {
  return request.post('/equipment/listEquipment', params) as Promise<Page<Equipment>>
}

// 获取器材详情
export function getEquipmentDetail(id: string | number): Promise<Equipment> {
  return request.get('/equipment/getEquipmentById', {
    params: { id: String(id) }
  }) as Promise<Equipment>
}

// 提交器材预约
export interface BookEquipmentRequest {
  equipmentId: number
  studentId: number
  startTime: string
  endTime: string
}

export function reserveEquipment(data: BookEquipmentRequest): Promise<boolean> {
  return request.post('/appointment/bookingEquipment', data) as Promise<boolean>
}

// 取消器材预约
export function cancelReservation(bookingId: number): Promise<boolean> {
  return request.get('/appointment/cancelBookingEquipment', {
    params: { bookingId }
  }) as Promise<boolean>
}

// 获取器材预约记录
export interface EquipmentReservationQueryParams {
  equipmentId: number
  status?: number
  pageNum?: number
  pageSize?: number
}

export function getEquipmentReservations(params: EquipmentReservationQueryParams): Promise<Page<EquipmentReservation>> {
  return request.post('/appointment/listEquipmentReservations', params) as Promise<Page<EquipmentReservation>>
}
