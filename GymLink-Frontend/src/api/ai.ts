/**
 * AI chat API
 */

const BASE_URL = 'http://localhost:8080/api'

export const chatStream = (
  message: string,
  onMessage: (text: string) => void,
  onError?: (error: Error) => void,
  onComplete?: () => void
) => {
  const token = localStorage.getItem('token')
  const controller = new AbortController()
  const decoder = new TextDecoder('utf-8')

  ;(async () => {
    try {
      const response = await fetch(`${BASE_URL}/ai/chat/stream?message=${encodeURIComponent(message)}`, {
        method: 'GET',
        headers: {
          GymLink: token || ''
        },
        signal: controller.signal
      })

      if (!response.ok || !response.body) {
        throw new Error('连接失败')
      }

      const reader = response.body.getReader()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          break
        }

        buffer += decoder.decode(value, { stream: true })
        const events = buffer.split('\n\n')
        buffer = events.pop() || ''

        for (const event of events) {
          const lines = event.split('\n')
          for (const line of lines) {
            if (line.startsWith('data:')) {
              const data = line.slice(5).trimStart()
              if (data) {
                onMessage(data)
              }
            }
          }
        }
      }

      const tail = decoder.decode()
      if (tail) {
        buffer += tail
      }

      if (buffer.trim()) {
        const lines = buffer.split('\n')
        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.slice(5).trimStart()
            if (data) {
              onMessage(data)
            }
          }
        }
      }

      if (onComplete) {
        onComplete()
      }
    } catch (error) {
      if (controller.signal.aborted) {
        if (onComplete) {
          onComplete()
        }
        return
      }
      if (onError) {
        onError(error instanceof Error ? error : new Error('连接失败'))
      }
      if (onComplete) {
        onComplete()
      }
    }
  })()

  return () => {
    controller.abort()
  }
}

export const chat = async (message: string): Promise<string> => {
  const token = localStorage.getItem('token')
  const response = await fetch(`${BASE_URL}/ai/chat?message=${encodeURIComponent(message)}`, {
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
