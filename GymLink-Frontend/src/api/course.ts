import request from '@/utils/request'
import type { Page } from './types'

export interface Course {
  id: number
  name: string
  coachId: number
  coachName?: string
  coachAvatar?: string
  image: string
  description: string
  price: number
  duration: number
  difficulty: string
  type: string
  deliveryMode?: number
  totalSessions?: number
  createTime: string
}

export interface CourseQueryParams {
  pageNum?: number
  current?: number
  pageSize?: number
  name?: string
  difficulty?: string
  type?: string
  deliveryMode?: number
  coachId?: number | string
  sortField?: string
  sortOrder?: string
}

export interface CourseSchedule {
  id: number
  courseId: number
  courseName?: string
  coachId: number
  coachName?: string
  startTime: string
  endTime: string
  capacity: number
  bookedCount: number
  remainingCapacity?: number
  status: number
  createTime: string
}

export interface CourseScheduleQueryParams {
  pageNum?: number
  pageSize?: number
  courseId?: number | string
  status?: number
}

export interface AddCourseScheduleRequest {
  courseId: number | string
  startTime: string
  endTime: string
  capacity: number
}

export interface UpdateCourseScheduleRequest {
  startTime?: string
  endTime?: string
  capacity?: number
  status?: number
}

export const getCourseList = (params: CourseQueryParams = {}) => {
  return request.post('/course/listCourse', params) as Promise<Page<Course>>
}

export const getCourseDetail = (id: number | string) => {
  return request.get('/course/getCourseById', {
    params: { id: String(id) }
  }) as Promise<Course>
}

export const listSchedule = (params: CourseScheduleQueryParams) => {
  return request.post('/course/listSchedule', {
    ...params,
    courseId: params.courseId ? String(params.courseId) : undefined
  }) as Promise<Page<CourseSchedule>>
}

export const listAvailableSchedules = (courseId: number | string) => {
  return request.get('/course/listAvailableSchedules', {
    params: { courseId: String(courseId) }
  }) as Promise<CourseSchedule[]>
}

export const addSchedule = (data: AddCourseScheduleRequest) => {
  return request.post('/course/addSchedule', {
    ...data,
    courseId: String(data.courseId)
  }) as Promise<CourseSchedule>
}

export const updateSchedule = (id: number | string, data: UpdateCourseScheduleRequest) => {
  return request.post('/course/updateSchedule', data, {
    params: { id: String(id) }
  }) as Promise<boolean>
}

export const deleteSchedule = (id: number | string) => {
  return request.post('/course/deleteSchedule', null, {
    params: { id: String(id) }
  }) as Promise<boolean>
}
