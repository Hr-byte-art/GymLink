import request from '@/utils/request'
import type { Page } from './types'

export interface CoachAppointment {
  id: number | string
  studentId: number | string
  coachId: number | string
  courseId?: number | string
  courseName?: string
  appointTime: string
  endTime?: string
  message: string
  status: number
  createTime: string
  coachName: string
  studentName: string
}

export interface EquipmentReservation {
  id: number | string
  studentId: number | string
  equipmentId: number | string
  startTime: string
  endTime: string
  status: number
  createTime: string
  equipmentName: string
  studentName: string
}

export interface CourseBooking {
  id: number | string
  studentId: number | string
  courseId: number | string
  courseName?: string
  coachId: number | string
  coachName?: string
  scheduleId: number | string
  orderId?: number | string
  scheduleStartTime?: string
  scheduleEndTime?: string
  status: number
  createTime: string
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

export interface StudentCourseBookingQueryRequest {
  pageNum?: number
  pageSize?: number
  studentId: number | string
  status?: number
}

export interface CoachAppointmentSearchRequest {
  pageNum?: number
  pageSize?: number
  coachId: number | string
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

export function listStudentCourseBooking(params: StudentCourseBookingQueryRequest): Promise<Page<CourseBooking>> {
  return request.post('/appointment/listStudentCourseBooking', {
    ...params,
    studentId: String(params.studentId)
  }) as Promise<Page<CourseBooking>>
}

export function cancelCoachAppointment(appointmentId: number | string): Promise<boolean> {
  return request.get('/appointment/cancelCoachAppointment', {
    params: { appointmentId: String(appointmentId) }
  }) as Promise<boolean>
}

export function cancelEquipmentReservation(bookingId: number | string): Promise<boolean> {
  return request.get('/appointment/cancelBookingEquipment', {
    params: { bookingId: String(bookingId) }
  }) as Promise<boolean>
}

export function cancelCourseBooking(bookingId: number | string): Promise<boolean> {
  return request.get('/appointment/cancelCourseBooking', {
    params: { bookingId: String(bookingId) }
  }) as Promise<boolean>
}

export function bookCourseSchedule(studentId: number | string, scheduleId: number | string): Promise<boolean> {
  return request.post('/appointment/bookCourseSchedule', {
    studentId: String(studentId),
    scheduleId: String(scheduleId)
  }) as Promise<boolean>
}

export function listAppointmentsByCoach(params: CoachAppointmentSearchRequest): Promise<Page<CoachAppointment>> {
  return request.post('/appointment/listAppointmentsByCoach', {
    ...params,
    coachId: String(params.coachId)
  }) as Promise<Page<CoachAppointment>>
}

export function confirmCoachAppointment(appointmentId: number | string): Promise<boolean> {
  return request.post('/appointment/confirmCoachAppointment', null, {
    params: { appointmentId: String(appointmentId) }
  }) as Promise<boolean>
}

export function rejectCoachAppointment(appointmentId: number | string): Promise<boolean> {
  return request.post('/appointment/rejectCoachAppointment', null, {
    params: { appointmentId: String(appointmentId) }
  }) as Promise<boolean>
}
