import request from '@/utils/request'

export interface Student {
  id: number | string
  username: string
  name: string
  gender: number
  phone: string
  avatar: string
  height: number
  weight: number
  balance: number
  createTime: string
}

export interface UpdateStudentRequest {
  name?: string
  gender?: number
  phone?: string
  avatar?: string
  height?: number
  weight?: number
}

export interface PurchasedCourse {
  orderId: number
  orderNo: string
  courseId: number
  courseName: string
  courseImage: string
  courseType: string
  deliveryMode?: number
  difficulty: string
  duration: number
  coachId: number
  coachName: string
  price: number
  totalSessions?: number
  remainingSessions?: number
  purchaseTime: string
  status: number
  studentName?: string
}

export interface PurchasedCourseQueryParams {
  studentId?: string | number
  pageNum?: number
  pageSize?: number
  courseName?: string
  courseId?: string | number
  coachId?: string | number
  status?: number
}

export function getStudentByUserId(userId: string | number): Promise<Student> {
  return request.get('/student/getStudentByUserId', {
    params: { userId: String(userId) }
  }) as Promise<Student>
}

export function updateStudent(id: string | number, data: UpdateStudentRequest): Promise<boolean> {
  return request.post(`/student/updateStudent?id=${String(id)}`, data) as Promise<boolean>
}

export function uploadStudentAvatar(studentId: string | number, file: File): Promise<string> {
  const formData = new FormData()
  formData.append('avatar', file)
  formData.append('studentId', String(studentId))
  return request.post('/student/updateStudentAvatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }) as Promise<string>
}

export function purchaseCourse(studentId: string | number, courseId: string | number): Promise<boolean> {
  return request.get('/student/studentsPurchaseCourses', {
    params: {
      studentId: String(studentId),
      courseId: String(courseId)
    }
  }) as Promise<boolean>
}

export function getPurchasedCourses(params: PurchasedCourseQueryParams): Promise<{
  records: PurchasedCourse[]
  total: number
}> {
  return request.post('/student/getPurchasedCourses', {
    ...params,
    studentId: params.studentId ? String(params.studentId) : undefined,
    courseId: params.courseId ? String(params.courseId) : undefined,
    coachId: params.coachId ? String(params.coachId) : undefined
  }) as Promise<{ records: PurchasedCourse[]; total: number }>
}

export function getPurchasedCourseIds(studentId: string | number): Promise<number[]> {
  return request.get('/student/getPurchasedCourseIds', {
    params: { studentId: String(studentId) }
  }) as Promise<number[]>
}

export function refundCourse(orderId: string | number): Promise<boolean> {
  return request.get('/student/refundCourse', {
    params: { orderId: String(orderId) }
  }) as Promise<boolean>
}

export function studentTopUp(studentId: string | number, money: number): Promise<boolean> {
  return request.get('/student/studentTopUp', {
    params: { id: String(studentId), money: String(money) }
  }) as Promise<boolean>
}
