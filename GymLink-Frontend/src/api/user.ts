import request from '@/utils/request'

export const userLogin = (params: any) => {
    console.log('API userLogin - 接收到的参数:', params)
    return request.post('/user/login', null, {
        params: params
    }) as Promise<any>
}

export const userRegister = (params: any) => {
    return request.post('/user/userRegister', null, {
        params: params
    }) as Promise<any>
}

export const userLogout = () => {
    return request.post('/user/userLogout') as Promise<any>
}

// 新增：获取登录用户信息
export const getLoginUserInfo = () => {
    return request.post('/user/getLoginUserInfo') as Promise<any>
}

// 修改密码请求参数
export interface ChangePasswordRequest {
    oldPassword: string
    newPassword: string
    checkedPassword: string
}

// 修改密码
export const changePassword = (data: ChangePasswordRequest) => {
    return request.post('/user/changePassword', data) as Promise<boolean>
}