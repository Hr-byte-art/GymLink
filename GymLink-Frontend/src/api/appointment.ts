import request from '@/utils/request'
import type { Page } from './types'

export interface CoachAppointment {
    id: number | string // 支持大数字转字符串
    studentId: number | string
    coachId: number | string
    appointTime: string
    endTime?: string
    message: string
    status: number
    createTime: string
    coachName: string
    studentName: string
}

export interface EquipmentReservation {
    id: number | string // 支持大数字转字符串
    studentId: number | string
    equipmentId: number | string
    startTime: string
    endTime: string
    status: number
    createTime: string
    equipmentName: string
    studentName: string
}

export interface StudentCoachAppointmentQueryRequest {
    pageNum?: number
    pageSize?: number
    studentId: number | string
    status?: number
}

export interface StudentEquipmentReservationQueryRequest {
    pageNum?: number
    pageSize?: number
    studentId: number | string
    status?: number
}

export function listStudentCoachAppointment(params: StudentCoachAppointmentQueryRequest): Promise<Page<CoachAppointment>> {
    return request.post('/appointment/listStudentCoachAppointment', {
        ...params,
        studentId: String(params.studentId)
    }) as Promise<Page<CoachAppointment>>
}

export function listStudentEquipmentReservation(params: StudentEquipmentReservationQueryRequest): Promise<Page<EquipmentReservation>> {
    return request.post('/appointment/listStudentEquipmentReservation', {
        ...params,
        studentId: String(params.studentId)
    }) as Promise<Page<EquipmentReservation>>
}

export function cancelCoachAppointment(appointmentId: number | string): Promise<boolean> {
    return request.get('/appointment/cancelCoachAppointment', { params: { appointmentId: String(appointmentId) } }) as Promise<boolean>
}

export function cancelEquipmentReservation(bookingId: number | string): Promise<boolean> {
    return request.get('/appointment/cancelBookingEquipment', { params: { bookingId: String(bookingId) } }) as Promise<boolean>
}

// 教练端预约管理接口
export interface CoachAppointmentSearchRequest {
    pageNum?: number
    pageSize?: number
    coachId: number | string
    status?: number
}

// 获取教练的预约列表
export function listAppointmentsByCoach(params: CoachAppointmentSearchRequest): Promise<Page<CoachAppointment>> {
    return request.post('/appointment/listAppointmentsByCoach', {
        ...params,
        coachId: String(params.coachId) // 转为字符串避免大数字精度丢失
    }) as Promise<Page<CoachAppointment>>
}

// 确认预约
export function confirmCoachAppointment(appointmentId: number | string): Promise<boolean> {
    return request.post('/appointment/confirmCoachAppointment', null, { params: { appointmentId: String(appointmentId) } }) as Promise<boolean>
}

// 拒绝预约
export function rejectCoachAppointment(appointmentId: number | string): Promise<boolean> {
    return request.post('/appointment/rejectCoachAppointment', null, { params: { appointmentId: String(appointmentId) } }) as Promise<boolean>
}
