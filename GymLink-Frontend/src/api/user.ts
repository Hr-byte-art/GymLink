import request from '@/utils/request'

export const userLogin = (params: any) => {
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
