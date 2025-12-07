import request from '@/utils/request'
import type { Page } from './types'

export interface CoachAppointment {
    id: number
    studentId: number
    coachId: number
    appointTime: string
    message: string
    status: number
    createTime: string
    coachName: string
    studentName: string
}

export interface EquipmentReservation {
    id: number
    studentId: number
    equipmentId: number
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
    studentId: number
    status?: number
}

export interface StudentEquipmentReservationQueryRequest {
    pageNum?: number
    pageSize?: number
    studentId: number
    status?: number
}

export function listStudentCoachAppointment(params: StudentCoachAppointmentQueryRequest): Promise<Page<CoachAppointment>> {
    return request.post('/appointment/listStudentCoachAppointment', params) as Promise<Page<CoachAppointment>>
}

export function listStudentEquipmentReservation(params: StudentEquipmentReservationQueryRequest): Promise<Page<EquipmentReservation>> {
    return request.post('/appointment/listStudentEquipmentReservation', params) as Promise<Page<EquipmentReservation>>
}

export function cancelCoachAppointment(appointmentId: number): Promise<boolean> {
    return request.get('/appointment/cancelCoachAppointment', { params: { appointmentId } }) as Promise<boolean>
}

export function cancelEquipmentReservation(bookingId: number): Promise<boolean> {
    return request.get('/appointment/cancelBookingEquipment', { params: { bookingId } }) as Promise<boolean>
}
