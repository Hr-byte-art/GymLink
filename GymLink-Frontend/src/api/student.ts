import request from '@/utils/request'

// 学员接口类型
export interface Student {
  id: number | string // 支持大数字转字符串
  username: string
  name: string
  gender: number // 1:男 2:女
  phone: string
  avatar: string
  height: number // 身高(cm)
  weight: number // 体重(kg)
  balance: number // 账户余额
  createTime: string
}

// 更新学员信息请求参数
export interface UpdateStudentRequest {
  name?: string
  gender?: number
  phone?: string
  avatar?: string
  height?: number
  weight?: number
}

// 根据用户ID获取学员信息
export function getStudentByUserId(userId: string | number): Promise<Student> {
  return request.get('/student/getStudentByUserId', {
    params: { userId: String(userId) }
  }) as Promise<Student>
}

// 更新学员信息
export function updateStudent(id: string | number, data: UpdateStudentRequest): Promise<boolean> {
  console.log('updateStudent - id:', id, 'data:', data)
  return request.post(`/student/updateStudent?id=${id}`, data) as Promise<boolean>
}


// 上传学员头像
export function uploadStudentAvatar(studentId: string | number, file: File): Promise<string> {
  const formData = new FormData()
  formData.append('avatar', file)
  formData.append('studentId', String(studentId))
  return request.post('/student/updateStudentAvatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }) as Promise<string>
}

// 学员购买课程
export function purchaseCourse(studentId: string | number, courseId: string | number): Promise<boolean> {
  return request.get('/student/studentsPurchaseCourses', {
    params: {
      studentId: String(studentId),
      courseId: String(courseId)
    }
  }) as Promise<boolean>
}

// 已购课程信息
export interface PurchasedCourse {
  orderId: number
  orderNo: string
  courseId: number
  courseName: string
  courseImage: string
  courseType: string
  difficulty: string
  duration: number
  coachId: number
  coachName: string
  price: number
  purchaseTime: string
  status: number // 1:已支付 2:已退款
}

// 已购课程查询参数
export interface PurchasedCourseQueryParams {
  studentId: string | number
  pageNum?: number
  pageSize?: number
  courseName?: string
  status?: number
}

// 获取学员已购课程列表
export function getPurchasedCourses(params: PurchasedCourseQueryParams): Promise<{
  records: PurchasedCourse[]
  total: number
}> {
  return request.post('/student/getPurchasedCourses', {
    ...params,
    studentId: String(params.studentId)
  }) as Promise<{ records: PurchasedCourse[]; total: number }>
}

// 获取学员已购课程ID列表
export function getPurchasedCourseIds(studentId: string | number): Promise<number[]> {
  return request.get('/student/getPurchasedCourseIds', {
    params: { studentId: String(studentId) }
  }) as Promise<number[]>
}

// 课程退款
export function refundCourse(orderId: string | number): Promise<boolean> {
  return request.get('/student/refundCourse', {
    params: { orderId: String(orderId) }
  }) as Promise<boolean>
}

// 学员充值
export function studentTopUp(studentId: string | number, money: number): Promise<boolean> {
  return request.get('/student/studentTopUp', {
    params: { id: String(studentId), money: String(money) }
  }) as Promise<boolean>
}
