import request from '@/utils/request'

type UserQueryParams = Record<string, string | number | boolean | null | undefined>

export const userLogin = (params: UserQueryParams) => {
  return request.post('/user/login', null, {
    params
  }) as Promise<unknown>
}

export const userRegister = (params: UserQueryParams) => {
  return request.post('/user/userRegister', null, {
    params
  }) as Promise<unknown>
}

export const userLogout = () => {
  return request.post('/user/userLogout') as Promise<unknown>
}

export const getLoginUserInfo = () => {
  return request.post('/user/getLoginUserInfo') as Promise<unknown>
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  checkedPassword: string
}

export const changePassword = (data: ChangePasswordRequest) => {
  return request.post('/user/changePassword', data) as Promise<boolean>
}
