import request from '@/utils/request'
import type { Page } from './types'

// 课程接口类型 - 匹配后端 CourseVo
export interface Course {
  id: number
  name: string
  coachId: number
  image: string
  description: string
  price: number
  duration: number // 分钟
  difficulty: string
  createTime: string
}

// 课程查询参数 - 匹配后端 CourseQueryPageRequest
export interface CourseQueryParams {
  current?: number // 当前页
  pageSize?: number // 每页大小
  name?: string // 课程名称搜索
  difficulty?: string // 难度等级
  coachId?: number // 教练ID
  sortField?: string
  sortOrder?: string
}

// 获取课程列表 - POST /course/listCourse
export const getCourseList = (params: CourseQueryParams = {}) => {
  return request.post('/course/listCourse', params) as Promise<Page<Course>>
}

// 获取课程详情 - GET /course/getCourseById
export const getCourseDetail = (id: number) => {
  return request.get('/course/getCourseById', {
    params: { id }
  }) as Promise<Course>
}