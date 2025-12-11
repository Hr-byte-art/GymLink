import request from '@/utils/request'
import type { Page } from './types'

// 课程评价接口类型
export interface CourseReview {
    id: number
    studentId: number
    studentName: string
    studentAvatar: string
    courseId: number
    courseName: string
    coachId: number
    rating: number
    content: string
    createTime: string
}

// 添加评价请求
export interface AddReviewRequest {
    studentId: string | number
    courseId?: string | number
    appointmentId?: string | number
    reviewType?: number // 1:课程评价 2:预约评价
    rating: number
    content?: string
}

// 评价查询参数
export interface ReviewQueryParams {
    pageNum?: number
    pageSize?: number
    coachId?: string | number
    courseId?: string | number
    studentId?: string | number
    rating?: number
}

// 教练评价统计
export interface CoachReviewStats {
    avgRating: number
    reviewCount: number
    studentCount: number
    courseCount: number
    ratingDistribution: Record<number, number>
}

// 添加评价（支持课程评价和预约评价）
export function addCourseReview(data: AddReviewRequest): Promise<CourseReview> {
    return request.post('/review/add', {
        ...data,
        studentId: Number(data.studentId),
        courseId: data.courseId ? Number(data.courseId) : undefined,
        appointmentId: data.appointmentId ? Number(data.appointmentId) : undefined,
        reviewType: data.reviewType || 1
    }) as Promise<CourseReview>
}

// 分页查询评价列表
export function getReviewList(params: ReviewQueryParams = {}): Promise<Page<CourseReview>> {
    return request.post('/review/list', {
        ...params,
        coachId: params.coachId ? Number(params.coachId) : undefined,
        courseId: params.courseId ? Number(params.courseId) : undefined,
        studentId: params.studentId ? Number(params.studentId) : undefined
    }) as Promise<Page<CourseReview>>
}

// 检查是否可以评价
export function canReviewCourse(studentId: string | number, courseId: string | number): Promise<boolean> {
    return request.get('/review/canReview', {
        params: {
            studentId: String(studentId),
            courseId: String(courseId)
        }
    }) as Promise<boolean>
}

// 获取教练评价统计
export function getCoachReviewStats(coachId: string | number): Promise<CoachReviewStats> {
    return request.get('/review/coachStats', {
        params: { coachId: String(coachId) }
    }) as Promise<CoachReviewStats>
}

// 检查是否可以评价预约
export function canReviewAppointment(studentId: string | number, appointmentId: string | number): Promise<boolean> {
    return request.get('/review/canReviewAppointment', {
        params: {
            studentId: String(studentId),
            appointmentId: String(appointmentId)
        }
    }) as Promise<boolean>
}
