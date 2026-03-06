/**
 * AI chat API
 */

const BASE_URL = 'http://localhost:8080/api'

export const chatStream = (
  userId: string | number,
  message: string,
  onMessage: (text: string) => void,
  onError?: (error: Error) => void,
  onComplete?: () => void
) => {
  const url = `${BASE_URL}/ai/chat/stream?userId=${userId}&message=${encodeURIComponent(message)}`

  const eventSource = new EventSource(url)

  eventSource.onmessage = (event) => {
    if (event.data) {
      onMessage(event.data)
    }
  }

  eventSource.onerror = () => {
    eventSource.close()
    if (onError) {
      onError(new Error('连接失败'))
    }
    if (onComplete) {
      onComplete()
    }
  }

  return () => {
    eventSource.close()
  }
}

export const chat = async (userId: string | number, message: string): Promise<string> => {
  const token = localStorage.getItem('token')
  const response = await fetch(`${BASE_URL}/ai/chat?userId=${userId}&message=${encodeURIComponent(message)}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      GymLink: token || ''
    }
  })

  if (!response.ok) {
    throw new Error('请求失败')
  }

  return response.text()
}
