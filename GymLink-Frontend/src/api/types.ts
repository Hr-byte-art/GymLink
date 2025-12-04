// 通用分页响应类型 - 匹配后端 MyBatis Plus Page<T>
export interface Page<T> {
    records: T[] // 数据列表
    total: number // 总记录数
    size: number // 每页大小
    current: number // 当前页
    pages: number // 总页数
}

// 通用请求响应类型 - 匹配后端 BaseResponse<T>
export interface BaseResponse<T> {
    code: number
    data: T
    message: string
}
