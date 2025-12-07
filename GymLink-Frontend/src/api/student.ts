import request from '@/utils/request'

// 学员接口类型
export interface Student {
  id: number
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
