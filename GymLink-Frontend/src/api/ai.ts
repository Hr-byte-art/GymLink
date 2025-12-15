/**
 * AI 聊天 API
 */

const BASE_URL = 'http://localhost:8080/api'

/**
 * 流式聊天接口 (SSE)
 * @param userId 用户ID
 * @param message 用户消息
 * @param onMessage 收到消息片段的回调
 * @param onError 错误回调
 * @param onComplete 完成回调
 */
export const chatStream = (
    userId: string | number,
    message: string,
    onMessage: (text: string) => void,
    onError?: (error: Error) => void,
    onComplete?: () => void
) => {
    const token = localStorage.getItem('token')
    const url = `${BASE_URL}/ai/chat/stream?userId=${userId}&message=${encodeURIComponent(message)}`

    const eventSource = new EventSource(url)

    eventSource.onmessage = (event) => {
        if (event.data) {
            onMessage(event.data)
        }
    }

    eventSource.onerror = (error) => {
        eventSource.close()
        if (onError) {
            onError(new Error('连接失败'))
        }
        if (onComplete) {
            onComplete()
        }
    }

    // 返回关闭函数
    return () => {
        eventSource.close()
    }
}

/**
 * 同步聊天接口
 * @param userId 用户ID
 * @param message 用户消息
 */
export const chat = async (userId: string | number, message: string): Promise<string> => {
    const token = localStorage.getItem('token')
    const response = await fetch(`${BASE_URL}/ai/chat?userId=${userId}&message=${encodeURIComponent(message)}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'GymLink': token || ''
        }
    })

    if (!response.ok) {
        throw new Error('请求失败')
    }

    return response.text()
}
