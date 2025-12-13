import { ElMessage } from 'element-plus'

// 统一的消息提示配置
const defaultDuration = 2000 // 默认显示2秒

export const message = {
    success(msg: string) {
        ElMessage.success({ message: msg, duration: defaultDuration, grouping: true })
    },
    error(msg: string) {
        ElMessage.error({ message: msg, duration: defaultDuration, grouping: true })
    },
    warning(msg: string) {
        ElMessage.warning({ message: msg, duration: defaultDuration, grouping: true })
    },
    info(msg: string) {
        ElMessage.info({ message: msg, duration: defaultDuration, grouping: true })
    }
}

export default message
